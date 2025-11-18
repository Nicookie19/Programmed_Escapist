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

        /**
         * Constructs a new inventory item.
         * @param name The name of the item.
         * @param weight The weight of the item.
         */
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

    /**
     * Constructs a new Hero with a given Random object for calculations.
     * @param random The Random object to use for this hero.
     */
    public Hero(Random random) {
        this.random = random;
        this.level = 1;
        this.xp = 0;
        this.xpToLevel = this.level * 100;
        this.gold = 0;
    }

    /**
     * Adds a specified amount of gold to the hero's total.
     * @param amount The amount of gold to add.
     */
    public void addGold(int amount) {
        this.gold += amount;
    }

    /**
     * Adds a specified amount of experience points (XP) to the hero.
     * @param amount The amount of XP to add.
     */
    public void addXP(int amount) {
        this.xp += amount;
        if (this.xp >= this.xpToLevel) {
            this.level++;
            this.xp = 0;
            this.xpToLevel = this.level * 100;
        }
    }

    /**
     * Adds an item to the hero's inventory. If the item already exists, its quantity is incremented.
     * @param name The name of the item to add.
     * @param weight The weight of the item.
     */
    public void addItem(String name, float weight) {
        for (InventoryItem item : inventory) {
            if (item.name.equals(name)) {
                item.quantity++;
                return;
            }
        }
        inventory.add(new InventoryItem(name, weight));
    }

    /**
     * Removes an item from the hero's inventory by name.
     * @param name The name of the item to remove.
     */
    public void removeItem(String name) {
        inventory.removeIf(item -> item.name.equals(name));
    }

    /**
     * Gets the hero's current inventory.
     * @return A list of InventoryItem objects.
     */
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

    /**
     * Gets the class name of the hero.
     * @return The simple name of the hero's class.
     */
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Gets the hero's defense value.
     * @return The defense value.
     */
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

    /**
     * Checks if the hero is in hardcore mode.
     * @return true if in hardcore mode, false otherwise.
     */
    public boolean isHardcoreMode() {
        return hardcoreMode;
    }

    /**
     * Gets the hero's current amount of gold.
     * @return The total gold.
     */
    public int getGold() {
        return gold;
    }

    /**
     * Checks if the hero is still alive.
     * @return true if the hero's HP is greater than 0, false otherwise.
     */
    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * Gets the hero's minimum damage for attacks.
     * @return The minimum damage.
     */
    public int getMinDamage() {
        return minDmg;
    }

    /**
     * Gets the hero's maximum damage for attacks.
     * @return The maximum damage.
     */
    public int getMaxDamage() {
        return maxDmg;
    }

    /**
     * Gets the hero's achievement manager.
     * @return The AchievementManager instance.
     */
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    /**
     * Gets a copy of the list of active status effects on the hero.
     * @return A list of StatusEffect objects.
     */
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

    /**
     * Applies a status effect to the hero.
     * @param effect The StatusEffect to apply.
     */
    public void applyStatusEffect(StatusEffect effect) {
        if (effect == null) return;
        activeStatusEffects.add(effect);
        effect.apply(this);
    }

    /**
     * Updates the duration and effects of all active status effects on the hero.
     * Removes effects that have expired.
     */
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

    /**
     * Gets the list of weapon types this hero is allowed to use.
     * @return A list of strings representing allowed weapon types.
     */
    protected List<String> getAllowedWeapons() {
        return Collections.emptyList();
    }

    /**
     * Gets the list of armor types this hero is allowed to wear.
     * @return A list of strings representing allowed armor types.
     */
    protected List<String> getAllowedArmors() {
        return Collections.emptyList();
    }

    /**
     * Decrements the cooldowns of any abilities the hero has.
     * Base implementation does nothing.
     */
    public void decrementCooldowns() {
        // Default: no cooldowns to decrement
    }

    /**
     * Applies any passive effects the hero might have at the start of combat or on level-up.
     * Base implementation does nothing.
     */
    public void applyPassiveEffects() {
        // Default: no passive effects
    }

    /**
     * Gets the skill multiplier for the hero, which can affect ability power.
     * @return The skill damage multiplier.
     */
    public double getSkillMultiplier() {
        return 1.0;
    }

    /**
     * Uses a hero's skill against an enemy.
     * @param skillIndex The index of the skill to use.
     * @param enemy The target enemy.
     */
    public void useSkill(int skillIndex, Enemy enemy) {
        // Base implementation: treat index 1 as a normal attack
        if (enemy == null) return;
        int base = minDmg + random.nextInt(Math.max(1, (maxDmg - minDmg + 1)));
        int damage = Combat.calculateDamage(base, this, enemy, 0, this);
        enemy.receiveDamage(damage);
        System.out.println(Color.colorize("You strike for " + damage + " damage.", Color.GREEN));
    }

    /**
     * Uses the hero's special ability in combat.
     * @param combat The current combat instance.
     */
    public void useSpecialAbility(Combat combat) {
        if (combat == null) return;
        // Default special routes to skill index 3, if subclasses choose to override behavior
        this.useSkill(3, combat.getEnemy());
        // Reset combo on special to avoid excessive stacking by default
        combat.resetCombo();
    }

    /**
     * Reduces the hero's HP by the specified damage amount.
     * @param dmg The amount of damage to receive.
     */
    public void receiveDamage(int dmg) {
        this.hp -= dmg;
    }

    /**
     * Displays the hero's available attacks to the console.
     * Used for the user interface in combat.
     */
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

    /**
     * Gets the available shouts for the hero.
     * @return An array of strings containing shout names.
     */
    public String[] getShouts() {
        return shouts != null ? shouts : new String[0];
    }

    /**
     * Uses a shout ability.
     * @param index The index of the shout to use.
     * @param enemy The target enemy (can be null if the shout doesn't target).
     */
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

    /**
     * Checks if the hero is a member of a specific faction.
     * @param name The name of the faction.
     * @return true if the hero is a member, false otherwise.
     */
    public boolean isInFaction(String name) {
        if (name == null) return false;
        for (Faction f : factions) {
            if (name.equalsIgnoreCase(f.getName())) {
                return f.isMember();
            }
        }
        return false;
    }

    /**
     * Makes the hero join a faction and applies its benefits.
     * @param faction The faction to join.
     */
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

    /**
     * Gets the list of factions the hero is associated with.
     * @return A copy of the list of factions.
     */
    public List<Faction> getFactions() {
        return new ArrayList<>(factions);
    }

    /**
     * Adds reputation points for a specific faction.
     * @param factionName The name of the faction.
     * @param amount The amount of reputation to add.
     */
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

    /**
     * Checks if the hero has enough XP to level up.
     * @return true if XP is greater than or equal to the XP required for the next level.
     */
    public boolean checkLevelUp() {
        return this.xp >= this.xpToLevel;
    }

    /**
     * Levels up the hero, increasing stats and resetting XP.
     * Subclasses can override for class-specific level-up bonuses.
     */
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