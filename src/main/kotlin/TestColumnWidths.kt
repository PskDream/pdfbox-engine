import org.apache.pdfbox.pdmodel.PDDocument
import pdfengine.*
import java.io.File
import java.util.*

fun main() {
    val doc = PDDocument()
    val userHome = System.getProperty("user.home")
    val outputFile = "test_column_widths.pdf"

    doc.use {
        PdfEngine(it).use { engine ->
            engine.setBreakIterator(Locale.forLanguageTag("th-TH"))

            val sarabunRegular = File("$userHome/Library/Fonts/Sarabun/Sarabun-Regular.ttf")
            if (sarabunRegular.exists()) {
                engine.fontManager.addFont("Sarabun", FontStyle.REGULAR, sarabunRegular)
            }
            engine.setFont("Sarabun", FontStyle.REGULAR)

            engine.drawTextLine("ทดสอบตารางกำหนดความกว้างคอลัมน์", alignment = HorizontalAlignment.CENTER)
            engine.newLine()

            // ตารางที่มีการกำหนดความกว้างคอลัมน์
            engine.drawTable {
                headers("ลำดับ", "รายการ", "ราคา")
                row("1", "ส้มโอหวลหวานจากสวนนนทบุรี", "150.00")
                row("2", "มังคุดคัดเกรดพรีเมียมจากนครศรีธรรมราช", "200.00")
                row("3", "ทุเรียนหมอนทอง", "500.00")
                
                config {
                    // กำหนดความกว้างแต่ละคอลัมน์
                    columnWidths(listOf(50f, 350f, 100f))
                    headerBackgroundColor(PdfColor.LIGHT_GRAY)
                    cellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
                }
            }

            engine.newLine(2f)
            engine.drawTextLine("ตารางปกติ (เฉลี่ยความกว้าง)", alignment = HorizontalAlignment.CENTER)
            engine.newLine()

            engine.drawTable {
                headers("ลำดับ", "รายการ", "ราคา")
                row("1", "ส้มโอหวลหวานจากสวนนนทบุรี", "150.00")
                row("2", "มังคุดคัดเกรดพรีเมียมจากนครศรีธรรมราช", "200.00")
                row("3", "ทุเรียนหมอนทอง", "500.00")
            }

            engine.save(outputFile)
            println("PDF created successfully: $outputFile")
        }
    }
}
