package pdfengine

import java.text.BreakIterator
import org.apache.pdfbox.pdmodel.font.PDFont

import java.util.Locale

class TextWrapper() {

    private var breakIteratorCache: ThreadLocal<BreakIterator>? = null

    fun setLocale(locale: Locale) {
        if (breakIteratorCache == null) {
            this.breakIteratorCache = ThreadLocal.withInitial { BreakIterator.getWordInstance(locale) }
        } else {
            throw IllegalStateException("BreakIterator already initialized.")
        }
    }

    private fun getBreakIterator(): BreakIterator {
        return breakIteratorCache?.get() ?: throw IllegalStateException("BreakIterator not initialized. Call setLocale(Locale) first.")
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
     */
    fun wrapText(text: String, font: PDFont, fontSize: Float, maxWidth: Float): List<String> {
        val breakIterator = getBreakIterator()
        breakIterator.setText(text)
        val lines = mutableListOf<String>()
        var currentLine = ""
        var start = breakIterator.first()
        var end = breakIterator.next()

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
            end = breakIterator.next()
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}
