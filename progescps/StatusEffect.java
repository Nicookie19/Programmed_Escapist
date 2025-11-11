package progescps;

import java.io.Serializable;


public class StatusEffect implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int duration;
    private double modifier;
    private String targetStat;
    private int damagePerTurn;
    private boolean isBuff; // true for buffs, false for debuffs

    
    private transient Integer originalMinDmg = null;
    private transient Integer originalMaxDmg = null;
    private transient boolean appliedToHero = false;
    private transient boolean appliedToEnemy = false;

    public StatusEffect(String name, int duration, double modifier, String targetStat, int damagePerTurn) {
        this(name, duration, modifier, targetStat, damagePerTurn, false);
    }

    public StatusEffect(String name, int duration, double modifier, String targetStat, int damagePerTurn, boolean isBuff) {
        this.name = name;
        this.duration = duration;
        this.modifier = modifier;
        this.targetStat = targetStat;
        this.damagePerTurn = damagePerTurn;
        this.isBuff = isBuff;
    }

    
    public void apply(Hero hero) {
        if (hero == null) return;
        if (targetStat.equals("damage")) {
            originalMinDmg = hero.minDmg;
            originalMaxDmg = hero.maxDmg;
            hero.minDmg = (int)(hero.minDmg * modifier);
            hero.maxDmg = (int)(hero.maxDmg * modifier);
            appliedToHero = true;
        } else if (targetStat.equals("speed")) {
            
        }
        System.out.println(Color.colorize(hero.getClassName() + " is affected by " + name + "!", Color.YELLOW));
    }

    
    public void apply(Enemy enemy) {
        if (enemy == null) return;
        if (targetStat.equals("damage")) {
            originalMinDmg = enemy.minDmg;
            originalMaxDmg = enemy.maxDmg;
            enemy.minDmg = (int)(enemy.minDmg * modifier);
            enemy.maxDmg = (int)(enemy.maxDmg * modifier);
            appliedToEnemy = true;
        } else if (targetStat.equals("speed")) {
            
        }
        System.out.println(Color.colorize(enemy.getDisplayName() + " is affected by " + name + "!", Color.YELLOW));
    }

    
    public void tick(Hero hero) {
        if (hero == null) {
            duration--;
            return;
        }
        if (damagePerTurn > 0) {
            hero.hp = Math.max(0, hero.hp - damagePerTurn);
            System.out.println(Color.colorize(hero.getClassName() + " takes " + damagePerTurn + " damage from " + name + "!", Color.RED));
        }
        duration--;
    }

    public void tick(Enemy enemy) {
        if (enemy == null) {
            duration--;
            return;
        }
        if (damagePerTurn > 0) {
            enemy.hp = Math.max(0, enemy.hp - damagePerTurn);
            System.out.println(Color.colorize(enemy.getDisplayName() + " takes " + damagePerTurn + " damage from " + name + "!", Color.RED));
        }
        duration--;
    }

    public boolean isActive() {
        return duration > 0;
    }

    public String getTargetStat() {
        return targetStat;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isBuff() {
        return isBuff;
    }

    
    public void restore(Hero hero) {
        if (hero == null) return;
        if (appliedToHero) {
            if (originalMinDmg != null) hero.minDmg = originalMinDmg;
            if (originalMaxDmg != null) hero.maxDmg = originalMaxDmg;
            appliedToHero = false;
            originalMinDmg = null;
            originalMaxDmg = null;
        }
    }

    
    public void restore(Enemy enemy) {
        if (enemy == null) return;
        if (appliedToEnemy) {
            if (originalMinDmg != null) enemy.minDmg = originalMinDmg;
            if (originalMaxDmg != null) enemy.maxDmg = originalMaxDmg;
            appliedToEnemy = false;
            originalMinDmg = null;
            originalMaxDmg = null;
        }
    }
}