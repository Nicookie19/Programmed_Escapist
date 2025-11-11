package progescps;

/**
 * UITheme class to manage different UI themes for the game
 */
public class UITheme {
    
    public enum ThemeType {
        DEFAULT, DARK, RETRO, CYBERPUNK
    }
    
    private ThemeType currentTheme;
    
    // Theme colors
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String textColor;
    private String highlightColor;
    
    public UITheme() {
        setTheme(ThemeType.DEFAULT);
    }
    
    public void setTheme(ThemeType theme) {
        this.currentTheme = theme;
        
        switch (theme) {
            case DEFAULT:
                primaryColor = Color.BLUE;
                secondaryColor = Color.GREEN;
                accentColor = Color.YELLOW;
                textColor = Color.WHITE;
                highlightColor = Color.BLUE;
                break;
            case DARK:
                primaryColor = Color.PURPLE;
                secondaryColor = Color.BLUE;
                accentColor = Color.BLUE;
                textColor = Color.GRAY;
                highlightColor = Color.WHITE;
                break;
            case RETRO:
                primaryColor = Color.GREEN;
                secondaryColor = Color.YELLOW;
                accentColor = Color.RED;
                textColor = Color.GREEN;
                highlightColor = Color.WHITE;
                break;
            case CYBERPUNK:
                primaryColor = Color.BLUE;
                secondaryColor = Color.PURPLE;
                accentColor = Color.YELLOW;
                textColor = Color.BLUE;
                highlightColor = Color.RED;
                break;
        }
    }
    
    public ThemeType getCurrentTheme() {
        return currentTheme;
    }
    
    public String getPrimaryColor() {
        return primaryColor;
    }
    
    public String getSecondaryColor() {
        return secondaryColor;
    }
    
    public String getAccentColor() {
        return accentColor;
    }
    
    public String getTextColor() {
        return textColor;
    }
    
    public String getHighlightColor() {
        return highlightColor;
    }
    
    public String formatHeader(String text) {
        return Color.colorize("╔═" + "═".repeat(text.length() + 2) + "═╗\n" +
                                "║ " + text + " ║\n" +
                                "╚═" + "═".repeat(text.length() + 2) + "═╝", primaryColor);
    }
    
    public String formatMenuItem(int index, String text) {
        return Color.colorize("[" + index + "] ", accentColor) + 
               Color.colorize(text, textColor);
    }
    
    public String formatHighlight(String text) {
        return Color.colorize(text, highlightColor);
    }
    
    public String formatEnemyArt(String art, Enemy.Tier tier) {
        String color = tier == Enemy.Tier.WEAK ? Color.GRAY :
                       tier == Enemy.Tier.NORMAL ? secondaryColor : accentColor;
        return Color.colorize(art, color);
    }

    public int getMenuWidth() {
        return 80; // Default menu width, can be made configurable later
    }

    // Menu-specific color methods
    public String getMainMenuColor() {
        switch (currentTheme) {
            case DEFAULT: return Color.BLUE;
            case DARK: return Color.PURPLE;
            case RETRO: return Color.GREEN;
            case CYBERPUNK: return Color.BLUE;
            default: return Color.BLUE;
        }
    }

    public String getCombatColor() {
        return Color.RED; // Consistent across themes
    }

    public String getFactionColor() {
        return Color.PURPLE; // Consistent across themes
    }

    public String getTownColor() {
        return Color.GREEN; // Consistent across themes
    }

    public String getInventoryColor() {
        return Color.PURPLE; // Consistent across themes
    }

    public String getQuestColor() {
        switch (currentTheme) {
            case DEFAULT: return Color.BLUE;
            case DARK: return Color.BLUE;
            case RETRO: return Color.GREEN;
            case CYBERPUNK: return Color.BLUE;
            default: return Color.BLUE;
        }
    }

    public String getSettingsColor() {
        return Color.PURPLE; // Consistent across themes
    }

    public String getDifficultyColor() {
        return Color.PURPLE; // Consistent across themes
    }

    public String getPermadeathColor() {
        return Color.RED; // Consistent across themes
    }

    public String getTravelColor() {
        return Color.PURPLE; // Consistent across themes
    }

    public String getNpcColor() {
        return Color.GREEN; // Consistent across themes
    }

    public String getTradeColor() {
        return Color.PURPLE; // Consistent across themes
    }

    public String getStatsColor() {
        return Color.RED; // Consistent across themes
    }

    // --- [FIXED] ---
    // Added the missing method for gold color
    public String getGoldColor() {
        return Color.YELLOW; // Consistent across themes
    }
    // --- [END FIX] ---

    public String getInnColor() {
        return Color.PURPLE; // Consistent across themes
    }

    public String getAchievementColor() {
        return Color.YELLOW; // Consistent across themes
    }
}