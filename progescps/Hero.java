package progescps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class Hero implements Serializable {
    private static final long serialVersionUID = 1L;

    public int hp;
    public int maxHP;
    public int mana;
    public int maxMana;
    public int minDmg;
    public int maxDmg;
    public int level;
    public int xp;
    // Target XP required to reach the next level (used by GameManager)
    public int xpToLevel;
    public int gold;
    public String[] attackNames;
    protected Random random;
    private boolean hardcoreMode = false;
    public Difficulty difficulty;
    // Basic shouts available to all heroes unless overridden by subclasses
    protected String[] shouts = new String[] {"Battle Cry", "Intimidate"};

    private List<InventoryItem> inventory = new ArrayList<>();
    private List<StatusEffect> activeStatusEffects = new ArrayList<>();
    private List<Faction> factions = new ArrayList<>();
    private AchievementManager achievementManager = new AchievementManager();

    public static class InventoryItem implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public float weight;
        public int quantity;

        public InventoryItem(String name, float weight) {
            this.name = name;
            this.weight = weight;
            this.quantity = 1;
        }
    }

    // --- [NEW] ---
    /**
     * Default constructor required for database loading.
     * Initializes random for a hero loaded from DB.
     */
    public Hero() {
        this.random = new Random();
        this.level = 1;
        this.xp = 0;
        this.xpToLevel = 100;
        this.gold = 0;
    }
    // --- [END NEW] ---

    public Hero(Random random) {
        this.random = random;
        this.level = 1;
        this.xp = 0;
        this.xpToLevel = this.level * 100;
        this.gold = 0;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public void addXP(int amount) {
        this.xp += amount;
        if (this.xp >= this.xpToLevel) {
            this.level++;
            this.xp = 0;
            this.xpToLevel = this.level * 100;
        }
    }

    public void addItem(String name, float weight) {
        for (InventoryItem item : inventory) {
            if (item.name.equals(name)) {
                item.quantity++;
                return;
            }
        }
        inventory.add(new InventoryItem(name, weight));
    }

    public void removeItem(String name) {
        inventory.removeIf(item -> item.name.equals(name));
    }

    public List<InventoryItem> getInventory() {
        return inventory;
    }

    // --- [NEW] ---
    /**
     * Sets the hero's inventory. Used by GameManager during load.
     */
    public void setInventory(List<InventoryItem> inventory) {
        this.inventory = (inventory != null) ? inventory : new ArrayList<>();
    }

    /**
     * Sets the hero's factions. Used by GameManager during load.
     */
    public void setFactions(List<Faction> factions) {
        this.factions = (factions != null) ? factions : new ArrayList<>();
    }
    // --- [END NEW] ---

    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public int getDefense() {
        return 0;
    }

    /**
     * Gets the hero's bonus critical hit chance.
     * @return The percentage bonus to critical hit chance.
     */
    public int getCritChanceBonus() {
        return 0; // Default hero has no bonus
    }

    public boolean isHardcoreMode() {
        return hardcoreMode;
    }

    public int getGold() {
        return gold;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public int getMinDamage() {
        return minDmg;
    }

public int getMaxDamage() {
        return maxDmg;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    // Expose active status effects for UI display
    public List<StatusEffect> getStatusEffects() {
        return new ArrayList<>(activeStatusEffects);
    }

    // Spend gold if available; returns true on success
    public boolean spendGold(int amount) {
        if (amount <= 0) return true;
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    // Clear all active status effects, restoring any modified stats
    public void clearStatusEffects() {
        List<StatusEffect> toRestore = new ArrayList<>(activeStatusEffects);
        for (StatusEffect effect : toRestore) {
            try {
                effect.restore(this);
            } catch (Exception ignored) {
                // Defensive: ensure clearing continues even if a specific effect errors
            }
        }
        activeStatusEffects.clear();
    }

    public void applyStatusEffect(StatusEffect effect) {
        if (effect == null) return;
        activeStatusEffects.add(effect);
        effect.apply(this);
    }

    public void updateStatusEffects() {
        List<StatusEffect> toRemove = new ArrayList<>();
        for (StatusEffect effect : activeStatusEffects) {
            effect.tick(this);
            if (!effect.isActive()) {
                effect.restore(this);
                toRemove.add(effect);
            }
        }
        activeStatusEffects.removeAll(toRemove);
    }

    protected List<String> getAllowedWeapons() {
        return Collections.emptyList();
    }

    protected List<String> getAllowedArmors() {
        return Collections.emptyList();
    }

    public void decrementCooldowns() {
        // Default: no cooldowns to decrement
    }

    public void applyPassiveEffects() {
        // Default: no passive effects
    }

    public double getSkillMultiplier() {
        return 1.0;
    }

    public void useSkill(int skillIndex, Enemy enemy) {
        // Base implementation: treat index 1 as a normal attack
        if (enemy == null) return;
        int base = minDmg + random.nextInt(Math.max(1, (maxDmg - minDmg + 1)));
        int damage = Combat.calculateDamage(base, this, enemy, 0, this);
        enemy.receiveDamage(damage);
        System.out.println(Color.colorize("You strike for " + damage + " damage.", Color.GREEN));
    }

    public void useSpecialAbility(Combat combat) {
        if (combat == null) return;
        // Default special routes to skill index 3, if subclasses choose to override behavior
        this.useSkill(3, combat.getEnemy());
        // Reset combo on special to avoid excessive stacking by default
        combat.resetCombo();
    }

    public void receiveDamage(int dmg) {
        this.hp -= dmg;
    }

    // Display available attacks for selection in UI
    public void showAttacks() {
        if (attackNames == null || attackNames.length == 0) {
            System.out.println(Color.colorize("No attacks available.", Color.YELLOW));
            return;
        }
        System.out.println(Color.colorize("Available attacks:", Color.YELLOW));
        for (int i = 0; i < attackNames.length; i++) {
            System.out.println(Color.colorize((i + 1) + ". " + attackNames[i], Color.WHITE));
        }
    }

    // Shout helpers used by GameManager
    public String[] getShouts() {
        return shouts != null ? shouts : new String[0];
    }

    public void useShout(int index, Enemy enemy) {
        String[] availableShouts = getShouts();
        if (index < 0 || index >= availableShouts.length) {
            System.out.println(Color.colorize("You try to shout, but nothing happens.", Color.YELLOW));
            return;
        }
        String shoutName = availableShouts[index];
        System.out.println(Color.colorize("You shout: " + shoutName, Color.BLUE));
        // Default shout behavior: minor psychological effect (no direct damage by default)
        // Subclasses can override to implement concrete effects.
    }

    // Faction helpers
    public boolean isInFaction(String name) {
        if (name == null) return false;
        for (Faction f : factions) {
            if (name.equalsIgnoreCase(f.getName())) {
                return f.isMember();
            }
        }
        return false;
    }

    public void joinFaction(Faction faction) {
        if (faction == null) return;
        // Add to list if not present
        boolean exists = false;
        for (Faction f : factions) {
            if (f.getName().equalsIgnoreCase(faction.getName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            factions.add(faction);
        }
        faction.joinFaction();
        faction.applyBenefits(this);
    }

    public List<Faction> getFactions() {
        return new ArrayList<>(factions);
    }

    public void addFactionReputation(String factionName, int amount) {
        if (factionName == null) return;
        for (Faction f : factions) {
            if (f.getName().equalsIgnoreCase(factionName)) {
                f.addReputation(amount);
                return;
            }
        }
        // If faction not yet in list, create a temporary membership and add reputation
        Faction f = new Faction(factionName);
        factions.add(f);
        f.joinFaction();
        f.addReputation(amount);
    }

    // Level-up checks used by GameManager
    public boolean checkLevelUp() {
        return this.xp >= this.xpToLevel;
    }

    public void levelUp() {
        this.level++;
        this.xp = 0;
        this.xpToLevel = this.level * 100;
        // Basic stat growth; subclasses can override for class-specific scaling
        this.maxHP += 10;
        this.maxMana += 5;
        this.minDmg = Math.max(1, this.minDmg + 1);
        this.maxDmg = Math.max(this.minDmg + 1, this.maxDmg + 2);
        this.hp = this.maxHP;
        this.mana = this.maxMana;
    }
}