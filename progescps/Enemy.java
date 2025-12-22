package progescps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Tier { WEAK, NORMAL, STRONG }
    private Tier tier;
    private Difficulty difficulty;

    Random random;
    int hp;
    int maxHP;
    int minDmg;
    int maxDmg;
    int baseMinDmg;
    int baseMaxDmg;
    int level;

    int gluttonyCooldown = 0;
    boolean bleedingActive = false;
    public boolean stunnedForNextTurn = false;
    boolean nextAttackIsDoubleDamage = false;
    private boolean manaDrainActive = false; 
    private int manaDrainTurns = 0;
    private boolean playerDamageReduced = false; 
    private int damageReductionTurns = 0;

    
    private List<StatusEffect> statusEffects = new ArrayList<>();

    LinkedList<String> enemyNames;
    String currentName;
    private transient Map<String, Runnable> specialAbilities = new HashMap<>();
    private String selectedAbility;

    
    private Boolean hostileOverride = null;

    public Enemy() {
        this(Tier.NORMAL);
    }

    public Enemy(Tier tier) {
        this(tier, 1);
    }

    public Enemy(Tier tier, int playerLevel) {
        this(tier, playerLevel, Difficulty.NORMAL);
    }

    public Enemy(Tier tier, int playerLevel, Difficulty difficulty) {
        this.tier = tier;
        this.difficulty = difficulty;
        this.random = new Random();
        this.level = playerLevel;
        initializeStats();
        applyDifficultyModifiers();
        initializeEnemyNames();
        this.currentName = getRandomEnemyName();
        initializeSpecialAbilities();
        selectRandomAbility();
        maxHP = (int)(maxHP * (1 + level * 0.1));
        minDmg = (int)(minDmg * (1 + level * 0.1));
        maxDmg = (int)(maxDmg * (1 + level * 0.1));
        hp = maxHP;
        System.out.println("A " + getDisplayName() + " (Level " + level + ") has appeared!");
    }

    private void initializeStats() {
        switch (tier) {
            case WEAK:
                maxHP = 60 + random.nextInt(20); // Increased base HP
                baseMinDmg = 6 + random.nextInt(5);
                baseMaxDmg = 12 + random.nextInt(5);
                break;
            case NORMAL:
                maxHP = 100 + random.nextInt(30); // Increased base HP
                baseMinDmg = 12 + random.nextInt(10);
                baseMaxDmg = 25 + random.nextInt(10);
                break;
            case STRONG:
                maxHP = 150 + random.nextInt(50); // Increased base HP
                baseMinDmg = 18 + random.nextInt(15);
                baseMaxDmg = 35 + random.nextInt(15);
                break;
        }
        // initialize working damage from base
        minDmg = baseMinDmg;
        maxDmg = baseMaxDmg;
        hp = maxHP;
    }

    private void applyDifficultyModifiers() {
        double hpMultiplier = 1.0;
        double dmgMultiplier = 1.0;
        
        switch (difficulty) {
            case EASY:
                hpMultiplier = 0.7;  // Reduced enemy health
                dmgMultiplier = 0.8; // Reduced enemy damage
                break;
            case HARD:
                hpMultiplier = 1.5;  // Significantly increased enemy health
                dmgMultiplier = 1.3; // Increased enemy damage
                break;
            case NORMAL:
            default:
                hpMultiplier = 1.0;
                dmgMultiplier = 1.0;
                break;
        }
        
        maxHP = (int)(maxHP * hpMultiplier);
        baseMinDmg = (int)(baseMinDmg * dmgMultiplier);
        baseMaxDmg = (int)(baseMaxDmg * dmgMultiplier);
        // reset current damage to scaled base
        minDmg = baseMinDmg;
        maxDmg = baseMaxDmg;
        hp = maxHP;
    }

    private void initializeSpecialAbilities() {
        specialAbilities = new HashMap<>();
        specialAbilities.put("Self-Repair", () -> {
            int heal = (int)(maxHP * 0.15);
            hp = Math.min(hp + heal, maxHP);
            System.out.println(currentName + " uses Self-Repair, restoring " + heal + " HP!");
        });
        specialAbilities.put("Data Poison", () -> {
            System.out.println(currentName + " inflicts data corruption! You lose 5 HP next turn.");
        });
        specialAbilities.put("Fork Bomb", () -> {
            System.out.println(currentName + " launches a fork bomb and attacks twice!");
        });
        specialAbilities.put("System Slowdown", () -> {
            int damage = random.nextInt(15) + 15;
            System.out.println(currentName + " causes System Slowdown, dealing " + damage + " damage and reducing your processing speed for 1 turn!");
            playerDamageReduced = true;
            damageReductionTurns = 1;
        });
        specialAbilities.put("Resource Drain", () -> {
            System.out.println(currentName + " drains your resources, reducing your mana for 2 turns!");
            manaDrainActive = true;
            manaDrainTurns = 2;
        });
    }

    public boolean useSpecialAbility() {
        if (specialAbilities == null || specialAbilities.isEmpty() || selectedAbility == null) {
            return false;
        }

        System.out.println(Color.colorize(currentName + " uses " + selectedAbility + "!", Color.RED));
        Runnable r = specialAbilities.get(selectedAbility);
        if (r != null) {
            r.run();
            return true;
        }
        return false;
    }

    public void provoke() {
        if (isDocile() && hostileOverride == null) {
            if (random.nextInt(100) < 40) { // 40% chance to turn hostile
                System.out.println(Color.colorize(currentName + " has been corrupted by your actions and turns hostile!", Color.RED));
                setHostile(true);
            }
        }
    }
    public void receiveDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void updateStatusEffects() {
        if (statusEffects == null) statusEffects = new ArrayList<>();
        Iterator<StatusEffect> iterator = statusEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effect.tick(this);
            if (!effect.isActive()) {
                
                effect.restore(this);
                iterator.remove();
            }
        }
    }

    String[] attackNames = {
        "Data Corruption", "Memory Leak", "System Crash",
        "Firewall Breach", "Code Injection", "Buffer Overflow",
        "DDoS Attack", "Encryption Break", "Privilege Escalation"
    };

    public static String[] listEnemyNames() {
        return new String[] {
            // Hostile
            "Virus", "Trojan", "Malware", "Spyware", "Ransomware", "Worm",
            "Rootkit", "Buffer Overflow", "SQL Injection", "Phishing Scam",
            "Adware", "Keylogger", "Botnet", "Exploit", "Zero Day",
            "Data Miner", "Cryptojacker", "DDoS Bot", "Logic Bomb",
            "Macro Virus", "File Infector", "Network Worm", "Drive By",
            "Clickjacking", "Session Hijacker", "Man in the Middle",
            "Credential Harvester", "Brute Force", "Dictionary Attack",
            "Rainbow Table", "Social Engineer", "Pharming Attack",
            "DNS Poisoner", "ARP Spoofer", "IP Spoofer", "Packet Sniffer",
            "Port Scanner", "Vulnerability Scanner", "Exploit Kit", "Data Leech",
            "Command Injector", "Cross Site", "Script Kiddie", "Black Hat",
            "Polymorphic Virus", "Stealth Rootkit", "AI Rogue", "Quantum Anomaly",
            "Corrupted AI", "Firewall Drake", "Data Golem", "Code Wraith",
            // New creative hostile enemies
            "Bit Bandit", "Null Specter", "Trojan Centurion", "Packet Phantom", "Glitch Revenant",
            "Spam Hydra", "Crypto Wraith", "Kernel Marauder", "Daemon Scribe", "Overclocked Juggernaut",
            "Malcode Djinn", "Phantom Process", "Backdoor Shade", "Bot Swarm", "Syntax Terror",
            // Friendly/Neutral
            "Firewall Guard", "Antivirus", "Debugger", "System Monitor",
            "Backup Service", "Patch Manager", "Security Scanner", "Code Librarian",
            "Data Archivist", "Network Cartographer", "Protocol Droid",
            // New creative friendly NPCs
            "Data Sage", "Patch Vendor", "Rogue AI", "Quantum Oracle", "Packet Courier",
            "Archivist Bot", "Syntax Mentor", "Bitwise Healer", "Glitch Tinkerer", "Memory Merchant"
        };
    }

    public void applyStatusEffect(StatusEffect effect) {
        if (statusEffects == null) statusEffects = new ArrayList<>();
        statusEffects.add(effect);
        effect.apply(this);
    }

    private void resetStat(String targetStat) {
        
    }

    public List<StatusEffect> getStatusEffects() {
        if (statusEffects == null) statusEffects = new ArrayList<>();
        return statusEffects;
    }

    private void selectRandomAbility() {
        if (specialAbilities == null || specialAbilities.isEmpty()) {
            selectRandomAbilityFallback();
            return;
        }
        List<String> abilityKeys = new ArrayList<>(specialAbilities.keySet());
        selectedAbility = abilityKeys.get(random.nextInt(abilityKeys.size()));
    }

    private void selectRandomAbilityFallback() {
        selectedAbility = "Self-Repair";
    }

    private void initializeEnemyNames() {
        enemyNames = new LinkedList<>();
        enemyNames.addAll(Arrays.asList(listEnemyNames()));
    }

    public String getCurrentName() {
        return currentName;
    }

    public String getDisplayName() {
        return currentName + " (" + tier + ")";
    }

    private String getRandomEnemyName() {
        if (enemyNames == null || enemyNames.isEmpty()) {
            initializeEnemyNames();
        }
        return enemyNames.get(random.nextInt(enemyNames.size()));
    }

    public void changeName(String newName) {
        if (enemyNames == null) initializeEnemyNames();
        if (enemyNames.contains(newName)) {
            this.currentName = newName;
        } else {
            this.currentName = getRandomEnemyName();
        }
    }

    private static final int AUTO_REPAIR_COOLDOWN_TURNS = 3;
    private int autoRepairCooldown = 0;
    private boolean isOverclocked = false;

    public void applyPassiveEffects(Hero player) {
        // Auto-repair: heal on cooldown only
        if (autoRepairCooldown > 0) {
            autoRepairCooldown--;
        } else {
            int healPercent = (tier == Tier.WEAK ? 1 : tier == Tier.NORMAL ? 2 : 2);
            int healAmount = (int)(maxHP * (healPercent / 100.0));
            if (healAmount > 0 && hp < maxHP) {
                hp = Math.min(hp + healAmount, maxHP);
                System.out.println(currentName + " auto-repairs " + healAmount + " HP due to Auto-Repair Protocol!");
                autoRepairCooldown = AUTO_REPAIR_COOLDOWN_TURNS;
            }
        }

        // Overclock: apply a temporary 20% boost when under 30% HP, remove when above
        boolean shouldOverclock = hp < maxHP * 0.3;
        if (shouldOverclock && !isOverclocked) {
            isOverclocked = true;
            minDmg = (int)(baseMinDmg * 1.2);
            maxDmg = (int)(baseMaxDmg * 1.2);
            System.out.println(currentName + " overclocks, increasing damage by 20%!");
        } else if (!shouldOverclock && isOverclocked) {
            isOverclocked = false;
            minDmg = baseMinDmg;
            maxDmg = baseMaxDmg;
            System.out.println(currentName + " stabilizes, damage returns to normal.");
        }
    }

    public void takeTurn(Hero player) {
        if (isDocile()) {
            System.out.println(currentName + " is a security program and refuses to fight.");
            return;
        }

        updateStatusEffects();

        applyPassiveEffects(player);

        if (gluttonyCooldown > 0) {
            gluttonyCooldown--;
        }

        if (stunnedForNextTurn) {
            System.out.println("Enemy (" + currentName + ") is frozen and cannot act!");
            stunnedForNextTurn = false;
            return;
        }

        
        if (manaDrainActive && manaDrainTurns > 0) {
            int manaDrain = (int)(player.maxMana * 0.1);
            player.mana = Math.max(0, player.mana - manaDrain);
            System.out.println(currentName + "'s Resource Drain consumes " + manaDrain + " of your mana!");
            manaDrainTurns--;
            if (manaDrainTurns == 0) {
                manaDrainActive = false;
            }
        }

        
        int originalPlayerMinDmg = player.minDmg;
        int originalPlayerMaxDmg = player.maxDmg;
        if (playerDamageReduced && damageReductionTurns > 0) {
            player.minDmg = (int)(player.minDmg * 0.8);
            player.maxDmg = (int)(player.maxDmg * 0.8);
            System.out.println("Your processing speed is reduced by System Slowdown!");
            damageReductionTurns--;
            if (damageReductionTurns == 0) {
                playerDamageReduced = false;
            }
        }

        // --- Strategic Decision Making ---
        boolean usedSpecial = false;
        // If low on health, prioritize healing or defensive abilities
        if (hp < maxHP * 0.35 && random.nextInt(100) < 60) {
            if ("Self-Repair".equals(selectedAbility)) {
                specialAbilities.get(selectedAbility).run();
                usedSpecial = true;
            }
        }
        // Otherwise, consider using any special ability
        else if (random.nextInt(100) < 30 && selectedAbility != null && specialAbilities != null && specialAbilities.containsKey(selectedAbility)) {
            specialAbilities.get(selectedAbility).run();
            usedSpecial = true;
            if (selectedAbility.equals("Data Poison")) {
                player.receiveDamage(Combat.calculateDamage(5, this, player, 0));
            } else if (selectedAbility.equals("Fork Bomb")) {
                for (int i = 0; i < 1; i++) { // Fork bomb now just attacks once after the announcement
                    int forkBase = random.nextInt(maxDmg - minDmg + 1) + minDmg;
                    int dmg = Combat.calculateDamage(forkBase, this, player, 0);
                    System.out.println("Enemy (" + currentName + ") attacks for " + dmg + " damage!");
                    player.receiveDamage(dmg);
                }
                return;
            } else if (selectedAbility.equals("System Slowdown")) {
                int baseSlowdown = random.nextInt(15) + 15;
                player.receiveDamage(Combat.calculateDamage(baseSlowdown, this, player, 0));
            }
        }

        // If a special ability was used that wasn't a direct attack, end the turn.
        if (usedSpecial && !"Fork Bomb".equals(selectedAbility)) {
             // Restore player damage if reduction period ended
            if (playerDamageReduced && damageReductionTurns == 0) {
                player.minDmg = originalPlayerMinDmg;
                player.maxDmg = originalPlayerMaxDmg;
            }
            return;
        }

        int attackIdx = random.nextInt(attackNames.length);
        String attack = attackNames[attackIdx];

        switch (attack) {
            case "Data Corruption":
                int baseCorruption = random.nextInt(20) + 20;
                 int corruptionDmg = Combat.calculateDamage(baseCorruption, this, player, 0);
                System.out.println("Enemy (" + currentName + ") uses Data Corruption and deals " + corruptionDmg + " damage!");
                player.receiveDamage(corruptionDmg);
                break;
            case "Encryption Break":
                int baseBreak = random.nextInt(10) + 10;
                int breakDmg = Combat.calculateDamage(baseBreak, this, player, 0);
                System.out.println("Enemy (" + currentName + ") uses Encryption Break, dealing " + breakDmg + " damage and lowering your defenses!");
                player.receiveDamage(breakDmg);
                // This is a placeholder for applying a defense-down status effect on the player.
                // To fully implement, a status effect system on the Hero would be needed.
                break;
            case "Privilege Escalation":
                System.out.println("Enemy (" + currentName + ") uses Privilege Escalation, boosting its next attack!");
                nextAttackIsDoubleDamage = true;
                // This attack takes the enemy's turn.
                return;
            case "Memory Leak":
                int baseLeak = random.nextInt(15) + 15;
                int leakDmg = Combat.calculateDamage(baseLeak, this, player, 0);
                System.out.println("Enemy (" + currentName + ") uses Memory Leak and deals " + leakDmg + " damage!");
                player.receiveDamage(leakDmg);
                if (random.nextInt(100) < 20) {
                    System.out.println("You have been corrupted! (You lose 5 HP next turn)");
                    player.receiveDamage(Combat.calculateDamage(5, this, player, 0));
                }
                break;
            case "System Crash":
                int baseCrash = random.nextInt(10) + 5;
                int crashDmg = Combat.calculateDamage(baseCrash, this, player, 0);
                System.out.println("Enemy (" + currentName + ") uses System Crash and deals " + crashDmg + " damage!");
                player.receiveDamage(crashDmg);
                if (random.nextInt(100) < 15) {
                    System.out.println("You are frozen and will miss your next turn!");
                }
                break;
            case "Firewall Breach":
                int breachHeal = random.nextInt(20) + 10;
                hp += breachHeal;
                if (hp > maxHP) {
                    hp = maxHP;
                }
                System.out.println("Enemy (" + currentName + ") uses Firewall Breach and restores " + breachHeal + " HP!");
                break;
            case "Code Injection":
                int baseInjection = random.nextInt(15) + 20;
                int injectionDmg = Combat.calculateDamage(baseInjection, this, player, 0);
                System.out.println("Enemy (" + currentName + ") uses Code Injection and deals " + injectionDmg + " damage! You are infected!");
                player.receiveDamage(injectionDmg);
                System.out.println("You lose 5 HP from infection.");
                player.receiveDamage(Combat.calculateDamage(5, this, player, 0));
                break;
            case "Buffer Overflow":
                System.out.println("Enemy (" + currentName + ") uses Buffer Overflow and strikes twice!");
                for (int i = 0; i < 2; i++) {
                    int baseOverflow = random.nextInt(15) + 10;
                    int overflowDmg = Combat.calculateDamage(baseOverflow, this, player, 0);
                    player.receiveDamage(overflowDmg);
                }
                break;
            case "DDoS Attack":
                if (gluttonyCooldown == 0 && player.hp > 50) {
                    int chance = random.nextInt(100);
                    if (chance < 30) {
                        int steal = (int) (player.hp * 0.1);
                        player.receiveDamage(Combat.calculateDamage(steal, this, player, 0));
                        hp += steal;
                        if (hp > maxHP) {
                            hp = maxHP;
                        }
                        System.out.println("Enemy (" + currentName + ") uses DDoS Attack! Steals " + steal + " HP and heals itself!");
                        gluttonyCooldown = 4;
                    } else {
                        System.out.println("Enemy (" + currentName + ") tries to use DDoS Attack but fails!");
                    }
                } else {
                    int baseFallback = random.nextInt(maxDmg - minDmg + 1) + minDmg;
                    int fallbackDmg = Combat.calculateDamage(baseFallback, this, player, 0);
                    System.out.println("Enemy (" + currentName + ") attacks for " + fallbackDmg + " damage!");
                    player.receiveDamage(fallbackDmg);
                }
                break;
            default:
                int baseDefault = random.nextInt(Math.max(1, maxDmg - minDmg + 1)) + minDmg;
                int dmg = Combat.calculateDamage(baseDefault, this, player, 0);
                // Apply critical hit bonus if the flag is set
                if (nextAttackIsDoubleDamage) {
                    dmg = (int) (dmg * 1.5);
                    System.out.println("Enemy (" + currentName + ") lands a critical attack for " + dmg + " damage!");
                    nextAttackIsDoubleDamage = false; // Reset the flag
                } else {
                    System.out.println("Enemy (" + currentName + ") attacks for " + dmg + " damage!");
                }
                player.receiveDamage(dmg);
                if (!player.isAlive()) {
                    System.out.println("You have been defeated!");
                    return; 
                }
                break;
        }

        
        if (playerDamageReduced && damageReductionTurns == 0) {
            player.minDmg = originalPlayerMinDmg;
            player.maxDmg = originalPlayerMaxDmg;
        }
    }

    public boolean isHostile() {
        if (hostileOverride != null) {
            return hostileOverride;
        }
        Set<String> alwaysHostile = new HashSet<>(Arrays.asList(
            "Virus", "Trojan", "Malware", "Spyware", "Ransomware", "Worm",
            "Rootkit", "Buffer Overflow", "SQL Injection", "Phishing Scam",
            "Adware", "Keylogger", "Botnet", "Exploit", "Zero Day",
            "Data Miner", "Cryptojacker", "DDoS Bot", "Logic Bomb",
            "Macro Virus", "File Infector", "Network Worm", "Drive By",
            "Clickjacking", "Session Hijacker", "Man in the Middle",
            "Credential Harvester", "Brute Force", "Dictionary Attack",
            "Rainbow Table", "Social Engineer", "Pharming Attack",
            "DNS Poisoner", "ARP Spoofer", "IP Spoofer", "Packet Sniffer",
            "Port Scanner", "Vulnerability Scanner", "Exploit Kit", "Data Leech",
            "Command Injector", "Cross Site", "Script Kiddie", "Black Hat",
            "Polymorphic Virus", "Stealth Rootkit", "AI Rogue", "Quantum Anomaly",
            "Corrupted AI", "Firewall Drake", "Data Golem", "Code Wraith"
        ));
        return alwaysHostile.contains(currentName);
    }

    public boolean isDocile() {
        Set<String> alwaysDocile = new HashSet<>(Arrays.asList(
            "Firewall Guard", "Antivirus", "Debugger", "System Monitor", "Backup Service",
            "Patch Manager", "Security Scanner", "Code Librarian", "Data Archivist",
            "Network Cartographer", "Protocol Droid"
        ));
        return alwaysDocile.contains(currentName);
    }

    public void setHostile(boolean hostile) {
        this.hostileOverride = hostile;
    }

    public Tier getTier() {
        return tier;
    }

    public int getDefense() {
        switch (tier) {
            case WEAK: return 2;
            case NORMAL: return 5;
            case STRONG: return 8;
            default: return 0;
        }
    }

    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (random == null) random = new Random();
        if (statusEffects == null) statusEffects = new ArrayList<>();
        if (enemyNames == null) initializeEnemyNames();
        if (specialAbilities == null) initializeSpecialAbilities();
        if (selectedAbility == null && specialAbilities != null && !specialAbilities.isEmpty()) {
            selectRandomAbility();
        }
    }
}