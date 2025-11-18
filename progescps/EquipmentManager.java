package progescps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * EquipmentManager class to manage equipment generation and inventory
 */
public class EquipmentManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * List of available equipment.
     */
    private List<Equipment> availableEquipment;
    /**
     * Random number generator for equipment generation.
     */
    private Random random;
    
    /**
     * Constructs an EquipmentManager, initializing the list of available equipment.
     */
    public EquipmentManager() {
        this.availableEquipment = new ArrayList<>();
        this.random = new Random();
        initializeEquipment();
    }

    private void initializeEquipment() {
        // Weapons
        addEquipment("Rusty Dagger", Equipment.Type.WEAPON, Equipment.Rarity.COMMON, 2, null);
        addEquipment("Iron Sword", Equipment.Type.WEAPON, Equipment.Rarity.COMMON, 5, null);
        addEquipment("Steel Blade", Equipment.Type.WEAPON, Equipment.Rarity.UNCOMMON, 8, null);
        addEquipment("Enchanted Rapier", Equipment.Type.WEAPON, Equipment.Rarity.RARE, 12, "5% chance to deal double damage");
        addEquipment("Debugger's Wand", Equipment.Type.WEAPON, Equipment.Rarity.EPIC, 15, "10% chance to heal on hit");
        addEquipment("Blade of Null Pointers", Equipment.Type.WEAPON, Equipment.Rarity.EPIC, 16, "Ignores a portion of enemy defense");
        addEquipment("Compiler's Hammer", Equipment.Type.WEAPON, Equipment.Rarity.LEGENDARY, 20, "15% chance to stun enemy");
        addEquipment("Zero-Day Blade", Equipment.Type.WEAPON, Equipment.Rarity.LEGENDARY, 22, "Deals bonus damage on first strike");
        
        // Armor
        addEquipment("Cloth Robe", Equipment.Type.ARMOR, Equipment.Rarity.COMMON, 2, null);
        addEquipment("Leather Vest", Equipment.Type.ARMOR, Equipment.Rarity.COMMON, 5, null);
        addEquipment("Chain Mail", Equipment.Type.ARMOR, Equipment.Rarity.UNCOMMON, 8, null);
        addEquipment("Firewall Shield", Equipment.Type.ARMOR, Equipment.Rarity.RARE, 12, "5% chance to reflect damage");
        addEquipment("Jacket of Holding", Equipment.Type.ARMOR, Equipment.Rarity.RARE, 10, "Increases inventory capacity");
        addEquipment("Encryption Plate", Equipment.Type.ARMOR, Equipment.Rarity.EPIC, 15, "10% chance to avoid damage");
        addEquipment("Aegis of the Kernel", Equipment.Type.ARMOR, Equipment.Rarity.EPIC, 17, "Reduces damage from critical hits");
        addEquipment("Quantum Armor", Equipment.Type.ARMOR, Equipment.Rarity.LEGENDARY, 20, "15% chance to regenerate HP each turn");
        addEquipment("Root's Embrace", Equipment.Type.ARMOR, Equipment.Rarity.LEGENDARY, 22, "Grants immunity to stun effects");
    }
    
    private void addEquipment(String name, Equipment.Type type, Equipment.Rarity rarity, int statBonus, String specialEffect) {
        availableEquipment.add(new Equipment(name, type, rarity, statBonus, specialEffect));
    }
    
    public Equipment generateRandomEquipment(int playerLevel, Difficulty difficulty) {
        // Determine rarity based on player level and difficulty
        double rarityRoll = random.nextDouble();
        Equipment.Rarity rarity;
        
        // Adjust rarity chances based on difficulty
        double legendaryChance = 0.05;
        double epicChance = 0.10;
        double rareChance = 0.20;
        double uncommonChance = 0.30;
        
        if (difficulty == Difficulty.EASY) {
            legendaryChance += 0.03;
            epicChance += 0.05;
            rareChance += 0.07;
        } else if (difficulty == Difficulty.HARD) {
            legendaryChance -= 0.02;
            epicChance -= 0.03;
            rareChance -= 0.05;
        }
        
        // Adjust rarity chances based on player level
        legendaryChance += playerLevel * 0.002;
        epicChance += playerLevel * 0.005;
        rareChance += playerLevel * 0.008;
        
        // Determine rarity
        if (rarityRoll < legendaryChance) {
            rarity = Equipment.Rarity.LEGENDARY;
        } else if (rarityRoll < legendaryChance + epicChance) {
            rarity = Equipment.Rarity.EPIC;
        } else if (rarityRoll < legendaryChance + epicChance + rareChance) {
            rarity = Equipment.Rarity.RARE;
        } else if (rarityRoll < legendaryChance + epicChance + rareChance + uncommonChance) {
            rarity = Equipment.Rarity.UNCOMMON;
        } else {
            rarity = Equipment.Rarity.COMMON;
        }
        
        // Filter equipment by rarity
        List<Equipment> filteredEquipment = new ArrayList<>();
        for (Equipment equipment : availableEquipment) {
            if (equipment.getRarity() == rarity) {
                filteredEquipment.add(equipment);
            }
        }
        
        // If no equipment of the selected rarity, use common
        if (filteredEquipment.isEmpty()) {
            for (Equipment equipment : availableEquipment) {
                if (equipment.getRarity() == Equipment.Rarity.COMMON) {
                    filteredEquipment.add(equipment);
                }
            }
        }
        
        // Select random equipment from filtered list
        return filteredEquipment.get(random.nextInt(filteredEquipment.size()));
    }

    /**
     * Gets a list of all available equipment.
     * @return A list of all available equipment.
     */
    public List<Equipment> getAllEquipment() {
        return new ArrayList<>(availableEquipment);
    }
    
    public List<Equipment> getEquipmentByType(Equipment.Type type) {
        List<Equipment> result = new ArrayList<>();
        for (Equipment equipment : availableEquipment) {
            if (equipment.getType() == type) {
                result.add(equipment);
            }
        }
        return result;
    }
}