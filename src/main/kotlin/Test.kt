package org.example


import com.ibm.icu.text.BreakIterator
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.util.Matrix
import java.io.File
import java.util.Locale

fun main() {
    val userHome = System.getProperty("user.home")
    val fontPath = "$userHome/Library/Fonts/Sarabun/Sarabun-Regular.ttf"
    PDDocument().use { document ->
        val page = PDPage()
        document.addPage(page)
        val font = PDType0Font.load(document, File(fontPath))

        PDPageContentStream(document, page).use { cs ->
            val longText = "สิทธิ์ในการใช้น้ำที่นี่สำคัญมาก ปู๊นปู๊น รถไฟจะไปโคราชตา ฎู ดำๆ ฎู" +
                    "การจัดการ Glyph ใน PDFBox ด้วย ICU4J ช่วยให้เราตัดคำไทยได้แม่นยำ " +
                    "และป้องกันปัญหาสระจมวรรณยุกต์ลอยได้อย่างมีประสิทธิภาพสูงสุด ปี่ ป่น "

            // เรียกใช้ฟังก์ชัน Warp Text
            drawWarpText(
                cs = cs,
                font = font,
                fontSize = 20f,
                startX = 50f,
                startY = 700f,
                maxWidth = 400f, // กำหนดความกว้างสูงสุดของบรรทัด
                text = longText
            )
        }
        document.save(File("test_warp_thai.pdf"))
        println("สร้างไฟล์ Warp Text สำเร็จ!")
    }
}

/**
 * ฟังก์ชันหลักสำหรับตัดคำและวาดข้อความพร้อมแก้สระจม
 */
fun drawWarpText(
    cs: PDPageContentStream,
    font: PDType0Font,
    fontSize: Float,
    startX: Float,
    startY: Float,
    maxWidth: Float,
    text: String
) {
    var currentX = startX
    var currentY = startY
    val lineHeight = fontSize * 1.5f // ระยะห่างระหว่างบรรทัด

    // 1. ตัดคำด้วย ICU4J
    val words = splitThaiText(text)

    for (word in words) {
        // 2. คำนวณความกว้างของคำปัจจุบัน
        val wordWidth = font.getStringWidth(word) / 1000f * fontSize

        // 3. ถ้าคำนี้วางแล้วเกิน maxWidth ให้ขึ้นบรรทัดใหม่ (ยกเว้นขึ้นบรรทัดใหม่แล้วคำยังยาวกว่า maxWidth)
        if (currentX + wordWidth > startX + maxWidth && currentX > startX) {
            currentX = startX
            currentY -= lineHeight
        }

        for (i in word.indices) {
            val char = word[i]
            val prevChar = if (i > 0) word[i - 1] else null
            val prevPrevChar = if (i > 1) word[i - 2] else null
            val nextChar = if (i < word.length - 1) word[i + 1] else null
            val type = com.ibm.icu.lang.UCharacter.getType(char.code)

            val isNonSpacing = type == com.ibm.icu.lang.UCharacterCategory.NON_SPACING_MARK.toInt() ||
                    type == com.ibm.icu.lang.UCharacterCategory.COMBINING_SPACING_MARK.toInt()

            // เช็คว่าพยัญชนะตัวก่อนหน้าหางยาวไหม
            val isTallChar = prevChar == 'ป' || prevChar == 'ฝ' || prevChar == 'ฟ'
            val isLowerVowel = prevChar in '\u0E38'..'\u0E39'
            println("Char: $char, Prev: $prevChar, TallPrev: $isTallChar")

            var offsetY = 0f

            when (char) {
                in '\u0E48'..'\u0E4C' -> { // วรรณยุกต์ (่ ้ ๊ ๋ ์)
                    val isPrevUpperVowel = prevChar in '\u0E31'..'\u0E37'
                    offsetY = when {
                        isPrevUpperVowel -> fontSize * 0.3f  // อยู่บนสระบน ยกสูงพิเศษ
                        isTallChar -> fontSize * 0.25f       // อยู่บน ป, ฝ, ฟ ยกหลบหาง
                        isLowerVowel -> {
                            if (prevPrevChar == 'ป' || prevChar == 'ฝ' || prevChar == 'ฟ') {
                                fontSize * 0.15f               // อยู่บนสระล่างที่มีหางยาวหลบหาง
                            } else {
                                fontSize * 0.10f               // อยู่บนสระล่างปกติ
                            }
                        }

                        else -> {
                            if (nextChar == '\u0E33') {
                                fontSize * 0.23f               // กรณีไม้วรรณยุกต์อยู่บนสระอำ
                            } else {
                                fontSize * 0f               // อยู่บนพยัญชนะปกติ
                            }
                        }
                    }
                }

                in '\u0E31'..'\u0E37', '\u0E47' -> { // สระบน (ิ ี ึ ื ั ็)
                    offsetY = if (isTallChar) fontSize * 0.15f else 0f // หลบหาง ป, ฝ, ฟ เล็กน้อยพอสวยงาม
                }

                in '\u0E38'..'\u0E39' -> { // สระล่าง (ุ ู)
                    // if ฎ ฏ
                    offsetY =
                        if (prevChar == 'ฎ' || prevChar == 'ฏ')
                            -fontSize * 0.2f
                        else -fontSize * 0.05f // ดันลงข้างล่างเล็กน้อยให้ไม่ชิดตัวอักษรเกินไป
                }
            }

            cs.beginText()
            cs.setFont(font, fontSize)
            cs.setTextMatrix(Matrix.getTranslateInstance(currentX, currentY + offsetY))
            cs.showText(char.toString())
            cs.endText()

            if (!isNonSpacing) {
                currentX += (font.getStringWidth(char.toString()) / 1000f) * fontSize
            }
        }
    }
}

//fun splitThaiText(text: String): List<String> {
//    val words = mutableListOf<String>()
//    val boundary = BreakIterator.getWordInstance(Locale.forLanguageTag("th"))
//    boundary.setText(text)
//
//    var start = boundary.first()
//    var end = boundary.next()
//
//    while (end != BreakIterator.DONE) {
//        words.add(text.substring(start, end))
//        start = end
//        end = boundary.next()
//    }
//    return words
//}