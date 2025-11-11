package progescps;

import java.util.HashMap;
import java.util.Map;

/**
 * AsciiArt class to manage ASCII art for enemies and other game elements
 */
public class AsciiArt {
    
    private static final Map<String, String> ENEMY_ART = new HashMap<>();
    
    static {
        // Initialize enemy art
        ENEMY_ART.put("virus", 
            "   .--.   \n" +
            "  |o_o |  \n" +
            "  |:_/ |  \n" +
            " //   \\ \\ \n" +
            "(|     | )\n" +
            "/'\\_   _/`\\\n" +
            "\\___)=(___/");
            
        ENEMY_ART.put("trojan", 
            "    /|    \n" +
            "   / |    \n" +
            "  *==|    \n" +
            "     |    \n" +
            "   __|__  \n" +
            "  /     \\ \n" +
            " |       |\n" +
            " |_______|\n");
            
        ENEMY_ART.put("worm", 
            "    _    \n" +
            "   (_)   \n" +
            " /~   ~\\ \n" +
            "@       @\n" +
            " \\_____/ \n");
            
        ENEMY_ART.put("bug", 
            "   \\ | /   \n" +
            "    \\|/    \n" +
            "    /^\\    \n" +
            "   //|\\\\   \n" +
            "  // | \\\\  \n");
            
        ENEMY_ART.put("default", 
            "   .---.   \n" +
            "  /     \\  \n" +
            " | () () | \n" +
            "  \\  ^  /  \n" +
            "   |||||   \n" +
            "   |||||   \n");
    }
    
    /**
     * Get ASCII art for an enemy based on its name
     * @param enemy The enemy to get art for
     * @return The ASCII art string
     */
    public static String getEnemyArt(Enemy enemy) {
        String enemyName = enemy.getCurrentName().toLowerCase();
        
        for (String key : ENEMY_ART.keySet()) {
            if (enemyName.contains(key)) {
                return ENEMY_ART.get(key);
            }
        }
        
        return ENEMY_ART.get("default");
    }
    
    /**
     * Get a simple frame for UI elements
     * @param width The width of the frame
     * @param height The height of the frame
     * @return The frame as a string
     */
    public static String getFrame(int width, int height) {
        StringBuilder frame = new StringBuilder();
        
        // Top border
        frame.append("┌").append("─".repeat(width - 2)).append("┐\n");
        
        // Middle section
        for (int i = 0; i < height - 2; i++) {
            frame.append("│").append(" ".repeat(width - 2)).append("│\n");
        }
        
        // Bottom border
        frame.append("└").append("─".repeat(width - 2)).append("┘\n");
        
        return frame.toString();
    }
}