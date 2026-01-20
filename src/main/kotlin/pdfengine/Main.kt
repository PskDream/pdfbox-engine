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
//            engine.setStyle("Sarabun", FontStyle.REGULAR)
//            engine.writeLine(
//                "Lorem Ipsum คือ เนื้อหาจำลองแบบเรียบๆ ที่ใช้กันในธุรกิจงานพิมพ์หรืองานเรียงพิมพ์ มันได้กลายมาเป็นเนื้อหาจำลองมาตรฐานของธุรกิจดังกล่าวมาตั้งแต่ศตวรรษที่ 16 เมื่อเครื่องพิมพ์โนเนมเครื่องหนึ่งนำรางตัวพิมพ์มาสลับสับตำแหน่งตัวอักษรเพื่อทำหนังสือตัวอย่าง Lorem Ipsum อยู่ยงคงกระพันมาไม่ใช่แค่เพียงห้าศตวรรษ แต่อยู่มาจนถึงยุคที่พลิกโฉมเข้าสู่งานเรียงพิมพ์ด้วยวิธีทางอิเล็กทรอนิกส์ และยังคงสภาพเดิมไว้อย่างไม่มีการเปลี่ยนแปลง มันได้รับความนิยมมากขึ้นในยุค ค.ศ. 1960 เมื่อแผ่น Letraset วางจำหน่ายโดยมีข้อความบนนั้นเป็น Lorem Ipsum และล่าสุดกว่านั้น คือเมื่อซอฟท์แวร์การทำสื่อสิ่งพิมพ์ (Desktop Publishing) อย่าง Aldus PageMaker ได้รวมเอา Lorem Ipsum เวอร์ชั่นต่างๆ เข้าไว้ในซอฟท์แวร์ด้วย",
//                wrapText = true
//            )
            engine.writeText("x1")
            engine.writeText("x2", x=100f)
            engine.newLine()

//            engine.addNewPage()

            // Example 1: Basic table with blue header
            engine.setStyle("Helvetica", FontStyle.REGULAR)
            engine.writeLine("Example 1: Employee Table (Blue Theme)")
            engine.newLine()

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
                cellHeight = 25f,
                borderColor = Triple(0f, 0f, 0f),
                headerBackgroundColor = Triple(0.2f, 0.4f, 0.7f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0f, 0f, 0f),
                alternateRowColor = Triple(0.95f, 0.95f, 0.95f),
                borderWidth = 1.5f
            )

            engine.newLine(3f)

            // Example 2: Green theme product table
            engine.writeLine("Example 2: Product Table (Green Theme)")
            engine.newLine()

            val productHeaders = listOf("Product", "Price", "Stock")
            val productRows = listOf(
                listOf("Laptop", "$999", "15"),
                listOf("Mouse", "$25", "50"),
                listOf("Keyboard", "$75", "30"),
                listOf("Monitor", "$299", "8")
            )

            engine.drawTable(
                headers = productHeaders,
                rows = productRows,
                cellHeight = 25f,
                borderColor = Triple(0f, 0.5f, 0f),
                headerBackgroundColor = Triple(0.1f, 0.6f, 0.1f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0f, 0.3f, 0f),
                alternateRowColor = Triple(0.9f, 0.98f, 0.9f),
                borderWidth = 2f
            )

            engine.newLine(3f)

            // Example 3: Dark theme score table
            engine.writeLine("Example 3: Score Table (Dark Theme)")
            engine.newLine()

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
                borderColor = Triple(0.3f, 0.3f, 0.3f),
                headerBackgroundColor = Triple(0.2f, 0.2f, 0.2f),
                headerFontColor = Triple(1f, 1f, 0f),
                rowFontColor = Triple(0.1f, 0.1f, 0.1f),
                alternateRowColor = Triple(0.92f, 0.92f, 0.92f),
                borderWidth = 1f
            )

            engine.newLine(3f)

            // Example 4: Thai text with orange theme
            engine.setStyle("Sarabun", FontStyle.REGULAR)
            engine.writeLine("Example 4: ตารางข้อมูลพนักงาน (Orange Theme)")
            engine.newLine()

            val thaiHeaders = listOf("เลขประจำตัว", "ชื่อ", "ตำแหน่ง")
            val thaiRows = listOf(
                listOf("001", "สมชาย ใจดี", "โปรแกรมเมอร์"),
                listOf("002", "สมหญิง สุขใจ", "ออกแบบ"),
                listOf("003", "ชัยวัฒน์ สงคราม", "ผู้บริหาร"),
                listOf("004", "นิดา จันทร์สว่าง", "นักวิเคราะห์")
            )

            engine.drawTable(
                headers = thaiHeaders,
                rows = thaiRows,
                cellHeight = 25f,
                borderColor = Triple(0.8f, 0.4f, 0f),
                headerBackgroundColor = Triple(0.8f, 0.4f, 0f),
                headerFontColor = Triple(1f, 1f, 1f),
                rowFontColor = Triple(0.5f, 0.3f, 0.1f),
                alternateRowColor = Triple(0.98f, 0.95f, 0.9f),
                borderWidth = 1.5f
            )

            engine.save("advanced_pdf_engine_example.pdf")


        }
    }
}
