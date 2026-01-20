package org.example

import org.apache.fontbox.encoding.Encoding
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.*
import java.io.File
import java.text.BreakIterator
import java.util.*

data class PdfFontFamily(
    val regular: PDFont,
    val bold: PDFont? = null,
    val italic: PDFont? = null,
    val boldItalic: PDFont? = null
)

class AiText(private val mediaBox: PDRectangle = PDRectangle.A4) : PDDocument() {
    private var currentPage: PDPage? = null
    private var contentStream: PDPageContentStream? = null

    private var defaultSize = 12f
    private val defaultSpacing = 1.5f

    private var defaultFontFamily: PdfFontFamily = PdfFontFamily(
        regular = PDType1Font(Standard14Fonts.FontName.HELVETICA),
        bold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
        italic = PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE),
        boldItalic = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE)
    )
    private var defaultFont: PDFont = defaultFontFamily.regular

    // ขอบหน้ากระดาษ
    private var marginTop = 50f
    private var marginBottom = 50f
    private var marginLeft = 50f
    private var marginRight = 50f

    // ตำแหน่ง Y ปัจจุบัน
    private var currentY = mediaBox.height - marginTop
    private val maxWidth = mediaBox.width - marginLeft - marginRight

    init {
        newPage()
    }

    private fun newPage() {
        contentStream?.close()
        currentPage = PDPage(mediaBox)
        addPage(currentPage)
        contentStream = PDPageContentStream(this, currentPage)
        currentY = mediaBox.height - marginTop
    }

    fun writeText(text: String, x: Float = marginLeft, size: Float = defaultSize, wrapText: Boolean = true) {
        val lineHeight = size * defaultSpacing
        val lines = if (wrapText) wrapText(text, defaultFont, size, maxWidth) else listOf(text)

        lines.forEach { line ->
            currentY -= lineHeight
            if (currentY - lineHeight < marginBottom) { newPage() }

            contentStream?.let { stream ->
                stream.beginText()
                stream.setFont(defaultFont, size)
                stream.newLineAtOffset(x, currentY)

                // แก้ไขตรงนี้: แทนที่จะ showText ตรงๆ
                // ให้ใช้การจัดการ Text Shape ก่อน (ถ้าทำได้)
                // หรือใช้ฟอนต์ที่แก้เรื่อง "จม" มาแล้วในตัวอย่าง Sarabun ของ Google Fonts
                stream.showText(line)
                stream.endText()
            }
        }
    }

    private fun wrapText(
        text: String,
        font: PDFont,
        fontSize: Float,
        maxWidth: Float
    ): List<String> {
        val iterator = BreakIterator.getWordInstance(Locale.forLanguageTag("th-TH"))
        iterator.setText(text)
        val lines = mutableListOf<String>()
        var currentLine = ""
        var start = iterator.first()
        var end = iterator.next()

        while (end != BreakIterator.DONE) {
            val word = text.substring(start, end)
            if (word.isNotEmpty()) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine$word"
                println(testLine)
                val width = font.getStringWidth(testLine) / 1000 * fontSize

                if (width > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                        currentLine = word
                    } else {
                        lines.add(word)
                        currentLine = ""
                    }
                } else {
                    currentLine = testLine
                }
            }
            start = end
            end = iterator.next()
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    fun setFontFamily(regularName: Standard14Fonts.FontName,
                      boldName: Standard14Fonts.FontName? = null,
                      italicName: Standard14Fonts.FontName? = null,
                      boldItalic: Standard14Fonts.FontName? = null) {
        this.defaultFontFamily = PdfFontFamily(
            regular = PDType1Font(regularName),
            bold = boldName?.let { PDType1Font(it) },
            italic = italicName?.let { PDType1Font(it) },
            boldItalic = boldItalic?.let { PDType1Font(it) }
        )
        this.defaultFont = defaultFontFamily.regular
    }

    fun loadFontFamily(
        regularPath: String,
        boldPath: String? = null,
        italicPath: String? = null,
        boldItalicPath: String? = null
    ) {
        this.defaultFontFamily = PdfFontFamily(

            regular = PDType0Font.load(this, File(regularPath)),
            bold = boldPath?.let { PDType0Font.load(this, File(it)) },
            italic = italicPath?.let { PDType0Font.load(this, File(it)) },
            boldItalic = boldItalicPath?.let { PDType0Font.load(this, File(it)) }
        )
        this.defaultFont = defaultFontFamily.regular // default เป็นตัวปกติ
    }

    fun setSize(size: Float) {
        defaultSize = size
    }

    fun setStyle(bold: Boolean = false, italic: Boolean = false) {
        val family = defaultFontFamily
        defaultFont = when {
            bold && italic -> family.boldItalic ?: family.bold ?: family.regular
            bold -> family.bold ?: family.regular
            italic -> family.italic ?: family.regular
            else -> family.regular
        }
    }

    fun setMargin(
        top: Float = marginTop,
        bottom: Float = marginBottom,
        left: Float = marginLeft,
        right: Float = marginRight
    ) {
        marginRight = right
        marginBottom = bottom
        marginLeft = left
        marginTop = top
    }

    override fun save(fileName: String) {
        contentStream?.close()
        super.save(fileName)
    }

    override fun close() {
        contentStream?.close()
        super.close()
    }
}

// วิธีใช้งาน
fun main() {
    AiText().use { doc ->
        // เขียนหลายบรรทัด
        val userHome = System.getProperty("user.home")
        doc.loadFontFamily(
            regularPath = "$userHome/Library/Fonts/Sarabun/Sarabun-Regular.ttf",
            boldPath = "$userHome/Library/Fonts/Sarabun/Sarabun-Bold.ttf",
            italicPath = "$userHome/Library/Fonts/Sarabun/Sarabun-Italic.ttf"
        )

        doc.setSize(16f)

        doc.setStyle(bold = true)
        repeat(20) { i ->
            doc.writeText("Line ${i + 1}: This is a sample text")
        }

        doc.setStyle(italic = true)
        doc.writeText(
            "Another paragraph with more content. This demonstrates how the automatic " +
                    "page break works when you have a lot of text content.", size = 10f
        )

        doc.setStyle(bold = true, italic = true)
        doc.writeText("Line This is a sample text", size = 10f)

        doc.setStyle()
        doc.writeText("แอบมองเหม่อมองย้อนกลับรู้สึกอยากจะไม่ยอมให้ฉันทำให้เธอ ไม่ว่าจะยากลำบากมากมายสักแค่ไหนก็ดีเฮอีกที จะเป็นเรื่องร้ายๆผ่านมาเต้นกันจนหมดแรง จะทำอะไรดีนะนิดๆกังวลไม่เหมือนที่ง่ายเลย ปล่อยใจไปให้เธอเอาไว้ไม่เคยจาง อย่ามัวลังเลปล่อยไปสิแล้วลองดูสักแค่เพียงเชื่อมั่นและครั้งท้องฟ้าก็ยังคงอยู่ จากจุดหมายที่ไหลมาหยุดเวลาที่ผ่านไป")

        doc.save("output.pdf")
    }


    println("PDF created successfully!")
}