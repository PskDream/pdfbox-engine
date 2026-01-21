package pdfengine

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.util.Locale

class PdfEngine(val document: PDDocument, private val mediaBox: PDRectangle = PDRectangle.A4) : AutoCloseable {

    private val textRenderer = TextRenderer()
    private val textWrapper = TextWrapper()
    private val tableRenderer by lazy { TableRenderer(textRenderer, textWrapper) }

    val fontManager = FontManager(document)

    private var pageConfig = PageConfig()
    private var tableConfig = TableConfig()

    private var currentPage: PDPage? = null
    private var contentStream: PDPageContentStream? = null
    private var currentY = mediaBox.height - pageConfig.marginTop

    private var defaultFontPair: PdfFont? = null

    init {
        addNewPage()
    }

    fun pageConfig(block: PageConfig.() -> PageConfig) {
        pageConfig = pageConfig.block()
    }

    fun tableConfig(block: TableConfig.() -> TableConfig) {
        tableConfig = tableConfig.block()
    }

    fun setBreakIterator(locale: Locale) {
        textWrapper.setLocale(locale)
    }

    val availableWidth: Float
        get() = mediaBox.width - pageConfig.marginLeft - pageConfig.marginRight

    fun drawText(text: String, x: Float = pageConfig.marginLeft) {
        val cs = contentStream ?: return
        textRenderer.drawText(cs, text, x, currentY, this.getFontPair(), pageConfig.defaultFontSize)
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

        val lines = if (wrapText) textWrapper.wrapText(text, pdFont, pageConfig.defaultFontSize, maxWidth) else listOf(text)

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
     * Draws a table using the provided data and optional configuration overrides.
     */
    fun drawTable(
        headers: List<String>,
        rows: List<List<String>>,
        cellWidth: Float? = null,
        cellHeight: Float = 30f,
        autoHeight: Boolean = true,
        borderColor: PdfColor = PdfColor.BLACK,
        headerBackgroundColor: PdfColor = PdfColor.LIGHT_GRAY,
        headerFontColor: PdfColor = PdfColor.WHITE,
        rowFontColor: PdfColor = PdfColor.BLACK,
        alternateRowColor: PdfColor? = null,
        borderWidth: Float = 1f,
        lineSpacingFactor: Float? = null,
        headerAlignment: CellAlignment? = null,
        cellAlignment: CellAlignment? = null
    ) {
        val currentTableConfig = tableConfig.copy(
            cellHeight = cellHeight,
            autoHeight = autoHeight,
            borderColor = borderColor,
            headerBackgroundColor = headerBackgroundColor,
            headerFontColor = headerFontColor,
            rowFontColor = rowFontColor,
            alternateRowColor = alternateRowColor,
            borderWidth = borderWidth,
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
        drawTable(builder.build())
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

