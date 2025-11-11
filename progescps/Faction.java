package progescps;

import java.io.Serializable;

public class Faction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private boolean isMember;
    private int reputation;

    public Faction(String name) {
        this.name = name;
        this.isMember = false;
        this.reputation = 0;
    }

    // --- [NEW] ---
    /**
     * Constructor for loading a faction from the database.
     * Assumes a loaded faction is one the player is already a member of.
     */
    public Faction(String name, int reputation) {
        this.name = name;
        this.reputation = reputation;
        this.isMember = true; // If it's in the save, the player is a member
    }
    // --- [END NEW] ---

    public String getName() {
        return name;
    }

    public boolean isMember() {
        return isMember;
    }

    public void joinFaction() {
        isMember = true;
        System.out.println("You have joined the " + name + "!");
    }

    public void addReputation(int amount) {
        reputation += amount;
        System.out.println("Gained " + amount + " reputation with " + name + ". Total: " + reputation);
    }

    public int getReputation() {
        return reputation;
    }

    public void applyBenefits(Hero player) {
        switch (name) {
            case "Companions":
                player.maxHP += 10;
                System.out.println("Companions training increases your HP!");
                break;
            case "Thieves Guild":
                player.gold += 50;
                System.out.println("Thieves Guild contacts provide extra gold!");
                break;
            case "Dark Brotherhood":
                player.maxDmg += 5;
                System.out.println("Dark Brotherhood training sharpens your attacks!");
                break;
            case "College of Winterhold":
                player.maxMana += 20;
                System.out.println("College studies enhance your mana!");
                break;
            case "Imperial Legion":
                player.minDmg += 3;
                System.out.println("Legion discipline improves your combat!");
                break;
        }
    }
}