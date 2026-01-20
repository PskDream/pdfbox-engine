package pdfengin

//import com.ibm.icu.text.BreakIterator
import java.text.BreakIterator
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.util.Matrix
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.util.Locale

class AdvancedPdfEngine(val document: PDDocument, private val mediaBox: PDRectangle = PDRectangle.A4) : AutoCloseable {

    private var breakIteratorCache: ThreadLocal<BreakIterator>? = null

    val fontManager = FontManager(document)
    private val frc = FontRenderContext(AffineTransform(), true, true)

    // ขอบหน้ากระดาษ
    private var marginTop = 50f
    private var marginBottom = 50f
    private var marginLeft = 50f
    private var marginRight = 50f

    private var currentPage: PDPage? = null
    private var contentStream: PDPageContentStream? = null
    private var currentY = mediaBox.height - marginTop

    private var defaultFontPair: FontPair? = null
    private var defaultFontSize = 14f

    init {
        addNewPage()
    }

    /**
     * Initializes a ThreadLocal BreakIterator instance for the specified locale.
     * If the BreakIterator has already been initialized, an exception is thrown.
     *
     * @param locale The locale for which the BreakIterator should be created.
     * @throws IllegalStateException If the BreakIterator is already initialized.
     */
    fun setBreakIterator(locale: Locale) {
        if (breakIteratorCache == null) {
            this.breakIteratorCache = ThreadLocal.withInitial { BreakIterator.getWordInstance(locale) }
        } else {
            throw IllegalStateException("BreakIterator already initialized.")
        }
    }


    /**
     * Calculates and returns the maximum width available for content within the current media box,
     * accounting for the left and right margins.
     *
     * @return the maximum width available for rendering content as a Float
     */
    fun getAvailableWidth(): Float {
        return mediaBox.width - marginLeft - marginRight
    }


    /**
     * Writes a string of text at the specified horizontal position on the current PDF page.
     * Depending on the font configuration, it uses either glyph-based precise rendering
     * or standard native text rendering.
     *
     * @param text The text to be written onto the PDF.
     * @param x The x-coordinate where the text starts. Defaults to the left margin.
     */
    fun writeText(text: String, x: Float = marginLeft) {
        renderText(text, x, currentY, this.getFontPair(), defaultFontSize)
    }

    /**
     * Writes a line of text onto the current page at the specified position, wrapping it if necessary,
     * and moves the cursor to the next line based on the specified line height factor.
     *
     * @param text The text to be written.
     * @param x The x-coordinate where the text starts. Defaults to the left margin.
     * @param lineHeightFactor The factor to calculate the spacing between lines relative to the default font size. Defaults to 1.5.
     * @param wrapText Whether to wrap the text if it exceeds the specified maximum width. Defaults to false.
     * @param maxWidth The maximum width allowed for the text before wrapping occurs. Defaults to the calculated maximum width of the page.
     * @throws IllegalStateException If a font has not been set prior to invoking this method.
     */
    fun writeLine(
        text: String,
        x: Float = marginLeft,
        lineHeightFactor: Float = 1.5f,
        wrapText: Boolean = false,
        maxWidth: Float = getAvailableWidth()
    ) {
        val fontPair = this.getFontPair()
        val pdFont = fontPair.pdFont
        val lineHeight = defaultFontSize * lineHeightFactor

        val lines = if (wrapText) wrapText(text, pdFont, defaultFontSize, maxWidth) else listOf(text)

        lines.forEach { line ->
            if (currentY - lineHeight < marginBottom) {
                addNewPage()
            }
            renderText(line, x, currentY, fontPair, defaultFontSize)
            currentY -= lineHeight
        }

    }

    fun newLine(lineHeightFactor: Float = 1.5f) {
        val lineHeight = defaultFontSize * lineHeightFactor
        currentY -= lineHeight
    }

    fun setStyle(familyName: String, style: FontStyle = FontStyle.REGULAR) {
        this.defaultFontPair = fontManager.getFont(familyName, style)
    }

    fun setSize(size: Float) {
        this.defaultFontSize = size
    }

