import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import pdfengine.PdfEngine
import pdfengine.CellAlignment
import pdfengine.FontStyle
import pdfengine.HorizontalAlignment
import pdfengine.VerticalAlignment
import pdfengine.PdfColor
import pdfengine.TableConfig
import java.io.File
import java.util.*

fun main() {
    val doc = PDDocument()
    val userHome = System.getProperty("user.home")
    doc.use {
        PdfEngine(it).use { engine ->
            engine.setBreakIterator(Locale.forLanguageTag("th-TH"))
            engine.pageConfig { defaultLineSpacingFactor(1.5f) }

            engine.fontManager.addStandardFont("Helvetica", FontStyle.REGULAR, Standard14Fonts.FontName.HELVETICA)
            engine.fontManager.addStandardFont(
                "Helvetica",
                FontStyle.BOLD,
                Standard14Fonts.FontName.HELVETICA_BOLD
            )

            engine.fontManager.addFont(
                name = "Sarabun",
                FontStyle.REGULAR,
                File("$userHome/Library/Fonts/Sarabun/Sarabun-Regular.ttf"),
                useShaping = true
            )

            engine.setFont("Helvetica", FontStyle.BOLD)
//            engine.setFontSize(24f)
            engine.drawTextLine(
                "Hello World ",
                alignment = HorizontalAlignment.CENTER
            )
//            engine.setFontSize(12f)
            engine.setFont("Helvetica", FontStyle.REGULAR)
//            engine.drawTextLine(
//                "orem Ipsum is simply dummy text of the printing and typesetting industry. " +
//                        "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an" +
//                        " unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived" +
//                        " not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged." +
//                        " It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, " +
//                        "and more recently with desktop publishing software like Aldus PageMaker" +
//                        " including versions of Lorem Ipsum.", wrapText = true
//            )
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawTextLine(
                "Lorem Ipsum คือ เนื้อหาจำลองแบบเรียบๆ ที่ใช้กันในธุรกิจงานพิมพ์หรืองานเรียงพิมพ์ มันได้กลายมาเป็นเนื้อหาจำลองมาตรฐานของธุรกิจดังกล่าวมาตั้งแต่ศตวรรษที่ 16 เมื่อเครื่องพิมพ์โนเนมเครื่องหนึ่งนำรางตัวพิมพ์มาสลับสับตำแหน่งตัวอักษรเพื่อทำหนังสือตัวอย่าง Lorem Ipsum อยู่ยงคงกระพันมาไม่ใช่แค่เพียงห้าศตวรรษ แต่อยู่มาจนถึงยุคที่พลิกโฉมเข้าสู่งานเรียงพิมพ์ด้วยวิธีทางอิเล็กทรอนิกส์ และยังคงสภาพเดิมไว้อย่างไม่มีการเปลี่ยนแปลง มันได้รับความนิยมมากขึ้นในยุค ค.ศ. 1960 เมื่อแผ่น Letraset วางจำหน่ายโดยมีข้อความบนนั้นเป็น Lorem Ipsum และล่าสุดกว่านั้น คือเมื่อซอฟท์แวร์การทำสื่อสิ่งพิมพ์ (Desktop Publishing) อย่าง Aldus PageMaker ได้รวมเอา Lorem Ipsum เวอร์ชั่นต่างๆ เข้าไว้ในซอฟท์แวร์ด้วย",
                wrapText = true, alignment = HorizontalAlignment.CENTER
            )
            engine.drawText("x1")
            engine.drawText("x2", x = 100f)
            engine.newLine()

            // Configure line spacing and padding

//            engine.setTableCellPadding(horizontal = 5f, vertical = 5f)  // Custom padding

            // Example 1: Basic table with blue header
            engine.setFont("Helvetica", FontStyle.REGULAR)
            engine.drawTextLine("Example 1: Employee Table (Blue Theme)")
            engine.newLine(1f)

            val headers = listOf("ID", "Name", "Role")
            val rows = listOf(
                listOf("001", "Alice Smith", "Developer Developer Developer Developer"),
                listOf("002", "Bob Jones", "Designer"),
                listOf("003", "Charlie Day", "Manager"),
                listOf("004", "Diana Prince", "Lead"),
                listOf("005", "Edward Norton", "Architect")
            )

            engine.drawTable(
                headers = headers,
                rows = rows,
                tableConfig = TableConfig(),
                autoHeight = true,
                borderColor = PdfColor.BLACK,
                headerBackgroundColor = PdfColor(0.2f, 0.4f, 0.7f),
                headerFontColor = PdfColor.WHITE,
                rowFontColor = PdfColor.BLACK,
                alternateRowColor = PdfColor(0.95f, 0.95f, 0.95f),
                borderWidth = 1.5f,
//                lineSpacingFactor = 1.1f  // Custom line spacing for this table
            )

            engine.newLine(3f)

            // Example 2: Green theme product table
            engine.setFontSize(18f)
            engine.drawTextLine("Example 2: Product Table (Green Theme) with Long Descriptions")
            engine.newLine(1f)

            val productHeaders = listOf("Product", "Description", "Price")
            val productRows = listOf(
                listOf(
                    "Laptop",
                    "High performance laptop with 16GB RAM and SSD storage suitable for development and design work",
                    "$999"
                ),
                listOf("Mouse", "Ergonomic wireless mouse with precision tracking", "$25"),
                listOf(
                    "Keyboard",
                    "Mechanical keyboard with RGB backlight and programmable keys for gaming and productivity",
                    "$75"
                ),
                listOf("Monitor", "4K Ultra HD monitor with HDR support perfect for professional work", "$299")
            )

            engine.drawTable(
                headers = productHeaders,
                rows = productRows,
                tableConfig = TableConfig(),
                autoHeight = true,
                borderColor = PdfColor(0f, 0.5f, 0f),
                headerBackgroundColor = PdfColor(0.1f, 0.6f, 0.1f),
                headerFontColor = PdfColor.WHITE,
                rowFontColor = PdfColor(0f, 0.3f, 0f),
                alternateRowColor = PdfColor(0.9f, 0.98f, 0.9f),
                borderWidth = 2f,
                lineSpacingFactor = 1.2f  // Tight line spacing for compact table
            )

            engine.newLine(3f)

            // Example 3: Dark theme score table with fixed height
            engine.drawTextLine("Example 3: Score Table (Dark Theme - Fixed Height)")
            engine.newLine(1f)

            val scoreHeaders = listOf("Student", "Math", "English", "Science")
            val scoreRows = listOf(
                listOf("John", "95", "88", "92"),
                listOf("Jane", "92", "94", "89"),
                listOf("Tom", "88", "85", "91")
            )

            engine.drawTable(
                headers = scoreHeaders,
                rows = scoreRows,
                tableConfig = TableConfig(),
                cellHeight = 25f,
                autoHeight = false,
                borderColor = PdfColor(0.3f, 0.3f, 0.3f),
                headerBackgroundColor = PdfColor(0.2f, 0.2f, 0.2f),
                headerFontColor = PdfColor(1f, 1f, 0f),
                rowFontColor = PdfColor(0.1f, 0.1f, 0.1f),
                alternateRowColor = PdfColor(0.92f, 0.92f, 0.92f),
                borderWidth = 1f
            )

            engine.newLine(3f)

            // Example 4: Thai text with orange theme and auto height
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawTextLine("Example 4: ตารางข้อมูลพนักงาน (Orange Theme - Auto Height)")
            engine.newLine(1f)

            val thaiHeaders = listOf("เลขประจำตัว", "ชื่อ", "ตำแหน่ง")
            val thaiRows = listOf(
                listOf("001", "สมชาย ใจดี", "โปรแกรมเมอร์ระดับ Senior ที่มีประสบการณ์มากกว่า 5 ปี"),
                listOf("002", "สมหญิง สุขใจ", "ออกแบบ UI/UX ชำนาญเรื่องการออกแบบ Interface"),
                listOf("003", "ชัยวัฒน์ สงคราม", "ผู้บริหารโครงการ Project Manager"),
                listOf("004", "นิดา จันทร์สว่าง", "นักวิเคราะห์ระบบ Business Analyst")
            )

            engine.drawTable(
                headers = thaiHeaders,
                rows = thaiRows,
                autoHeight = true,
                tableConfig = TableConfig(),
                borderColor = PdfColor(0.8f, 0.4f, 0f),
                headerBackgroundColor = PdfColor(0.8f, 0.4f, 0f),
                headerFontColor = PdfColor.WHITE,
                rowFontColor = PdfColor(0.5f, 0.3f, 0.1f),
                alternateRowColor = PdfColor(0.98f, 0.95f, 0.9f),
                borderWidth = 1.5f,
                lineSpacingFactor = 1.3f  // Slightly more spacing for Thai text
            )

            engine.newLine(3f)

            // Example 5: Cell alignment demonstration
            engine.drawTextLine("Example 5: Cell Alignment Demonstration")
            engine.newLine(1f)

            val alignmentHeaders = listOf("Left", "Center", "Right")
            val alignmentRows = listOf(
                listOf("Product A", "High quality item", "100.00"),
                listOf("Product B", "Medium grade product", "75.50"),
                listOf("Product C", "Economy option", "25.99")
            )

            engine.drawTable(
                headers = alignmentHeaders,
                rows = alignmentRows,
                tableConfig = TableConfig(),
                autoHeight = true,
                borderColor = PdfColor(0.2f, 0.2f, 0.2f),
                headerBackgroundColor = PdfColor(0.3f, 0.3f, 0.3f),
                headerFontColor = PdfColor.WHITE,
                rowFontColor = PdfColor.BLACK,
                alternateRowColor = PdfColor(0.98f, 0.98f, 0.98f),
                borderWidth = 1f,
                headerAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
                cellAlignment = CellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
            )

            engine.newLine(3f)

            // Example 6: Right-aligned numbers table
            engine.drawTextLine("Example 6: Right-Aligned Numbers")
            engine.newLine(1f)

            val numberHeaders = listOf("Item", "Quantity", "Price", "Total")
            val numberRows = listOf(
                listOf("Apple", "10", "2.50", "25.00"),
                listOf("Orange", "8", "1.75", "14.00"),
                listOf("Banana", "15", "0.50", "7.50"),
                listOf("Mango", "5", "3.00", "15.00")
            )

            engine.drawTable(
                headers = numberHeaders,
                rows = numberRows,
                tableConfig = TableConfig(),
                autoHeight = true,
                borderColor = PdfColor(0f, 0.4f, 0.7f),
                headerBackgroundColor = PdfColor(0f, 0.4f, 0.7f),
                headerFontColor = PdfColor.WHITE,
                rowFontColor = PdfColor(0.1f, 0.1f, 0.1f),
                alternateRowColor = PdfColor(0.95f, 0.98f, 1f),
                borderWidth = 1.5f,
                headerAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
                cellAlignment = CellAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE)
            )

            engine.newLine(3f)

            // Example 7: Top aligned text
            engine.drawTextLine("Example 7: Top-Aligned Content")
            engine.newLine(1f)

            val topAlignHeaders = listOf("Title", "Description")
            val topAlignRows = listOf(
                listOf(
                    "Feature A",
                    "This is a long description that wraps to multiple lines and demonstrates top alignment"
                ),
                listOf("Feature B", "Another description for testing vertical alignment"),
                listOf("Feature C", "Final test row with top vertical alignment")
            )

            engine.drawTable(
                headers = topAlignHeaders,
                rows = topAlignRows,
                cellHeight = 60f,
                autoHeight = false,
                borderColor = PdfColor(0.3f, 0.5f, 0.2f),
                headerBackgroundColor = PdfColor(0.4f, 0.7f, 0.3f),
                headerFontColor = PdfColor.WHITE,
                rowFontColor = PdfColor(0.2f, 0.2f, 0.2f),
                alternateRowColor = PdfColor(0.96f, 0.99f, 0.95f),
                borderWidth = 1.5f,
                headerAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
                cellAlignment = CellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
            )

            engine.newLine(3f)

            // Example 8: Bottom aligned text
            engine.drawTextLine("Example 8: Bottom-Aligned Content")
            engine.newLine(1f)

            val bottomAlignHeaders = listOf("Status", "Notes")
            val bottomAlignRows = listOf(
                listOf("Active", "Item is currently active in the system"),
                listOf("Pending", "Awaiting approval from administrator"),
                listOf("Inactive", "No longer in use")
            )

            engine.drawTable {
                headers("Status", "Notes")
                row("Active", "Item is currently active in the system")
                row("Pending", "Awaiting approval from administrator")
                row("Inactive", "No longer in use")
                config {
                    cellHeight(50f)
                        .autoHeight(false)
                        .borderColor(PdfColor(0.7f, 0.3f, 0.1f))
                        .headerBackgroundColor(PdfColor(0.8f, 0.5f, 0.2f))
                        .headerFontColor(PdfColor.WHITE)
                        .rowFontColor(PdfColor(0.3f, 0.1f, 0f))
                        .alternateRowColor(PdfColor(0.99f, 0.95f, 0.92f))
                        .borderWidth(1f)
                        .headerAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
                        .cellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
                }
            }

            // Example 9: Image support
            engine.addNewPage()
            engine.setFont("Helvetica", FontStyle.BOLD)
            engine.drawTextLine("Example 9: Image Support")
//            engine.newLine(1f)

            // Note: Since I don't have a real image file path, 
            // I'll assume there might be a placeholder or just document how to use it.
            // In a real scenario, you would pass a valid File object.
            val imageFile = File("1084-536x354-grayscale.jpg") // Replace with actual image path
            if (imageFile.exists()) {
                // Draw image and text on the same line
                val imgWidth = 100f
                val (imageWidth,imageHeight) = engine.drawImage(imageFile, width = imgWidth, updateY = false)
                // Use the new helper method for easier vertical alignment
                engine.drawTextInHeight(
                    "This is a long description that should wrap to multiple lines when using drawTextInHeight with wrapText=true next to an image.",
                    height = imageHeight,
                    width = engine.availableWidth - imageWidth,
                    x = 160f,
                    verticalAlignment = VerticalAlignment.MIDDLE,
                    wrapText = true,
//                    spacing = 1.2f
                )

                // Move to next line manually since we didn't update Y
                engine.newLine((imageHeight / 14f) + 1f)
                engine.drawTextLine("Back to normal block rendering.")
            } else {
                engine.setFont("Helvetica", FontStyle.REGULAR)
                engine.drawTextLine("[Image 'placeholder.png' not found - demonstration of API only]")
                engine.drawTextLine("Usage: engine.drawImage(File(\"path/to/image.png\"), width = 200f)")
            }

            engine.save("advanced_pdf_engine_example.pdf")
        }
    }
}
