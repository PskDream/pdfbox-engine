import org.apache.pdfbox.pdmodel.PDDocument
import pdfengine.*
import java.io.File
import java.util.*

fun main() {
    val doc = PDDocument()
    val userHome = System.getProperty("user.home")
    val outputFile = "ใบกำกับภาษี_example.pdf"

    doc.use {
        PdfEngine(it).use { engine ->
            // 1. Setup Locale and Fonts
            engine.setBreakIterator(Locale.forLanguageTag("th-TH"))

            // Register Sarabun font (Bold and Regular)
            val sarabunRegular = File("$userHome/Library/Fonts/Sarabun/Sarabun-Regular.ttf")
            val sarabunBold = File("$userHome/Library/Fonts/Sarabun/Sarabun-Bold.ttf")

            if (sarabunRegular.exists()) {
                engine.fontManager.addFont("Sarabun", FontStyle.REGULAR, sarabunRegular)
            }
            if (sarabunBold.exists()) {
                engine.fontManager.addFont("Sarabun", FontStyle.BOLD, sarabunBold)
            }

            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.pageConfig {
                marginTop(30f)
                marginLeft(40f)
                marginRight(40f)
                defaultFontSize(10f)
                defaultLineSpacingFactor(1.6f)
            }

            // 2. Header Section
            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.setFontSize(14f)
            engine.drawTextLine("การทางพิเศษแห่งประเทศไทย", alignment = HorizontalAlignment.CENTER)

            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.setFontSize(10f)
            engine.drawTextLine(
                "อาคารศูนย์บริหารทางพิเศษ กทพ. เลขที่ 111 ถนนริมคลองบางกะปิ แขวงบางกะปิ เขตห้วยขวาง กรุงเทพมหานคร 10310",
                alignment = HorizontalAlignment.CENTER
            )
            engine.drawTextLine(
                "โทรศัพท์ 02-558-9800 แฟกซ์ 02-558-9788, 02-558-9789", alignment =
                    HorizontalAlignment.CENTER
            )
            engine.drawTextLine(
                "เลขประจำตัวผู้เสียภาษีอากร 0994000165421 (00000)", alignment =
                    HorizontalAlignment.CENTER
            )

            engine.newLine(1.5f)
            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.setFontSize(12f)
            engine.drawTextLine("ใบกำกับภาษี", alignment = HorizontalAlignment.CENTER)
            engine.newLine()

            // 3. Customer Information Section
            engine.setFontSize(10f)

            // Row 1
            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("รหัสลูกค้า", x = 40f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("A1301106363", x = 110f)

            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("รหัสบัตร Smart Card(S/N)", x = 200f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("3443048944", x = 320f)

            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("เลขที่", x = 430f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("EE0332926010100604", x = 480f)

            engine.newLine()

            // Row 2: Customer Name and Date
            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("ชื่อลูกค้า", x = 40f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("กองทุน สวัสดิการ สำนักงาน คปภ.", x = 110f)

            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("วันที่", x = 430f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("01/01/2026", x = 480f)

            engine.newLine()

            // Row 3: Address
            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("ที่อยู่", x = 40f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("22/79 หมู่บ้านสำนักงาน คปภ. ถนนรัชดาภิเษก แขวงจันทรเกษม เขตจตุจักร", x = 110f)
            engine.drawText("สำนักงานใหญ่", x = 430f)

            engine.newLine()
            engine.drawText("จังหวัดกรุงเทพมหานคร 10900", x = 110f)

            engine.newLine()

            // Row 4: Tax ID and License Plate
            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("เลขประจำตัวผู้เสียภาษีอากรผู้ซื้อ", x = 40f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("0994000146221", x = 180f)

            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("ทะเบียนรถ", x = 330f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("9กฎ 5159 กรุงเทพมหานคร", x = 390f)

            engine.newLine()

            // 4. Items Table
            engine.drawTable {
                headers(
                    "ลำดับที่",
                    "อ้างถึงใบกำกับภาษี อย่างย่อเลขที่",
                    "รายการสินค้าหรือบริการ",
                    "จำนวน",
                    "ราคาต่อหน่วย",
                    "จำนวนเงิน"
                )
                for (i in 1..10){
                    row(
                        i.toString(), "EE03329260101",
                        "ค่าผ่านทางพิเศษ ด่านจตุโชติ บัตร Easy-Pass หมายเลข 3085860601276589019 วันที่ 01/01/2026",
                        "1", "45.00", "45.00"
                    )
                }
                config {
                    headerBackgroundColor(PdfColor(0.85f, 0.9f, 0.95f))
                        .headerFontColor(PdfColor.BLACK)
                        .columnWidths(listOf(50f, 100f, 220f, 70f, 70f))
                        .borderColor(PdfColor.BLACK)
                        .headerAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
                        .cellAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
                }
            }

            // 5. Summary Section
            val summaryXLabel = 350f
            val summaryXValue = 500f

            engine.newLine()
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("หมายเหตุ", x = 40f)

            // Summary rows
            val summaryRows = listOf(
                "จำนวนเงินรวม" to "45.00",
                "ส่วนลด" to "0.00",
                "จำนวนเงินสุทธิ" to "45.00",
                "ภาษีมูลค่าเพิ่ม 7%" to "2.94",
                "มูลค่าสินค้าหรือบริการ" to "42.06"
            )

            summaryRows.forEach { (label, value) ->
                engine.drawText(label, x = summaryXLabel)
                engine.drawText(value, x = summaryXValue)
                engine.newLine(1.2f)
            }

            engine.newLine(1f)
            engine.setFont("Sarabun", FontStyle.BOLD)
            engine.drawText("จำนวนเงินสุทธิ (สี่สิบห้าบาทถ้วน)", x = 40f)
            engine.newLine(1.2f)
            engine.drawText("(ตัวอักษร)", x = 40f)

            engine.newLine(2f)
            engine.setFont("Sarabun", FontStyle.REGULAR)
            engine.drawText("หากเอกสารฉบับนี้ไม่ถูกต้อง กรุณานำมาแก้ไขภายใน 7 วัน", x = 40f)

            // 6. Save
            engine.save(outputFile)
            println("PDF created successfully: $outputFile")
        }
    }
}
