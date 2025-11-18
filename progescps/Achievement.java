package progescps;

import java.io.Serializable;
import java.util.Date;

/**
 * Achievement class to represent player achievements
 */
public class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String description;
    private boolean unlocked;
    private Date unlockDate;
    
    /**
     * Constructs a new Achievement with the specified id, name, and description.
     * The achievement is initially unlocked.
     * @param id The unique identifier for the achievement.
     * @param name The name of the achievement.
     * @param description The description of the achievement.
     */
    public Achievement(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unlocked = false;
    }

    // --- [NEW] ---
    /**
     * Constructor for loading achievements from the database.
     */
    public Achievement(String id, String name, String description, boolean unlocked, Date unlockDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
        this.unlockDate = unlockDate;
    }
    // --- [END NEW] ---
    
    /**
     * Gets the unique identifier of the achievement.
     * @return The achievement ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name of the achievement.
     * @return The achievement name.
     */
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isUnlocked() {
        return unlocked;
    }
    
    public Date getUnlockDate() {
        return unlockDate;
    }
    
    public void unlock() {
        if (!unlocked) {
            unlocked = true;
            unlockDate = new Date();
            System.out.println(Color.colorize("\n★ ACHIEVEMENT UNLOCKED: " + name + " ★", Color.YELLOW));
            System.out.println(Color.colorize(description, Color.GRAY));
        }
    }
}