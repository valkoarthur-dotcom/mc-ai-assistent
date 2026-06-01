package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import android.util.Log
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BuildRepository
import com.example.data.GeminiService
import com.example.data.MinecraftBuild
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

// Custom Block definition to render inside Blueprint View
data class BlockConfig(
    val name: String,
    val color: Color,
    val textColor: Color = Color.White
)

val blockMap = mapOf(
    "P" to BlockConfig("Oak Planks", Color(0xFFC4AB80), Color(0xFF4E3629)),
    "C" to BlockConfig("Cobblestone", Color(0xFF6F6F6F)),
    "S" to BlockConfig("Smooth Stone", Color(0xFF989898)),
    "O" to BlockConfig("Oak Logs", Color(0xFF533F2E)),
    "G" to BlockConfig("Glass Pane", Color(0xFFB9EBFF), Color(0xFF1E5670)),
    "D" to BlockConfig("Dirt Block", Color(0xFF7D593E)),
    "W" to BlockConfig("Water Block", Color(0xFF2E6EE6)),
    "L" to BlockConfig("Lava Source", Color(0xFFFF4D00)),
    "R" to BlockConfig("Redstone Dust", Color(0xFFFF2200)),
    "T" to BlockConfig("Redstone Torch", Color(0xFFFFA200), Color(0xFF421D00)),
    "B" to BlockConfig("Redstone Block", Color(0xFFDA2222)),
    "I" to BlockConfig("Iron Block", Color(0xFFE4E9EC), Color(0xFF3B444C)),
    "Y" to BlockConfig("Obsidian", Color(0xFF1F102F)),
    "V" to BlockConfig("Villager Host", Color(0xFFB18060)),
    "Z" to BlockConfig("Zombie Spawn", Color(0xFF388E3C)),
    "H" to BlockConfig("Hopper Input", Color(0xFF3C3C3C)),
    "M" to BlockConfig("Magma Core", Color(0xFFB74E06)),
    "E" to BlockConfig("Emerald Block", Color(0xFF00C853)),
    "K" to BlockConfig("Sticky Piston", Color(0xFF4C7E4F)),
    "." to BlockConfig("Air / Clear Space", Color.Transparent)
)

// Minecraft & Bento Hybrid Colors
val MinecraftGreen = Color(0xFF4A7227)
val MinecraftGreenLight = Color(0xFF74B72E) // Bento Prime Green
val MinecraftDarkBackground = Color(0xFF121212) // Bento Dark Canvas
val MinecraftStoneGray = Color(0xFF1E1E1E) // Bento Slate Stone
val MinecraftDirtBrown = Color(0xFF573D26)
val MinecraftWoodBrown = Color(0xFF866043)
val MinecraftGold = Color(0xFFFFD700)
val MinecraftGoldDark = Color(0xFFB8860B)
val LightAccentColor = Color(0xFFE0E0E0)

