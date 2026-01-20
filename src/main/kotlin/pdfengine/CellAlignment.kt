package pdfengine

/**
 * Enum class representing text vertical alignment options for table cells.
 */
enum class VerticalAlignment {
    /** Align text to the top of the cell */
    TOP,

    /** Vertically center text in the cell */
    MIDDLE,

    /** Align text to the bottom of the cell */
    BOTTOM
}

/**
 * Enum class representing text horizontal alignment options for table cells.
 */
enum class HorizontalAlignment {
    /** Align text to the left side of the cell */
    LEFT,

    /** Center align text in the cell */
    CENTER,

    /** Align text to the right side of the cell */
    RIGHT
}

/**
 * Data class representing cell alignment with both horizontal and vertical components.
 *
 * @param horizontal The horizontal alignment (LEFT, CENTER, or RIGHT)
 * @param vertical The vertical alignment (TOP, MIDDLE, or BOTTOM)
 */
data class CellAlignment(
    val horizontal: HorizontalAlignment = HorizontalAlignment.LEFT,
    val vertical: VerticalAlignment = VerticalAlignment.MIDDLE
)


