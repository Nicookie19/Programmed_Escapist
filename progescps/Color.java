package progescps;

public class Color {

    /**
     * Provides ANSI color codes for console output.
     *
     * The colorize method applies these codes to a given text, enabling colored output in the console.
     */

    public static final String RESET = "\u001B[0m";
    public static final String WHITE = "\u001B[37m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String ORANGE = "\u001B[38;5;208m";
    public static final String GRAY = "\u001B[90m";
    public static boolean USE_ANSI = true;

    public static String colorize(String text, String color) {
    /**
     * Applies an ANSI color code to a given text.
     *
     * @param text  The text to colorize.
     * @param color The ANSI color code to apply.
     * @return The colorized text, or the original text if ANSI colors are disabled.
     */
        if (!USE_ANSI) {
            return text;
        }
        return color + text + RESET;
    }
}