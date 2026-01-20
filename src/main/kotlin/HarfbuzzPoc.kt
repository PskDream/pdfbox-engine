package org.example

import com.ibm.icu.text.BreakIterator
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.util.Matrix
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.io.File
import java.io.FileInputStream
import java.util.Locale

fun main() {
    val userHome = System.getProperty("user.home")
    // ** ปรับ Path ให้ตรงกับเครื่องคุณ **
    val fontPath = "$userHome/Library/Fonts/Sarabun/Sarabun-Regular.ttf"
    val fontFile = File(fontPath)

    if (!fontFile.exists()) {
        println("หาไฟล์ฟอนต์ไม่เจอ: $fontPath")
        return
    }

    val fontSize = 20f

    // ข้อความทดสอบ (ยาวๆ และมีสระซับซ้อน)
    val longText = "สิทธิ์ในการใช้น้ำที่นี่สำคัญมาก ปู๊นปู๊น รถไฟจะไปโคราชตากลม " +
            "ฎู ดำๆ ฎู การจัดการ Glyph ใน PDFBox ผสานพลังกับ ICU4J " +
            "ช่วยให้เราตัดคำไทยได้แม่นยำ และป้องกันปัญหาสระจมวรรณยุกต์ลอย" +
            "ได้อย่างมีประสิทธิภาพสูงสุด ปี่ ป่น น้ำใจ ญี่ปุ่น"

    PDDocument().use { document ->
        val page = PDPage()
        document.addPage(page)

        // 1. โหลด Font แบบ Full Embed (embedSubset = false) เพื่อให้ใช้ drawRawGlyph ได้
        val pdFont = PDType0Font.load(document, FileInputStream(fontFile), false)

        // 2. โหลด Java AWT Font เพื่อใช้คำนวณตำแหน่ง Glyph (สระ/วรรณยุกต์)
        val javaFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(fontSize)

        PDPageContentStream(document, page).use { cs ->
            drawSmartThaiText(
                cs = cs,
                pdFont = pdFont,
                javaFont = javaFont,
                fontSize = fontSize,
                startX = 50f,
                startY = 700f,
                maxWidth = 400f, // กำหนดความกว้างของคอลัมน์
                text = longText
            )
        }
        document.save(File("final_thai_wrap_and_glyph.pdf"))
        println("สร้างไฟล์สำเร็จ! (Word Wrap + Glyph Positioning)")
    }
}

/**
 * ฟังก์ชันหลัก: ตัดคำด้วย ICU และวาดด้วย AWT GlyphVector
 */
fun drawSmartThaiText(
    cs: PDPageContentStream,
    pdFont: PDType0Font,
    javaFont: Font,
    fontSize: Float,
    startX: Float,
    startY: Float,
    maxWidth: Float,
    text: String
) {
    var currentX = startX
    var currentY = startY
    val lineHeight = fontSize * 1.5f
    val frc = FontRenderContext(null, true, true)

    // 1. ตัดคำภาษาไทยด้วย ICU4J
    val words = splitThaiText(text)

    for (word in words) {
        // สร้าง GlyphVector ของคำนั้นๆ เพื่อหาความกว้างที่แท้จริง
        val gv: GlyphVector = javaFont.layoutGlyphVector(
            frc, word.toCharArray(), 0, word.length, Font.LAYOUT_LEFT_TO_RIGHT
        )

        // ความกว้างของคำนี้ (คำนวณจาก Glyph Vector จะแม่นยำกว่า getStringWidth)
        val wordWidth = gv.logicalBounds.width.toFloat()

        // 2. เช็คการขึ้นบรรทัดใหม่
        // ถ้าเขียนคำนี้แล้วเกินขอบขวา และไม่ใช่คำแรกของบรรทัด -> ขึ้นบรรทัดใหม่
        if (currentX + wordWidth > startX + maxWidth && currentX > startX) {
            currentX = startX
            currentY -= lineHeight
        }

        // 3. วาดคำนั้นลงไป (ทีละ Glyph ตามตำแหน่งเป๊ะๆ)
        drawGlyphs(cs, pdFont, gv, currentX, currentY, fontSize)

        // ขยับเคอร์เซอร์ไปทางขวา เพื่อเตรียมเขียนคำต่อไป
        currentX += wordWidth
    }
}


/**
 * วาด GlyphVector ลงใน Stream
 */
private fun drawGlyphs(
    cs: PDPageContentStream,
    pdFont: PDType0Font,
    gv: GlyphVector,
    startX: Float,
    startY: Float,
    fontSize: Float
) {
    for (i in 0 until gv.numGlyphs) {
        val glyphId = gv.getGlyphCode(i)
        val pos = gv.getGlyphPosition(i)
        val gx = startX + pos.x
        val gy = startY - pos.y

        cs.beginText()
        cs.setFont(pdFont, fontSize)
        cs.setTextMatrix(Matrix.getTranslateInstance(gx.toFloat(), gy.toFloat()))

        val unicode = pdFont.toUnicode(glyphId)

        if (unicode != null) {
            cs.showText(unicode)
        } else {
            // ใช้ท่าไม้ตาย appendRawCommands (ไม่ต้องแงะ Reflection)
            val hex = String.format("%04X", glyphId)
            cs.appendRawCommands("<$hex> Tj ")
        }
        cs.endText()
    }
}

/**
 * ฟังก์ชันตัดคำไทยด้วย ICU
 * (ยกมาจาก Snippet 2)
 */
fun splitThaiText(text: String): List<String> {
    val words = mutableListOf<String>()
    val boundary = BreakIterator.getWordInstance(Locale.forLanguageTag("th"))
    boundary.setText(text)

    var start = boundary.first()
    var end = boundary.next()

    while (end != BreakIterator.DONE) {
        words.add(text.substring(start, end))
        start = end
        end = boundary.next()
    }
    return words
}