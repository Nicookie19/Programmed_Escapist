package progescps;

import java.io.Serializable;

/**
 * Equipment class to represent weapons and armor
 */
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        WEAPON, ARMOR, ITEM
    }
    
    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
    }
    
    private String name;
    private Type type;
    private Rarity rarity;
    private int statBonus;
    private String specialEffect;
    
    public Equipment(String name, Type type, Rarity rarity, int statBonus, String specialEffect) {
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.statBonus = statBonus;
        this.specialEffect = specialEffect;
    }
    
    public String getName() {
        return name;
    }
    
    public Type getType() {
        return type;
    }
    
    public Rarity getRarity() {
        return rarity;
    }
    
    public int getStatBonus() {
        return statBonus;
    }
    
    public String getSpecialEffect() {
        return specialEffect;
    }
    
    public String getColoredName() {
        String color;
        
        switch (rarity) {
            case COMMON:
                color = Color.WHITE;
                break;
            case UNCOMMON:
                color = Color.GREEN;
                break;
            case RARE:
                color = Color.BLUE;
                break;
            case EPIC:
                color = Color.PURPLE;
                break;
            case LEGENDARY:
                color = Color.YELLOW;
                break;
            default:
                color = Color.WHITE;
        }
        
        return Color.colorize(name, color);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getColoredName());
        sb.append(" (").append(type).append(")");
        sb.append("\nRarity: ").append(rarity);
        
        if (type == Type.WEAPON) {
            sb.append("\nDamage Bonus: +").append(statBonus);
        } else {
            sb.append("\nDefense Bonus: +").append(statBonus);
        }
        
        if (specialEffect != null && !specialEffect.isEmpty()) {
            sb.append("\nSpecial Effect: ").append(specialEffect);
        }
        
        return sb.toString();
    }
}