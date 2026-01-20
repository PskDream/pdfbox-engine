
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.min

/**
 * ตัวอย่างการใช้ PDFBox กับ Java 2D และ HarfBuzz สำหรับภาษาไทย
 * ใช้ฟอนต์ Sarabun จาก Google Fonts
 *
 * วิธีการติดตั้งฟอนต์:
 * 1. ดาวน์โหลด Sarabun จาก https://fonts.google.com/specimen/Sarabun
 * 2. แตกไฟล์และคัดลอกไปที่ ~/Library/Fonts/Sarabun/
 * 3. รันโปรแกรม
 */
class ThaiPDFExample(private val FONT_PATH: String) {

    /**
     * วิธีที่ 1: ใช้ PDFBox โดยตรงกับฟอนต์ Sarabun
     */
    @Throws(IOException::class)
    fun createPDFWithFont(outputPath: String?) {
        val document = PDDocument()
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        // โหลดฟอนต์ Sarabun
        val fontFile = File(FONT_PATH)
        val font = PDType0Font.load(document, fontFile)

        val contentStream = PDPageContentStream(document, page)


        // หัวเรื่อง
        contentStream.beginText()
        contentStream.setFont(font, 28f)
        contentStream.setNonStrokingColor(0.2f, 0.3f, 0.6f) // สีน้ำเงิน
        contentStream.newLineAtOffset(50f, 750f)
        contentStream.showText("ทดสอบฟอนต์ Sarabun ใน PDF")
        contentStream.endText()


        // เนื้อหา
        contentStream.beginText()
        contentStream.setFont(font, 16f)
        contentStream.setNonStrokingColor(0f, 0f, 0f) // สีดำ
        contentStream.newLineAtOffset(50f, 700f)

        val lines = arrayOf<String?>(
            "สวัสดีครับ ยินดีต้อนรับสู่ตัวอย่างการใช้ PDFBox",
            "",
            "พยัญชนะไทย 44 ตัว:",
            "ก ข ฃ ค ฅ ฆ ง จ ฉ ช ซ ฌ ญ ฎ ฏ",
            "ฐ ฑ ฒ ณ ด ต ถ ท ธ น บ ป ผ ฝ พ ฟ",
            "ภ ม ย ร ฤ ล ฦ ว ศ ษ ส ห ฬ อ ฮ",
            "",
            "สระและวรรณยุกต์:",
            "า ิ ี ึ ื ุ ู เ แ โ ใ ไ ็ ่ ้ ๊ ๋ ์ ํ ๎",
            "",
            "ตัวอย่างคำและประโยค:",
            "• กระต่าย กระรอก กระเต้า กระดาษ",
            "• ไก่ หมู วัว ควาย ม้า แกะ เป็ด ห่าน",
            "• วันนี้อากาศดีมาก ๆ เลย",
            "• ภาษาไทยเป็นภาษาที่สวยงามและมีเอกลักษณ์",
            "• การจัดการสระและวรรณยุกต์ถูกต้องแล้ว",
            "",
            "ทดสอบตัวเลขไทย: ๐ ๑ ๒ ๓ ๔ ๕ ๖ ๗ ๘ ๙",
            "ตัวเลขอารบิก: 0 1 2 3 4 5 6 7 8 9"
        )

        for (line in lines) {
            contentStream.showText(line)
            contentStream.newLineAtOffset(0f, -25f)
        }

        contentStream.endText()


        // footer
        contentStream.beginText()
        contentStream.setFont(font, 12f)
        contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f)
        contentStream.newLineAtOffset(50f, 50f)
        contentStream.showText("สร้างด้วย Apache PDFBox + Sarabun Font")
        contentStream.endText()

