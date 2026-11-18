package com.example.data.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class WeeklyScheduleDay(
    val dayOfWeekIndex: Int, // java.util.Calendar.SATURDAY .. FRIDAY
    val dayNameEn: String,
    val dayNameFa: String,
    val themeEn: String,
    val themeFa: String,
    val recommendedModule: String
)

object StarterLessonsAndScenariosSeed {
    private val json = Json { ignoreUnknownKeys = true }

    // Weekly Learning Schedule per Confirmed Section G
    val weeklySchedule = listOf(
        WeeklyScheduleDay(7, "Saturday", "شنبه", "Everyday English & Social Communication", "انگلیسی روزمره و ارتباطات اجتماعی", "SPEAKING_EVERYDAY"),
        WeeklyScheduleDay(1, "Sunday", "یک‌شنبه", "Fluency, Spontaneous Responses & Confidence", "روانی گفتار، پاسخ‌های بداهه و اعتمادبه‌نفس", "SPEAKING_FLUENCY"),
        WeeklyScheduleDay(2, "Monday", "دوشنبه", "Professional Workplace English", "انگلیسی حرفه‌ای محیط کار", "ENGINEERING_PM"),
        WeeklyScheduleDay(3, "Tuesday", "سه‌شنبه", "Construction Site Conversations & Supervision", "مکالمات کارگاه ساختمانی و نظارت مهندسی", "ENGINEERING_SITE"),
        WeeklyScheduleDay(4, "Wednesday", "چهارشنبه", "Technical Problems, QC & Contractor Coordination", "مسائل فنی، کنترل کیفیت و هماهنگی پیمانکار", "ROLEPLAY_QC"),
        WeeklyScheduleDay(5, "Thursday", "پنج‌شنبه", "Job Interviews, Management Meetings & Communication", "مصاحبه شغلی، جلسات مدیریتی و ارتباطات حرفه‌ای", "ROLEPLAY_INTERVIEW"),
        WeeklyScheduleDay(6, "Friday", "جمعه", "Rest Day or Optional Light Revision", "روز استراحت یا مرور سبک اختیاری (واژگان و شنیداری همیشه فعال)", "VOCAB_REVIEW")
    )

    // 10 Selectable Offline Content Packages per Section J
    fun getSelectablePackages(): List<ContentPackageEntity> = listOf(
        ContentPackageEntity(
            id = "pkg_core_vocab",
            title = "Core English Vocabulary (B2–C1)",
            titleFa = "واژگان هسته انگلیسی (B2 به C1)",
            category = "VOCABULARY",
            description = "High-frequency spoken & professional vocabulary, phrasal verbs, and collocations with Persian translations.",
            descriptionFa = "واژگان پرکاربرد محاوره‌ای و حرفه‌ای، افعال عبارتی و هم‌آیی‌ها با ترجمه فارسی.",
            sizeBytes = 4_800_000L,
            version = "1.2.0",
            licenseName = "CC0 1.0 / CC BY 2.0 FR",
            attribution = "Tatoeba Project (tatoeba.org) & English Pro Curated Core",
            downloadUrl = "https://tatoeba.org/en/downloads",
            status = "INSTALLED",
            downloadedBytes = 4_800_000L,
            progressPercent = 100,
            installedItemCount = 60
        ),
        ContentPackageEntity(
            id = "pkg_everyday_conv",
            title = "Everyday English Conversations",
            titleFa = "مکالمات روزمره انگلیسی",
            category = "SPEAKING",
            description = "Natural daily dialogues for international relocation, travel, housing, banking, and social confidence.",
            descriptionFa = "مکالمات طبیعی روزمره برای زندگی بین‌المللی، سفر، مسکن و ارتباطات اجتماعی.",
            sizeBytes = 6_200_000L,
            version = "1.1.0",
            licenseName = "CC BY 4.0 / CC0",
            attribution = "British Council Reference Patterns & Tatoeba CC0 Sentences",
            downloadUrl = "https://tatoeba.org/en/downloads",
            status = "NOT_INSTALLED"
        ),
        ContentPackageEntity(
            id = "pkg_general_listening",
            title = "General Listening & Shadowing Pack",
            titleFa = "بسته شنیداری و سایه‌زنی عمومی",
            category = "LISTENING",
            description = "Mixed-accent listening scripts, comprehension quizzes, and sentence-by-sentence shadowing drills.",
            descriptionFa = "متون شنیداری با لهجه‌های متنوع، آزمون درک مطلب و تمرین سایه‌زنی جمله‌به‌جمله.",
            sizeBytes = 14_500_000L,
            version = "1.0.0",
            licenseName = "CC0 1.0 (Common Voice Subset)",
            attribution = "Mozilla Common Voice CC0 Dataset Reference",
            downloadUrl = "https://commonvoice.mozilla.org/en/datasets",
            status = "NOT_INSTALLED"
        ),
        ContentPackageEntity(
            id = "pkg_us_audio",
            title = "American English Pronunciation Pack",
            titleFa = "بسته تلفظ انگلیسی آمریکایی",
            category = "AUDIO",
            description = "US General American stress, flap-T, and connected speech training materials.",
            descriptionFa = "آموزش استرس واژگان، تلفظ آمریکایی و گفتار پیوسته.",
            sizeBytes = 18_400_000L,
            version = "1.0.0",
            licenseName = "CC0 1.0",
            attribution = "Mozilla Common Voice US / Open Speech Corpus",
            downloadUrl = "https://commonvoice.mozilla.org/en/datasets",
            status = "NOT_INSTALLED"
        ),
        ContentPackageEntity(
            id = "pkg_uk_audio",
            title = "British English Pronunciation Pack",
            titleFa = "بسته تلفظ انگلیسی بریتانیایی",
            category = "AUDIO",
            description = "British RP/Modern RP listening drills, non-rhotic endings, and UK engineering terms.",
            descriptionFa = "تمرین‌های شنیداری بریتانیایی و اصطلاحات مهندسی رایج در بریتانیا و استانداردهای BS.",
            sizeBytes = 17_900_000L,
            version = "1.0.0",
            licenseName = "CC0 1.0",
            attribution = "Mozilla Common Voice UK / Open Speech Corpus",
            downloadUrl = "https://commonvoice.mozilla.org/en/datasets",
            status = "NOT_INSTALLED"
        ),
        ContentPackageEntity(
            id = "pkg_civil_vocab",
            title = "Civil Engineering Vocabulary",
            titleFa = "واژگان تخصصی مهندسی عمران و اجرا",
            category = "ENGINEERING",
            description = "Site execution, concrete, structural detailing, and geotechnical engineering vocabulary.",
            descriptionFa = "واژگان اجرای کارگاه، بتن و سازه، ژئوتکنیک و مهندسی پی.",
            sizeBytes = 7_500_000L,
            version = "1.3.0",
            licenseName = "Public Domain / CC0",
            attribution = "USACE Official Engineering Manuals (publications.usace.army.mil) & English Pro",
            downloadUrl = "https://www.publications.usace.army.mil/",
            status = "INSTALLED",
            downloadedBytes = 7_500_000L,
            progressPercent = 100,
            installedItemCount = 24
        ),
        ContentPackageEntity(
            id = "pkg_site_dialogues",
            title = "Construction Site Dialogues",
            titleFa = "مکالمات واقعی کارگاه ساختمانی",
            category = "ENGINEERING",
            description = "Resident Engineer inspections, contractor briefings, rebar checks, and site clash resolutions.",
            descriptionFa = "بازرسی مهندس مقیم، جلسات با پیمانکار، کنترل آرماتور و حل تداخل‌های کارگاهی.",
            sizeBytes = 9_100_000L,
            version = "1.2.0",
            licenseName = "CC0 1.0",
            attribution = "English Pro Civil Supervision Corpus",
            downloadUrl = "https://www.publications.usace.army.mil/",
            status = "NOT_INSTALLED"
        ),
        ContentPackageEntity(
            id = "pkg_pm_english",
            title = "Project Management & FIDIC English",
            titleFa = "انگلیسی مدیریت پروژه و قراردادهای FIDIC",
            category = "ENGINEERING",
            description = "Critical path delays, variation orders, NCRs, RFIs, claims, and monthly progress meetings.",
            descriptionFa = "تاخیرات مسیر بحرانی، دستور تغییر کار، NCR، RFI، لایحه تاخیر و جلسات پیشرفت.",
            sizeBytes = 8_300_000L,
            version = "1.1.0",
            licenseName = "CC0 1.0",
            attribution = "English Pro Contract Administration Reference",
            downloadUrl = "https://www.publications.usace.army.mil/",
            status = "NOT_INSTALLED"
        ),
        ContentPackageEntity(
            id = "pkg_wastewater",
            title = "Wastewater Treatment Plant English",
            titleFa = "انگلیسی تخصصی تصفیه‌خانه‌های آب و فاضلاب",
            category = "ENGINEERING",
            description = "Clarifiers, aeration basins, activated sludge, M&E coordination, and wet commissioning.",
            descriptionFa = "کلاریفایرها، حوضچه‌های هوادهی، لجن فعال، هماهنگی تاسیسات مکانیکی/برقی و راه‌اندازی.",
            sizeBytes = 6_900_000L,
            version = "1.1.0",
            licenseName = "Public Domain",
            attribution = "US EPA & USACE Hydraulic/Wastewater Public Publications",
            downloadUrl = "https://www.publications.usace.army.mil/",
            status = "NOT_INSTALLED"
        ),
        ContentPackageEntity(
            id = "pkg_safety",
            title = "Construction Safety (HSE) English",
            titleFa = "انگلیسی ایمنی کارگاه ساختمانی (HSE)",
            category = "ENGINEERING",
            description = "Excavation safety, confined space permits, working at height, crane lifting plans, and toolbox talks.",
            descriptionFa = "ایمنی گودبرداری، مجوز فضای بسته، کار در ارتفاع، طرح باربرداری و جلسات تول‌باکس.",
            sizeBytes = 5_600_000L,
            version = "1.0.0",
            licenseName = "Public Domain (U.S. Gov Work)",
            attribution = "OSHA Construction Standards (osha.gov/construction)",
            downloadUrl = "https://www.osha.gov/construction",
            status = "NOT_INSTALLED"
        )
    )

