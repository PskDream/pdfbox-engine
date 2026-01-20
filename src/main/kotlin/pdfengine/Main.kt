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
            engine.writeLine(
                "orem Ipsum is simply dummy text of the printing and typesetting industry. " +
                        "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an" +
                        " unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived" +
                        " not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged." +
                        " It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, " +
                        "and more recently with desktop publishing software like Aldus PageMaker" +
                        " including versions of Lorem Ipsum.", wrapText = true
            )
            engine.setStyle("Sarabun", FontStyle.REGULAR)
            engine.writeLine(
                "Lorem Ipsum คือ เนื้อหาจำลองแบบเรียบๆ ที่ใช้กันในธุรกิจงานพิมพ์หรืองานเรียงพิมพ์ มันได้กลายมาเป็นเนื้อหาจำลองมาตรฐานของธุรกิจดังกล่าวมาตั้งแต่ศตวรรษที่ 16 เมื่อเครื่องพิมพ์โนเนมเครื่องหนึ่งนำรางตัวพิมพ์มาสลับสับตำแหน่งตัวอักษรเพื่อทำหนังสือตัวอย่าง Lorem Ipsum อยู่ยงคงกระพันมาไม่ใช่แค่เพียงห้าศตวรรษ แต่อยู่มาจนถึงยุคที่พลิกโฉมเข้าสู่งานเรียงพิมพ์ด้วยวิธีทางอิเล็กทรอนิกส์ และยังคงสภาพเดิมไว้อย่างไม่มีการเปลี่ยนแปลง มันได้รับความนิยมมากขึ้นในยุค ค.ศ. 1960 เมื่อแผ่น Letraset วางจำหน่ายโดยมีข้อความบนนั้นเป็น Lorem Ipsum และล่าสุดกว่านั้น คือเมื่อซอฟท์แวร์การทำสื่อสิ่งพิมพ์ (Desktop Publishing) อย่าง Aldus PageMaker ได้รวมเอา Lorem Ipsum เวอร์ชั่นต่างๆ เข้าไว้ในซอฟท์แวร์ด้วย",
                wrapText = true
            )
            engine.newLine()
            engine.writeText("x1")
            engine.writeText("x2", x=100f)


//            engine.document.
            val content = arrayOf<Array<String>>(
                arrayOf<String>("ID", "Name", "Role"),
                arrayOf<String>("001", "Alice Smith", "Developer"),
                arrayOf<String>("002", "Bob Jones", "Designer"),
                arrayOf<String>("003", "Charlie Day", "Manager")
            )

            engine.save("advanced_pdf_engine_example.pdf")


        }
    }
}
