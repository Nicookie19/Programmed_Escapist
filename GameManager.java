package progescps;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Full GameManager.java - merged and updated.
 *
 * Changes:
 * - Added justLoaded flag to indicate when a save was loaded so post-selection flow
 *   doesn't re-run new-character prompts.
 * - Added loadGameFromAsync(...) to safely load from DB on a worker thread and start
 *   the game thread only after the load completes (with EDT callbacks).
 * - Ensured uiLoadGame / uiLoadGameFrom start the in-game thread after loading.
 *
 * Note: This file assumes the rest of your project classes exist (Hero, Enemy,
 * QuestManager, InputProvider, GameDatabase, UITheme, Color, StatusEffect, Combat, etc.)
 * and matches the structure you provided earlier.
 */
public class GameManager {

    private final InputProvider scan;

    public Hero player;
    public Enemy enemy;

    Stack<Integer> lastPlayerHP = new Stack<>();
    Stack<Integer> lastEnemyHP = new Stack<>();

    int gameTimer = 2;
    String equippedWeapon = "Basic Sword";
    String equippedArmor = "Cloth Armor";
    private boolean useColor = true;
    private QuestManager questManager = new QuestManager();
    private UITheme uiTheme = new UITheme();

    private Map<String, Location> worldMap;
    private Random random = new Random();
    private List<Faction> availableFactions;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean permadeathEnabled = false;

    private volatile boolean stopRequested = false;
    private volatile boolean gameRunning = false;
    private boolean inCombat = false;
    private boolean isMoving = false;
    private long lastMoveTime = 0;
    private GameUI ui; // Reference to the UI

    // Flag used to signal that loadGameFrom(...) was just executed so startGamePostSelection
    // won't try to re-run new-game prompts.
    private volatile boolean justLoaded = false;

    public boolean isInCombat() {
        return inCombat;
    }

    public boolean isMoving() {
        // Consider movement expired after 2 seconds
        if (isMoving && System.currentTimeMillis() - lastMoveTime > 2000) {
            isMoving = false;
        }
        return isMoving;
    }

    protected void setMoving(boolean moving) {
        if (moving) {
            lastMoveTime = System.currentTimeMillis();
        }
        isMoving = moving;
    }

    protected void setCombat(boolean combat) {
        inCombat = combat;
    }

    private static class GameStopException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public GameManager() {
        this(new InputProvider());
    }

    public GameManager(InputProvider inputProvider) {
        this.scan = inputProvider != null ? inputProvider : new InputProvider();
        initializeWorld();
        initializeFactions();
    }
    public void setUi(GameUI ui) {
        this.ui = ui;
        initializeWorld();
        initializeFactions();
    }

    public void requestStopCurrentGame() {
        if (!gameRunning) {
            stopRequested = false;
            scan.clearPending();  // ✅ Clear input
            return;
        }

        System.out.println("Stopping current game...");
        stopRequested = true;

        scan.clearPending();  // ✅ Clear pending input first
        scan.submitTrimmed("");  // Unblock waiting input

        // ✅ Wait up to 3 seconds
        long deadline = System.currentTimeMillis() + 3000;
        while (gameRunning && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // ✅ Force stop if timeout
        if (gameRunning) {
            System.out.println("Force stopping game thread...");
            gameRunning = false;
        }

        stopRequested = false;
        scan.clearPending();  // ✅ Clear again after stopping

        // ✅ Brief cleanup delay
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
        }
    }

    private void resetForNewGame() {
        initializeWorld();
        initializeFactions();
        this.questManager = new QuestManager();
        this.equippedWeapon = "Basic Sword";
        this.equippedArmor = "Cloth Armor";
        this.gameTimer = 2;
        this.player = null;
        this.enemy = null;
        this.justLoaded = false;
    }

    public void start() {
        displayMainMenu();
    }

    public void uiNewGame() {
        new Thread(() -> {
            System.out.println(Color.colorize("UI: Starting new game...", Color.YELLOW));
            try {
                startGame();
            } catch (Throwable t) {
                System.err.println("Error starting new game: " + t.getMessage());
                t.printStackTrace();
            }
        }, "UI-NewGame-Thread").start();
    }

    public void uiStartGameWithClass(int choice) {
        new Thread(() -> {
            System.out.println(Color.colorize("UI: Starting new game (GUI class selection) ...", Color.YELLOW));
            try {
                startGameWithChoice(choice);
            } catch (Throwable t) {
                System.err.println("Error starting new game with choice: " + t.getMessage());
                t.printStackTrace();
            }
        }, "UI-StartGame-Thread").start();
    }