// Bento Grid specific parameters
val BentoCardBg = Color(0xFF252525)
val BentoBorder = Color.White.copy(alpha = 0.05f)
val BentoBorderLight = Color.White.copy(alpha = 0.1f)
val BentoCyan = Color(0xFF55FFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    buildRepository: BuildRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Navigation and Tab States
    var currentTab by remember { mutableStateOf("home") } // home, gallery, favorites, leaderboard, profile
    
    // Search & Generation Prompt states
    var promptInput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var searchCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    // Detail modal states
    var selectedBuild by remember { mutableStateOf<MinecraftBuild?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // User Profile context
    var userEmail by remember { mutableStateOf("valko.arthur@gmail.com") }
    var userName by remember { mutableStateOf("Arthur Valko") }
    var isPremium by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    // Active screen flow lists
    val allBuildsList by buildRepository.allBuilds.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoritesList by buildRepository.favoriteBuilds.collectAsStateWithLifecycle(initialValue = emptyList())
    val galleryList by buildRepository.sharedBuilds.collectAsStateWithLifecycle(initialValue = emptyList())

    // Admin state numbers (purged ids logging to show admin controls working)
    var purgedBuildsCount by remember { mutableStateOf(0) }

    // Example Prompt chips
    val examples = remember {
        listOf(
            "Modern beach villa",
            "Survival starter base",
            "Medieval watchtower",
            "Redstone automatic sorter",
            "Underground obsidian bunker",
            "Skyblock starter tent",
            "Pixel art creeper face"
        )
    }

    // Categories list
    val categories = remember {
        listOf(
            "Houses",
            "Castles",
            "Farms",
            "Redstone",
            "Survival Bases",
            "Mega Builds",
            "PvP Builds",
            "Pixel Art"
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MinecraftDarkBackground),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Custom Bento-styled double-bordered wooden/grass block representation
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF5E8A31), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color(0xFF3A551E)), RoundedCornerShape(8.dp))
                                .padding(bottom = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color(0xFFD9D9D9))
                                    .border(1.dp, Color(0xFF0F172A))
                            )
                        }
                        
                        Column {
                            Text(
                                text = "AI Builder",
                                fontWeight = FontWeight.ExtraBold,
                                color = MinecraftGreenLight,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "MINECRAFT EDITION",
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    val initials = userName.split(" ")
                        .mapNotNull { it.firstOrNull() }
                        .joinToString("")
                        .uppercase()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (isPremium) {
                            Badge(
                                containerColor = MinecraftGold,
                                contentColor = Color.Black,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text("PRO", fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                            }
                        } else {
                            Button(
                                onClick = { isPremium = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MinecraftGold, contentColor = Color.Black),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Star, "Premium icon", modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("GO PRO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Round user initials avatar with click listener to go to profile settings
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1E293B))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(18.dp))
                                .clickable { currentTab = "profile" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (initials.isNotEmpty()) initials else "AV",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MinecraftStoneGray,
                contentColor = Color.White,
                windowInsets = WindowInsets.navigationBars
            ) {
                listOf(
                    Triple("home", Icons.Default.Construction, "Architect"),
                    Triple("gallery", Icons.Default.Public, "Gallery"),
                    Triple("favorites", Icons.Default.Favorite, "Saved"),
                    Triple("leaderboard", Icons.Default.Leaderboard, "Ranks"),
                    Triple("profile", Icons.Default.Person, "Profile")
                ).forEach { (tab, icon, title) ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (selected) {
                                    if (isPremium) MinecraftGold else MinecraftGreenLight
                                } else Color.LightGray
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else Color.LightGray,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MinecraftDarkBackground
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MinecraftDarkBackground, Color(0xFF121212))
                    )
                )
        ) {
            // Main views switcher
            when (currentTab) {
                "home" -> HomeScreenView(
                    promptInput = promptInput,
                    onPromptChange = { promptInput = it },
                    examples = examples,
                    categories = categories,
                    isGenerating = isGenerating,
                    allBuilds = allBuildsList,
                    isPremium = isPremium,
                    onGenerate = { prompt ->
                        if (isGenerating) return@HomeScreenView
                        if (prompt.isBlank()) {
                            Toast.makeText(context, "Please write a building idea first!", Toast.LENGTH_SHORT).show()
                            return@HomeScreenView
                        }
                        
                        // Limit on non-premium
                        val generatedCount = allBuildsList.count { it.creatorEmail == "ai@minecraft.net" }
                        if (!isPremium && generatedCount >= 3) {
                            Toast.makeText(context, "Pro limit reached! Upgrade to PRO for unlimited generations.", Toast.LENGTH_LONG).show()
                            currentTab = "profile"
                            return@HomeScreenView
                        }

                        coroutineScope.launch {
                            isGenerating = true
                            val generated = GeminiService.generateMinecraftBuild(prompt)
                            isGenerating = false
                            if (generated != null) {
                                // Dynamic tag categories matching
                                val assignedCategory = categories.firstOrNull {
                                    prompt.contains(it, ignoreCase = true) || 
                                    generated.description.contains(it, ignoreCase = true) || 
                                    generated.title.contains(it, ignoreCase = true)
                                } ?: "Houses"

                                val finalizedBuild = generated.copy(
                                    category = assignedCategory,
                                    creatorEmail = userEmail,
                                    creatorName = userName.split(" ").firstOrNull() ?: "Stevie"
                                )
                                val newId = buildRepository.saveBuild(finalizedBuild)
                                val freshlyLoaded = buildRepository.getBuildById(newId.toInt())
                                selectedBuild = freshlyLoaded ?: finalizedBuild.copy(id = newId.toInt())
                                showDetailDialog = true
                                promptInput = ""
                            } else {
                                Toast.makeText(context, "Failed to connect to AI builders. Ensure your Gemini key is set in AI Studio secrets!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onSelectBuild = {
                        selectedBuild = it
                        showDetailDialog = true
                    },
                    onNavigateToTab = { tab, cat ->
                        currentTab = tab
                        if (cat != null) {
                            searchCategory = cat
                        }
                    }
                )

                "gallery" -> PublicGalleryView(
                    galleryList = galleryList,
                    categories = categories,
                    searchCategory = searchCategory,
                    onCategoryChange = { searchCategory = it },
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSelectBuild = {
                        selectedBuild = it
                        showDetailDialog = true
                    },
                    onLikeBuild = { build ->
                        coroutineScope.launch {
                            val updated = build.copy(
                                likesCount = if (build.userLiked) build.likesCount - 1 else build.likesCount + 1,
                                userLiked = !build.userLiked
                            )
                            buildRepository.updateBuild(updated)
                        }
                    },
                    onSaveBuild = { build ->
                        coroutineScope.launch {
                            val updated = build.copy(isFavorite = true)
                            buildRepository.updateBuild(updated)
                            Toast.makeText(context, "Saved securely to offline favorites!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                "favorites" -> SavedFavoritesView(
                    favoritesList = favoritesList,
                    onSelectBuild = {
                        selectedBuild = it
                        showDetailDialog = true
                    },
                    onRemoveFavorite = { build ->
                        coroutineScope.launch {
                            val updated = build.copy(isFavorite = false)
                            buildRepository.updateBuild(updated)
                            Toast.makeText(context, "Removed from local favorite bookmarks.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                "leaderboard" -> LeaderboardsView(
                    galleryList = galleryList,
                    onSelectBuild = {
                        selectedBuild = it
                        showDetailDialog = true
                    }
                )

                "profile" -> UserAccountsProfileView(
                    userEmail = userEmail,
                    userName = userName,
                    isPremium = isPremium,
                    isAdmin = isAdmin,
                    generationsCount = allBuildsList.count { it.creatorEmail == userEmail },
                    onTogglePremium = { isPremium = it },
                    onToggleAdmin = { isAdmin = it },
                    onUpdateUser = { email, name ->
                        userEmail = email
                        userName = name
                    },
                    purgedBuildsCount = purgedBuildsCount,
                    onPurgeDatabase = {
                        coroutineScope.launch {
                            // Erase AI generated items to show admin panel active
                            var count = 0
                            allBuildsList.forEach {
                                if (it.creatorEmail == userEmail || it.creatorName == "AI Builder") {
                                    buildRepository.deleteBuild(it)
                                    count++
                                }
                            }
                            purgedBuildsCount += count
                            Toast.makeText(context, "Admin action completed. Cleaned $count records.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // Spinner Generation Overlays
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .border(BorderStroke(2.dp, MinecraftGreenLight), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MinecraftGreenLight,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "AI MINECRAFT GENIUS AT WORK...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Carving columns, loading redstone, aligning blocks, and calculating perfect side layers for your project.",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Detail and Blueprints Modal Dialog
            if (showDetailDialog && selectedBuild != null) {
                BuildDetailModal(
                    build = selectedBuild!!,
                    isPremium = isPremium,
                    onDismiss = { showDetailDialog = false },
                    onToggleFavorite = {
                        coroutineScope.launch {
                            val updated = selectedBuild!!.copy(isFavorite = !selectedBuild!!.isFavorite)
                            buildRepository.updateBuild(updated)
                            selectedBuild = updated
                        }
                    },
                    onShareToGallery = {
                        coroutineScope.launch {
                            val updated = selectedBuild!!.copy(isShared = true)
                            buildRepository.updateBuild(updated)
                            selectedBuild = updated
                            Toast.makeText(context, "Uploaded successfully to the build gallery!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

// ------------------------------------
// 1. HOME SCREEN VIEW — BENTO GRID PATTERN
// ------------------------------------
@Composable
fun HomeScreenView(
    promptInput: String,
    onPromptChange: (String) -> Unit,
    examples: List<String>,
    categories: List<String>,
    isGenerating: Boolean,
    allBuilds: List<MinecraftBuild>,
    isPremium: Boolean,
    onGenerate: (String) -> Unit,
    onSelectBuild: (MinecraftBuild) -> Unit,
    onNavigateToTab: (String, String?) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bento Spec 1: Featured Blueprint (Steampunk Airship or Gladiator Arena)
        item {
            val featuredBuild = allBuilds.find { it.title.contains("Gladiator", ignoreCase = true) } 
                ?: allBuilds.firstOrNull()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (featuredBuild != null) {
                            onSelectBuild(featuredBuild)
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF373737), Color(0xFF1A1A1A))
                            )
                        )
                        .border(BorderStroke(1.dp, BentoBorderLight), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEW BLUEPRINT",
                                color = MinecraftGreenLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(MinecraftGold, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "PREMIUM",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = featuredBuild?.title ?: "Steampunk Airship",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "A massive canvas of floating wood gears, clockwork turbines, and premium TNT cannons.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Overlapping face avatar stack simulator with counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row {
                                val colors = listOf(Color(0xFF4E7728), Color(0xFF55FFFF), Color(0xFFFFD700))
                                colors.forEachIndexed { i, c ->
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .offset(x = (-i * 6).dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(c)
                                            .border(1.dp, Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
                                    )
                                }
                            }
                            Text(
                                text = "1.2k master builders crafting this",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.offset(x = (-12).dp)
                            )
                        }
                    }
                }
            }
        }

        // Bento Spec 2: Prompt Input Area
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "DESCRIBE YOUR SCHEMATIC",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "What should we build today?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = onPromptChange,
                        placeholder = { Text("Describe a castle base, redstone elevator, or warm hotel room...", color = Color.DarkGray, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("prompt_input_field"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MinecraftGreenLight,
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedContainerColor = Color(0xFF121212),
                            unfocusedContainerColor = Color(0xFF121212)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { onGenerate(promptInput) },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_blueprint_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPremium) MinecraftGold else MinecraftGreenLight,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, "Auto icon", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isPremium) "CARVE PREMIUM BLUEPRINT" else "CARVE BLUEPRINT (Free)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Bento Spec 3: Fast Action Stats / Saved Split Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Statistical Count Bento Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "TOTAL BLUEPRINTS",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${allBuilds.size + 120}",
                            color = BentoCyan,
                            fontWeight = FontWeight.Light,
                            fontSize = 32.sp
                        )
                        Text(
                            text = "Ready to build layers",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                // Interactive Bookmarks Bento Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab("favorites", null) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Liked",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "SAVED BOOKMARKS",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        val favCount = allBuilds.count { it.isFavorite }
                        Text(
                            text = "$favCount items",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(MinecraftGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, MinecraftGreenLight), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MANAGE MAPS",
                                color = MinecraftGreenLight,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Bento Spec 4: Bento Categories Layout Container
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK CATEGORIES",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.clickable { onNavigateToTab("gallery", "All") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View All",
                                color = MinecraftGreenLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Go",
                                tint = MinecraftGreenLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 4-Column aspect ratio category bento grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tiles = listOf(
                            Triple("🏰", "Castles", "Castles"),
                            Triple("🏠", "Houses", "Houses"),
                            Triple("⚙️", "Redstone", "Redstone"),
                            Triple("🌲", "Survival", "Survival Bases")
                        )

                        tiles.forEach { (emoji, label, catName) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { onNavigateToTab("gallery", catName) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                                border = BorderStroke(1.dp, BentoBorder)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(6.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Examples / Hot Prompts Scroll
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🔥 HOT BLUEPRINT DESIGN PROMPTS",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(examples) { prompt ->
                        SuggestionChip(
                            onClick = { onPromptChange(prompt) },
                            label = { Text(prompt, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF1E1E1E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF333333))
                        )
                    }
                }
            }
        }

        // Generated Recent Schematics Header
        item {
            Text(
                "📦 RECENT SHIPMENTS / GENERATIONS",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }

        if (allBuilds.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BentoCardBg, RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, BentoBorder), RoundedCornerShape(16.dp))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.HelpOutline,
                            "None",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No customized schematics generated yet.",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Submit an aesthetic idea to test your AI master builder!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(allBuilds) { build ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBuild(build) }
                        .testTag("recent_build_card_${build.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray),
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = build.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                DifficultyBadge(build.difficulty)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = build.description,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "⚡ Time: ${build.estimatedTime}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    "📏 Size: ${build.dimensions}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = MinecraftGreenLight
                        )
                    }
                }
            }
        }
    }
}

// Helper Difficulty Badge
@Composable
fun DifficultyBadge(difficulty: String) {
    val color = when (difficulty.lowercase()) {
        "easy" -> Color(0xFF2E7D32)
        "medium" -> Color(0xFFEF6C00)
        else -> Color(0xFFC62828)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, color), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = difficulty.uppercase(),
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ------------------------------------
// 2. PUBLIC GALLERY VIEW
// ------------------------------------
@Composable
fun PublicGalleryView(
    galleryList: List<MinecraftBuild>,
    categories: List<String>,
    searchCategory: String,
    onCategoryChange: (String) -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSelectBuild: (MinecraftBuild) -> Unit,
    onLikeBuild: (MinecraftBuild) -> Unit,
    onSaveBuild: (MinecraftBuild) -> Unit
) {
    val filteredList = remember(galleryList, searchCategory, searchQuery) {
        galleryList.filter { build ->
            val matchesCategory = searchCategory == "All" || build.category == searchCategory
            val matchesQuery = searchQuery.isBlank() || 
                    build.title.contains(searchQuery, ignoreCase = true) || 
                    build.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gallery_view_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search line
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search blueprints...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, "Search icon", tint = Color.LightGray) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gallery_search_bars"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MinecraftGreenLight,
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = MinecraftStoneGray,
                unfocusedContainerColor = MinecraftStoneGray
            ),
            singleLine = true
        )

        // Categories list horizontal
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = searchCategory == "All",
                    onClick = { onCategoryChange("All") },
                    label = { Text("All Categories", color = Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MinecraftGreenLight,
                        containerColor = MinecraftStoneGray
                    )
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = searchCategory == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat, color = Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MinecraftGreenLight,
                        containerColor = MinecraftStoneGray
                    )
                )
            }
        }

        // Shared feeds list
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudQueue, "Cloud", tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No public builds match this criteria.", color = Color.Gray)
                    Text("Generate a build and hit 'Share to Gallery'!", color = Color.DarkGray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { build ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBuild(build) }
                            .testTag("gallery_build_${build.id}"),
                        colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header matching
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MinecraftGreen, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = build.creatorName.firstOrNull()?.toString()?.uppercase() ?: "S",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Column {
                                        Text(build.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("By ${build.creatorName} • ${build.category}", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                                DifficultyBadge(build.difficulty)
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(build.description, color = Color.LightGray, fontSize = 13.sp)

                            Spacer(Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📏 ${build.dimensions}", color = Color.Gray, fontSize = 11.sp)
                                Text("⏰ Build time: ${build.estimatedTime}", color = Color.Gray, fontSize = 11.sp)
                            }

                            Spacer(Modifier.height(12.dp))
                            Divider(color = Color.DarkGray)
                            Spacer(Modifier.height(8.dp))

                            // Interactive Like & Save bars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onLikeBuild(build) }
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (build.userLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like Button",
                                        tint = if (build.userLiked) Color.Red else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${build.likesCount} Liked",
                                        color = if (build.userLiked) Color.Red else Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onSaveBuild(build) }
                                        .padding(8.dp)
                                ) {
                                    Icon(Icons.Default.Download, "Save to local Favorites", tint = MinecraftGreenLight, modifier = Modifier.size(18.dp))
                                    Text("Download Blueprint", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------
// 3. SAVED FAVORITES VIEW
// ------------------------------------
@Composable
fun SavedFavoritesView(
    favoritesList: List<MinecraftBuild>,
    onSelectBuild: (MinecraftBuild) -> Unit,
    onRemoveFavorite: (MinecraftBuild) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("favorites_view_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "💾 OFFLINE SAVED BLUEPRINTS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        if (favoritesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, "Offline saved folder empty", tint = Color.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No saved blueprints found offline.", color = Color.LightGray)
                    Text("Tapping the download/save heart icon stores guides forever.", color = Color.DarkGray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoritesList) { build ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBuild(build) }
                            .testTag("favorite_card_${build.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(build.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(MinecraftGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .border(BorderStroke(1.dp, MinecraftGreenLight.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(build.category, color = MinecraftGreenLight, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("📏 Size: ${build.dimensions} • Estimated: ${build.estimatedTime}", color = Color.LightGray, fontSize = 12.sp)
                            }
                            IconButton(onClick = { onRemoveFavorite(build) }) {
                                Icon(Icons.Default.DeleteOutline, "Remove", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------
// 4. LEADERBOARDS VIEW
// ------------------------------------
@Composable
fun LeaderboardsView(
    galleryList: List<MinecraftBuild>,
    onSelectBuild: (MinecraftBuild) -> Unit
) {
    val rankingList = remember(galleryList) {
        galleryList.sortedByDescending { it.likesCount }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboards_view_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🏆 WORLD LEADERBOARDS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "🔥 Trending Builders",
                    color = MinecraftGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                listOf(
                    Pair("Arthur Valko", "4 generated • Tier: Master"),
                    Pair("RedstoneKing", "18 generated • Tier: Redstone Legend"),
                    Pair("PixelQueen", "5 generated • Tier: Decor Expert")
                ).forEachIndexed { i, builder ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("#${i + 1}", color = MinecraftGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(builder.first, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(builder.second, color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }

        Text("✨ MOST LIKED BLUEPRINTS OVERALL", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)

        if (rankingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Seed blueprints are being registered on ranks...", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(rankingList) { index, build ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBuild(build) },
                        colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (index < 3) MinecraftGold else Color.DarkGray,
                                            RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (index < 3) Color.Black else Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                                Column {
                                    Text(build.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("By ${build.creatorName} in ${build.category}", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Favorite, "Likes", tint = Color.Red, modifier = Modifier.size(16.dp))
                                Text("${build.likesCount}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------
// 5. USER PROFILE & ADMIN VIEW
// ------------------------------------
@Composable
fun UserAccountsProfileView(
    userEmail: String,
    userName: String,
    isPremium: Boolean,
    isAdmin: Boolean,
    generationsCount: Int,
    onTogglePremium: (Boolean) -> Unit,
    onToggleAdmin: (Boolean) -> Unit,
    onUpdateUser: (String, String) -> Unit,
    purgedBuildsCount: Int,
    onPurgeDatabase: () -> Unit
) {
    var editName by remember { mutableStateOf(userName) }
    var editEmail by remember { mutableStateOf(userEmail) }
    var isEditing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_view_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🏹 MINECRAFT PROFILE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Profile details
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            2.dp,
                            if (isPremium) MinecraftGold else Color.Transparent
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Steve pixel custom avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MinecraftGreen, RoundedCornerShape(8.dp))
                                .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👾",
                                fontSize = 32.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(userEmail, color = Color.LightGray, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isPremium) {
                                    Badge(containerColor = MinecraftGold, contentColor = Color.Black) {
                                        Text("PREMIUM ARCHITECT", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Badge(containerColor = Color.DarkGray, contentColor = Color.White) {
                                        Text("FREE ACCOUNT")
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color.DarkGray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GENERATED", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$generationsCount builds", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color.DarkGray)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BUILDER TIER", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val tierName = when {
                                generationsCount >= 6 -> "Minecraft God"
                                generationsCount >= 3 -> "Expert Crafter"
                                else -> "Survival Novice"
                            }
                            Text(tierName, color = if (isPremium) MinecraftGold else MinecraftGreenLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Configuration edit form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Modify Builder Credentials", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Gamertag Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedContainerColor = MinecraftDarkBackground,
                                unfocusedContainerColor = MinecraftDarkBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Contact") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedContainerColor = MinecraftDarkBackground,
                                unfocusedContainerColor = MinecraftDarkBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onUpdateUser(editEmail, editName)
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MinecraftGreenLight)
                            ) {
                                Text("Save Credentials")
                            }
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancel", color = Color.LightGray)
                            }
                        }
                    } else {
                        Text("Verify/Swap your connected Minecraft account.", color = Color.LightGray, fontSize = 12.sp)
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Icon(Icons.Default.Edit, "Edit icon", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Account Details", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // PREMIUM MEMBERSHIP TOGGLER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.7f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "👑 PRO Member Features",
                            color = MinecraftGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Unlock infinite generations, custom side measurements and advanced material lists.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isPremium,
                        onCheckedChange = onTogglePremium,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MinecraftGold,
                            checkedTrackColor = MinecraftGold.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        // ADMIN ACCESS PANEL
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🛡️ Admin Panel Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Access special content filtering and user settings.", color = Color.LightGray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isAdmin,
                            onCheckedChange = onToggleAdmin
                        )
                    }

                    if (isAdmin) {
                        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 6.dp))
                        Text("Live Server Analytics", fontWeight = FontWeight.Bold, color = MinecraftGreenLight, fontSize = 12.sp)
                        Text("Total Database Connections: Active", fontSize = 11.sp, color = Color.LightGray)
                        Text("Registered blueprints: Safe content validated", fontSize = 11.sp, color = Color.LightGray)
                        Text("Purged offensive items: $purgedBuildsCount records", fontSize = 11.sp, color = Color.LightGray)

                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = onPurgeDatabase,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                        ) {
                            Text("Purge generated builds", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------
// 6. BUILD DETAIL AND LAYER-BY-LAYER BLUEPRINT MODAL
// ------------------------------------
@Composable
fun BuildDetailModal(
    build: MinecraftBuild,
    isPremium: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShareToGallery: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("details") } // details, instructions, blueprints
    var currentLayerIndex by remember { mutableStateOf(0) }
    var sideViewIsTop by remember { mutableStateOf(true) } // True = Top View, False = Side View

    // Parse layersJson 3D grid layout
    val layersMatrix = remember(build.layersJson) {
        val list = mutableListOf<List<List<String>>>()
        try {
            val rootArray = JSONArray(build.layersJson)
            for (y in 0 until rootArray.length()) {
                val layerArr = rootArray.getJSONArray(y)
                val layerList = mutableListOf<List<String>>()
                for (x in 0 until layerArr.length()) {
                    val rowArr = layerArr.getJSONArray(x)
                    val rowList = mutableListOf<String>()
                    for (z in 0 until rowArr.length()) {
                        rowList.add(rowArr.getString(z))
                    }
                    layerList.add(rowList)
                }
                list.add(layerList)
            }
        } catch (e: JSONException) {
            Log.e("BuildDetail", "JSON parsing failure: ${e.message}")
            // Fallback basic layer
            list.add(listOf(listOf("C", "C"), listOf("C", "C")))
        }
        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            color = MinecraftDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(16.dp)
            ) {
                // Header Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MinecraftStoneGray)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(build.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            DifficultyBadge(build.difficulty)
                        }
                        Text("Category: ${build.category}", color = Color.LightGray, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close modal", tint = Color.White)
                    }
                }

                // Sub headers Navigation tabs
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "instructions" -> 1
                        "blueprints" -> 2
                        else -> 0
                    },
                    containerColor = MinecraftStoneGray,
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedTab == "details",
                        onClick = { selectedTab = "details" },
                        text = { Text("Details", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == "instructions",
                        onClick = { selectedTab = "instructions" },
                        text = { Text("Steps", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == "blueprints",
                        onClick = { selectedTab = "blueprints" },
                        text = { Text("Blueprints Grid", fontWeight = FontWeight.Bold) }
                    )
                }

                // Main Tab Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        "details" -> DetailsTabContent(
                            build = build,
                            onToggleFavorite = onToggleFavorite,
                            onShareToGallery = onShareToGallery
                        )

                        "instructions" -> InstructionsTabContent(
                            build = build
                        )

                        "blueprints" -> BlueprintsTabContent(
                            layersMatrix = layersMatrix,
                            currentLayerIndex = currentLayerIndex,
                            onLayerSelected = { currentLayerIndex = it },
                            sideViewIsTop = sideViewIsTop,
                            onToggleViewMode = { sideViewIsTop = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsTabContent(
    build: MinecraftBuild,
    onToggleFavorite: () -> Unit,
    onShareToGallery: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 Description", color = MinecraftGreenLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(build.description, color = Color.White, fontSize = 13.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⏰ Build Time", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(build.estimatedTime, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📐 Dimensions", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(build.dimensions, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📋 Required Materials Checklist", color = MinecraftGreenLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val matsList = build.materials.split(",")
                    matsList.forEach { mat ->
                        if (mat.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, "Unchecked box", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Text(mat.trim(), color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinecraftStoneGray)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("💡 Expert Builder Tip", color = MinecraftGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(build.tips, color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleFavorite,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (build.isFavorite) Color.Red else MinecraftGreen
                    )
                ) {
                    Icon(
                        imageVector = if (build.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save favorite"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = if (build.isFavorite) "Saved offline" else "Save offline", fontWeight = FontWeight.Bold)
                }

                if (!build.isShared) {
                    Button(
                        onClick = onShareToGallery,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MinecraftGold, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Share, "Share icon")
                        Spacer(Modifier.width(8.dp))
                        Text("Share to feed", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InstructionsTabContent(build: MinecraftBuild) {
    val steps = remember(build.instructions) {
        build.instructions.split("\n")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "🏁 BUILD PROGRESS CHECKLIST",
                color = MinecraftGreenLight,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        items(steps) { step ->
            if (step.isNotBlank()) {
                // Parse step number vs content
                val stepNum = step.substringBefore(".", "").trim()
                val stepDesc = step.substringAfter(".", step).trim()
                
                var isChecked by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChecked = !isChecked },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) MinecraftGreen.copy(alpha = 0.2f) else MinecraftStoneGray
                    ),
                    border = BorderStroke(1.dp, if (isChecked) MinecraftGreen else Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MinecraftGreenLight,
                                uncheckedColor = Color.DarkGray
                            )
                        )
                        Column {
                            if (stepNum.isNotBlank()) {
                                Text(
                                    "STEP $stepNum",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) MinecraftGreenLight else Color.LightGray
                                )
                            }
                            Text(
                                stepDesc,
                                color = if (isChecked) Color.Gray else Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlueprintsTabContent(
    layersMatrix: List<List<List<String>>>,
    currentLayerIndex: Int,
    onLayerSelected: (Int) -> Unit,
    sideViewIsTop: Boolean,
    onToggleViewMode: (Boolean) -> Unit
) {
    val selectedMatrixLayer = layersMatrix.getOrNull(currentLayerIndex) ?: emptyList()

    // Calculate maximum row/cols count
    val rowsCount = selectedMatrixLayer.size
    val colsCount = selectedMatrixLayer.firstOrNull()?.size ?: 0

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toggle blueprint view Top / Side view
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Layer Selection (${currentLayerIndex + 1}/${layersMatrix.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { onToggleViewMode(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sideViewIsTop) MinecraftGreenLight else Color.DarkGray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Top View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onToggleViewMode(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!sideViewIsTop) MinecraftGreenLight else Color.DarkGray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Side Slice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Layer selection steppers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { if (currentLayerIndex > 0) onLayerSelected(currentLayerIndex - 1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.weight(1f)
            ) {
                Text("DOWN LAYER")
            }
            Box(
                modifier = Modifier
                    .background(MinecraftStoneGray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Y = $currentLayerIndex", fontWeight = FontWeight.Bold, color = MinecraftGold, fontSize = 14.sp)
            }
            Button(
                onClick = { if (currentLayerIndex < layersMatrix.size - 1) onLayerSelected(currentLayerIndex + 1) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.weight(1f)
            ) {
                Text("UP LAYER")
            }
        }

        // Main Rendering Blueprint Block Canvas Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(BorderStroke(1.dp, Color.DarkGray), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = MinecraftDarkBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (sideViewIsTop) {
                    // Standard Top-down projection
                    if (selectedMatrixLayer.isEmpty()) {
                        Text("No data", color = Color.Gray)
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (rowIndex in 0 until rowsCount) {
                                val row = selectedMatrixLayer[rowIndex]
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (colIndex in 0 until row.size) {
                                        val cellType = row[colIndex]
                                        BlockMapCell(cellType = cellType)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Side slices vertical representation projection (using X rows vs Y layers)
                    // Rows are vertical layers, elements are columns slice
                    val sideSliceColsCount = layersMatrix.firstOrNull()?.firstOrNull()?.size ?: 0
                    val sideSliceHeight = layersMatrix.size

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (y in (sideSliceHeight - 1) downTo 0) {
                            val activeLayer = layersMatrix[y]
                            // View a middle row X slice projection
                            val activeRowIndex = activeLayer.size / 2
                            val activeRow = activeLayer.getOrNull(activeRowIndex) ?: emptyList()

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (z in 0 until sideSliceColsCount) {
                                    val cellType = activeRow.getOrNull(z) ?: "."
                                    BlockMapCell(cellType = cellType)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Legend of active block symbols
        Text("Block Legend", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val uniqueList = if (sideViewIsTop) {
                selectedMatrixLayer.flatten().distinct().filter { it != "." }
            } else {
                layersMatrix.flatMap { it.flatMap { it } }.distinct().filter { it != "." }
            }

            if (uniqueList.isEmpty()) {
                item { Text("Air/Vacuum only", color = Color.DarkGray, fontSize = 11.sp) }
            } else {
                items(uniqueList) { char ->
                    val block = blockMap[char] ?: BlockConfig("Block $char", Color.Gray)
                    Row(
                        modifier = Modifier
                            .background(MinecraftStoneGray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(block.color, RoundedCornerShape(2.dp))
                        )
                        Text(block.name, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BlockMapCell(cellType: String) {
    val block = remember(cellType) {
        blockMap[cellType] ?: BlockConfig("Unknown", Color.Magenta)
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (cellType == ".") Color(0xFF222222) else block.color)
            .border(
                BorderStroke(
                    1.dp,
                    if (cellType == ".") Color(0xFF333333) else block.color.copy(alpha = 0.5f)
                ),
                RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cellType != ".") {
            Text(
                text = cellType,
                color = block.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
