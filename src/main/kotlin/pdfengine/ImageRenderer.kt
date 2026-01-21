package pdfengine

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File

class ImageRenderer(private val document: PDDocument) {

    /**
     * Draws an image at the specified position with optional scaling.
     */
    fun drawImage(
        contentStream: PDPageContentStream,
        file: File,
        x: Float,
        y: Float,
        width: Float? = null,
        height: Float? = null
    ) {
        val pdImage = PDImageXObject.createFromFile(file.absolutePath, document)
        
        val finalWidth = width ?: pdImage.width.toFloat()
        val finalHeight = height ?: pdImage.height.toFloat()
        
        contentStream.drawImage(pdImage, x, y, finalWidth, finalHeight)
    }

    /**
     * Draws an image with automatic scaling to fit the specified width while maintaining aspect ratio.
     */
    fun drawImageWithFixedWidth(
        contentStream: PDPageContentStream,
        file: File,
        x: Float,
        y: Float,
        targetWidth: Float
    ) {
        val pdImage = PDImageXObject.createFromFile(file.absolutePath, document)
        val aspectRatio = pdImage.height.toFloat() / pdImage.width.toFloat()
        val targetHeight = targetWidth * aspectRatio
        
        contentStream.drawImage(pdImage, x, y, targetWidth, targetHeight)
    }
}
