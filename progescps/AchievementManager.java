package progescps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AchievementManager class to manage player achievements (Checked!)
 */
public class AchievementManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Achievement> achievements;
    
    /**
     * Constructs a new AchievementManager and initializes the default set of achievements.
     */
    public AchievementManager() {
        achievements = new HashMap<>();
        initializeAchievements();
    }

    /**
     * Initializes the default achievements by adding them to the map.
     */
    private void initializeAchievements() {
        // Combat achievements
        addAchievement("first_blood", "First Blood", "Defeat your first enemy");
        addAchievement("combo_master", "Combo Master", "Reach a 5x combo in combat");
        addAchievement("critical_hit", "Critical Success", "Land a critical hit");
        addAchievement("boss_slayer", "Boss Slayer", "Defeat a boss enemy");
        
        // Progression achievements
        addAchievement("level_5", "Apprentice", "Reach level 5");
        addAchievement("level_10", "Professional", "Reach level 10");
        addAchievement("level_20", "Master", "Reach level 20");
        
        // Quest achievements
        addAchievement("quest_complete", "Quest Complete", "Complete your first quest");
        addAchievement("quest_master", "Quest Master", "Complete 10 quests");
        
        // Hardcore achievements
        addAchievement("hardcore_survivor", "Hardcore Survivor", "Reach level 10 in hardcore mode");
        
        // Exploration achievements
        addAchievement("explorer", "Explorer", "Visit 5 different locations");
        
        // Faction achievements
        addAchievement("faction_friend", "Faction Friend", "Reach friendly status with a faction");
        addAchievement("faction_ally", "Faction Ally", "Reach allied status with a faction");
    }
    
    /**
     * Adds a new achievement to the manager.
     * @param id The unique identifier for the achievement.
     * @param name The name of the achievement.
     * @param description The description of the achievement.
     */
    private void addAchievement(String id, String name, String description) {
        achievements.put(id, new Achievement(id, name, description));
    }

    /**
     * Unlocks the achievement with the specified ID if it exists and is not already unlocked.
     * @param id The ID of the achievement to unlock.
     */
    public void unlockAchievement(String id) {
        if (achievements.containsKey(id) && !achievements.get(id).isUnlocked()) {
            achievements.get(id).unlock();
        }
    }

    /**
     * Checks if the achievement with the specified ID is unlocked.
     * @param id The ID of the achievement to check.
     * @return true if the achievement is unlocked, false otherwise.
     */
    public boolean isAchievementUnlocked(String id) {
        return achievements.containsKey(id) && achievements.get(id).isUnlocked();
    }

    /**
     * Gets a list of all achievements.
     * @return A list containing all achievements.
     */
    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }

    // --- [NEW] ---
    /**
     * Gets the raw map of achievements for saving.
     */
    public Map<String, Achievement> getAchievements() {
        return this.achievements;
    }

    /**
     * Sets the achievements map from a loaded save.
     * This will overwrite the default initialized achievements.
     */
    public void setAchievements(Map<String, Achievement> achievements) {
        if (achievements != null) {
            this.achievements = achievements;
        } else {
            // Failsafe: if null is passed, re-initialize
            this.achievements = new HashMap<>();
            initializeAchievements();
        }
    }
    // --- [END NEW] ---
    
    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                unlocked.add(achievement);
            }
        }
        return unlocked;
    }
    
    public int getUnlockedCount() {
        return getUnlockedAchievements().size();
    }
    
    public int getTotalCount() {
        return achievements.size();
    }
    
    /**
     * Checks and unlocks combat-related achievements based on the player's actions.
     * @param player The hero player.
     * @param enemy The enemy involved in combat.
     * @param comboCounter The current combo counter.
     */
    public void checkCombatAchievements(Hero player, Enemy enemy, int comboCounter) {
        // First blood achievement
        if (enemy.hp <= 0) {
            unlockAchievement("first_blood");

            // Boss slayer achievement
            if (enemy.getTier() == Enemy.Tier.STRONG) {
                unlockAchievement("boss_slayer");
            }
        }

        // Combo master achievement
        if (comboCounter >= 5) {
            unlockAchievement("combo_master");
        }
    }

    /**
     * Checks and unlocks progression-related achievements based on the player's level and mode.
     * @param player The hero player.
     */
    public void checkProgressionAchievements(Hero player) {
        // Level achievements
        if (player.level >= 5) {
            unlockAchievement("level_5");
        }
        if (player.level >= 10) {
            unlockAchievement("level_10");
        }
        if (player.level >= 20) {
            unlockAchievement("level_20");
        }

        // Hardcore achievement
        if (player.level >= 10 && player.isHardcoreMode()) {
            unlockAchievement("hardcore_survivor");
        }
    }

    /**
     * Displays all achievements to the console, showing unlocked and locked status.
     */
    public void displayAchievements() {
        System.out.println(Color.colorize("\n===== ACHIEVEMENTS =====", Color.YELLOW));
        System.out.println(Color.colorize("Unlocked: " + getUnlockedCount() + "/" + getTotalCount(), Color.BLUE));
        
        for (Achievement achievement : getAllAchievements()) {
            String status = achievement.isUnlocked() ? 
                Color.colorize("[✓] ", Color.GREEN) : 
                Color.colorize("[?] ", Color.GRAY);
            
            String name = achievement.isUnlocked() ? 
                Color.colorize(achievement.getName(), Color.WHITE) : 
                Color.colorize("???", Color.GRAY);
            
            String description = achievement.isUnlocked() ? 
                Color.colorize(achievement.getDescription(), Color.BLUE) : 
                Color.colorize("Complete this achievement to unlock", Color.GRAY);
            
            System.out.println(status + name + " - " + description);
        }
    }
}