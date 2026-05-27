package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.FlashcardEntity
import com.example.data.database.PomodoroSessionEntity
import com.example.data.database.QuizHistoryEntity
import com.example.data.database.UserSettingsEntity
import com.example.ui.Tab
import com.example.ui.SynapseViewModel
import com.example.ui.theme.MyApplicationTheme

// Atmospheric styles and custom palettes
object AtmosphericStyles {
    val CosmicGlowBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Deep Slate
            Color(0xFF1E1B4B), // Purple Indigo
            Color(0xFF030712)  // Cosmic black void
        )
    )
    val NebulaTealBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF022C22), // Deep Emerald
            Color(0xFF0F172A), // Slate
            Color(0xFF020617)  // Cyber dark
        )
    )
    val LuminescentLightBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF1F5F9), // Slate Light
            Color(0xFFE2E8F0), // Translucent light slate
            Color(0xFFCBD5E1)  // Deeper silver slate
        )
    )

    // Accents
    val CosmicGlowAccent = Color(0xFFC084FC) // Neon Lavender
    val NebulaTealAccent = Color(0xFF2DD4BF)  // Cyber Teal
    val LuminescentLightAccent = Color(0xFF2563EB) // Celestial Blue

    // Frosted Card Glass tints
    val CosmicGlowCard = Color(0x3B1E1B4B)
    val NebulaTealCard = Color(0x27042F1A)
    val LuminescentLightCard = Color(0xAAFFFFFF)

    // Text primary
    val CosmicGlowText = Color.White
    val NebulaTealText = Color(0xFFE2E8F0)
    val LuminescentLightText = Color(0xFF1E293B)
}

