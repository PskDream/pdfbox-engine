package pdfengine

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.util.Locale

class PdfEngine(val document: PDDocument, private val mediaBox: PDRectangle = PDRectangle.A4) : AutoCloseable {

    private val textRenderer = TextRenderer()
    private val textWrapper = TextWrapper()
    private val tableRenderer by lazy { TableRenderer(textRenderer, textWrapper) }
    private val imageRenderer by lazy { ImageRenderer(document) }

    val fontManager = FontManager(document)

    private var pageConfig = PageConfig()

    private var currentPage: PDPage? = null
    private var contentStream: PDPageContentStream? = null
    var currentY = mediaBox.height - pageConfig.marginTop
        private set

    private var defaultFontPair: PdfFont? = null

    init {
        addNewPage()
    }

    fun pageConfig(block: PageConfig.() -> Unit) {
        pageConfig.block()
    }

    fun setBreakIterator(locale: Locale) {
        textWrapper.setLocale(locale)
    }

    val availableWidth: Float
        get() = mediaBox.width - pageConfig.marginLeft - pageConfig.marginRight

    fun drawText(text: String, x: Float = pageConfig.marginLeft, y: Float = currentY, color: PdfColor? = null) {
        val cs = contentStream ?: return
        color?.let { cs.setNonStrokingColor(it.r, it.g, it.b) }
        textRenderer.drawText(cs, text, x, y, this.getFontPair(), pageConfig.defaultFontSize)
        if (color != null) {
            // Reset to black (or we could store previous color, but usually it's black)
            cs.setNonStrokingColor(0f, 0f, 0f)
        }
    }

    /**
     * Draws text aligned vertically within a given height (e.g., next to an image).
     * Supports multi-line text wrapping if wrapText is true.
     */
    fun drawTextInHeight(
        text: String,
        height: Float,
        x: Float = pageConfig.marginLeft,
        y: Float = currentY,
        spacing: Float = pageConfig.defaultLineSpacingFactor,
        verticalAlignment: VerticalAlignment = VerticalAlignment.MIDDLE,
        wrapText: Boolean = false,
        width: Float = availableWidth
    ) {
        val fontPair = this.getFontPair()
        val pdFont = fontPair.pdFont
        val fontSize = pageConfig.defaultFontSize
        val lineHeight = fontSize * spacing

        val lines = if (wrapText) {
            textWrapper.wrapText(text, pdFont, fontSize, width)
        } else {
            listOf(text)
        }

        val totalTextHeight = if (lines.size > 1) {
            (lines.size - 1) * lineHeight + fontSize
        } else {
            fontSize
        }

        var startY = when (verticalAlignment) {
            VerticalAlignment.TOP -> y - (fontSize * 0.8f)
            VerticalAlignment.MIDDLE -> y - (height / 2) + (totalTextHeight / 2) - (fontSize * 0.8f)
            VerticalAlignment.BOTTOM -> y - height + totalTextHeight - (fontSize * 0.2f)
        }

        val cs = contentStream ?: return
        lines.forEach { line ->
            textRenderer.drawText(cs, line, x, startY, fontPair, fontSize)
            startY -= lineHeight
        }
    }

    fun drawTextLine(
        text: String,
        x: Float = pageConfig.marginLeft,
        lineHeightFactor: Float? = null,
        wrapText: Boolean = false,
        maxWidth: Float = availableWidth,
        alignment: HorizontalAlignment? = null
    ) {
        val fontPair = this.getFontPair()
        val pdFont = fontPair.pdFont
        val actualLineHeightFactor = lineHeightFactor ?: pageConfig.defaultLineSpacingFactor
        val lineHeight = pageConfig.defaultFontSize * actualLineHeightFactor

        val lines =
            if (wrapText) textWrapper.wrapText(text, pdFont, pageConfig.defaultFontSize, maxWidth) else listOf(text)

        lines.forEach { line ->
            if (currentY - lineHeight < pageConfig.marginBottom) {
                addNewPage()
            }

            // Calculate X position based on alignment
            val textX = when (alignment) {
                null, HorizontalAlignment.LEFT -> x
                HorizontalAlignment.CENTER -> {
                    val lineWidth = pdFont.getStringWidth(line) / 1000 * pageConfig.defaultFontSize
                    x + (maxWidth - lineWidth) / 2
                }

                HorizontalAlignment.RIGHT -> {
                    val lineWidth = pdFont.getStringWidth(line) / 1000 * pageConfig.defaultFontSize
                    x + maxWidth - lineWidth
                }
            }

            val cs = contentStream ?: return@forEach
            textRenderer.drawText(cs, line, textX, currentY, fontPair, pageConfig.defaultFontSize)
            currentY -= lineHeight
        }
    }

