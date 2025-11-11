package progescps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private ArrayList<String> objectives;
    private int currentObjective;
    private HashMap<String, Integer> rewards;
    private String faction;
    private boolean isComplete;

    public Quest(String name, String description, List<String> objectives, Map<String, Integer> rewards, String faction) {
        this.name = name;
        this.description = description;
        this.objectives = objectives != null ? new ArrayList<>(objectives) : new ArrayList<>();
        this.rewards = rewards != null ? new HashMap<>(rewards) : new HashMap<>();
        this.faction = faction;
        this.currentObjective = 0;
        this.isComplete = false;
    }

    public void progress() {
        if (currentObjective < objectives.size() - 1) {
            currentObjective++;
            System.out.println(Color.colorize("Quest updated: " + objectives.get(currentObjective), Color.YELLOW));
        } else {
            complete();
        }
    }

    private void complete() {
        isComplete = true;
        System.out.println(Color.colorize("Quest completed: " + name, Color.GREEN));
    }

    public String getName() {
        return name;
    }

    public String getCurrentObjective() {
        return objectives.get(currentObjective);
    }

    public Map<String, Integer> getRewards() {
        return rewards;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public String getDescription() {
        return description;
    }

    public String getFaction() {
        return faction;
    }

    public List<String> getObjectives() {
        return new ArrayList<>(objectives);
    }

    public int getCurrentObjectiveIndex() {
        return currentObjective;
    }

    // --- [NEW] ---
    /**
     * Sets the current objective index. Used by GameManager during load.
     */
    public void setCurrentObjectiveIndex(int index) {
        if (index >= 0 && index < this.objectives.size()) {
            this.currentObjective = index;
        }
    }

    /**
     * Sets the completion status. Used by GameManager during load.
     * This bypasses the normal "complete()" method to avoid print statements.
     */
    public void setCompleted(boolean isComplete) {
        this.isComplete = isComplete;
    }
    // --- [END NEW] ---
}