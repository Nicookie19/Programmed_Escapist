package progescps;

public class SpriteTest {
    public static void main(String[] args) {
        // Force loading of every class sprite
        String[] classes = {"Debugger", "Hacker", "PenTester", "Architect", "Tester", "Support"};
        for (String cls : classes) {
            System.out.println("Testing sprite load for: " + cls);
            CharacterSprite sprite = new CharacterSprite(cls, null);
            if (sprite.isLoaded()) {
                System.out.println("  -> SUCCESS: " + cls + " sprite loaded.");
            } else {
                System.out.println("  -> FAIL: " + cls + " sprite NOT loaded.");
            }
        }
    }
}