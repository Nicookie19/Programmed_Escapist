package progescps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class QuestManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Quest> activeQuests = new ArrayList<>();
    private List<Quest> completedQuests = new ArrayList<>();
    private Random random = new Random();
    
    // Arrays for procedural quest generation
    private static final String[] QUEST_TEMPLATES = {
        "Eliminate %s in %s",
        "Collect data from %s",
        "Secure %s against %s attacks",
        "Investigate anomalies in %s",
        "Recover lost files from %s",
        "Debug corrupted code in %s",
        "Infiltrate %s network",
        "Escort data packet through %s"
    };
    
    private static final String[] QUEST_DESCRIPTIONS = {
        "A dangerous %s has been causing trouble in %s. Eliminate it to restore stability.",
        "Important data needs to be collected from %s for analysis.",
        "%s is vulnerable to %s attacks. Secure it before critical systems are compromised.",
        "Strange anomalies have been detected in %s. Investigate the cause.",
        "Critical files have been lost in %s. Recover them before they're permanently deleted.",
        "Code in %s has become corrupted. Debug and fix the issues.",
        "Gain access to the %s network to extract valuable information.",
        "A critical data packet needs safe passage through %s. Ensure it arrives intact."
    };

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.activeQuests == null) this.activeQuests = new ArrayList<>();
        if (this.completedQuests == null) this.completedQuests = new ArrayList<>();
    }

    public void addQuest(String name, String description, List<String> objectives, Map<String, Integer> rewards, String faction) {
        Quest quest = new Quest(name, description, objectives, rewards, faction);
        activeQuests.add(quest);
        System.out.println(Color.colorize("New quest: " + quest.getName(), Color.YELLOW));
    }

    public void updateQuest(String objective, Hero hero) {
        for (Quest quest : new ArrayList<>(activeQuests)) { 
            if (quest.getCurrentObjective().equals(objective)) {
                quest.progress();
                if (quest.isComplete()) {
                    applyRewards(quest, hero);
                    completedQuests.add(quest);
                    activeQuests.remove(quest);
                }
                break;
            }
        }
    }

    public void generateProceduralQuest(Location location, List<Faction> availableFactions) {
        // Select random quest template and enemy type
        int templateIndex = random.nextInt(QUEST_TEMPLATES.length);
        String enemyType = location.enemyPool[random.nextInt(location.enemyPool.length)];
        
        // Generate quest name and description
        String questName;
        String questDescription;
        
        if (templateIndex <= 2) { // Templates that need two parameters
            questName = String.format(QUEST_TEMPLATES[templateIndex], enemyType, location.name);
            questDescription = String.format(QUEST_DESCRIPTIONS[templateIndex], enemyType, location.name);
        } else {
            questName = String.format(QUEST_TEMPLATES[templateIndex], location.name);
            questDescription = String.format(QUEST_DESCRIPTIONS[templateIndex], location.name);
        }
        
        // Create objectives
        List<String> objectives = new ArrayList<>();
        objectives.add("Travel to " + location.name);
        
        if (templateIndex == 0) { // Elimination quest
            objectives.add("Defeat " + enemyType + " in " + location.name);
        } else if (templateIndex == 1) { // Collection quest
            objectives.add("Collect data samples in " + location.name);
            objectives.add("Return to quest giver");
        } else {
            objectives.add("Complete mission in " + location.name);
        }
        
        // Generate rewards based on location danger level
        int goldReward = 50 + (location.dangerLevel * 20) + random.nextInt(50);
        int xpReward = 30 + (location.dangerLevel * 15) + random.nextInt(30);
        
        // Create rewards map
        java.util.Map<String, Integer> rewards = new java.util.HashMap<>();
        rewards.put("gold", goldReward);
        rewards.put("xp", xpReward);

        // Randomly select a faction if available
        String faction = null;
        if (!availableFactions.isEmpty() && random.nextBoolean()) {
            faction = availableFactions.get(random.nextInt(availableFactions.size())).getName();
            rewards.put("reputation", 5 + random.nextInt(10));
        }
        
        // Add the quest
        addQuest(questName, questDescription, objectives, rewards, faction);
        
        System.out.println(Color.colorize("\nNew procedural quest available: " + questName, Color.GREEN));
        System.out.println(Color.colorize(questDescription, Color.YELLOW));
    }

    private void applyRewards(Quest quest, Hero hero) {
        quest.getRewards().forEach((reward, amount) -> {
            if (reward.equals("gold")) hero.addGold(amount);
            else if (reward.equals("xp")) hero.addXP(amount);
            else if (reward.equals("item")) hero.addItem("Quest Reward Item", 1.0f);
            else if (reward.equals("reputation")) {
                // Handle reputation rewards for factions
                if (quest.getFaction() != null) {
                    hero.addFactionReputation(quest.getFaction(), amount);
                }
            }
            System.out.println(Color.colorize("Received " + amount + " " + reward + "!", Color.GREEN));
        });
    }

    public List<Quest> getActiveQuests() {
        return activeQuests;
    }

    public List<Quest> getCompletedQuests() {
        return completedQuests;
    }

    // --- [NEW] ---
    /**
     * Gets all quests (active and completed) for saving.
     */
    public List<Quest> getQuests() {
        List<Quest> allQuests = new ArrayList<>(activeQuests);
        allQuests.addAll(completedQuests);
        return allQuests;
    }

    /**
     * Repopulates the quest lists from a loaded save.
     */
    public void setQuests(List<Quest> quests) {
        this.activeQuests = new ArrayList<>();
        this.completedQuests = new ArrayList<>();
        
        if (quests == null) return;
        
        for (Quest quest : quests) {
            if (quest.isComplete()) { // Assumes Quest has this method
                this.completedQuests.add(quest);
            } else {
                this.activeQuests.add(quest);
            }
        }
    }
    // --- [END NEW] ---

}