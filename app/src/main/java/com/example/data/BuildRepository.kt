package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BuildRepository(private val buildDao: BuildDao) {

    val allBuilds: Flow<List<MinecraftBuild>> = buildDao.getAllBuilds()
    val favoriteBuilds: Flow<List<MinecraftBuild>> = buildDao.getFavoriteBuilds()
    val sharedBuilds: Flow<List<MinecraftBuild>> = buildDao.getSharedBuilds()

    fun getSharedByCategory(category: String): Flow<List<MinecraftBuild>> {
        return buildDao.getSharedBuildsByCategory(category)
    }

    suspend fun getBuildById(id: Int): MinecraftBuild? {
        return buildDao.getBuildById(id)
    }

    suspend fun saveBuild(build: MinecraftBuild): Long {
        return buildDao.insertBuild(build)
    }

    suspend fun updateBuild(build: MinecraftBuild) {
        buildDao.updateBuild(build)
    }

    suspend fun deleteBuild(build: MinecraftBuild) {
        buildDao.deleteBuild(build)
    }

    suspend fun deleteBuildById(id: Int) {
        buildDao.deleteBuildById(id)
    }

    suspend fun getCountByCreator(email: String): Int {
        return buildDao.getBuildsCountByCreator(email)
    }

    // Check if the database needs initial seeding of Minecraft designs
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val count = buildDao.getTotalBuildsCount()
        if (count == 0) {
            val seeds = getSeedBuilds()
            for (build in seeds) {
                buildDao.insertBuild(build)
            }
        }
    }

    private fun getSeedBuilds(): List<MinecraftBuild> {
        val list = mutableListOf<MinecraftBuild>()

        // 1. Cozy Starter Cabin
        list.add(
            MinecraftBuild(
                title = "Cozy Starter Cabin",
                description = "An aesthetic wood and stone survival shelter. Elevated with a stone base and glass panorama windows. Quick to build and highly resource-safe.",
                category = "Houses",
                difficulty = "Easy",
                estimatedTime = "25 Mins",
                materials = "Oak Logs: 32, Oak Planks: 64, Glass Panes: 16, Cobblestone: 48, Wooden Door: 1, Torch: 8",
                dimensions = "6 x 6 x 4",
                instructions = "1. Place Cobblestone in a 6x6 square foundation.\n2. Erect Oak Logs 4 blocks high on all four corners.\n3. Fill walls with Oak Planks, leaving 2x1 openings for windows on the sides and center-front for the wooden door.\n4. Complete the flat roof using Oak Planks and decorate corners with Torches.",
                tips = "Extend this cabin with a sub-level basement to place down your initial storage canisters and furnace arrays.",
                layersJson = create3DGridJson(
                    listOf(
                        // L1 Foundation
                        listOf(
                            listOf("C", "C", "C", "C", "C", "C"),
                            listOf("C", "P", "P", "P", "P", "C"),
                            listOf("C", "P", "P", "P", "P", "C"),
                            listOf("C", "P", "P", "P", "P", "C"),
                            listOf("C", "P", "P", "P", "P", "C"),
                            listOf("C", "C", "C", "C", "C", "C")
                        ),
                        // L2 Walls
                        listOf(
                            listOf("O", "P", "P", "P", "P", "O"),
                            listOf("P", ".", ".", ".", ".", "P"),
                            listOf("P", ".", ".", ".", ".", "P"),
                            listOf("P", ".", ".", ".", ".", "P"),
                            listOf("P", ".", ".", ".", ".", "P"),
                            listOf("O", "P", "D", "P", "P", "O")
                        ),
                        // L3 Windows
                        listOf(
                            listOf("O", "G", "G", "G", "G", "O"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("O", "G", ".", "G", "G", "O")
                        ),
                        // L4 Roof
                        listOf(
                            listOf("P", "P", "P", "P", "P", "P"),
                            listOf("P", "P", "P", "P", "P", "P"),
                            listOf("P", "P", "P", "P", "P", "P"),
                            listOf("P", "P", "P", "P", "P", "P"),
                            listOf("P", "P", "P", "P", "P", "P"),
                            listOf("P", "P", "P", "P", "P", "P")
                        )
                    )
                ),
                creatorName = "GamerSteve",
                creatorEmail = "valko.arthur@gmail.com",
                isShared = true,
                likesCount = 234,
                userLiked = false
            )
        )

        // 2. Automated Iron Farm
        list.add(
            MinecraftBuild(
                title = "Automated Iron Farm",
                description = "Spawn Golems reliably using three villagers and a zombie. Fully scalable hopper collector that funnels freshly made iron ingots automatically.",
                category = "Farms",
                difficulty = "Medium",
                estimatedTime = "50 Mins",
                materials = "Glass Blocks: 120, Cheste: 4, Hoppers: 9, Water Buckets: 2, Lava Bucket: 1, Beds: 3, Name Tag: 1, Zombie Egg: 1, Cobblestone slabs: 32",
                dimensions = "7 x 7 x 6",
                instructions = "1. Build an elevated villager platform 8 blocks above ground using Grass or Planks.\n2. Design three containment sleeping units for villagers.\n3. Build a central pit for a Zombie fitted with a Name Tag to avoid despawning.\n4. Erect a water-covered collection floor lined with Lava sign tags at height level 3 to funnel Golems into Hopper collectors.",
                tips = "Ensure no blocks within 8 blocks of the farm allow Golem spawns. Pre-carpet all neighboring high points.",
                layersJson = create3DGridJson(
                    listOf(
                        // L1: Platform
                        listOf(
                            listOf("I", "I", "I", "I", "I", "I", "I"),
                            listOf("I", ".", ".", ".", ".", ".", "I"),
                            listOf("I", ".", "B", "B", "B", ".", "I"),
                            listOf("I", ".", "B", "Z", "B", ".", "I"),
                            listOf("I", ".", "B", "B", "B", ".", "I"),
                            listOf("I", ".", ".", ".", ".", ".", "I"),
                            listOf("I", "I", "I", "I", "I", "I", "I")
                        ),
                        // L2: Villager beds & containments
                        listOf(
                            listOf("G", "G", "G", "G", "G", "G", "G"),
                            listOf("G", "V", ".", ".", ".", "V", "G"),
                            listOf("G", ".", "B", "B", "B", ".", "G"),
                            listOf("G", ".", "B", "Z", "B", ".", "G"),
                            listOf("G", ".", "B", "B", "B", ".", "G"),
                            listOf("G", "V", ".", ".", ".", "V", "G"),
                            listOf("G", "G", "G", "G", "G", "G", "G")
                        ),
                        // L3: Killing floor base (Hoppers)
                        listOf(
                            listOf("C", "C", "C", "C", "C", "C", "C"),
                            listOf("C", "W", "W", "W", "W", "W", "C"),
                            listOf("C", "W", "H", "H", "H", "W", "C"),
                            listOf("C", "W", "H", "H", "H", "W", "C"),
                            listOf("C", "W", "H", "H", "H", "W", "C"),
                            listOf("C", "W", "W", "W", "W", "W", "C"),
                            listOf("C", "C", "C", "C", "C", "C", "C")
                        ),
                        // L4: Lava killer layer
                        listOf(
                            listOf("G", "G", "G", "G", "G", "G", "G"),
                            listOf("G", ".", ".", ".", ".", ".", "G"),
                            listOf("G", ".", "L", "L", "L", ".", "G"),
                            listOf("G", ".", "L", "L", "L", ".", "G"),
                            listOf("G", ".", "L", "L", "L", ".", "G"),
                            listOf("G", ".", ".", ".", ".", ".", "G"),
                            listOf("G", "G", "G", "G", "G", "G", "G")
                        )
                    )
                ),
                creatorName = "RedstoneKing",
                creatorEmail = "redstone_king@minecraft.net",
                isShared = true,
                likesCount = 412,
                userLiked = false
            )
        )

        // 3. Compact T-Flip-Flop
        list.add(
            MinecraftBuild(
                title = "Compact T-Flip-Flop",
                description = "Converts a standard push button pulse into a permanent ON/OFF toggle state using clean sticky-piston leaf mechanics. Super compact for automated vaults.",
                category = "Redstone",
                difficulty = "Easy",
                estimatedTime = "10 Mins",
                materials = "Smoothe Stone: 5, Redstone Dust: 4, Redstone Repeater: 1, Sticky Piston: 1, Redstone Block: 1, Lever/Button: 1",
                dimensions = "3 x 3 x 2",
                instructions = "1. Place a stone block with a wooden button attached on one side.\n2. Behind the block, lay a Redstone Dust dot.\n3. Fit a Sticky Piston facing towards the side block carrying a Redstone Block asset.\n4. Route your output wire through a Repeater sensing the toggled block positions.",
                tips = "You can route the pulse wire around walls to hide the machinery entirely under decorative flooring.",
                layersJson = create3DGridJson(
                    listOf(
                        listOf(
                            listOf("S", "R", "."),
                            listOf("S", "K", "A"),
                            listOf("S", "R", ".")
                        ),
                        listOf(
                            listOf(".", ".", "."),
                            listOf(".", "R", "R"),
                            listOf(".", ".", ".")
                        )
                    )
                ),
                creatorName = "AlexM",
                creatorEmail = "valko.arthur@gmail.com",
                isShared = true,
                likesCount = 125,
                userLiked = false
            )
        )

        // 4. Obsidian PvP Fortress Arena
        list.add(
            MinecraftBuild(
                title = "Obsidian Gladiator Arena",
                description = "High-contrast tactical fortress designed for 1v1 Gladiator combat. Fitted with safety viewing galleries, lava pits, and automatic sticky portculises.",
                category = "PvP Builds",
                difficulty = "Hard",
                estimatedTime = "4 Hours",
                materials = "Obsidian: 256, Netherrack: 120, Glass: 64, Lava: 16, Redstone Components: 32, Iron Bars: 48",
                dimensions = "12 x 12 x 5",
                instructions = "1. Lay an indestructible 12x12 Obsidian foundation.\n2. Carve glowing corner containment pits and fill with flowing Lava hazards.\n3. Assemble two opposing starting iron cages operated by redstone draw levers.\n4. Enclose the perimeter with glass panels to enable safe spectator viewpoints.",
                tips = "Set Netherrack on fire on top of obsidian columns to create spectacular natural illumination.",
                layersJson = create3DGridJson(
                    listOf(
                        // L1 Foundation
                        listOf(
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y"),
                            listOf("Y", "L", "L", "Y", "Y", "Y", "Y", "Y", "Y", "L", "L", "Y"),
                            listOf("Y", "L", "L", "Y", "Y", "Y", "Y", "Y", "Y", "L", "L", "Y"),
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y"),
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y"),
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y"),
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y"),
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y"),
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y"),
                            listOf("Y", "L", "L", "Y", "Y", "Y", "Y", "Y", "Y", "L", "L", "Y"),
                            listOf("Y", "L", "L", "Y", "Y", "Y", "Y", "Y", "Y", "L", "L", "Y"),
                            listOf("Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y", "Y")
                        ),
                        // L2 Pit openings & glass panes
                        listOf(
                            listOf("Y", "Y", "Y", "Y", "G", "G", "G", "G", "Y", "Y", "Y", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", "Y", "Y", "I", ".", ".", ".", ".", "I", "Y", "Y", "Y"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("Y", "Y", "Y", "I", ".", ".", ".", ".", "I", "Y", "Y", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", "Y", "Y", "Y", "G", "G", "G", "G", "Y", "Y", "Y", "Y")
                        ),
                        // L3 Gates & fence layers
                        listOf(
                            listOf("Y", "Y", "Y", "Y", "G", "G", "G", "G", "Y", "Y", "Y", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", "Y", "Y", "R", ".", ".", ".", ".", "R", "Y", "Y", "Y"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "G"),
                            listOf("Y", "Y", "Y", "R", ".", ".", ".", ".", "R", "Y", "Y", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", ".", ".", "Y", ".", ".", ".", ".", "Y", ".", ".", "Y"),
                            listOf("Y", "Y", "Y", "Y", "G", "G", "G", "G", "Y", "Y", "Y", "Y")
                        )
                    )
                ),
                creatorName = "GamerSteve",
                creatorEmail = "valko.arthur@gmail.com",
                isShared = true,
                likesCount = 598,
                userLiked = false
            )
        )

        // Add to return list
        list.addAll(getRemainingSeeds())
        return list
    }

    private fun getRemainingSeeds(): List<MinecraftBuild> {
        val list = mutableListOf<MinecraftBuild>()

        // 5. Fire Breathing Castle Gates
        list.add(
            MinecraftBuild(
                title = "Fire Castle Gates",
                description = "Imposing castle gate designed with a cobblestone rampart and dispensers loaded with Fire Charges. Linked together with redstone pressure detectors to vaporize hostiles.",
                category = "Castles",
                difficulty = "Hard",
                estimatedTime = "1.5 Hours",
                materials = "Cobblestone Bricks: 150, Iron Gates: 2, Redstone Repeaters: 8, Fire Charge Dispenser: 4, Pressure Plates: 4",
                dimensions = "8 x 8 x 5",
                instructions = "1. Construct twin stone towers 5 blocks tall on both sides of a 2-block wide road.\n2. Suspend Dispenser units above the center entryway pointing downward.\n3. Lay redstone wire connecting hidden trigger plates to the firing dispensers.\n4. Fill dispensers with fireballs/TNT and install sturdy iron spikes.",
                tips = "Conceal the redstone routing inside the hollow towers to protect them from external explosions.",
                layersJson = create3DGridJson(
                    listOf(
                        listOf(
                            listOf("C", "C", "C", "P", "P", "C", "C", "C"),
                            listOf("C", ".", "C", "P", "P", "C", ".", "C"),
                            listOf("C", ".", "C", ".", ".", "C", ".", "C")
                        )
                    )
                ),
                creatorName = "HerobrineHunter",
                creatorEmail = "hunter@minecraft.net",
                isShared = true,
                likesCount = 189,
                userLiked = false
            )
        )

        // 6. Retro Diamond Sword Sprite
        list.add(
            MinecraftBuild(
                title = "Diamond Sword Sprite",
                description = "Gorgeously stylized retro look Diamond Sword art. Built using layered wool and obsidian frames. Ideal decoration for world lobbies or base facades.",
                category = "Pixel Art",
                difficulty = "Easy",
                estimatedTime = "20 Mins",
                materials = "Obsidian Blocks: 45, Light Blue Wool: 32, Cyan Wool: 18, Oak Planks: 8, Diamond Blocks: 4",
                dimensions = "10 x 10 x 1",
                instructions = "1. Draft the diagonal obsidian hilt casing starting from coordinates (0,0).\n2. Form the handguard crossbars using dark brown wooden elements.\n3. Trace the diagonal blade with obsidian blocks.\n4. Core the center with Cyan Wool and Light Blue Wool highlights.",
                tips = "You can alternate wool blocks with Glowing obsidian variants to make the sword shine spectacularly at night.",
                layersJson = create3DGridJson(
                    listOf(
                        listOf(
                            listOf(".", ".", ".", ".", ".", ".", "Y", "Y", "Y", "Y"),
                            listOf(".", ".", ".", ".", ".", "Y", "I", "I", "Y", "Y"),
                            listOf(".", ".", ".", ".", "Y", "I", "I", "Y", "Y", "."),
                            listOf(".", ".", ".", "Y", "I", "I", "Y", ".", ".", "."),
                            listOf(".", ".", "Y", "I", "Y", "Y", ".", ".", ".", "."),
                            listOf(".", "Y", "O", "Y", "Y", ".", ".", ".", ".", "."),
                            listOf("Y", "O", "Y", ".", ".", ".", ".", ".", ".", "."),
                            listOf("Y", "Y", ".", ".", ".", ".", ".", ".", ".", ".")
                        )
                    )
                ),
                creatorName = "PixelQueen",
                creatorEmail = "pixel_queen@minecraft.net",
                isShared = true,
                likesCount = 371,
                userLiked = false
            )
        )

        // 7. Survival Starter Greenhouse
        list.add(
            MinecraftBuild(
                title = "Starter Greenhouse",
                description = "Modern and highly space-efficient farming glasshouse. Features a central water hydration hub that powers over 32 wheat, pumpkin, and melon tilled beds.",
                category = "Farms",
                difficulty = "Easy",
                estimatedTime = "15 Mins",
                materials = "Dirt: 32, Glass: 48, Water Bucket: 1, Oak Planks: 24, Oak Fence: 12",
                dimensions = "6 x 6 x 3",
                instructions = "1. Excavate a single block in the center and fill with a water channel.\n2. Till a 5x5 crop circle orbiting the water block.\n3. Surround the tilled perimeter with neat wooden planks, leaving space for a gate.\n4. Wall the farm with a 2-block high clear glass dome.",
                tips = "Install glowing glowstone below the water source block to keep the farming space lit 24/7.",
                layersJson = create3DGridJson(
                    listOf(
                        listOf(
                            listOf("P", "P", "P", "P", "P", "P"),
                            listOf("P", "D", "D", "D", "D", "P"),
                            listOf("P", "D", "D", "D", "D", "P"),
                            listOf("P", "D", "D", "W", "D", "P"),
                            listOf("P", "D", "D", "D", "D", "P"),
                            listOf("P", "P", "P", "E", "P", "P")
                        ),
                        listOf(
                            listOf("G", "G", "G", "G", "G", "G"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("G", ".", ".", ".", ".", "G"),
                            listOf("G", "G", "G", "E", "G", "G")
                        )
                    )
                ),
                creatorName = "GamerSteve",
                creatorEmail = "valko.arthur@gmail.com",
                isShared = true,
                likesCount = 145,
                userLiked = false
            )
        )

        return list
    }

    // Utility to construct layer layout JSONs
    private fun create3DGridJson(layout: List<List<List<String>>>): String {
        val root = JSONArray()
        for (layer in layout) {
            val layerArr = JSONArray()
            for (row in layer) {
                val rowArr = JSONArray()
                for (cell in row) {
                    rowArr.put(cell)
                }
                layerArr.put(rowArr)
            }
            root.put(layerArr)
        }
        return root.toString()
    }
}
