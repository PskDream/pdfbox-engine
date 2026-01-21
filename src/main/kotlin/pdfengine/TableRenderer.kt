package pdfengine

import org.apache.pdfbox.pdmodel.PDPageContentStream
import kotlin.math.max

/**
 * Data class representing a table to be rendered.
 */
class PdfTable(
    val headers: List<String>,
    val rows: List<List<String>>,
    val config: TableConfig = TableConfig()
)

/**
 * Builder for [PdfTable] to provide a DSL-like experience.
 */
class PdfTableBuilder {
    private var headers: List<String> = emptyList()
    private val rows: MutableList<List<String>> = mutableListOf()
    private var config: TableConfig = TableConfig()

    fun headers(vararg names: String) {
        headers = names.toList()
    }

    fun headers(names: List<String>) {
        headers = names
    }

    fun row(vararg cells: String) {
        rows.add(cells.toList())
    }

    fun row(cells: List<String>) {
        rows.add(cells)
    }

    fun rows(allRows: List<List<String>>) {
        rows.addAll(allRows)
    }

    fun config(block: TableConfig.() -> Unit) {
        config.block()
    }

    fun build(): PdfTable = PdfTable(headers, rows, config)
}

class TableRenderer(
    private val textRenderer: TextRenderer,
    private val textWrapper: TextWrapper
) {

    fun drawTable(
        contentStream: PDPageContentStream,
        startX: Float,
        startY: Float,
        availableWidth: Float,
        table: PdfTable,
        fontPair: PdfFont,
        fontSize: Float,
        onNewPageNeeded: (Float) -> PDPageContentStream
    ): Float {
        val config = table.config
        var currentContentStream = contentStream
        var currentY = startY
        val numColumns = table.headers.size
        if (numColumns == 0) return currentY
        
        val columnWidths = config.columnWidths ?: List(numColumns) { availableWidth / numColumns }

        // Calculate header height
        val headerHeight = if (config.autoHeight) {
            calculateRowHeight(table.headers, columnWidths, fontPair, fontSize, config)
        } else {
            config.cellHeight
        }

        // Draw header row
        drawTableRow(
            currentContentStream, table.headers, startX, currentY, columnWidths, headerHeight,
            config.borderColor, config.headerBackgroundColor, config.borderWidth, fontPair, fontSize,
            config.headerFontColor, config.lineSpacingFactor, config.headerAlignment, config.horizontalPadding, config.verticalPadding
        )

        currentY -= headerHeight

        // Draw data rows
        table.rows.forEachIndexed { rowIndex, row ->
            val actualRowHeight = if (config.autoHeight) {
                calculateRowHeight(row, columnWidths, fontPair, fontSize, config)
            } else {
                config.cellHeight
            }

            // Check if we need a new page
            if (currentY - actualRowHeight < 50f) { 
                currentContentStream = onNewPageNeeded(actualRowHeight)
                currentY = 841.89f - 50f 
            }

            val rowBackgroundColor = if (config.alternateRowColor != null && rowIndex % 2 == 1) {
                config.alternateRowColor
            } else {
                null
            }

            drawTableRow(
                currentContentStream, row, startX, currentY, columnWidths, actualRowHeight,
                config.borderColor, rowBackgroundColor, config.borderWidth, fontPair, fontSize,
                config.rowFontColor, config.lineSpacingFactor, config.cellAlignment, config.horizontalPadding, config.verticalPadding
            )

            currentY -= actualRowHeight
        }
        
        return currentY
    }

    fun calculateRowHeight(
        cells: List<String>,
        columnWidths: List<Float>,
        fontPair: PdfFont,
        fontSize: Float,
        config: TableConfig
    ): Float {
        var maxLines = 1

        cells.forEachIndexed { index, cellText ->
            val cellWidth = columnWidths.getOrElse(index) { 0f }
            val maxCellWidth = cellWidth - (config.horizontalPadding * 2)
            if (maxCellWidth > 0) {
                val lines = textWrapper.wrapText(cellText, fontPair.pdFont, fontSize, maxCellWidth)
                maxLines = max(maxLines, lines.size)
            }
        }

        val lineHeight = fontSize * config.lineSpacingFactor
        return (maxLines * lineHeight) + (config.verticalPadding * 2)
    }

    fun drawTableRow(
        contentStream: PDPageContentStream,
        cells: List<String>,
        startX: Float,
        startY: Float,
        columnWidths: List<Float>,
        cellHeight: Float,
        borderColor: PdfColor,
        backgroundColor: PdfColor?,
        borderWidth: Float,
        fontPair: PdfFont,
        fontSize: Float,
        fontColor: PdfColor,
        lineSpacingFactor: Float,
        cellAlignment: CellAlignment,
        horizontalPadding: Float,
        verticalPadding: Float
    ) {
        var currentX = startX

        cells.forEachIndexed { index, cellText ->
            val cellWidth = columnWidths.getOrElse(index) { 0f }
            
            // 1. Draw background
            if (backgroundColor != null) {
                contentStream.setNonStrokingColor(backgroundColor.r, backgroundColor.g, backgroundColor.b)
                contentStream.addRect(currentX, startY - cellHeight, cellWidth, cellHeight)
                contentStream.fill()
            }

            // 2. Draw border
            contentStream.setStrokingColor(borderColor.r, borderColor.g, borderColor.b)
            contentStream.setLineWidth(borderWidth)
            contentStream.addRect(currentX, startY - cellHeight, cellWidth, cellHeight)
            contentStream.stroke()

            // 3. Draw text
            val maxCellWidth = cellWidth - (horizontalPadding * 2)
            if (maxCellWidth > 0) {
                val lines = textWrapper.wrapText(cellText, fontPair.pdFont, fontSize, maxCellWidth)
                val totalTextHeight = lines.size * (fontSize * lineSpacingFactor)

                // Calculate starting Y for vertical alignment
                val startTextY = when (cellAlignment.vertical) {
                    VerticalAlignment.TOP -> startY - verticalPadding - fontSize
                    VerticalAlignment.MIDDLE -> startY - (cellHeight - totalTextHeight) / 2 - fontSize
                    VerticalAlignment.BOTTOM -> startY - cellHeight + verticalPadding + (totalTextHeight - (fontSize * lineSpacingFactor))
                }

                lines.forEachIndexed { lineIndex, line ->
                    val lineWidth = fontPair.pdFont.getStringWidth(line) / 1000 * fontSize
                    val lineX = when (cellAlignment.horizontal) {
                        HorizontalAlignment.LEFT -> currentX + horizontalPadding
                        HorizontalAlignment.CENTER -> currentX + (cellWidth - lineWidth) / 2
                        HorizontalAlignment.RIGHT -> currentX + cellWidth - horizontalPadding - lineWidth
                    }
                    val lineY = startTextY - (lineIndex * fontSize * lineSpacingFactor)

                    contentStream.setNonStrokingColor(fontColor.r, fontColor.g, fontColor.b)
                    textRenderer.drawText(contentStream, line, lineX, lineY, fontPair, fontSize)
                }
            }

            currentX += cellWidth
        }
    }
}
