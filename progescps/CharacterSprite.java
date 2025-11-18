package progescps;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CharacterSprite {
    private static final int SPRITE_SIZE = 48;  // Base sprite size
    private static final int BASE_DISPLAY_SIZE = 200; // Base display size
    private static final int ANIMATION_SPEED = 500; // Milliseconds per frame
    private static final int FRAMES_PER_ROW = 4; // Number of frames in sprite sheet row
    private static final int PADDING = 20; // Padding around sprite
    
    public enum State {
        IDLE,       // Standing still
        MOVING,     // Traveling/moving
        COMBAT      // In combat
    }
    
    private BufferedImage spriteSheet;
    private int currentFrame = 0;
    private final Timer animationTimer;
    private final String characterClass;
    private final JComponent parent; // Parent component for repainting
    private BufferedImage[] frameCache; // Cache for sprite frames
    private State currentState = State.IDLE;
    
    /**
     * Constructs a new CharacterSprite for the specified character class.
     * @param characterClass The class of the character (e.g., "hacker", "debugger").
     * @param parent The parent JComponent for repainting during animation.
     */
    public CharacterSprite(String characterClass, JComponent parent) {
        this.characterClass = characterClass;
        this.parent = parent;

        // Set up animation timer first
        animationTimer = new Timer(ANIMATION_SPEED, e -> {
            currentFrame = (currentFrame + 1) % FRAMES_PER_ROW;
            if (parent != null) {
                parent.repaint();
            }
        });
        animationTimer.setInitialDelay(ANIMATION_SPEED);

        // Load sprite and cache frames (which will start animation when ready)
        System.out.println("Loading sprite for " + characterClass);
        loadSprite();
    }
    
    /**
     * Loads the sprite sheet for the character class from various possible locations.
     */
    private void loadSprite() {
        try {
            // Load via classpath so it works from jar or any working directory
            String resourcePath = getClasspathSpritePathForClass(characterClass);
            System.out.println("Attempting to load sprite resource: " + resourcePath);

            URL res = CharacterSprite.class.getResource(resourcePath);
            if (res != null) {
                spriteSheet = ImageIO.read(res);
                if (spriteSheet != null) {
                    System.out.println("Loaded sprite from classpath: " + resourcePath);
                    cacheFrames();
                    return;
                }
            } else {
                System.err.println("Sprite resource not found: " + resourcePath);
            }

            // Fallback to generic idle from classpath
            String idleResource = "/progescps/sprites/idle.png";
            URL idleRes = CharacterSprite.class.getResource(idleResource);
            if (idleRes != null) {
                spriteSheet = ImageIO.read(idleRes);
                if (spriteSheet != null) {
                    System.out.println("Loaded fallback idle sprite from classpath: " + idleResource);
                    cacheFrames();
                    return;
                }
            } else {
                System.err.println("Fallback idle resource not found: " + idleResource);
            }

            // As an additional fallback, attempt filesystem paths as you suggested
            String spritePathFs = getFileSpritePathForClass(characterClass);
            System.out.println("Attempting filesystem load for " + characterClass + " from " + spritePathFs);

            String[] basePaths = {
                "",
                "ProgEscps/",
                "./",
                "../",
                "./ProgEscps/",
                "../ProgEscps/",
                "bin/",
                "./bin/",
                "../bin/"
            };

            String[] pathPrefixes = {
                "",
                "src/",
                "src/progescps/",
                "ProgEscps/src/progescps/",
                "progescps/"
            };

            for (String basePath : basePaths) {
                for (String prefix : pathPrefixes) {
                    String fullPath = basePath + prefix + spritePathFs;
                    File spriteFile = new File(fullPath);
                    System.out.println("Checking path: " + spriteFile.getAbsolutePath());
                    if (spriteFile.exists()) {
                        System.out.println("Found sprite at: " + spriteFile.getAbsolutePath());
                        spriteSheet = ImageIO.read(spriteFile);
                        if (spriteSheet != null) {
                            cacheFrames();
                            return;
                        }
                    }
                }
            }

            // Try specific class-numbered file combinations on filesystem
            String charNum = switch(characterClass.toLowerCase()) {
                case "debugger" -> "1";
                case "hacker" -> "2";
                case "pentester" -> "3";
                case "architect" -> "7";
                case "tester" -> "8";
                case "support" -> "9";
                default -> "1";
            };

            String charFile = switch(characterClass.toLowerCase()) {
                case "debugger" -> "Character 1.png";
                case "hacker" -> "Character 5.png";
                case "pentester" -> "Character 9.png";
                case "architect" -> "Character 1.png";
                case "tester" -> "Character 5.png";
                case "support" -> "Character 9.png";
                default -> "Character 1.png";
            };

            String spriteFolder = "72 Character Free/Char ";
            for (String basePath : basePaths) {
                for (String prefix : pathPrefixes) {
                    String fullPath = basePath + prefix + spriteFolder + charNum + "/" + charFile;
                    File spriteFile = new File(fullPath);
                    System.out.println("Trying specific path: " + spriteFile.getAbsolutePath());
                    if (spriteFile.exists()) {
                        System.out.println("Found sprite at specific path: " + spriteFile.getAbsolutePath());
                        spriteSheet = ImageIO.read(spriteFile);
                        if (spriteSheet != null) {
                            cacheFrames();
                            return;
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading sprite for " + characterClass + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getClasspathSpritePathForClass(String className) {
        // First normalize the class name to handle case variations
        String normalizedClass = className.trim().toLowerCase();
        
        String spritePath;
        switch (normalizedClass) {
            case "debugger": spritePath = "sprites/debugger.png"; break;
            case "hacker": spritePath = "sprites/hacker.png"; break;
            case "pentester":
            case "pen tester":
                spritePath = "sprites/pentester.png"; break;
            case "architect": spritePath = "sprites/architect.png"; break;
            case "tester": spritePath = "sprites/tester.png"; break;
            case "support": spritePath = "sprites/support.png"; break;
            default:
                System.err.println("Warning: Using default sprite for unknown class: " + className);
                spritePath = "sprites/idle.png";
                break;
        }
        return "/progescps/" + spritePath;
    }

    private String getFileSpritePathForClass(String className) {
        // Same mapping as above, but returns filesystem-relative path within src
        String normalizedClass = className.trim().toLowerCase();
        String spritePath;
        switch (normalizedClass) {
            case "debugger": spritePath = "72 Character Free/Char 1/Character 1.png"; break;
            case "hacker": spritePath = "72 Character Free/Char 2/Character 5.png"; break;
            case "pentester":
            case "pen tester":
                spritePath = "72 Character Free/Char 3/Character 9.png"; break;
            case "architect": spritePath = "72 Character Free/Char 7/Character 1.png"; break;
            case "tester": spritePath = "72 Character Free/Char 8/Character 5.png"; break;
            case "support": spritePath = "72 Character Free/Char 9/Character 9.png"; break;
            default:
                spritePath = "72 Character Free/Char 1/Character 1.png";
                break;
        }
        return "src/progescps/" + spritePath;
    }
    
    private void cacheFrames() {
        if (spriteSheet == null) return;

        try {
            int sheetW = spriteSheet.getWidth();
            int sheetH = spriteSheet.getHeight();
            int frameW = SPRITE_SIZE;
            int frameH = Math.min(SPRITE_SIZE, sheetH);

            // Determine how many frames are actually available horizontally
            int available = Math.max(1, sheetW / Math.max(1, frameW));
            int framesToUse = Math.max(1, Math.min(FRAMES_PER_ROW, available));

            frameCache = new BufferedImage[framesToUse];
            int scaledSize = BASE_DISPLAY_SIZE - (PADDING * 2);

            for (int i = 0; i < framesToUse; i++) {
                int x = i * frameW;
                if (x + frameW > sheetW) break;
                BufferedImage frame = spriteSheet.getSubimage(x, 0, frameW, frameH);

                BufferedImage scaled = new BufferedImage(scaledSize, scaledSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaled.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.drawImage(frame, 0, 0, scaledSize, scaledSize, null);
                g2d.dispose();

                frameCache[i] = scaled;
            }

            // Start animation only if we have multiple frames
            if (frameCache.length > 1) {
                animationTimer.start();
            } else {
                animationTimer.stop();
                currentState = State.IDLE;
            }
        } catch (RuntimeException ex) {
            // Fallback: show the whole image as a single frame if subimage slicing fails
            int scaledSize = BASE_DISPLAY_SIZE - (PADDING * 2);
            BufferedImage scaled = new BufferedImage(scaledSize, scaledSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(spriteSheet, 0, 0, scaledSize, scaledSize, null);
            g2d.dispose();
            frameCache = new BufferedImage[] { scaled };
            animationTimer.stop();
            currentState = State.IDLE;
            System.err.println("Sprite slicing failed, using single-frame fallback: " + ex.getMessage());
        }
    }
    
    public void paint(Graphics g, int x, int y, int width, int height) {
        int scaledSize = Math.max(0, Math.min(width, height) - (PADDING * 2));
        int xPos = x + (width - scaledSize) / 2;
        int yPos = y + (height - scaledSize) / 2;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (frameCache != null && currentFrame < frameCache.length) {
            BufferedImage frame = frameCache[currentState == State.IDLE ? 0 : currentFrame];
            g2d.drawImage(frame, xPos, yPos, scaledSize, scaledSize, null);
        } else if (spriteSheet != null) {
            // Fallback draw of the entire sprite sheet scaled
            g2d.drawImage(spriteSheet, xPos, yPos, scaledSize, scaledSize, null);
        }
    }
    
    public void setState(State newState) {
        if (this.currentState == newState) return;
        
        this.currentState = newState;
        currentFrame = 0;
        
        if (newState == State.IDLE) {
            animationTimer.stop();
        } else {
            animationTimer.start();
        }
    }
    
    public void stopAnimation() {
        setState(State.IDLE);
    }

    public boolean isLoaded() {
        return spriteSheet != null;
    }
    
    public void startAnimation() {
        setState(State.MOVING);
    }
    
    public State getState() {
        return currentState;
    }
}