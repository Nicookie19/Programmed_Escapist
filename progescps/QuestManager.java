package progescps;

import java.io.Serializable;
import java.util.*;


public class QuestManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // =================================================================
    // INNER CLASS: QuestObjective
    // =================================================================
    public static class QuestObjective implements Serializable {
        public enum ObjectiveType { DEFEAT, TALK_TO, COLLECT }

        private String description;
        private ObjectiveType type;
        private String target; // e.g., "Virus", "Firewall Guard", "Data Shard"
        private int requiredAmount;
        private int currentAmount;
        private boolean isComplete;

        public QuestObjective(String description, ObjectiveType type, String target, int requiredAmount) {
            this.description = description;
            this.type = type;
            this.target = target;
            this.requiredAmount = requiredAmount;
            this.currentAmount = 0;
            this.isComplete = false;
        }

        public void updateProgress(int amount) {
            if (!isComplete) {
                this.currentAmount += amount;
                if (this.currentAmount >= this.requiredAmount) {
                    this.currentAmount = this.requiredAmount;
                    this.isComplete = true;
                    System.out.println("Objective complete: " + description);
                }
            }
        }

        public boolean isComplete() {
            return isComplete;
        }

        public String getStatus() {
            return description + " (" + currentAmount + "/" + requiredAmount + ")";
        }

        public ObjectiveType getType() { return type; }
        public String getTarget() { return target; }
    }

    // =================================================================
    // INNER CLASS: Quest
    // =================================================================
    public static class Quest implements Serializable {
        public enum QuestStatus { NOT_STARTED, IN_PROGRESS, COMPLETE }

        private String title;
        private String description;
        private List<QuestObjective> objectives;
        private QuestStatus status;

        // Requirements
        private String requiredClass; // e.g., "Data-Slinger", "Firewall-Specialist"
        private String requiredFaction; // e.g., "System Guardians", "Data Liberators"

        // Rewards
        private int rewardXp;
        private int rewardGold;

        // Old constructor for database loading
        public Quest(String name, String description, List<String> objectives, Map<String, Integer> rewards, String faction) {
            this.title = name;
            this.description = description;
            this.requiredFaction = faction;
            this.objectives = new ArrayList<>();
            for (String objDesc : objectives) {
                // This is a simplification; the old system didn't have typed objectives.
                this.objectives.add(new QuestObjective(objDesc, QuestObjective.ObjectiveType.DEFEAT, "Unknown", 1));
            }
            this.rewardXp = rewards.getOrDefault("xp", 0);
            this.rewardGold = rewards.getOrDefault("gold", 0);
            this.status = QuestStatus.NOT_STARTED;
        }

        public Quest(String title, String description, String requiredClass, String requiredFaction, int rewardXp, int rewardGold) {
            this.title = title;
            this.description = description;
            this.requiredClass = requiredClass;
            this.requiredFaction = requiredFaction;
            this.rewardXp = rewardXp;
            this.rewardGold = rewardGold;
            this.objectives = new ArrayList<>();
            this.status = QuestStatus.NOT_STARTED;
        }

        public void addObjective(QuestObjective objective) {
            this.objectives.add(objective);
        }

        public void startQuest() {
            this.status = QuestStatus.IN_PROGRESS;
            System.out.println("New Quest Started: " + title);
            System.out.println("    " + description);
        }

        public void checkCompletion() {
            if (status == QuestStatus.IN_PROGRESS) {
                boolean allComplete = objectives.stream().allMatch(QuestObjective::isComplete);
                if (allComplete) {
                    this.status = QuestStatus.COMPLETE;
                    System.out.println("Quest Complete: " + title);
                }
            }
        }

        public String getTitle() { return title; }
        public QuestStatus getStatus() { return status; }
        public List<QuestObjective> getObjectives() { return objectives; }
        public String getDescription() { return description; }
        public String getFaction() { return requiredFaction; }
        public boolean isComplete() { return status == QuestStatus.COMPLETE; }
        public void setCompleted(boolean completed) { if(completed) this.status = QuestStatus.COMPLETE; }

        // These are needed for the old database save/load logic
        public String getName() { return title; }
        public int getCurrentObjectiveIndex() {
            for (int i = 0; i < objectives.size(); i++) {
                if (!objectives.get(i).isComplete()) {
                    return i;
                }
            }
            return objectives.size();
        }
        public String getCurrentObjective() {
            int index = getCurrentObjectiveIndex();
            if (index < objectives.size()) {
                return objectives.get(index).getStatus();
            }
            return "Completed";
        }
        public Map<String, Integer> getRewards() {
            Map<String, Integer> rewards = new HashMap<>();
            rewards.put("xp", rewardXp);
            rewards.put("gold", rewardGold);
            return rewards;
        }
        public void progress() {
            int index = getCurrentObjectiveIndex();
            if (index < objectives.size()) {
                objectives.get(index).updateProgress(1);
                checkCompletion();
            }
        }
        public void setCurrentObjectiveIndex(int index) {
            for (int i = 0; i < objectives.size(); i++) {
                if (i < index) {
                    objectives.get(i).updateProgress(objectives.get(i).requiredAmount);
                }
            }
        }
    }

    // =================================================================
    // QuestManager Fields and Methods
    // =================================================================
    private List<QuestManager.Quest> activeQuests = new ArrayList<>();
    private List<QuestManager.Quest> completedQuests = new ArrayList<>();
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
        QuestManager.Quest quest = new QuestManager.Quest(name, description, objectives, rewards, faction);
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
        for (Map.Entry<String, Integer> entry : quest.getRewards().entrySet()) {
            String reward = entry.getKey();
            Integer amount = entry.getValue();
            if (reward.equals("gold")) hero.addGold(amount);
            else if (reward.equals("xp")) hero.addXP(amount);
            else if (reward.equals("item")) hero.addItem("Quest Reward Item", 1.0f);
            else if (reward.equals("reputation") && quest.getFaction() != null) {
                hero.addFactionReputation(quest.getFaction(), amount);
            }
            System.out.println(Color.colorize("Received " + amount + " " + reward + "!", Color.GREEN));
        }
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
    public List<QuestManager.Quest> getQuests() {
        List<QuestManager.Quest> allQuests = new ArrayList<>(activeQuests);
        allQuests.addAll(completedQuests);
        return allQuests;
    }

    /**
     * Repopulates the quest lists from a loaded save.
     */
    public void setQuests(List<QuestManager.Quest> quests) {
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