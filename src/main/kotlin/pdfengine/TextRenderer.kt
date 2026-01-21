package pdfengine

import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.util.Matrix
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform

class TextRenderer(private val frc: FontRenderContext = FontRenderContext(AffineTransform(), true, true)) {

    fun drawText(
        contentStream: PDPageContentStream,
        text: String,
        x: Float,
        y: Float,
        fontPair: PdfFont,
        fontSize: Float
    ) {
        if (isGlyphsRenderingNeeded(fontPair)) {
            drawTextGlyphs(contentStream, text, x, y, fontPair, fontSize)
        } else {
            drawTextNative(contentStream, text, x, y, fontPair.pdFont, fontSize)
        }
    }

    private fun isGlyphsRenderingNeeded(fontPair: PdfFont): Boolean {
        return fontPair.useShaping && fontPair.awtFont != null
    }

    private fun drawTextGlyphs(
        contentStream: PDPageContentStream,
        text: String,
        x: Float,
        y: Float,
        fontPair: PdfFont,
        fontSize: Float
    ) {
        val awtFont = fontPair.awtFont ?: return
        val pdFont = fontPair.pdFont

        val derivedFont = awtFont.deriveFont(fontSize)
        val gv: GlyphVector = derivedFont.layoutGlyphVector(
            frc, text.toCharArray(), 0, text.length, Font.LAYOUT_LEFT_TO_RIGHT
        )

        contentStream.beginText()
        contentStream.setFont(pdFont, fontSize)

        for (i in 0 until gv.numGlyphs) {
            val glyphId = gv.getGlyphCode(i)
            val pos = gv.getGlyphPosition(i)

            // Calculate exact position for each glyph
            val gx = x + pos.x.toFloat()
            val gy = y - pos.y.toFloat()
            contentStream.setTextMatrix(Matrix.getTranslateInstance(gx, gy))

            val hex = String.format("%04X", glyphId)
            contentStream.appendRawCommands("<$hex> Tj ")
        }
        contentStream.endText()
    }

    private fun drawTextNative(
        contentStream: PDPageContentStream,
        text: String,
        x: Float,
        y: Float,
        pdFont: PDFont,
        fontSize: Float
    ) {
        contentStream.beginText()
        contentStream.setFont(pdFont, fontSize)
        contentStream.setTextMatrix(Matrix.getTranslateInstance(x, y))
        try {
            contentStream.showText(text)
        } catch (e: Exception) {
            // Fallback for characters not in font (very basic)
            val sanitized = text.filter { char ->
                try {
                    pdFont.encode(char.toString())
                    true
                } catch (ex: Exception) {
                    false
                }
            }
            if (sanitized.isNotEmpty()) {
                contentStream.showText(sanitized)
            }
        }
        contentStream.endText()
    }
}
