package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.FsrsCardEntity
import com.example.data.local.VocabExample
import com.example.data.local.VocabWordEntity
import com.example.domain.srs.FsrsScheduler
import com.example.ui.components.CefrBadge
import com.example.ui.components.EnglishTextLtr
import com.example.ui.components.LtrBlock
import com.example.ui.components.VibrantAccentCard
import com.example.ui.localization.UiStrings
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val vocabJson = Json { ignoreUnknownKeys = true }

@Composable
fun VocabularyScreen(
    allVocab: List<VocabWordEntity>,
    filteredVocab: List<VocabWordEntity>,
    allCards: List<FsrsCardEntity>,
    dueCards: List<FsrsCardEntity>,
    searchQuery: String,
    selectedCategory: String,
    selectedCefr: String,
    isNewVocabGated: Boolean,
    strings: UiStrings,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onCefrChange: (String) -> Unit,
    onPlayAudio: (String, String) -> Unit,
    onSubmitReview: (VocabWordEntity, FsrsScheduler.Rating, String, Long, String) -> Unit,
    onUpdateSelfRating: (VocabWordEntity, String) -> Unit,
    onGenerateSentence: suspend (VocabWordEntity, Int) -> VocabExample,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        strings.flashcardsTab,
        strings.srsTab,
        strings.learnTab,
        strings.searchTab,
        strings.practiceTab
    )

    Column(modifier = modifier.fillMaxSize().testTag("vocabulary_screen")) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1, softWrap = false) },
                    modifier = Modifier.testTag("vocab_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> FlashcardsTab(
                allVocab = allVocab,
                dueCards = dueCards,
                strings = strings,
                onPlayAudio = onPlayAudio,
                onSubmitReview = onSubmitReview,
                onGenerateSentence = onGenerateSentence
            )
            1 -> SrsReviewTab(
                allVocab = allVocab,
                dueCards = dueCards,
                allCards = allCards,
                strings = strings,
                onPlayAudio = onPlayAudio,
                onSubmitReview = onSubmitReview
            )
            2 -> ActiveLearnTab(
                allVocab = allVocab,
                allCards = allCards,
                isNewVocabGated = isNewVocabGated,
                strings = strings,
                onPlayAudio = onPlayAudio,
                onSubmitReview = onSubmitReview,
                onUpdateSelfRating = onUpdateSelfRating
            )
            3 -> DictionarySearchTab(
                words = filteredVocab,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                selectedCefr = selectedCefr,
                strings = strings,
                onSearchChange = onSearchChange,
                onCategoryChange = onCategoryChange,
                onCefrChange = onCefrChange,
                onPlayAudio = onPlayAudio
            )
            4 -> RecallDrillsTab(
                allVocab = allVocab,
                strings = strings,
                onPlayAudio = onPlayAudio,
                onSubmitReview = onSubmitReview
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlashcardsTab(
    allVocab: List<VocabWordEntity>,
    dueCards: List<FsrsCardEntity>,
    strings: UiStrings,
    onPlayAudio: (String, String) -> Unit,
    onSubmitReview: (VocabWordEntity, FsrsScheduler.Rating, String, Long, String) -> Unit,
    onGenerateSentence: suspend (VocabWordEntity, Int) -> VocabExample
) {
    val scope = rememberCoroutineScope()
    var deckFilter by remember { mutableStateOf("ALL") } // ALL, DUE, CIVIL, DIFFICULT
    var shuffleSeed by remember { mutableIntStateOf(0) }
    var cardIndex by remember(deckFilter, shuffleSeed) { mutableIntStateOf(0) }

    val dueIds = remember(dueCards) { dueCards.map { it.wordId }.toSet() }
    val deck = remember(allVocab, dueIds, deckFilter, shuffleSeed) {
        val base = when (deckFilter) {
            "DUE" -> allVocab.filter { it.id in dueIds }
            "CIVIL" -> allVocab.filter { it.category != "GENERAL" }
            "DIFFICULT" -> allVocab.filter { it.selfRating == "DIFFICULT" || it.isPassiveOnly }
            else -> allVocab
        }
        if (shuffleSeed > 0) base.shuffled(kotlin.random.Random(shuffleSeed)) else base
    }

    val safeIndex = if (deck.isEmpty()) 0 else cardIndex.coerceIn(0, deck.lastIndex)
    val currentWord = deck.getOrNull(safeIndex)

    var showPersianMeaning by remember(currentWord?.id) { mutableStateOf(false) }
    var showSentenceSection by remember(currentWord?.id) { mutableStateOf(false) }
    var generatedSentence by remember(currentWord?.id) { mutableStateOf<VocabExample?>(null) }
    var sentenceVariant by remember(currentWord?.id) { mutableIntStateOf(0) }
    var isGeneratingSentence by remember(currentWord?.id) { mutableStateOf(false) }
    var userSentence by remember(currentWord?.id) { mutableStateOf("") }
    var cardShownAt by remember(currentWord?.id) { mutableLongStateOf(System.currentTimeMillis()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("flashcards_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Deck filter & shuffle bar
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "ALL" to (if (strings.isPersian) "همه (${allVocab.size})" else "All (${allVocab.size})"),
                    "CIVIL" to (if (strings.isPersian) "مهندسی عمران" else "Civil Eng"),
                    "DUE" to (if (strings.isPersian) "سررسید (${dueCards.size})" else "Due (${dueCards.size})"),
                    "DIFFICULT" to (if (strings.isPersian) "دشوار / غیرفعال" else "Difficult")
                )
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = deckFilter == key,
                        onClick = { deckFilter = key },
                        label = { Text(label, maxLines = 1, softWrap = false) }
                    )
                }
                AssistChip(
                    onClick = { shuffleSeed++ },
                    label = { Text(if (strings.isPersian) "بُر زدن" else "Shuffle", maxLines = 1, softWrap = false) },
                    leadingIcon = { Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        if (currentWord == null) {
            item {
                VibrantAccentCard(accentColor = VibrantAmber, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Style, contentDescription = null, tint = VibrantAmber, modifier = Modifier.size(44.dp))
                        Text(
                            text = if (strings.isPersian) "در این دسته فلش‌کارتی وجود ندارد." else "No flashcards match this filter.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Progress & counter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (strings.isPersian) "فلش‌کارت ${safeIndex + 1} از ${deck.size}" else "Flashcard ${safeIndex + 1} of ${deck.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        CefrBadge(currentWord.category.replace("_", " "), accentColor = VibrantTeal)
                    }
                    LinearProgressIndicator(
                        progress = { ((safeIndex + 1).toFloat() / deck.size.coerceAtLeast(1)) },
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                        color = VioletAccent,
                        trackColor = VioletAccent.copy(alpha = 0.16f)
                    )
                }
            }

            // Main Flashcard Surface
            item {
                VibrantAccentCard(
                    accentColor = VioletAccent,
                    modifier = Modifier.fillMaxWidth().testTag("flashcard_main_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LtrBlock {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentWord.partOfSpeech.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VioletAccent,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CefrBadge(currentWord.cefrLevel, accentColor = VibrantRoyalBlue)
                                        CefrBadge(currentWord.register, accentColor = VibrantTeal)
                                    }
                                }

                                EnglishTextLtr(
                                    text = currentWord.word,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                EnglishTextLtr(
                                    text = "US: ${currentWord.ipaUs}   •   UK: ${currentWord.ipaUk}",
                                    style = MaterialTheme.typography.labelMedium,
                                    monospace = true,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = { onPlayAudio(currentWord.word, "US") },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Filled.VolumeUp, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("US", maxLines = 1, softWrap = false)
                                    }
                                    FilledTonalButton(
                                        onClick = { onPlayAudio(currentWord.word, "UK") },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Filled.VolumeUp, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("UK", maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = VioletAccent.copy(alpha = 0.25f))

                        // On-demand Reveal Toggles (Persian Meaning & Sentence Builder)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { showPersianMeaning = !showPersianMeaning },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (showPersianMeaning) EmeraldSuccess.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (showPersianMeaning) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.2.dp, EmeraldSuccess.copy(alpha = 0.55f)),
                                modifier = Modifier.heightIn(min = 48.dp).testTag("flashcard_toggle_persian")
                            ) {
                                Icon(
                                    if (showPersianMeaning) Icons.Filled.VisibilityOff else Icons.Filled.Translate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (strings.isPersian) {
                                        if (showPersianMeaning) "پنهان کردن معنی فارسی" else "نمایش معنی فارسی"
                                    } else {
                                        if (showPersianMeaning) "Hide Persian" else "Show Persian Meaning"
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    val nextState = !showSentenceSection
                                    showSentenceSection = nextState
                                    if (nextState && generatedSentence == null) {
                                        scope.launch {
                                            isGeneratingSentence = true
                                            generatedSentence = onGenerateSentence(currentWord, sentenceVariant)
                                            sentenceVariant++
                                            isGeneratingSentence = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (showSentenceSection) VibrantRoyalBlue.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (showSentenceSection) VibrantRoyalBlue else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.2.dp, VibrantRoyalBlue.copy(alpha = 0.55f)),
                                modifier = Modifier.heightIn(min = 48.dp).testTag("flashcard_toggle_sentences")
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (strings.isPersian) {
                                        if (showSentenceSection) "پنهان کردن جمله‌بندی" else "جمله‌بندی با این کلمه"
                                    } else {
                                        if (showSentenceSection) "Hide Sentences" else "Build Sentences"
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Revealed Persian Meaning Panel
                        if (showPersianMeaning) {
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.12f),
                                border = BorderStroke(1.2.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().testTag("flashcard_persian_panel")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (strings.isPersian) "معنی فارسی:" else "Persian Meaning:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentWord.persianMeaning,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    EnglishTextLtr(
                                        text = currentWord.englishDefinition,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Revealed Sentence-Building Panel
                        if (showSentenceSection) {
                            val storedExamples = remember(currentWord.id) {
                                runCatching { vocabJson.decodeFromString<List<VocabExample>>(currentWord.examplesJson) }.getOrDefault(emptyList())
                            }
                            val collocs = remember(currentWord.id) {
                                runCatching { vocabJson.decodeFromString<List<String>>(currentWord.collocationsJson) }.getOrDefault(emptyList())
                            }
                            Surface(
                                color = VibrantRoyalBlue.copy(alpha = 0.09f),
                                border = BorderStroke(1.2.dp, VibrantRoyalBlue.copy(alpha = 0.45f)),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().testTag("flashcard_sentence_panel")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (strings.isPersian) "جمله‌بندی و کاربرد در جمله:" else "Sentence Construction & Examples:",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = VibrantRoyalBlue
                                        )
                                        AssistChip(
                                            onClick = {
                                                scope.launch {
                                                    isGeneratingSentence = true
                                                    generatedSentence = onGenerateSentence(currentWord, sentenceVariant)
                                                    sentenceVariant++
                                                    isGeneratingSentence = false
                                                }
                                            },
                                            label = {
                                                Text(
                                                    if (strings.isPersian) "ساخت جمله جدید" else "New Sentence",
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        )
                                    }

                                    if (isGeneratingSentence) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                    }

                                    generatedSentence?.let { gen ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.5f)),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    EnglishTextLtr(gen.english, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                                    IconButton(onClick = { onPlayAudio(gen.english, "INTERNATIONAL") }) {
                                                        Icon(Icons.Filled.VolumeUp, contentDescription = "Play generated sentence", tint = VioletAccent)
                                                    }
                                                }
                                                Text(gen.persian, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    storedExamples.forEach { ex ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, VibrantTeal.copy(alpha = 0.35f)),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    EnglishTextLtr(ex.english, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                                    IconButton(onClick = { onPlayAudio(ex.english, "INTERNATIONAL") }) {
                                                        Icon(Icons.Filled.VolumeUp, contentDescription = "Play example sentence", tint = VibrantTeal)
                                                    }
                                                }
                                                Text(ex.persian, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    if (collocs.isNotEmpty()) {
                                        EnglishTextLtr(
                                            text = "Useful collocations: ${collocs.joinToString("  •  ")}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    OutlinedTextField(
                                        value = userSentence,
                                        onValueChange = { userSentence = it },
                                        modifier = Modifier.fillMaxWidth().testTag("flashcard_user_sentence_input"),
                                        label = {
                                            Text(
                                                if (strings.isPersian) "جمله انگلیسی خود را با '${currentWord.word}' بنویسید:" else "Write your own sentence using '${currentWord.word}':"
                                            )
                                        },
                                        minLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Navigation Prev / Next + Quick FSRS Rating
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (safeIndex > 0) cardIndex = safeIndex - 1 },
                            enabled = safeIndex > 0,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("flashcard_prev_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (strings.isPersian) "کارت قبلی" else "Previous", maxLines = 1, softWrap = false)
                        }
                        Button(
                            onClick = { cardIndex = if (safeIndex < deck.lastIndex) safeIndex + 1 else 0 },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("flashcard_next_button")
                        ) {
                            Text(if (strings.isPersian) "کارت بعدی" else "Next Card", maxLines = 1, softWrap = false)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }

                    Text(
                        text = if (strings.isPersian) "ثبت سریع تسلط روی این فلش‌کارت (FSRS):" else "Quick Mastery Rating (FSRS):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    val ratingButtons = listOf(
                        Triple(FsrsScheduler.Rating.AGAIN, if (strings.isPersian) "فراموش" else "Again", CoralAccent),
                        Triple(FsrsScheduler.Rating.HARD, if (strings.isPersian) "سخت" else "Hard", VibrantAmber),
                        Triple(FsrsScheduler.Rating.GOOD, if (strings.isPersian) "خوب" else "Good", VibrantRoyalBlue),
                        Triple(FsrsScheduler.Rating.EASY, if (strings.isPersian) "آسان" else "Easy", EmeraldSuccess)
                    )
                    ratingButtons.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { (rating, label, btnColor) ->
                                FilledTonalButton(
                                    onClick = {
                                        val elapsed = System.currentTimeMillis() - cardShownAt
                                        val mode = if (userSentence.trim().length >= 8) "PRODUCTION" else "RECOGNITION"
                                        onSubmitReview(currentWord, rating, mode, elapsed, userSentence)
                                        cardIndex = if (safeIndex < deck.lastIndex) safeIndex + 1 else 0
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = btnColor.copy(alpha = 0.18f),
                                        contentColor = btnColor
                                    ),
                                    border = BorderStroke(1.dp, btnColor.copy(alpha = 0.55f)),
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                ) {
                                    Text(label, maxLines = 1, softWrap = false, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SrsReviewTab(
    allVocab: List<VocabWordEntity>,
    dueCards: List<FsrsCardEntity>,
    allCards: List<FsrsCardEntity>,
    strings: UiStrings,
    onPlayAudio: (String, String) -> Unit,
    onSubmitReview: (VocabWordEntity, FsrsScheduler.Rating, String, Long, String) -> Unit
) {
    var difficultOnly by remember { mutableStateOf(false) }
    val vocabMap = remember(allVocab) { allVocab.associateBy { it.id } }

    val queue = remember(dueCards, allCards, allVocab, difficultOnly) {
        if (difficultOnly) {
            allVocab.filter { it.selfRating == "DIFFICULT" || it.isPassiveOnly }
        } else {
            dueCards.mapNotNull { vocabMap[it.wordId] }
        }
    }

    val currentWord = queue.firstOrNull()
    var revealed by remember(currentWord?.id) { mutableStateOf(false) }
    var userSentence by remember(currentWord?.id) { mutableStateOf("") }
    var cardShownAt by remember(currentWord?.id) { mutableLongStateOf(System.currentTimeMillis()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (strings.isPersian) "سررسید مرور: ${dueCards.size} کلمه" else "Due for review: ${dueCards.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FilterChip(
                    selected = difficultOnly,
                    onClick = { difficultOnly = !difficultOnly },
                    label = {
                        Text(
                            if (strings.isPersian) "فقط واژگان دشوار/غیرفعال" else "Difficult & Passive Only",
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }

        if (currentWord == null) {
            item {
                VibrantAccentCard(accentColor = EmeraldSuccess, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(48.dp))
                        Text(
                            text = if (strings.isPersian) "مرورهای سررسید این نوبت تکمیل شد!" else "All scheduled reviews caught up!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            val card = allCards.firstOrNull { it.wordId == currentWord.id }
            val mode = if (currentWord.isPassiveOnly || (card?.recognitionPassCount ?: 0) >= 1) "PRODUCTION" else "RECOGNITION"
            item {
                VocabDetailCard(
                    word = currentWord,
                    showBack = revealed,
                    productionPrompt = mode == "PRODUCTION",
                    userSentence = userSentence,
                    onUserSentenceChange = { userSentence = it },
                    onPlayAudio = onPlayAudio
                )
            }
            item {
                if (!revealed) {
                    Button(
                        onClick = { revealed = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("srs_reveal_button")
                    ) {
                        Text(if (strings.isPersian) "نمایش معنی و مثال‌ها" else "Show Meaning & Examples", maxLines = 1, softWrap = false)
                    }
                } else {
                    Text(
                        text = if (strings.isPersian) "کیفیت یادآوری (الگوریتم FSRS):" else "FSRS Recall Rating:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    val ratingButtons = listOf(
                        Triple(FsrsScheduler.Rating.AGAIN, if (strings.isPersian) "فراموش" else "Again", CoralAccent),
                        Triple(FsrsScheduler.Rating.HARD, if (strings.isPersian) "سخت" else "Hard", VibrantAmber),
                        Triple(FsrsScheduler.Rating.GOOD, if (strings.isPersian) "خوب" else "Good", VibrantRoyalBlue),
                        Triple(FsrsScheduler.Rating.EASY, if (strings.isPersian) "آسان" else "Easy", EmeraldSuccess)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ratingButtons.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { (rating, label, btnColor) ->
                                    FilledTonalButton(
                                        onClick = {
                                            val elapsed = System.currentTimeMillis() - cardShownAt
                                            onSubmitReview(currentWord, rating, mode, elapsed, userSentence)
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = btnColor.copy(alpha = 0.18f),
                                            contentColor = btnColor
                                        ),
                                        border = BorderStroke(1.dp, btnColor.copy(alpha = 0.55f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("srs_rate_${rating.name.lowercase()}")
                                    ) {
                                        Text(label, maxLines = 1, softWrap = false, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveLearnTab(
    allVocab: List<VocabWordEntity>,
    allCards: List<FsrsCardEntity>,
    isNewVocabGated: Boolean,
    strings: UiStrings,
    onPlayAudio: (String, String) -> Unit,
    onSubmitReview: (VocabWordEntity, FsrsScheduler.Rating, String, Long, String) -> Unit,
    onUpdateSelfRating: (VocabWordEntity, String) -> Unit
) {
    val newIds = remember(allCards) { allCards.filter { it.state == 0 }.map { it.wordId }.toSet() }
    val newWords = remember(allVocab, newIds) { allVocab.filter { it.id in newIds } }
    var index by remember { mutableIntStateOf(0) }
    val current = newWords.getOrNull(index.coerceIn(0, (newWords.size - 1).coerceAtLeast(0)))
    var sentence by remember(current?.id) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isNewVocabGated) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = strings.backlogWarning,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else if (current == null) {
            item {
                VibrantAccentCard(accentColor = EmeraldSuccess, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (strings.isPersian) "همه واژگان بسته فعلی وارد مرور شده‌اند." else "All words in installed packages are already in review!",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            item {
                VocabDetailCard(
                    word = current,
                    showBack = true,
                    productionPrompt = true,
                    userSentence = sentence,
                    onUserSentenceChange = { sentence = it },
                    onPlayAudio = onPlayAudio
                )
            }
            item {
                Text(
                    text = if (strings.isPersian) "ارزیابی اولیه خودتان:" else "Self-assessment:",
                    style = MaterialTheme.typography.labelLarge
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val labels = listOf("NEW" to "New", "DIFFICULT" to "Difficult", "PARTIAL" to "Partial", "KNOWN" to "Known", "EASY" to "Easy")
                    items(labels) { (key, text) ->
                        FilterChip(
                            selected = current.selfRating == key,
                            onClick = { onUpdateSelfRating(current, key) },
                            label = { Text(text, maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val mode = if (sentence.trim().length >= 8) "PRODUCTION" else "RECOGNITION"
                        onSubmitReview(current, FsrsScheduler.Rating.GOOD, mode, 4500L, sentence)
                        if (index < newWords.lastIndex) index++
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("learn_word_commit_button")
                ) {
                    Text(
                        if (strings.isPersian) "ثبت جمله و افزودن به چرخه FSRS" else "Save Sentence & Add to FSRS Queue",
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DictionarySearchTab(
    words: List<VocabWordEntity>,
    searchQuery: String,
    selectedCategory: String,
    selectedCefr: String,
    strings: UiStrings,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onCefrChange: (String) -> Unit,
    onPlayAudio: (String, String) -> Unit
) {
    var expandedWordId by remember { mutableStateOf<String?>(null) }
    val categories = listOf(
        "ALL" to "All",
        "GENERAL" to "General",
        "CIVIL_SITE" to "Site",
        "CONCRETE_STRUCTURAL" to "Concrete",
        "GEOTECHNICAL" to "Geotech",
        "WATER_WASTEWATER" to "Wastewater",
        "PROJECT_MANAGEMENT" to "PM",
        "FIDIC" to "FIDIC",
        "SAFETY" to "HSE",
        "MEETINGS" to "Meetings"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth().testTag("vocab_search_input"),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(if (strings.isPersian) "جستجوی انگلیسی یا فارسی (آفلاین)..." else "Search English or Persian (offline)...") },
                singleLine = true
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { onCategoryChange(key) },
                        label = { Text(label, maxLines = 1, softWrap = false) }
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("ALL", "B2", "C1")) { lvl ->
                    FilterChip(
                        selected = selectedCefr == lvl,
                        onClick = { onCefrChange(lvl) },
                        label = { Text("CEFR: $lvl", maxLines = 1, softWrap = false) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(words, key = { it.id }) { word ->
                val isExpanded = expandedWordId == word.id
                val cardAccent = when (word.cefrLevel) {
                    "C1" -> VioletAccent
                    "B2+" -> VibrantTeal
                    else -> VibrantRoyalBlue
                }
                VibrantAccentCard(
                    accentColor = cardAccent,
                    onClick = { expandedWordId = if (isExpanded) null else word.id },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                EnglishTextLtr(
                                    text = word.word,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = cardAccent
                                )
                                EnglishTextLtr(
                                    text = "US ${word.ipaUs}  |  UK ${word.ipaUk}",
                                    style = MaterialTheme.typography.labelSmall,
                                    monospace = true,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                CefrBadge(level = word.cefrLevel, accentColor = cardAccent)
                                IconButton(onClick = { onPlayAudio(word.word, "US") }) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = "US audio", tint = cardAccent)
                                }
                            }
                        }
                        Text(word.persianMeaning, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        if (isExpanded) {
                            VocabExtendedFields(word = word, onPlayAudio = onPlayAudio)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecallDrillsTab(
    allVocab: List<VocabWordEntity>,
    strings: UiStrings,
    onPlayAudio: (String, String) -> Unit,
    onSubmitReview: (VocabWordEntity, FsrsScheduler.Rating, String, Long, String) -> Unit
) {
    var drillIndex by remember { mutableIntStateOf(0) }
    var drillMode by remember { mutableStateOf("E_TO_FA") }
    var selectedChoice by remember(drillIndex, drillMode) { mutableStateOf<Int?>(null) }

    if (allVocab.size < 4) return
    val target = allVocab[drillIndex % allVocab.size]
    val distractors = remember(target.id) {
        allVocab.filter { it.id != target.id }.shuffled().take(3)
    }
    val options = remember(target.id, drillMode) {
        (listOf(target) + distractors).shuffled()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val modes = listOf(
                    "E_TO_FA" to "EN→FA",
                    "FA_TO_E" to "FA→EN",
                    "CLOZE" to "Fill-in-Blank",
                    "SYNONYM" to "Synonym",
                    "LISTEN" to "Listen & Pick"
                )
                items(modes) { (m, label) ->
                    FilterChip(
                        selected = drillMode == m,
                        onClick = { drillMode = m },
                        label = { Text(label, maxLines = 1, softWrap = false) }
                    )
                }
            }
        }
        item {
            VibrantAccentCard(accentColor = VioletAccent, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (drillMode) {
                        "E_TO_FA" -> EnglishTextLtr(target.word, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = VioletAccent)
                        "FA_TO_E" -> Text(target.persianMeaning, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = VioletAccent)
                        "CLOZE" -> {
                            val examples = runCatching { vocabJson.decodeFromString<List<VocabExample>>(target.examplesJson) }.getOrDefault(emptyList())
                            val blanked = examples.firstOrNull()?.english?.replace(target.word, "_____", ignoreCase = true) ?: target.englishDefinition
                            EnglishTextLtr(blanked, style = MaterialTheme.typography.bodyLarge)
                        }
                        "SYNONYM" -> {
                            val syns = runCatching { vocabJson.decodeFromString<List<String>>(target.synonymsJson) }.getOrDefault(emptyList())
                            EnglishTextLtr("Select the word matching synonym(s): ${syns.joinToString(", ").ifBlank { target.englishDefinition }}", style = MaterialTheme.typography.bodyLarge)
                        }
                        "LISTEN" -> {
                            Button(onClick = { onPlayAudio(target.word, "INTERNATIONAL") }, modifier = Modifier.heightIn(min = 48.dp)) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Play Spoken Word", maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    options.forEachIndexed { idx, opt ->
                        val label = if (drillMode == "E_TO_FA") opt.persianMeaning else opt.word
                        val isCorrect = opt.id == target.id
                        val isPicked = selectedChoice == idx
                        OutlinedButton(
                            onClick = {
                                selectedChoice = idx
                                val rating = if (isCorrect) FsrsScheduler.Rating.GOOD else FsrsScheduler.Rating.AGAIN
                                onSubmitReview(target, rating, "RECOGNITION", 3500L, "")
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = when {
                                selectedChoice == null -> ButtonDefaults.outlinedButtonColors()
                                isCorrect -> ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                isPicked -> ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                else -> ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(label)
                        }
                    }

                    Button(
                        onClick = { drillIndex++ },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(if (strings.isPersian) "تمرین بعدی" else "Next Question", maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VocabDetailCard(
    word: VocabWordEntity,
    showBack: Boolean,
    productionPrompt: Boolean,
    userSentence: String,
    onUserSentenceChange: (String) -> Unit,
    onPlayAudio: (String, String) -> Unit
) {
    VibrantAccentCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LtrBlock {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EnglishTextLtr(
                            word.word,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CefrBadge(word.cefrLevel, accentColor = VibrantRoyalBlue)
                            CefrBadge(word.register, accentColor = VibrantTeal)
                        }
                    }
                    EnglishTextLtr("US: ${word.ipaUs}   •   UK: ${word.ipaUk}", style = MaterialTheme.typography.labelMedium, monospace = true)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(onClick = { onPlayAudio(word.word, "US") }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("US", maxLines = 1, softWrap = false)
                        }
                        FilledTonalButton(onClick = { onPlayAudio(word.word, "UK") }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("UK", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            if (productionPrompt) {
                OutlinedTextField(
                    value = userSentence,
                    onValueChange = onUserSentenceChange,
                    modifier = Modifier.fillMaxWidth().testTag("active_sentence_input"),
                    label = { Text("Active Production: Write/speak an original sentence using '${word.word}'") },
                    minLines = 2
                )
            }

            if (showBack) {
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                Text(word.persianMeaning, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                VocabExtendedFields(word = word, onPlayAudio = onPlayAudio)
            }
        }
    }
}

@Composable
private fun VocabExtendedFields(
    word: VocabWordEntity,
    onPlayAudio: (String, String) -> Unit
) {
    val examples = remember(word.id) { runCatching { vocabJson.decodeFromString<List<VocabExample>>(word.examplesJson) }.getOrDefault(emptyList()) }
    val collocs = remember(word.id) { runCatching { vocabJson.decodeFromString<List<String>>(word.collocationsJson) }.getOrDefault(emptyList()) }
    val syns = remember(word.id) { runCatching { vocabJson.decodeFromString<List<String>>(word.synonymsJson) }.getOrDefault(emptyList()) }
    val family = remember(word.id) { runCatching { vocabJson.decodeFromString<List<String>>(word.wordFamilyJson) }.getOrDefault(emptyList()) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        EnglishTextLtr("Definition: ${word.englishDefinition}", style = MaterialTheme.typography.bodyMedium)
        examples.forEach { ex ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EnglishTextLtr(ex.english, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onPlayAudio(ex.english, "INTERNATIONAL") }) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = "Play sentence", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Text(ex.persian, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (collocs.isNotEmpty()) EnglishTextLtr("Collocations: ${collocs.joinToString(" • ")}", style = MaterialTheme.typography.bodySmall)
        if (syns.isNotEmpty()) EnglishTextLtr("Synonyms: ${syns.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
        if (family.isNotEmpty()) EnglishTextLtr("Word Family: ${family.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
        if (word.commonMistakes.isNotBlank()) EnglishTextLtr("Watch out: ${word.commonMistakes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        if (word.britishVsAmericanNote.isNotBlank()) EnglishTextLtr("US vs UK: ${word.britishVsAmericanNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        EnglishTextLtr("Source: ${word.sourceAttribution} (${word.licenseInfo})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