    /**
     * Draws a table on the PDF with the specified structure, data, and formatting options.
     *
     * @param headers List of header cell values for the table columns
     * @param rows List of rows, where each row is a list of cell values
     * @param cellWidth Width of each cell. If not specified, calculates equally based on available width
     * @param cellHeight Height of each cell (in points)
     * @param borderColor RGB color for table borders (default: black)
     * @param headerBackgroundColor RGB color for header background (default: light gray)
     * @param alternateRowColor Optional RGB color for alternating row backgrounds
     * @throws IllegalStateException If font has not been set
     */
    fun drawTable(
        headers: List<String>,
        rows: List<List<String>>,
        cellWidth: Float? = null,
        cellHeight: Float = 30f,
        borderColor: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
        headerBackgroundColor: Triple<Float, Float, Float> = Triple(0.8f, 0.8f, 0.8f),
        alternateRowColor: Triple<Float, Float, Float>? = null,
        borderWidth: Float = 1f
    ) {
        val fontPair = this.getFontPair()
        val numColumns = headers.size
        val calculatedCellWidth = cellWidth ?: (getAvailableWidth() / numColumns)

        var tableStartY = currentY

        val cs = contentStream ?: return

        // Draw header row
        drawTableRow(
            cs, headers, marginLeft, tableStartY, calculatedCellWidth, cellHeight,
            borderColor, headerBackgroundColor, borderWidth, fontPair
        )

        tableStartY -= cellHeight
        currentY -= cellHeight

        // Draw data rows
        rows.forEachIndexed { rowIndex, row ->
            // Check if we need a new page
            if (tableStartY - cellHeight < marginBottom) {
                addNewPage()
                tableStartY = currentY
            }

            val rowBackgroundColor = if (alternateRowColor != null && rowIndex % 2 == 1) {
                alternateRowColor
            } else {
                null
            }

            drawTableRow(
                cs, row, marginLeft, tableStartY, calculatedCellWidth, cellHeight,
                borderColor, rowBackgroundColor, borderWidth, fontPair
            )

            tableStartY -= cellHeight
            currentY -= cellHeight
        }
    }

    /**
     * Helper function to draw a single row of the table
     *
     * @param cs The content stream to draw on
     * @param cells List of cell values for this row
     * @param startX Starting x-coordinate for the row
     * @param startY Starting y-coordinate for the row
     * @param cellWidth Width of each cell
     * @param cellHeight Height of the cell
     * @param borderColor RGB color for borders
     * @param backgroundColor Optional RGB color for cell background
     * @param borderWidth Width of the border line
     * @param fontPair The font pair to use for rendering text
     */
    private fun drawTableRow(
        cs: PDPageContentStream,
        cells: List<String>,
        startX: Float,
        startY: Float,
        cellWidth: Float,
        cellHeight: Float,
        borderColor: Triple<Float, Float, Float>,
        backgroundColor: Triple<Float, Float, Float>?,
        borderWidth: Float,
        fontPair: FontPair
    ) {
        cells.forEachIndexed { index, cellText ->
            val cellX = startX + (index * cellWidth)
            val cellY = startY - cellHeight

            // Draw background if specified
            if (backgroundColor != null) {
                cs.setNonStrokingColor(backgroundColor.first, backgroundColor.second, backgroundColor.third)
                cs.addRect(cellX, cellY, cellWidth, cellHeight)
                cs.fill()
            }

            // Draw border
            cs.setStrokingColor(borderColor.first, borderColor.second, borderColor.third)
            cs.setLineWidth(borderWidth)
            cs.addRect(cellX, cellY, cellWidth, cellHeight)
            cs.stroke()

            // Draw text
            val textX = cellX + 5f // 5 points padding
            val textY = startY - (cellHeight / 2) - (defaultFontSize / 4) // Vertically center
            renderText(cellText, textX, textY, fontPair, defaultFontSize)
        }
    }

    fun save(fileName: String) {
        contentStream?.close()
        document.save(fileName)
    }

    override fun close() {
        contentStream?.close()
    }

    private fun renderText(text: String, x: Float, y: Float, fontPair: FontPair, fontSize: Float) {
        if (isGlyphsRenderingNeeded(fontPair)) {
            renderGlyphs(text, x, y, fontPair, fontSize)
        } else {
            renderTextNative(text, x, y, fontPair.pdFont, fontSize)
        }
    }

    /**
     * Retrieves the current font pair configuration used for rendering text in the PDF document.
     * The font pair consists of a PDFont, an optional AWT Font (for advanced text shaping),
     * and a flag indicating whether text shaping is required.
     *
     * @return the current font pair, which includes the PDFont, optional AWT Font, and shaping flag
     * @throws IllegalStateException if the font pair has not been set
     */
    private fun getFontPair(): FontPair = defaultFontPair ?: throw IllegalStateException("Font not set.")

    /**
     * Determines whether glyph-based rendering is required for the given font pair.
     * Glyph rendering is necessary when advanced text shaping is enabled and an AWT font is provided.
     *
     * @param fontPair The font configuration containing a PDFont, an optional AWT Font,
     *                 and a flag indicating if text shaping is required.
     * @return `true` if glyph-based rendering is needed; `false` otherwise.
     */
    private fun isGlyphsRenderingNeeded(fontPair: FontPair): Boolean {
        return fontPair.useShaping && fontPair.awtFont != null
    }