    // --- [THIS IS THE NEW DATABASE SAVE/LOAD UI BLOCK] ---
    public void uiSaveGame() {
        // This is a wrapper for the console 'save' command
        new Thread(() -> {
            System.out.println(Color.colorize("UI: Saving game...", Color.YELLOW));
            try {
                saveGame();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Successfully saved in the database as 'saves'.",
                            "Save Game",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Throwable t) {
                String errorMessage = "A database error occurred:\n" + t.getMessage();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            errorMessage,
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                });
                t.printStackTrace();
            }
        }, "UI-Save-Thread").start();
    }

    public void uiSaveGameTo(String filename) {
        new Thread(() -> {
            System.out.println(Color.colorize("UI: Saving game to '" + filename + "'...", Color.YELLOW));
            try {
                // 1. Attempt to save
                saveGameTo(filename);

                // 2. If it returns without error, show success
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Successfully saved in the database.",
                            "Save Game",
                            JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception e) { // Catch the thrown SQLException
                // 3. If it fails, show the error
                String errorMessage = "A database error occurred:\n" + e.getMessage();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            errorMessage,
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace(); // Also print to console
            }
        }, "UI-SaveSlot-Thread").start();
    }

    /**
     * Asynchronously load a save slot and resume the game. This is the method
     * GameUI should call when loading from the GUI so it can show a progress dialog
     * while the database + game thread are started. onSuccess/onError are executed
     * on the EDT (Swing thread).
     *
     * Usage from UI:
     *   manager.loadGameFromAsync(filename,
     *       () -> { // success on EDT: switch UI to game card, refresh stats },
     *       ex -> { // error on EDT: show dialog }
     *   );
     */
    public void loadGameFromAsync(String filename, Runnable onSuccess, Consumer<Exception> onError) {
        new Thread(() -> {
            try {
                loadGameFrom(filename);

                // Start the main game flow in its own thread so it runs independently of the loader thread.
                Thread gameThread = new Thread(() -> {
                    try {
                        gameRunning = true;
                        stopRequested = false;
                        startGamePostSelection();
                    } catch (Throwable t) {
                        System.err.println("Error while resuming loaded game: " + t.getMessage());
                        t.printStackTrace();
                    } finally {
                        gameRunning = false;
                    }
                }, "Game-Thread");
                gameThread.setDaemon(true);
                gameThread.start();

                if (onSuccess != null) {
                    SwingUtilities.invokeLater(onSuccess);
                }
            } catch (Exception e) {
                if (onError != null) {
                    final Exception ex = e;
                    SwingUtilities.invokeLater(() -> onError.accept(ex));
                } else {
                    // fallback logging
                    System.err.println("Error loading game asynchronously: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, "LoadAsync-Thread").start();
    }

    public void uiLoadGame() {
        // This is a wrapper for the console 'load' command
        new Thread(() -> {
            System.out.println(Color.colorize("UI: Loading game...", Color.YELLOW));
            try {
                loadGame();

                // Start the in-game flow after loading (console wrapper)
                Thread gameThread = new Thread(() -> {
                    try {
                        gameRunning = true;
                        stopRequested = false;
                        startGamePostSelection();
                    } catch (Throwable t) {
                        System.err.println("Error while resuming loaded game: " + t.getMessage());
                        t.printStackTrace();
                    } finally {
                        gameRunning = false;
                    }
                }, "Game-Thread");
                gameThread.setDaemon(true);
                gameThread.start();

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Successfully loaded game",
                            "Load Game",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Throwable t) {
                String errorMessage = "Failed to load game:\n" + t.getMessage();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            errorMessage,
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE);
                });
                t.printStackTrace();
            }
        }, "UI-Load-Thread").start();
    }

    public void uiLoadGameFrom(String filename) {
        new Thread(() -> {
            System.out.println(Color.colorize("UI: Loading game from '" + filename + "'...", Color.YELLOW));
            try {
                // 1. Attempt to load
                loadGameFrom(filename);

                // 2. Start the main game flow in its own thread so it runs independently of the loader thread.
                Thread gameThread = new Thread(() -> {
                    try {
                        gameRunning = true;
                        stopRequested = false;
                        // startGamePostSelection will continue the in-game loop (quests, menu, etc.)
                        startGamePostSelection();
                    } catch (Throwable t) {
                        System.err.println("Error while resuming loaded game: " + t.getMessage());
                        t.printStackTrace();
                    } finally {
                        gameRunning = false;
                    }
                }, "Game-Thread");
                gameThread.setDaemon(true);
                gameThread.start();

                // 3. If load succeeded, show success dialog on EDT
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Successfully loaded game: " + filename,
                            "Load Game",
                            JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception e) { // Catch any exception
                // 3. If it fails, show the error
                String errorMessage = "Failed to load game:\n" + e.getMessage();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            errorMessage,
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace(); // Also print to console
            }
        }, "UI-LoadSlot-Thread").start();
    }

    public void uiQuitGame() {
        new Thread(() -> {
            System.out.println(Color.colorize("UI: Quitting game...", Color.YELLOW));
            try {
                Thread.sleep(120);
            } catch (InterruptedException ignored) {
            }
            SwingUtilities.invokeLater(() -> System.exit(0));
        }, "UI-Quit-Thread").start();
    }

    // --- [THIS IS THE NEW DATABASE SAVE/LOAD LOGIC] ---
    public List<String> listSaveFiles() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT save_name FROM save_slots ORDER BY last_modified DESC";

        try (Connection conn = GameDatabase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                names.add(rs.getString("save_name"));
            }
        } catch (SQLException e) {
            System.out.println(Color.colorize("Error listing save files from database: " + e.getMessage(), Color.RED));
        }
        return names;
    }

    private synchronized void saveGameTo(String filename) throws SQLException {
        if (player == null) {
            throw new SQLException("No active player to save."); // Throw error
        }

        Connection conn = null;
        try {
            conn = GameDatabase.getConnection();
            conn.setAutoCommit(false); // Start transaction

            GameDatabase db = new GameDatabase(conn); // Use connection for all ops

            // 1. Find old hero to delete later
            int oldHeroId = db.findHeroIdBySaveName(filename);

            // 2. Save the new hero data
            int newHeroId = db.saveHero(this.player, this.permadeathEnabled);

            // 3. Save all related data
            db.saveEquipment(newHeroId, this.equippedWeapon, this.equippedArmor);
            db.saveInventory(newHeroId, this.player.getInventory());
            db.saveFactions(newHeroId, this.player.getFactions());
            db.saveQuests(newHeroId, this.questManager);
            db.saveAchievements(newHeroId, this.player.getAchievementManager());

            // 4. Link the save slot to the NEW hero
            db.linkSaveToHero(filename, newHeroId);

            // 5. If we're overwriting, delete the OLD hero data
            if (oldHeroId != -1 && oldHeroId != newHeroId) {
                db.deleteHero(oldHeroId);
            }

            // 6. Commit transaction
            conn.commit();
            // Success!

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
            // This is the CRITICAL part: throw the error so the UI can see it
            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
    }

    private synchronized void saveGame() throws SQLException {
        saveGameTo("DefaultSave"); // This is the default slot name
    }

    /**
     * Loads the hero and related data from DB into this GameManager.
     * This method sets justLoaded = true so callers (especially UI) can
     * know it was a load operation.
     */
    private synchronized void loadGameFrom(String filename) throws SQLException, IOException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = GameDatabase.getConnection();
            GameDatabase db = new GameDatabase(conn);

            // 1. Find hero ID from save name
            int heroId = db.findHeroIdBySaveName(filename);
            if (heroId == -1) {
                throw new FileNotFoundException("Save file not found in database: " + filename);
            }

            // 2. Load the main Hero object
            this.player = db.loadHero(heroId);

            // 3. Load all related data and hydrate the game state
            String[] equipment = db.loadEquipment(heroId);
            this.equippedWeapon = equipment[0];
            this.equippedArmor = equipment[1];

            this.player.setInventory(db.loadInventory(heroId));
            this.player.setFactions(db.loadFactions(heroId));

            // Rebuild the quest manager
            this.questManager = new QuestManager();
            this.questManager.setQuests(db.loadQuests(heroId));

            this.player.getAchievementManager().setAchievements(db.loadAchievements(heroId));

            // Load the other game manager fields
            try (PreparedStatement ps = conn.prepareStatement("SELECT hardcoreMode FROM hero WHERE id = ?")) {
                ps.setInt(1, heroId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        this.permadeathEnabled = rs.getBoolean("hardcoreMode");
                    }
                }
            }
            this.difficulty = this.player.difficulty;

            this.gameTimer = 2; // Reset
            this.useColor = true; // Reset
            Color.USE_ANSI = this.useColor;

            // mark that we just loaded a save so startGamePostSelection can skip prompts
            this.justLoaded = true;

            // Success!
        } catch (SQLException | IOException e) {
            throw e; // Re-throw for the UI to catch
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
    }

    private synchronized void loadGame() throws SQLException, IOException, ClassNotFoundException {
        loadGameFrom("saves"); // This method now throws
    }

    private void deleteAllSaveFiles() {
        // This is now much simpler and safer
        String sqlHeroes = "DELETE FROM hero";
        String sqlSlots = "DELETE FROM save_slots";

        try (Connection conn = GameDatabase.getConnection(); Statement stmt = conn.createStatement()) {

            // Must delete from slots first if ON DELETE CASCADE is not set on hero
            // But since we set it, deleting from hero will cascade.
            // Let's delete from both to be safe and clear all heroes.
            int slotRows = stmt.executeUpdate(sqlSlots);
            System.out.println(Color.colorize("Deleted " + slotRows + " save slot link(s).", Color.YELLOW));

            int heroRows = stmt.executeUpdate(sqlHeroes);
            System.out.println(Color.colorize("Deleted " + heroRows + " hero save(s) and all related data.", Color.YELLOW));

        } catch (SQLException e) {
            System.out.println(Color.colorize("Error deleting all save data: " + e.getMessage(), Color.RED));
        }
    }   // --- [UI HELPER METHODS & WORLD INITIALIZATION] ---

    private void printCenteredLine(String text, String color) {
        String trimmed = text == null ? "" : text.trim();
        int width = uiTheme.getMenuWidth();
        int contentWidth = Math.max(0, width - 2);
        if (trimmed.length() > contentWidth && contentWidth >= 3) {
            trimmed = trimmed.substring(0, contentWidth - 3) + "...";
        }
        int padding = Math.max(0, (contentWidth - trimmed.length()) / 2);
        int rightPadding = contentWidth - padding - trimmed.length();
        String line = "|" + " ".repeat(padding) + trimmed + " ".repeat(Math.max(0, rightPadding)) + "|";
        System.out.println(Color.colorize(line, color));
    }

    private void printBorder(String type) {
        int width = uiTheme.getMenuWidth();
        System.out.println(Color.colorize("+" + "=".repeat(Math.max(0, width - 2)) + "+", uiTheme.getTextColor()));
    }

    private void initializeFactions() {
        availableFactions = new ArrayList<>();
        availableFactions.add(new Faction("Hackers Alliance"));
        availableFactions.add(new Faction("Cyber Thieves"));
        availableFactions.add(new Faction("Shadow Coders"));
        availableFactions.add(new Faction("Tech University"));
        availableFactions.add(new Faction("Firewall Guardians"));
    }

    private void initializeWorld() {
        worldMap = new HashMap<>();

        Location centralServer = new Location("Central Server Hub",
                "A vast data center with interconnected servers, home to programmers and the mighty Hackers Alliance.", 1, true,
                new String[]{"Virus", "Trojan", "Malware"});
        centralServer.addFeature("Hackers Den");
        centralServer.addFeature("Black Market");
        worldMap.put("Central Server Hub", centralServer);

        Location darkWeb = new Location("Dark Web Forest",
                "A dense network shrouded in encryption, hiding cybercriminals and data smugglers.", 2, true,
                new String[]{"Trojan", "Spyware", "Worm"});
        darkWeb.addFeature("Cyber Thieves Hideout");
        darkWeb.addFeature("Encrypted Keep");
        worldMap.put("Dark Web Forest", darkWeb);

        Location firewall = new Location("Firewall Cliffs",
                "Towering firewalls overlooking the digital sea, home to the Firewall Guardians and elite coders.", 3, true,
                new String[]{"Ransomware", "Phishing", "Trojan"});
        firewall.addFeature("Blue Palace");
        firewall.addFeature("Docks");
        worldMap.put("Firewall Cliffs", firewall);

        Location frozenCode = new Location("Frozen Code Tundra",
                "A frozen wasteland where hackers study ancient algorithms at the Tech University.", 4, false,
                new String[]{"Ice Virus", "Frost Worm", "Glitch"});
        frozenCode.addFeature("Tech University");
        frozenCode.addFeature("Frozen Archives");
        frozenCode.setEnvironmentalEffect("Blizzard");
        worldMap.put("Frozen Code Tundra", frozenCode);

        Location corruptedData = new Location("Corrupted Data Ruins",
                "Ancient server ruins carved into mountains, plagued by malware and dark code.", 5, false,
                new String[]{"Malware", "Corrupt File", "Rootkit"});
        corruptedData.addFeature("Data Museum");
        corruptedData.addFeature("Shadow Coders Sanctuary");
        corruptedData.setEnvironmentalEffect("Cave-in");
        worldMap.put("Corrupted Data Ruins", corruptedData);

        Location backup = new Location("Backup Coast",
                "A chilly coastal server battered by data storms, known for its hardy backups.", 2, true,
                new String[]{"Backup Worm", "Trojan", "Ice Malware"});
        backup.addFeature("Backup Harbor");
        backup.addFeature("Data Mine");
        worldMap.put("Backup Coast", backup);

        Location malware = new Location("Malware Forest",
                "A lush network with towering data trees, haunted by viruses and cybercriminals.", 2, true,
                new String[]{"Trojan", "Spyware", "Worm"});
        malware.addFeature("Malware Graveyard");
        malware.addFeature("Jarl's Longhouse");
        worldMap.put("Malware Forest", malware);

        Location glitch = new Location("Glitch Marshes",
                "A foggy swamp filled with buggy code and dangerous glitches.", 3, true,
                new String[]{"Bug", "Glitch Troll", "Error"});
        glitch.addFeature("Glitch Inn");
        glitch.addFeature("Highmoon Hall");
        glitch.setEnvironmentalEffect("Thick Fog");
        worldMap.put("Glitch Marshes", glitch);

        Location encrypted = new Location("Encrypted Snowfields",
                "A snow-covered expanse surrounding the ancient city of the encrypted.", 3, true,
                new String[]{"Ice Virus", "Snow Worm", "Trojan"});
        encrypted.addFeature("Palace of the Kings");
        encrypted.addFeature("Candlehearth Hall");
        worldMap.put("Encrypted Snowfields", encrypted);

        Location dataStream = new Location("Data Stream Valley",
                "A peaceful valley with a rushing data stream, home to miners and traders.", 1, true,
                new String[]{"Virus", "Trojan", "Bug"});
        dataStream.addFeature("Sleeping Giant Inn");
        dataStream.addFeature("Data Stream Trader");
        worldMap.put("Data Stream Valley", dataStream);

        Location forgottenCache = new Location("Forgotten Cache Barrow",
                "An ancient data cache filled with traps and corrupt files.", 4, false,
                new String[]{"Corrupt File", "Skeleton Code", "Rootkit"});
        forgottenCache.addFeature("Ancient Altar");
        forgottenCache.addFeature("Code Wall");
        worldMap.put("Forgotten Cache Barrow", forgottenCache);

        Location summitServer = new Location("Summit Server",
                "The sacred mountain server of the Greybeards, shrouded in mist.", 5, false,
                new String[]{"Frost Worm", "Ice Virus", "Snow Malware"});
        summitServer.addFeature("Greybeard Sanctum");
        summitServer.addFeature("Meditation Chamber");
        worldMap.put("Summit Server", summitServer);

        Location remoteNode = new Location("Remote Node Village",
                "A small settlement at the base of the Summit Server.", 1, true,
                new String[]{"Virus", "Trojan", "Worm"});
        remoteNode.addFeature("Vilemyr Inn");
        remoteNode.addFeature("Riftweald Farm");
        worldMap.put("Remote Node Village", remoteNode);

        Location firewallBridge = new Location("Firewall Bridge",
                "A strategic crossing with a firewall bridge shaped like a dragon.", 2, true,
                new String[]{"Trojan", "Malware", "Ransomware"});
        firewallBridge.addFeature("Four Shields Tavern");
        firewallBridge.addFeature("Firewall Bridge Lumber Camp");
        worldMap.put("Firewall Bridge", firewallBridge);

        Location mining = new Location("Mining Hills",
                "Rugged hills rich with data mines, contested by ransomware.", 3, true,
                new String[]{"Ransomware", "Trojan", "Glitch"});
        mining.addFeature("Data Mine");
        mining.addFeature("Mining Hall");
        worldMap.put("Mining Hills", mining);

        Location fertile = new Location("Fertile Fields",
                "Fertile data fields known for bountiful code and peaceful folk.", 1, true,
                new String[]{"Virus", "Trojan", "Fox"});
        fertile.addFeature("Frostfruit Inn");
        fertile.addFeature("Cowflop Farm");
        worldMap.put("Fertile Fields", fertile);

        Location crashedSystem = new Location("Crashed System Ruins",
                "A destroyed server recently ravaged by a rootkit attack.", 3, false,
                new String[]{"Trojan", "Corrupt File", "Skeleton Code"});
        crashedSystem.addFeature("Burned Keep");
        crashedSystem.addFeature("Hidden Escape Tunnel");
        worldMap.put("Crashed System Ruins", crashedSystem);

        Location undergroundNetwork = new Location("Underground Network",
                "A vast underground network lit by glowing LEDs and server ruins.", 5, false,
                new String[]{"Malware", "Chaurus", "Automaton"});
        undergroundNetwork.addFeature("Tower of Mzark");
        undergroundNetwork.addFeature("Silent City");
        undergroundNetwork.setEnvironmentalEffect("Glowing Spores");
        worldMap.put("Underground Network", undergroundNetwork);

        Location voidCache = new Location("Void Cache",
                "A desolate plane of the void filled with lost data and necrotic energy.", 5, false,
                new String[]{"Boneman", "Mistman", "Wrathman"});
        voidCache.addFeature("Data Well");
        voidCache.addFeature("Boneyard");
        voidCache.setEnvironmentalEffect("Soul Drain");
        worldMap.put("Void Cache", voidCache);

        Location hiddenPartition = new Location("Hidden Partition",
                "A hidden glacial partition home to ancient malware temples.", 4, false,
                new String[]{"Malware", "Frostbite Spider", "Ice Virus"});
        hiddenPartition.addFeature("Auriel's Shrine");
        hiddenPartition.addFeature("Frozen Lake");
        hiddenPartition.setEnvironmentalEffect("Ancient Power");
        worldMap.put("Hidden Partition", hiddenPartition);

        Location overclocked = new Location("Overclocked Springs",
                "Steaming geothermal pools surrounded by volcanic circuits.", 3, false,
                new String[]{"Horker", "Troll", "Ash Spawn"});
        overclocked.addFeature("Sulfur Pools");
        overclocked.addFeature("Ancient Cairn");
        overclocked.setEnvironmentalEffect("Soothing Vapors");
        worldMap.put("Overclocked Springs", overclocked);

        Location bugBog = new Location("Bug Bog",
                "A treacherous wetland teeming with dangerous bugs.", 3, false,
                new String[]{"Bug", "Glitch Troll", "Chaurus"});
        bugBog.addFeature("Sunken Ruins");
        bugBog.addFeature("Bog Beacon");
        worldMap.put("Bug Bog", bugBog);

        Location frozenSector = new Location("Frozen Sector",
                "A stark, snowy landscape with scattered server ruins.", 2, false,
                new String[]{"Snow Virus", "Ice Virus", "Skeleton Code"});
        frozenSector.addFeature("Frostmere Crypt");
        frozenSector.addFeature("Ancient Watchtower");
        worldMap.put("Frozen Sector", frozenSector);

        Location isolatedCoast = new Location("Isolated Coast",
                "A frozen shoreline littered with crashed servers and ice floes.", 3, false,
                new String[]{"Horker", "Ice Malware", "Frost Worm"});
        isolatedCoast.addFeature("Wreck of the Winter War");
        isolatedCoast.addFeature("Ice Cave");
        worldMap.put("Isolated Coast", isolatedCoast);

        Location vampireServer = new Location("Vampire Server",
                "A foreboding vampire server on a remote island.", 5, false,
                new String[]{"Vampire", "Death Hound", "Gargoyle"});
        vampireServer.addFeature("Volkihar Cathedral");
        vampireServer.addFeature("Bloodstone Chalice");
        vampireServer.setEnvironmentalEffect("Vampiric Aura");
        worldMap.put("Vampire Server", vampireServer);

        Location ashNode = new Location("Ash Node",
                "A Dunmer colony on the ash-covered island of Solstheim.", 4, true,
                new String[]{"Ash Spawn", "Riekling", "Netch"});
        ashNode.addFeature("Redoran Council Hall");
        ashNode.addFeature("The Retching Netch");
        worldMap.put("Ash Node", ashNode);

        Location wizardTower = new Location("Wizard Tower",
                "A Telvanni wizard tower surrounded by ash and fungal growths.", 4, false,
                new String[]{"Ash Spawn", "Spriggan", "Burnt Spriggan"});
        wizardTower.addFeature("Telvanni Tower");
        wizardTower.addFeature("Silt Strider Stable");
        worldMap.put("Wizard Tower", wizardTower);

        Location nordicVillage = new Location("Nordic Village",
                "A small Nordic settlement on Solstheim, devoted to the All-Maker.", 2, true,
                new String[]{"Virus", "Worm", "Riekling"});
        nordicVillage.addFeature("Shaman's Hut");
        nordicVillage.addFeature("Greathall");
        worldMap.put("Nordic Village", nordicVillage);

        Location warriorHall = new Location("Warrior Hall",
                "A warrior lodge on Solstheim, recently reclaimed from Riekling.", 3, true,
                new String[]{"Riekling", "Worm", "Troll"});
        warriorHall.addFeature("Mead Hall");
        warriorHall.addFeature("Hunter's Camp");
        worldMap.put("Warrior Hall", warriorHall);

        Location forbiddenRealm = new Location("Forbidden Realm",
                "The otherworldly realm of Hermaeus Mora, filled with forbidden knowledge.", 5, false,
                new String[]{"Seeker", "Lurker", "Daedra"});
        forbiddenRealm.addFeature("Black Book Archive");
        forbiddenRealm.addFeature("Forbidden Library");
        forbiddenRealm.setEnvironmentalEffect("Forbidden Knowledge");
        worldMap.put("Forbidden Realm", forbiddenRealm);
    }

    // ----------------------------- Menus & Game Flow -----------------------------

    private void displaySettingsMenu() {
        boolean settingsOpen = true;

        while (settingsOpen) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            printBorder("top");
            printCenteredLine("Settings", uiTheme.getPrimaryColor());
            printBorder("divider");
            printCenteredLine("1. Change UI Theme", uiTheme.getTextColor());
            printCenteredLine("2. Toggle Color Mode", uiTheme.getTextColor());
            printCenteredLine("3. Back to Main Menu", uiTheme.getTextColor());
            printBorder("bottom");

            System.out.print(Color.colorize("Choose an option (1-3): ", uiTheme.getHighlightColor()));
            String input = scan.nextLine().trim();

            if (input.equals("1")) {
                displayThemeMenu();
            } else if (input.equals("2")) {
                useColor = !useColor;
                System.out.println(Color.colorize("Color mode " + (useColor ? "enabled" : "disabled"), uiTheme.getSecondaryColor()));
                System.out.println("\nPress Enter to continue...");
                scan.nextLine();
            } else if (input.equals("3")) {
                settingsOpen = false;
            } else {
                System.out.println(Color.colorize("Invalid option. Please choose again.", Color.RED));
            }
        }
    }

    private void displayThemeMenu() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        printBorder("top");
        printCenteredLine("Select Theme", uiTheme.getPrimaryColor());
        printBorder("divider");
        printCenteredLine("1. Default", uiTheme.getTextColor());
        printCenteredLine("2. Dark", uiTheme.getTextColor());
        printCenteredLine("3. Retro", uiTheme.getTextColor());
        printCenteredLine("4. Cyberpunk", uiTheme.getTextColor());
        printBorder("bottom");

        System.out.print(Color.colorize("Choose a theme (1-4): ", uiTheme.getHighlightColor()));
        String input = scan.nextLine().trim();

        if (input.equals("1")) {
            uiTheme.setTheme(UITheme.ThemeType.DEFAULT);
        } else if (input.equals("2")) {
            uiTheme.setTheme(UITheme.ThemeType.DARK);
        } else if (input.equals("3")) {
            uiTheme.setTheme(UITheme.ThemeType.RETRO);
        } else if (input.equals("4")) {
            uiTheme.setTheme(UITheme.ThemeType.CYBERPUNK);
        } else {
            System.out.println(Color.colorize("Invalid option. Using default theme.", Color.RED));
            uiTheme.setTheme(UITheme.ThemeType.DEFAULT);
        }

        System.out.println(Color.colorize("Theme updated!", uiTheme.getSecondaryColor()));
        System.out.println("\nPress Enter to continue...");
        scan.nextLine();
    }

    public void displayMainMenu() {
        while (true) {
            printBorder("top");
            printCenteredLine("Codeborne: Odyssey of the Programmer", uiTheme.getPrimaryColor());
            printCenteredLine("A Tale of Code and Digital Adventures", uiTheme.getTextColor());
            printBorder("divider");
            printCenteredLine("1. Start New Game", uiTheme.getTextColor());
            printCenteredLine("2. Load Game", uiTheme.getTextColor());
            printCenteredLine("3. Settings", uiTheme.getTextColor());
            printCenteredLine("4. Achievements", uiTheme.getAccentColor());
            printCenteredLine("5. Exit", uiTheme.getTextColor());
            printBorder("bottom");
            System.out.print(Color.colorize("Choose an option (1-5): ", uiTheme.getHighlightColor()));

            String input = scan.nextLine().trim();
            if (input.equals("1")) {
                requestStopCurrentGame();
                resetForNewGame();
                startGame();
            } else if (input.equals("2")) {
                try {
                    loadGame(); // This now throws an exception
                    // If loadGame succeeds, we need to start the in-game menu
                    startGamePostSelection();
                } catch (Exception e) {
                    System.out.println(Color.colorize("Failed to load game: " + e.getMessage(), Color.RED));
                    System.out.println("\nPress Enter to continue...");
                    scan.nextLine();
                }
            } else if (input.equals("3")) {
                displaySettingsMenu();
            } else if (input.equals("4")) {
                if (player != null) {
                    player.getAchievementManager().displayAchievements();
                    System.out.println("\nPress Enter to continue...");
                    scan.nextLine();
                } else {
                    System.out.println(Color.colorize("No active game. Start a new game to track achievements.", uiTheme.getAccentColor()));
                    System.out.println("\nPress Enter to continue...");
                    scan.nextLine();
                }
            } else if (input.equals("5")) {
                System.out.println(Color.colorize("Thank you for playing! Farewell, adventurer!", uiTheme.getSecondaryColor()));
                break;
            } else {
                System.out.println(Color.colorize("Invalid option. Please choose again.", Color.RED));
            }
        }
    }

    private void promptDifficulty() {
        printBorder("top");
        printCenteredLine("Select Difficulty", uiTheme.getDifficultyColor());
        printBorder("divider");
        printCenteredLine("Choose your challenge level:", uiTheme.getHighlightColor());
        {
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("|" + " ".repeat(Math.max(0, w - 2)) + "|", uiTheme.getTextColor()));
        }
        System.out.println(Color.colorize("| 1. Easy: Reduced enemy strength, more rewards           |", uiTheme.getSecondaryColor()));
        System.out.println(Color.colorize("| 2. Normal: Balanced gameplay                            |", uiTheme.getHighlightColor()));
        System.out.println(Color.colorize("| 3. Hard: Increased enemy strength, fewer rewards        |", uiTheme.getCombatColor()));
        {
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("|" + " ".repeat(Math.max(0, w - 2)) + "|", uiTheme.getTextColor()));
        }
        printBorder("bottom");
        System.out.print(Color.colorize("Enter your choice (1-3): ", uiTheme.getHighlightColor()));
        int diffChoice = getChoice(1, 3);
        switch (diffChoice) {
            case 1:
                difficulty = Difficulty.EASY;
                break;
            case 2:
                difficulty = Difficulty.NORMAL;
                break;
            case 3:
                difficulty = Difficulty.HARD;
                break;
        }
        System.out.println(Color.colorize("Difficulty set to " + difficulty + "!", uiTheme.getSecondaryColor()));
    }

    private void promptPermadeath() {
        printBorder("top");
        printCenteredLine("Hardcore Mode", uiTheme.getPermadeathColor());
        printBorder("divider");
        printCenteredLine("Enable permadeath?", uiTheme.getHighlightColor());
        {
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("|" + " ".repeat(Math.max(0, w - 2)) + "|", uiTheme.getTextColor()));
        }
        System.out.println(Color.colorize("| 1. No  - Normal gameplay with save/load                 |", uiTheme.getSecondaryColor()));
        System.out.println(Color.colorize("| 2. Yes - Permadeath: Game ends permanently on death     |", uiTheme.getCombatColor()));
        {
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("|" + " ".repeat(Math.max(0, w - 2)) + "|", uiTheme.getTextColor()));
        }
        printBorder("bottom");
        System.out.print(Color.colorize("Enter your choice (1-2): ", uiTheme.getHighlightColor()));
        int pdChoice = getChoice(1, 2);
        permadeathEnabled = (pdChoice == 2);
        if (permadeathEnabled) {
            System.out.println(Color.colorize("Permadeath enabled! Good luck.", uiTheme.getPermadeathColor()));
        } else {
            System.out.println(Color.colorize("Permadeath disabled.", uiTheme.getSecondaryColor()));
        }
    }

    private void startGame() {
        gameRunning = true;
        stopRequested = false;

        promptDifficulty();
        promptPermadeath();

        printBorder("top");
        printCenteredLine("Choose Your Class", uiTheme.getPrimaryColor());
        printBorder("divider");
        printCenteredLine("1. Debugger", uiTheme.getTextColor());
        printCenteredLine("2. Hacker", uiTheme.getTextColor());
        printCenteredLine("3. Tester", uiTheme.getTextColor());
        printCenteredLine("4. Architect", uiTheme.getTextColor());
        printCenteredLine("5. PenTester", uiTheme.getTextColor());
        printCenteredLine("6. Support", uiTheme.getTextColor());
        printBorder("bottom");
        System.out.print(Color.colorize("Choose your class (1-6): ", uiTheme.getHighlightColor()));
        int choice = getChoice(1, 6);

        startGameWithChoice(choice);
    }

    private void startGameWithChoice(int choice) {
        gameRunning = true;
        stopRequested = false;

        // resetForNewGame() is called by the UI/console before this
        // difficulty/permadeath are set by the UI/console before this
        switch (choice) {
            case 1:
                player = new Debugger();
                break;
            case 2:
                player = new Hacker();
                break;
            case 3:
                player = new Tester();
                break;
            case 4:
                player = new Architect();
                break;
            case 5:
                player = new PenTester();
                break;
            case 6:
                player = new Support();
                break;
            default:
                player = new Debugger();
                break;
        }

        if (player != null) {
            player.difficulty = this.difficulty;
        }
        System.out.println(Color.colorize("You are now a " + player.getClassName() + "! The world awaits your legend.", Color.GREEN));

        startGamePostSelection();
    }

    private void startGamePostSelection() {
        try {
            // Give the new hero a starting quest only if they have no quests
            if (!justLoaded) {
                if (questManager.getActiveQuests().isEmpty() && questManager.getCompletedQuests().isEmpty()) {
                    questManager.addQuest(
                            "Find code snippet in Central Server Hub",
                            "A local programmer needs a specific code snippet. Find it in Central Server Hub.",
                            Arrays.asList("Find code snippet in Central Server Hub"),
                            Map.of("gold", 50, "xp", 20),
                            "Hackers Alliance"
                    );
                }
            } else {
                // clear the flag immediately so subsequent new-game flows behave normally
                justLoaded = false;
            }

            // Start the in-game menu loop
            inGameMenu();

        } catch (GameStopException e) {
            gameRunning = false;
            stopRequested = false;
            System.out.println(Color.colorize("Game stopped. Returning to Main Menu.", Color.YELLOW));
        } catch (Exception e) {
            gameRunning = false;
            stopRequested = false;
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            gameRunning = false;
            stopRequested = false;
        }
    }

    private void inGameMenu() {
        stopRequested = false;
        try {
            while (true) {
                if (stopRequested) {
                    throw new GameStopException();
                }

                printBorder("top");
                printCenteredLine("Adventurer's Lodge", uiTheme.getInnColor());
                printBorder("divider");
                printCenteredLine("1. Travel to New Lands", uiTheme.getTextColor());
                printCenteredLine("2. Manage Inventory and Equipment", uiTheme.getTextColor());
                printCenteredLine("3. View Gold", uiTheme.getTextColor());
                printCenteredLine("4. View Quest Log", uiTheme.getTextColor());
                printCenteredLine("5. View Player Stats", uiTheme.getTextColor());
                printCenteredLine("6. Rest at Inn", uiTheme.getTextColor());
                printCenteredLine("7. Faction Menu", uiTheme.getTextColor());
                printCenteredLine("8. Save Game", uiTheme.getTextColor());
                printCenteredLine("9. View Achievements", uiTheme.getTextColor());
                printBorder("bottom");
                System.out.print(Color.colorize("Choose an option (1-9): ", uiTheme.getHighlightColor()));

                int choice = getChoice(1, 9);

                switch (choice) {
                    case 1:
                        travel();
                        break;
                    case 2:
                        manageInventoryAndEquipment();
                        break;
                    case 3:
                        viewGold();
                        break;
                    case 4:
                        viewQuestLog();
                        break;
                    case 5:
                        viewPlayerStats();
                        break;
                    case 6:
                        restAtInn();
                        break;
                    case 7:
                        factionMenu();
                        break;
                    case 8:
                        try {
                            saveGame(); // This now throws
                            System.out.println(Color.colorize("Game saved successfully to database as 'Saves'", Color.GREEN));
                        } catch (SQLException e) {
                            System.out.println(Color.colorize("Error saving game to database: " + e.getMessage(), Color.RED));
                        }
                        break;
                    case 9:
                        viewAchievements();
                        break;
                    default:
                        System.out.println(Color.colorize("Invalid option. Try again.", Color.RED));
                }
            }
        } catch (GameStopException e) {
            gameRunning = false;
            stopRequested = false;
            return;
        }
    }

    private void manageInventoryAndEquipment() {
        try {
            while (true) {
                if (stopRequested) {
                    throw new GameStopException();
                }

                printBorder("top");
                printCenteredLine("Inventory and Equipment", uiTheme.getInventoryColor());
                printBorder("divider");
                printCenteredLine("1. View Inventory", uiTheme.getTextColor());
                printCenteredLine("2. View Equipment", uiTheme.getTextColor());
                printCenteredLine("3. Equip or Use Item", uiTheme.getTextColor());
                printCenteredLine("4. Back", uiTheme.getTextColor());
                printBorder("bottom");
                System.out.print(Color.colorize("Choose an option (1-4): ", uiTheme.getHighlightColor()));

                String input = scan.nextLine().trim();
                if (stopRequested) {
                    throw new GameStopException();
                }
                switch (input) {
                    case "1":
                        viewInventory();
                        break;
                    case "2":
                        viewEquipment();
                        break;
                    case "3":
                        equipOrUseItem();
                        break;
                    case "4":
                        return;
                    default:
                        System.out.println(Color.colorize("Invalid option. Try again.", Color.RED));
                }
            }
        } catch (GameStopException e) {
            throw e;
        }
    }

    private void factionMenu() {
        try {
            while (true) {
                if (stopRequested) {
                    throw new GameStopException();
                }

                printBorder("top");
                printCenteredLine("Faction Hall", uiTheme.getFactionColor());
                printBorder("divider");
                printCenteredLine("1. Join a Faction", uiTheme.getTextColor());
                printCenteredLine("2. View Faction Status", uiTheme.getTextColor());
                printCenteredLine("3. Undertake Faction Quest", uiTheme.getTextColor());
                printCenteredLine("4. Back", uiTheme.getTextColor());
                printBorder("bottom");
                System.out.print(Color.colorize("Choose an option (1-4): ", uiTheme.getHighlightColor()));
                int choice = getChoice(1, 4);
                switch (choice) {
                    case 1:
                        joinFaction();
                        break;
                    case 2:
                        viewFactions();
                        break;
                    case 3:
                        doFactionQuest();
                        break;
                    case 4:
                        return;
                }
            }
        } catch (GameStopException e) {
            throw e;
        }
    }

    private void joinFaction() {
        printBorder("top");
        printCenteredLine("Available Factions", uiTheme.getFactionColor());
        printBorder("divider");
        int count = 0;
        for (int i = 0; i < availableFactions.size(); i++) {
            Faction faction = availableFactions.get(i);
            if (!player.isInFaction(faction.getName())) {
                count++;
                int w = uiTheme.getMenuWidth();
                System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", count, faction.getName()) + " |", uiTheme.getTextColor()));
            }
        }
        if (count == 0) {
            printCenteredLine("No factions available to join.", uiTheme.getTextColor());
            printBorder("bottom");
            return;
        }
        int w = uiTheme.getMenuWidth();
        System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", count + 1, "Cancel") + " |", uiTheme.getTextColor()));
        printBorder("bottom");
        System.out.print(Color.colorize("Choose a faction (1-" + (count + 1) + "): ", uiTheme.getHighlightColor()));
        int choice = getChoice(1, count + 1);
        if (choice <= count) {
            int factionIdx = 0;
            for (Faction faction : availableFactions) {
                if (!player.isInFaction(faction.getName())) {
                    if (factionIdx == choice - 1) {
                        player.joinFaction(faction);
                        System.out.println(Color.colorize("You have joined the " + faction.getName() + "!", uiTheme.getSecondaryColor()));
                        return;
                    }
                    factionIdx++;
                }
            }
        }
    }

    private void viewFactions() {
        printBorder("top");
        printCenteredLine("Your Factions", Color.PURPLE);
        printBorder("divider");
        if (player.getFactions().isEmpty()) {
            printCenteredLine("You are not a member of any factions.", Color.GRAY);
        } else {
            for (Faction faction : player.getFactions()) {
                String factionLine = faction.getName() + " (Reputation: " + faction.getReputation() + ")";
                int w = uiTheme.getMenuWidth();
                System.out.println(Color.colorize("| " + String.format("%-" + (Math.max(0, w - 4)) + "s", factionLine) + " |", uiTheme.getTextColor()));
            }
        }
        printBorder("bottom");
    }

    private void doFactionQuest() {
        if (player.getFactions().isEmpty()) {
            printBorder("top");
            printCenteredLine("Faction Quest", Color.PURPLE);
            printBorder("divider");
            printCenteredLine("You must join a faction first!", Color.RED);
            printBorder("bottom");
            return;
        }
        printBorder("top");
        printCenteredLine("Choose a Faction Quest", Color.PURPLE);
        printBorder("divider");
        List<Faction> playerFactions = player.getFactions();
        for (int i = 0; i < playerFactions.size(); i++) {
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", i + 1, playerFactions.get(i).getName()) + " |", uiTheme.getTextColor()));
        }
        int w = uiTheme.getMenuWidth();
        System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", playerFactions.size() + 1, "Cancel") + " |", uiTheme.getTextColor()));
        printBorder("bottom");
        System.out.print(Color.colorize("Choose a faction (1-" + (playerFactions.size() + 1) + "): ", Color.YELLOW));
        int choice = getChoice(1, playerFactions.size() + 1);
        if (choice <= playerFactions.size()) {
            Faction faction = playerFactions.get(choice - 1);
            String questName = getFactionQuestName(faction.getName());
            String objective = getFactionQuestObjective(faction.getName());
            questManager.addQuest(
                    questName,
                    objective,
                    Arrays.asList(objective),
                    Map.of("gold", 100, "xp", 50),
                    faction.getName()
            );
            player.addFactionReputation(faction.getName(), 50);
        }
    }

    private String getFactionQuestName(String factionName) { 
        switch (factionName) {
            case "Companions":
                return "Clear Bandit Camp";
            case "Thieves Guild":
                return "Steal Noble Artifact";
            case "Dark Brotherhood":
                return "Assassinate Merchant";
            case "College of Winterhold":
                return "Retrieve Ancient Tome";
            case "Imperial Legion":
                return "Defend Supply Caravan";
            default:
                return "No Quest";
        }
    }

    private String getFactionQuestObjective(String factionName) {
        switch (factionName) {
            case "Companions":
                return "Clear a bandit camp near Whiterun Plains";
            case "Thieves Guild":
                return "Steal a valuable artifact in Riften Woods";
            case "Dark Brotherhood":
                return "Assassinate a corrupt merchant in Solitude Cliffs";
            case "College of Winterhold":
                return "Retrieve an ancient tome in Winterhold Tundra";
            case "Imperial Legion":
                return "Defend a supply caravan near Solitude Cliffs";
            default:
                return "No quest available";
        }
    }

    private int getChoice(int min, int max) {
        while (true) {
            if (stopRequested) {
                throw new GameStopException();
            }
            String line = scan.nextLine();
            if (stopRequested) {
                throw new GameStopException();
            }
            if (line == null) {
                line = "";
            }
            line = line.trim();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // --- [CORE GAMEPLAY & COMBAT LOGIC] ---
    public void travel() {
        setMoving(true); // Set movement state when traveling starts
        printBorder("top");
        printCenteredLine("Available Locations", Color.PURPLE);
        printBorder("divider");
        int i = 1;
        List<String> locations = new ArrayList<>(worldMap.keySet());
        for (String name : locations) {
            Location loc = worldMap.get(name);
            String locLine = String.format("%d. %s (Danger: %d/5)", i, name, loc.dangerLevel);
            printCenteredLine(locLine, uiTheme.getTextColor());
            printCenteredLine(loc.description, Color.GRAY);
            i++;
        }
        printBorder("bottom");
        System.out.print(Color.colorize("Choose a location (1-" + worldMap.size() + "): ", Color.YELLOW));
        try {
            int choice = getChoice(1, worldMap.size()) - 1;
            if (choice >= 0 && choice < worldMap.size()) {
                String destination = locations.get(choice);
                Location loc = worldMap.get(destination);
                enterLocation(loc);
            } else {
                System.out.println(Color.colorize("Invalid choice.", Color.RED));
            }
        } catch (GameStopException e) {
            throw e;
        }
    }

    private void enterLocation(Location loc) {
        if (stopRequested) {
            throw new GameStopException();
        }
        if (player == null) {    // safety-check
            System.err.println("enterLocation called before player was created – skipping.");
            return;
        }

        System.out.println(Color.colorize("\nYou arrive at " + loc.name, Color.YELLOW));
        System.out.println(Color.colorize(loc.description, Color.GRAY));

        // Generate procedural quest with low chance when entering locations
        if (random.nextFloat() < 0.2f) {
            questManager.generateProceduralQuest(loc, availableFactions);
        }

        if (loc.hasTown) {
            System.out.println(Color.colorize("\nThis location has a town where you can rest and trade.", Color.GREEN));
            encounterNPC(loc);
        }

        int encounters = 1 + random.nextInt(loc.dangerLevel);
        for (int i = 0; i < encounters; i++) {
            if (stopRequested) {
                throw new GameStopException();
            }
            if (random.nextFloat() < 0.6f) {
                generateEncounter(loc);
            } else {
                generateDiscovery(loc);
            }
        }
    }

    private void generateEncounter(Location loc) {
        if (stopRequested) {
            throw new GameStopException();
        }
        if (player == null) {
            System.out.println(Color.colorize("Error: No player selected. Please start a new game.", Color.RED));
            return;
        }

        setMoving(false);
        setCombat(true);

        Enemy.Tier tier = generateEnemyTierBasedOnDifficulty();
        enemy = new Enemy(tier, player.level, difficulty);
        enemy.changeName(loc.enemyPool[random.nextInt(loc.enemyPool.length)]);
        displayEnemyArt(enemy);
        String color = enemy.getTier() == Enemy.Tier.WEAK ? Color.GRAY
                : enemy.getTier() == Enemy.Tier.NORMAL ? Color.YELLOW : Color.RED;
        System.out.println("\n" + Color.colorize("You encounter a " + enemy.getDisplayName() + " in " + loc.name + "!", color));
        encounter(loc);
    }

    private Enemy.Tier generateEnemyTierBasedOnDifficulty() {
        int roll = random.nextInt(100);
        switch (difficulty) {
            case EASY:
                if (roll < 60) {
                    return Enemy.Tier.WEAK;
                } else if (roll < 90) {
                    return Enemy.Tier.NORMAL;
                } else {
                    return Enemy.Tier.STRONG;
                }
            case NORMAL:
                if (roll < 40) {
                    return Enemy.Tier.WEAK;
                } else if (roll < 70) {
                    return Enemy.Tier.NORMAL;
                } else {
                    return Enemy.Tier.STRONG;
                }
            case HARD:
                if (roll < 20) {
                    return Enemy.Tier.WEAK;
                } else if (roll < 50) {
                    return Enemy.Tier.NORMAL;
                } else {
                    return Enemy.Tier.STRONG;
                }
            default:
                return Enemy.Tier.NORMAL;
        }
    }

    private void displayEnemyArt(Enemy enemy) {
        String enemyName = enemy.getCurrentName();
        if (enemyName.equals("Dragon")) {
            System.out.println(Color.colorize(
                    "      /|\\\n"
                    + "    / 0 \\\n"
                    + "   / ===Y*===\n"
                    + "  /_______/",
                    Color.RED));
        } else if (enemyName.equals("Bandit")) {
            System.out.println(Color.colorize(
                    "    O\n"
                    + "   /|\\\n"
                    + "   / \\",
                    Color.YELLOW));
        }
    }

    private void encounter(Location loc) {
        if (player == null || enemy == null) {
            System.out.println(Color.colorize("Error: Combat cannot start. Player or enemy is missing.", Color.RED));
            return;
        }

        List<String> combatLog = new ArrayList<>();
        combatLog.add("Combat starts!");

        boolean envEffectsEnabled = true;
        if (loc.environmentalEffect != null && !loc.environmentalEffect.isEmpty()) {
            printBorder("top");
            printCenteredLine("Environment: " + loc.environmentalEffect, Color.PURPLE);
            printBorder("divider");
            printCenteredLine("Apply environmental effects this combat?", Color.YELLOW);
            printBorder("divider");
            printCenteredLine("1. Yes (recommended)", Color.GREEN);
            printCenteredLine("2. No (ignore environment)", Color.RED);
            printBorder("bottom");
            System.out.print(Color.colorize("Enter your choice (1-2): ", Color.YELLOW));
            int envChoice = getChoice(1, 2);
            envEffectsEnabled = (envChoice == 1);
        }

        try {
            while (player.hp > 0 && enemy.isAlive()) {
                if (stopRequested) {
                    throw new GameStopException();
                }

                System.out.print("\033[H\033[2J");
                System.out.flush();

                if (envEffectsEnabled) {
                    applyEnvironmentalEffects(player, enemy, loc);
                }
                player.updateStatusEffects();
                enemy.updateStatusEffects();

                if (player.hp <= 0) {
                    setCombat(false);
                    setMoving(false);
                    combatLog.add("Defeat! You have been slain by the " + enemy.getCurrentName() + "...");
                    System.out.println(Color.colorize("You have been defeated by the " + enemy.getCurrentName() + "...", Color.RED));

                    if (permadeathEnabled) {
                        System.out.println(Color.colorize("HARDCORE MODE: Your character has died permanently!", Color.RED));
                        System.out.println(Color.colorize("Game Over - Your journey ends here.", Color.RED));
                        deleteAllSaveFiles();
                        System.exit(0);
                    } else {
                        System.out.println(Color.colorize("Game Over - Returning to main menu.", Color.YELLOW));
                        this.enemy = null;
                        this.player = null;
                        if (ui != null) {
                            ui.showMainMenu();
                        }
                        throw new GameStopException(); // Stop the game thread
                    }
                }

                displayCombatStatus(player, enemy, combatLog);

                printBorder("top");
                printCenteredLine("Your turn! Choose an action:", Color.YELLOW);
                printBorder("divider");
                printCenteredLine("1. Attack", uiTheme.getTextColor());
                printCenteredLine("2. Use Skill", uiTheme.getTextColor());
                printCenteredLine("3. Use Shout", uiTheme.getTextColor());
                printCenteredLine("4. Flee", uiTheme.getTextColor());
                printBorder("bottom");
                System.out.print(Color.colorize("Choose an option (1-4): ", Color.YELLOW));
                int choice = getChoice(1, 4);

                boolean dodgeAttack = checkDodge(player);

                if (choice == 1) {
                    performPlayerAttack(player, enemy, combatLog);
                } else if (choice == 2) {
                    performPlayerSkill(player, enemy, combatLog);
                } else if (choice == 3) {
                    performPlayerShout(player, enemy, combatLog);
                } else {
                    if (random.nextInt(100) < 50) {
                        combatLog.add(player.getClassName() + " flees from battle!");
                        System.out.println(Color.colorize("You fled from the battle!", Color.YELLOW));
                        setCombat(false);
                        setMoving(true);
                        return;
                    } else {
                        combatLog.add(player.getClassName() + " fails to flee!");
                        System.out.println(Color.colorize("You failed to flee!", Color.RED));
                    }
                }

                if (player instanceof Architect && random.nextInt(100) < 15) {
                    int counterDmg = Combat.calculateDamage(player.minDmg, player, enemy, 0);
                    combatLog.add(player.getClassName() + " counterattacks for " + counterDmg + " damage!");
                    System.out.println(Color.colorize(player.getClassName() + " counterattacks for " + counterDmg + " damage!", Color.GREEN));
                    enemy.receiveDamage(counterDmg);
                }

                player.decrementCooldowns();

                if (!enemy.isAlive()) {
                    handleVictory(player, combatLog, enemy);
                    break;
                }

                if (dodgeAttack || enemy.stunnedForNextTurn) {
                    combatLog.add("Enemy is stunned or you dodged, so they skip their turn!");
                    System.out.println(Color.colorize("Enemy is stunned or you dodged, so they skip their turn!", Color.GREEN));
                    enemy.stunnedForNextTurn = false;
                } else {
                    if (player instanceof PenTester && ((PenTester) player).smokeBombActive) {
                        combatLog.add("Enemy's attack is weakened by Smoke Bomb!");
                        System.out.println(Color.colorize("Enemy's attack is weakened by Smoke Bomb!", Color.YELLOW));
                        int originalMinDmg = enemy.minDmg;
                        int originalMaxDmg = enemy.maxDmg;
                        enemy.minDmg = (int) (enemy.minDmg * 0.5);
                        enemy.maxDmg = (int) (enemy.maxDmg * 0.5);
                        enemy.takeTurn(player);
                        enemy.minDmg = originalMinDmg;
                        enemy.maxDmg = originalMaxDmg;
                    } else {
                        enemy.takeTurn(player);
                    }
                }

                if (player.hp <= 0) {
                    setCombat(false);
                    setMoving(false);
                    combatLog.add("Defeat! You have been slain by the " + enemy.getCurrentName() + "...");
                    System.out.println(Color.colorize("You have been defeated by the " + enemy.getCurrentName() + "...", Color.RED));

                    if (permadeathEnabled) {
                        System.out.println(Color.colorize("HARDCORE MODE: Your character has died permanently!", Color.RED));
                        System.out.println(Color.colorize("Game Over - Your journey ends here.", Color.RED));
                        deleteAllSaveFiles();
                        System.exit(0); // Exit the game
                    } else {
                        System.out.println(Color.colorize("Game Over - Returning to main menu.", Color.YELLOW));
                        System.out.println("Press Enter to continue...");
                        scan.nextLine();
                        this.enemy = null;
                        // player is set to null, which is correct.
                        this.player = null;
                        if (ui != null) {
                            ui.showMainMenu();
                        }
                        throw new GameStopException();
                    }
                }
            }
        } catch (GameStopException e) {
            System.out.println(Color.colorize("Combat interrupted by request. Exiting combat.", Color.YELLOW));
            return;
        }

        if (player != null && player.checkLevelUp()) {
            player.levelUp();
            combatLog.add(player.getClassName() + " levels up to " + player.level + "!");
        }

        System.out.println("\n" + Color.colorize("=== Combat Log Summary ===", Color.YELLOW));
        combatLog.forEach(System.out::println);
    }

    private boolean checkDodge(Hero player) {
        if (player instanceof Hacker && random.nextInt(100) < 20) {
            System.out.println(Color.colorize(player.getClassName() + " dodges the enemy’s next attack!", Color.GREEN));
            return true;
        } else if (player instanceof PenTester && random.nextInt(100) < 10) {
            System.out.println(Color.colorize("PenTester dodges the enemy’s next attack!", Color.GREEN));
            return true;
        }
        return false;
    }

    private void performPlayerAttack(Hero player, Enemy enemy, List<String> combatLog) {
        int baseDamage = player.minDmg + random.nextInt(player.maxDmg - player.minDmg + 1);
        int damage = Combat.calculateDamage(baseDamage, player, enemy, 0);

        combatLog.add(player.getClassName() + " attacks for " + damage + " damage!");
        System.out.println(Color.colorize("You attack for " + damage + " damage!", uiTheme.getTextColor()));
        enemy.receiveDamage(damage);
    }

    private void performPlayerSkill(Hero player, Enemy enemy, List<String> combatLog) {
        player.showAttacks();
        System.out.print(Color.colorize("Choose a skill (1-" + player.attackNames.length + "): ", Color.YELLOW));
        int skillChoice = getChoice(1, player.attackNames.length);
        String skillName = player.attackNames[skillChoice - 1];

        if (player instanceof Support) {
            if (skillName.equals("Fireball")) {
                int damage = Combat.calculateDamage(player.minDmg + 10, player, enemy, 0);
                enemy.receiveDamage(damage);
                enemy.applyStatusEffect(new StatusEffect("Burn", 3, 1.0, "damage", 5));
                combatLog.add(player.getClassName() + " casts Fireball for " + damage + " damage and applies Burn!");
            } else if (skillName.equals("Ice Storm")) {
                int damage = Combat.calculateDamage(player.minDmg + 5, player, enemy, 0);
                enemy.receiveDamage(damage);
                enemy.applyStatusEffect(new StatusEffect("Freeze", 2, 0.5, "damage", 0));
                combatLog.add(player.getClassName() + " casts Ice Storm for " + damage + " damage and applies Freeze!");
            } else {
                player.useSkill(skillChoice - 1, enemy);
                combatLog.add(player.getClassName() + " uses " + skillName);
            }
        } else {
            player.useSkill(skillChoice - 1, enemy);
            combatLog.add(player.getClassName() + " uses " + skillName);
        }
    }

    private void performPlayerShout(Hero player, Enemy enemy, List<String> combatLog) {
        player.showAttacks();
        System.out.print(Color.colorize("Choose a shout (1-" + player.getShouts().length + "): ", Color.YELLOW));
        int shoutChoice = getChoice(1, player.getShouts().length);
        String shoutName = player.getShouts()[shoutChoice - 1];

        player.useShout(shoutChoice - 1, enemy);
        combatLog.add(player.getClassName() + " shouts " + shoutName);
    }

    private void handleVictory(Hero player, List<String> combatLog, Enemy enemy) {
        setCombat(false);
        setMoving(true);
        int baseGold = 20;
        int baseXP = 20;
        double goldMultiplier = 1.0;
        double xpMultiplier = 1.0;
        switch (difficulty) {
            case EASY:
                goldMultiplier = 1.3;
                xpMultiplier = 1.2;
                break;
            case HARD:
                goldMultiplier = 0.7;
                xpMultiplier = 1.5;
                break;
            case NORMAL:
            default:
                goldMultiplier = 1.0;
                xpMultiplier = 1.0;
                break;
        }
        int goldReward = (int) (baseGold * goldMultiplier);
        int xpReward = (int) (baseXP * xpMultiplier);
        player.addGold(goldReward);
        player.addXP(xpReward);
        combatLog.add("You gain " + goldReward + " gold and " + xpReward + " XP.");
        String color = enemy.getTier() == Enemy.Tier.WEAK ? Color.GRAY
                : enemy.getTier() == Enemy.Tier.NORMAL ? Color.YELLOW : Color.RED;
        combatLog.add("You defeated the " + Color.colorize(enemy.getDisplayName(), color) + "!");
        if (enemy.getCurrentName().equals("Virus")) {
            questManager.updateQuest("Defeat a Virus in Data Stream Valley", player);
            questManager.updateQuest("Clear a virus camp near Central Server Hub", player);
        }
    }

    private void applyEnvironmentalEffects(Hero player, Enemy enemy, Location loc) {
        if (loc.environmentalEffect == null) {
            return;
        }

        System.out.println(Color.colorize("The environment affects the battle!", Color.PURPLE));

        switch (loc.environmentalEffect) {
            case "Blizzard":
                System.out.println(Color.colorize("A fierce blizzard rages, dealing 5 damage to all combatants.", Color.BLUE));
                player.receiveDamage(Combat.calculateDamage(5, null, player, 0));
                enemy.receiveDamage(Combat.calculateDamage(5, null, enemy, 0));
                break;
            case "Cave-in":
                if (random.nextInt(100) < 10) {
                    System.out.println(Color.colorize("The cave trembles! A rock falls, dealing 20 damage to a random combatant.", Color.RED));
                    if (random.nextBoolean()) {
                        player.receiveDamage(Combat.calculateDamage(20, null, player, 0));
                    } else {
                        enemy.receiveDamage(Combat.calculateDamage(20, null, enemy, 0));
                    }
                }
                break;
            case "Thick Fog":
                System.out.println(Color.colorize("Thick fog reduces accuracy for all combatants.", Color.GRAY));
                break;
            case "Glowing Spores":
                System.out.println(Color.colorize("Glowing spores heal all combatants for 5 HP.", Color.GREEN));
                player.hp = Math.min(player.maxHP, player.hp + 5);
                enemy.hp = Math.min(enemy.maxHP, enemy.hp + 5);
                break;
            case "Soul Drain":
                System.out.println(Color.colorize("The Soul Cairn drains 5 mana from the player.", Color.PURPLE));
                player.mana = Math.max(0, player.mana - 5);
                break;
            case "Ancient Power":
                System.out.println(Color.colorize("The Forgotten Vale's ancient power boosts the player's damage by 10%.", Color.YELLOW));
                player.minDmg = (int) (player.minDmg * 1.1);
                player.maxDmg = (int) (player.maxDmg * 1.1);
                break;
            case "Soothing Vapors":
                System.out.println(Color.colorize("Soothing vapors from the hot springs restore 10 HP to all combatants.", Color.GREEN));
                player.hp = Math.min(player.maxHP, player.hp + 10);
                enemy.hp = Math.min(enemy.maxHP, enemy.hp + 10);
                break;
            case "Vampiric Aura":
                System.out.println(Color.colorize("A vampiric aura drains 5 HP from the player and heals the enemy.", Color.RED));
                player.receiveDamage(Combat.calculateDamage(5, null, player, 0));
                enemy.hp = Math.min(enemy.maxHP, enemy.hp + 5);
                break;
            case "Forbidden Knowledge":
                System.out.println(Color.colorize("Forbidden knowledge from Apocrypha boosts the player's mana regeneration by 5.", Color.BLUE));
                player.mana = Math.min(player.maxMana, player.mana + 5);
                break;
        }
    }

    private void displayCombatStatus(Hero player, Enemy enemy, List<String> combatLog) {
        printBorder("top");
        printCenteredLine("=== Combat Status ===", Color.YELLOW);
        printBorder("divider");
        System.out.printf("| %-30s | %-30s |%n",
                player.getClassName() + " (Lv " + player.level + ")",
                enemy.getDisplayName() + " (Lv " + enemy.level + ")");
        System.out.printf("| HP: %-25s | HP: %-25s |%n",
                player.hp + "/" + player.maxHP + " [" + "=".repeat(Math.max(0, player.hp * 20 / player.maxHP)) + "]",
                enemy.hp + "/" + enemy.maxHP + " [" + "=".repeat(Math.max(0, enemy.hp * 20 / enemy.maxHP)) + "]");
        System.out.printf("| Mana: %-25s | %-30s |%n",
                player.mana + "/" + player.maxMana + " [" + "=".repeat(Math.max(0, player.mana * 20 / player.maxMana)) + "]", "");
        String playerStatus = player.getStatusEffects().stream().map(StatusEffect::getName).collect(Collectors.joining(", "));
        String enemyStatus = enemy.getStatusEffects().stream().map(StatusEffect::getName).collect(Collectors.joining(", "));
        if (!playerStatus.isEmpty() || !enemyStatus.isEmpty()) {
            System.out.printf("| Status: %-25s | Status: %-25s |%n", playerStatus, enemyStatus);
        }
        printBorder("divider");
        int previewDmg = Combat.calculateDamage(enemy.minDmg, enemy, player, 0);
        String intentLine = String.format("| Next enemy action: ~%d damage%s", previewDmg, enemy.stunnedForNextTurn ? " (stunned)" : "");
        System.out.println(Color.colorize(intentLine, Color.YELLOW));
        System.out.println("| Recent Actions:");
        int start = Math.max(0, combatLog.size() - 3);
        for (int i = start; i < combatLog.size(); i++) {
            System.out.println("| " + combatLog.get(i));
        }
        printBorder("bottom");
    }

    private String getItemRarity(String itemName) {
        if (itemName == null) {
            return uiTheme.getTextColor();
        }
        if (itemName.contains("Dragonbone") || itemName.contains("Dawnbreaker")
                || itemName.contains("Chillrend") || itemName.contains("Dragonbane")
                || itemName.contains("Archmage") || itemName.contains("Daedric")) {
            return Color.PURPLE;
        } else if (itemName.contains("Elven") || itemName.contains("Glass")
                || itemName.contains("Greater") || itemName.contains("Major")
                || itemName.contains("Cloak of Shadows") || itemName.contains("Nightshade")
                || itemName.contains("Orb of Elements")) {
            return Color.BLUE;
        } else if (itemName.contains("Steel") || itemName.contains("Mithril")
                || itemName.contains("Leather") || itemName.contains("Mana")
                || itemName.contains("Chainmail") || itemName.contains("Composite")
                || itemName.contains("Longbow")) {
            return Color.GREEN;
        } else {
            return uiTheme.getTextColor();
        }
    }

    private void generateDiscovery(Location loc) {
        if (stopRequested) {
            throw new GameStopException();
        }
        if (player == null) {    // safety-check to avoid NPE
            System.err.println("generateDiscovery called before player was created – skipping.");
            return;
        }

        String[] discoveries = {
            "You find an abandoned campsite",
            "You discover a hidden cave",
            "You stumble upon an ancient relic",
            "You meet a traveling merchant"
        };

        String discovery = discoveries[random.nextInt(discoveries.length)];
        System.out.println("\n" + Color.colorize(discovery + " in " + loc.name, Color.GREEN));

        if (discovery.contains("relic")) {
            player.addItem("Ancient Relic", 1.0f);
            System.out.println(Color.colorize("You found an Ancient Relic!", Color.YELLOW));
            if (loc.name.equals("Central Server Hub")) {
                questManager.updateQuest("Find code snippet in Central Server Hub", player);
            }
            questManager.updateQuest("Find a Lost Relic for " + loc.name, player);
        }

        String[] loot = {"gold", "potion", "weapon", "armor", "food", "misc"};
        String found = loot[random.nextInt(loot.length)];

        if (found.equals("gold")) {
            int amount = 10 + random.nextInt(20 * loc.dangerLevel);
            player.addGold(amount);
            System.out.println(Color.colorize("You found " + amount + " gold!", Color.YELLOW));
        } else if (found.equals("potion")) {
            String[] potions = {
                "Health Potion", "Mana Potion", "Greater Health Potion", "Major Health Potion",
                "Minor Health Potion", "Greater Mana Potion", "Major Mana Potion", "Minor Mana Potion",
                "Antidote Potion", "Fire Resistance Potion", "Frost Resistance Potion",
                "Poison Resistance Potion", "Healing Elixir", "Potion of Ultimate Healing"
            };
            String potion = potions[random.nextInt(potions.length)];
            player.addItem(potion, 0.5f);
            System.out.println(Color.colorize("You found a " + potion + "!", getItemRarity(potion)));
        } else if (found.equals("weapon")) {
            String[] classWeapons = player instanceof Debugger ? new String[]{
                "Iron Sword", "Steel Sword", "Mithril Sword", "Elven Sword", "Glass Sword",
                "Daedric Sword", "Dragonbone Sword", "Dawnbreaker", "Chillrend", "Dragonbane"
            }
                    : player instanceof Hacker ? new String[]{
                        "Fire Staff", "Ice Wand", "Staff of Fireballs", "Staff of Ice Storms",
                        "Staff of Healing", "Wand of Mana", "Orb of Elements"
                    }
                    : player instanceof Tester ? new String[]{
                        "Hunting Bow", "Longbow", "Composite Bow", "Elven Bow", "Glass Bow",
                        "Daedric Bow", "Dragonbone Bow"
                    }
                    : player instanceof Architect ? new String[]{
                        "Warhammer", "Battleaxe", "Mace", "Flail"
                    }
                    : player instanceof PenTester ? new String[]{
                        "Iron Dagger", "Steel Dagger", "Mithril Dagger", "Elven Dagger", "Glass Dagger",
                        "Daedric Dagger", "Ebony Dagger"
                    }
                    : new String[]{
                        "Staff of Healing", "Holy Scepter", "Divine Mace"
                    };
            String weapon = classWeapons[random.nextInt(classWeapons.length)];
            player.addItem(weapon, 2.0f);
            System.out.println(Color.colorize("You found a " + weapon + "!", getItemRarity(weapon)));
        } else if (found.equals("armor")) {
            String[] classNames = player instanceof Debugger ? new String[]{
                "Plate Armor", "Dragonbone Armor"
            } : player instanceof Hacker ? new String[]{"Robe of Protection", "Archmage Robes"
            } : player instanceof Tester ? new String[]{
                "Leather Armor", "Elven Armor"
            } : player instanceof Architect ? new String[]{
                "Chainmail", "Dragonscale Armor"
            } : player instanceof PenTester ? new String[]{
                "Cloak of Shadows", "Nightshade Cloak"
            } : new String[]{"Robe of Protection", "Holy Shroud"};
            String armor = classNames[random.nextInt(classNames.length)];
            player.addItem(armor, 3.0f);
            System.out.println(Color.colorize("You found a " + armor + "!", getItemRarity(armor)));
        } else if (found.equals("food")) {
            String[] foods = {
                "Apple", "Bread Loaf", "Cheese Wheel", "Roasted Meat", "Vegetable Stew"
            };
            String food = foods[random.nextInt(foods.length)];
            player.addItem(food, 0.4f);
            System.out.println(Color.colorize("You found a " + food + "!", getItemRarity(food)));
        } else if (found.equals("misc")) {
            String[] misc = {"Torch", "Map of the Realm", "Ancient Coin", "Silver Ring", "Amulet of Talos"};
            String item = misc[random.nextInt(misc.length)];
            player.addItem(item, 0.3f);
            System.out.println(Color.colorize("You found a " + item + "!", getItemRarity(item)));
        }
    }

    private void encounterNPC(Location loc) {
        try {
            if (stopRequested) {
                throw new GameStopException();
            }

            printBorder("top");
            printCenteredLine("Town of " + loc.name, Color.PURPLE);
            printBorder("divider");
            printCenteredLine("You enter a bustling town...", Color.YELLOW);

            int npcCount = 4 + random.nextInt(2);
            List<Enemy> npcs = new ArrayList<>();
            List<String> npcNames = new ArrayList<>();
            LinkedList<String> availableNames = new LinkedList<>();
            availableNames.addAll(Arrays.asList(Enemy.listEnemyNames()));

            for (int i = 0; i < npcCount && !availableNames.isEmpty(); i++) {
                if (stopRequested) {
                    throw new GameStopException();
                }
                Enemy npc = new Enemy();
                String uniqueName = availableNames.remove(random.nextInt(availableNames.size()));
                npc.changeName(uniqueName);
                int roll = random.nextInt(100);
                npc.setHostile(roll < 50);
                npcs.add(npc);
                String color = npc.isHostile() ? Color.RED : Color.GREEN;
                npcNames.add(Color.colorize(npc.getDisplayName(), color));
                int w = uiTheme.getMenuWidth();
                System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", i + 1, npcNames.get(i)) + " |", uiTheme.getTextColor()));
            }
            printBorder("bottom");

            while (true) {
                if (stopRequested) {
                    throw new GameStopException();
                }
                printBorder("top");
                printCenteredLine("Town Options", Color.PURPLE);
                printBorder("divider");
                for (int i = 0; i < npcs.size(); i++) {
                    int w = uiTheme.getMenuWidth();
                    System.out.println(Color.colorize("| " + String.format("%d. Interact with %-" + (Math.max(0, w - 14)) + "s", i + 1, npcNames.get(i)) + " |", uiTheme.getTextColor()));
                }
                int w = uiTheme.getMenuWidth();
                System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", npcs.size() + 1, "Continue Exploring") + " |", uiTheme.getTextColor()));
                System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", npcs.size() + 2, "Leave Town") + " |", uiTheme.getTextColor()));
                printBorder("bottom");
                System.out.print(Color.colorize("Choose an option (1-" + (npcs.size() + 2) + "): ", Color.YELLOW));
                int choice = getChoice(1, npcs.size() + 2);

                try {
                    if (choice >= 1 && choice <= npcs.size()) {
                        Enemy npc = npcs.get(choice - 1);
                        if (npc.isHostile()) {
                            interactWithHostileNPC(npc, loc.name);
                            return;
                        } else {
                            System.out.println(Color.colorize("This NPC is friendly and cannot be fought.", Color.GREEN));
                            interactWithDocileNPC(npc.getCurrentName(), loc);
                        }
                    } else if (choice == npcs.size() + 1) {
                        continueExploring(loc);
                    } else if (choice == npcs.size() + 2) {
                        System.out.println(Color.colorize("You leave the town.", Color.YELLOW));
                        break;
                    } else {
                        System.out.println(Color.colorize("Invalid choice.", Color.RED));
                    }
                } catch (GameStopException e) {
                    throw e;
                }
            }
        } catch (GameStopException e) {
            System.out.println(Color.colorize("Town interaction interrupted by request.", Color.YELLOW));
            throw e;
        }
    }

    private void continueExploring(Location loc) {
        if (stopRequested) {
            throw new GameStopException();
        }
        System.out.println(Color.colorize("\nYou continue exploring " + loc.name + "...", Color.YELLOW));
        if (random.nextFloat() < 0.5f) {
            generateRandomNPCEncounter(loc);
        } else {
            System.out.println(Color.colorize("You find nothing of interest.", Color.GRAY));
        }
    }

    private void generateRandomNPCEncounter(Location loc) {
        if (stopRequested) {
            throw new GameStopException();
        }
        if (player == null) {
            System.out.println(Color.colorize("Error: No player selected. Please start a new game.", Color.RED));
            return;
        }
        Enemy npc = new Enemy(Enemy.Tier.values()[random.nextInt(3)], player.level);
        int roll = random.nextInt(100);
        npc.setHostile(roll < 50);
        String color = npc.isHostile() ? Color.RED : Color.GREEN;
        System.out.println("\n" + Color.colorize("You encounter a " + npc.getDisplayName() + " while exploring " + loc.name + "!", color));
        if (npc.isHostile()) {
            interactWithHostileNPC(npc, loc.name);
            return;
        } else {
            System.out.println(Color.colorize("This NPC is friendly and cannot be fought.", Color.GREEN));
            interactWithDocileNPC(npc.getCurrentName(), loc);
        }
    }

    private void interactWithHostileNPC(Enemy npc, String location) {
        if (!npc.isHostile()) {
            System.out.println(Color.colorize(npc.getCurrentName() + " is not hostile. You cannot fight them.", Color.YELLOW));
            interactWithDocileNPC(npc.getCurrentName(), worldMap.get(location));
            return;
        }
        System.out.println(Color.colorize(npc.getDisplayName() + " attacks!", Color.RED));
        this.enemy = npc;
        encounter(worldMap.get(location));
    }

    private void interactWithDocileNPC(String npcName, Location loc) {
        printBorder("top");
        printCenteredLine("Friendly NPC: " + npcName, Color.GREEN);
        printBorder("divider");
        while (true) {
            if (stopRequested) {
                throw new GameStopException();
            }
            printCenteredLine("What would you like to do?", uiTheme.getHighlightColor());
            printCenteredLine("1. Trade (buy/sell items)", uiTheme.getTextColor());
            printCenteredLine("2. Talk (learn about " + loc.name + ")", uiTheme.getTextColor());
            printCenteredLine("3. Leave", uiTheme.getTextColor());
            printBorder("bottom");
            System.out.print(Color.colorize("Choose an option (1-3): ", Color.YELLOW));
            String choice = scan.nextLine().trim();
            if (choice.equals("1")) {
                tradeWithNPC(npcName);
            } else if (choice.equals("2")) {
                talkToNPC(npcName, loc);
            } else if (choice.equals("3")) {
                System.out.println(Color.colorize("You leave " + npcName + ".", Color.YELLOW));
                break;
            } else {
                System.out.println(Color.colorize("Invalid option. Try again.", Color.RED));
            }
        }
    }

    private void tradeWithNPC(String npcName) {
        while (true) {
            if (stopRequested) {
                throw new GameStopException();
            }
            printBorder("top");
            printCenteredLine("Trading with " + npcName, Color.PURPLE);
            printBorder("divider");
            printCenteredLine("1. Buy Items", uiTheme.getTextColor());
            printCenteredLine("2. Sell Items", uiTheme.getTextColor());
            printCenteredLine("3. Cancel", uiTheme.getTextColor());
            printBorder("bottom");
            System.out.print(Color.colorize("Choose an option (1-3): ", Color.YELLOW));

            String choice = scan.nextLine().trim();
            if (choice.equals("1")) {
                buyItem(npcName);
            } else if (choice.equals("2")) {
                sellItem(npcName);
            } else if (choice.equals("3")) {
                System.out.println(Color.colorize("You stop trading with " + npcName + ".", Color.GREEN));
                break;
            } else {
                System.out.println(Color.colorize("Invalid option. Try again.", Color.RED));
            }
        }
    }

    private void buyItem(String npcName) {
        printBorder("top");
        printCenteredLine("Items for Sale from " + npcName, Color.PURPLE);
        printBorder("divider");
        String[] items;
        int[] prices;
        if (player instanceof Debugger) {
            items = new String[]{
                "Iron Sword", "Steel Sword", "Mithril Sword", "Elven Sword", "Glass Sword",
                "Daedric Sword", "Dragonbone Sword", "Dawnbreaker", "Chillrend", "Dragonbane",
                "Plate Armor", "Dragonbone Armor",
                "Health Potion", "Potion of Ultimate Healing", "Amulet of Talos"
            };
            prices = new int[]{10, 15, 20, 25, 30, 35, 40, 45, 42, 40, 20, 35, 5, 15, 10};
        } else if (player instanceof Hacker) {
            items = new String[]{
                "Fire Staff", "Ice Wand", "Staff of Fireballs", "Staff of Ice Storm",
                "Staff of Healing", "Wand of Lightning", "Orb of Elements",
                "Robe of Protection", "Archmage Robes",
                "Mana Potion", "Potion of Ultimate Healing", "Amulet of Talos"
            };
            prices = new int[]{15, 20, 25, 30, 35, 40, 45, 15, 25, 7, 15, 10};
        } else if (player instanceof Tester) {
            items = new String[]{
                "Hunting Bow", "Longbow", "Composite Bow", "Elven Bow", "Glass Bow",
                "Daedric Bow", "Dragonbone Bow",
                "Leather Armor", "Elven Armor",
                "Health Potion", "Potion of Ultimate Healing", "Amulet of Talos"
            };
            prices = new int[]{10, 15, 20, 25, 30, 35, 40, 15, 25, 5, 15, 10};
        } else if (player instanceof Architect) {
            items = new String[]{
                "Warhammer", "Battleaxe", "Mace", "Flail",
                "Chainmail", "Dragonscale Armor",
                "Health Potion", "Potion of Ultimate Healing", "Amulet of Talos"
            };
            prices = new int[]{15, 20, 25, 30, 20, 35, 5, 15, 10};
        } else if (player instanceof PenTester) {
            items = new String[]{
                "Iron Dagger", "Steel Dagger", "Mithril Dagger", "Elven Dagger", "Glass Dagger",
                "Daedric Dagger", "Ebony Dagger",
                "Cloak of Shadows", "Nightshade Cloak",
                "Health Potion", "Potion of Ultimate Healing", "Amulet of Talos"
            };
            prices = new int[]{10, 15, 20, 25, 30, 35, 40, 15, 25, 5, 15, 10};
        } else {
            items = new String[]{
                "Staff of Healing", "Holy Scepter", "Divine Mace",
                "Robe of Protection", "Holy Shroud",
                "Mana Potion", "Potion of Ultimate Healing", "Amulet of Talos"
            };
            prices = new int[]{15, 20, 25, 15, 25, 7, 15, 10};
        }

        for (int i = 0; i < items.length; i++) {
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 14)) + "s", i + 1, items[i] + " (" + prices[i] + " gold)") + " |", getItemRarity(items[i])));
        }
        int w = uiTheme.getMenuWidth();
        System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", items.length + 1, "Cancel") + " |", uiTheme.getTextColor()));
        printBorder("bottom");
        System.out.print(Color.colorize("Choose an item to buy (1-" + (items.length + 1) + "): ", Color.YELLOW));
        int choice = getChoice(1, items.length + 1);

        if (choice <= items.length) {
            String item = items[choice - 1];
            int price = prices[choice - 1];
            if (player.spendGold(price)) {
                player.addItem(item, 1.0f);
                System.out.println(Color.colorize("You bought a " + item + " for " + price + " gold!", getItemRarity(item)));
            } else {
                System.out.println(Color.colorize("You don't have enough gold!", Color.RED));
            }
        } else {
            System.out.println(Color.colorize("Purchase cancelled.", Color.YELLOW));
        }
    }

    private void sellItem(String npcName) {
        printBorder("top");
        printCenteredLine("Your Inventory for Sale to " + npcName, Color.PURPLE);
        printBorder("divider");
        List<Hero.InventoryItem> inventory = player.getInventory();
        if (inventory.isEmpty()) {
            printCenteredLine("Your inventory is empty!", Color.GRAY);
            printBorder("bottom");
            return;
        }

        int[] prices = new int[inventory.size()];
        for (int i = 0; i < inventory.size(); i++) {
            Hero.InventoryItem item = inventory.get(i);
            prices[i] = (int) (Math.random() * 10 + 5); // Simple sell price
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 14)) + "s", i + 1, item.name + " x" + item.quantity + " (" + prices[i] + " gold)") + " |", getItemRarity(item.name)));
        }
        int w = uiTheme.getMenuWidth();
        System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", inventory.size() + 1, "Cancel") + " |", Color.WHITE));
        printBorder("bottom");
        System.out.print(Color.colorize("Choose an item to sell (1-" + (inventory.size() + 1) + "): ", Color.YELLOW));

        int choice = getChoice(1, inventory.size() + 1);

        if (choice <= inventory.size()) {
            Hero.InventoryItem item = inventory.get(choice - 1);
            int price = prices[choice - 1];
            player.removeItem(item.name);
            player.addGold(price);
            System.out.println(Color.colorize("You sold a " + item.name + " for " + price + " gold!", getItemRarity(item.name)));
        } else {
            System.out.println(Color.colorize("Sale cancelled.", Color.YELLOW));
        }
    }

    private void talkToNPC(String npcName, Location loc) {
        printBorder("top");
        printCenteredLine("Conversation with " + npcName, Color.GREEN);
        printBorder("divider");
        String[] rumors = {
            npcName + " shares a tale about a hidden treasure in " + loc.name + ".",
            npcName + " warns you about a dangerous " + loc.enemyPool[random.nextInt(loc.enemyPool.length)] + " nearby.",
            npcName + " mentions a local festival happening soon in " + loc.name + ".",
            npcName + " offers insight about a powerful artifact lost in " + loc.name + "."
        };
        String rumor = rumors[random.nextInt(rumors.length)];
        int w = uiTheme.getMenuWidth();
        System.out.println(Color.colorize("| " + String.format("%-" + (Math.max(0, w - 4)) + "s", rumor) + " |", Color.WHITE));
        printBorder("bottom");
        if (rumor.contains("powerful artifact")) {
            questManager.addQuest(
                    "Find a Lost Relic for " + loc.name,
                    "Locate the artifact mentioned by " + npcName + " in " + loc.name + ".",
                    Arrays.asList("Find a Lost Relic for " + loc.name),
                    Map.of("gold", 150, "xp", 75),
                    null
            );
        }
        if (loc.name.equals("Whiterun Plains")) { // Example quest update
            questManager.updateQuest("Return to Whiterun", player);
        }
    }

    private void viewInventory() {
        printBorder("top");
        printCenteredLine("Your Inventory", Color.PURPLE);
        printBorder("divider");
        List<Hero.InventoryItem> inventory = player.getInventory();
        if (inventory.isEmpty()) {
            printCenteredLine("Your inventory is empty!", Color.GRAY);
        } else {
            for (Hero.InventoryItem item : inventory) {
                String itemLine = item.name + " x" + item.quantity + " (Weight: " + item.weight + ")";
                int w = uiTheme.getMenuWidth();
                System.out.println(Color.colorize("| " + String.format("%-" + (Math.max(0, w - 4)) + "s", itemLine) + " |", getItemRarity(item.name)));
            }
        }
        printBorder("bottom");
    }

    private void viewEquipment() {
        printBorder("top");
        printCenteredLine("Your Equipment", Color.PURPLE);
        printBorder("divider");
        printCenteredLine("Weapon: " + equippedWeapon, getItemRarity(equippedWeapon));
        printCenteredLine("Armor: " + equippedArmor, getItemRarity(equippedArmor));
        printBorder("bottom");
    }

    private void equipOrUseItem() {
        printBorder("top");
        printCenteredLine("Equip or Use Item", Color.PURPLE);
        printBorder("divider");
        List<Hero.InventoryItem> inventory = player.getInventory();
        if (inventory.isEmpty()) {
            printCenteredLine("Your inventory is empty!", Color.GRAY);
            printBorder("bottom");
            return;
        }

        for (int i = 0; i < inventory.size(); i++) {
            Hero.InventoryItem item = inventory.get(i);
            int w = uiTheme.getMenuWidth();
            System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", i + 1, item.name) + " |", getItemRarity(item.name)));
        }
        int w = uiTheme.getMenuWidth();
        System.out.println(Color.colorize("| " + String.format("%d. %-" + (Math.max(0, w - 6)) + "s", inventory.size() + 1, "Cancel") + " |", Color.WHITE));
        printBorder("bottom");
        System.out.print(Color.colorize("Choose an item (1-" + (inventory.size() + 1) + "): ", Color.YELLOW));
        int choice = getChoice(1, inventory.size() + 1);

        if (choice <= inventory.size()) {
            Hero.InventoryItem item = inventory.get(choice - 1);
            String itemName = item.name;
            if (itemName.toLowerCase().contains("potion") || itemName.toLowerCase().contains("elixir")) {
                if (itemName.toLowerCase().contains("health")) {
                    player.hp = Math.min(player.maxHP, player.hp + 50);
                    System.out.println(Color.colorize("You used a " + itemName + " and restored 50 HP!", getItemRarity(itemName)));
                } else if (itemName.toLowerCase().contains("mana")) {
                    player.mana = Math.min(player.maxMana, player.mana + 50);
                    System.out.println(Color.colorize("You used a " + itemName + " and restored 50 Mana!", getItemRarity(itemName)));
                } else {

                    player.hp = Math.min(player.maxHP, player.hp + 20);
                    player.mana = Math.min(player.maxMana, player.mana + 20);
                    System.out.println(Color.colorize("You used a " + itemName + "!", getItemRarity(itemName)));
                }
                player.removeItem(itemName);
            } else if (itemName.contains("Sword") || itemName.contains("Staff") || itemName.contains("Bow")
                    || itemName.contains("Dagger") || itemName.contains("Mace") || itemName.contains("Warhammer")
                    || itemName.contains("Battleaxe") || itemName.contains("Flail") || itemName.contains("Wand")
                    || itemName.contains("Scepter")) {
                player.addItem(equippedWeapon, 2.0f); // Re-add old weapon to inventory
                equippedWeapon = itemName;
                player.removeItem(itemName);
                System.out.println(Color.colorize("You equipped " + itemName + "!", getItemRarity(itemName)));
            } else if (itemName.contains("Armor") || itemName.contains("Robe") || itemName.contains("Cloak")
                    || itemName.contains("Chainmail") || itemName.contains("Shroud")) {
                player.addItem(equippedArmor, 3.0f); // Re-add old armor to inventory
                equippedArmor = itemName;
                player.removeItem(itemName);
                System.out.println(Color.colorize("You equipped " + itemName + "!", getItemRarity(itemName)));
            } else {
                System.out.println(Color.colorize("You cannot use or equip " + itemName + ".", Color.RED));
            }
        } else {
            System.out.println(Color.colorize("Cancelled.", Color.YELLOW));
        }
    }

    private void viewGold() {
        printBorder("top");
        printCenteredLine("Your Treasury", uiTheme.getGoldColor());
        printBorder("divider");
        printCenteredLine("You have " + player.getGold() + " gold.", uiTheme.getTextColor());
        printBorder("bottom");
        System.out.println("\nPress Enter to continue...");
        scan.nextLine();
    }

    private void viewQuestLog() {
        printBorder("top");
        printCenteredLine("Quest Log", uiTheme.getQuestColor());
        printBorder("divider");

        List<Quest> active = questManager.getActiveQuests();
        if (active.isEmpty()) {
            printCenteredLine("No active quests.", Color.GRAY);
        } else {
            for (Quest quest : active) {
                printCenteredLine(quest.getName(), uiTheme.getTextColor());
                printCenteredLine(" - " + quest.getCurrentObjective(), Color.GRAY);
            }
        }

        printBorder("divider");
        printCenteredLine("Completed", uiTheme.getSecondaryColor());
        List<Quest> completed = questManager.getCompletedQuests();
        if (completed.isEmpty()) {
            printCenteredLine("No completed quests.", Color.GRAY);
        } else {
            for (Quest quest : completed) {
                printCenteredLine(quest.getName(), Color.GRAY);
            }
        }

        printBorder("bottom");
        System.out.println("\nPress Enter to continue...");
        scan.nextLine();
    }

    private void viewPlayerStats() {
        printBorder("top");
        printCenteredLine(player.getClassName() + " - Level " + player.level, uiTheme.getPrimaryColor());
        printBorder("divider");

        printCenteredLine("HP: " + player.hp + " / " + player.maxHP, uiTheme.getTextColor());
        printCenteredLine("Mana: " + player.mana + " / " + player.maxMana, uiTheme.getTextColor());
        printCenteredLine("Damage: " + player.minDmg + " - " + player.maxDmg, uiTheme.getTextColor());
        printCenteredLine("XP: " + player.xp + " / " + player.xpToLevel, uiTheme.getTextColor());
        printCenteredLine("Gold: " + player.gold, uiTheme.getGoldColor());

        printBorder("divider");
        printCenteredLine("Weapon: " + equippedWeapon, getItemRarity(equippedWeapon));
        printCenteredLine("Armor: " + equippedArmor, getItemRarity(equippedArmor));

        printBorder("bottom");
        System.out.println("\nPress Enter to continue...");
        scan.nextLine();
    }

    private void viewAchievements() {
        if (player != null) {
            player.getAchievementManager().displayAchievements();
        } else {
            printCenteredLine("No player active.", Color.RED);
        }
        System.out.println("\nPress Enter to continue...");
        scan.nextLine();
    }

    private void restAtInn() {
        int cost = 10;
        printBorder("top");
        printCenteredLine("Rest at Inn", uiTheme.getInnColor());
        printBorder("divider");
        printCenteredLine("Resting costs " + cost + " gold.", uiTheme.getTextColor());
        printCenteredLine("This will restore all HP and Mana.", uiTheme.getTextColor());
        printCenteredLine("Your gold: " + player.getGold(), uiTheme.getGoldColor());
        printBorder("bottom");
        System.out.print(Color.colorize("Do you want to rest? (1. Yes / 2. No): ", uiTheme.getHighlightColor()));

        int choice = getChoice(1, 2);
        if (choice == 1) {
            if (player.spendGold(cost)) {
                player.hp = player.maxHP;
                player.mana = player.maxMana;
                System.out.println(Color.colorize("You feel well rested. HP and Mana restored!", Color.GREEN));
            } else {
                System.out.println(Color.colorize("You don't have enough gold to rest.", Color.RED));
            }
        } else {
            System.out.println(Color.colorize("You decide not to rest.", Color.YELLOW));
        }
        System.out.println("\nPress Enter to continue...");
        scan.nextLine();
    }
}