    // 10 Comprehensive Lessons covering Civil D1-D7 + Listening & Everyday/Workplace
    fun getStarterLessons(): List<LessonModuleEntity> = listOf(
        LessonModuleEntity(
            id = "les_d1_site",
            packageId = "pkg_civil_vocab",
            category = "D1_SITE",
            title = "Deep Excavation, Dewatering & Shoring Control",
            titleFa = "کنترل گودبرداری عمیق، آب‌کشی و مهاربندی",
            accent = "US",
            difficultyCefr = "B2",
            durationMinutes = 15,
            vocabularyIdsJson = json.encodeToString(listOf("d1_001", "d1_002", "d1_003", "d1_006")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("Hold the bulk excavation at minus six meters until the piezometer readings stabilize.", "گودبرداری عمومی را در تراز منهای شش متر متوقف کنید تا قرائت پیزومترها پایدار شود."),
                VocabExample("The contractor can't backfill behind the retaining wall until the waterproofing is signed off.", "پیمانکار تا زمانی که عایق رطوبتی تایید نشود نمی‌تواند پشت دیوار حائل خاک‌ریزی کند.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("Resident Engineer (You)", "US", "Good morning, Tariq. I noticed groundwater seepage along the east shoring wall. What's the discharge rate on the well-point pumps?", "صبح بخیر طارق. نشت آب زیرزمینی در دیواره مهاربندی شرقی دیدم. دبی خروجی پمپ‌های ول‌پوینت چقدر است؟"),
                LessonDialogueTurn("Site Superintendent", "UK", "Morning. One standby submersible pump tripped overnight, so the water table rose roughly 300 millimetres. Our electrician is resetting the panel now.", "صبح بخیر. یکی از پمپ‌های مستغرق رزرو دیشب قطع شد و سطح آب حدود ۳۰۰ میلی‌متر بالا آمد. برق‌کار ما الان در حال ریست تابلو است."),
                LessonDialogueTurn("Resident Engineer (You)", "US", "Understood, but we cannot pour the blinding concrete in wet conditions. Let's inspect the sump and verify the shoring struts before resuming.", "متوجه شدم، اما در شرایط خیس نمی‌توانیم بتن مگر بریزیم. بیایید حوضچه پمپاژ و استرات‌های مهاربندی را قبل از ادامه کار بازرسی کنیم.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "Why did the groundwater level rise 300 mm overnight?",
                    listOf("Heavy rainfall flooded the site", "A standby submersible pump tripped", "The blinding concrete cracked"),
                    1,
                    "The superintendent states one standby submersible pump tripped overnight."
                ),
                ComprehensionQuestion(
                    "What condition does the Resident Engineer require before pouring blinding concrete?",
                    listOf("Wait 7 days", "Drain the water and inspect sump & shoring struts", "Replace the structural designer"),
                    1,
                    "Blinding concrete cannot be poured in wet conditions; sump and struts must be checked first."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("tripped overnight", "An electrical breaker switched off automatically during the night", "قطع شدن کلید ایمنی برق در طول شب", "Standard site M&E phrasing"),
                ExpressionNote("signed off", "Formally approved with a signature", "امضا و تایید رسمی شدن", "Workplace & QA/QC")
            )),
            practicalSpeakingTask = "Record a 45-second voice instruction to a site foreman explaining why excavation must pause until dewatering lowers the water table below blinding level.",
            reviewQuestionsJson = json.encodeToString(listOf(
                "Explain the difference between 'blinding' (UK) and 'mud slab' (US).",
                "When is engineered backfill allowed behind a basement wall?"
            )),
            britishVsAmericanNotes = "UK: 'blinding concrete', 'layers' | US: 'mud slab', 'lifts' (compacted layers)."
        ),
        LessonModuleEntity(
            id = "les_d2_concrete",
            packageId = "pkg_civil_vocab",
            category = "D2_CONCRETE",
            title = "Pre-Pour Rebar Inspection & Concrete Quality Control",
            titleFa = "بازرسی آرماتور پیش از بتن‌ریزی و کنترل کیفیت بتن",
            accent = "INT",
            difficultyCefr = "B2+",
            durationMinutes = 15,
            vocabularyIdsJson = json.encodeToString(listOf("d2_001", "d2_002", "d2_003", "d2_004", "d2_005", "d2_007")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("Please clear all tie-wire clippings and sawdust from the beam soffit before closing the formwork.", "لطفاً قبل از بستن قالب، تمام خرده سیم‌آرماتور و خاک‌اره را از زیر تیر تمیز کنید."),
                VocabExample("If the slump exceeds 170 mm, reject the transit mixer on the spot.", "اگر اسلامپ از ۱۷۰ میلی‌متر بیشتر بود، تراک میکسر را همان‌جا برگشت بزنید.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("QA/QC Engineer", "UK", "We're checking Grid B-4 columns before this afternoon's pour. The lap splice length looks short on the corner bars.", "در حال کنترل ستون‌های محور B-4 قبل از بتن‌ریزی عصر هستیم. طول اورلب میلگردهای گوشه کوتاه به نظر می‌رسد."),
                LessonDialogueTurn("Contractor Foreman", "US", "We followed the bar-bending schedule, but let me grab the structural sheet. Also, do we have enough 50 mm spacers?", "طبق لیست‌وفر آرماتور بستیم، الان نقشه سازه را می‌آورم. ضمناً اسپیسر ۵۰ میلی‌متری به‌اندازه کافی داریم؟"),
                LessonDialogueTurn("QA/QC Engineer", "UK", "Not on the west face—add chairs every 800 mm to guarantee concrete cover, and make sure a standby vibrator is ready so we avoid honeycombing.", "در جبهه غربی کافی نیست؛ هر ۸۰۰ میلی‌متر اسپیسر بگذارید تا کاور بتن تضمین شود و ویبراتور رزرو آماده باشد تا کرموشدگی رخ ندهد.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "Why is a standby vibrator required?",
                    listOf("To speed up rebar tying", "To prevent honeycombing if the main vibrator fails", "To measure slump"),
                    1,
                    "Inadequate vibration during a pour causes honeycombing voids."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("on the spot", "Immediately at the scene without delay", "درجا و همان لحظه", "Natural spoken idiom"),
                ExpressionNote("bar-bending schedule (BBS)", "Tabulated list of rebar shapes, lengths, and diameters", "لیست‌وفر میلگرد", "Standard UK/International engineering")
            )),
            practicalSpeakingTask = "Describe 4 mandatory items you verify during a pre-pour rebar and formwork inspection in a 1-minute spoken briefing.",
            reviewQuestionsJson = json.encodeToString(listOf(
                "How does a planned construction joint differ from a cold joint?",
                "Why are concrete cylinders tested at slightly lower MPa than 150 mm cubes?"
            )),
            britishVsAmericanNotes = "UK: 'cover blocks / spacers', 'cube test' | US: 'rebar chairs / dobies', 'cylinder test'."
        ),
        LessonModuleEntity(
            id = "les_d3_geotech",
            packageId = "pkg_civil_vocab",
            category = "D3_GEOTECH",
            title = "Geotechnical Investigation, Micropiles & Settlement",
            titleFa = "مطالعات ژئوتکنیک، میکروپایل و کنترل نشست",
            accent = "US",
            difficultyCefr = "C1",
            durationMinutes = 15,
            vocabularyIdsJson = json.encodeToString(listOf("d3_001", "d3_002", "d3_003", "d3_004", "d3_005")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("Given the soft clay lens at 8 meters, micropiles are more reliable than shallow spread footings.", "با توجه به عدسی رس نرم در عمق ۸ متری، میکروپایل مطمئن‌تر از پی منفرد سطحی است.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("Geotech Specialist", "US", "Borehole BH-07 showed an SPT N-value of only 4 in the silty clay layer, well below our design assumption.", "گمانه BH-07 عدد SPT فقط ۴ را در لایه رس لای‌دار نشان داد که خیلی کمتر از فرض طراحی است."),
                LessonDialogueTurn("Resident Engineer (You)", "US", "That explains the risk of differential settlement beneath the heavy blower building. Should we switch to grouted micropiles or increase mat thickness?", "این ریسک نشست نامتقارن زیر ساختمان سنگین بلوئرها را توضیح می‌دهد. آیا به میکروپایل تزریقی تغییر دهیم یا ضخامت پی گسترده را زیاد کنیم؟")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "What triggered the concern about differential settlement?",
                    listOf("High concrete slump", "Low SPT N-value of 4 in silty clay", "Missing retaining wall weep holes"),
                    1,
                    "Low SPT N-value indicates weak compressible soil."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("well below assumption", "Significantly lower than the design value assumed", "به‌مراتب پایین‌تر از فرض طراحی", "Formal technical evaluation")
            )),
            practicalSpeakingTask = "Explain to a non-specialist Project Manager why unexpected soft clay in a borehole requires revising the foundation approach.",
            reviewQuestionsJson = json.encodeToString(listOf("What is the difference between uniform settlement and differential settlement?")),
            britishVsAmericanNotes = "UK: 'minipile', 'raft foundation', 'trial pit' | US: 'micropile', 'mat foundation', 'test pit'."
        ),
        LessonModuleEntity(
            id = "les_d4_wastewater",
            packageId = "pkg_civil_vocab",
            category = "D4_WASTEWATER",
            title = "Wastewater Treatment Plant: M&E Coordination & Commissioning",
            titleFa = "تصفیه‌خانه فاضلاب: هماهنگی سازه با تاسیسات و راه‌اندازی",
            accent = "INT",
            difficultyCefr = "C1",
            durationMinutes = 18,
            vocabularyIdsJson = json.encodeToString(listOf("d4_001", "d4_002", "d4_003", "d4_004", "d4_005")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("Confirm every embedded pipe sleeve and puddle flange before pouring the aeration tank walls.", "پیش از بتن‌ریزی دیوارهای حوضچه هوادهی، تمام غلاف‌ها و فلنج‌های آب‌بند مدفون را تایید کنید.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("Mechanical Lead", "UK", "Before you pour Wall Section W3 of Secondary Clarifier 1, we need to verify the elevation of the 400 mm scum-trough sleeve.", "قبل از بتن‌ریزی قطعه دیوار W3 کلاریفایر ثانویه ۱، باید تراز غلاف ۴۰۰ میلی‌متری کف‌گیر را کنترل کنیم."),
                LessonDialogueTurn("Civil Supervision Manager (You)", "US", "Good catch. Also, have the puddle flanges been pressure-checked and centered? Once concrete cures, core-drilling through heavily reinforced walls is costly and risks leaks.", "نکته خوبی بود. ضمناً آیا فلنج‌های آب‌بند هم‌محور و کنترل شده‌اند؟ بعد از سخت شدن بتن، کرگیری از دیوارهای مسلح پرهزینه و دارای ریسک نشتی است.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "Why must puddle flanges and sleeves be checked before pouring clarifier walls?",
                    listOf("To lower cement cost", "Core-drilling later is costly and risks leakage", "To speed up sludge drying"),
                    1,
                    "Water-retaining structures require leak-free embedded sleeves prior to pouring."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("Good catch!", "Thank you for noticing an important mistake or detail in time", "چه دقت خوبی / به‌موقع متوجه شدی", "Warm, natural workplace praise"),
                ExpressionNote("puddle flange", "Watertight steel collar welded around a pipe embedded in concrete", "فلنج آب‌بند مدفون در بتن", "Hydraulic & treatment plant essential")
            )),
            practicalSpeakingTask = "Conduct a 1-minute coordination briefing between your civil crew and the M&E team prior to wet commissioning a clarifier and aeration basin.",
            reviewQuestionsJson = json.encodeToString(listOf("Distinguish dry commissioning from wet commissioning in a treatment plant.")),
            britishVsAmericanNotes = "UK: 'inlet works', 'scour valve' | US: 'headworks', 'drain/blow-off valve'."
        ),
        LessonModuleEntity(
            id = "les_d5_fidic",
            packageId = "pkg_core_vocab",
            category = "D5_PM_FIDIC",
            title = "FIDIC Delay Analysis, Variation Orders & NCR Close-Out",
            titleFa = "تحلیل تاخیرات فیدیک، دستور تغییر کار و بستن NCR",
            accent = "UK",
            difficultyCefr = "C1",
            durationMinutes = 18,
            vocabularyIdsJson = json.encodeToString(listOf("d5_001", "d5_002", "d5_003", "d5_004", "d5_005", "d5_006")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("A Variation Order requires prior written instruction from the Engineer before the contractor executes additional scope.", "دستور تغییر کار پیش از اجرای کار اضافی توسط پیمانکار، نیازمند ابلاغ کتبی قبلی از سوی مهندس مشاور است.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("Contractor PM", "UK", "We're claiming a 14-day Extension of Time because the RFI response on the culvert headwall took two weeks.", "ما درخواست ۱۴ روز تمدید مدت داریم چون پاسخ RFI دیوار بالی کالورت دو هفته طول کشید."),
                LessonDialogueTurn("Engineer's Representative (You)", "US", "I've reviewed your baseline programme. That culvert had 18 days of total float, so it didn't sit on the critical path. We can discuss mitigation, not an EoT.", "برنامه مبنا را بررسی کردم. آن کالورت ۱۸ روز شناوری کل داشت، پس روی مسیر بحرانی نبود. می‌توانیم درباره جبران صحبت کنیم، نه تمدید مدت.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "Why was the 14-day Extension of Time claim rejected?",
                    listOf("The culvert activity had 18 days of float and wasn't on the critical path", "The RFI was never submitted", "The concrete passed 28-day strength"),
                    0,
                    "Under FIDIC delay analysis, non-critical delays within total float generally do not extend Time for Completion."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("total float", "Amount of time an activity can slip without delaying project completion", "شناوری کل فعالیت", "Project planning essential")
            )),
            practicalSpeakingTask = "Respond diplomatically but firmly to a contractor requesting a Variation Order for work that was already included in the tender specification.",
            reviewQuestionsJson = json.encodeToString(listOf("What is the difference between Variation Order (FIDIC) and Change Order (US)?")),
            britishVsAmericanNotes = "UK/FIDIC: 'Baseline Programme', 'Variation Order', 'Extension of Time', 'Delay Damages' | US: 'Baseline Schedule', 'Change Order', 'Time Extension', 'Liquidated Damages'."
        ),
        LessonModuleEntity(
            id = "les_d6_safety",
            packageId = "pkg_core_vocab",
            category = "D6_SAFETY",
            title = "Site HSE Walkdown: Confined Spaces & Lifting Plans",
            titleFa = "بازرسی ایمنی کارگاه: فضای بسته و طرح باربرداری",
            accent = "US",
            difficultyCefr = "B2",
            durationMinutes = 12,
            vocabularyIdsJson = json.encodeToString(listOf("d6_001", "d6_002", "d6_003", "d6_004")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("Stop the lift immediately—the crane outrigger isn't sitting on a timber or steel spreader pad.", "باربرداری را فوراً متوقف کنید—جک جرثقیل روی صفحه توزیع بار چوبی یا فلزی قرار نگرفته است.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("HSE Manager", "US", "Before the crew enters Manhole MH-12, show me the calibrated four-gas monitor log and the signed confined-space entry permit.", "پیش از ورود اکیپ به منهول MH-12، گزارش کالیبراسیون گازسنج چهارگانه و مجوز امضاشده فضای بسته را نشان دهید."),
                LessonDialogueTurn("Site Engineer (You)", "US", "Here is the permit. Oxygen is at 20.9%, H2S is zero, our standby attendant is stationed at the top with a tripod winch, and we just finished the toolbox talk.", "این مجوز است. اکسیژن ۲۰.۹ درصد، گاز سولفید هیدروژن صفر، نفر ناظر با وینچ سه‌پایه در دهانه مستقر است و همین الان تول‌باکس تاک را تمام کردیم.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "What safety controls are verified before manhole entry?",
                    listOf("Gas monitoring, entry permit, standby attendant & tripod winch", "Only a hard hat", "A concrete slump cone"),
                    0,
                    "Confined space entry requires atmospheric testing, a permit, a standby attendant, and rescue retrieval gear."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("standby attendant", "Trained person stationed outside a confined space to monitor entrants", "نفر مراقب/ناظر دهانه فضای بسته", "OSHA confined space term")
            )),
            practicalSpeakingTask = "Deliver a crisp 45-second English toolbox talk on working at height and excavation edge protection.",
            reviewQuestionsJson = json.encodeToString(listOf("Why must near misses be logged even if nobody was hurt?")),
            britishVsAmericanNotes = "UK: 'banksman', 'method statement' | US: 'signalperson', 'job hazard analysis (JHA)'."
        ),
        LessonModuleEntity(
            id = "les_d7_meetings",
            packageId = "pkg_core_vocab",
            category = "D7_MEETINGS",
            title = "Chairing Weekly Progress Meetings & Diplomatic Pushback",
            titleFa = "اداره جلسات هفتگی پیشرفت و مخالفت حرفه‌ای",
            accent = "INT",
            difficultyCefr = "C1",
            durationMinutes = 15,
            vocabularyIdsJson = json.encodeToString(listOf("gen_001", "gen_014", "d7_001", "d7_002", "d7_005", "d7_007")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("With respect, the daily pour logs don't support that figure—let's check the actual batch tickets.", "با احترام، گزارش‌های روزانه بتن‌ریزی آن عدد را تایید نمی‌کنند—بیایید برگه‌های بچینگ واقعی را بررسی کنیم.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("Project Director", "UK", "Let's move to Item 3: physical progress on the industrial warehouse slab. Are we on track for Friday?", "برویم سر بند ۳: پیشرفت فیزیکی دال سوله صنعتی. آیا برای جمعه طبق برنامه هستیم؟"),
                LessonDialogueTurn("Supervision Manager (You)", "US", "To be candid, we're two days behind due to rebar delivery, and I want to flag a clash with the fire-water pipe sleeves. However, if we add a second fixing crew tonight, we can iron it out and stay on track.", "صادقانه بگویم، به‌خاطر تحویل میلگرد دو روز عقب هستیم و می‌خواهم تداخل غلاف‌های لوله اطفای حریق را گوشزد کنم. اما اگر امشب یک اکیپ آرماتوربند دوم اضافه کنیم، مشکل را حل می‌کنیم و روی برنامه می‌مانیم.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "How does the speaker handle reporting a two-day delay?",
                    listOf("Hides the delay until next week", "States it candidly, flags a clash, and proposes a concrete recovery action", "Blames the client"),
                    1,
                    "Strong international leaders pair transparency about risks with actionable recovery solutions."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("To be candid", "Speaking honestly and directly without hiding facts", "صادقانه بگویم", "Executive meeting phrase"),
                ExpressionNote("With respect", "Polite way to disagree without sounding aggressive", "با احترام (برای مخالفت حرفه‌ای)", "Diplomatic disagreement")
            )),
            practicalSpeakingTask = "Practice chairing the first 60 seconds of a weekly progress meeting: welcome attendees, review last week's action items, and invite the contractor's update.",
            reviewQuestionsJson = json.encodeToString(listOf("Give two natural ways to challenge an inaccurate statement in a meeting without sounding rude.")),
            britishVsAmericanNotes = "UK: 'table an item' = bring it up for discussion | US: 'table an item' = postpone it."
        ),
        LessonModuleEntity(
            id = "les_listen_everyday",
            packageId = "pkg_core_vocab",
            category = "LISTENING_GENERAL",
            title = "Everyday Listening: Relocating & Settling into a New City",
            titleFa = "شنیداری روزمره: جابه‌جایی و زندگی در شهر جدید",
            accent = "UK",
            difficultyCefr = "B2",
            durationMinutes = 12,
            vocabularyIdsJson = json.encodeToString(listOf("gen_007", "gen_013", "gen_015")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("Once you get the hang of the metro transfer, the commute to the head office takes under twenty minutes.", "وقتی قلق تعویض خط مترو دستتان بیاید، مسیر رفت‌وآمد به دفتر مرکزی زیر بیست دقیقه است.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("Colleague (London)", "UK", "Hey, how are you settling in during your first week? Did you manage to sort out your local bank account and SIM card?", "سلام، هفته اول چطور جا افتادید؟ توانستید حساب بانکی محلی و سیم‌کارت را ردیف کنید؟"),
                LessonDialogueTurn("You", "US", "Mostly, yes. The paperwork was pretty straightforward once I brought my employment letter, though I'm still waiting on a tentative lease confirmation for an apartment near the station.", "تا حد زیادی بله. وقتی نامه اشتغال را بردم کارهای اداری کاملاً سرراست بود، هرچند هنوز منتظر تایید موقت قرارداد اجاره آپارتمان نزدیک ایستگاه هستم.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "What is the speaker still waiting for?",
                    listOf("A bank card", "A tentative lease confirmation for an apartment", "A flight ticket"),
                    1,
                    "They already handled bank paperwork and are waiting on apartment lease confirmation."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("settling in", "Becoming comfortable in a new home, city, or job", "جا افتادن در محل یا کار جدید", "Everyday conversational"),
                ExpressionNote("sort out", "Organize or solve something practically", "ردیف کردن، حل و فصل کردن", "High-frequency UK/INT spoken idiom")
            )),
            practicalSpeakingTask = "Shadow each sentence at 0.8x speed first, then introduce yourself to a new colleague in 45 seconds.",
            reviewQuestionsJson = json.encodeToString(listOf("Use 'straightforward' and 'tentative' in sentences about daily life.")),
            britishVsAmericanNotes = "UK: 'flat', 'sort out', 'queue' | US: 'apartment', 'take care of', 'line'."
        ),
        LessonModuleEntity(
            id = "les_listen_interview",
            packageId = "pkg_core_vocab",
            category = "LISTENING_WORKPLACE",
            title = "International Engineering Interview: Presenting 30 Years of Experience",
            titleFa = "مصاحبه بین‌المللی مهندسی: ارائه ۳۰ سال سابقه کاری",
            accent = "INT",
            difficultyCefr = "C1",
            durationMinutes = 15,
            vocabularyIdsJson = json.encodeToString(listOf("gen_004", "gen_008", "d5_001", "d7_006")),
            naturalSentencesJson = json.encodeToString(listOf(
                VocabExample("Over the past three decades, I've supervised complex civil infrastructure—from wastewater plants to high-rise residential towers.", "طی سه دهه گذشته بر پروژه‌های پیچیده زیرساختی عمرانی—از تصفیه‌خانه‌های فاضلاب تا برج‌های مسکونی—نظارت داشته‌ام.")
            )),
            dialogueTurnsJson = json.encodeToString(listOf(
                LessonDialogueTurn("Technical Director", "UK", "You have an impressive track record across water treatment and large residential complexes. Tell me about a time a contractor pushed back on a quality rejection—how did you handle it?", "کارنامه چشمگیری در تصفیه‌خانه‌ها و مجتمع‌های مسکونی بزرگ دارید. از زمانی بگویید که پیمانکار در برابر رد کیفیت مقاومت کرد—چطور مدیریتش کردید؟"),
                LessonDialogueTurn("Candidate (You)", "US", "On a 120,000 m³/day wastewater project, a clarifier wall showed severe honeycombing after stripping. The contractor wanted to patch it superficially, but I pushed back with core test data, issued an NCR, and had them submit an approved structural repair method statement—without compromising the 50-year design life.", "در پروژه تصفیه‌خانه ۱۲۰ هزار مترمکعبی، دیوار کلاریفایر پس از قالب‌برداری کرموشدگی شدید داشت. پیمانکار می‌خواست سطحی ترمیم کند، ولی با استناد به نتایج مغزه‌گیری مخالفت کردم، NCR صادر کردم و شرح روش ترمیم سازه‌ای تاییدشده گرفتم—بدون اینکه عمر طراحی ۵۰ ساله به خطر بیفتد.")
            )),
            comprehensionQuestionsJson = json.encodeToString(listOf(
                ComprehensionQuestion(
                    "How did the candidate substantiate rejecting superficial patching?",
                    listOf("By shouting at the foreman", "By using core test data and issuing an NCR", "By delaying payment indefinitely"),
                    1,
                    "Using objective test data + formal NCR + approved repair method statement demonstrates international engineering professionalism."
                )
            )),
            difficultExpressionsJson = json.encodeToString(listOf(
                ExpressionNote("track record", "Past achievements and performance history", "کارنامه و سوابق عملکرد", "Essential interview vocabulary")
            )),
            practicalSpeakingTask = "Answer in 60–90 seconds using the STAR method (Situation, Task, Action, Result): Describe a major technical challenge you resolved on site.",
            reviewQuestionsJson = json.encodeToString(listOf("Summarize 30 years of civil supervision experience in 3 crisp sentences without overusing 'and then'.")),
            britishVsAmericanNotes = "Maintain clear, measured pacing (~120–135 words/min) rather than rushing."
        )
    )

    // All 20 Realistic Role-Play Scenarios from Module E
    fun getRolePlayScenarios(): List<RolePlayScenarioEntity> = listOf(
        rp(1, "rp_01", "Weekly Progress Meeting", "جلسه هفتگی پیشرفت کار", "PM_MEETINGS", "International Project Manager", "B2",
            "Report true physical progress, explain a rebar delay, and commit to a realistic recovery plan.",
            "Week 18 progress meeting on a multi-building industrial plant. Concrete pours are 2 days behind.",
            "The PM suddenly demands you recover 5 days by cutting concrete curing time.",
            listOf(
                OfflineBranchNode(0, "Good morning. Looking at the dashboard, substructure pours are two days behind baseline. Walk me through what happened and your recovery plan.", listOf("Rebar delivery slipped by two days, so we shifted formwork crews to Zone B.", "We're adding a night shift for rebar fixing without cutting curing time."), listOf("rebar", "baseline", "curing", "shift"), "علت تاخیر را شفاف بگویید و راه‌حل جبرانی بدهید بدون اینکه کیفیت را فدا کنید."),
                OfflineBranchNode(1, "Two days is tight—can't we strip the column formwork after 18 hours instead of 36 to speed things up?", listOf("I have to push back on that; stripping early compromises compressive strength and risks edge spalling.", "Instead, let's bring in a second concrete pump on Thursday to recover the two days safely."), listOf("push back", "compressive", "compromise", "pump"), "حرفه‌ای مخالفت کنید (push back) و جایگزین ایمن پیشنهاد دهید.")
            )),
        rp(2, "rp_02", "Discussing Delays with Contractor", "بحث درباره تاخیرات با پیمانکار", "PM_MEETINGS", "Contractor Representative", "B2+",
            "Challenge an unsubstantiated delay claim and require contemporaneous records.",
            "The contractor blames utility relocation for a 3-week slip on the administration building.",
            "The contractor threatens to slow down work unless you sign an Extension of Time today.",
            listOf(
                OfflineBranchNode(0, "Engineer, the municipal water line delayed our excavation by three weeks. We need your signature on an Extension of Time right now.", listOf("Let's review the daily logs first—that zone isn't on the critical path according to last week's programme.", "Please submit contemporaneous records to substantiate the exact impact before we assess an EoT."), listOf("critical path", "substantiate", "logs", "programme"), "بگویید باید مستندات هم‌زمان و مسیر بحرانی بررسی شود."),
                OfflineBranchNode(1, "My head office is pressuring me. If we don't get approval today, we can't mobilize the second crane.", listOf("I understand the commercial pressure, but under FIDIC we cannot approve an EoT without documented critical-path impact.", "Submit the formal delay analysis by Wednesday and I'll expedite our review."), listOf("FIDIC", "documented", "review", "Wednesday"), "همدلی کنید ولی بر چارچوب قرارداد FIDIC پافشاری کنید.")
            )),
        rp(3, "rp_03", "Inspecting Reinforcement Before Pour", "بازرسی آرماتور قبل از بتن‌ریزی", "SITE_QC", "Site Engineer (Contractor)", "B2",
            "Conduct a thorough pre-pour rebar check and identify cover and splice issues.",
            "Afternoon slab pour scheduled at 3 PM; you find missing spacers and debris in the beam box.",
            "Ready-mix concrete trucks are already dispatched and waiting at the gate.",
            listOf(
                OfflineBranchNode(0, "Everything is ready for the 3 PM slab pour, and the first two concrete trucks just arrived at the gate. Can you sign the WIR?", listOf("Hold on—I see missing 40 mm spacers along Grid C and tie-wire clippings inside the beam formwork.", "We cannot sign off the WIR until you install chairs and blow out the debris."), listOf("spacers", "formwork", "debris", "WIR"), "بازرسی را سرسری نگیرید؛ نواقص کاور و نظافت قالب را بگویید."),
                OfflineBranchNode(1, "If we hold the trucks for 30 minutes, the batch might lose slump or be rejected!", listOf("Then put six helpers on Grid C right now to fix the spacers and clean out with compressed air.", "Meanwhile, check the slump and concrete temperature at the gate; quality comes before rushing a pour."), listOf("slump", "compressed air", "temperature", "quality"), "اقدام فوری و هماهنگ همزمان با کنترل اسلامپ را اعلام کنید.")
            )),
        rp(4, "rp_04", "Discussing Poor Workmanship", "رسیدگی به اجرای ضعیف و کیفیت پایین", "SITE_QC", "Construction Manager", "B2+",
            "Address repeated plastering and masonry alignment defects firmly and constructively.",
            "Second floor partition walls fail plumbness tolerance by 18 mm.",
            "The Construction Manager claims 'finishing plaster will hide it'.",
            listOf(
                OfflineBranchNode(0, "Look, the masonry crew is experienced. Once we apply 25 mm of render, nobody will notice the wall is slightly out of plumb.", listOf("An 18 mm deviation exceeds specification tolerance and creates thick, crack-prone plaster.", "Let's rectify the worst panels now rather than covering up poor workmanship."), listOf("tolerance", "deviation", "rectify", "specification"), "بگویید ماست‌مالی کردن با گچ باعث ترک و خروج از تلرانس می‌شود.")
            )),
        rp(5, "rp_05", "Rejecting Non-Compliant Work", "رد کردن کار غیراستاندارد (NCR)", "SITE_QC", "QA/QC Manager & Contractor", "C1",
            "Formally reject honeycombed concrete columns and require an approved repair procedure.",
            "Stripped basement columns show exposed rebar and voids near the base.",
            "Contractor asks to fill it with normal mortar immediately.",
            listOf(
                OfflineBranchNode(0, "It's just minor surface honeycombing at the kicker. Can our masons patch it with cement-sand mortar this afternoon?", listOf("No—rebar is exposed, which makes this a structural non-conformance, not cosmetic.", "I am issuing an NCR; stop patching and submit a non-shrink structural repair method statement plus ultrasonic testing."), listOf("NCR", "exposed", "method statement", "structural"), "اعلام صدور NCR و الزام ارائه Method Statement ترمیم سازه‌ای.")
            )),
        rp(6, "rp_06", "Reviewing Contractor Method Statement", "بررسی شرح روش اجرای پیمانکار", "SITE_QC", "Technical Director", "C1",
            "Review gaps in a dewatering and shoring method statement.",
            "Deep 9-meter pump station excavation next to an existing road.",
            "No standby generator or piezometer monitoring is included in the submission.",
            listOf(
                OfflineBranchNode(0, "We submitted our excavation and dewatering method statement yesterday. Do you see any blockers for approval?", listOf("Yes, two gaps: there is no standby power backup for the dewatering pumps and no inclinometer monitoring frequency.", "Revise and resubmit with contingency power and daily settlement readings."), listOf("standby", "monitoring", "contingency", "resubmit"), "نقص برق اضطراری و پایش جابه‌جایی دیواره را بیان کنید.")
            )),
        rp(7, "rp_07", "Discussing a Variation Order", "مذاکره درباره دستور تغییر کار (VO)", "PM_MEETINGS", "Client Representative", "C1",
            "Justify a necessary design variation due to unforeseen underground rock.",
            "Boreholes missed a basalt lens along the pipeline trench.",
            "The Client resists extra cost and asks why the consultant didn't predict it.",
            listOf(
                OfflineBranchNode(0, "Why am I paying extra for rock excavation? Wasn't soil investigation already completed?", listOf("Standard boreholes are spaced every 100 meters, and this localized rock lens fell between BH-02 and BH-03.", "We verified quantities jointly with the surveyor so the Variation Order covers only proven net volume."), listOf("boreholes", "Variation Order", "verified", "volume"), "منطق فنی فاصله گمانه‌ها و کنترل دقیق احجام را توضیح دهید.")
            )),
        rp(8, "rp_08", "Negotiating Revised Construction Schedule", "مذاکره برای برنامه زمان‌بندی اصلاحی", "PM_MEETINGS", "International Project Manager", "C1",
            "Negotiate fast-tracking and resource leveling without sacrificing safety.",
            "Switchgear shipment delayed 4 weeks due to port congestion.",
            "Client insists handover date cannot move.",
            listOf(
                OfflineBranchNode(0, "If the MCC switchgear arrives four weeks late, how can we possibly hold our commissioning handover date?", listOf("We can re-sequence dry testing of pumps using temporary power and complete civil snagging in parallel.", "Let's overlap pre-commissioning steps so only final energization waits for the switchgear."), listOf("re-sequence", "parallel", "pre-commissioning", "energization"), "هم‌پوشانی فعالیت‌های خشک و تغییر توالی را پیشنهاد دهید.")
            )),
        rp(9, "rp_09", "Explaining Technical Problem to PM", "توضیح یک مشکل فنی به مدیر پروژه", "ENGINEERING", "Project Manager", "B2",
            "Summarize a concrete batching temperature issue concisely for non-structural management.",
            "Ambient temperature hit 43°C and concrete arrived at 34°C (spec max 32°C).",
            "PM asks why pouring can't happen at noon.",
            listOf(
                OfflineBranchNode(0, "Give me the bottom line—why did you stop today's raft pour?", listOf("Concrete arrived at 34 degrees, above the 32-degree limit, which accelerates setting and risks thermal cracking.", "We are shifting the pour to 4 AM using chilled water and flaked ice at the batching plant."), listOf("thermal cracking", "chilled", "limit", "4 AM"), "خلاصه شفاف + راهکار دمایی (آب سرد/یخ و بتن‌ریزی سحر) بدهید.")
            )),
        rp(10, "rp_10", "Groundwater Problems During Excavation", "مدیریت مشکل آب زیرزمینی در گودبرداری", "SITE_QC", "Consultant's Resident Engineer", "B2+",
            "Coordinate emergency sump control and prevent soil piping/boiling.",
            "High groundwater inflow observed at toe of sheet-pile wall.",
            "Fine sand starts washing in with the water.",
            listOf(
                OfflineBranchNode(0, "Water is bubbling up with fine sand near the south sheet-pile toe. What's your immediate instruction?", listOf("Stop excavating that corner immediately—fine sand washing in indicates piping and risks wall instability.", "Place a graded gravel filter blanket over the boil and add relief wells before continuing."), listOf("piping", "filter", "instability", "stop"), "خطر شسته شدن ماسه (piping) را هشدار دهید و فیلتر شنی بگذارید.")
            )),
        rp(11, "rp_11", "Reviewing Micropile Installation", "بررسی اجرای میکروپایل‌ها", "SITE_QC", "Geotechnical Site Engineer", "C1",
            "Audit grout take, casing depth, and load test readiness for micropiles.",
            "Installing 45 micropiles for wastewater tank underpinning.",
            "Grout consumption suddenly doubled on Pile MP-19.",
            listOf(
                OfflineBranchNode(0, "Pile MP-19 took nearly double the theoretical grout volume. Should we keep pumping or cap it?", listOf("Check pressure first—a drop in pressure with high grout take suggests a subterranean void or cavity.", "Hold grouting temporarily, log the depth, and notify the geotechnical designer before grouting adjacent piles."), listOf("pressure", "void", "log", "designer"), "احتمال حفره زیرسطحی و لزوم ثبت فشار و هماهنگی با طراح را بگویید.")
            )),
        rp(12, "rp_12", "Discussing Concrete Test Results", "تحلیل نتایج آزمایشگاهی بتن", "SITE_QC", "QA/QC Manager", "B2",
            "Interpret borderline 7-day cube strengths and decide on 28-day & core testing.",
            "7-day strength is 62% of target C40 instead of usual 70%.",
            "Contractor wants to keep loading the slab anyway.",
            listOf(
                OfflineBranchNode(0, "The 7-day cubes hit only 62% of C40, maybe because of the fly-ash blend. Can we remove the slab props tomorrow?", listOf("Keep the props in place until 28-day breaks or extra companion cubes confirm required strength.", "If 28-day results fall short, we will schedule Schmidt hammer screening and calibrated core tests."), listOf("props", "28-day", "core", "strength"), "تاکید بر نگه‌داشتن جک‌ها تا تایید مقاومت ۲۸ روزه.")
            )),
        rp(13, "rp_13", "Coordinating Civil, M&E Works", "هماهنگی عملیات سیویل، مکانیک و برق", "WASTEWATER", "MEP Manager", "C1",
            "Resolve clashes between cable trays, HVAC ducts, and structural beams.",
            "Control building basement ceiling has 3 overlapping services.",
            "MEP demands core-drilling a primary reinforced beam.",
            listOf(
                OfflineBranchNode(0, "Our 600 mm chilled-water pipe hits the concrete beam on Axis 5. Can we core a 650 mm hole through the beam?", listOf("Absolutely not through the mid-span tension zone without structural redesign.", "Let's walk through the BIM clash sheet—we can offset the pipe beneath the beam or route along the corridor."), listOf("tension", "redesign", "offset", "clash"), "ممانعت از سوراخ‌کاری تیر اصلی بدون تایید طراح و ارائه مسیر جایگزین.")
            )),
        rp(14, "rp_14", "Conducting a Site Safety Inspection", "انجام بازرسی ایمنی کارگاه", "SAFETY", "HSE Manager", "B2",
            "Lead a joint safety walkdown and enforce immediate stop-work on fall hazards.",
            "Workers on 6-meter scaffolding without guardrails or harnesses.",
            "Foreman says 'it will only take ten minutes'.",
            listOf(
                OfflineBranchNode(0, "The scaffolders just need ten minutes to finish clamping the top ledger. Can they finish without mid-rails?", listOf("Ten minutes is enough for a fatal fall—issue an immediate stop-work until full guardrails and harnesses are in place.", "Please brief the crew in a five-minute toolbox talk before resuming."), listOf("stop-work", "guardrails", "harness", "toolbox talk"), "توقف فوری کار بدون پذیرش بهانه 'فقط ده دقیقه'.")
            )),
        rp(15, "rp_15", "Explaining a Wastewater Treatment Process", "توضیح فرآیند تصفیه‌خانه فاضلاب", "WASTEWATER", "Client Technical Auditor", "C1",
            "Clearly explain biological nutrient removal, aeration, and secondary clarification.",
            "Walking an international client auditor through a 80,000 m³/day plant.",
            "Auditor asks what happens during peak wet-weather flow.",
            listOf(
                OfflineBranchNode(0, "Walk me through how the biological stage handles sudden peak inflow during storm events without washing out the activated sludge.", listOf("Equalization basins buffer peak flows before the aeration tanks, and we adjust return activated sludge pumping rates to keep MLSS stable in the clarifiers."), listOf("equalization", "aeration", "sludge", "clarifiers"), "نقش حوضچه متعادل‌سازی و کنترل نرخ برگشت لجن را توضیح دهید.")
            )),
        rp(16, "rp_16", "Participating in a Job Interview", "مصاحبه شغلی بین‌المللی", "CAREER", "HR & Engineering Director", "C1",
            "Present leadership style, international standards familiarity, and adaptability.",
            "Interviewing for a Senior Construction Supervision Manager role abroad.",
            "Interviewer asks why you haven't worked in an English-only office recently.",
            listOf(
                OfflineBranchNode(0, "Your technical background is very strong, though you've worked mostly in Persian-speaking teams recently. How confident are you chairing daily English site meetings?", listOf("Technical leadership relies on clear structure and precision, and I've been actively refreshing my spoken fluency and FIDIC terminology daily.", "Even in past international vendor meetings, I comfortably led drawing reviews and quality negotiations in English."), listOf("precision", "FIDIC", "fluency", "negotiations"), "اعتمادبه‌نفس حرفه‌ای + تجربه واقعی کار با وندورها را بیان کنید.")
            )),
        rp(17, "rp_17", "Introducing Experience to Employer", "معرفی ۳۰ سال سابقه به کارفرمای بین‌المللی", "CAREER", "Technical Director", "B2+",
            "Deliver a concise, high-impact 60-second executive summary of 30 years in civil engineering.",
            "First round screening call with an international EPC/PMC firm.",
            "Director interrupts: 'Give me your top 3 projects in 30 seconds.'",
            listOf(
                OfflineBranchNode(0, "Tell me about your background in civil engineering and what role suits you best here.", listOf("I am a civil engineer with 30 years of hands-on experience in construction supervision, infrastructure, water and wastewater plants, and major industrial complexes—most recently acting as Resident / Supervision Manager."), listOf("30 years", "supervision", "wastewater", "infrastructure"), "معرفی دقیق، روان و بدون پرگویی ارائه دهید.")
            )),
        rp(18, "rp_18", "Discussing Salary & Employment Terms", "مذاکره حقوق، مسئولیت‌ها و شرایط قرارداد", "CAREER", "International HR Manager", "C1",
            "Clarify mobilization, housing allowance, leave rotation, and compensation professionally.",
            "Final offer stage for an overseas engineering contract.",
            "HR gives a base salary lower than expected.",
            listOf(
                OfflineBranchNode(0, "We'd love to offer you the Senior Supervision Manager position. Our standard base package is \$X—does that align with your expectations?", listOf("Thank you for the offer—I'm very excited about the project. Considering my 30 years of supervision experience, could we review the base package or include a housing and mobilization allowance?"), listOf("thank you", "experience", "allowance", "review"), "قدردانی کنید و با تکیه بر ۳۰ سال سابقه، بسته‌های جانبی یا حقوق پایه را مذاکره کنید.")
            )),
        rp(19, "rp_19", "Making a Technical Presentation", "ارائه گزارش فنی به هیئت مدیره", "PM_MEETINGS", "Client Board & Stakeholders", "C1",
            "Structure a clear problem-solution-impact technical briefing.",
            "Quarterly review of foundation remediation and treatment plant progress.",
            "A board member asks a tough question about budget overrun.",
            listOf(
                OfflineBranchNode(0, "Please present the key findings from your geotechnical remediation review and what it means for the project budget.", listOf("In summary, adopting micropiles added 3% to foundation cost but avoided a four-month dewatering delay and protected adjacent structures."), listOf("summary", "micropiles", "avoided", "protected"), "اصل راه‌حل و صرفه‌جویی زمانی/ایمنی آن را برجسته کنید.")
            )),
        rp(20, "rp_20", "Handling Disagreement in Site Meeting", "مدیریت اختلاف‌نظر در جلسه کارگاهی", "PM_MEETINGS", "Contractor & Design Lead", "C1",
            "De-escalate tension between design and contractor teams and steer toward data.",
            "Heated argument over who caused a 10-day rebar shop-drawing delay.",
            "Voices rise and both sides blame each other.",
            listOf(
                OfflineBranchNode(0, "The design team gave us incomplete details—it's 100% their fault! / No, your draftsmen misread Revision B!", listOf("Let's pause for a moment and separate contractual blame from today's site priority.", "Let's look at the RFI log dates right now, agree on the final sketch by 4 PM so rebar cutting resumes tomorrow, and log any time impact separately."), listOf("pause", "priority", "RFI", "separately"), "تنش را بخوابانید، کارگاه را نجات دهید و بحث قراردادی را جدا ثبت کنید.")
            ))
    )

    private fun rp(
        num: Int,
        id: String,
        title: String,
        titleFa: String,
        cat: String,
        role: String,
        cefr: String,
        objective: String,
        background: String,
        complication: String,
        nodes: List<OfflineBranchNode>
    ) = RolePlayScenarioEntity(
        id = id,
        number = num,
        title = title,
        titleFa = titleFa,
        category = cat,
        aiRole = role,
        difficultyCefr = cefr,
        objective = objective,
        background = background,
        complicationPrompt = complication,
        usefulExpressionsJson = json.encodeToString(
            listOf(
                ExpressionNote("Let's look at the contemporaneous records", "Base decisions on dated site logs", "بیایید به سوابق و گزارش‌های روزانه مستند تکیه کنیم", "Formal"),
                ExpressionNote("We cannot compromise structural integrity", "Quality and safety come first", "نمی‌توانیم یکپارچگی سازه را به خطر بیندازیم", "Technical")
            )
        ),
        targetVocabJson = json.encodeToString(listOf("clarify", "mitigate", "push back on", "non-conformance report", "critical path")),
        offlineBranchingJson = json.encodeToString(nodes)
    )

    fun getInitialErrorPatterns(): List<GrammarErrorPatternEntity> = listOf(
        GrammarErrorPatternEntity(
            patternKey = "prepositions_engineering",
            patternLabel = "Prepositions after technical verbs (e.g., comply with, account for, follow up on)",
            count = 2,
            lastExample = "Check 'comply with specification' instead of 'comply to'.",
            updatedAtEpochMs = System.currentTimeMillis()
        ),
        GrammarErrorPatternEntity(
            patternKey = "uncountable_site_nouns",
            patternLabel = "Uncountable engineering nouns (equipment, formwork, rebar, concrete, scaffolding)",
            count = 2,
            lastExample = "Use 'formwork' and 'equipment' (singular), not 'formworks' or 'equipments'.",
            updatedAtEpochMs = System.currentTimeMillis()
        ),
        GrammarErrorPatternEntity(
            patternKey = "past_tense_narration",
            patternLabel = "Consistent Past Simple / Past Perfect when narrating site incidents",
            count = 1,
            lastExample = "Use 'The pump tripped after the crew had left' for clear sequencing.",
            updatedAtEpochMs = System.currentTimeMillis()
        )
    )
}
