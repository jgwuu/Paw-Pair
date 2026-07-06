package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PetEntity
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper
import android.os.Build
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        com.example.widget.PetWidgetWorker.setupPeriodicRefresh(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        enableEdgeToEdge()
        setContent {
            val viewModel: PetViewModel = viewModel()
            val userStats by viewModel.userStats.collectAsState()
            MyApplicationTheme(appTheme = userStats?.appTheme ?: "FOREST") {
                MainContentScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContentScreen(viewModel: PetViewModel) {
    val context = LocalContext.current

    // Database states
    val isLoading by viewModel.isLoading.collectAsState()
    val allPets by viewModel.allPets.collectAsState()
    val activePet by viewModel.activePet.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()

    // UI Navigation & Dialog states
    var currentTab by remember { mutableIntStateOf(0) } // 0: Home/Cuidado, 1: Cocina, 2: Minigames, 3: Shop, 4: CoCare
    var showAdoptionScreen by remember { mutableStateOf(false) }
    var showPetSelectionDialog by remember { mutableStateOf(false) }
    
    // Minigame active session states
    var activeMinigameType by remember { mutableStateOf<String?>(null) } // "CATCHER" or "MEMORY"

    // Toast notifications collector
    LaunchedEffect(key1 = true) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Onboarding flow: Choice screen or selection/adoption screen
    var showSelectionForm by remember { mutableStateOf(false) }

    // On startup, check if pet data exists
    if (isLoading) {
        // Show empty box or loading indicator
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (allPets.isEmpty() && !showSelectionForm) {
        StartChoiceScreen(
            viewModel = viewModel,
            onAdoptSelected = { showSelectionForm = true }
        )
    } else if (allPets.isEmpty() || showAdoptionScreen) {
        SelectionScreen(
            viewModel = viewModel,
            onBack = {
                showSelectionForm = false
                showAdoptionScreen = false
            }
        )
    } else {
        // Main scaffold with safe bottom navigation paddings
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { currentTab = 5 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userStats?.myAvatar ?: "👤", fontSize = 18.sp)
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showPetSelectionDialog = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activePet?.name ?: "PawPair",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val iconEmoji = when (activePet?.type) {
                                "SHIBA" -> "🐶"
                                "SLIME" -> "💧"
                                "KITTY" -> "🐱"
                                "DRACO" -> "🔥"
                                "AXOLOTL" -> "🌸"
                                else -> "🐾"
                            }
                            Text(text = iconEmoji, fontSize = 20.sp)
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                "Mascotas",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (activePet?.isSleeping == true) Color(0xFF0F172A) else MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        // Quick shop shortcut with coins display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .clickable { currentTab = 2 }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Filled.MonetizationOn,
                                "Coins",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userStats?.coins ?: 0}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = if (activePet?.isSleeping == true) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0 && activeMinigameType == null,
                        onClick = {
                            currentTab = 0
                            activeMinigameType = null
                        },
                        icon = { Icon(if (currentTab == 0) Icons.Filled.Pets else Icons.Outlined.Pets, "Cuidado") },
                        label = { Text("Cuidado", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1 && activeMinigameType == null,
                        onClick = {
                            currentTab = 1
                            activeMinigameType = null
                        },
                        icon = { Icon(if (currentTab == 1) Icons.Filled.Restaurant else Icons.Outlined.Restaurant, "Cocina") },
                        label = { Text("Cocina", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2 || activeMinigameType != null,
                        onClick = { currentTab = 2 },
                        icon = { Icon(if (currentTab == 2) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports, "Minijuegos") },
                        label = { Text("Juegos", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Icon(if (currentTab == 3) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart, "Tienda") },
                        label = { Text("Tienda", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                // Determine active Screen Tab
                when {
                    activeMinigameType == "CATCHER" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CatcherGameView(
                                petType = activePet?.type ?: "SHIBA",
                                petName = activePet?.name ?: "Mascota",
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Catcher", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0 // return home
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "MEMORY" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            MemoryMatchGameView(
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Memory", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0 // return home
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "TICTACTOE" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            TicTacToeGameView(
                                petType = activePet?.type ?: "SHIBA",
                                petName = activePet?.name ?: "Mascota",
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("TicTacToe", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0 // return home
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "WORDSEARCH" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            WordSearchGameView(
                                petType = activePet?.type ?: "SHIBA",
                                petName = activePet?.name ?: "Mascota",
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("WordSearch", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0 // return home
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "SIMON" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            SimonSaysGame(
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Simon", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "FLAPPY" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            FlappyPetGame(
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Flappy", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "QUIZ" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            PetQuizGame(
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Quiz", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "BUBBLE" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            BubblePopGame(
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Bubble", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "MATH" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            PetMathGame(
                                onGameFinished = { coins, score ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Math", coins, score)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onClose = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "RUNNER" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            SpaceRunnerGame(
                                onGameFinished = { coins, xp ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Runner", coins, xp)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onBack = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "GUESS" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            MagicGuessGame(
                                onGameFinished = { coins, xp ->
                                    viewModel.earnCoinsAndStatsFromMinigame("Guess", coins, xp)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onBack = { activeMinigameType = null }
                            )
                        }
                    }
                    activeMinigameType == "RPS" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            RockPaperScissorsGame(
                                onGameFinished = { coins, xp ->
                                    viewModel.earnCoinsAndStatsFromMinigame("RPS", coins, xp)
                                    activeMinigameType = null
                                    currentTab = 0
                                },
                                onBack = { activeMinigameType = null }
                            )
                        }
                    }
                    else -> {
                        // Regular Tabs Navigation
                        when (currentTab) {
                            0 -> {
                                activePet?.let { pet ->
                                    DashboardScreen(
                                        rawPet = pet,
                                        stats = userStats,
                                        inventory = inventory,
                                        viewModel = viewModel,
                                        onAdoptNew = { showPetSelectionDialog = true },
                                        onOpenGames = { currentTab = 2 },
                                        modifier = Modifier.testTag("dashboard_viewport")
                                    )
                                }
                            }
                            1 -> {
                                KitchenScreen(
                                    activePet = activePet,
                                    inventory = inventory,
                                    stats = userStats,
                                    viewModel = viewModel,
                                    modifier = Modifier.testTag("kitchen_viewport")
                                )
                            }
                            2 -> {
                                // ARCADE PORTAL SCREEN
                                ArcadePortalScreen(
                                    activePet = activePet,
                                    onLaunchCatcher = { activeMinigameType = "CATCHER" },
                                    onLaunchMemory = { activeMinigameType = "MEMORY" },
                                    onLaunchTicTacToe = { activeMinigameType = "TICTACTOE" },
                                    onLaunchWordSearch = { activeMinigameType = "WORDSEARCH" },
                                    onLaunchSimon = { activeMinigameType = "SIMON" },
                                    onLaunchFlappy = { activeMinigameType = "FLAPPY" },
                                    onLaunchQuiz = { activeMinigameType = "QUIZ" },
                                    onLaunchBubble = { activeMinigameType = "BUBBLE" },
                                    onLaunchMath = { activeMinigameType = "MATH" },
                                    onLaunchRunner = { activeMinigameType = "RUNNER" },
                                    onLaunchGuess = { activeMinigameType = "GUESS" },
                                    onLaunchRPS = { activeMinigameType = "RPS" }
                                )
                            }
                            3 -> {
                                ShopScreen(
                                    inventory = inventory,
                                    stats = userStats,
                                    activePet = activePet,
                                    viewModel = viewModel
                                )
                            }
                            4 -> {
                                CoCareScreen(
                                    stats = userStats,
                                    activePet = activePet,
                                    logs = activityLogs,
                                    viewModel = viewModel
                                )
                            }
                            5 -> {
                                ProfileScreen(
                                    stats = userStats,
                                    onSave = { name, avatar ->
                                        viewModel.updateProfile(name, avatar)
                                        currentTab = 0
                                    },
                                    onGoToCoCare = {
                                        currentTab = 4
                                    },
                                    onThemeChanged = { code ->
                                        viewModel.updateAppTheme(code)
                                    },
                                    onNotificationsChanged = { enabled ->
                                        viewModel.updateNotificationsEnabled(enabled)
                                    },
                                    onRoutineHourChanged = { hour ->
                                        viewModel.updateRoutineHour(hour)
                                    },
                                    onReleaseOrDeletePet = { isOwner ->
                                        viewModel.releaseOrDeletePet(isOwner) { success, msg ->
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                            if (success) currentTab = 0
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to choose or switch adopted pets (Refugio Familiar)
    if (showPetSelectionDialog) {
        Dialog(
            onDismissRequest = { showPetSelectionDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Refugio Familiar de Mascotas 🏡",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Elige cuál de tus mascotas consentidas cuidar hoy:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of adoptable pets in DB
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allPets) { petItem ->
                            val isActive = petItem.id == activePet?.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectPet(petItem.id)
                                        showPetSelectionDialog = false
                                        currentTab = 0
                                    }
                                    .testTag("pet_entry_${petItem.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                border = if (isActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom renderer preview
                                    Box(modifier = Modifier.size(45.dp)) {
                                        PetRenderer(
                                            type = petItem.type,
                                            isSleeping = petItem.isSleeping,
                                            hunger = 90f,
                                            happiness = 90f,
                                            equippedHat = petItem.equippedHat,
                                            equippedAccessory = petItem.equippedAccessory,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = petItem.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Tipo: ${getPetNameTypeLocal(petItem.type)} • Estado: ${if (petItem.isSleeping) "Durmiendo" else "Despierto"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                                        )
                                    }

                                    if (isActive) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons inside dialog: adopt new or close
                    Button(
                        onClick = {
                            showPetSelectionDialog = false
                            showAdoptionScreen = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, "Adopt another")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adopta Otra Mascota 🐾", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showPetSelectionDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Cerrar", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ArcadePortalScreen(
    activePet: PetEntity?,
    onLaunchCatcher: () -> Unit,
    onLaunchMemory: () -> Unit,
    onLaunchTicTacToe: () -> Unit,
    onLaunchWordSearch: () -> Unit,
    onLaunchSimon: () -> Unit = {},
    onLaunchFlappy: () -> Unit = {},
    onLaunchQuiz: () -> Unit = {},
    onLaunchBubble: () -> Unit = {},
    onLaunchMath: () -> Unit = {},
    onLaunchRunner: () -> Unit = {},
    onLaunchGuess: () -> Unit = {},
    onLaunchRPS: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "Minijuegos Arcade 🍓",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "¡Consigue dinero para consentir a ${activePet?.name ?: "tu mascota"}!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category filter chips
        var selectedCategory by remember { mutableStateOf("TODOS") }

        ScrollableTabRow(
            selectedTabIndex = when (selectedCategory) {
                "ACCIÓN" -> 1
                "MENTE" -> 2
                "PUZLES" -> 3
                else -> 0
            },
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            listOf("TODOS" to "🌟 Todos (12)", "ACCIÓN" to "🚀 Acción", "MENTE" to "🧠 Mente", "PUZLES" to "🧩 Puzles").forEach { pair ->
                Tab(
                    selected = selectedCategory == pair.first,
                    onClick = { selectedCategory = pair.first },
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        pair.second,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCategory == pair.first) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
        }

        data class MinigameCardInfo(
            val title: String,
            val emoji: String,
            val desc: String,
            val category: String,
            val bgColor: Color,
            val action: () -> Unit
        )

        val allGames = listOf(
            MinigameCardInfo("Carrera Espacial 🚀", "🛸", "¡Esquiva asteroides y recolecta estrellas en el espacio sideral!", "ACCIÓN", Color(0xFFE0F2FE), onLaunchRunner),
            MinigameCardInfo("Adivina el Número 🔮", "🎯", "Deduce el número mágico oculto entre 1 y 100 con pistas intuitivas.", "MENTE", Color(0xFFF3E8FF), onLaunchGuess),
            MinigameCardInfo("Duelo Elemental 🔥", "⚡", "¡Batalla épica de Fuego, Agua y Planta contra la IA de tu mascota!", "ACCIÓN", Color(0xFFFEE2E2), onLaunchRPS),
            MinigameCardInfo("Sopa de Letras Gourmet", "✍️", "Encuentra palabras ocultas en el tablero en tiempo récord.", "PUZLES", Color(0xFFE8F5E9), onLaunchWordSearch),
            MinigameCardInfo("Slime Catcher", "🍓", "¡Mueve tu canasta para atrapar frutas frescas y esquivar petardos!", "ACCIÓN", Color(0xFFE3F2FD), onLaunchCatcher),
            MinigameCardInfo("Memoria PawPair", "🐾", "Voltea cartas y encuentra todas las parejas de mascotas en menor número de turnos.", "PUZLES", Color(0xFFEDE7F6), onLaunchMemory),
            MinigameCardInfo("Tres en Raya vs Mascota", "⭕", "Juega el clásico Tic-Tac-Toe retando a la IA inteligente de tu mascota.", "MENTE", Color(0xFFFFF9C4), onLaunchTicTacToe),
            MinigameCardInfo("Simón Dice", "🎵", "¡Repite secuencias crecientes de colores musicales sin equivocarte!", "MENTE", Color(0xFFFBE9E7), onLaunchSimon),
            MinigameCardInfo("Flappy Pet", "🕊️", "¡Vuela entre tuberías dando golpecitos en la pantalla para alcanzar récords!", "ACCIÓN", Color(0xFFE0F7FA), onLaunchFlappy),
            MinigameCardInfo("Trivia Mascotas", "💡", "Demuestra cuánto sabes sobre cuidados veterinarios y curiosidades de animales.", "MENTE", Color(0xFFFFF8E1), onLaunchQuiz),
            MinigameCardInfo("Explota Burbujas", "🫧", "¡Pincha burbujas de colores a toda velocidad antes de que se acabe el tiempo!", "ACCIÓN", Color(0xFFE1F5FE), onLaunchBubble),
            MinigameCardInfo("Matemáticas Rápidas", "🔢", "¡Resuelve sumas veloces para ejercitar el cerebro y ganar abundantes monedas!", "MENTE", Color(0xFFF1F8E9), onLaunchMath)
        )

        val filteredGames = allGames.filter { selectedCategory == "TODOS" || it.category == selectedCategory }

        filteredGames.forEach { game ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(game.bgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(game.emoji, fontSize = 30.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(game.title, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(game.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(game.desc, fontSize = 11.sp, color = Color.Gray, lineHeight = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = game.action,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .fillMaxWidth()
                        ) {
                            Text("¡Jugar Ahora! 🕹️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun getPetNameTypeLocal(type: String): String {
    return when (type) {
        "SHIBA" -> "Shiba 🐶"
        "SLIME" -> "Slime 💧"
        "KITTY" -> "Gatito 🐱"
        "DRACO" -> "Mini Dragón 🐉"
        "AXOLOTL" -> "Axol Mágico 🌸"
        else -> "Mascota 🐾"
    }
}
