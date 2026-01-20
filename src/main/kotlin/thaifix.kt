package org.example

import org.apache.pdfbox.pdmodel.font.PDType0Font

fun fixThaiGlyphs(text: String): String {
    val result = StringBuilder()

    for (i in text.indices) {
        val char = text[i]
        val prev = if (i > 0) text[i - 1] else null
        val prevPrev = if (i > 1) text[i - 2] else null

        // ตรวจสอบพยัญชนะที่มีหาง (ป, ฝ, ฟ)
        val isTallChar = prev == 'ป' || prev == 'ฝ' || prev == 'ฟ'

        val fixedChar = when (char) {
            // 1. จัดการ "วรรณยุกต์" เมื่ออยู่บน "สระบน" (เช่น สิทธิ์, ที่)
            in '\u0E48'..'\u0E4C' -> { // ่ ้ ๊ ๋ ์
                if (prev in '\u0E31'..'\u0E37') { // ถ้าข้างล่างเป็นสระบน (ิ ี ึ ื ั)
                    // เปลี่ยนเป็นวรรณยุกต์ตัวสูง (PUA Mapping สำหรับฟอนต์มาตรฐาน)
                    (char.toInt() + 0x0060).toChar()
                } else if (isTallChar) {
                    // ถ้าพยัญชนะหางยาว ให้ขยับหลบหาง (บางฟอนต์ใช้รหัสต่างกัน)
                    (char.toInt() + 0x0050).toChar()
                } else {
                    char
                }
            }

            // 2. จัดการ "สระบน" เมื่ออยู่บนพยัญชนะหางยาว (เช่น ปี่, ฟิล์ม)
            in '\u0E31'..'\u0E37' -> {
                if (isTallChar) {
                    (char.toInt() + 0x0010).toChar() // หลบหาง ป, ฝ, ฟ
                } else {
                    char
                }
            }

            // 3. จัดการ "น้ำ" (สระอำ + ไม้โท)
            '\u0E33' -> { // สระอำ
                if (prev in '\u0E48'..'\u0E4B') {
                    // สลับลำดับให้ไม้โทอยู่ก่อนสระอำเพื่อให้ Glyph ทำงานถูก
                    // หรือใช้การแทนที่รหัสพิเศษถ้าฟอนต์รองรับ
                    char
                } else {
                    char
                }
            }

            else -> char
        }
        result.append(fixedChar)
    }
    return result.toString()
}