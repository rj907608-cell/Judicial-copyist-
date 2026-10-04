package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.data.model.DocumentCategory
import com.example.data.model.SampleDocument
import com.example.data.model.SampleVisualType

object SampleDataHelper {

    val SAMPLES = listOf(
        SampleDocument(
            id = "sample_court_session",
            title = "محضر جلسة قضائية: دعوى مستحقات مقاولة",
            subtitle = "محضر جلسة بخط يد فضيلة القاضي متضمن الحضور والأقوال والقرار",
            category = DocumentCategory.COURT_SESSIONS,
            description = "محضر جلسة رسمي يحتوي على ديباجة، أقوال وكيل المدعي، جواب المدعى عليه، والقرار القضائي الصادر.",
            visualType = SampleVisualType.OFFICIAL_LETTER,
            formattedResult = """
# محضر جلسة قضائية

## الدائرة الحقوقية الأولى بالمحكمة العامة
**رقم القضية:** 461028392/1446هـ | **تاريخ الجلسة:** 1446/08/14هـ | **الموضوع:** مطالبة مالية بعقد مقاولة

## أولاً: افتتاح الجلسة وإثبات الحضور
افتُتحت الجلسة في تمام الساعة التاسعة والنصف صباحاً عبر الاتصال المرئي برئاسة فضيلة ناظر القضية، وحضر المدعي أصالة ومعه وكيله الشرعي، كما حضر وكيل المدعى عليها بموجب الوكالة الإلكترونية المعتمدة.

## ثانياً: تحرير الدعوى وأقوال المدعي
وبسؤال وكيل المدعي عن دعواه قرر قائلاً: "أحيل إلى لائحة الدعوى، ومجملها أن موكلي تعاقد مع المدعى عليها بموجب عقد المقاولة المؤرخ في 1445/03/10هـ لتنفيذ أعمال التشطيبات بمبلغ إجمالي قدره (380,000) ريال، وقد سلّم موكلي الأعمال كاملة بموجب محضر استلام موقع، وتبقى في ذمة المدعى عليها مبلغ (95,000) ريال يمتنعون عن سداده، وأطلب إلزامهم بدفع المبلغ المتبقي مع أتعاب المحاماة".

## ثالثاً: إجابة المدعى عليه ودفوعه
وبمواجهة وكيل المدعى عليها بدعوى المدعي ومحضر الاستلام أجاب قائلاً: "صادق على صحة العقد المبرم ومبلغ المطالبة، إلا أن المدعي تأخر في تسليم المشروع مدة شهرين مما رتب غرامة تأخير تعاقدية قدرها [كلمة غير واضحة] ريال، ولذا نطلب مهلة لتقديم كشف الحساب وتطبيق غرامة التأخير".

## رابعاً: جدول الدفعات والمسيرات المالية
| الدفعة | التاريخ | المبلغ المستحق (ريال) | حالة السداد |
|---|---|---|---|
| الدفعة الأولى (مقدمة) | 1445/03/15هـ | 100,000 | تم السداد |
| الدفعة الثانية (إنجاز 50%) | 1445/06/01هـ | 100,000 | تم السداد |
| الدفعة الثالثة (إنجاز 80%) | 1445/09/20هـ | 85,000 | تم السداد |
| الدفعة الختامية (تسليم نهائي) | 1446/01/10هـ | 95,000 | معلق في ذمة المدعى عليها |

## خامساً: قرار الدائرة وتوجيهاتها
نظراً لصلاحية القضية واستمهال وكيل المدعى عليها لتقديم مذكرة تفصيلية بشأن غرامات التأخير، **قررت الدائرة ما يلي:**
1. إمهال وكيل المدعى عليها مدة عشرة أيام من تاريخه لتقديم مذكرته ومستنداته عبر النظام الإلكتروني.
2. تزويد وكيل المدعي بنسخة للرد خلال سبعة أيام تالية لتقديم المذكرة.
3. تأجيل نظر الدعوى إلى جلسة يوم الثلاثاء الموافق 1446/09/08هـ الساعة العاشرة صباحاً.

ورفعت الجلسة في تمام الساعة العاشرة والربع صباحاً وأثبت ما تقدم.
            """.trimIndent()
        ),
        SampleDocument(
            id = "sample_judicial_decision",
            title = "قرار قضائي مسبب: إثبات صحة ونفاذ العقد",
            subtitle = "مسودة قرار قضائي مكتوب يدوياً مع التسبيب والأسانيد والمنطوق",
            category = DocumentCategory.COURT_DECISIONS,
            description = "قرار قضائي متكامل يحتوي على حيثيات الحكم، التسبيب الشرعي والنظامي، ومنطوق الحكم النهائي.",
            visualType = SampleVisualType.LECTURE_NOTES,
            formattedResult = """
# قرار قضائي مسبب

## الصادر عن المحكمة التجارية - الدائرة الأولى
**القضية المقيدة برقم:** 46291044 / 1446هـ

## أولاً: الوقائع وحاصل الدعوى
تتحصل وقائع هذه الدعوى بالقدر اللازم لإصدار هذا القرار في أن المدعي تقدم بصحيفة دعوى يطلب فيها إلزام المدعى عليه بالوفاء بالالتزامات الواردة في العقد التجاري المؤرخ في 1445/05/20هـ، وحيث عقدت الدائرة جلساتها لنظر النزاع واستمعت لدعوى المدعي ومصادقة المدعى عليه على أصل التعامل، وقفل باب المرافعة تمهيداً للفصل في النزاع.

## ثانياً: الأسباب والتسبيب الشرعي والنظامي
- **من حيث الاختصاص:** حيث إن النزاع ناشئ عن عقد تجاري بين تاجرين لأعمالهم التجارية، فإن الدائرة مختصة ولائياً ونوعياً بنظر الدعوى استناداً لنظام المحاكم التجارية.
- **من حيث الموضوع:** لما كان الأصل في العقود والشروط اللزوم والصحة استناداً لقوله تعالى: ﴿يَا أَيُّهَا الَّذِينَ آمَنُوا أَوْفُوا بِالْعُقُودِ﴾، وحيث إن المدعى عليه أقر بصحة توقيعه وتسلّمه للبضاعة الموصوفة، ولما تقرر فقهاً وقضاءً أن الإقرار حجة قاصرة على المقر ملزمة له، وحيث لم يقدم المدعى عليه أي دفع صحيح يبرر الامتناع عن أداء المقابل المتفق عليه؛ فإن حقه ثابت شرعاً ونظاماً.

## ثالثاً: منطوق القرار / الحكم
**لذلك كله؛ حكمت الدائرة بما يلي:**
- **أولاً:** إلزام المدعى عليه بأن يدفع للمدعي مبلغاً قدره (215,000) مائتان وخمسة عشر ألف ريال سعودي.
- **ثانياً:** إلزام المدعى عليه بدفع أتعاب التقاضي وقدرها (15,000) خمسة عشر ألف ريال سعودي.
- **ثالثاً:** إفهام الأطراف بأن هذا القرار قابل للاعتراض بطريق الاستئناف خلال ثلاثين يوماً تبدأ من اليوم التالي لتاريخ استلام الصك.
            """.trimIndent()
        ),
        SampleDocument(
            id = "sample_witness_testimony",
            title = "محضر ضبط أقوال وسماع شهادة",
            subtitle = "ضبط شهادة شاهد بخط يد كاتب الضبط في مجلس القضاء",
            category = DocumentCategory.STATEMENTS,
            description = "محضر ضبط أقوال يوثق بيانات الشاهد، حلف اليمين الشرعية، الأسئلة الموجهة إليه، والمصادقة.",
            visualType = SampleVisualType.OFFICIAL_LETTER,
            formattedResult = """
# محضر ضبط شهادة وسماع أقوال

## لدى المحكمة العامة - الدائرة الحقوقية
**رقم المعاملة:** 46081734 | **تاريخ الضبط:** 1446/08/12هـ

## أولاً: بيانات الشاهد وحلف اليمين
مَثَلَ أمام فضيلة ناظر القضية الشاهد المكلف بالإدلاء بشهادته:
- **اسم الشاهد:** عبد العزيز بن صالح بن عبد الله
- **رقم الهوية الوطنية:** [كلمة غير واضحة]1084920
- **المهنة والصلة بالخصوم:** مهندس إشراف، لا قرابة له بأي من أطراف الدعوى.
وبعد تذكيره بعظم أداء الشهادة ووجوب الصدق، حلف بالله العظيم قائلاً: *"أقسم بالله العظيم أن أقول الحق ولا شيء غير الحق"*.

## ثانياً: الأسئلة والأقوال المضبوطة
- **س/ فضيلة القاضي:** ما هي معلوماتك بشأن تسليم الموقع والأعمال المنجزة من المدعي للمدعى عليه؟
- **ج/ الشاهد:** أشهد بالله العظيم أنني كنت المشرف الهندسي المفوض بالموقع، وقد حضر المدعي وسلم مفاتيح المشروع والأعمال منتهية بالكامل بحضوري وحضور مدير فرع المدعى عليه بتاريخ 1445/11/02هـ دون أي تحفظات جوهرية حينها.
- **س/ فضيلة القاضي:** هل لاحظت أي عيوب أو إخلال فني بالمواصفات المعتمدة؟
- **ج/ الشاهد:** الأعمال كانت مطابقة لجداول الكميات والمخططات المعتمدة، ولم يسجل الاستشاري أي ملاحظات سالبة.

## ثالثاً: المصادقة والإقفال
تليت على الشاهد شهادته فأصر عليها وصادق على صحة ما دُوّن، وأُقفل المحضر في حينه.
            """.trimIndent()
        )
    )