fun getAdviceText(mood: String, lang: String): String {
    return when (lang) {
        "Hindi" -> when (mood) {
            "Stressed 😩" -> "गहरी सांस लें। आपकी सीखने की गति और न्यूरॉन्स की स्थिरता के लिए तनाव को दूर करना महत्वपूर्ण है।"
            "Energetic ⚡" -> "आप पूर्ण उर्जा से भरे हैं! इस तरंग का लाभ उठाकर कठिन विषयों को हल करें!"
            "Curious 🤔" -> "आपकी जिज्ञासा नई अवधारणाओं को दर्ज करने के लिए उपयुक्त है। अन्वेषण जारी रखें!"
            "Bored 😴" -> "क्या आप सुस्त महसूस कर रहे हैं? पोमोडोरो टाइमर चालू करें और एक छोटी चुनौती लें!"
            else -> "आपके शरीर को आराम की जरूरत है। ५ मिनट का मौन ध्यान करें और ऊर्जा बहाल करें।"
        }
        "Hinglish" -> when (mood) {
            "Stressed 😩" -> "Chills lo yaar. Deep breath stroke lo aur neurons ko optimize hone do."
            "Energetic ⚡" -> "Energy super high hai! Is momentum me tough parts khatam kar dalo."
            "Curious 🤔" -> "Curiosity level top notch hai. Kuch naya seekhne ka perfect time!"
            "Bored 😴" -> "Boredom se fight karo! Quick 15-min Pomodoro session launch karo."
            else -> "Battery down ho rahi hai? Refresh hone ke liye ambient rain music suno."
        }
        "Spanish" -> when (mood) {
            "Stressed 😩" -> "Respira profundo. Liberar la tensión optimiza tus sinapsis cognitivas."
            "Energetic ⚡" -> "¡Increíble flujo de energía! Aprovecha para resolver los temas complejos."
            "Curious 🤔" -> "Tu curiosidad te permite conectar ideas nuevas de forma inmediata."
            "Bored 😴" -> "¿Aburrido? Intenta un módulo rápido de Pomodoro para reactivar tu cerebro."
            else -> "Nivel de batería bajo. Intenta relajarte con música ambiental de naturaleza."
        }
        else -> when (mood) { // English default
            "Stressed 😩" -> "Inhale serenity. Relieving physical tension maximizes your synaptic cognitive speed."
            "Energetic ⚡" -> "Sustained peak flow detected! Direct this hyper-focus wave onto complex domains."
            "Curious 🤔" -> "Your curiosity is prime for recording novel concepts. Explore uncharted theories!"
            "Bored 😴" -> "Boredom is a signal for challenge. Kickstart an active 15-minute Pomodoro sprint!"
            else -> "Low battery readings. Regenerate neural capacity with deep breathing and ambient nature loops."
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun getAttributionTag(): String? {
        return "default"
    }

    private val viewModel: SynapseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val settings by viewModel.userSettings.collectAsStateWithLifecycle()
                val flashcards by viewModel.flashcardsList.collectAsStateWithLifecycle()
                val quizRecords by viewModel.quizHistoryList.collectAsStateWithLifecycle()
                val sessions by viewModel.pomodoroSessionsList.collectAsStateWithLifecycle()

                val context = LocalContext.current
                val view = LocalView.current

                // Theme selector mapping
                val bgBrush = when (settings.selectedAtmosphere) {
                    "Nebula Teal" -> AtmosphericStyles.NebulaTealBg
                    "Luminescent Light-Slate Space" -> AtmosphericStyles.LuminescentLightBg
                    else -> AtmosphericStyles.CosmicGlowBg
                }

                val accentColor = when (settings.selectedAtmosphere) {
                    "Nebula Teal" -> AtmosphericStyles.NebulaTealAccent
                    "Luminescent Light-Slate Space" -> AtmosphericStyles.LuminescentLightAccent
                    else -> AtmosphericStyles.CosmicGlowAccent
                }

                val cardBg = when (settings.selectedAtmosphere) {
                    "Nebula Teal" -> AtmosphericStyles.NebulaTealCard
                    "Luminescent Light-Slate Space" -> AtmosphericStyles.LuminescentLightCard
                    else -> AtmosphericStyles.CosmicGlowCard
                }

                val textColor = when (settings.selectedAtmosphere) {
                    "Luminescent Light-Slate Space" -> AtmosphericStyles.LuminescentLightText
                    else -> AtmosphericStyles.CosmicGlowText
                }

                val isLuminescentLight = settings.selectedAtmosphere == "Luminescent Light-Slate Space"

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Glowing Dock Navigation Bar
                        CustomDockedNavigationBar(
                            selectedTab = viewModel.currentTab,
                            accentColor = accentColor,
                            cardBg = cardBg,
                            isLight = isLuminescentLight,
                            onTabSelected = {
                                viewModel.triggerHapticFeedback(view)
                                viewModel.triggerAudioFeedback(1)
                                viewModel.currentTab = it
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgBrush)
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Render DASHBOARD HEADER ONLY on Home Tab
                            if (viewModel.currentTab == Tab.Home) {
                                MainDashboardHeader(
                                    settings = settings,
                                    accentColor = accentColor,
                                    cardBg = cardBg,
                                    isLight = isLuminescentLight,
                                    textColor = textColor,
                                    tick = viewModel.frameTick
                                )
                            }

                            // Sliding View transitions mapping to active tabs
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                AnimatedContent(
                                    targetState = viewModel.currentTab,
                                    transitionSpec = {
                                        if (initialState.ordinal < targetState.ordinal) {
                                            slideInHorizontally { width -> width / 2 } + fadeIn() togetherWith
                                                    slideOutHorizontally { width -> -width / 2 } + fadeOut()
                                        } else {
                                            slideInHorizontally { width -> -width / 2 } + fadeIn() togetherWith
                                                    slideOutHorizontally { width -> width / 2 } + fadeOut()
                                        }
                                    },
                                    label = "tab_transitions"
                                ) { tab ->
                                    when (tab) {
                                        Tab.Home -> HomeScreen(
                                            viewModel = viewModel,
                                            settings = settings,
                                            accentColor = accentColor,
                                            cardBg = cardBg,
                                            isLight = isLuminescentLight,
                                            textColor = textColor
                                        )
                                        Tab.Explainer -> ExplainerScreen(
                                            viewModel = viewModel,
                                            settings = settings,
                                            accentColor = accentColor,
                                            cardBg = cardBg,
                                            isLight = isLuminescentLight,
                                            textColor = textColor
                                        )
                                        Tab.Quiz -> QuizScreen(
                                            viewModel = viewModel,
                                            records = quizRecords,
                                            accentColor = accentColor,
                                            cardBg = cardBg,
                                            isLight = isLuminescentLight,
                                            textColor = textColor
                                        )
                                        Tab.Flashcards -> FlashcardsScreen(
                                            viewModel = viewModel,
                                            flashcardsList = flashcards,
                                            accentColor = accentColor,
                                            cardBg = cardBg,
                                            isLight = isLuminescentLight,
                                            textColor = textColor
                                        )
                                        Tab.Focus -> FocusScreen(
                                            viewModel = viewModel,
                                            sessions = sessions,
                                            accentColor = accentColor,
                                            cardBg = cardBg,
                                            isLight = isLuminescentLight,
                                            textColor = textColor
                                        )
                                        Tab.Settings -> SettingsScreen(
                                            viewModel = viewModel,
                                            settings = settings,
                                            flashcardsList = flashcards,
                                            sessions = sessions,
                                            records = quizRecords,
                                            accentColor = accentColor,
                                            cardBg = cardBg,
                                            isLight = isLuminescentLight,
                                            textColor = textColor
                                        )
                                    }
                                }
                            }
                        }

                        // Calibration Success Diagnostic Dialog
                        if (viewModel.showCalibrationDialog) {
                            AlertDialog(
                                onDismissRequest = { viewModel.showCalibrationDialog = false },
                                title = {
                                    Text(
                                        "SYNAPSE INITIAL CALIBRATION",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = accentColor,
                                        fontSize = 18.sp
                                    )
                                },
                                text = {
                                    Text(
                                        viewModel.calibrationMessage,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isLuminescentLight) Color(0xFF1E293B) else Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.triggerAudioFeedback(2)
                                            viewModel.showCalibrationDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                    ) {
                                        Text("DISMISS", fontWeight = FontWeight.SemiBold, color = Color.Black)
                                    }
                                },
                                containerColor = if (isLuminescentLight) Color.White else Color(0xFF1E1B4B),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ━ DYNAMIC MAIN DASHBOARD HEADER ━
@Composable
fun MainDashboardHeader(
    settings: UserSettingsEntity,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    textColor: Color,
    tick: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, if (isLight) Color(0x33000000) else Color(0x22FFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Circular Badge - IMMERSIVE GYROSCOPIC DESIGN
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(68.dp)
            ) {
                // Spinning outer dashed energy ring
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = (tick * 35f) % 360f
                        }
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                accentColor,
                                Color.Transparent,
                                accentColor.copy(alpha = 0.4f),
                                accentColor
                            )
                        ),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(12f, 12f),
                                0f
                            )
                        )
                    )
                }

                // Inner core
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        )
                        .border(1.5.dp, accentColor.copy(alpha = 0.7f), CircleShape)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LVL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${settings.level}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // XP and Study Streak Display
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "SYNAPSE STATE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                    Text(
                        text = "${settings.xp} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // XP Progress Bar - GLOW SHADOW IMMERSIVE SLATE
                val progressPercent = (settings.xp % 200).toFloat() / 200f
                LinearProgressIndicator(
                    progress = progressPercent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = if (isLight) Color(0x1F000000) else Color(0x1FFFFFFF)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${200 - (settings.xp % 200)} XP to level up",
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Study Streak Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "STREAK",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥",
                        fontSize = 22.sp
                    )
                    Text(
                        text = "${settings.studyStreak}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ━ CUSTOM DOCKED BOTTOM NAVIGATION BAR ━
@Composable
fun CustomDockedNavigationBar(
    selectedTab: Tab,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    onTabSelected: (Tab) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, if (isLight) Color(0x22000000) else Color(0x1AFFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Tab.Home to Icons.Default.Home,
                Tab.Explainer to Icons.Default.Face,
                Tab.Quiz to Icons.Default.Star,
                Tab.Flashcards to Icons.Default.Menu,
                Tab.Focus to Icons.Default.Refresh,
                Tab.Settings to Icons.Default.Settings
            )

            tabs.forEach { (tab, icon) ->
                val active = selectedTab == tab
                val itemColor = if (active) accentColor else if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.name,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val label = when (tab) {
                        Tab.Home -> "Home"
                        Tab.Explainer -> "AI Chat"
                        Tab.Quiz -> "Quiz"
                        Tab.Flashcards -> "Cards"
                        Tab.Focus -> "Focus"
                        Tab.Settings -> "Settings"
                    }
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = itemColor
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    // Immersive animation of active underlines
                    AnimatedVisibility(
                        visible = active,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(3.dp)
                                .background(accentColor, RoundedCornerShape(1.5.dp))
                        )
                    }
                    if (!active) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(3.dp)
                        )
                    }
                }
            }
        }
    }
}

