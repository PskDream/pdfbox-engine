package pdfengin

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.awt.Font
import java.io.File
import java.io.FileInputStream

// 1. Enum สำหรับระบุสไตล์
enum class FontStyle {
    REGULAR, BOLD, ITALIC, BOLD_ITALIC
}

// เปลี่ยนชื่อ Class ให้สื่อความหมาย (เก็บได้ทั้ง Type0 และ Type1)
// เก็บข้อมูลฟอนต์ พร้อมการตั้งค่าว่า "ตัวนี้ต้องจัดสระไหม?"
data class FontPair(
    val pdFont: PDFont,
    val awtFont: Font?, // <--- เปลี่ยนเป็น Nullable (มีค่าเฉพาะตอน useShaping = true)
    val useShaping: Boolean
)

class FontManager(private val document: PDDocument) {

    // เปลี่ยน Value Type เป็น PDFont (Class แม่)
    private val fontFamilies = mutableMapOf<String, MutableMap<FontStyle, FontPair>>()

    /**
     * ลงทะเบียน Custom Font (เช่น Sarabun.ttf) สำหรับภาษาไทย
     * (Logic เดิม)
     */
    fun registerCustomFont(
        name: String, style: FontStyle, file: File,
        useShaping: Boolean = true // Default = true สำหรับ Custom Font (มักเป็นไทย)
    ) {
        // ถ้าไม่ shape ก็ปล่อย true ได้ (ประหยัดไฟล์)
        // แต่ถ้า shape ต้อง false (เพื่อเอา raw glyph)
        val embedSubset = !useShaping
        val pdFont = PDType0Font.load(document, FileInputStream(file), embedSubset)

        // *** OPTIMIZATION: โหลด AWT เฉพาะตอนจะทำ Shaping ***
        val awtFont = if (useShaping) Font.createFont(Font.TRUETYPE_FONT, file) else null

        storeFont(name, style, pdFont, awtFont, useShaping)
    }

    /**
     * ลงทะเบียน Standard Font (เช่น Helvetica) สำหรับภาษาอังกฤษ
     * (Logic ใหม่)
     */
    fun registerStandardFont(
        name: String, style: FontStyle, stdName: Standard14Fonts.FontName
    ) {
        val pdFont = PDType1Font(stdName)
        // Standard Font ไม่ทำ Shaping อยู่แล้ว -> awtFont = null เสมอ
        storeFont(name, style, pdFont, null, false)
    }

    private fun storeFont(name: String, style: FontStyle, pdFont: PDFont, awtFont: Font?, useShaping: Boolean) {
        fontFamilies.computeIfAbsent(name) { mutableMapOf() }[style] = FontPair(pdFont, awtFont, useShaping)
    }

    fun getFont(familyName: String, style: FontStyle): FontPair {
        val family = fontFamilies[familyName]
            ?: throw IllegalArgumentException("Font Family '$familyName' not registered.")
        return family[style] ?: family[FontStyle.REGULAR]
        ?: throw IllegalArgumentException("Style $style not found for $familyName")
    }
}