        contentStream.close()
        document.save(outputPath)
        document.close()
    }

    /**
     * วิธีที่ 2: ใช้ Java 2D วาดข้อความ (รองรับ HarfBuzz ผ่าน JDK 11+)
     * แล้วแปลงเป็นรูปภาพใส่ใน PDF
     */
    @Throws(IOException::class)
    fun createPDFWithJava2D(outputPath: String?) {
        // สร้าง BufferedImage
        val width = 900
        val height = 1200
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()

        // ตั้งค่าการ render คุณภาพสูง
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

        // พื้นหลังสีขาว
        g2d.setColor(Color.WHITE)
        g2d.fillRect(0, 0, width, height)

        // โหลดฟอนต์ Sarabun
        var sarabunFont: Font? = null
        try {
            sarabunFont = Font.createFont(Font.TRUETYPE_FONT, File(FONT_PATH))
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            ge.registerFont(sarabunFont)
        } catch (e: FontFormatException) {
            System.err.println("ไม่สามารถโหลดฟอนต์สำหรับ Java 2D")
            e.printStackTrace()
            return
        } catch (e: IOException) {
            System.err.println("ไม่สามารถโหลดฟอนต์สำหรับ Java 2D")
            e.printStackTrace()
            return
        }

        // หัวเรื่อง
        val titleFont = sarabunFont.deriveFont(Font.BOLD, 36f)
        g2d.setFont(titleFont)
        g2d.setColor(Color(51, 77, 153))
        g2d.drawString("ทดสอบ Java 2D + HarfBuzz", 50, 60)


        // เส้นแบ่ง
        g2d.setColor(Color(200, 200, 200))
        g2d.fillRect(50, 75, 800, 2)

        // เนื้อหา
        val bodyFont = sarabunFont.deriveFont(Font.PLAIN, 24f)
        g2d.setFont(bodyFont)
        g2d.setColor(Color.BLACK)

        var y = 120
        val thaiTexts = arrayOf<String?>(
            "✨ Java 2D รองรับการจัดวางข้อความภาษาไทยผ่าน HarfBuzz",
            "",
            "พยัญชนะไทย:",
            "ก ข ค ฆ ง จ ฉ ช ซ ฌ ญ ด ต ถ ท ธ น",
            "บ ป ผ ฝ พ ฟ ภ ม ย ร ล ว ศ ษ ส ห ฬ อ ฮ",
            "",
            "สระและวรรณยุกต์:",
            "กา กิ กี กึ กื กุ กู เก แก โก ใก ไก",
            "ก่ ก้ ก๊ ก๋ กํา กั กะ",
            "",
            "ตัวอย่างคำที่มีการเรียงซับซ้อน:",
            "• สวัสดี ครับ ค่ะ ขอบคุณ เดินทาง",
            "• ประเทศไทย กรุงเทพมหานคร",
            "• ผลไม้: มะม่วง มังคุด ทุเรียน ลำไย ลิ้นจี่",
            "• สัตว์: ช้าง เสือ หมี กระต่าย นก ปลา",
            "",
            "ทดสอบการเรนเดอร์ที่ซับซ้อน:",
            "🎯 ใช้งานได้ดีกับสระล่าง สระบน และวรรณยุกต์",
            "🎯 รองรับการจัดวางที่ถูกต้องตามหลักภาษา",
        )

        for (text in thaiTexts) {
            if (text!!.isEmpty()) {
                y += 15
            } else {
                g2d.drawString(text, 50, y)
                y += 40
            }
        }

        // ทดสอบสไตล์ต่าง ๆ
        y += 20
        val boldFont = sarabunFont.deriveFont(Font.BOLD, 28f)
        g2d.setFont(boldFont)
        g2d.setColor(Color(204, 0, 0))
        g2d.drawString("ตัวหนา: การทดสอบฟอนต์แบบตัวหนา", 50, y)

        y += 50
        val italicFont = sarabunFont.deriveFont(Font.ITALIC, 28f)
        g2d.setFont(italicFont)
        g2d.setColor(Color(0, 153, 0))
        g2d.drawString("ตัวเอียง: การทดสอบฟอนต์แบบตัวเอียง", 50, y)

        // Footer
        y = height - 40
        val footerFont = sarabunFont.deriveFont(Font.PLAIN, 18f)
        g2d.setFont(footerFont)
        g2d.setColor(Color(128, 128, 128))
        g2d.drawString("สร้างด้วย Java 2D + Apache PDFBox + Sarabun Font", 50, y)

        g2d.dispose()

        // บันทึกรูปภาพชั่วคระ
        val tempImage = File("temp_thai_text_sarabun.png")
        ImageIO.write(image, "PNG", tempImage)

        // สร้าง PDF และใส่รูปภาพ
        val document = PDDocument()
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        val pdImage = PDImageXObject.createFromFile(tempImage.getAbsolutePath(), document)

        val contentStream = PDPageContentStream(document, page)


        // ปรับขนาดรูปภาพให้พอดีกับหน้า
        val scale = min(
            page.getMediaBox().getWidth() / pdImage.getWidth(),
            page.getMediaBox().getHeight() / pdImage.getHeight()
        ) * 0.95f

        val xPos = (page.getMediaBox().getWidth() - (pdImage.getWidth() * scale)) / 2
        val yPos = page.getMediaBox().getHeight() - (pdImage.getHeight() * scale) - 20

        contentStream.drawImage(
            pdImage, xPos, yPos,
            pdImage.getWidth() * scale,
            pdImage.getHeight() * scale
        )

        contentStream.close()
        document.save(outputPath)
        document.close()

        // ลบไฟล์ชั่วคระ
        tempImage.delete()
    }
}



fun main(args: Array<String>) {
    val FONT_PATH = System.getProperty("user.home") +
            "/Library/Fonts/Sarabun/Sarabun-Regular.ttf"
    try {
        // ตรวจสอบว่ามีฟอนต์หรือไม่
        val fontFile = File(FONT_PATH)
        if (!fontFile.exists()) {
            System.err.println("❌ ไม่พบไฟล์ฟอนต์: " + FONT_PATH)
            System.err.println("กรุณาดาวน์โหลด Sarabun จาก https://fonts.google.com/specimen/Sarabun")
            System.err.println("และวางไฟล์ที่: " + FONT_PATH)
            return
        }

        println("✅ พบฟอนต์ Sarabun: " + FONT_PATH)

        val  pdfEngine = ThaiPDFExample(FONT_PATH)

        // วิธีที่ 1: สร้าง PDF ด้วย PDFBox โดยตรง
        pdfEngine.createPDFWithFont("output_direct_sarabun.pdf")
        println("✅ สร้าง output_direct_sarabun.pdf สำเร็จ")


        // วิธีที่ 2: ใช้ Java 2D วาดข้อความแล้วแปลงเป็นรูปภาพใส่ใน PDF
        pdfEngine.createPDFWithJava2D("output_java2d_sarabun.pdf")
        println("✅ สร้าง output_java2d_sarabun.pdf สำเร็จ")

        println("\n🎉 สร้าง PDF ทั้งหมดสำเร็จ!")
    } catch (e: Exception) {
        System.err.println("❌ เกิดข้อผิดพลาด:")
        e.printStackTrace()
    }
}