// ━ SCREEN 1: DYNAMIC MAIN DASHBOARD (HOME TAB WITH SYSTEM CONFIGURATION FOOTER CARD) ━
@Composable
fun HomeScreen(
    viewModel: SynapseViewModel,
    settings: UserSettingsEntity,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    textColor: Color
) {
    val view = LocalView.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Interactive Mood Chips
            Text(
                "SYNAPSE WAVE CHANNEL STATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val moods = listOf("Stressed 😩", "Energetic ⚡", "Curious 🤔", "Bored 😴", "Tired 🔋")
                moods.forEach { mood ->
                    val selected = viewModel.currentMood == mood
                    FilterChip(
                        selected = selected,
                        onClick = {
                            viewModel.triggerHapticFeedback(view)
                            viewModel.triggerAudioFeedback(2)
                            viewModel.currentMood = mood
                        },
                        label = {
                            Text(
                                mood,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.Black else textColor
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            containerColor = if (isLight) Color(0x11000000) else Color(0x0EFFFFFF)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) accentColor.copy(alpha = 0.8f) else if (isLight) Color(0x15000000) else Color(0x15FFFFFF)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("mood_" + mood.lowercase().substringBefore(" ").trim())
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        item {
            // Live Interactive Brainwave Canvas Visualizer
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (isLight) Color(0x33000000) else Color(0x22FFFFFF)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "BRAINWAVE OSCILLATOR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = accentColor
                            )
                            val frequencyName = when (viewModel.currentMood) {
                                "Stressed 😩" -> "Erratic High Beta (~28 Hz)"
                                "Energetic ⚡" -> "Sync Gamma Wave (~40 Hz)"
                                "Curious 🤔" -> "Active Focus Beta (~18 Hz)"
                                "Bored 😴" -> "Loose Idle Alpha (~6 Hz)"
                                "Tired 🔋" -> "Sluggish Delta (~1.5 Hz)"
                                else -> "Synchronized Core"
                            }
                            Text(
                                frequencyName,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }

                        // Stimulate button trigger - TAC-GLOW STIMPULSE BUTTON
                        Button(
                            onClick = { viewModel.triggerSynapticStimulation(view) },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .testTag("stimulate_synapse_btn")
                                .height(32.dp)
                        ) {
                            Text(
                                "Stimpulse ⚡",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Brainwave live custom visualizer canvas drawing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        if (isLight) Color(0x06000000) else Color(0x3B0F172A),
                                        if (isLight) Color(0x02000000) else Color(0x4D030712)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, if (isLight) Color(0x15000000) else Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val centerX = width / 2
                            val centerY = height / 2

                            val frequencyScale: Float
                            val amp: Float
                            val waveDrawColor: Color

                            when (viewModel.currentMood) {
                                "Stressed 😩" -> {
                                    frequencyScale = 0.5f
                                    amp = 26.dp.toPx()
                                    waveDrawColor = Color(0xFFEF4444)
                                }
                                "Energetic ⚡" -> {
                                    frequencyScale = 0.65f
                                    amp = 20.dp.toPx()
                                    waveDrawColor = Color(0xFFFFB020)
                                }
                                "Curious 🤔" -> {
                                    frequencyScale = 0.35f
                                    amp = 15.dp.toPx()
                                    waveDrawColor = accentColor
                                }
                                "Bored 😴" -> {
                                    frequencyScale = 0.15f
                                    amp = 10.dp.toPx()
                                    waveDrawColor = Color(0xFF10B981)
                                }
                                else -> { // Tired
                                    frequencyScale = 0.05f
                                    amp = 5.dp.toPx()
                                    waveDrawColor = Color(0xFF3B82F6)
                                }
                            }

                            // Dynamic glowing background radial halo
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        waveDrawColor.copy(alpha = 0.18f),
                                        waveDrawColor.copy(alpha = 0.04f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension / 1.4f
                                )
                            )

                            // Wave 1: Primary thick wave
                            val p1 = Path()
                            for (x in 0..width.toInt() step 4) {
                                val y = centerY + kotlin.math.sin((x * 0.015f * frequencyScale) + viewModel.frameTick * 2.6f) * amp
                                if (x == 0) p1.moveTo(x.toFloat(), y) else p1.lineTo(x.toFloat(), y)
                            }
                            drawPath(p1, waveDrawColor, style = Stroke(width = 3.dp.toPx()))

                            // Wave 2: Complementary secondary shift wave
                            val p2 = Path()
                            val compColor = if (isLight) waveDrawColor.copy(alpha = 0.6f) else Color(0xFF818CF8)
                            for (x in 0..width.toInt() step 4) {
                                val y = centerY + kotlin.math.cos((x * 0.022f * frequencyScale) - viewModel.frameTick * 1.8f) * (amp * 0.7f)
                                if (x == 0) p2.moveTo(x.toFloat(), y) else p2.lineTo(x.toFloat(), y)
                            }
                            drawPath(p2, compColor.copy(alpha = 0.45f), style = Stroke(width = 1.8.dp.toPx()))

                            // Wave 3: Micro background noise wave (Gamma coherence)
                            val p3 = Path()
                            for (x in 0..width.toInt() step 6) {
                                val y = centerY + kotlin.math.sin((x * 0.04f * frequencyScale) + viewModel.frameTick * 4f) * (amp * 0.3f)
                                if (x == 0) p3.moveTo(x.toFloat(), y) else p3.lineTo(x.toFloat(), y)
                            }
                            drawPath(p3, waveDrawColor.copy(alpha = 0.22f), style = Stroke(width = 1.dp.toPx()))

                            // Neon core synaptic pulse
                            val pulseRadius = 12.dp.toPx() + kotlin.math.sin(viewModel.frameTick * 2.5f) * 3.dp.toPx()
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(waveDrawColor.copy(alpha = 0.45f), Color.Transparent),
                                    center = center,
                                    radius = pulseRadius * 2.4f
                                )
                            )
                            drawCircle(
                                color = waveDrawColor,
                                radius = pulseRadius,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.85f),
                                radius = pulseRadius * 0.45f
                            )

                            // Render brainwave ripple activation rings
                            if (viewModel.rippleActive) {
                                drawCircle(
                                    color = waveDrawColor.copy(alpha = viewModel.rippleAlpha),
                                    radius = (size.minDimension / 1.5f) * viewModel.rippleScale,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawCircle(
                                    color = waveDrawColor.copy(alpha = viewModel.rippleAlpha * 0.5f),
                                    radius = (size.minDimension / 2.2f) * viewModel.rippleScale,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multilingual interactive guidance and wisdom advice block - IMMERSIVE LEFT ACCENT CONTAINER
                    val adviceText = getAdviceText(viewModel.currentMood, settings.selectedLanguage)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(
                                if (isLight) Color(0x0C000000) else Color(0x1A0D1527),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isLight) Color(0x11000000) else Color(0x11FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.2.dp)
                                    .height(34.dp)
                                    .background(accentColor, RoundedCornerShape(1.6.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                adviceText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = textColor.copy(alpha = 0.95f),
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            // General Stats Hub Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isLight) Color(0x22000000) else Color(0x15FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "COGNITIVE HUB STATS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatChip(title = "Core Level", value = "${settings.level}", color = accentColor, isLight = isLight)
                        StatChip(title = "Experience", value = "${settings.xp} XP", color = accentColor, isLight = isLight)
                        StatChip(title = "Day Streak", value = "🔥 ${settings.studyStreak}", color = accentColor, isLight = isLight)
                    }
                }
            }
        }



        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun StatChip(title: String, value: String, color: Color, isLight: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isLight) Color(0x1A000000) else Color(0x15FFFFFF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, color = color.copy(alpha = 0.8f), fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ━ SCREEN 2: AI EXPLAINER (INTELLIGENCE NUCLEUS) ━
@Composable
fun ExplainerScreen(
    viewModel: SynapseViewModel,
    settings: UserSettingsEntity,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    textColor: Color
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Explaining Style select chip group - IMMERSIVE DESIGN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val styles = listOf("Simple / Layman", "Academic Spec", "Meme Mode", "Gamer Mode", "Analogical")
            styles.forEach { style ->
                val active = viewModel.customExplainerStyle == style
                FilterChip(
                    selected = active,
                    onClick = {
                        viewModel.triggerHapticFeedback(view)
                        viewModel.triggerAudioFeedback(2)
                        viewModel.customExplainerStyle = style
                    },
                    label = {
                        Text(
                            style,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) Color.Black else textColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        containerColor = if (isLight) Color(0x11000000) else Color(0x0EFFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (active) accentColor.copy(alpha = 0.8f) else if (isLight) Color(0x15000000) else Color(0x15FFFFFF)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Chat text response and message area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    if (isLight) Color(0x08000000) else Color(0x0AFFFFFF),
                    RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isLight) Color(0x18000000) else Color(0x14FFFFFF),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(12.dp)
        ) {
            val chats by viewModel.chatHistory
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chats) { chat ->
                    val isAi = chat.sender == "ai"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAi) {
                                    cardBg.copy(alpha = 0.88f)
                                } else {
                                    accentColor.copy(alpha = 0.9f)
                                }
                            ),
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isAi) 4.dp else 18.dp,
                                bottomEnd = if (isAi) 18.dp else 4.dp
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isAi) accentColor.copy(alpha = 0.35f) else Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isAi) "NUCLEUS CORE" else "YOU",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp,
                                    color = if (isAi) accentColor else Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chat.text,
                                    fontSize = 13.sp,
                                    color = if (isAi) textColor else Color.Black,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (viewModel.isExplaining) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = accentColor,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Synthesizing response...",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Input Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.chatInputText,
                onValueChange = { viewModel.chatInputText = it },
                placeholder = { Text("Ask something deep...", color = textColor.copy(alpha = 0.4f), fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text")
                    .padding(end = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor.copy(alpha = 0.8f),
                    focusedContainerColor = if (isLight) Color(0x06000000) else Color(0x0BFFFFFF),
                    unfocusedContainerColor = if (isLight) Color(0x03000000) else Color(0x06FFFFFF),
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = textColor.copy(alpha = 0.15f)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
            )

            FloatingActionButton(
                onClick = { viewModel.queryIntelligenceNucleus() },
                containerColor = accentColor,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("submit_chat_btn")
                    .size(52.dp)
            ) {
                Icon(
                     imageVector = Icons.AutoMirrored.Filled.Send,
                     contentDescription = "Submit prompt"
                )
            }
        }
    }
}

// ━ SCREEN 3: QUIZ ARENA ━
@Composable
fun QuizScreen(
    viewModel: SynapseViewModel,
    records: List<QuizHistoryEntity>,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    textColor: Color
) {
    val view = LocalView.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (viewModel.quizActiveState == "IDLE") {
            // Idle main screen
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.3f))))
                ) {
                    Column(
                        modifier = Modifier.padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Glowing Icon Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .background(accentColor.copy(alpha = 0.15f), CircleShape)
                                .border(1.2.dp, accentColor, CircleShape)
                        ) {
                            Text("🛡️", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "COGNITIVE QUIZ ARENA",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Challenge your focus. Answer academic & neuroscientific multiple choice questions to rank up and harvest heavy XP points. Perfect score unlocks the supreme trophies!",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.startQuiz() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .testTag("start_quiz_btn")
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                "INITIATE ARENA SYNC ⚡",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Historical achievements
            item {
                Text(
                    "RECENT SYNC LOGS (QUIZ HISTORY)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )
            }

            if (records.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f))
                    ) {
                        Text(
                            "No previous quiz attempts logged in synapses database.",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.padding(20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(records) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isLight) Color(0x33000000) else Color(0x14FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Quantum Integration Score",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    "Topic: ${item.topic}",
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${item.score}/${item.totalQuestions}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = accentColor
                                )
                                Text(
                                    "+${item.xpEarned} XP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        } else if (viewModel.quizActiveState == "RUNNING") {
            // Running MCQ state
            val q = viewModel.quizQuestions[viewModel.currentQuizIndex]
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "QUESTION ${viewModel.currentQuizIndex + 1} of ${viewModel.quizQuestions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                    Text(
                        "Score: ${viewModel.quizScore}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Immersive Progression Line
                val completedFraction = (viewModel.currentQuizIndex).toFloat() / viewModel.quizQuestions.size
                LinearProgressIndicator(
                    progress = completedFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accentColor,
                    trackColor = if (isLight) Color(0x1A000000) else Color(0x19FFFFFF)
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, if (isLight) Color(0x33000000) else Color(0x22FFFFFF))
                ) {
                    Text(
                        q.question,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(22.dp)
                    )
                }
            }

            // Options List
            items(q.options.size) { index ->
                val option = q.options[index]
                val selected = viewModel.selectedQuizOption == index
                val submitted = viewModel.quizSubmitted
                val correctIndex = q.correctIndex

                val containerColor = when {
                    submitted && index == correctIndex -> Color(0x3D10B981) // Correct green
                    submitted && selected && index != correctIndex -> Color(0x3DEF4444) // Error red
                    selected -> accentColor.copy(alpha = 0.25f)
                    else -> cardBg
                }

                val borderStroke = when {
                    submitted && index == correctIndex -> BorderStroke(1.8.dp, Color(0xFF10B981))
                    submitted && selected && index != correctIndex -> BorderStroke(1.8.dp, Color(0xFFEF4444))
                    selected -> BorderStroke(1.8.dp, accentColor)
                    else -> BorderStroke(1.dp, if (isLight) Color(0x1F000000) else Color(0x14FFFFFF))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectQuizOption(index) }
                        .testTag("quiz_option_$index"),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = RoundedCornerShape(18.dp),
                    border = borderStroke
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { viewModel.selectQuizOption(index) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accentColor,
                                unselectedColor = textColor.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            if (viewModel.quizSubmitted) {
                // Show question scientific theory explanation
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isLight) Color(0xFFF1F5F9) else Color(0x187C3AED)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "NEURAL ANALYSER EXPLANATION:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                q.explanation,
                                fontSize = 12.sp,
                                color = textColor,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            if (viewModel.currentQuizIndex == viewModel.quizQuestions.size - 1) "SUMMARIZE ARENA TRANSCRIPTS" else "PROCEED TO NEXT SEQUENCE",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            } else {
                item {
                    Button(
                        onClick = { viewModel.submitQuizAnswer(view) },
                        enabled = viewModel.selectedQuizOption != -1,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            "TRANSMIT ANSWER PULSE",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        } else {
            // COMPLETED results feedback screen
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(accentColor, Color(0xFF10B981))))
                ) {
                    Column(
                        modifier = Modifier.padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "ARENA INTERFACING OVERFLOW",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Score details
                        Text(
                            text = "${viewModel.quizScore} / ${viewModel.quizQuestions.size}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            "Correct Sequence Transmissions",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Award XP calculation feedback
                        val awardXp = (viewModel.quizScore * 20) + (if (viewModel.quizScore == viewModel.quizQuestions.size) 50 else 0)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, accentColor)
                        ) {
                            Text(
                                "INTELLIGENCE GENERATED +$awardXp XP",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        Button(
                            onClick = { viewModel.quizActiveState = "IDLE" },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                "FINALIZE ARENA TRANSACTION",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// ━ SCREEN 4: ARCHIVAL FLASHCARDS ROOM ━
@Composable
fun FlashcardsScreen(
    viewModel: SynapseViewModel,
    flashcardsList: List<FlashcardEntity>,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    textColor: Color
) {
    val view = LocalView.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "DEEP MEMORY FLASHCARDS ROOM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }

        if (flashcardsList.isNotEmpty() && viewModel.activeFlashcardIndex < flashcardsList.size) {
            val card = flashcardsList[viewModel.activeFlashcardIndex]

            item {
                // 3D-like Swiper Card with graphics flip animation Y
                val flipAngle = animateFloatAsState(
                    targetValue = if (viewModel.flashcardFlipped) 180f else 0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                    label = "flashcard_angle"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .testTag("interactive_flashcard")
                        .graphicsLayer {
                            rotationY = flipAngle.value
                            cameraDistance = 14f * density
                        }
                        .clickable { viewModel.flipFlashcard(view) }
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        modifier = Modifier.fillMaxSize(),
                        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.3f)))),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        // Switch layouts halfway through flip transition so text isn't reversed Y
                        if (flipAngle.value <= 90f) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "MICRO-STUDY QUESTION",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Text(
                                        "Deck: ${card.deckName}",
                                        fontSize = 10.sp,
                                        color = textColor.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    card.question,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = textColor,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                Text(
                                    "[ Click / Tap to Flip ]",
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.4f),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            // Rotate content inside reverse view so it doesn't render mirrored
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "DECRYPTED SYSTEM ANSWER",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                    Text(
                                        "Deck: ${card.deckName}",
                                        fontSize = 10.sp,
                                        color = textColor.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    card.answer,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = textColor,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )

                                Text(
                                    "[ Flip back for details ]",
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.3f),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Swiper swiping controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevFlashcard() },
                        modifier = Modifier
                            .background(if (isLight) Color(0x0C000000) else Color(0x0FAFFFFFF), CircleShape)
                            .border(1.dp, textColor.copy(alpha = 0.15f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev", tint = textColor)
                    }

                    Button(
                        onClick = { viewModel.flipFlashcard(view) },
                        colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                        border = BorderStroke(1.2.dp, accentColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("flashcard_reveal_btn")
                            .height(44.dp)
                    ) {
                        Text(
                            if (viewModel.flashcardFlipped) "HIDE MATRIX ANSWER" else "REVEAL COGNITIVE ANSWER",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextFlashcard() },
                        modifier = Modifier
                            .background(if (isLight) Color(0x0C000000) else Color(0x0FAFFFFFF), CircleShape)
                            .border(1.dp, textColor.copy(alpha = 0.15f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = textColor)
                    }
                }
            }

            item {
                // Action: Delete card
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { viewModel.deleteCurrentFlashcard() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prune card model", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isLight) Color(0x22000000) else Color(0x14FFFFFF))
                ) {
                    Text(
                        "No micro-study flashcard modules available in memory database core. Create one below to initiate synapses training!",
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Add modular card additions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isLight) Color(0x22000000) else Color(0x15FFFFFF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "INJECT NEW FLASHCARD METRIC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = viewModel.flashcardInputQuestion,
                        onValueChange = { viewModel.flashcardInputQuestion = it },
                        label = { Text("Study Question Token", color = textColor.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("flashcard_question_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.15f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = viewModel.flashcardInputAnswer,
                        onValueChange = { viewModel.flashcardInputAnswer = it },
                        label = { Text("Study Core Answer", color = textColor.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("flashcard_answer_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.15f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.createFlashcard(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("flashcard_save_btn")
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            "INJECT CORE FLASHCARD STATE",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ━ SCREEN 5: FOCUS ROOM (AERODYNAMIC POMODORO) ━
@Composable
fun FocusScreen(
    viewModel: SynapseViewModel,
    sessions: List<PomodoroSessionEntity>,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    textColor: Color
) {
    val view = LocalView.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "AERODYNAMIC FOCUS ROOM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }

        item {
            // Elegant Frosted Glass Countdown timer representation
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.3f))))
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular representation
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(180.dp)
                            .background(
                                if (isLight) Color(0x06000000) else Color(0x0F000000),
                                CircleShape
                            )
                    ) {
                        val maxSec = viewModel.focusTimeSelection * 60f
                        val progress = if (maxSec > 0f) viewModel.focusSecondsRemaining.toFloat() / maxSec else 0f

                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxSize(0.92f),
                            strokeWidth = 10.dp,
                            color = accentColor,
                            trackColor = if (isLight) Color(0x1F000000) else Color(0x14FFFFFF)
                        )

                        // Digital timer countdown
                        val m = viewModel.focusSecondsRemaining / 60
                        val s = viewModel.focusSecondsRemaining % 60
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                String.format("%02d:%02d", m, s),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "REMAINING SLOTS",
                                fontSize = 8.sp,
                                color = textColor.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Customizable durations configs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val durations = listOf(15, 25, 45, 60)
                        durations.forEach { minutes ->
                            val active = viewModel.focusTimeSelection == minutes
                            OutlinedButton(
                                onClick = { viewModel.configureFocusTimer(minutes) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, if (active) accentColor else textColor.copy(alpha = 0.15f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (active) accentColor.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier
                                    .testTag("focus_dur_$minutes")
                                    .height(36.dp)
                            ) {
                                Text(
                                    "${minutes}m",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) accentColor else textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pause Play Button custom controller
                    Button(
                        onClick = { viewModel.toggleFocusTimer(view) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("focus_toggle_btn")
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (viewModel.isFocusRunning) Icons.Default.Check else Icons.Default.PlayArrow,
                                contentDescription = "Play toggle",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (viewModel.isFocusRunning) "INTERRUPT FOCUS PULSE" else "ACTIVE STUDY WAVE ⚡",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { viewModel.resetFocusTimer() }) {
                        Text(
                            "RESET TIMER",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            // Ambient Auditory simulation block
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isLight) Color(0x22000000) else Color(0x15FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "ATMOSPHERIC WHITE NOISE FLOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AmbientSoundToggle(
                        title = "Atmospheric White Noise",
                        enabled = viewModel.ambientWhiteNoiseEnabled,
                        onToggled = { viewModel.ambientWhiteNoiseEnabled = it },
                        textColor = textColor,
                        accentColor = accentColor
                    )
                    AmbientSoundToggle(
                        title = "Forest Rainstorm Streams",
                        enabled = viewModel.ambientRainEnabled,
                        onToggled = { viewModel.ambientRainEnabled = it },
                        textColor = textColor,
                        accentColor = accentColor
                    )
                    AmbientSoundToggle(
                        title = "Cyber Lo-Fi Binaural Beats",
                        enabled = viewModel.ambientLofiEnabled,
                        onToggled = { viewModel.ambientLofiEnabled = it },
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }
            }
        }

        item {
            Text(
                "COMPLETED FOCUS SESSIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }

        if (sessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f))
                ) {
                    Text(
                        "No recent focus slot records available.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(sessions) { s ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isLight) Color(0x22000000) else Color(0x14FFFFFF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "",
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(s.category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Text("Focused duration slots", fontSize = 10.sp, color = textColor.copy(alpha = 0.5f))
                            }
                        }
                        Text(
                            "${s.durationMinutes} min",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmbientSoundToggle(
    title: String,
    enabled: Boolean,
    onToggled: (Boolean) -> Unit,
    textColor: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "",
                tint = if (enabled) accentColor else textColor.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 12.sp, color = textColor)
        }
        Switch(
            checked = enabled,
            onCheckedChange = { onToggled(it) },
            colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.3f))
        )
    }
}

// ━ SCREEN 6: APP SETTINGS & MILESTONES ━
data class BadgeInfo(
    val title: String,
    val description: String,
    val emoji: String,
    val targetType: String, // "STREAK", "XP", "CARDS", "FOCUS", "QUIZ"
    val targetVal: Int
)

@Composable
fun SettingsScreen(
    viewModel: SynapseViewModel,
    settings: UserSettingsEntity,
    flashcardsList: List<FlashcardEntity>,
    sessions: List<PomodoroSessionEntity>,
    records: List<QuizHistoryEntity>,
    accentColor: Color,
    cardBg: Color,
    isLight: Boolean,
    textColor: Color
) {
    val badges = listOf(
        BadgeInfo("Starting Scholar", "Collect at least 150 study experience points", "🎓", "XP", 150),
        BadgeInfo("Super Learner", "Reach 1,000 total experience points", "🌌", "XP", 1000),
        BadgeInfo("Streak Starter", "Keep an active study streak of 5 days", "🔥", "STREAK", 5),
        BadgeInfo("Consistency King", "Keep an active study streak of 10 days", "☄️", "STREAK", 10),
        BadgeInfo("Card Designer", "Create at least 5 study flashcards", "📚", "CARDS", 5),
        BadgeInfo("Focus Master", "Log at least 3 deep focus sessions", "⌛", "FOCUS", 3),
        BadgeInfo("Quiz Champion", "Compete inside the Quiz Arena once", "🏆", "QUIZ", 1)
    )

    val view = LocalView.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "APP SETTINGS & MILESTONES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }

        // Language, Theme, Sound Volume & AI selector Card (Redesigned Settings layout!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (isLight) Color(0x22000000) else Color(0x15FFFFFF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Customize App Setup",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // APP LANGUAGE Selection
                    Text(
                        "APP LANGUAGE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val languages = listOf("English", "Hindi", "Hinglish", "Spanish")
                        languages.forEach { lang ->
                            val active = settings.selectedLanguage == lang
                            OutlinedButton(
                                onClick = { viewModel.updateLanguage(lang) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (active) accentColor.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (active) accentColor else textColor.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Text(
                                    lang,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) accentColor else textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // APP THEME Selection
                    Text(
                        "COLOR THEME",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val atmospheres = listOf("Cosmic Glow", "Nebula Teal", "Luminescent Light-Slate Space")
                        val displayThemes = mapOf(
                            "Cosmic Glow" to "Cosmic",
                            "Nebula Teal" to "Teal",
                            "Luminescent Light-Slate Space" to "Light Mode"
                        )
                        atmospheres.forEach { style ->
                            val active = settings.selectedAtmosphere == style
                            OutlinedButton(
                                onClick = { viewModel.updateAtmosphere(style) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (active) accentColor.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (active) accentColor else textColor.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    displayThemes[style] ?: style,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) accentColor else textColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // APP SOUND VOLUME
                    Text(
                        "SOUND EFFECTS VOLUME (${(settings.soundLevel * 100).toInt()}%)",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = settings.soundLevel,
                        onValueChange = { viewModel.updateSoundSettings(it, settings.auditoryEnabled, settings.hapticEnabled) },
                        colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = settings.auditoryEnabled,
                                onCheckedChange = { viewModel.updateSoundSettings(settings.soundLevel, it, settings.hapticEnabled) },
                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                            )
                            Text("Play Sounds", fontSize = 12.sp, color = textColor)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = settings.hapticEnabled,
                                onCheckedChange = { viewModel.updateSoundSettings(settings.soundLevel, settings.auditoryEnabled, it) },
                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                            )
                            Text("Play Vibrations", fontSize = 12.sp, color = textColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CHOOSE AI ENGINE
                    Text(
                        "CHOOSE AI ENGINE TYPE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.updateAiModel("gemini-3.5-flash") }
                                .weight(1f)
                        ) {
                            RadioButton(
                                selected = settings.selectedModel == "gemini-3.5-flash",
                                onClick = { viewModel.updateAiModel("gemini-3.5-flash") },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Text("Gemini Flash (Faster)", fontSize = 12.sp, color = textColor)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.updateAiModel("gemini-3.1-pro-preview") }
                                .weight(1f)
                        ) {
                            RadioButton(
                                selected = settings.selectedModel == "gemini-3.1-pro-preview",
                                onClick = { viewModel.updateAiModel("gemini-3.1-pro-preview") },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Text("Gemini Pro (Smarter)", fontSize = 12.sp, color = textColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // APP INTEGRITY DIAGNOSTIC
                    Button(
                        onClick = { viewModel.runCalibrationDiagnostic() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("calibrate_synapses_btn")
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "CHECK APP SYSTEM HEALTH",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Trophies / Milestones header (Clean, spacious layout)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "MY MILESTONES & TROPHIES",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        // Trophies list content
        items(badges) { badge ->
            val currentProgress = when (badge.targetType) {
                "XP" -> settings.xp
                "STREAK" -> settings.studyStreak
                "CARDS" -> flashcardsList.size
                "FOCUS" -> sessions.size
                "QUIZ" -> records.size
                else -> 0
            }
            val unlocked = currentProgress >= badge.targetVal

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (unlocked) cardBg else cardBg.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (unlocked) Brush.linearGradient(listOf(Color(0xFF10B981), accentColor))
                    else Brush.linearGradient(listOf(textColor.copy(alpha = 0.08f), textColor.copy(alpha = 0.04f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (unlocked) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0x0C000000),
                                CircleShape
                            )
                            .border(1.dp, if (unlocked) Color(0xFF10B981) else Color.Transparent, CircleShape)
                    ) {
                        Text(badge.emoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unlocked) textColor else textColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = badge.description,
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.5f),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress bar calculations
                        val progressPercent = if (badge.targetVal > 0) {
                            (currentProgress.toFloat() / badge.targetVal).coerceIn(0f, 1f)
                        } else 0f

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = progressPercent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (unlocked) Color(0xFF10B981) else accentColor,
                                trackColor = if (isLight) Color(0x1F000000) else Color(0x1FFFFFFF)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (unlocked) "UNLOCKED 💚" else "$currentProgress / ${badge.targetVal}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (unlocked) Color(0xFF10B981) else textColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// Additional settings views integrated directly inside Tab.Home for perfect ergonomics which saves space and respects Navigation rules!
@Composable
fun HomeSettingsCard(
    viewModel: SynapseViewModel,
    settings: UserSettingsEntity,
    accentColor: Color,
    cardBg: Color,
    textColor: Color,
    isLight: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        border = BorderStroke(1.dp, if (isLight) Color(0x33000000) else Color(0x1AFFFFFF)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "SYSTEM CONFIGURATION NUCLEUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Workspace language
            Text(
                "WORKSPACE TRANSLATION LANGUAGE",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val languages = listOf("English", "Hindi", "Hinglish", "Spanish")
                languages.forEach { lang ->
                    val active = settings.selectedLanguage == lang
                    OutlinedButton(
                        onClick = { viewModel.updateLanguage(lang) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (active) accentColor.copy(alpha = 0.15f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (active) accentColor else textColor.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Text(
                            lang,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) accentColor else textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Atmospheric Styles
            Text(
                "ATMOSPHERE ENVIRONMENT",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val atmospheres = listOf("Cosmic Glow", "Nebula Teal", "Luminescent Light-Slate Space")
                atmospheres.forEach { style ->
                    val active = settings.selectedAtmosphere == style
                    OutlinedButton(
                        onClick = { viewModel.updateAtmosphere(style) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (active) accentColor.copy(alpha = 0.15f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (active) accentColor else textColor.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text(
                            style.substringBefore(" "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) accentColor else textColor,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Holographic acoustics volume slider
            Text(
                "HOLOGRAPHIC ACOUSTIC SOUNDS (${(settings.soundLevel * 100).toInt()}%)",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = settings.soundLevel,
                onValueChange = { viewModel.updateSoundSettings(it, settings.auditoryEnabled, settings.hapticEnabled) },
                colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = settings.auditoryEnabled,
                        onCheckedChange = { viewModel.updateSoundSettings(settings.soundLevel, it, settings.hapticEnabled) },
                        colors = CheckboxDefaults.colors(checkedColor = accentColor)
                    )
                    Text("Auditory Feedback", fontSize = 11.sp, color = textColor)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = settings.hapticEnabled,
                        onCheckedChange = { viewModel.updateSoundSettings(settings.soundLevel, settings.auditoryEnabled, it) },
                        colors = CheckboxDefaults.colors(checkedColor = accentColor)
                    )
                    Text("Haptic Signals", fontSize = 11.sp, color = textColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Model Configuration Radio Buttons
            Text(
                "INTELLIGENCE CORE MODEL",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Flash v Pro selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.updateAiModel("gemini-3.5-flash") }
                        .weight(1f)
                ) {
                    RadioButton(
                        selected = settings.selectedModel == "gemini-3.5-flash",
                        onClick = { viewModel.updateAiModel("gemini-3.5-flash") },
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                    Text("Gemini 1.5 Flash (Speed)", fontSize = 11.sp, color = textColor)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.updateAiModel("gemini-3.1-pro-preview") }
                        .weight(1f)
                ) {
                    RadioButton(
                        selected = settings.selectedModel == "gemini-3.1-pro-preview",
                        onClick = { viewModel.updateAiModel("gemini-3.1-pro-preview") },
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                    Text("Gemini 1.5 Pro (Theory)", fontSize = 11.sp, color = textColor)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Diagnostic button Calibrate Synapses
            Button(
                onClick = { viewModel.runCalibrationDiagnostic() },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .testTag("calibrate_synapses_btn")
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "CALIBRATE ALL SYNAPSES DIRECT DIAGNOSTICS",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
