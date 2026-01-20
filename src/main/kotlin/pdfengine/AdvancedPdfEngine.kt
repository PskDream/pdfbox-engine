package pdfengine

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

    // Line spacing configuration
    private var defaultLineSpacingFactor = 1.5f // Default: 150% of font size

    // Table configuration
    private var defaultTableLineSpacingFactor = 1.2f // For wrapped text in table cells
    private var defaultTableCellPaddingHorizontal = 5f
    private var defaultTableCellPaddingVertical = 5f
    private var defaultTableCellAlignment = CellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
    private var defaultTableHeaderAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)

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
     * @param lineHeightFactor The factor to calculate the spacing between lines relative to the default font size.
     *                        If null, uses the default line spacing factor.
     * @param wrapText Whether to wrap the text if it exceeds the specified maximum width. Defaults to false.
     * @param maxWidth The maximum width allowed for the text before wrapping occurs. Defaults to the calculated maximum width of the page.
     * @throws IllegalStateException If a font has not been set prior to invoking this method.
     */
    fun writeLine(
        text: String,
        x: Float = marginLeft,
        lineHeightFactor: Float? = null,
        wrapText: Boolean = false,
        maxWidth: Float = getAvailableWidth()
    ) {
        val fontPair = this.getFontPair()
        val pdFont = fontPair.pdFont
        val actualLineHeightFactor = lineHeightFactor ?: defaultLineSpacingFactor
        val lineHeight = defaultFontSize * actualLineHeightFactor

        val lines = if (wrapText) wrapText(text, pdFont, defaultFontSize, maxWidth) else listOf(text)

        lines.forEach { line ->
            if (currentY - lineHeight < marginBottom) {
                addNewPage()
            }
            renderText(line, x, currentY, fontPair, defaultFontSize)
            currentY -= lineHeight
        }

    }

    fun newLine(lineHeightFactor: Float? = null) {
        val actualLineHeightFactor = lineHeightFactor ?: defaultLineSpacingFactor
        val lineHeight = defaultFontSize * actualLineHeightFactor
        currentY -= lineHeight
    }

    fun setStyle(familyName: String, style: FontStyle = FontStyle.REGULAR) {
        this.defaultFontPair = fontManager.getFont(familyName, style)
    }

    fun setSize(size: Float) {
        this.defaultFontSize = size
    }

    /**
     * Sets the default line spacing factor for regular text (writeLine, etc).
     * The actual line height is calculated as: fontSize * lineSpacingFactor
     *
     * @param factor The multiplier for line spacing (e.g., 1.5 means 150% of font size)
     */
    fun setLineSpacingFactor(factor: Float) {
        this.defaultLineSpacingFactor = factor
    }

    /**
     * Gets the default line spacing factor for regular text.
     *
     * @return The current line spacing factor
     */
    fun getLineSpacingFactor(): Float = defaultLineSpacingFactor

    /**
     * Gets the line spacing factor for table cells.
     *
     * @return The current table line spacing factor
     */
    fun getTableLineSpacingFactor(): Float = defaultTableLineSpacingFactor

    /**
     * Sets the horizontal and vertical padding for table cells.
     *
     * @param horizontal Horizontal padding in points (left and right)
     * @param vertical Vertical padding in points (top and bottom)
     */
    fun setTableCellPadding(horizontal: Float, vertical: Float) {
        this.defaultTableCellPaddingHorizontal = horizontal
        this.defaultTableCellPaddingVertical = vertical
    }

    /**
     * Gets the horizontal padding for table cells.
     *
     * @return Horizontal padding value in points
     */
    fun getTableCellPaddingHorizontal(): Float = defaultTableCellPaddingHorizontal

    /**
     * Gets the vertical padding for table cells.
     *
     * @return Vertical padding value in points
     */
    fun getTableCellPaddingVertical(): Float = defaultTableCellPaddingVertical

    /**
     * Sets the default alignment for regular table cells.
     *
     * @param alignment The alignment to use (LEFT, CENTER, or RIGHT)
     */
    fun setTableCellAlignment(alignment: CellAlignment) {
        this.defaultTableCellAlignment = alignment
    }

    /**
     * Gets the default alignment for regular table cells.
     *
     * @return The current cell alignment
     */
    fun getTableCellAlignment(): CellAlignment = defaultTableCellAlignment

    /**
     * Sets the default alignment for table header cells.
     *
     * @param alignment The alignment to use (LEFT, CENTER, or RIGHT)
     */
    fun setTableHeaderAlignment(alignment: CellAlignment) {
        this.defaultTableHeaderAlignment = alignment
    }

    /**
     * Gets the default alignment for table header cells.
     *
     * @return The current header alignment
     */
    fun getTableHeaderAlignment(): CellAlignment = defaultTableHeaderAlignment

    /**
     * Draws a table on the PDF with the specified structure, data, and formatting options.
     *
     * @param headers List of header cell values for the table columns
     * @param rows List of rows, where each row is a list of cell values
     * @param cellWidth Width of each cell. If not specified, calculates equally based on available width
     * @param cellHeight Height of each cell (in points). Can be overridden by autoHeight
     * @param autoHeight If true, cell height is calculated based on wrapped text content
     * @param borderColor RGB color for table borders (default: black)
     * @param headerBackgroundColor RGB color for header background (default: light gray)
     * @param headerFontColor RGB color for header text (default: white)
     * @param rowFontColor RGB color for row text (default: black)
     * @param alternateRowColor Optional RGB color for alternating row backgrounds
     * @param borderWidth Width of border lines
     * @param lineSpacingFactor Line spacing multiplier for wrapped text (uses table line spacing if null)
     * @param headerAlignment Text alignment for header cells (default: CENTER)
     * @param cellAlignment Text alignment for data cells (default: LEFT)
     * @throws IllegalStateException If font has not been set
     */
    fun drawTable(
        headers: List<String>,
        rows: List<List<String>>,
        cellWidth: Float? = null,
        cellHeight: Float = 30f,
        autoHeight: Boolean = true,
        borderColor: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
        headerBackgroundColor: Triple<Float, Float, Float> = Triple(0.8f, 0.8f, 0.8f),
        headerFontColor: Triple<Float, Float, Float> = Triple(1f, 1f, 1f),
        rowFontColor: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
        alternateRowColor: Triple<Float, Float, Float>? = null,
        borderWidth: Float = 1f,
        lineSpacingFactor: Float? = null,
        headerAlignment: CellAlignment? = null,
        cellAlignment: CellAlignment? = null
    ) {
        val fontPair = this.getFontPair()
        val numColumns = headers.size
        val calculatedCellWidth = cellWidth ?: (getAvailableWidth() / numColumns)
        val actualLineSpacingFactor = lineSpacingFactor ?: defaultTableLineSpacingFactor
        val actualHeaderAlignment = headerAlignment ?: defaultTableHeaderAlignment
        val actualCellAlignment = cellAlignment ?: defaultTableCellAlignment

        var tableStartY = currentY

        // Calculate header height
        val headerHeight = if (autoHeight) {
            calculateRowHeight(headers, calculatedCellWidth, fontPair, actualLineSpacingFactor)
        } else {
            cellHeight
        }

        // Draw header row
        drawTableRow(
            headers, marginLeft, tableStartY, calculatedCellWidth, headerHeight,
            borderColor, headerBackgroundColor, borderWidth, fontPair, headerFontColor,
            actualLineSpacingFactor, actualHeaderAlignment
        )

        tableStartY -= headerHeight
        currentY -= headerHeight

        // Draw data rows
        rows.forEachIndexed { rowIndex, row ->
            // Calculate actual row height for this row
            val actualRowHeight = if (autoHeight) {
                calculateRowHeight(row, calculatedCellWidth, fontPair, actualLineSpacingFactor)
            } else {
                cellHeight
            }

            // Check if we need a new page
            if (tableStartY - actualRowHeight < marginBottom) {
                addNewPage()
                tableStartY = currentY
            }

            val rowBackgroundColor = if (alternateRowColor != null && rowIndex % 2 == 1) {
                alternateRowColor
            } else {
                null
            }

            drawTableRow(
                row, marginLeft, tableStartY, calculatedCellWidth, actualRowHeight,
                borderColor, rowBackgroundColor, borderWidth, fontPair, rowFontColor,
                actualLineSpacingFactor, actualCellAlignment
            )

            tableStartY -= actualRowHeight
            currentY -= actualRowHeight
        }
    }

    /**
     * Calculates the required height for a row based on the maximum number of wrapped text lines
     * in any cell of that row.
     *
     * @param cells List of cell values in the row
     * @param cellWidth Width of each cell (used for wrapping calculation)
     * @param fontPair Font pair used for text measurement
     * @param lineSpacingFactor Line spacing multiplier for text
     * @return The minimum height needed to display all cell content
     */
    private fun calculateRowHeight(
        cells: List<String>,
        cellWidth: Float,
        fontPair: FontPair,
        lineSpacingFactor: Float = defaultTableLineSpacingFactor
    ): Float {
        val maxCellWidth = cellWidth - (defaultTableCellPaddingHorizontal * 2)
        var maxLines = 1

        cells.forEach { cellText ->
            val wrappedLines = wrapText(cellText, fontPair.pdFont, defaultFontSize, maxCellWidth)
            if (wrappedLines.size > maxLines) {
                maxLines = wrappedLines.size
            }
        }

        val lineHeight = defaultFontSize * lineSpacingFactor
        val verticalPadding = defaultTableCellPaddingVertical * 2
        return (maxLines * lineHeight) + verticalPadding
    }

    /**
     * Helper function to draw a single row of the table
     *
     * @param cells List of cell values for this row
     * @param startX Starting x-coordinate for the row
     * @param startY Starting y-coordinate for the row
     * @param cellWidth Width of each cell
     * @param cellHeight Height of the cell
     * @param borderColor RGB color for borders
     * @param backgroundColor Optional RGB color for cell background
     * @param borderWidth Width of the border line
     * @param fontPair The font pair to use for rendering text
     * @param fontColor RGB color for the text
     * @param lineSpacingFactor Line spacing multiplier for wrapped text
     * @param cellAlignment Text alignment within cells (LEFT, CENTER, or RIGHT)
     */
    private fun drawTableRow(
        cells: List<String>,
        startX: Float,
        startY: Float,
        cellWidth: Float,
        cellHeight: Float,
        borderColor: Triple<Float, Float, Float>,
        backgroundColor: Triple<Float, Float, Float>?,
        borderWidth: Float,
        fontPair: FontPair,
        fontColor: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
        lineSpacingFactor: Float = defaultTableLineSpacingFactor,
        cellAlignment: CellAlignment = CellAlignment()
    ) {
        cells.forEachIndexed { index, cellText ->
            val cellX = startX + (index * cellWidth)
            val cellY = startY - cellHeight

            // Always get fresh contentStream (may be new after page break)
            val cs = contentStream ?: return

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

            // Set font color and draw text
            cs.setNonStrokingColor(fontColor.first, fontColor.second, fontColor.third)

            // Wrap text to fit within cell width (accounting for padding)
            val maxCellWidth = cellWidth - (defaultTableCellPaddingHorizontal * 2)
            val wrappedLines = wrapText(cellText, fontPair.pdFont, defaultFontSize, maxCellWidth)

            // Draw wrapped text lines
            val lineHeight = defaultFontSize * lineSpacingFactor
            val totalLinesHeight = wrappedLines.size * lineHeight

            // Calculate starting Y position based on vertical alignment
            val startTextY = when (cellAlignment.vertical) {
                VerticalAlignment.TOP -> startY - defaultTableCellPaddingVertical - defaultFontSize
                VerticalAlignment.MIDDLE -> {
                    val availableHeight = cellHeight - (defaultTableCellPaddingVertical * 2)
                    val verticalOffset = (availableHeight - totalLinesHeight) / 2
                    startY - defaultTableCellPaddingVertical - verticalOffset - defaultFontSize
                }
                VerticalAlignment.BOTTOM -> {
                    val bottomPadding = defaultTableCellPaddingVertical
                    startY - cellHeight + bottomPadding + (totalLinesHeight - lineHeight)
                }
            }

            wrappedLines.forEachIndexed { lineIndex, line ->
                // Calculate X position based on horizontal alignment
                val textX = when (cellAlignment.horizontal) {
                    HorizontalAlignment.LEFT -> cellX + defaultTableCellPaddingHorizontal
                    HorizontalAlignment.CENTER -> {
                        val lineWidth = fontPair.pdFont.getStringWidth(line) / 1000 * defaultFontSize
                        cellX + (cellWidth - lineWidth) / 2
                    }
                    HorizontalAlignment.RIGHT -> {
                        val lineWidth = fontPair.pdFont.getStringWidth(line) / 1000 * defaultFontSize
                        cellX + cellWidth - lineWidth - defaultTableCellPaddingHorizontal
                    }
                }

                val textY = startTextY - (lineIndex * lineHeight)
                renderText(line, textX, textY, fontPair, defaultFontSize)
            }
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
    fun addNewPage() {
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
        text: String, x: Float, y: Float, pdFont: PDFont, fontSize: Float,
    ) {
        val cs = contentStream ?: return
        cs.beginText()
        cs.setFont(pdFont, fontSize)
        cs.setTextMatrix(Matrix.getTranslateInstance(x, y))
        cs.showText(text)
        cs.endText()
    }
}

