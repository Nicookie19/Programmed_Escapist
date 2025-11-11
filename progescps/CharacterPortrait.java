package progescps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.*;

public class CharacterPortrait extends JPanel {
    private CharacterSprite sprite;
    private static final int MIN_SIZE = 160;
    private static final int PREFERRED_SIZE = 240;
    private CharacterSprite.State currentState = CharacterSprite.State.IDLE;
    
    public CharacterPortrait() {
        setPreferredSize(new Dimension(PREFERRED_SIZE, PREFERRED_SIZE));
        setMinimumSize(new Dimension(MIN_SIZE, MIN_SIZE));
        setBackground(new Color(28, 34, 45)); // Dark background for the portrait
        setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60), 2));
    }
    
    public void setCharacterClass(String className) {
        try {
            if (className == null || className.trim().isEmpty()) {
                System.err.println("Warning: Attempted to set null or empty class name");
                return;
            }
            
            // Stop any existing sprite animation
            if (sprite != null) {
                sprite.stopAnimation();
                sprite = null; // Clear old sprite
            }
            
            // Create new sprite
            System.out.println("Creating new sprite for class: " + className);
            sprite = new CharacterSprite(className, this);
            
            // Set initial state
            if (sprite != null) {
                sprite.setState(currentState);
                System.out.println("Successfully created sprite for " + className);
            } else {
                System.err.println("Failed to create sprite for " + className);
            }
            
            // Force redraw
            revalidate();
            repaint();
        } catch (Exception e) {
            System.err.println("Error creating sprite for " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void setState(CharacterSprite.State state) {
        currentState = state;
        if (sprite != null) {
            sprite.setState(state);
        }
    }
    
    public void startAnimation() {
        if (sprite != null) {
            sprite.startAnimation();
        }
    }
    
    public void stopAnimation() {
        if (sprite != null) {
            sprite.stopAnimation();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (sprite != null && sprite.isLoaded()) {
            // Center the sprite inside the panel's available area (respect insets/border)
            java.awt.Insets in = getInsets();
            int availW = getWidth() - in.left - in.right;
            int availH = getHeight() - in.top - in.bottom;
            int size = Math.max(0, Math.min(availW, availH));

            // Leave a small inner margin so the sprite doesn't touch the borders
            final int INNER_MARGIN = 16;
            size = Math.max(0, size - INNER_MARGIN);

            int x = in.left + (availW - size) / 2;
            int y = in.top + (availH - size) / 2;

            sprite.paint(g, x, y, size, size);
        } else {
            // Draw placeholder text if no sprite is loaded
            g.setColor(Color.GRAY);
            FontMetrics fm = g.getFontMetrics();
            String text = "Character Portrait";
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2;
            g.drawString(text, textX, textY);
        }
    }
}