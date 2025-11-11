package progescps;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Tester extends Hero {
    private static final long serialVersionUID = 1L;
    private int scanCooldown = 0;
    private int verifyCooldown = 0;
    private int penetrationCooldown = 0;
    private boolean bugReported = false;
    private int bugTurnsRemaining = 0;
    private int bugDamagePerTurn;

    public Tester() {
        super(new Random());
        this.maxHP = 220;
        this.hp = this.maxHP;
        this.minDmg = 15;
        this.maxDmg = 35;
        this.maxMana = 80;
        this.mana = this.maxMana;
        this.attackNames = new String[]{"Scan", "Verify", "Bug Report", "Regression"};
    }

    @Override
    public String getClassName() {
        return "Tester";
    }

    @Override
    protected List<String> getAllowedWeapons() {
        return Arrays.asList("Basic Exploit", "Advanced Exploit", "Zero-Day Exploit", "Kernel Exploit", "Buffer Overflow Exploit");
    }

    @Override
    protected List<String> getAllowedArmors() {
        return Arrays.asList("Firewall Vest", "Encryption Cloak");
    }

    @Override
    public void decrementCooldowns() {
        if (scanCooldown > 0) scanCooldown--;
        if (verifyCooldown > 0) verifyCooldown--;
        if (bugTurnsRemaining > 0) {
            bugTurnsRemaining--;
            if (bugTurnsRemaining == 0) {
                bugReported = false;
            }
        }
    }

    @Override
    public void applyPassiveEffects() {
        // Critical Boost: Increased crit chance when HP is above 80%
        // Already implemented in Combat.getCriticalChance()
    }

    @Override
    public void useSkill(int skillIdx, Enemy enemy) {
        double multiplier = getSkillMultiplier();
        int critChance = (hp > maxHP * 0.8) ? 25 : 15; 
        if (bugReported && bugTurnsRemaining > 0) {
            System.out.println("Enemy takes " + bugDamagePerTurn + " bug damage!");
            enemy.receiveDamage(bugDamagePerTurn);
        }
        switch (skillIdx) {
            case 0: 
                if (scanCooldown == 0 && mana >= 15) {
                    int baseDamage = minDmg + random.nextInt(maxDmg - minDmg + 1);
                    int preDefense = (int)(baseDamage * multiplier);
                    int damage = Combat.calculateDamage(preDefense, this, enemy, 0);
                    System.out.println("You scan and deal " + damage + " damage!");
                    enemy.receiveDamage(damage);
                    bugReported = true;
                    bugTurnsRemaining = 3;
                    bugDamagePerTurn = (int)(5 * multiplier);
                    mana -= 15;
                    scanCooldown = 3;
                } else {
                    System.out.println("Scan is on cooldown or insufficient mana! Using normal attack.");
                    super.useSkill(1, enemy);
                }
                break;
            case 1: 
                super.useSkill(1, enemy);
                break;
            case 2: 
                if (verifyCooldown == 0 && mana >= 10) {
                    int baseDamage = minDmg + random.nextInt(maxDmg - minDmg + 1);
                    int preDefense = (int)(baseDamage * multiplier);
                    int damage = Combat.calculateDamage(preDefense, this, enemy, 0);
                    System.out.println("You report a bug and deal " + damage + " damage!");
                    enemy.receiveDamage(damage);
                    bugReported = true;
                    bugTurnsRemaining = 3;
                    bugDamagePerTurn = (int)(5 * multiplier);
                    mana -= 10;
                    verifyCooldown = 3;
                } else {
                    System.out.println("Bug Report is on cooldown or insufficient mana! Using normal attack.");
                    super.useSkill(1, enemy);
                }
                break;
            case 3: 
                if (mana >= 15) {
                    int baseDamage = minDmg + random.nextInt(maxDmg - minDmg + 1);
                    int preDefense = (int)(baseDamage * 2 * multiplier);
                    int damage = Combat.calculateDamage(preDefense, this, enemy, 0);
                    System.out.println("You run regression tests and deal " + damage + " damage!");
                    enemy.receiveDamage(damage);
                    mana -= 15;
                } else {
                    System.out.println("Insufficient mana for Regression! Using normal attack.");
                    super.useSkill(1, enemy);
                }
                break;
            default:
                super.useSkill(1, enemy);
                break;
        }
    }
}