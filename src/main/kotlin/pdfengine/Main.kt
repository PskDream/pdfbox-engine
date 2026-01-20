package pdfengine

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.File
import java.util.*

fun main() {
    val doc = PDDocument()
    val userHome = System.getProperty("user.home")
    doc.use {
        AdvancedPdfEngine(it).use { engine ->
            engine.setBreakIterator(Locale.forLanguageTag("th-TH"))
            engine.setLineSpacingFactor(2f)  // 130% line spacing for body text

            engine.fontManager.registerStandardFont("Helvetica", FontStyle.REGULAR, Standard14Fonts.FontName.HELVETICA)
            engine.fontManager.registerStandardFont(
                "Helvetica",
                FontStyle.BOLD,
                Standard14Fonts.FontName.HELVETICA_BOLD
            )

            engine.fontManager.registerCustomFont(
                name = "Sarabun",
                FontStyle.REGULAR,
                File("$userHome/Library/Fonts/Sarabun/Sarabun-Regular.ttf"),
                useShaping = true
            )

            engine.setStyle("Helvetica", FontStyle.BOLD)
//            engine.setSize(24f)
            engine.writeLine("Hello World ", x = engine.getAvailableWidth() / 2)
//            engine.setSize(12f)
            engine.setStyle("Helvetica", FontStyle.REGULAR)
//            engine.writeLine(
//                "orem Ipsum is simply dummy text of the printing and typesetting industry. " +
//                        "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an" +
//                        " unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived" +
//                        " not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged." +
//                        " It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, " +
//                        "and more recently with desktop publishing software like Aldus PageMaker" +
//                        " including versions of Lorem Ipsum.", wrapText = true
//            )
            engine.setStyle("Sarabun", FontStyle.REGULAR)
            engine.writeLine(
                "Lorem Ipsum คือ เนื้อหาจำลองแบบเรียบๆ ที่ใช้กันในธุรกิจงานพิมพ์หรืองานเรียงพิมพ์ มันได้กลายมาเป็นเนื้อหาจำลองมาตรฐานของธุรกิจดังกล่าวมาตั้งแต่ศตวรรษที่ 16 เมื่อเครื่องพิมพ์โนเนมเครื่องหนึ่งนำรางตัวพิมพ์มาสลับสับตำแหน่งตัวอักษรเพื่อทำหนังสือตัวอย่าง Lorem Ipsum อยู่ยงคงกระพันมาไม่ใช่แค่เพียงห้าศตวรรษ แต่อยู่มาจนถึงยุคที่พลิกโฉมเข้าสู่งานเรียงพิมพ์ด้วยวิธีทางอิเล็กทรอนิกส์ และยังคงสภาพเดิมไว้อย่างไม่มีการเปลี่ยนแปลง มันได้รับความนิยมมากขึ้นในยุค ค.ศ. 1960 เมื่อแผ่น Letraset วางจำหน่ายโดยมีข้อความบนนั้นเป็น Lorem Ipsum และล่าสุดกว่านั้น คือเมื่อซอฟท์แวร์การทำสื่อสิ่งพิมพ์ (Desktop Publishing) อย่าง Aldus PageMaker ได้รวมเอา Lorem Ipsum เวอร์ชั่นต่างๆ เข้าไว้ในซอฟท์แวร์ด้วย",
                wrapText = true
            )
            engine.writeText("x1")
            engine.writeText("x2", x=100f)
            engine.newLine()

            // Configure line spacing and padding

//            engine.setTableCellPadding(horizontal = 5f, vertical = 5f)  // Custom padding

            // Example 1: Basic table with blue header
            engine.setStyle("Helvetica", FontStyle.REGULAR)
            engine.writeLine("Example 1: Employee Table (Blue Theme)")
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
                autoHeight = true,
                borderColor = Triple(0f, 0f, 0f),
                headerBackgroundColor = Triple(0.2f, 0.4f, 0.7f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0f, 0f, 0f),
                alternateRowColor = Triple(0.95f, 0.95f, 0.95f),
                borderWidth = 1.5f,
//                lineSpacingFactor = 1.1f  // Custom line spacing for this table
            )

            engine.newLine(3f)

            // Example 2: Green theme product table
            engine.setSize(18f)
            engine.writeLine("Example 2: Product Table (Green Theme) with Long Descriptions")
            engine.newLine(1f)

            val productHeaders = listOf("Product", "Description", "Price")
            val productRows = listOf(
                listOf("Laptop", "High performance laptop with 16GB RAM and SSD storage suitable for development and design work", "$999"),
                listOf("Mouse", "Ergonomic wireless mouse with precision tracking", "$25"),
                listOf("Keyboard", "Mechanical keyboard with RGB backlight and programmable keys for gaming and productivity", "$75"),
                listOf("Monitor", "4K Ultra HD monitor with HDR support perfect for professional work", "$299")
            )

            engine.drawTable(
                headers = productHeaders,
                rows = productRows,
                autoHeight = true,
                borderColor = Triple(0f, 0.5f, 0f),
                headerBackgroundColor = Triple(0.1f, 0.6f, 0.1f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0f, 0.3f, 0f),
                alternateRowColor = Triple(0.9f, 0.98f, 0.9f),
                borderWidth = 2f,
                lineSpacingFactor = 1.2f  // Tight line spacing for compact table
            )

            engine.newLine(3f)

            // Example 3: Dark theme score table with fixed height
            engine.writeLine("Example 3: Score Table (Dark Theme - Fixed Height)")
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
                cellHeight = 25f,
                autoHeight = false,
                borderColor = Triple(0.3f, 0.3f, 0.3f),
                headerBackgroundColor = Triple(0.2f, 0.2f, 0.2f),
                headerFontColor = Triple(1f, 1f, 0f),
                rowFontColor = Triple(0.1f, 0.1f, 0.1f),
                alternateRowColor = Triple(0.92f, 0.92f, 0.92f),
                borderWidth = 1f
            )

            engine.newLine(3f)

            // Example 4: Thai text with orange theme and auto height
            engine.setStyle("Sarabun", FontStyle.REGULAR)
            engine.writeLine("Example 4: ตารางข้อมูลพนักงาน (Orange Theme - Auto Height)")
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
                borderColor = Triple(0.8f, 0.4f, 0f),
                headerBackgroundColor = Triple(0.8f, 0.4f, 0f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0.5f, 0.3f, 0.1f),
                alternateRowColor = Triple(0.98f, 0.95f, 0.9f),
                borderWidth = 1.5f,
                lineSpacingFactor = 1.15f  // Slightly more spacing for Thai text
            )

            engine.newLine(3f)

            // Example 5: Cell alignment demonstration
            engine.writeLine("Example 5: Cell Alignment Demonstration")
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
                autoHeight = true,
                borderColor = Triple(0.2f, 0.2f, 0.2f),
                headerBackgroundColor = Triple(0.3f, 0.3f, 0.3f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0f, 0f, 0f),
                alternateRowColor = Triple(0.98f, 0.98f, 0.98f),
                borderWidth = 1f,
                headerAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
                cellAlignment = CellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
            )

            engine.newLine(3f)

            // Example 6: Right-aligned numbers table
            engine.writeLine("Example 6: Right-Aligned Numbers")
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
                autoHeight = true,
                borderColor = Triple(0f, 0.4f, 0.7f),
                headerBackgroundColor = Triple(0f, 0.4f, 0.7f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0.1f, 0.1f, 0.1f),
                alternateRowColor = Triple(0.95f, 0.98f, 1f),
                borderWidth = 1.5f,
                headerAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
                cellAlignment = CellAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE)
            )

            engine.newLine(3f)

            // Example 7: Top aligned text
            engine.writeLine("Example 7: Top-Aligned Content")
            engine.newLine(1f)

            val topAlignHeaders = listOf("Title", "Description")
            val topAlignRows = listOf(
                listOf("Feature A", "This is a long description that wraps to multiple lines and demonstrates top alignment"),
                listOf("Feature B", "Another description for testing vertical alignment"),
                listOf("Feature C", "Final test row with top vertical alignment")
            )

            engine.drawTable(
                headers = topAlignHeaders,
                rows = topAlignRows,
                cellHeight = 60f,
                autoHeight = false,
                borderColor = Triple(0.3f, 0.5f, 0.2f),
                headerBackgroundColor = Triple(0.4f, 0.7f, 0.3f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0.2f, 0.2f, 0.2f),
                alternateRowColor = Triple(0.96f, 0.99f, 0.95f),
                borderWidth = 1.5f,
                headerAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
                cellAlignment = CellAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
            )

            engine.newLine(3f)

            // Example 8: Bottom aligned text
            engine.writeLine("Example 8: Bottom-Aligned Content")
            engine.newLine(1f)

            val bottomAlignHeaders = listOf("Status", "Notes")
            val bottomAlignRows = listOf(
                listOf("Active", "Item is currently active in the system"),
                listOf("Pending", "Awaiting approval from administrator"),
                listOf("Inactive", "No longer in use")
            )

            engine.drawTable(
                headers = bottomAlignHeaders,
                rows = bottomAlignRows,
                cellHeight = 50f,
                autoHeight = false,
                borderColor = Triple(0.7f, 0.3f, 0.1f),
                headerBackgroundColor = Triple(0.8f, 0.5f, 0.2f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0.3f, 0.1f, 0f),
                alternateRowColor = Triple(0.99f, 0.95f, 0.92f),
                borderWidth = 1f,
                headerAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
                cellAlignment = CellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
            )

            engine.save("advanced_pdf_engine_example.pdf")


        }
    }
}