    fun generateSampleBitmap(sample: SampleDocument): Bitmap {
        val width = 800
        val height = 1100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Court Official Paper background
        val bgPaint = Paint().apply {
            color = Color.rgb(252, 250, 245)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Ruled session guide lines
        val linePaint = Paint().apply {
            color = Color.argb(35, 120, 100, 80)
            strokeWidth = 1.2f
        }
        val startY = 150f
        val lineSpacing = 36f
        var currentY = startY
        while (currentY < height - 80) {
            canvas.drawLine(50f, currentY, width - 50f, currentY, linePaint)
            currentY += lineSpacing
        }

        // Court registry archive punch margin on right side
        val marginPaint = Paint().apply {
            color = Color.argb(55, 10, 77, 64)
            strokeWidth = 2f
        }
        canvas.drawLine(width - 100f, 60f, width - 100f, height - 60f, marginPaint)

        // Header Title
        val titlePaint = Paint().apply {
            color = Color.rgb(10, 77, 64)
            isAntiAlias = true
            textSize = 26f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(sample.title, width / 2f, 85f, titlePaint)

        // Handwritten strokes
        val inkPaint = Paint().apply {
            color = Color.rgb(20, 32, 45) // Judicial blue/black ink
            isAntiAlias = true
            strokeWidth = 3f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val path = Path()
        val randomSeeds = listOf(160f, 196f, 232f, 268f, 304f, 340f, 376f, 412f, 448f, 484f, 520f, 556f, 592f, 628f, 664f, 700f, 736f, 772f, 808f, 844f, 880f)
        for ((idx, y) in randomSeeds.withIndex()) {
            val lineRight = width - 120f
            val lineLeft = 80f + (idx % 3) * 35f

            path.reset()
            path.moveTo(lineRight, y - 2f)

            var cx = lineRight
            while (cx > lineLeft) {
                val step = 45f + (cx % 35f)
                val nextX = (cx - step).coerceAtLeast(lineLeft)
                val curveY = y + if ((cx.toInt() / 20) % 2 == 0) -6f else 4f
                path.quadTo((cx + nextX) / 2f, curveY, nextX, y - 2f)
                cx = nextX
            }
            canvas.drawPath(path, inkPaint)
        }

        // Court stamp simulation
        val stampPaint = Paint().apply {
            color = Color.argb(130, 10, 77, 64)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawOval(RectF(70f, 70f, 170f, 130f), stampPaint)
        val stampTextPaint = Paint().apply {
            color = Color.argb(170, 10, 77, 64)
            textSize = 13f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("وزارة العدل", 120f, 105f, stampTextPaint)

        return bitmap
    }
}
