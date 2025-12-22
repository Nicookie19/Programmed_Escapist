package progescps;

import java.util.Objects;
import java.util.Random;

public class Combat {
    private int comboCounter = 0;
    private static final double COMBO_MULTIPLIER = 0.1;
    private final Random random = new Random();
    private final Hero player;
    private final Enemy enemy;
    private boolean isOver = false;

    public Combat(Hero player, Enemy enemy) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.enemy = Objects.requireNonNull(enemy, "Enemy cannot be null");
    }

    public boolean isCombatOver() {
        return isOver || !player.isAlive() || !enemy.isAlive();
    }

    public void processRound(String action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        switch (action.toLowerCase()) {
            case "attack":
                int damage = calculateDamage(
                    player.getMinDamage() + random.nextInt(player.getMaxDamage() - player.getMinDamage() + 1),
                    player,
                    enemy,
                    comboCounter,
                    player
                );
                enemy.receiveDamage(damage);
                System.out.println(Color.colorize("You deal " + damage + " damage!", Color.GREEN));
                comboCounter++;

                // Debugger passive: Auto-heal on kill
                if (player instanceof Debugger && !enemy.isAlive()) {
                    int heal = (int)(player.maxHP * 0.1);
                    player.hp = Math.min(player.hp + heal, player.maxHP);
                    System.out.println(Color.colorize("Debugger auto-heals " + heal + " HP on kill!", Color.GREEN));
                }
                break;
            case "special":
                player.useSpecialAbility(this);
                break;
            case "flee":
                if (random.nextInt(100) < 40) { 
                    System.out.println(Color.colorize("You successfully fled from combat!", Color.YELLOW));
                    isOver = true;
                    return;
                } else {
                    System.out.println(Color.colorize("Failed to flee!", Color.RED));
                }
                break;
            default:
                System.out.println("Invalid action!");
                break;
        }

        if (enemy.isAlive()) {
            processEnemyTurn();
        }

        player.updateStatusEffects();
        enemy.updateStatusEffects();
    }

    private void processEnemyTurn() {
        if (enemy.stunnedForNextTurn) {
            System.out.println(Color.colorize("Enemy is stunned!", Color.YELLOW));
            enemy.stunnedForNextTurn = false;
            return;
        }

        if (random.nextInt(100) < 20) {
            enemy.useSpecialAbility();
        } else {
            int damage = calculateDamage(
                enemy.minDmg + random.nextInt(enemy.maxDmg - enemy.minDmg + 1),
                enemy,
                player,
                0,
                player
            );
            player.receiveDamage(damage);
            System.out.println(Color.colorize("Enemy deals " + damage + " damage!", Color.RED));
        }
    }

    public static int calculateDamage(int baseDamage, Object attacker, Object defender, int comboCounter) {
        return calculateDamage(baseDamage, attacker, defender, comboCounter, null);
    }

    private static final Random FALLBACK_RANDOM = new Random();
    private static Random getRandom(Object attacker, Object defender) {
        if (attacker instanceof Hero) {
            return ((Hero) attacker).random != null ? ((Hero) attacker).random : FALLBACK_RANDOM;
        }
        if (attacker instanceof Enemy) {
            Enemy e = (Enemy) attacker;
            return e.random != null ? e.random : FALLBACK_RANDOM;
        }
        if (defender instanceof Hero) {
            return ((Hero) defender).random != null ? ((Hero) defender).random : FALLBACK_RANDOM;
        }
        if (defender instanceof Enemy) {
            Enemy e = (Enemy) defender;
            return e.random != null ? e.random : FALLBACK_RANDOM;
        }
        return FALLBACK_RANDOM;
    }

    public static int calculateDamage(int baseDamage, Object attacker, Object defender, int comboCounter, Object hero) {
        double totalDamage = baseDamage;
        Random random = getRandom(attacker, defender);

        if (attacker instanceof Hero) {
            Hero h = (Hero) attacker;
            // Enhanced combo system with cap
            double comboMultiplier = Math.min(1 + (comboCounter * COMBO_MULTIPLIER), 2.5);
            totalDamage *= comboMultiplier;

            // Null-safe difficulty: default to NORMAL if hero difficulty not set
            Difficulty effDiff = h.difficulty != null ? h.difficulty : Difficulty.NORMAL;
            if (random.nextInt(100) < getCriticalChance(h, effDiff)) {
                totalDamage *= 2;
                System.out.println(Color.colorize("Critical Hit!", Color.YELLOW));
            }

            // New: Dodge chance for enemies (5% base, +10% if enemy is STRONG)
            if (defender instanceof Enemy) {
                Enemy e = (Enemy) defender;
                int dodgeChance = 5;
                if (e.getTier() == Enemy.Tier.STRONG) dodgeChance += 10;
                if (random.nextInt(100) < dodgeChance) {
                    System.out.println(Color.colorize("Enemy dodged your attack!", Color.CYAN));
                    return 0;
                }
            }

            // Add random variance (±10%)
            double variance = 0.9 + (random.nextDouble() * 0.2);
            totalDamage *= variance;
        }

        if (defender instanceof Hero) {
            totalDamage = Math.max(1, totalDamage - ((Hero) defender).getDefense());
        } else if (defender instanceof Enemy) {
            totalDamage = Math.max(1, totalDamage - ((Enemy) defender).getDefense());
        }

        return (int) Math.round(totalDamage);
    }

    private static int getCriticalChance(Hero player, Difficulty difficulty) {
        int baseCritChance = 10; // 10% base chance
        
        // Class-based adjustments
        if (player instanceof Hacker) {
            baseCritChance += 12; // Increased from 10%
        } else if (player instanceof Debugger) {
            baseCritChance += 8;  // Increased from 5%
        } else if (player instanceof Architect) {
            baseCritChance += 6;  // New bonus for Architect
        } else if (player instanceof Tester) {
            baseCritChance += 10;
            // Tester passive: +5% crit when HP > 80%
            if (player.hp > player.maxHP * 0.8) {
                baseCritChance += 5;
            }
        } else if (player instanceof PenTester) {
            baseCritChance += 15;
        }
        
        // Difficulty adjustments
        switch (difficulty) {
            case EASY:
                baseCritChance += 5; // Easier crits on easy mode
                break;
            case HARD:
                baseCritChance -= 2; // Harder to crit on hard mode
                break;
            default:
                break;
        }
        
        return baseCritChance;
    }

    public void resetCombo() {
        comboCounter = 0;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public Hero getPlayer() {
        return player;
    }
}