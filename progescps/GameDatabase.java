package progescps;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameDatabase
 *
 * Reads connection info from environment variables if available: - DB_URL
 * (default
 * jdbc:mysql://localhost:3306/programmed_escapist?zeroDateTimeBehavior=CONVERT_TO_NULL)
 * - DB_USER (default root) - DB_PASS (default empty)
 *
 * Provides helper DAO methods used by GameManager and other classes.
 */
public class GameDatabase {

    // Default connection info (can be overridden by environment variables)
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/programmed_escapist?zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    // Get a database connection (call when you need it)
    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("DB_USER", DEFAULT_USER);
        String pass = System.getenv().getOrDefault("DB_PASS", DEFAULT_PASS);
        return DriverManager.getConnection(url, user, pass);
    }

    // --- Instance DAO METHODS (for convenience when you have a Connection instance) ---
    private final Connection conn;

    public GameDatabase(Connection conn) {
        this.conn = conn;
    }

    // ENEMIES
    public List<String> getAllEnemies() throws SQLException {
        List<String> enemies = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT currentName FROM enemy")) {
            while (rs.next()) {
                enemies.add(rs.getString("currentName"));
            }
        }
        return enemies;
    }

    // LOCATIONS
    public List<String> getAllLocations() throws SQLException {
        List<String> locations = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM location")) {
            while (rs.next()) {
                locations.add(rs.getString("name"));
            }
        }
        return locations;
    }

    // EQUIPMENT
    public List<String> getAllEquipment() throws SQLException {
        List<String> equipment = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM equipment")) {
            while (rs.next()) {
                equipment.add(rs.getString("name"));
            }
        }
        return equipment;
    }

    // QUESTS
    public List<String> getAllQuests() throws SQLException {
        List<String> quests = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM quest")) {
            while (rs.next()) {
                quests.add(rs.getString("name"));
            }
        }
        return quests;
    }

    // LOCATION FEATURES (as Map: location -> list of features)
    public Map<String, List<String>> getLocationFeatures() throws SQLException {
        Map<String, List<String>> locationFeatures = new HashMap<>();
        String sql = "SELECT l.name, lf.featureName FROM location l JOIN locationfeature lf ON l.id = lf.locationId";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String location = rs.getString("name");
                String feature = rs.getString("featureName");
                locationFeatures.computeIfAbsent(location, k -> new ArrayList<>()).add(feature);
            }
        }
        return locationFeatures;
    }

    // FACTIONS
    public List<String> getAllFactions() throws SQLException {
        List<String> factions = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM faction")) {
            while (rs.next()) {
                factions.add(rs.getString("name"));
            }
        }
        return factions;
    }

    // Helper: returns faction id by name (or -1 if not found)
    public int getFactionIdByName(String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM faction WHERE name = ? LIMIT 1")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // NEW SAVE/LOAD DAO METHODS
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Finds the heroId associated with a save slot name.
     *
     * @return heroId, or -1 if not found.
     */
    public int findHeroIdBySaveName(String saveName) throws SQLException {
        String sql = "SELECT hero_id FROM save_slots WHERE save_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saveName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("hero_id");
                }
            }
        }
        return -1;
    }

    /**
     * Deletes a hero and all their associated data (via CASCADE).
     */
    public void deleteHero(int heroId) throws SQLException {
        String sql = "DELETE FROM hero WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, heroId);
            ps.executeUpdate();
        }
    }

    /**
     * Links a save_name to a hero_id, overwriting if it already exists.
     */
    public void linkSaveToHero(String saveName, int heroId) throws SQLException {
        String sql = "INSERT INTO save_slots (save_name, hero_id) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE hero_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saveName);
            ps.setInt(2, heroId);
            ps.setInt(3, heroId);
            ps.executeUpdate();
        }
    }

    /**
     * Saves the main Hero object and returns the new auto-incremented ID.
     */
    public int saveHero(Hero hero, boolean permadeath) throws SQLException {
        String sql = "INSERT INTO hero (className, maxHP, hp, minDmg, maxDmg, maxMana, mana, gold, xp, level, xpToLevel, difficulty, hardcoreMode) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hero.getClassName());
            ps.setInt(2, hero.maxHP);
            ps.setInt(3, hero.hp);
            ps.setInt(4, hero.minDmg);
            ps.setInt(5, hero.maxDmg);
            ps.setInt(6, hero.maxMana);
            ps.setInt(7, hero.mana);
            ps.setInt(8, hero.gold);
            ps.setInt(9, hero.xp);
            ps.setInt(10, hero.level);
            ps.setInt(11, hero.xpToLevel);
            ps.setString(12, hero.difficulty.name());
            ps.setBoolean(13, permadeath);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating hero failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Saves the hero's current equipment.
     */
    public void saveEquipment(int heroId, String weapon, String armor) throws SQLException {
        // Using "upsert" logic in case a row already exists
        String sql = "INSERT INTO heroequipment (heroId, weapon, armor) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE weapon = ?, armor = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, heroId);
            ps.setString(2, weapon);
            ps.setString(3, armor);
            ps.setString(4, weapon);
            ps.setString(5, armor);
            ps.executeUpdate();
        }
    }

    /**
     * Saves all inventory items in a batch.
     */
    public void saveInventory(int heroId, List<Hero.InventoryItem> inventory) throws SQLException {
        String sql = "INSERT INTO inventoryitem (heroId, name, quantity, weight) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Hero.InventoryItem item : inventory) {
                ps.setInt(1, heroId);
                ps.setString(2, item.name);
                ps.setInt(3, item.quantity);
                ps.setFloat(4, item.weight);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Saves all faction memberships and reputation.
     */
    public void saveFactions(int heroId, List<Faction> factions) throws SQLException {
        String sql = "INSERT INTO herofaction (heroId, factionId, reputation) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // This is slow, but safe. A faster way would be to get all faction IDs first.
            for (Faction faction : factions) {
                int factionId = getFactionIdByName(faction.getName()); // Use your existing method!
                if (factionId != -1) {
                    ps.setInt(1, heroId);
                    ps.setInt(2, factionId);
                    ps.setInt(3, faction.getReputation());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    /**
     * Saves all active quests, their objectives, and their rewards.
     */
    public void saveQuests(int heroId, QuestManager questManager) throws SQLException {
        String questSql = "INSERT INTO quest (name, description, currentObjectiveIndex, completed, faction, heroId) VALUES (?, ?, ?, ?, ?, ?)";
        String objSql = "INSERT INTO questobjective (questId, objective, idx) VALUES (?, ?, ?)";
        String rewardSql = "INSERT INTO questreward (questId, rewardType, rewardAmount) VALUES (?, ?, ?)";

        try (PreparedStatement psQuest = conn.prepareStatement(questSql, Statement.RETURN_GENERATED_KEYS); PreparedStatement psObj = conn.prepareStatement(objSql); PreparedStatement psReward = conn.prepareStatement(rewardSql)) { //This is a QuestManager.Quest

            for (QuestManager.Quest quest : questManager.getQuests()) {
                // 1. Save Quest
                psQuest.setString(1, quest.getName());
                psQuest.setString(2, quest.getDescription());
                psQuest.setInt(3, quest.getCurrentObjectiveIndex());
                psQuest.setBoolean(4, quest.isComplete());
                psQuest.setString(5, quest.getFaction());
                psQuest.setInt(6, heroId);
                psQuest.executeUpdate();

                int questId;
                try (ResultSet rs = psQuest.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to get quest ID.");
                    }
                    questId = rs.getInt(1);
                }

                // 2. Save Objectives
                List<QuestManager.QuestObjective> objectives = quest.getObjectives();
                for (int i = 0; i < objectives.size(); i++) {
                    psObj.setInt(1, questId);
                    psObj.setString(2, objectives.get(i).getStatus()); // Use getStatus() for the description
                    psObj.setInt(3, i);
                    psObj.addBatch();
                }
                psObj.executeBatch();

                // 3. Save Rewards
                for (Map.Entry<String, Integer> reward : quest.getRewards().entrySet()) {
                    psReward.setInt(1, questId);
                    psReward.setString(2, reward.getKey()); // e.g., "gold", "xp"
                    psReward.setInt(3, reward.getValue());
                    psReward.addBatch();
                }
                psReward.executeBatch();
            }
        }
    }

    /**
     * Saves all unlocked achievements for the hero.
     */
    public void saveAchievements(int heroId, AchievementManager manager) throws SQLException {
        // We only save unlocked achievements to the heroachievement table
        String sql = "INSERT INTO heroachievement (heroId, achievementId, unlockDate) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Achievement ach : manager.getUnlockedAchievements()) {
                ps.setInt(1, heroId);
                ps.setString(2, ach.getId());
                ps.setTimestamp(3, new java.sql.Timestamp(ach.getUnlockDate().getTime()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Loads the main Hero object from the database.
     */
    public Hero loadHero(int heroId) throws SQLException {
        String sql = "SELECT * FROM hero WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, heroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String className = rs.getString("className");
                    Hero hero;

                    // Instantiate the correct Hero subclass
                    switch (className) {
                        case "Debugger":
                            hero = new Debugger();
                            break;
                        case "Hacker":
                            hero = new Hacker();
                            break;
                        case "Tester":
                            hero = new Tester();
                            break;
                        case "Architect":
                            hero = new Architect();
                            break;
                        case "PenTester":
                            hero = new PenTester();
                            break;
                        case "Support":
                            hero = new Support();
                            break;
                        default:
                            throw new SQLException("Unknown hero class: " + className);
                    }

                    // Hydrate the object with data
                    hero.maxHP = rs.getInt("maxHP");
                    hero.hp = rs.getInt("hp");
                    hero.minDmg = rs.getInt("minDmg");
                    hero.maxDmg = rs.getInt("maxDmg");
                    hero.maxMana = rs.getInt("maxMana");
                    hero.mana = rs.getInt("mana");
                    hero.gold = rs.getInt("gold");
                    hero.xp = rs.getInt("xp");
                    hero.level = rs.getInt("level");
                    hero.xpToLevel = rs.getInt("xpToLevel");
                    hero.difficulty = Difficulty.valueOf(rs.getString("difficulty"));
                    // Note: permadeath is loaded separately by GameManager

                    return hero;
                } else {
                    throw new SQLException("No hero found with ID: " + heroId);
                }
            }
        }
    }

    /**
     * Loads the hero's equipment.
     *
     * @return String array [weapon, armor]
     */
    public String[] loadEquipment(int heroId) throws SQLException {
        String sql = "SELECT weapon, armor FROM heroequipment WHERE heroId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, heroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("weapon"),
                        rs.getString("armor")
                    };
                }
            }
        }
        // Return defaults if nothing was found
        return new String[]{"Basic Sword", "Cloth Armor"};
    }

    /**
     * Loads the hero's inventory.
     */
    public List<Hero.InventoryItem> loadInventory(int heroId) throws SQLException {
        List<Hero.InventoryItem> inventory = new ArrayList<>();
        String sql = "SELECT name, quantity, weight FROM inventoryitem WHERE heroId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, heroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Assumes InventoryItem is a public (or accessible) inner class
                    // And has a constructor or fields we can set
                    Hero.InventoryItem item = new Hero.InventoryItem(
                            rs.getString("name"),
                            rs.getFloat("weight")
                    );
                    item.quantity = rs.getInt("quantity");
                    inventory.add(item);
                }
            }
        }
        return inventory;
    }

    /**
     * Loads the hero's factions and reputation.
     */
    public List<Faction> loadFactions(int heroId) throws SQLException {
        List<Faction> factions = new ArrayList<>();
        // Join with faction table to get the name
        String sql = "SELECT f.name, hf.reputation FROM herofaction hf "
                + "JOIN faction f ON hf.factionId = f.id "
                + "WHERE hf.heroId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, heroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Assumes Faction constructor Faction(name, reputation)
                    factions.add(new Faction(
                            rs.getString("name"),
                            rs.getInt("reputation")
                    ));
                }
            }
        }
        return factions;
    }

    /**
     * Loads all quests, objectives, and rewards for a hero.
     */
    public List<QuestManager.Quest> loadQuests(int heroId) throws SQLException {
        List<QuestManager.Quest> quests = new ArrayList<>();
        String sql = "SELECT * FROM quest WHERE heroId = ?";
        String objSql = "SELECT objective FROM questobjective WHERE questId = ? ORDER BY idx";
        String rewardSql = "SELECT rewardType, rewardAmount FROM questreward WHERE questId = ?";

        try (PreparedStatement psQuest = conn.prepareStatement(sql); PreparedStatement psObj = conn.prepareStatement(objSql); PreparedStatement psReward = conn.prepareStatement(rewardSql)) {

            psQuest.setInt(1, heroId);
            try (ResultSet rsQuest = psQuest.executeQuery()) {
                while (rsQuest.next()) {
                    int questId = rsQuest.getInt("id");

                    // 1. Load Objectives
                    List<String> objectives = new ArrayList<>();
                    psObj.setInt(1, questId);
                    try (ResultSet rsObj = psObj.executeQuery()) {
                        while (rsObj.next()) {
                            objectives.add(rsObj.getString("objective"));
                        }
                    }

                    // 2. Load Rewards
                    Map<String, Integer> rewards = new HashMap<>();
                    psReward.setInt(1, questId);
                    try (ResultSet rsReward = psReward.executeQuery()) {
                        while (rsReward.next()) {
                            rewards.put(rsReward.getString("rewardType"), rsReward.getInt("rewardAmount"));
                        }
                    }

                    // 3. Re-create Quest object
                    QuestManager.Quest quest = new QuestManager.Quest(
                            rsQuest.getString("name"),
                            rsQuest.getString("description"),
                            objectives,
                            rewards,
                            rsQuest.getString("faction")
                    );

                    // 4. Set the loaded state
                    quest.setCurrentObjectiveIndex(rsQuest.getInt("currentObjectiveIndex"));

                    if (rsQuest.getBoolean("completed")) {
                        quest.setCompleted(true); // Use the new setter
                    }

                    quests.add(quest);
                }
            }
        }
        return quests;
    }

    /**
     * Loads all achievements for a hero, merging unlocked status.
     */
    public Map<String, Achievement> loadAchievements(int heroId) throws SQLException {
        Map<String, Achievement> achievements = new HashMap<>();

        // 1. Load all *possible* achievements from the main 'achievement' table
        String sqlAll = "SELECT id, name, description FROM achievement";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlAll)) {
            while (rs.next()) {
                String id = rs.getString("id");
                achievements.put(id, new Achievement(
                        id,
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        }

        // 2. Load the hero's *unlocked* achievements
        String sqlUnlocked = "SELECT achievementId, unlockDate FROM heroachievement WHERE heroId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUnlocked)) {
            ps.setInt(1, heroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("achievementId");
                    Date unlockDate = rs.getTimestamp("unlockDate");

                    // 3. Overwrite the default achievement with the unlocked one
                    if (achievements.containsKey(id)) {
                        Achievement ach = achievements.get(id);
                        achievements.put(id, new Achievement(
                                id,
                                ach.getName(),
                                ach.getDescription(),
                                true, // Mark as unlocked
                                unlockDate
                        ));
                    }
                }
            }
        }
        return achievements;
    }

    // Add this method anywhere inside your GameDatabase.java class
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(1); // Test connection with a 1-second timeout
        } catch (SQLException e) {
            System.err.println("Database connection test FAILED: " + e.getMessage());
            return false;
        }
    }
}
