package com.example.ai

import android.util.Log
import com.example.data.local.RolePlayScenarioEntity
import com.example.data.local.TurnCorrection
import com.example.data.local.VocabExample
import com.example.data.local.VocabWordEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject

private const val TAG = "AiSpeakingCoachService"

@Serializable
data class CoachSessionReport(
    val fluencyScore: Int,
    val vocabRangeScore: Int,
    val grammarScore: Int,
    val complexityScore: Int,
    val pronunciationScore: Int,
    val spontaneousScore: Int,
    val hesitationNotes: String,
    val feedbackSummary: String,
    val recommendations: List<String>
)

data class CoachReplyResult(
    val englishReply: String,
    val correction: TurnCorrection,
    val persianHint: String = "",
    val isFromOnlineAi: Boolean = true
)

class AiSpeakingCoachService {

    private val json = Json { ignoreUnknownKeys = true }

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val correctionSchema = Schema.obj(
        properties = mapOf(
            "englishReply" to Schema.string(description = "Natural conversational coach reply in contemporary English (2-4 sentences, ending with a relevant follow-up question)."),
            "category" to Schema.enumeration(listOf("INCORRECT", "UNNATURAL", "NATURAL"), description = "Classification of learner utterance"),
            "correctedSentence" to Schema.string(description = "Corrected English sentence if needed, or empty if natural"),
            "naturalAlternative" to Schema.string(description = "More natural native-like phrasing"),
            "explanation" to Schema.string(description = "Short constructive coaching explanation"),
            "persianNote" to Schema.string(description = "Brief Persian clarification only if essential or requested")
        ),
        optionalProperties = listOf("correctedSentence", "naturalAlternative", "persianNote")
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = correctionSchema
            }
        )
    }

    private val reportSchema = Schema.obj(
        properties = mapOf(
            "fluencyScore" to Schema.integer(description = "0-100 fluency score"),
            "vocabRangeScore" to Schema.integer(description = "0-100 vocabulary range score"),
            "grammarScore" to Schema.integer(description = "0-100 grammatical accuracy score"),
            "complexityScore" to Schema.integer(description = "0-100 sentence complexity score"),
            "pronunciationScore" to Schema.integer(description = "0-100 estimated intelligibility from transcript"),
            "spontaneousScore" to Schema.integer(description = "0-100 spontaneous responsiveness"),
            "hesitationNotes" to Schema.string(description = "Observation on pauses, short answers, or hesitation"),
            "feedbackSummary" to Schema.string(description = "Concise 2-sentence coach summary"),
            "recommendations" to Schema.array(items = Schema.string(), description = "3 to 5 specific improvement actions")
        )
    )

    private val reportModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = reportSchema
            }
        )
    }

    fun streamCoachText(
        userMessage: String,
        historyPairs: List<Pair<String, String>>,
        difficulty: String,
        correctionMode: String,
        targetWords: List<String>,
        scenario: RolePlayScenarioEntity? = null
    ): Flow<String> = flow {
        val systemInstruction = buildCoachPrompt(
            userMessage, historyPairs, difficulty, correctionMode, targetWords, scenario, structured = false
        )
        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            chatModel.generateContentStream(systemInstruction).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }
    }

    suspend fun generateStructuredCoachReply(
        userMessage: String,
        historyPairs: List<Pair<String, String>>,
        difficulty: String,
        correctionMode: String,
        targetWords: List<String>,
        scenario: RolePlayScenarioEntity? = null
    ): CoachReplyResult {
        val prompt = buildCoachPrompt(
            userMessage, historyPairs, difficulty, correctionMode, targetWords, scenario, structured = true
        )
        val response = withTimeout(60_000L) {
            structuredModel.generateContent(prompt)
        }
        val raw = response.text ?: error("Gemini returned an empty response.")
        val obj = JSONObject(raw)
        return CoachReplyResult(
            englishReply = obj.getString("englishReply"),
            correction = TurnCorrection(
                category = obj.optString("category", "NATURAL"),
                learnerSaid = userMessage,
                correctedSentence = obj.optString("correctedSentence", ""),
                naturalAlternative = obj.optString("naturalAlternative", ""),
                explanation = obj.optString("explanation", ""),
                persianNote = obj.optString("persianNote", "")
            ),
            isFromOnlineAi = true
        )
    }

    suspend fun translateOrExplainInPersian(englishText: String, explainExpressionOnly: Boolean): String {
        val prompt = if (explainExpressionOnly) {
            "Explain the meaning, register, and civil engineering/workplace usage of this English text briefly in Persian with 1 short example:\n\"$englishText\""
        } else {
            "Translate this natural English sentence accurately into clear, modern Persian:\n\"$englishText\""
        }
        val response = withTimeout(45_000L) {
            chatModel.generateContent(prompt)
        }
        return response.text?.trim().orEmpty().ifBlank { "Translation unavailable." }
    }

    suspend fun generateSessionReport(
        transcriptLines: List<String>,
        targetWords: List<String>
    ): CoachSessionReport {
        val prompt = """
            Evaluate this English speaking practice transcript by an adult civil engineer (target B2 -> C1).
            Target vocabulary to encourage: ${targetWords.joinToString(", ")}.
            Transcript:
            ${transcriptLines.joinToString("\n")}
            
            Score fairly (0-100) without falsely penalizing natural spoken English. Provide 3-5 concrete recommendations.
        """.trimIndent()
        val response = withTimeout(60_000L) {
            reportModel.generateContent(prompt)
        }
        val raw = response.text ?: error("Empty report response")
        val obj = JSONObject(raw)
        val recsArray = obj.getJSONArray("recommendations")
        val recs = (0 until recsArray.length()).map { recsArray.getString(it) }
        return CoachSessionReport(
            fluencyScore = obj.getInt("fluencyScore").coerceIn(0, 100),
            vocabRangeScore = obj.getInt("vocabRangeScore").coerceIn(0, 100),
            grammarScore = obj.getInt("grammarScore").coerceIn(0, 100),
            complexityScore = obj.getInt("complexityScore").coerceIn(0, 100),
            pronunciationScore = obj.getInt("pronunciationScore").coerceIn(0, 100),
            spontaneousScore = obj.getInt("spontaneousScore").coerceIn(0, 100),
            hesitationNotes = obj.getString("hesitationNotes"),
            feedbackSummary = obj.getString("feedbackSummary"),
            recommendations = recs
        )
    }

    private fun buildCoachPrompt(
        userMessage: String,
        historyPairs: List<Pair<String, String>>,
        difficulty: String,
        correctionMode: String,
        targetWords: List<String>,
        scenario: RolePlayScenarioEntity?,
        structured: Boolean
    ): String {
        val historyBlock = historyPairs.takeLast(8).joinToString("\n") { (speaker, msg) ->
            "$speaker: $msg"
        }
        val roleBlock = if (scenario != null) {
            "Role-Play Character: ${scenario.aiRole}. Scenario: ${scenario.title} — ${scenario.background}. Objective for learner: ${scenario.objective}. Introduce realistic complication when appropriate: ${scenario.complicationPrompt}."
        } else {
            "Role: Patient, realistic executive English speaking coach for a senior civil engineer (30+ years experience in construction supervision, infrastructure, and wastewater treatment plants)."
        }

        val correctionRule = if (correctionMode == "ACCURACY") {
            "Accuracy Mode: Correct significant grammatical or technical phrasing errors concisely before continuing."
        } else {
            "Fluency Mode: Prioritize smooth communication first. Only flag high-impact mistakes gently without interrupting flow."
        }

        return """
            $roleBlock
            Target Level: $difficulty (B2 progressing toward C1). Never treat the learner as a beginner.
            $correctionRule
            Rules:
            - Speak primarily in natural contemporary English (do NOT translate everything to Persian).
            - Never label acceptable spoken English as incorrect just because a textbook form is stiffer.
            - Encourage longer answers and naturally weave in target words: ${targetWords.take(5).joinToString(", ")}.
            Recent turns:
            $historyBlock
            Learner just said: "$userMessage"
            ${if (structured) "Respond strictly in JSON matching the schema." else "Respond naturally as the coach/role-play partner."}
        """.trimIndent()
    }

    /**
     * Offline Fallback Coach when internet is unavailable or Firebase AI times out.
     * Ensures the learner can always continue practicing without blank screens or crashes.
     */
    fun generateOfflineFallbackReply(
        userMessage: String,
        turnIndex: Int,
        scenario: RolePlayScenarioEntity?,
        targetWords: List<String>
    ): CoachReplyResult {
        val lower = userMessage.lowercase()
        val hasPastMistake = lower.contains("did went") || lower.contains("yesterday i go") || lower.contains("equipments") || lower.contains("formworks")
        val correction = when {
            lower.contains("equipments") || lower.contains("formworks") -> TurnCorrection(
                category = "INCORRECT",
                learnerSaid = userMessage,
                correctedSentence = userMessage.replace("equipments", "equipment", true).replace("formworks", "formwork", true),
                naturalAlternative = "We mobilized all heavy equipment and formwork panels on schedule.",
                explanation = "'Equipment', 'formwork', 'rebar', and 'scaffolding' are uncountable in English.",
                persianNote = "واژگان equipment و formwork جمع بسته نمی‌شوند."
            )
            lower.contains("comply to") -> TurnCorrection(
                category = "INCORRECT",
                learnerSaid = userMessage,
                correctedSentence = userMessage.replace("comply to", "comply with", true),
                naturalAlternative = "The concrete mix complies with the C35 specification.",
                explanation = "Always use 'comply with' rather than 'comply to'.",
                persianNote = "حرف اضافه صحیح بعد از comply، کلمه with است."
            )
            hasPastMistake -> TurnCorrection(
                category = "INCORRECT",
                learnerSaid = userMessage,
                correctedSentence = "Yesterday we inspected the site and checked the rebar spacing.",
                naturalAlternative = "We completed the inspection yesterday before the pour started.",
                explanation = "Use Past Simple consistently when narrating completed site events.",
                persianNote = "برای روایت وقایع گذشته از زمان گذشته ساده استفاده کنید."
            )
            userMessage.split(" ").size < 6 -> TurnCorrection(
                category = "UNNATURAL",
                learnerSaid = userMessage,
                correctedSentence = userMessage,
                naturalAlternative = "Try expanding with cause and action: 'Because of..., we decided to...'",
                explanation = "Your point is clear, but adding one technical detail makes your answer sound more B2+/C1.",
                persianNote = "پاسخ درست است؛ برای روانی بیشتر یک دلیل یا جزئیات اجرایی اضافه کنید."
            )
            else -> TurnCorrection(
                category = "NATURAL",
                learnerSaid = userMessage,
                explanation = "Clear and natural communication. Keep developing your point!"
            )
        }

        val reply = if (scenario != null) {
            val prompts = listOf(
                "Good point. As the ${scenario.aiRole}, let me ask: ${scenario.complicationPrompt} How would you respond on site?",
                "I appreciate that clarity. What measurable verification or record will you require before signing off?",
                "Let's wrap up this scenario step: summarize your final decision and responsibility assignment in two sentences."
            )
            prompts[turnIndex % prompts.size]
        } else {
            val wordHint = targetWords.getOrNull(turnIndex % targetWords.size.coerceAtLeast(1)) ?: "mitigate"
            val followUps = listOf(
                "That makes sense—could you walk me through a concrete example from one of your wastewater or industrial projects?",
                "Interesting perspective. If a contractor pushes back on that point, how would you substantiate your position using '$wordHint'?",
                "Well explained. What contingency plan would you put in place to keep the critical path on track?"
            )
            followUps[turnIndex % followUps.size]
        }

        return CoachReplyResult(
            englishReply = reply,
            correction = correction,
            persianHint = "حالت آفلاین فعال است (مربی هوشمند محلی)",
            isFromOnlineAi = false
        )
    }

    fun generateOfflineSessionReport(
        userMessages: List<String>,
        targetWords: List<String>
    ): CoachSessionReport {
        val avgWords = if (userMessages.isEmpty()) 0 else userMessages.sumOf { it.split(Regex("\\s+")).size } / userMessages.size
        val fluency = (58 + (avgWords * 2).coerceAtMost(32)).coerceIn(45, 92)
        val usedTargetCount = targetWords.count { target ->
            userMessages.any { it.contains(target, ignoreCase = true) }
        }
        val vocabScore = (62 + usedTargetCount * 8).coerceIn(50, 94)
        return CoachSessionReport(
            fluencyScore = fluency,
            vocabRangeScore = vocabScore,
            grammarScore = 70,
            complexityScore = (60 + (avgWords * 1.5).toInt()).coerceIn(50, 90),
            pronunciationScore = 68,
            spontaneousScore = fluency,
            hesitationNotes = "Average response length: $avgWords words/turn (Offline local heuristic; connect online for Gemini deep analysis).",
            feedbackSummary = "Completed ${userMessages.size} spoken/written turns in offline mode with average $avgWords words per turn.",
            recommendations = listOf(
                "Aim for 18–30 words per speaking turn using connectors like 'however', 'given that', and 'as a result'.",
                "Reuse target civil vocabulary in full sentences: ${targetWords.take(3).joinToString(", ")}.",
                "Run a 5-minute shadowing drill at 0.8x speed on technical meeting dialogues."
            )
        )
    }

    private val sentenceSchema = Schema.obj(
        properties = mapOf(
            "english" to Schema.string(description = "One natural B2/C1 English sentence using the exact target word, suited for an experienced civil engineer or international workplace."),
            "persian" to Schema.string(description = "Clear Persian translation of the English sentence.")
        )
    )

    private val sentenceModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = sentenceSchema
            }
        )
    }

    suspend fun generateFlashcardSentence(
        word: VocabWordEntity,
        isOnline: Boolean,
        variantIndex: Int = 0
    ): VocabExample {
        if (isOnline) {
            try {
                val prompt = """
                    Write 1 fresh, practical B2/C1 English sentence using the word/phrase "${word.word}" (${word.partOfSpeech}, category: ${word.category}, meaning: ${word.englishDefinition}).
                    Audience: Senior civil engineer preparing for international infrastructure & site supervision work.
                    Avoid repeating generic dictionary definitions; make it realistic and spoken/professional, and provide its natural Persian translation.
                """.trimIndent()
                val response = withTimeout(25_000L) {
                    sentenceModel.generateContent(prompt)
                }
                val raw = response.text.orEmpty()
                if (raw.isNotBlank()) {
                    val obj = JSONObject(raw)
                    val en = obj.optString("english", "").trim()
                    val fa = obj.optString("persian", "").trim()
                    if (en.isNotBlank()) {
                        return VocabExample(english = en, persian = fa.ifBlank { word.persianMeaning })
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Falling back to local flashcard sentence builder", e)
            }
        }
        return buildOfflineFlashcardSentence(word, variantIndex)
    }

    fun buildOfflineFlashcardSentence(word: VocabWordEntity, variantIndex: Int = 0): VocabExample {
        val storedExamples = runCatching { json.decodeFromString<List<VocabExample>>(word.examplesJson) }.getOrDefault(emptyList())
        val collocations = runCatching { json.decodeFromString<List<String>>(word.collocationsJson) }.getOrDefault(emptyList())
        val pos = word.partOfSpeech.lowercase()
        val w = word.word
        val phrase = collocations.firstOrNull()?.takeIf { it.isNotBlank() }

        val generatedCandidates = when {
            pos.contains("verb") -> listOf(
                VocabExample(
                    english = if (phrase != null) "During the morning site walk, we need to $phrase before the client inspection."
                    else "As the Resident Engineer, I will $w the issue before signing off the inspection request.",
                    persian = "در بازدید صبحگاهی کارگاه، پیش از تایید نهایی باید موضوع «${word.persianMeaning}» را پیگیری کنیم."
                ),
                VocabExample(
                    english = "Our supervision team agreed to $w the revised method statement ahead of Friday's pour.",
                    persian = "تیم نظارت توافق کرد پیش از بتن‌ریزی جمعه، دستورالعمل اصلاح‌شده را بر اساس «${word.persianMeaning}» اجرا کند."
                )
            )
            pos.contains("adj") -> listOf(
                VocabExample(
                    english = "Maintaining a $w schedule across earthworks and utility corridors prevents costly handover delays.",
                    persian = "حفظ شرایط «${word.persianMeaning}» در عملیات خاکی و تاسیسات از تاخیرهای پرهزینه تحویل جلوگیری می‌کند."
                ),
                VocabExample(
                    english = "In international FIDIC contracts, every $w detail must be backed by signed site records.",
                    persian = "در قراردادهای بین‌المللی فیدیک، هر مورد «${word.persianMeaning}» باید با صورت‌جلسه امضاشده کارگاهی مستند شود."
                )
            )
            else -> listOf(
                VocabExample(
                    english = if (phrase != null) "The contractor submitted a formal update covering $phrase for Section B."
                    else "Please include the $w details in today's progress report and circulate it before 4 PM.",
                    persian = "لطفاً جزئیات مربوط به «${word.persianMeaning}» را در گزارش پیشرفت امروز قرار دهید."
                ),
                VocabExample(
                    english = "During the wastewater plant coordination meeting, we reviewed the $w requirement under ASTM standards.",
                    persian = "در جلسه هماهنگی تصفیه‌خانه، الزام مربوط به «${word.persianMeaning}» را بر اساس استاندارد بررسی کردیم."
                )
            )
        }
        val combined = generatedCandidates + storedExamples
        return combined[variantIndex % combined.size.coerceAtLeast(1)]
    }
}
