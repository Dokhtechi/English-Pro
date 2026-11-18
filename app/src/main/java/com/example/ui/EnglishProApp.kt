package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.device.DeviceDiagnosticsManager
import com.example.ui.localization.AppStrings
import com.example.ui.navigation.*
import com.example.ui.screens.*
import com.example.ui.theme.EnglishProTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SpeakingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishProApp(
    mainViewModel: MainViewModel = viewModel(),
    speakingViewModel: SpeakingViewModel = viewModel()
) {
    val context = LocalContext.current
    val baseDensity = LocalDensity.current
    val profile by mainViewModel.profile.collectAsStateWithLifecycle()
    val isOnline by mainViewModel.isOnline.collectAsStateWithLifecycle()
    val allVocab by mainViewModel.allVocab.collectAsStateWithLifecycle()
    val filteredVocab by mainViewModel.filteredVocab.collectAsStateWithLifecycle()
    val allCards by mainViewModel.allFsrsCards.collectAsStateWithLifecycle()
    val dueCards by mainViewModel.dueCards.collectAsStateWithLifecycle()
    val dueCount by mainViewModel.dueCount.collectAsStateWithLifecycle()
    val activeMastered by mainViewModel.activeMasteredCount.collectAsStateWithLifecycle()
    val passiveOnly by mainViewModel.passiveOnlyCount.collectAsStateWithLifecycle()
    val civilMastered by mainViewModel.civilMasteredCount.collectAsStateWithLifecycle()
    val dailyPlan by mainViewModel.dailyPlan.collectAsStateWithLifecycle()
    val packages by mainViewModel.packages.collectAsStateWithLifecycle()
    val lessons by mainViewModel.lessons.collectAsStateWithLifecycle()
    val scenarios by mainViewModel.scenarios.collectAsStateWithLifecycle()
    val assessments by mainViewModel.assessments.collectAsStateWithLifecycle()
    val studyLogs by mainViewModel.studyLogs.collectAsStateWithLifecycle()
    val errorPatterns by mainViewModel.errorPatterns.collectAsStateWithLifecycle()
    val reviewLogs by mainViewModel.reviewLogs.collectAsStateWithLifecycle()
    val sessions by mainViewModel.conversationSessions.collectAsStateWithLifecycle()
    val recordings by mainViewModel.recordings.collectAsStateWithLifecycle()
    val searchQuery by mainViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by mainViewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCefr by mainViewModel.selectedCefr.collectAsStateWithLifecycle()
    val diagnostics by mainViewModel.diagnostics.collectAsStateWithLifecycle()
    val featureMatrix by mainViewModel.featureMatrix.collectAsStateWithLifecycle()
    val speechModels by mainViewModel.speechModels.collectAsStateWithLifecycle()
    val ttsStatus by mainViewModel.ttsManager.status.collectAsStateWithLifecycle()
    val statusBanner by mainViewModel.statusBanner.collectAsStateWithLifecycle()

    val strings = remember(profile.uiLanguage) { AppStrings.forLang(profile.uiLanguage) }
    val layoutDirection = if (profile.uiLanguage == "FA") LayoutDirection.Rtl else LayoutDirection.Ltr
    val todayIso = remember { mainViewModel.repository.todayIso() }
    val todayLog = studyLogs.firstOrNull { it.dateIso == todayIso }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            snackbarHostState.showSnackbar(statusBanner!!)
            mainViewModel.clearBanner()
        }
    }

    val topDestinations = listOf(
        Triple(DashboardRoute, strings.navDashboard, Icons.Filled.Home),
        Triple(SpeakingRoute, strings.navSpeaking, Icons.Filled.Mic),
        Triple(VocabRoute, strings.navVocab, Icons.Filled.Style),
        Triple(EngineeringRoute, strings.navEngineering, Icons.Filled.Engineering),
        Triple(ToolsHubRoute, strings.navTools, Icons.Filled.GridView)
    )

    val isTopLevel = currentDest == null ||
        currentDest.hasRoute<DashboardRoute>() ||
        currentDest.hasRoute<SpeakingRoute>() ||
        currentDest.hasRoute<VocabRoute>() ||
        currentDest.hasRoute<EngineeringRoute>() ||
        currentDest.hasRoute<ToolsHubRoute>()

    val scaledDensity = remember(baseDensity, profile.fontScale) {
        Density(
            density = baseDensity.density,
            fontScale = (baseDensity.fontScale * profile.fontScale).coerceIn(0.75f, 1.65f)
        )
    }

    EnglishProTheme(darkThemePreference = profile.darkModePreference) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection,
            LocalDensity provides scaledDensity
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 600.dp

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (!isTopLevel && !currentDest!!.hasRoute<AssessmentRoute>()) {
                            TopAppBar(
                                title = { Text(strings.appTitle, maxLines = 1, softWrap = false) },
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!useNavRail && isTopLevel) {
                            NavigationBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                topDestinations.forEach { (route, label, icon) ->
                                    val selected = currentDest?.hasRoute(route::class) == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(DashboardRoute) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                                        modifier = Modifier.testTag("nav_${label.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavRail && isTopLevel) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.testTag("side_nav_rail")
                            ) {
                                topDestinations.forEach { (route, label, icon) ->
                                    val selected = currentDest?.hasRoute(route::class) == true
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(DashboardRoute) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = DashboardRoute,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            composable<DashboardRoute> {
                                DashboardScreen(
                                    profile = profile,
                                    todayLog = todayLog,
                                    dueCount = dueCount,
                                    vocabCount = allVocab.size,
                                    activeMasteredCount = activeMastered,
                                    dailyPlan = dailyPlan,
                                    isOnline = isOnline,
                                    strings = strings,
                                    onNavigateToSpeaking = { navController.navigate(SpeakingRoute) },
                                    onNavigateToVocab = { navController.navigate(VocabRoute) },
                                    onNavigateToEngineering = { navController.navigate(EngineeringRoute) },
                                    onNavigateToListening = { navController.navigate(ListeningRoute) },
                                    onNavigateToAssessment = { navController.navigate(AssessmentRoute) },
                                    onNavigateToAnalytics = { navController.navigate(AnalyticsRoute) },
                                    onNavigateToTools = { navController.navigate(ToolsHubRoute) },
                                    onOpenSettings = { navController.navigate(BackupRoute) },
                                    onToggleThemeMode = mainViewModel::setDarkMode
                                )
                            }
                            composable<SpeakingRoute> {
                                SpeakingCoachScreen(
                                    speakingViewModel = speakingViewModel,
                                    voiceGender = profile.voiceGender,
                                    onSetVoiceGender = mainViewModel::setVoiceGender,
                                    isOnline = isOnline,
                                    strings = strings
                                )
                            }
                            composable<VocabRoute> {
                                VocabularyScreen(
                                    allVocab = allVocab,
                                    filteredVocab = filteredVocab,
                                    allCards = allCards,
                                    dueCards = dueCards,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    selectedCefr = selectedCefr,
                                    isNewVocabGated = dailyPlan.isNewVocabGatedByBacklog,
                                    strings = strings,
                                    onSearchChange = mainViewModel::updateSearchQuery,
                                    onCategoryChange = mainViewModel::updateCategoryFilter,
                                    onCefrChange = mainViewModel::updateCefrFilter,
                                    onPlayAudio = { txt, acc -> mainViewModel.speakEnglish(txt, acc) },
                                    onSubmitReview = mainViewModel::submitVocabReview,
                                    onUpdateSelfRating = mainViewModel::updateSelfRating,
                                    onGenerateSentence = mainViewModel::generateFlashcardSentence
                                )
                            }
                            composable<EngineeringRoute> {
                                EngineeringScreen(
                                    lessons = lessons,
                                    scenarios = scenarios,
                                    strings = strings,
                                    onPlayAudio = { txt, acc -> mainViewModel.speakEnglish(txt, acc) },
                                    onStartRolePlayInCoach = { scenario ->
                                        speakingViewModel.startRolePlayScenario(scenario)
                                        navController.navigate(SpeakingRoute)
                                    },
                                    onMarkLessonComplete = mainViewModel::markLessonComplete
                                )
                            }
                            composable<ToolsHubRoute> {
                                ToolsHubScreen(
                                    strings = strings,
                                    onOpenListening = { navController.navigate(ListeningRoute) },
                                    onOpenAnalytics = { navController.navigate(AnalyticsRoute) },
                                    onOpenPackages = { navController.navigate(PackagesRoute) },
                                    onOpenDiagnostics = {
                                        mainViewModel.refreshDeviceDiagnostics()
                                        navController.navigate(DiagnosticsRoute)
                                    },
                                    onOpenBackup = { navController.navigate(BackupRoute) },
                                    onOpenAssessment = { navController.navigate(AssessmentRoute) }
                                )
                            }
                            composable<ListeningRoute> {
                                ListeningScreen(
                                    lessons = lessons,
                                    recordings = recordings,
                                    audioRecorder = mainViewModel.audioRecorder,
                                    strings = strings,
                                    onPlayTts = { txt, acc, spd -> mainViewModel.speakEnglish(txt, acc, spd) },
                                    onSaveShadowingRecording = mainViewModel::saveShadowingRecording
                                )
                            }
                            composable<AnalyticsRoute> {
                                AnalyticsScreen(
                                    allVocab = allVocab,
                                    activeMasteredCount = activeMastered,
                                    passiveOnlyCount = passiveOnly,
                                    civilMasteredCount = civilMastered,
                                    reviewLogs = reviewLogs,
                                    sessions = sessions,
                                    scenarios = scenarios,
                                    assessments = assessments,
                                    studyLogs = studyLogs,
                                    errorPatterns = errorPatterns,
                                    strings = strings
                                )
                            }
                            composable<PackagesRoute> {
                                PackagesAndSpeechScreen(
                                    packages = packages,
                                    speechModels = speechModels,
                                    ttsStatus = ttsStatus,
                                    availableStorageBytes = DeviceDiagnosticsManager.getAvailableStorageBytes(context),
                                    isOnline = isOnline,
                                    strings = strings,
                                    onInstallPackage = mainViewModel::installOrDownloadPackage,
                                    onPauseDownload = mainViewModel::pauseDownload,
                                    onDeletePackage = mainViewModel::deletePackage,
                                    onDownloadVoskModel = mainViewModel::downloadVoskModel,
                                    onRemoveVoskModel = mainViewModel::removeVoskModel,
                                    onRefreshSpeechStatus = mainViewModel::refreshDeviceDiagnostics
                                )
                            }
                            composable<DiagnosticsRoute> {
                                DiagnosticsScreen(
                                    report = diagnostics,
                                    featureMatrix = featureMatrix,
                                    strings = strings,
                                    onRefresh = mainViewModel::refreshDeviceDiagnostics
                                )
                            }
                            composable<BackupRoute> {
                                BackupAndSettingsScreen(
                                    profile = profile,
                                    ttsStatus = ttsStatus,
                                    strings = strings,
                                    studyDaysCount = studyLogs.size,
                                    vocabCount = allVocab.size,
                                    sessionsCount = sessions.size,
                                    onSetUiLanguage = mainViewModel::setUiLanguage,
                                    onSetDarkMode = mainViewModel::setDarkMode,
                                    onSetDailyTarget = mainViewModel::setDailyTargetMinutes,
                                    onSetAccentPreference = mainViewModel::setAccentPreference,
                                    onSetVoiceGender = mainViewModel::setVoiceGender,
                                    onTestVoice = {
                                        mainViewModel.speakEnglish(
                                            "Good morning engineer. The concrete slump test and rebar inspection meet ASTM standards."
                                        )
                                    },
                                    onSetFontScale = mainViewModel::setFontScale,
                                    onExportBackup = mainViewModel::exportBackupToUri,
                                    onPrepareShareBackup = mainViewModel::prepareBackupForShare,
                                    onImportBackup = mainViewModel::importBackupFromUri
                                )
                            }
                            composable<AssessmentRoute> {
                                AssessmentScreen(
                                    history = assessments,
                                    audioRecorder = mainViewModel.audioRecorder,
                                    strings = strings,
                                    onPlayTts = { txt, acc -> mainViewModel.speakEnglish(txt, acc) },
                                    onSaveAssessment = mainViewModel::submitAssessment,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