    /**
     * Adds a new page to the current PDF document and initializes the content stream for the new page.
     * This method is responsible for finalizing the content stream of the current page, adding a fresh page
     * to the document, and setting up a new content stream tied to the newly added page.
     *
     * Steps performed by this method:
     * 1. Closes the current content stream if it exists.
     * 2. Creates a new `PDPage` using the defined `mediaBox` dimensions.
     * 3. Appends the newly created page to the PDF document.
     * 4. Updates the current page reference to the newly added page.
     * 5. Initializes a new `PDPageContentStream` for the new page.
     * 6. Resets the vertical positioning (`currentY`) to the top of the page, accounting for the top margin.
     *
     * Note:
     * - This method assumes a valid `document` object and `mediaBox` are already initialized.
     * - The `contentStream` and `currentPage` are modified by this method to reflect the new page setup.
     *
     * Precondition:
     * - Ensure that `document` is not null and properly initialized before invoking this method.
     *
     * Postcondition:
     * - A new page is added to the document, and its content stream is prepared for rendering operations.
     */
    private fun addNewPage() {
        contentStream?.close()

        val page = PDPage(mediaBox)
        document.addPage(page)
        currentPage = page

        contentStream = PDPageContentStream(document, page)
        currentY = mediaBox.height - marginTop
    }

    /**
     * Wraps a given text into multiple lines such that each line does not exceed the specified maximum width
     * when rendered with the specified font and font size.
     *
     * @param text the text to be wrapped into multiple lines
     * @param font the font to be used for measuring the text width
     * @param fontSize the font size to be used for measuring the text width
     * @param maxWidth the maximum allowable width for each line
     * @return a list of strings, where each string represents a line of wrapped text
     * @throws IllegalStateException if the BreakIterator is not initialized
     */
    private fun wrapText(text: String, font: PDFont, fontSize: Float, maxWidth: Float): List<String> {
        val iterator = breakIteratorCache?.get() ?: throw IllegalStateException("BreakIterator not initialized.")
        iterator.setText(text)
        val lines = mutableListOf<String>()
        var currentLine = ""
        var start = iterator.first()
        var end = iterator.next()

        while (end != BreakIterator.DONE) {
            val word = text.substring(start, end)
            if (word.isNotEmpty()) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine$word"
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

    /**
     * Renders the specified text using precise glyph positioning to ensure accurate rendering
     * of complex text, such as when shaping is needed for certain fonts or character sets.
     *
     * @param text The text to be rendered.
     * @param x The x-coordinate of the starting position for rendering the text.
     * @param y The y-coordinate of the baseline for rendering the text.
     * @param fontPair The font information, containing both the PDFont and optional AWT Font for shaping.
     * @param fontSize The size of the font to be rendered.
     */
    private fun renderGlyphs(text: String, x: Float, y: Float, fontPair: FontPair, fontSize: Float) {
        val cs = contentStream ?: return
        val awtFont = fontPair.awtFont ?: return
        val pdFont = fontPair.pdFont

        // 1. เตรียม AWT Font ตามขนาด
        val derivedFont = awtFont.deriveFont(fontSize)

        // 2. คำนวณ GlyphVector
        val gv: GlyphVector = derivedFont.layoutGlyphVector(
            frc, text.toCharArray(), 0, text.length, Font.LAYOUT_LEFT_TO_RIGHT
        )

        cs.beginText()
        cs.setFont(pdFont, fontSize)

        // 3. วาดทีละ Glyph ตามตำแหน่งที่ AWT คำนวณมาให้
        for (i in 0 until gv.numGlyphs) {
            val glyphId = gv.getGlyphCode(i)
            val pos = gv.getGlyphPosition(i)

            // คำนวณพิกัดจริงบน PDF
            // x คือจุดเริ่มต้นบรรทัด, pos.x คือระยะห่างจากจุดเริ่มต้น
            val gx = x + pos.x
            // y คือ Baseline, pos.y คือการขยับขึ้นลง (AWT y ลงเป็นบวก, PDF y ขึ้นเป็นบวก จึงต้องลบ)
            val gy = y - pos.y

            cs.setTextMatrix(Matrix.getTranslateInstance(gx.toFloat(), gy.toFloat()))

            val hex = String.format("%04X", glyphId)
            cs.appendRawCommands("<$hex> Tj ")
        }

        cs.endText()
    }

    /**
     * Renders text onto a PDF page using the native PDFBox text rendering features.
     * Does not account for advanced shaping or glyph positioning.
     *
     * @param text the text to be rendered
     * @param x the x-coordinate of the starting position for rendering the text
     * @param y the y-coordinate of the baseline position for rendering the text
     * @param pdFont the font to be used for rendering the text
     * @param fontSize the size of the font to be used
     */
    private fun renderTextNative(
        text: String, x: Float, y: Float, pdFont: PDFont, fontSize: Float
    ) {
        val cs = contentStream ?: return
        cs.beginText()
        cs.setFont(pdFont, fontSize)
        cs.setTextMatrix(Matrix.getTranslateInstance(x, y))
        cs.newLineAtOffset(0f, fontSize)
        cs.showText(text)
        cs.endText()
    }
}

