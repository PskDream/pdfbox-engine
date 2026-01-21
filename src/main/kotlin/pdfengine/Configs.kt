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
    val cellHeight: Float = 30f,
    val autoHeight: Boolean = true,
    val borderColor: PdfColor = PdfColor.BLACK,
    val headerBackgroundColor: PdfColor = PdfColor.LIGHT_GRAY,
    val headerFontColor: PdfColor = PdfColor.WHITE,
    val rowFontColor: PdfColor = PdfColor.BLACK,
    val alternateRowColor: PdfColor? = null,
    val borderWidth: Float = 1f,
    val lineSpacingFactor: Float = 1.2f,
    val horizontalPadding: Float = 5f,
    val verticalPadding: Float = 5f,
    val headerAlignment: CellAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
    val cellAlignment: CellAlignment = CellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
) {
    fun cellHeight(value: Float) = copy(cellHeight = value)
    fun autoHeight(value: Boolean) = copy(autoHeight = value)
    fun borderColor(value: PdfColor) = copy(borderColor = value)
    fun headerBackgroundColor(value: PdfColor) = copy(headerBackgroundColor = value)
    fun headerFontColor(value: PdfColor) = copy(headerFontColor = value)
    fun rowFontColor(value: PdfColor) = copy(rowFontColor = value)
    fun alternateRowColor(value: PdfColor?) = copy(alternateRowColor = value)
    fun borderWidth(value: Float) = copy(borderWidth = value)
    fun lineSpacingFactor(value: Float) = copy(lineSpacingFactor = value)
    fun horizontalPadding(value: Float) = copy(horizontalPadding = value)
    fun verticalPadding(value: Float) = copy(verticalPadding = value)
    fun headerAlignment(horizontal: HorizontalAlignment = HorizontalAlignment.CENTER, vertical: VerticalAlignment = VerticalAlignment.MIDDLE) = 
        copy(headerAlignment = CellAlignment(horizontal, vertical))
    fun cellAlignment(horizontal: HorizontalAlignment = HorizontalAlignment.LEFT, vertical: VerticalAlignment = VerticalAlignment.MIDDLE) = 
        copy(cellAlignment = CellAlignment(horizontal, vertical))
}

/**
 * Configuration for page layout.
 */
data class PageConfig(
    val marginTop: Float = 50f,
    val marginBottom: Float = 50f,
    val marginLeft: Float = 50f,
    val marginRight: Float = 50f,
    val defaultFontSize: Float = 14f,
    val defaultLineSpacingFactor: Float = 1.5f
) {
    fun marginTop(value: Float) = copy(marginTop = value)
    fun marginBottom(value: Float) = copy(marginBottom = value)
    fun marginLeft(value: Float) = copy(marginLeft = value)
    fun marginRight(value: Float) = copy(marginRight = value)
    fun defaultFontSize(value: Float) = copy(defaultFontSize = value)
    fun defaultLineSpacingFactor(value: Float) = copy(defaultLineSpacingFactor = value)
}