    fun newLine(lineHeightFactor: Float? = null) {
        val actualLineHeightFactor = lineHeightFactor ?: pageConfig.defaultLineSpacingFactor
        val lineHeight = pageConfig.defaultFontSize * actualLineHeightFactor
        currentY -= lineHeight
    }

    fun setFont(familyName: String, style: FontStyle = FontStyle.REGULAR) {
        this.defaultFontPair = fontManager.getFont(familyName, style)
    }

    /**
     * Sets the default font size.
     */
    fun setFontSize(size: Float) {
        pageConfig = pageConfig.copy(defaultFontSize = size)
    }

    /**
     * Draws an image at the current Y position.
     * Updates currentY based on the image height and spacing.
     */
    fun drawImage(
        file: File,
        width: Float? = null,
        height: Float? = null,
        x: Float = pageConfig.marginLeft,
        spacing: Float = 10f,
        updateY: Boolean = true
    ): Pair<Float, Float> {
        val cs = contentStream ?: return Pair(0f, 0f)

        // If height is not provided, we need to calculate it to update currentY correctly
        val finalWidth: Float
        val finalHeight: Float

        if (width != null && height == null) {
            // Scale height based on width
            val pdImage = PDImageXObject.createFromFile(file.absolutePath, document)
            val aspectRatio = pdImage.height.toFloat() / pdImage.width.toFloat()
            finalWidth = width
            finalHeight = width * aspectRatio
        } else if (width == null && height == null) {
            val pdImage = PDImageXObject.createFromFile(file.absolutePath, document)
            finalWidth = pdImage.width.toFloat()
            finalHeight = pdImage.height.toFloat()
        } else {
            finalWidth = width ?: 0f // Should not happen with current logic but for safety
            finalHeight = height ?: 0f
        }

        // Check for new page
        if (currentY - finalHeight < pageConfig.marginBottom) {
            addNewPage()
        }

        imageRenderer.drawImage(cs, file, x, currentY - finalHeight, finalWidth, finalHeight)

        if (updateY) {
            currentY -= (finalHeight + spacing)
        }
        
        return Pair(finalWidth, finalHeight)
    }

    /**
     * Draws a table using the provided data and optional configuration overrides.
     */
    fun drawTable(
        headers: List<String>,
        rows: List<List<String>>,
        tableConfig: TableConfig = TableConfig(),
        cellWidth: Float? = null,
        cellHeight: Float? = null,
        autoHeight: Boolean? = null,
        borderColor: PdfColor? = null,
        headerBackgroundColor: PdfColor? = null,
        headerFontColor: PdfColor? = null,
        rowFontColor: PdfColor? = null,
        alternateRowColor: PdfColor? = null,
        borderWidth: Float? = null,
        lineSpacingFactor: Float? = null,
        headerAlignment: CellAlignment? = null,
        cellAlignment: CellAlignment? = null
    ) {
        val currentTableConfig = tableConfig.copy(
            cellHeight = cellHeight ?: tableConfig.cellHeight,
            autoHeight = autoHeight ?: tableConfig.autoHeight,
            borderColor = borderColor ?: tableConfig.borderColor,
            headerBackgroundColor = headerBackgroundColor ?: tableConfig.headerBackgroundColor,
            headerFontColor = headerFontColor ?: tableConfig.headerFontColor,
            rowFontColor = rowFontColor ?: tableConfig.rowFontColor,
            alternateRowColor = alternateRowColor ?: tableConfig.alternateRowColor,
            borderWidth = borderWidth ?: tableConfig.borderWidth,
            lineSpacingFactor = lineSpacingFactor ?: tableConfig.lineSpacingFactor,
            headerAlignment = headerAlignment ?: tableConfig.headerAlignment,
            cellAlignment = cellAlignment ?: tableConfig.cellAlignment
        )

        val table = PdfTable(headers, rows, currentTableConfig)
        drawTable(table)
    }

    fun drawTable(table: PdfTable) {
        val fontPair = this.getFontPair()
        val cs = contentStream ?: return

        currentY = tableRenderer.drawTable(
            cs, pageConfig.marginLeft, currentY, availableWidth,
            table, fontPair, pageConfig.defaultFontSize
        ) { rowHeight ->
            addNewPage()
            contentStream!!
        }
    }

    fun drawTable(block: PdfTableBuilder.() -> Unit) {
        val builder = PdfTableBuilder()
        builder.block()
        val table = builder.build()
        drawTable(table)
    }

    fun save(fileName: String) {
        contentStream?.close()
        document.save(fileName)
    }

    override fun close() {
        contentStream?.close()
    }

    private fun getFontPair(): PdfFont = defaultFontPair ?: throw IllegalStateException("Font not set.")

    fun addNewPage() {
        contentStream?.close()

        val page = PDPage(mediaBox)
        document.addPage(page)
        currentPage = page

        contentStream = PDPageContentStream(document, page)
        currentY = mediaBox.height - pageConfig.marginTop
    }
}

