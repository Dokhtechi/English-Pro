package com.example.data.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object StarterVocabularySeed {
    private val json = Json { ignoreUnknownKeys = true }

    private fun w(
        id: String,
        word: String,
        ipaUs: String,
        ipaUk: String,
        pos: String,
        cefr: String,
        rank: Int,
        topic: String,
        category: String,
        register: String,
        fa: String,
        def: String,
        examples: List<VocabExample>,
        collocations: List<String>,
        synonyms: List<String>,
        antonyms: List<String>,
        family: List<String>,
        mistake: String,
        usUkNote: String = "",
        source: String = "English Pro Curated / Tatoeba CC0",
        license: String = "CC0 1.0 / Public Domain",
        pkgId: String = "pkg_core_vocab",
        now: Long = 0L
    ) = VocabWordEntity(
        id = id,
        word = word,
        ipaUs = ipaUs,
        ipaUk = ipaUk,
        partOfSpeech = pos,
        cefrLevel = cefr,
        frequencyRank = rank,
        topic = topic,
        category = category,
        register = register,
        persianMeaning = fa,
        englishDefinition = def,
        examplesJson = json.encodeToString(examples),
        collocationsJson = json.encodeToString(collocations),
        synonymsJson = json.encodeToString(synonyms),
        antonymsJson = json.encodeToString(antonyms),
        wordFamilyJson = json.encodeToString(family),
        commonMistakes = mistake,
        britishVsAmericanNote = usUkNote,
        sourceAttribution = source,
        licenseInfo = license,
        packageId = pkgId,
        createdAtEpochMs = now
    )

    fun getStarterWords(now: Long): List<VocabWordEntity> = listOf(
        // --- GENERAL B2/C1 COMMUNICATION & WORKPLACE ---
        w(
            "gen_001", "clarify", "/ˈklær.ɪ.faɪ/", "/ˈklær.ɪ.faɪ/", "verb", "B2", 1,
            "Meetings", "GENERAL", "NEUTRAL",
            "روشن کردن، شفاف‌سازی کردن",
            "To make a statement or situation less confused and more clearly understandable.",
            listOf(
                VocabExample("Could you clarify what you meant by the revised completion date?", "ممکن است توضیح دهید منظورتان از تاریخ تکمیل اصلاح‌شده چیست؟"),
                VocabExample("Let me clarify our position before we sign the minutes of meeting.", "اجازه دهید قبل از امضای صورت‌جلسه موضع خود را روشن کنم.")
            ),
            listOf("clarify a point", "seek clarification", "clarify the scope"),
            listOf("explain", "elucidate", "spell out"), listOf("obscure", "confuse"),
            listOf("clarification (n)", "clear (adj)", "clearly (adv)"),
            "Avoid saying 'clarify me'. Use 'clarify this point for me'.",
            now = now
        ),
        w(
            "gen_002", "bottleneck", "/ˈbɑː.t̬əl.nek/", "/ˈbɒt.əl.nek/", "noun", "B2", 2,
            "Operations", "GENERAL", "NEUTRAL",
            "گلوگاه، مانع اصلی پیشرفت کار",
            "A point of congestion or delay that slows down an entire workflow or project.",
            listOf(
                VocabExample("Steel delivery has become the main bottleneck on this site.", "تحویل فولاد به گلوگاه اصلی این کارگاه تبدیل شده است."),
                VocabExample("We need to identify bottlenecks before the commissioning phase begins.", "پیش از شروع مرحله راه‌اندازی باید گلوگاه‌ها را شناسایی کنیم.")
            ),
            listOf("create a bottleneck", "resolve a bottleneck", "supply chain bottleneck"),
            listOf("obstruction", "impediment", "constraint"), listOf("breakthrough"),
            listOf("bottlenecked (adj)"),
            "Often used metaphorically in management; pronounce clearly without dropping /t/ or /k/.",
            "US uses flap /t̬/ (/ˈbɑː.t̬əl.nek/); UK keeps crisp /t/ (/ˈbɒt.əl.nek/).",
            now = now
        ),
        w(
            "gen_003", "feasible", "/ˈfiː.zə.bəl/", "/ˈfiː.zə.bəl/", "adjective", "B2", 3,
            "Decision Making", "GENERAL", "FORMAL",
            "عملی، امکان‌پذیر، شدنی",
            "Possible and practical to achieve easily or conveniently under current constraints.",
            listOf(
                VocabExample("Pumping at that flow rate isn't technically feasible with one unit.", "پمپاژ با آن دبی با یک دستگاه از نظر فنی عملی نیست."),
                VocabExample("Is it economically feasible to relocate the utility trench?", "آیا جابه‌جایی ترانشه تأسیسات از نظر اقتصادی به‌صرفه و عملی است؟")
            ),
            listOf("technically feasible", "economically feasible", "feasibility study"),
            listOf("viable", "practical", "workable"), listOf("impractical", "unfeasible"),
            listOf("feasibility (n)", "feasibly (adv)"),
            "Do not confuse 'feasible' (practical/doable) with 'possible' (merely conceivable).",
            now = now
        ),
        w(
            "gen_004", "mitigate", "/ˈmɪt̬.ɪ.ɡeɪt/", "/ˈmɪt.ɪ.ɡeɪt/", "verb", "C1", 4,
            "Risk Management", "GENERAL", "FORMAL",
            "کاهش دادن شدت یا اثر منفی، تعدیل کردن",
            "To make something less severe, harmful, or painful.",
            listOf(
                VocabExample("We installed sheet piles to mitigate the risk of trench collapse.", "برای کاهش ریسک ریزش ترانشه، سپرکوبی اجرا کردیم."),
                VocabExample("The contractor proposed night shifts to mitigate the schedule delay.", "پیمانکار شیفت شب را برای جبران بخشی از تاخیر برنامه پیشنهاد داد.")
            ),
            listOf("mitigate risk", "mitigation measure", "mitigate the impact"),
            listOf("alleviate", "reduce", "curb"), listOf("aggravate", "exacerbate"),
            listOf("mitigation (n)", "mitigating (adj)"),
            "Use 'mitigate a risk/problem', NOT 'mitigate against a problem'.",
            now = now
        ),
        w(
            "gen_005", "follow up on", "/ˈfɑː.loʊ ʌp ɑːn/", "/ˈfɒl.əʊ ʌp ɒn/", "phrasal verb", "B2", 5,
            "Communication", "GENERAL", "NEUTRAL",
            "پیگیری کردن، دنبال کردن نتیجه کاری",
            "To check the progress of something or take further action after an earlier step.",
            listOf(
                VocabExample("I'll follow up on the lab test results first thing tomorrow.", "فردا اول وقت نتایج آزمایشگاه را پیگیری می‌کنم."),
                VocabExample("Did you follow up on the outstanding RFI with the structural consultant?", "آیا RFI معوق را از مشاور سازه پیگیری کردید؟")
            ),
            listOf("follow up on an issue", "follow-up meeting", "follow up by email"),
            listOf("pursue", "check on", "track"), listOf("neglect", "drop"),
            listOf("follow-up (n/adj)"),
            "As a verb write 'follow up' (two words); as a noun/adjective write 'follow-up' (hyphenated).",
            now = now
        ),
        w(
            "gen_006", "rule out", "/ruːl aʊt/", "/ruːl aʊt/", "phrasal verb", "B2", 6,
            "Problem Solving", "GENERAL", "NEUTRAL",
            "رد کردن احتمال، کنار گذاشتن گزینه",
            "To exclude something as a possibility or option.",
            listOf(
                VocabExample("We can't rule out groundwater seepage until the borehole logs arrive.", "تا رسیدن لاگ گمانه‌ها نمی‌توانیم نفوذ آب زیرزمینی را منتفی بدانیم."),
                VocabExample("The client ruled out any extension of cost.", "کارفرما هرگونه افزایش هزینه را منتفی اعلام کرد.")
            ),
            listOf("rule out the possibility", "cannot be ruled out"),
            listOf("exclude", "eliminate", "discount"), listOf("consider", "allow for"),
            listOf("ruling out"),
            "Always use 'out' — never 'rule off' for excluding possibilities.",
            now = now
        ),
        w(
            "gen_007", "straightforward", "/ˌstreɪtˈfɔːr.wɚd/", "/ˌstreɪtˈfɔː.wəd/", "adjective", "B2", 7,
            "Everyday & Work", "GENERAL", "NEUTRAL",
            "سرراست، ساده و بدون پیچیدگی، رک",
            "Uncomplicated and easy to understand or execute; honest and direct.",
            listOf(
                VocabExample("The site access route is fairly straightforward once you pass the main gate.", "مسیر دسترسی کارگاه پس از عبور از دروازه اصلی کاملاً سرراست است."),
                VocabExample("Replacing the gasket is a straightforward maintenance task.", "تعویض واشر یک کار نگهداری ساده و سرراست است.")
            ),
            listOf("straightforward process", "quite straightforward", "be straightforward with someone"),
            listOf("uncomplicated", "direct", "clear-cut"), listOf("convoluted", "tricky"),
            listOf("straightforwardly (adv)", "straightforwardness (n)"),
            "Spelled as one word, not 'straight forward'.",
            now = now
        ),
        w(
            "gen_008", "compromise", "/ˈkɑːm.prə.maɪz/", "/ˈkɒm.prə.maɪz/", "verb/noun", "B2", 8,
            "Negotiation & Quality", "GENERAL", "NEUTRAL",
            "مصالحه کردن؛ به خطر انداختن (کیفیت/ایمنی)",
            "To settle a dispute by mutual concession; OR to weaken quality, safety, or integrity.",
            listOf(
                VocabExample("We will never compromise structural safety to catch up on the schedule.", "ما هرگز ایمنی سازه را فدای جبران برنامه زمانی نمی‌کنیم."),
                VocabExample("Both parties reached a practical compromise on the night-shift hours.", "هر دو طرف بر سر ساعات شیفت شب به یک توافق عملی رسیدند.")
            ),
            listOf("compromise safety", "reach a compromise", "without compromising quality"),
            listOf("jeopardize", "settle", "meet halfway"), listOf("safeguard", "insist"),
            listOf("compromising (adj)", "uncompromising (adj)"),
            "In engineering, 'compromise quality' is negative (damage quality), whereas 'reach a compromise' is positive (negotiate).",
            now = now
        ),
        w(
            "gen_009", "account for", "/əˈkaʊnt fɔːr/", "/əˈkaʊnt fɔː/", "phrasal verb", "B2", 9,
            "Analysis", "GENERAL", "FORMAL",
            "توجیه کردن، توضیح دادن علت؛ در نظر گرفتن",
            "To explain the reason for something, or constitute a specific share of a total.",
            listOf(
                VocabExample("Earthworks account for nearly 25 percent of the initial budget.", "عملیات خاکی نزدیک به ۲۵ درصد بودجه اولیه را تشکیل می‌دهد."),
                VocabExample("How do you account for the discrepancy in the survey coordinates?", "اختلاف در مختصات نقشه‌برداری را چگونه توجیه می‌کنید؟")
            ),
            listOf("account for delays", "account for a discrepancy", "account for 30% of"),
            listOf("explain", "justify", "comprise"), listOf("ignore"),
            listOf("accountable (adj)", "accountability (n)"),
            "Always requires 'for'. Never say 'account the delay'.",
            now = now
        ),
        w(
            "gen_010", "discrepancy", "/dɪˈskrep.ən.si/", "/dɪˈskrep.ən.si/", "noun", "C1", 10,
            "Quality & Drawings", "GENERAL", "FORMAL",
            "مغایرت، اختلاف بین دو مدرک یا عدد",
            "An unexpected difference between two figures, facts, drawings, or accounts.",
            listOf(
                VocabExample("There is a clear discrepancy between the structural drawings and the MEP layout.", "بین نقشه‌های سازه و نقشه تأسیسات مغایرت واضحی وجود دارد."),
                VocabExample("Please issue an RFI to resolve the elevation discrepancy.", "لطفاً برای رفع مغایرت تراز ارتفاعی یک RFI صادر کنید.")
            ),
            listOf("resolve a discrepancy", "spot a discrepancy", "discrepancy between A and B"),
            listOf("inconsistency", "mismatch", "variance"), listOf("consistency", "agreement"),
            listOf("discrepant (adj)"),
            "Use 'discrepancy between X and Y', not 'discrepancy of X with Y'.",
            now = now
        ),
        w(
            "gen_011", "alleviate", "/əˈliː.vi.eɪt/", "/əˈliː.vi.eɪt/", "verb", "C1", 11,
            "Problem Solving", "GENERAL", "FORMAL",
            "تسکین دادن، سبک‌تر کردن مشکل",
            "To make a problem, congestion, or difficulty less intense or severe.",
            listOf(
                VocabExample("Adding a second crane will alleviate the material-hoisting congestion.", "افزودن جرثقیل دوم ازدحام جابه‌جایی مصالح را کاهش می‌دهد.")
            ),
            listOf("alleviate pressure", "alleviate congestion"),
            listOf("ease", "relieve", "mitigate"), listOf("worsen", "intensify"),
            listOf("alleviation (n)"),
            "Note stress on second syllable: a-LEE-vi-ate.",
            now = now
        ),
        w(
            "gen_012", "unanimously", "/juːˈnæn.ə.məs.li/", "/juːˈnæn.ɪ.məs.li/", "adverb", "C1", 12,
            "Meetings", "GENERAL", "FORMAL",
            "به‌اتفاق آرا",
            "Without opposition; with the agreement of all people involved.",
            listOf(
                VocabExample("The committee unanimously approved the revised dewatering method statement.", "کمیته به‌اتفاق آرا روش اجرای اصلاح‌شده زهکشی را تصویب کرد.")
            ),
            listOf("unanimously agreed", "unanimously approved"),
            listOf("collectively", "by consensus"), listOf("dividedly"),
            listOf("unanimous (adj)", "unanimity (n)"),
            "Starts with /j/ sound ('yoo-NAN...'), so use 'a unanimous decision', not 'an unanimous'.",
            now = now
        ),
        w(
            "gen_013", "come across", "/kʌm əˈkrɑːs/", "/kʌm əˈkrɒs/", "phrasal verb", "B2", 13,
            "Everyday English", "GENERAL", "INFORMAL",
            "به‌تصادف برخورد کردن به؛ به نظر رسیدن",
            "To find something by chance; or to give a particular impression to others.",
            listOf(
                VocabExample("During excavation, the crew came across an uncharted utility line.", "حین گودبرداری، اکیپ به یک خط تأسیسات ثبت‌نشده برخورد کرد."),
                VocabExample("You want to come across as confident yet collaborative in the interview.", "در مصاحبه باید فردی بااعتمادبه‌نفس و در عین حال اهل همکاری به نظر برسید.")
            ),
            listOf("come across an issue", "come across as confident"),
            listOf("encounter", "stumble upon", "seem"), listOf("overlook"),
            listOf("-"),
            "Inseparable phrasal verb: say 'came across a cable', NEVER 'came a cable across'.",
            now = now
        ),
        w(
            "gen_014", "bring up", "/brɪŋ ʌp/", "/brɪŋ ʌp/", "phrasal verb", "B2", 14,
            "Meetings", "GENERAL", "NEUTRAL",
            "مطرح کردن موضوع در جلسه",
            "To start talking about a particular subject in a conversation or meeting.",
            listOf(
                VocabExample("I'd like to bring up the concrete curing delay under Item 4 of the agenda.", "می‌خواهم موضوع تاخیر عمل‌آوری بتن را زیر بند ۴ دستور جلسه مطرح کنم.")
            ),
            listOf("bring up an issue", "glad you brought that up"),
            listOf("raise", "mention", "table"), listOf("suppress", "drop"),
            listOf("-"),
            "Very natural in spoken English; 'raise' is slightly more formal.",
            "In British meetings, 'table an item' means bring it up for discussion; in US English 'table' means postpone it!",
            now = now
        ),
        w(
            "gen_015", "tentative", "/ˈten.tə.tɪv/", "/ˈten.tə.tɪv/", "adjective", "B2", 15,
            "Scheduling", "GENERAL", "NEUTRAL",
            "موقتی، آزمایشی، قطعی‌نشده",
            "Not certain or fixed; provisional and subject to confirmation.",
            listOf(
                VocabExample("We have a tentative handover date set for mid-October.", "یک تاریخ تحویل موقت برای اواسط اکتبر تعیین کرده‌ایم.")
            ),
            listOf("tentative schedule", "tentative agreement", "tentatively scheduled"),
            listOf("provisional", "preliminary"), listOf("definitive", "finalized"),
            listOf("tentatively (adv)"),
            "Don't confuse with 'tempting' (وسوسه‌انگیز).",
            now = now
        ),
        w(
            "gen_016", "reiterate", "/riˈɪt̬.ə.reɪt/", "/riˈɪt.ə.reɪt/", "verb", "C1", 16,
            "Communication", "GENERAL", "FORMAL",
            "تاکید مجدد کردن، دوباره بیان کردن",
            "To say something again or a number of times, typically for emphasis or clarity.",
            listOf(
                VocabExample("Let me reiterate that no concrete pour may proceed without an approved inspection request.", "اجازه دهید مجدداً تاکید کنم هیچ بتن‌ریزی بدون درخواست بازرسی تاییدشده مجاز نیست.")
            ),
            listOf("reiterate a requirement", "reiterate our commitment"),
            listOf("restate", "emphasize", "underscore"), listOf("retract"),
            listOf("reiteration (n)"),
            "Do NOT say 'reiterate again' — 're-' already means again!",
            now = now
        ),

        // --- D1: CONSTRUCTION SITE VOCABULARY ---
        w(
            "d1_001", "excavation", "/ˌek.skəˈveɪ.ʃən/", "/ˌek.skəˈveɪ.ʃən/", "noun", "B2", 17,
            "D1 Construction Site", "CIVIL_SITE", "TECHNICAL",
            "گودبرداری، خاک‌برداری",
            "The process of digging out earth, rock, or other materials to form a cavity or trench for foundations.",
            listOf(
                VocabExample("Deep excavation requires continuous monitoring of adjacent building settlements.", "گودبرداری عمیق نیازمند پایش مستمر نشست ساختمان‌های مجاور است.")
            ),
            listOf("bulk excavation", "trench excavation", "excavation support"),
            listOf("digging", "trenching"), listOf("backfilling"),
            listOf("excavate (v)", "excavator (n)"),
            "Use 'carry out excavation', not 'do excavation'.",
            source = "USACE EM 385-1-1 / OSHA", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d1_002", "dewatering", "/diːˈwɔː.t̬ɚ.ɪŋ/", "/diːˈwɔː.tər.ɪŋ/", "noun", "B2", 18,
            "D1 Construction Site", "CIVIL_SITE", "TECHNICAL",
            "زهکشی و آب‌کشی کارگاه / پایین بردن سطح آب زیرزمینی",
            "The removal of groundwater or surface water from a construction site, caisson, or excavation.",
            listOf(
                VocabExample("Well-point dewatering lowered the water table two meters below the blinding level.", "آب‌کشی با سیستم ول‌پوینت سطح آب زیرزمینی را دو متر پایین‌تر از تراز بتن مگر آورد.")
            ),
            listOf("well-point dewatering", "dewatering system", "submersible dewatering pump"),
            listOf("groundwater drainage", "pumping out"), listOf("flooding"),
            listOf("dewater (v)"),
            "Say 'dewatering', not 'unwatering' in modern international construction.",
            "In UK sites, lean concrete at excavation base is often called 'blinding'; in US, 'mud slab' or 'lean concrete'.",
            source = "USACE Engineering Manual", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d1_003", "shoring", "/ˈʃɔːr.ɪŋ/", "/ˈʃɔː.rɪŋ/", "noun", "B2", 19,
            "D1 Construction Site", "CIVIL_SITE", "TECHNICAL",
            "شمع‌بندی، مهاربندی دیواره گود یا قالب",
            "Temporary structural props or bracing used to support an unsafe structure or excavation wall.",
            listOf(
                VocabExample("The contractor failed to install adequate shoring along the north excavation face.", "پیمانکار در جبهه شمالی گود مهاربندی مناسب اجرا نکرده بود.")
            ),
            listOf("trench shoring", "shoring system", "hydraulic shoring"),
            listOf("bracing", "propping", "strutting"), listOf("-"),
            listOf("shore up (phrasal v)"),
            "'Shore up' is also a common figurative idiom meaning 'reinforce or support a weak plan/system'.",
            source = "OSHA 1926 Subpart P", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d1_004", "formwork", "/ˈfɔːrm.wɝːk/", "/ˈfɔːm.wɜːk/", "noun", "B2", 20,
            "D1 Construction Site", "CIVIL_SITE", "TECHNICAL",
            "قالب‌بندی بتن",
            "Temporary or permanent molds into which fresh concrete is poured until it sets and gains strength.",
            listOf(
                VocabExample("Inspect all formwork ties and alignment before authorizing the column pour.", "پیش از صدور مجوز بتن‌ریزی ستون‌ها، تمام بولت‌ها و شاغولی قالب‌بندی را کنترل کنید.")
            ),
            listOf("strip formwork", "slipform", "formwork alignment", "release agent"),
            listOf("shuttering", "falsework (support)"), listOf("-"),
            listOf("-"),
            "Uncountable noun: say 'formwork', NEVER 'formworks'.",
            "UK & Commonwealth engineers frequently say 'shuttering' alongside 'formwork'; US engineers almost exclusively say 'formwork'.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d1_005", "compaction", "/kəmˈpæk.ʃən/", "/kəmˈpæk.ʃən/", "noun", "B2", 21,
            "D1 Construction Site", "CIVIL_SITE", "TECHNICAL",
            "تراکم خاک یا بتن",
            "Increasing soil or fresh concrete density by mechanical vibration, rolling, or tamping to expel air voids.",
            listOf(
                VocabExample("Field density tests confirmed 98% compaction of the sub-base layer.", "آزمایش دانسیته درجا تراکم ۹۸ درصد لایه ساب‌بیس را تایید کرد.")
            ),
            listOf("compaction test", "Proctor compaction", "degree of compaction"),
            listOf("consolidation (note: distinct in geotech!)", "tamping"), listOf("loosening"),
            listOf("compact (v/adj)", "compactor (n)"),
            "In geotechnics, don't confuse rapid mechanical 'compaction' (air expulsion) with slow 'consolidation' (pore-water expulsion).",
            source = "USACE Soil Mechanics", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d1_006", "backfilling", "/ˈbæk.fɪl.ɪŋ/", "/ˈbæk.fɪl.ɪŋ/", "noun", "B2", 22,
            "D1 Construction Site", "CIVIL_SITE", "TECHNICAL",
            "خاک‌ریزی مجدد اطراف فونداسیون یا ترانشه",
            "Refilling an excavated trench or area around a foundation with approved engineered soil in compacted layers.",
            listOf(
                VocabExample("Backfilling against basement walls must wait until the waterproofing membrane passes inspection.", "خاک‌ریزی مجدد پشت دیوارهای زیرزمین باید تا تایید عایق رطوبتی متوقف بماند.")
            ),
            listOf("engineered backfill", "backfill in lifts", "pipe bedding and backfill"),
            listOf("refilling"), listOf("excavation"),
            listOf("backfill (n/v)"),
            "Say 'backfill in 200 mm lifts/layers', not 'step by step'.",
            "US uses 'lifts' for compacted soil layers; UK also commonly uses 'layers'.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d1_007", "scaffolding", "/ˈskæf.əl.dɪŋ/", "/ˈskæf.əʊl.dɪŋ/", "noun", "B2", 23,
            "D1 Construction Site", "CIVIL_SITE", "TECHNICAL",
            "داربست",
            "A temporary elevated platform and supporting framework used by workers and materials during construction.",
            listOf(
                VocabExample("No worker is permitted on the scaffolding without a valid green inspection tag.", "هیچ کارگری بدون تگ سبز بازرسی مجاز به کار روی داربست نیست.")
            ),
            listOf("erect scaffolding", "dismantle scaffolding", "scaffold tag"),
            listOf("staging"), listOf("-"),
            listOf("scaffold (n/v)", "scaffolder (n)"),
            "Uncountable for the whole system ('scaffolding'); 'a scaffold' refers to a single structure.",
            source = "OSHA 1926 Subpart L", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),

        // --- D2: CONCRETE AND STRUCTURAL ENGINEERING ---
        w(
            "d2_001", "honeycombing", "/ˈhʌn.i.koʊ.mɪŋ/", "/ˈhʌn.i.kəʊ.mɪŋ/", "noun", "B2", 24,
            "D2 Concrete & Structural", "CONCRETE_STRUCTURAL", "TECHNICAL",
            "کرموشدگی بتن",
            "Voids or rough stony pockets left in hardened concrete due to insufficient vibration or mortar leakage.",
            listOf(
                VocabExample("After stripping the wall formwork, we discovered severe honeycombing around the construction joint.", "پس از باز کردن قالب دیوار، کرموشدگی شدیدی در اطراف درز اجرایی مشاهده کردیم.")
            ),
            listOf("severe honeycombing", "repair honeycombing", "segregation and honeycombing"),
            listOf("voids", "rock pockets"), listOf("dense concrete finish"),
            listOf("honeycombed (adj)"),
            "Pronounce as 'honey-combing' (silent 'b').",
            "US also uses 'rock pockets'; international FIDIC sites prefer 'honeycombing'.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d2_002", "slump test", "/slʌmp test/", "/slʌmp test/", "noun", "B2", 25,
            "D2 Concrete & Structural", "CONCRETE_STRUCTURAL", "TECHNICAL",
            "آزمایش اسلامپ بتن (روانی بتن)",
            "A field test using a truncated cone to measure the workability and consistency of fresh concrete.",
            listOf(
                VocabExample("The truck mixer was rejected because the slump test exceeded 160 mm.", "تراک میکسر به دلیل اسلامپ بالای ۱۶۰ میلی‌متر رد شد.")
            ),
            listOf("perform a slump test", "slump cone", "slump loss"),
            listOf("workability test"), listOf("-"),
            listOf("slump (n/v)"),
            "Say 'high/low slump', not 'big/small slump'.",
            "US follows ASTM C143; UK/EU follow BS EN 12350-2.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d2_003", "concrete cover", "/ˈkɑːn.kriːt ˈkʌv.ɚ/", "/ˈkɒŋ.kriːt ˈkʌv.ə/", "noun", "B2", 26,
            "D2 Concrete & Structural", "CONCRETE_STRUCTURAL", "TECHNICAL",
            "پوشش بتنی روی میلگرد (کاور بتن)",
            "The minimum thickness of concrete protecting steel reinforcement from corrosion and fire.",
            listOf(
                VocabExample("Use plastic spacers to maintain a minimum 50 mm concrete cover in the aeration tank.", "برای حفظ حداقل ۵۰ میلی‌متر کاور بتن در حوضچه هوادهی از اسپیسر پلاستیکی استفاده کنید.")
            ),
            listOf("nominal cover", "cover block / spacer", "insufficient concrete cover"),
            listOf("rebar cover"), listOf("exposed rebar"),
            listOf("-"),
            "Say 'concrete cover' or 'clear cover', not 'concrete covering'.",
            "UK uses 'cover blocks' or 'spacers'; US often says 'rebar chairs' or 'dobies'.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d2_004", "cold joint", "/koʊld dʒɔɪnt/", "/kəʊld dʒɔɪnt/", "noun", "C1", 27,
            "D2 Concrete & Structural", "CONCRETE_STRUCTURAL", "TECHNICAL",
            "درز سرد بتن",
            "A plane of weakness formed when fresh concrete is poured against concrete that has already begun to set.",
            listOf(
                VocabExample("A two-hour batching plant breakdown created a risk of a cold joint in the raft slab.", "خرابی دو ساعته بچینگ ریسک ایجاد درز سرد در دال فونداسیون را بالا برد.")
            ),
            listOf("prevent a cold joint", "unexpected cold joint", "waterstop at cold joint"),
            listOf("unintended construction joint"), listOf("monolithic pour"),
            listOf("-"),
            "Distinguish planned 'construction joint' (درز اجرایی برنامه‌ریزی‌شده) from unplanned 'cold joint' (درز سرد).",
            source = "USACE Concrete Practice", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d2_005", "compressive strength", "/kəmˈpres.ɪv streŋθ/", "/kəmˈpres.ɪv streŋθ/", "noun", "B2", 28,
            "D2 Concrete & Structural", "CONCRETE_STRUCTURAL", "TECHNICAL",
            "مقاومت فشاری",
            "The capacity of a material such as concrete or rock to withstand axially directed pushing forces.",
            listOf(
                VocabExample("The 28-day concrete cube compressive strength reached 42 MPa, above the C35 specification.", "مقاومت فشاری ۲۸ روزه نمونه مکعبی بتن به ۴۲ مگاپاسکال رسید که بالاتر از مشخصات C35 است.")
            ),
            listOf("28-day compressive strength", "cube/cylinder crushing test"),
            listOf("crushing strength"), listOf("tensile strength"),
            listOf("compress (v)", "compression (n)"),
            "UK/Middle East sites test 150 mm 'cubes'; US/ACI projects test 150x300 mm 'cylinders' (remember cylinder strength ≈ 0.8 × cube strength).",
            "Cube test (BS EN) vs Cylinder test (ASTM C39).",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d2_006", "concrete curing", "/ˈkɑːn.kriːt ˈkjʊr.ɪŋ/", "/ˈkɒŋ.kriːt ˈkjʊə.rɪŋ/", "noun", "B2", 29,
            "D2 Concrete & Structural", "CONCRETE_STRUCTURAL", "TECHNICAL",
            "عمل‌آوری بتن",
            "Maintaining adequate moisture and temperature in freshly placed concrete to ensure proper hydration.",
            listOf(
                VocabExample("In hot weather, wet burlap and curing compound must be applied immediately after finishing.", "در هوای گرم بلافاصله پس از پرداخت باید گونی خیس و کیورینگ کامپاند اعمال شود.")
            ),
            listOf("wet curing", "curing compound", "curing period"),
            listOf("hydration maintenance"), listOf("premature drying"),
            listOf("cure (v)"),
            "In English, concrete 'cures' (gains strength via hydration), it doesn't 'dry'.",
            "US says 'burlap'; UK says 'hessian' for wet curing cloth.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d2_007", "rebar spacing", "/ˈriː.bɑːr ˈspeɪ.sɪŋ/", "/ˈriː.bɑː ˈspeɪ.sɪŋ/", "noun", "B2", 30,
            "D2 Concrete & Structural", "CONCRETE_STRUCTURAL", "TECHNICAL",
            "فاصله آرماتورها / میلگردها",
            "The center-to-center or clear distance between reinforcing steel bars in a concrete element.",
            listOf(
                VocabExample("Verify that rebar spacing at the slab edge matches 150 mm centers on Drawing S-104.", "کنترل کنید فاصله میلگردها در لبه دال با آکس‌به‌آکس ۱۵۰ میلی‌متر در نقشه S-104 مطابقت داشته باشد.")
            ),
            listOf("center-to-center spacing", "bar lap splice", "reinforcement detailing"),
            listOf("bar pitch", "reinforcement spacing"), listOf("-"),
            listOf("rebar (n)", "reinforce (v)"),
            "'Rebar' is uncountable when referring to material ('tons of rebar'), not 'rebars'.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),

        // --- D3: GEOTECHNICAL AND FOUNDATION ENGINEERING ---
        w(
            "d3_001", "bearing capacity", "/ˈber.ɪŋ kəˈpæs.ə.t̬i/", "/ˈbeə.rɪŋ kəˈpæs.ə.ti/", "noun", "B2", 31,
            "D3 Geotechnical", "GEOTECHNICAL", "TECHNICAL",
            "ظرفیت باربری خاک",
            "The maximum average contact pressure between the foundation and the soil without shear failure or excessive settlement.",
            listOf(
                VocabExample("Plate load tests indicated that the allowable bearing capacity is lower than assumed in the design.", "آزمایش بارگذاری صفحه‌ای نشان داد ظرفیت باربری مجاز کمتر از فرض طراحی است.")
            ),
            listOf("allowable bearing capacity", "ultimate bearing capacity", "bearing stratum"),
            listOf("soil support capacity"), listOf("bearing failure"),
            listOf("bear (v)"),
            "Always specify 'allowable' vs 'ultimate' bearing capacity in engineering discussions.",
            source = "USACE Geotechnical Manual", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d3_002", "micropile", "/ˈmaɪ.kroʊ.paɪl/", "/ˈmaɪ.krəʊ.paɪl/", "noun", "C1", 32,
            "D3 Geotechnical", "GEOTECHNICAL", "TECHNICAL",
            "میکروپایل (ریز‌شمع)",
            "A small-diameter (typically <300 mm), drilled and grouted replacement pile reinforced with high-strength steel.",
            listOf(
                VocabExample("Micropiles were selected for underpinning the pump station due to restricted headroom.", "به‌دلیل محدودیت ارتفاع، برای بهسازی زیر پی ایستگاه پمپاژ از میکروپایل استفاده شد.")
            ),
            listOf("grouted micropile", "micropile load test", "underpinning with micropiles"),
            listOf("minipile"), listOf("driven precast pile"),
            listOf("piling (n)"),
            "Useful term when explaining retrofit, low-headroom, or soft-soil foundation solutions.",
            "Also called 'minipile' in UK geotechnical specs.",
            source = "FHWA / USACE", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d3_003", "differential settlement", "/ˌdɪf.əˈren.ʃəl ˈset̬.əl.mənt/", "/ˌdɪf.əˈren.ʃəl ˈset.əl.mənt/", "noun", "C1", 33,
            "D3 Geotechnical", "GEOTECHNICAL", "TECHNICAL",
            "نشست نامتقارن پی",
            "Unequal downward movement of different parts of a foundation, causing structural tilting or cracking.",
            listOf(
                VocabExample("Excessive differential settlement between the clarifier tank and inlet channel damaged the expansion joint.", "نشست نامتقارن بیش‌ازحد بین حوضچه ته‌نشینی و کانال ورودی به درز انبساط آسیب زد.")
            ),
            listOf("limit differential settlement", "settlement monitoring pins"),
            listOf("uneven settlement"), listOf("uniform settlement"),
            listOf("settle (v)"),
            "Say 'settlement' (singular uncountable) for structural sinking, not 'settlements'.",
            source = "USACE Settlement Manual", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d3_004", "borehole", "/ˈbɔːr.hoʊl/", "/ˈbɔː.həʊl/", "noun", "B2", 34,
            "D3 Geotechnical", "GEOTECHNICAL", "TECHNICAL",
            "گمانه ژئوتکنیکی",
            "A deep, narrow shaft drilled into the ground for soil investigation, SPT sampling, or groundwater monitoring.",
            listOf(
                VocabExample("Borehole BH-04 encountered dense gravel at a depth of 14 meters.", "گمانه BH-04 در عمق ۱۴ متری به شن متراکم برخورد کرد.")
            ),
            listOf("borehole log", "SPT borehole", "core recovery"),
            listOf("test boring", "drill hole"), listOf("-"),
            listOf("bore (v)"),
            "Pronounce clearly as two parts: BORE-hole.",
            "UK prefers 'borehole'; US also frequently uses 'soil boring'.",
            source = "English Pro Civil Corpus", license = "CC0 1.0", pkgId = "pkg_civil_vocab", now = now
        ),
        w(
            "d3_005", "retaining wall", "/rɪˈteɪ.nɪŋ wɔːl/", "/rɪˈteɪ.nɪŋ wɔːl/", "noun", "B2", 35,
            "D3 Geotechnical", "GEOTECHNICAL", "TECHNICAL",
            "دیوار حائل",
            "A rigid structural wall designed to restrain lateral earth pressure where ground elevation changes abruptly.",
            listOf(
                VocabExample("Weep holes must remain unobstructed to relieve hydrostatic pressure behind the retaining wall.", "سوراخ‌های زهکش باید باز بمانند تا فشار هیدرواستاتیک پشت دیوار حائل تخلیه شود.")
            ),
            listOf("cantilever retaining wall", "secant pile wall", "weep holes"),
            listOf("earth-retaining structure"), listOf("-"),
            listOf("retain (v)"),
            "Use 'lateral earth pressure', not 'side soil pressure'.",
            source = "USACE Retaining Walls", license = "Public Domain", pkgId = "pkg_civil_vocab", now = now
        ),

        // --- D4: WATER AND WASTEWATER TREATMENT ---
        w(
            "d4_001", "clarifier", "/ˈkler.ə.faɪ.ɚ/", "/ˈklær.ɪ.faɪ.ə/", "noun", "C1", 36,
            "D4 Water & Wastewater", "WATER_WASTEWATER", "TECHNICAL",
            "حوضچه ته‌نشینی / کلاریفایر",
            "A large settling tank used in water and wastewater treatment to separate suspended solids by gravity.",
            listOf(
                VocabExample("The radial scraper bridge in Secondary Clarifier No. 2 requires laser alignment before wet commissioning.", "پل روبنده دوار در کلاریفایر ثانویه شماره ۲ پیش از تست آب‌گیری نیازمند هم‌محوری لیزری است.")
            ),
            listOf("primary clarifier", "secondary clarifier", "sludge scraper", "weir level"),
            listOf("sedimentation tank", "settling basin"), listOf("-"),
            listOf("clarification (n)"),
            "Distinguish 'clarifier' (equipment/tank) from 'clarify' (verb).",
            source = "EPA / USACE Wastewater Design", license = "Public Domain", pkgId = "pkg_wastewater", now = now
        ),
        w(
            "d4_002", "aeration tank", "/erˈeɪ.ʃən tæŋk/", "/eəˈreɪ.ʃən tæŋk/", "noun", "C1", 37,
            "D4 Water & Wastewater", "WATER_WASTEWATER", "TECHNICAL",
            "حوضچه هوادهی",
            "A biological reactor basin where air or oxygen is diffused into wastewater to sustain aerobic microorganisms.",
            listOf(
                VocabExample("Check the membrane diffusers across the aeration tank floor before performing the clean-water oxygen transfer test.", "پیش از انجام تست انتقال اکسیژن با آب تمیز، دیفیوزرهای غشایی کف حوضچه هوادهی را بازبینی کنید.")
            ),
            listOf("fine-bubble diffuser", "activated sludge process", "dissolved oxygen (DO)"),
            listOf("biological reactor"), listOf("anaerobic digester"),
            listOf("aerate (v)", "aeration (n)"),
            "Pronounced 'air-AY-shun', NOT 'ah-er-ation'.",
            source = "EPA Wastewater Technology", license = "Public Domain", pkgId = "pkg_wastewater", now = now
        ),
        w(
            "d4_003", "commissioning", "/kəˈmɪʃ.ən.ɪŋ/", "/kəˈmɪʃ.ən.ɪŋ/", "noun", "B2", 38,
            "D4 Water & Wastewater", "WATER_WASTEWATER", "TECHNICAL",
            "راه‌اندازی و تست عملکردی سیستم پیش از تحویل",
            "The systematic testing, verification, and startup of plant equipment and pipelines prior to operational handover.",
            listOf(
                VocabExample("Dry commissioning of the blowers is complete; wet commissioning starts next Monday.", "راه‌اندازی خشک بلوئرها کامل شده است؛ تست آب‌گیری از دوشنبه آینده شروع می‌شود.")
            ),
            listOf("pre-commissioning", "dry/wet commissioning", "commissioning checklist", "handover"),
            listOf("startup testing", "functional testing"), listOf("decommissioning"),
            listOf("commission (v)"),
            "In engineering, 'commissioning' means testing & bringing into service (NOT paying a sales commission!).",
            source = "English Pro Wastewater Corpus", license = "CC0 1.0", pkgId = "pkg_wastewater", now = now
        ),
        w(
            "d4_004", "activated sludge", "/ˈæk.tɪ.veɪ.t̬ɪd slʌdʒ/", "/ˈæk.tɪ.veɪ.tɪd slʌdʒ/", "noun", "C1", 39,
            "D4 Water & Wastewater", "WATER_WASTEWATER", "TECHNICAL",
            "لجن فعال (در تصفیه بیولوژیکی فاضلاب)",
            "A biological mass of aerobic bacteria and protozoa grown in aeration tanks to break down organic pollutants.",
            listOf(
                VocabExample("Return activated sludge pumps must maintain steady recirculation to stabilize MLSS levels.", "پمپ‌های برگشت لجن فعال باید سیرکولاسیون پایدار را برای تثبیت MLSS حفظ کنند.")
            ),
            listOf("return activated sludge (RAS)", "waste activated sludge (WAS)", "sludge dewatering"),
            listOf("biological floc"), listOf("-"),
            listOf("-"),
            "Pronounce 'sludge' with /ʌ/ ('sluj'), rhyming with 'judge'.",
            source = "EPA Wastewater Terminology", license = "Public Domain", pkgId = "pkg_wastewater", now = now
        ),
        w(
            "d4_005", "hydrostatic test", "/ˌhaɪ.droʊˈstæt̬.ɪk test/", "/ˌhaɪ.drəˈstæt.ɪk test/", "noun", "B2", 40,
            "D4 Water & Wastewater", "WATER_WASTEWATER", "TECHNICAL",
            "آزمایش فشار آب (هیدرواستاتیک) خطوط لوله و مخازن",
            "A pressure test where a pipe or water-retaining tank is filled with water to verify leak-tightness and structural integrity.",
            listOf(
                VocabExample("All HDPE process pipelines passed the hydrostatic test at 1.5 times the operating pressure.", "تمامی خطوط لوله فرآیندی HDPE تست هیدرواستاتیک را با ۱.۵ برابر فشار کاری پاس کردند.")
            ),
            listOf("water-tightness test", "pressure gauge", "allowable drop"),
            listOf("pressure test", "leak test"), listOf("-"),
            listOf("hydrostatically (adv)"),
            "Distinguish 'water-tightness test' (for concrete tanks) from 'pressure test' (for closed pipes).",
            source = "USACE Pipeline Specs", license = "Public Domain", pkgId = "pkg_wastewater", now = now
        ),

        // --- D5: PROJECT MANAGEMENT, QA/QC & FIDIC CONTRACTS ---
        w(
            "d5_001", "non-conformance report", "/ˌnɑːn.kənˈfɔːr.məns rɪˈpɔːrt/", "/ˌnɒn.kənˈfɔː.məns rɪˈpɔːt/", "noun", "B2", 41,
            "D5 PM & QA/QC", "PROJECT_MANAGEMENT", "TECHNICAL",
            "گزارش عدم انطباق (NCR)",
            "A formal quality document (NCR) recording work or materials that fail to meet project specifications or drawings.",
            listOf(
                VocabExample("The Resident Engineer issued an NCR due to inadequate concrete cover in Pier 3.", "مهندس مقیم به‌دلیل کاور ناکافی بتن در پایه شماره ۳ یک گزارش عدم انطباق صادر کرد.")
            ),
            listOf("issue an NCR", "close out an NCR", "root cause analysis", "corrective action"),
            listOf("NCR", "defect notice"), listOf("compliance certificate"),
            listOf("non-conforming (adj)"),
            "Say 'issue an NCR' and 'close out an NCR' (not 'finish an NCR').",
            source = "English Pro PM Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d5_002", "method statement", "/ˈmeθ.əd ˈsteɪt.mənt/", "/ˈmeθ.əd ˈsteɪt.mənt/", "noun", "B2", 42,
            "D5 PM & QA/QC", "PROJECT_MANAGEMENT", "TECHNICAL",
            "شرح روش اجرا (Method Statement)",
            "A detailed document submitted by a contractor explaining step-by-step how a specific construction activity will be safely executed.",
            listOf(
                VocabExample("No heavy lifting is allowed until the rigging method statement is approved.", "تا زمانی که شرح روش اجرای باربرداری تایید نشود، هیچ عملیات سنگین مجاز نیست.")
            ),
            listOf("submit a method statement", "approved with comments", "RAMS (Risk Assessment & Method Statement)"),
            listOf("work procedure", "execution plan"), listOf("-"),
            listOf("-"),
            "Very common on international FIDIC sites; UK/EU often combine it as RAMS.",
            "UK/International: 'Method Statement'; US: often 'Work Plan' or 'Activity Hazard Analysis (AHA)'.",
            source = "English Pro PM Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d5_003", "variation order", "/ˌver.iˈeɪ.ʃən ˈɔːr.dɚ/", "/ˌveə.riˈeɪ.ʃən ˈɔː.də/", "noun", "C1", 43,
            "D5 FIDIC Contracts", "FIDIC", "FORMAL",
            "دستور تغییر کار (در قراردادهای فیدیک)",
            "A written instruction under a FIDIC-style contract modifying the scope, quantity, or quality of the works.",
            listOf(
                VocabExample("Relocating the 800 mm outfall pipe requires a formal variation order signed by the Engineer.", "جابه‌جایی خط خروجی ۸۰۰ میلی‌متری نیازمند دستور تغییر کار رسمی با امضای مهندس مشاور است.")
            ),
            listOf("issue a variation order", "cost and time impact", "FIDIC Clause 13"),
            listOf("change order (US)", "contract amendment"), listOf("original scope"),
            listOf("vary (v)", "variation (n)"),
            "In FIDIC terminology, the Consultant/Supervisor is capitalized as 'The Engineer' and the owner is 'The Employer'.",
            "FIDIC / UK / International: 'Variation Order (VO)'; US / AIA: 'Change Order (CO)'.",
            source = "FIDIC Standard Terminology", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d5_004", "critical path", "/ˈkrɪt̬.ɪ.kəl pæθ/", "/ˈkrɪt.ɪ.kəl pɑːθ/", "noun", "B2", 44,
            "D5 PM & Planning", "PROJECT_MANAGEMENT", "TECHNICAL",
            "مسیر بحرانی پروژه",
            "The longest sequence of dependent activities that determines the minimum total duration of a project.",
            listOf(
                VocabExample("A delay in the substation switchgear delivery directly impacts the critical path.", "تاخیر در تحویل تابلوبرق پست مستقیماً روی مسیر بحرانی اثر می‌گذارد.")
            ),
            listOf("critical path method (CPM)", "zero float", "baseline programme"),
            listOf("driving sequence"), listOf("non-critical activity with float"),
            listOf("critical (adj)"),
            "Never say 'critical road' or 'critical way' — always 'critical path'.",
            "UK/FIDIC says 'Baseline Programme' (note spelling: programme); US says 'Baseline Schedule'.",
            source = "English Pro PM Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d5_005", "inspection request", "/ɪnˈspek.ʃən rɪˈkwest/", "/ɪnˈspek.ʃən rɪˈkwest/", "noun", "B2", 45,
            "D5 PM & QA/QC", "PROJECT_MANAGEMENT", "TECHNICAL",
            "درخواست بازرسی کار (WIR / IR)",
            "A formal notice submitted by the contractor inviting the supervision engineer to inspect completed work before covering or pouring.",
            listOf(
                VocabExample("The contractor submitted the Work Inspection Request only two hours before the planned pour.", "پیمانکار درخواست بازرسی کار را تنها دو ساعت قبل از زمان بتن‌ریزی ارسال کرد.")
            ),
            listOf("submit a WIR", "hold point", "witness point", "sign off an IR"),
            listOf("WIR", "RFI (Request for Inspection)"), listOf("-"),
            listOf("inspect (v)", "inspector (n)"),
            "Don't confuse RFI ('Request for Information' — design clarification) with WIR/IR ('Work Inspection Request').",
            source = "English Pro PM Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d5_006", "liquidated damages", "/ˈlɪk.wɪ.deɪ.t̬ɪd ˈdæm.ɪ.dʒɪz/", "/ˈlɪk.wɪ.deɪ.tɪd ˈdæm.ɪ.dʒɪz/", "noun", "C1", 46,
            "D5 FIDIC Contracts", "FIDIC", "FORMAL",
            "خسارت تاخیر قراردادی ازپیش‌تعیین‌شده",
            "Pre-agreed daily or weekly monetary compensation payable by a contractor for unexcused delay beyond the Time for Completion.",
            listOf(
                VocabExample("Unless the contractor substantiates the Extension of Time claim, liquidated damages will apply.", "مگر اینکه پیمانکار لایحه تمدید مدت را مستند کند، در غیر این صورت جریمه تاخیر اعمال می‌شود.")
            ),
            listOf("apply liquidated damages", "Extension of Time (EoT)", "Time for Completion"),
            listOf("delay damages (FIDIC Red Book)"), listOf("bonus for early completion"),
            listOf("-"),
            "FIDIC 1999/2017 officially uses 'Delay Damages' rather than 'penalty' (penalties are often unenforceable under common law).",
            "FIDIC: 'Delay Damages'; US contracts: 'Liquidated Damages (LDs)'.",
            source = "FIDIC Standard Terminology", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d5_007", "as-built drawings", "/æz bɪlt ˈdrɔː.ɪŋz/", "/æz bɪlt ˈdrɔː.ɪŋz/", "noun", "B2", 47,
            "D5 PM & Handover", "PROJECT_MANAGEMENT", "TECHNICAL",
            "نقشه‌های چون‌ساخت (ازبیلت)",
            "Final revised drawings reflecting every modification made during actual construction, delivered at project handover.",
            listOf(
                VocabExample("Taking-Over Certificate issuance is contingent upon approved as-built drawings.", "صدور گواهی تحویل موقت مشروط به تایید نقشه‌های چون‌ساخت است.")
            ),
            listOf("red-line markups", "submit as-builts", "O&M manuals"),
            listOf("record drawings"), listOf("issued-for-construction (IFC) drawings"),
            listOf("-"),
            "Hyphenated when before a noun ('as-built drawings'); often plural 'as-builts'.",
            source = "English Pro PM Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d5_008", "snag list", "/snæɡ lɪst/", "/snæɡ lɪst/", "noun", "B2", 48,
            "D5 PM & Handover", "PROJECT_MANAGEMENT", "TECHNICAL",
            "فهرست نواقص و ایرادات پیش از تحویل",
            "A checklist of minor defects or incomplete items compiled before final handover that the contractor must rectify.",
            listOf(
                VocabExample("We completed the joint walkdown and issued a 40-item snag list for the administration building.", "بازدید مشترک را انجام دادیم و یک فهرست نواقص ۴۰ موردی برای ساختمان اداری صادر کردیم.")
            ),
            listOf("snagging inspection", "close out punch-list items", "defects liability period"),
            listOf("punch list (US)", "defects list"), listOf("snag-free handover"),
            listOf("snagging (n)"),
            "Very high-frequency word in Middle East & European international projects!",
            "UK / Gulf / International: 'Snag list'; US: 'Punch list'.",
            source = "English Pro PM Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),

        // --- D6: CONSTRUCTION SAFETY ---
        w(
            "d6_001", "confined space", "/kənˈfaɪnd speɪs/", "/kənˈfaɪnd speɪs/", "noun", "B2", 49,
            "D6 Construction Safety", "SAFETY", "TECHNICAL",
            "فضای بسته و محدود (مانند منهول، مخزن، تونل)",
            "An enclosed area with limited entry/exit and potential atmospheric hazards (e.g., manholes, wet wells, digesters).",
            listOf(
                VocabExample("Entering the pump-station wet well requires a confined-space permit and continuous gas monitoring.", "ورود به چاه تر ایستگاه پمپاژ نیازمند مجوز فضای بسته و پایش مداوم گاز است.")
            ),
            listOf("confined-space entry permit", "gas detector", "standby attendant"),
            listOf("enclosed hazardous space"), listOf("open-air work area"),
            listOf("confine (v)"),
            "Crucial in water/wastewater projects due to H2S and oxygen deficiency risks.",
            source = "OSHA 1926 Subpart AA", license = "Public Domain", pkgId = "pkg_safety", now = now
        ),
        w(
            "d6_002", "toolbox talk", "/ˈtuːl.bɑːks tɔːk/", "/ˈtuːl.bɒks tɔːk/", "noun", "B2", 50,
            "D6 Construction Safety", "SAFETY", "NEUTRAL",
            "جلسه کوتاه ایمنی پیش از شروع کار (تول‌باکس)",
            "A brief 5–10 minute pre-shift safety briefing held on site focusing on the specific hazards of the day's task.",
            listOf(
                VocabExample("The HSE officer conducted a toolbox talk on crane swing radius before lifting the precast beams.", "افسر ایمنی پیش از باربرداری تیرهای پیش‌ساخته، جلسه کوتاه ایمنی درباره شعاع چرخش جرثقیل برگزار کرد.")
            ),
            listOf("hold a toolbox talk", "pre-shift briefing", "sign the attendance sheet"),
            listOf("safety huddle", "tailgate briefing (US)"), listOf("-"),
            listOf("-"),
            "Say 'hold/conduct a toolbox talk', not 'make a toolbox talk'.",
            "UK/International: 'Toolbox talk'; US: 'Toolbox talk' or 'Tailgate meeting'.",
            source = "OSHA Safety Resources", license = "Public Domain", pkgId = "pkg_safety", now = now
        ),
        w(
            "d6_003", "near miss", "/nɪr mɪs/", "/nɪə mɪs/", "noun", "B2", 51,
            "D6 Construction Safety", "SAFETY", "TECHNICAL",
            "حادثه نزدیک به وقوع / شبه‌حادثه",
            "An unplanned event that did not result in injury or damage, but had the clear potential to do so.",
            listOf(
                VocabExample("A dropped wrench from level 4 was logged as a near miss and triggered mandatory tool tethering.", "سقوط آچار از طبقه چهارم به‌عنوان شبه‌حادثه ثبت شد و بستن بند ایمنی ابزار را اجباری کرد.")
            ),
            listOf("report a near miss", "near-miss investigation", "preventive measure"),
            listOf("close call"), listOf("lost-time injury (LTI)"),
            listOf("-"),
            "Even without injury, international HSE standards require formal near-miss reporting.",
            source = "OSHA Safety Resources", license = "Public Domain", pkgId = "pkg_safety", now = now
        ),
        w(
            "d6_004", "working at height", "/ˈwɝː.kɪŋ æt haɪt/", "/ˈwɜː.kɪŋ æt haɪt/", "noun phrase", "B2", 52,
            "D6 Construction Safety", "SAFETY", "TECHNICAL",
            "کار در ارتفاع",
            "Any work in a place where a person could fall a distance liable to cause personal injury.",
            listOf(
                VocabExample("Full-body harnesses with double lanyards are mandatory when working at height above 1.8 meters.", "استفاده از هارنس تمام‌بدنه با لنیارد دوتایی هنگام کار در ارتفاع بالای ۱.۸ متر الزامی است.")
            ),
            listOf("fall arrest system", "edge protection", "anchor point", "safety harness"),
            listOf("elevated work"), listOf("ground-level work"),
            listOf("-"),
            "Use singular 'height', NOT 'working at heights'.",
            "UK HSE uses 'Working at height'; US OSHA uses 'Fall protection / elevated work'.",
            source = "OSHA / HSE", license = "Public Domain", pkgId = "pkg_safety", now = now
        ),

        // --- D7: PROFESSIONAL MEETINGS & COMMUNICATION ---
        w(
            "d7_001", "escalate", "/ˈes.kə.leɪt/", "/ˈes.kə.leɪt/", "verb", "B2", 53,
            "D7 Meetings & Leadership", "MEETINGS", "FORMAL",
            "ارجاع موضوع حل‌نشده به سطح مدیریت بالاتر",
            "To raise an unresolved technical, commercial, or safety issue to higher authority for action.",
            listOf(
                VocabExample("If the contractor ignores this second notice, we will escalate the matter to the Project Director.", "اگر پیمانکار این اخطار دوم را نادیده بگیرد، موضوع را به مدیر پروژه ارجاع می‌دهیم.")
            ),
            listOf("escalate an issue", "escalation matrix", "escalate to senior management"),
            listOf("refer upward", "elevate"), listOf("resolve at site level"),
            listOf("escalation (n)"),
            "In meetings, 'escalate' means refer to higher management—distinct from 'conflict escalating' (getting worse).",
            source = "English Pro Meetings Corpus", license = "CC0 1.0", pkgId = "pkg_site_dialogues", now = now
        ),
        w(
            "d7_002", "push back on", "/pʊʃ bæk ɑːn/", "/pʊʃ bæk ɒn/", "phrasal verb", "C1", 54,
            "D7 Meetings & Leadership", "MEETINGS", "NEUTRAL",
            "مخالفت حرفه‌ای و مستدل کردن با یک پیشنهاد یا ادعا",
            "To resist, challenge, or oppose a proposal, schedule, or claim constructively.",
            listOf(
                VocabExample("As the Consultant, we need to push back on their unsubstantiated three-week delay claim.", "به‌عنوان مشاور، باید در برابر ادعای تاخیر سه‌هفته‌ای بدون مستندات آن‌ها ایستادگی کنیم.")
            ),
            listOf("push back on a claim", "push back against a deadline"),
            listOf("challenge", "object to", "contest"), listOf("concede", "accept"),
            listOf("pushback (n)"),
            "Very common and natural in international engineering & management meetings.",
            source = "English Pro Meetings Corpus", license = "CC0 1.0", pkgId = "pkg_site_dialogues", now = now
        ),
        w(
            "d7_003", "on track", "/ɑːn træk/", "/ɒn træk/", "idiom", "B2", 55,
            "D7 Meetings & Progress", "MEETINGS", "NEUTRAL",
            "طبق برنامه پیش رفتن",
            "Making progress as planned and likely to achieve a target or deadline.",
            listOf(
                VocabExample("Despite last week's rain, foundation works for Building B are back on track.", "با وجود بارندگی هفته گذشته، عملیات فونداسیون ساختمان B دوباره روی ریل برنامه است.")
            ),
            listOf("stay on track", "get back on track", "off track"),
            listOf("on schedule", "proceeding as planned"), listOf("behind schedule"),
            listOf("-"),
            "Great conversational alternative to stiff 'according to the time schedule'.",
            source = "English Pro Meetings Corpus", license = "CC0 1.0", pkgId = "pkg_site_dialogues", now = now
        ),
        w(
            "d7_004", "iron out", "/ˈaɪ.ɚn aʊt/", "/ˈaɪ.ən aʊt/", "phrasal verb", "C1", 56,
            "D7 Meetings & Coordination", "MEETINGS", "NEUTRAL",
            "رفع و رجوع کردن اختلافات یا مشکلات جزئی",
            "To resolve small remaining problems, disagreements, or technical clashes smoothly.",
            listOf(
                VocabExample("Let's hold a coordination meeting tomorrow to iron out the pipe-sleeve clashes with the MEP team.", "بیایید فردا یک جلسه هماهنگی بگذاریم تا تداخل غلاف لوله‌ها با تیم تأسیسات را برطرف کنیم.")
            ),
            listOf("iron out details", "iron out clashes", "iron out differences"),
            listOf("resolve", "sort out", "smooth over"), listOf("complicate"),
            listOf("-"),
            "Natural spoken English; note pronunciation 'EYE-ern out' (silent 'r' in UK).",
            source = "English Pro Meetings Corpus", license = "CC0 1.0", pkgId = "pkg_site_dialogues", now = now
        ),
        w(
            "d7_005", "walk someone through", "/wɔːk θruː/", "/wɔːk θruː/", "phrasal verb", "B2", 57,
            "D7 Meetings & Presentations", "MEETINGS", "NEUTRAL",
            "گام‌به‌گام توضیح دادن برای کسی",
            "To guide someone step by step through a drawing, report, process, or calculation.",
            listOf(
                VocabExample("Could you walk us through the revised critical path on slide four?", "ممکن است مسیر بحرانی اصلاح‌شده در اسلاید چهار را گام‌به‌گام برایمان توضیح دهید؟")
            ),
            listOf("walk through a drawing", "walk through the sequence"),
            listOf("explain step by step", "guide through"), listOf("skip over"),
            listOf("walk-through / walkdown (n)"),
            "Much more natural than 'explain to us step by step' in live meetings.",
            source = "English Pro Meetings Corpus", license = "CC0 1.0", pkgId = "pkg_site_dialogues", now = now
        ),
        w(
            "d7_006", "substantiate", "/səbˈstæn.ʃi.eɪt/", "/səbˈstæn.ʃi.eɪt/", "verb", "C1", 58,
            "D7 Claims & Supervision", "MEETINGS", "FORMAL",
            "با مدارک و شواهد مستند اثبات کردن",
            "To provide solid evidence, records, or data to prove the truth of a claim or statement.",
            listOf(
                VocabExample("The contractor must substantiate the weather delay with daily site logs and meteorological data.", "پیمانکار باید تاخیر جوی را با گزارش‌های روزانه کارگاه و داده‌های هواشناسی مستند کند.")
            ),
            listOf("substantiate a claim", "well-substantiated", "unsubstantiated"),
            listOf("corroborate", "back up", "document"), listOf("disprove", "contradict"),
            listOf("substantiation (n)"),
            "Essential formal verb in FIDIC claims; conversational equivalent is 'back up with evidence'.",
            source = "FIDIC / Engineering Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        ),
        w(
            "d7_007", "flag", "/flæɡ/", "/flæɡ/", "verb", "B2", 59,
            "D7 Meetings & Risk", "MEETINGS", "NEUTRAL",
            "هشدار دادن / برجسته کردن یک ریسک یا مشکل",
            "To draw attention to a potential issue, risk, or discrepancy early so it can be addressed.",
            listOf(
                VocabExample("I want to flag a potential clash between the storm drain and the substation foundation.", "می‌خواهم یک تداخل احتمالی بین خط آب سطحی و فونداسیون پست برق را از الان یادآوری و برجسته کنم.")
            ),
            listOf("flag an issue early", "flag a risk", "red flag"),
            listOf("highlight", "point out", "signal"), listOf("conceal"),
            listOf("flagged (adj)"),
            "Natural workplace verb — concise and confident.",
            source = "English Pro Meetings Corpus", license = "CC0 1.0", pkgId = "pkg_site_dialogues", now = now
        ),
        w(
            "d7_008", "contingency", "/kənˈtɪn.dʒən.si/", "/kənˈtɪn.dʒən.si/", "noun", "C1", 60,
            "D7 Planning & Risk", "MEETINGS", "FORMAL",
            "برنامه یا بودجه احتیاطی برای شرایط پیش‌بینی‌نشده",
            "A reserve of time, money, or an alternative plan prepared for unexpected emergencies or risks.",
            listOf(
                VocabExample("What is our contingency plan if the concrete pump breaks down mid-pour?", "اگر پمپ بتن وسط بتن‌ریزی خراب شود، برنامه جایگزین ما چیست؟")
            ),
            listOf("contingency plan", "contingency reserve", "budget contingency"),
            listOf("fallback plan", "buffer"), listOf("-"),
            listOf("contingent (adj)"),
            "Use 'contingency plan' rather than 'emergency plan' when referring to schedule/logistics backups.",
            source = "English Pro PM Corpus", license = "CC0 1.0", pkgId = "pkg_pm_english", now = now
        )
    )

    /**
     * Generates domain-authentic expansion entries when the learner downloads/installs
     * an offline content package from the Package Manager.
     */
    fun getExpansionWordsForPackage(packageId: String, now: Long): List<VocabWordEntity> {
        return when (packageId) {
            "pkg_civil_vocab" -> listOf(
                w("exp_civ_1", "blinding concrete", "/ˈblaɪn.dɪŋ/", "/ˈblaɪn.dɪŋ/", "noun", "B2", 101, "Foundation", "CIVIL_SITE", "TECHNICAL", "بتن مگر (نظافت کف گود)", "A thin layer of weak concrete poured over soil to provide a clean, level base for reinforcement.", listOf(VocabExample("Pour 75 mm of blinding concrete immediately after soil approval.", "بلافاصله پس از تایید بستر خاک، ۷۵ میلی‌متر بتن مگر بریزید.")), listOf("lean concrete", "mud slab (US)"), listOf("blinding layer"), listOf("-"), listOf("-"), "Uncountable: 'blinding concrete'.", "UK: 'blinding'; US: 'mud slab' or 'rat slab'.", "USACE / BS", "CC0 1.0", packageId, now),
                w("exp_civ_2", "waterstop", "/ˈwɔː.t̬ɚ.stɑːp/", "/ˈwɔː.tə.stɒp/", "noun", "C1", 102, "Waterproofing", "CONCRETE_STRUCTURAL", "TECHNICAL", "واتراستاپ (نوار آب‌بند درز بتن)", "A PVC, rubber, or hydrophilic strip embedded across concrete joints to prevent water seepage.", listOf(VocabExample("Ensure the center-bulb waterstop is securely tied so it won't fold during vibration.", "مطمئن شوید واتراستاپ حباب‌دار محکم بسته شده تا حین ویبره تا نخورد.")), listOf("PVC waterstop", "hydrophilic strip"), listOf("water bar"), listOf("-"), listOf("-"), "Specify 'internal' vs 'external' waterstop.", "UK also uses 'water bar'.", "USACE", "Public Domain", packageId, now),
                w("exp_civ_3", "pile integrity test", "/paɪl ɪnˈteɡ.rə.t̬i test/", "/paɪl ɪnˈteɡ.rə.ti test/", "noun", "C1", 103, "Foundations", "GEOTECHNICAL", "TECHNICAL", "آزمایش سلامت شمع (PIT)", "Low-strain sonic echo or cross-hole ultrasonic testing to detect necking, voids, or cracks in cast-in-situ piles.", listOf(VocabExample("All bored piles must pass low-strain pile integrity testing prior to pile-cap excavation.", "همه شمع‌های درجا باید پیش از خاک‌برداری سرشمع آزمایش سلامت شمع را پاس کنند.")), listOf("PIT", "cross-hole sonic logging (CSL)"), listOf("sonic echo test"), listOf("-"), listOf("-"), "Pronounce 'integrity' with stress on '-TEG-'.", "", "FHWA / USACE", "Public Domain", packageId, now),
                w("exp_civ_4", "grouting", "/ˈɡraʊ.t̬ɪŋ/", "/ˈɡraʊ.tɪŋ/", "noun", "B2", 104, "Structural & Geotech", "GEOTECHNICAL", "TECHNICAL", "تزریق دوغاب / گروت‌ریزی", "Injecting fluid cementitious or chemical mix into voids, micropiles, rock fissures, or under baseplates.", listOf(VocabExample("Non-shrink cementitious grout was poured beneath the crane rail baseplates.", "گروت سیمانی بدون انقباض زیر بیس‌پلیت‌های ریل جرثقیل ریخته شد.")), listOf("non-shrink grout", "pressure grouting", "grout cube test"), listOf("slurry injection"), listOf("-"), listOf("grout (n/v)"), "Don't confuse structural 'grout' (between plates/tiles) with 'mortar' (between bricks).", "", "USACE", "Public Domain", packageId, now)
            )
            "pkg_wastewater" -> listOf(
                w("exp_ww_1", "headworks", "/ˈhed.wɝːks/", "/ˈhed.wɜːks/", "noun", "C1", 105, "Wastewater", "WATER_WASTEWATER", "TECHNICAL", "تاسیسات ورودی تصفیه‌خانه (آشغال‌گیر و دانه‌گیر)", "The initial treatment stage of a wastewater plant including coarse screens, fine screens, and grit chambers.", listOf(VocabExample("Mechanical bar screens at the headworks remove large debris before raw sewage reaches the lift pumps.", "آشغال‌گیرهای مکانیکی در هدورکس زباله‌های درشت را پیش از رسیدن به پمپ‌های بالابر جدا می‌کنند.")), listOf("grit chamber", "mechanical bar screen"), listOf("inlet works (UK)"), listOf("effluent outfall"), listOf("-"), "Always plural in form ('headworks'), takes singular or plural verb.", "US: 'headworks'; UK: 'inlet works'.", "EPA", "Public Domain", packageId, now),
                w("exp_ww_2", "effluent", "/ˈef.lu.ənt/", "/ˈef.lu.ənt/", "noun", "C1", 106, "Wastewater", "WATER_WASTEWATER", "TECHNICAL", "پساب تصفیه‌شده خروجی", "Treated or partially treated liquid wastewater flowing out of a treatment plant or basin.", listOf(VocabExample("Final effluent turbidity and BOD5 met the agricultural reuse standard.", "کدورت و BOD5 پساب نهایی با استاندارد بازچرخانی کشاورزی مطابقت داشت.")), listOf("treated effluent", "effluent discharge standards"), listOf("outflow"), listOf("influent"), listOf("-"), "Contrast 'influent' (فاضلاب ورودی) vs 'effluent' (پساب خروجی).", "", "EPA", "Public Domain", packageId, now),
                w("exp_ww_3", "baffle wall", "/ˈbæf.əl wɔːl/", "/ˈbæf.əl wɔːl/", "noun", "C1", 107, "Hydraulics", "WATER_WASTEWATER", "TECHNICAL", "دیواره بافل / هدایت‌کننده جریان", "An internal wall or partition in a tank designed to direct water flow, prevent short-circuiting, or retain scum.", listOf(VocabExample("Installing baffle walls in the chlorine contact tank ensures the required hydraulic retention time.", "نصب دیواره‌های بافل در مخزن تماس کلر زمان ماند هیدرولیکی مورد نیاز را تضمین می‌کند.")), listOf("scum baffle", "short-circuiting"), listOf("flow deflector"), listOf("-"), listOf("-"), "Pronounced 'BAF-əl'.", "", "USACE / EPA", "Public Domain", packageId, now)
            )
            "pkg_pm_english" -> listOf(
                w("exp_pm_1", "notice of delay", "/ˈnoʊ.t̬ɪs əv dɪˈleɪ/", "/ˈnəʊ.tɪs əv dɪˈleɪ/", "noun", "C1", 108, "FIDIC", "FIDIC", "FORMAL", "اعلامیه تاخیر قراردادی", "A formal contractual notice required within a strict time-bar (e.g., 28 days in FIDIC) when an employer-risk delay occurs.", listOf(VocabExample("Failure to serve a notice of delay within 28 days may time-bar the contractor's claim under Sub-Clause 20.1.", "عدم ارسال اعلامیه تاخیر ظرف ۲۸ روز ممکن است ادعای پیمانکار را طبق بند ۲۰.۱ مشمول مرور زمان کند.")), listOf("time-bar clause", "contemporaneous records"), listOf("delay notification"), listOf("-"), listOf("-"), "Use verb 'serve' or 'issue' a notice.", "", "FIDIC Reference", "CC0 1.0", packageId, now),
                w("exp_pm_2", "material submittal", "/məˈtɪr.i.əl səbˈmɪt̬.əl/", "/məˈtɪə.ri.əl səbˈmɪt.əl/", "noun", "B2", 109, "QA/QC", "PROJECT_MANAGEMENT", "TECHNICAL", "مدارک پیشنهادی مصالح (تاییدیه متریال)", "A formal package of datasheets, test certificates, and samples submitted for consultant approval before procurement.", listOf(VocabExample("The waterproofing material submittal was returned 'Revise and Resubmit' due to missing UV resistance certificates.", "مدارک متریال عایق رطوبتی به‌دلیل نبود گواهی مقاومت UV با وضعیت اصلاح و ارسال مجدد برگشت داده شد.")), listOf("MAR (Material Approval Request)", "Revise and Resubmit (Code C)"), listOf("technical submittal"), listOf("-"), listOf("submit (v)"), "Common approval codes: A (Approved), B (Approved with Comments), C (Revise & Resubmit), D (Rejected).", "", "English Pro PM", "CC0 1.0", packageId, now)
            )
            "pkg_safety" -> listOf(
                w("exp_saf_1", "lockout tagout", "/ˈlɑːk.aʊt ˈtæɡ.aʊt/", "/ˈlɒk.aʊt ˈtæɡ.aʊt/", "noun", "C1", 110, "M&E Safety", "SAFETY", "TECHNICAL", "قفل‌گذاری و برچسب‌گذاری ایمنی انرژی (LOTO)", "Safety procedure ensuring hazardous electrical, mechanical, or hydraulic equipment is properly isolated and cannot start during maintenance.", listOf(VocabExample("Strict lockout-tagout procedures apply before opening the blower MCC panel.", "پیش از باز کردن تابلو برق بلوئرها، دستورالعمل دقیق LOTO الزامی است.")), listOf("LOTO", "energy isolation", "zero-energy state"), listOf("isolation procedure"), listOf("-"), listOf("-"), "Critical during M&E commissioning of treatment plants.", "", "OSHA 1910.147", "Public Domain", packageId, now),
                w("exp_saf_2", "lifting plan", "/ˈlɪf.tɪŋ plæn/", "/ˈlɪf.tɪŋ plæn/", "noun", "B2", 111, "Crane Operations", "SAFETY", "TECHNICAL", "طرح و نقشه باربرداری ایمن", "An engineered plan specifying crane capacity, outrigger loads, sling angles, wind limits, and exclusion zones.", listOf(VocabExample("Tandem lifts of the 18-ton precast girders require a critical lifting plan signed by an appointed person.", "باربرداری دوگانه تیرهای پیش‌ساخته ۱۸ تنی نیازمند طرح باربرداری بحرانی با امضای شخص ذی‌صلاح است.")), listOf("critical lift", "outrigger pad", "banksman / rigger"), listOf("rigging plan"), listOf("-"), listOf("-"), "UK uses 'banksman' or 'slinger-signaller'; US uses 'rigger / signalperson'.", "UK: 'banksman'; US: 'signalperson'.", "OSHA / LOLER", "Public Domain", packageId, now)
            )
            else -> listOf(
                w("exp_gen_${packageId}_1", "on the same page", "/ɑːn ðə seɪm peɪdʒ/", "/ɒn ðə seɪm peɪdʒ/", "idiom", "B2", 112, "Workplace", "GENERAL", "NEUTRAL", "هم‌نظر و هماهنگ بودن", "Having a shared understanding of a plan, requirement, or next steps.", listOf(VocabExample("Let's do a quick five-minute recap so everyone is on the same page before leaving site.", "بیایید یک جمع‌بندی سریع پنج‌دقیقه‌ای داشته باشیم تا همه قبل از ترک کارگاه هماهنگ باشیم.")), listOf("get everyone on the same page"), listOf("aligned", "in agreement"), listOf("at cross purposes"), listOf("-"), "Natural idiomatic spoken English.", "", "Tatoeba CC0", "CC0 1.0", packageId, now),
                w("exp_gen_${packageId}_2", "touch base", "/tʌtʃ beɪs/", "/tʌtʃ beɪs/", "idiom", "B2", 113, "Communication", "GENERAL", "INFORMAL", "تماس کوتاه گرفتن برای هماهنگی یا اطلاع", "To briefly make contact with someone to check progress or update them.", listOf(VocabExample("I'll touch base with the structural designer this afternoon and call you back.", "امروز بعدازظهر با طراح سازه هماهنگ می‌کنم و با شما تماس می‌گیرم.")), listOf("touch base tomorrow", "touch base briefly"), listOf("check in", "follow up briefly"), listOf("-"), listOf("-"), "Very common in spoken workplace English; avoid in formal contractual letters.", "", "English Pro Core", "CC0 1.0", packageId, now)
            )
        }
    }
}
