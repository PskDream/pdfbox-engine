package pdfengine

/**
 * Represents a color in RGB format.
 *
 * @property r Red component (0.0 to 1.0)
 * @property g Green component (0.0 to 1.0)
 * @property b Blue component (0.0 to 1.0)
 */
data class PdfColor(val r: Float, val g: Float, val b: Float) {
    companion object {
        val BLACK = PdfColor(0f, 0f, 0f)
        val WHITE = PdfColor(1f, 1f, 1f)
        val GRAY = PdfColor(0.5f, 0.5f, 0.5f)
        val LIGHT_GRAY = PdfColor(0.8f, 0.8f, 0.8f)
        val RED = PdfColor(1f, 0f, 0f)
        val GREEN = PdfColor(0f, 1f, 0f)
        val BLUE = PdfColor(0f, 0f, 1f)
        val ORANGE = PdfColor(1f, 0.5f, 0f)
    }
}

/**
 * Configuration for table rendering.
 */
data class TableConfig(
    var cellHeight: Float = 30f,
    var autoHeight: Boolean = true,
    var borderColor: PdfColor = PdfColor.BLACK,
    var headerBackgroundColor: PdfColor = PdfColor.LIGHT_GRAY,
    var headerFontColor: PdfColor = PdfColor.WHITE,
    var rowFontColor: PdfColor = PdfColor.BLACK,
    var alternateRowColor: PdfColor? = null,
    var borderWidth: Float = 1f,
    var lineSpacingFactor: Float = 1.2f,
    var horizontalPadding: Float = 5f,
    var verticalPadding: Float = 5f,
    var columnWidths: List<Float>? = null,
    var headerAlignment: CellAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
    var cellAlignment: CellAlignment = CellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
) {
    fun cellHeight(value: Float): TableConfig { cellHeight = value; return this }
    fun autoHeight(value: Boolean): TableConfig { autoHeight = value; return this }
    fun borderColor(value: PdfColor): TableConfig { borderColor = value; return this }
    fun headerBackgroundColor(value: PdfColor): TableConfig { headerBackgroundColor = value; return this }
    fun headerFontColor(value: PdfColor): TableConfig { headerFontColor = value; return this }
    fun rowFontColor(value: PdfColor): TableConfig { rowFontColor = value; return this }
    fun alternateRowColor(value: PdfColor?): TableConfig { alternateRowColor = value; return this }
    fun borderWidth(value: Float): TableConfig { borderWidth = value; return this }
    fun lineSpacingFactor(value: Float): TableConfig { lineSpacingFactor = value; return this }
    fun horizontalPadding(value: Float): TableConfig { horizontalPadding = value; return this }
    fun verticalPadding(value: Float): TableConfig { verticalPadding = value; return this }
    fun columnWidths(value: List<Float>?): TableConfig { columnWidths = value; return this }
    fun headerAlignment(horizontal: HorizontalAlignment = HorizontalAlignment.CENTER, vertical: VerticalAlignment = VerticalAlignment.MIDDLE): TableConfig {
        headerAlignment = CellAlignment(horizontal, vertical)
        return this
    }
    fun cellAlignment(horizontal: HorizontalAlignment = HorizontalAlignment.LEFT, vertical: VerticalAlignment = VerticalAlignment.MIDDLE): TableConfig {
        cellAlignment = CellAlignment(horizontal, vertical)
        return this
    }
}

/**
 * Configuration for page layout.
 */
data class PageConfig(
    var marginTop: Float = 50f,
    var marginBottom: Float = 50f,
    var marginLeft: Float = 50f,
    var marginRight: Float = 50f,
    var defaultFontSize: Float = 14f,
    var defaultLineSpacingFactor: Float = 1.2f
) {
    fun marginTop(value: Float): PageConfig { marginTop = value; return this }
    fun marginBottom(value: Float): PageConfig { marginBottom = value; return this }
    fun marginLeft(value: Float): PageConfig { marginLeft = value; return this }
    fun marginRight(value: Float): PageConfig { marginRight = value; return this }
    fun defaultFontSize(value: Float): PageConfig { defaultFontSize = value; return this }
    fun defaultLineSpacingFactor(value: Float): PageConfig { defaultLineSpacingFactor = value; return this }
}
