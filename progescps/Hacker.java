package progescps;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Hacker extends Hero {
    private static final long serialVersionUID = 1L;
    private int exploitCooldown = 0;
    private int ddosCooldown = 0;
    private int rootkitCooldown = 0;

    public Hacker() {
        super(new Random());
        this.maxHP = 180;
        this.hp = this.maxHP; 
        this.minDmg = 10;
        this.maxDmg = 25;
        this.maxMana = 120;
        this.mana = 120;
        this.attackNames = new String[]{"Vulnerability Exploit", "Code Injection", "DDoS Flood", "Rootkit Installation"};
    }

    @Override
    public String getClassName() {
        return "Hacker";
    }

    @Override
    protected List<String> getAllowedWeapons() {
        return Arrays.asList("Exploit Kit", "Injection Tool", "DDoS Script", "Rootkit Module", "Firewall Bypass");
    }

    @Override
    protected List<String> getAllowedArmors() {
        return Arrays.asList("Firewall Robe", "Encryption Cloak");
    }

    @Override
    public void decrementCooldowns() {
        if (exploitCooldown > 0) exploitCooldown--;
        if (ddosCooldown > 0) ddosCooldown--;
        if (rootkitCooldown > 0) rootkitCooldown--;
    }

    @Override
    public void applyPassiveEffects() {
        // Mana Regen: Regenerate 10% of max mana per turn
        int manaRegen = (int)(maxMana * 0.1);
        mana = Math.min(mana + manaRegen, maxMana);
        System.out.println("Hacker regenerates " + manaRegen + " mana.");

        // Mana Surge: If mana below 20%, gain extra 10%
        if (mana < maxMana * 0.2) {
            int surgeMana = (int)(maxMana * 0.1);
            mana = Math.min(mana + surgeMana, maxMana);
            System.out.println("Mana Surge activates, restoring " + surgeMana + " mana!");
        }
    }

    @Override
    public void useSkill(int skillIdx, Enemy enemy) {
        double multiplier = getSkillMultiplier();
        switch (skillIdx) {
            case 0: 
                if (exploitCooldown == 0 && mana >= 25) {
                    int baseDamage = minDmg + random.nextInt(maxDmg - minDmg + 1);
                    int preDefense = (int)(baseDamage * 2 * multiplier);
                    int damage = Combat.calculateDamage(preDefense, this, enemy, 0, this);
                    System.out.println("You exploit a vulnerability, dealing " + damage + " damage and weakening defenses!");
                    enemy.receiveDamage(damage);
                    
                    enemy.applyStatusEffect(new StatusEffect("Defense Down", 2, 0.8, "damage", 0));
                    mana -= 25;
                    exploitCooldown = 5;
                } else {
                    System.out.println("Vulnerability Exploit is on cooldown or insufficient mana! Using normal attack.");
                    super.useSkill(1, enemy);
                }
                break;
            case 1:
                if (mana >= 15) {
                    int baseDamage = minDmg + random.nextInt(maxDmg - minDmg + 1);
                    int preDefense = (int)(baseDamage * multiplier);
                    int damage = Combat.calculateDamage(preDefense, this, enemy, 0, this);
                    System.out.println("You inject malicious code, dealing " + damage + " damage and causing bleed!");
                    enemy.receiveDamage(damage);
                    enemy.applyStatusEffect(new StatusEffect("Bleed", 3, 1.0, "damage", 5));
                    mana -= 15;
                } else {
                    System.out.println("Insufficient mana for Code Injection! Using normal attack.");
                    super.useSkill(1, enemy);
                }
                break;
            case 2:
                if (ddosCooldown == 0 && mana >= 20) {
                    int baseDamage = minDmg + random.nextInt(maxDmg - minDmg + 1);
                    int preDefense = (int)(baseDamage * 1.5 * multiplier);
                    int damage = Combat.calculateDamage(preDefense, this, enemy, 0, this);
                    System.out.println("You flood with DDoS, dealing " + damage + " damage and stunning the enemy!");
                    enemy.receiveDamage(damage);
                    enemy.stunnedForNextTurn = true;
                    mana -= 20;
                    ddosCooldown = 4;
                } else {
                    System.out.println("DDoS Flood is on cooldown or insufficient mana! Using normal attack.");
                    super.useSkill(1, enemy);
                }
                break;
            case 3:
                if (rootkitCooldown == 0 && mana >= 30) {
                    int baseDamage = minDmg + random.nextInt(maxDmg - minDmg + 1);
                    int preDefense = (int)(baseDamage * 2.5 * multiplier);
                    int damage = Combat.calculateDamage(preDefense, this, enemy, 0, this);
                    System.out.println("You install a rootkit, dealing " + damage + " damage and draining enemy strength!");
                    enemy.receiveDamage(damage);

                    enemy.applyStatusEffect(new StatusEffect("Weakened", 2, 0.7, "damage", 0));
                    mana -= 30;
                    rootkitCooldown = 5;
                } else {
                    System.out.println("Rootkit Installation is on cooldown or insufficient mana! Using normal attack.");
                    super.useSkill(1, enemy);
                }
                break;
            default:
                super.useSkill(1, enemy);
                break;
        }
    }
}