package progescps;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The main panel for the game screen, layering game UI over an animated background.
 * This panel organizes the game into status, combat, and action areas.
 */
public class GamePanel extends JLayeredPane {

    private static final Font  UI_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final java.awt.Color UI_TEXT_COLOR = new java.awt.Color(220, 220, 220);
    private static final java.awt.Color PANEL_BACKGROUND_COLOR = new java.awt.Color(40, 40, 55, 180);
    private static final java.awt.Color BORDER_COLOR = new java.awt.Color(100, 100, 120);

    private StatusPanel statusPanel;
    private MessageLog messageLog;
    private ActionPanel actionPanel;
    private JPanel combatArea;

    public GamePanel() {
        // Set the layout for the JLayeredPane itself
        // 1. Add the animated background to the bottom layer
        AnimatedBackgroundPanel background = new AnimatedBackgroundPanel();
        background.setBounds(0, 0, 800, 600); // Initial size, will be resized
        add(background, JLayeredPane.DEFAULT_LAYER);

        // 2. Create a transparent panel for all the game UI
        JPanel  uiContainer = new JPanel(new BorderLayout());
        uiContainer.setOpaque(false);

        // --- Create the different sections of the game UI ---

        // Top: Status Panel (Player and Enemy Info)
        statusPanel = new StatusPanel();
        uiContainer.add(statusPanel, BorderLayout.NORTH);

        // Center: Combat Area (for character sprites, etc.)
        combatArea = new JPanel(new BorderLayout());
        combatArea.setOpaque(false); 
        combatArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel combatPlaceholder = new JLabel("Combat and Character Sprites Appear Here", SwingConstants.CENTER);
        combatPlaceholder.setFont(UI_FONT.deriveFont(20f));
        combatPlaceholder.setForeground(UI_TEXT_COLOR);
        combatArea.add(combatPlaceholder, BorderLayout.CENTER);
        uiContainer.add(combatArea, BorderLayout.CENTER);

        // Bottom: Action Menu and Message Log
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        messageLog = new MessageLog();
        actionPanel = new ActionPanel();

        bottomPanel.add(messageLog, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        uiContainer.add(bottomPanel, BorderLayout.SOUTH);

        // Add the UI container to the top layer
        uiContainer.setBounds(0, 0, 800, 600);
        add(uiContainer, JLayeredPane.PALETTE_LAYER);

        // Add a component listener to resize layers correctly
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                background.setBounds(0, 0, getWidth(), getHeight());
                uiContainer.setBounds(0, 0, getWidth(), getHeight());
            }
        });
    }

    // --- Public Methods to Update UI ---

    /**
     * Updates the player's status in the UI.
     * @param hp The current health points of the player.
     * @param maxHp The maximum health points of the player.
     * @param mana The current mana points of the player.
     * @param maxMana The maximum mana points of the player.
     */
    public void updatePlayerStatus(int hp, int maxHp, int mana, int maxMana) {
        statusPanel.updatePlayerStatus(hp, maxHp, mana, maxMana);
    }

    /**
     * Updates the enemy's status in the UI.
     * @param name The name of the enemy.
     * @param hp The current health points of the enemy.
     * @param maxHp The maximum health points of the enemy.
     */
    public void updateEnemyStatus(String name, int hp, int maxHp) {
        statusPanel.updateEnemyStatus(name, hp, maxHp);
    }

    /**
     * Adds a message to the combat log.
     * @param message The message to add to the combat log.
     */
    public void addCombatLogMessage(String message) {
        messageLog.addMessage(message);
    }

    /**
     * Clears the combat log.
     */
    public void clearCombatLog() {
        messageLog.clearLog();
    }

    /**
     * A panel to display player and enemy status.
     */
    private class StatusPanel extends JPanel {
        private JProgressBar playerHpBar, playerManaBar, enemyHpBar;

        private JLabel enemyNameLabel;
        private JPanel playerStatusEffects, enemyStatusEffects;

        StatusPanel() {
            setOpaque(false);
            setLayout(new GridLayout(1, 2, 20, 0)); // 1 row, 2 columns
            setBorder(new EmptyBorder(10, 10, 10, 10));

            // Player Status (Left)
            JPanel playerStatusPanel = createStyledPanel();
            playerStatusPanel.setLayout(new BoxLayout(playerStatusPanel, BoxLayout.Y_AXIS));
            playerHpBar = createStyledBar(0, 100, java.awt.Color.RED);
            playerManaBar = createStyledBar(0, 100, java.awt.Color.BLUE);
            playerStatusEffects = createStatusEffectIconPanel();

            playerStatusPanel.add(new JLabel("Player"));
            playerStatusPanel.add(playerHpBar);
            playerStatusPanel.add(playerManaBar);
            playerStatusPanel.add(playerStatusEffects);

            // Enemy Status (Right)
            JPanel enemyStatusPanel = createStyledPanel();
            enemyStatusPanel.setLayout(new BoxLayout(enemyStatusPanel, BoxLayout.Y_AXIS));
            enemyNameLabel = new JLabel("Enemy");
            enemyHpBar = createStyledBar(0, 100, java.awt.Color.RED);
            enemyStatusEffects = createStatusEffectIconPanel();

            enemyStatusPanel.add(enemyNameLabel);
            enemyStatusPanel.add(enemyHpBar);
            enemyStatusPanel.add(enemyStatusEffects);

            add(playerStatusPanel);
            add(enemyStatusPanel);
        }

        void updatePlayerStatus(int hp, int maxHp, int mana, int maxMana) {
            playerHpBar.setMaximum(maxHp);
            playerHpBar.setValue(hp);
            playerHpBar.setString("HP: " + hp + "/" + maxHp);

            playerManaBar.setMaximum(maxMana);
            playerManaBar.setValue(mana);
            playerManaBar.setString("MP: " + mana + "/" + maxMana);
        }

    /**
     * Updates the enemy's status in the UI.
     * @param name  The name of the enemy.
     * @param hp    The current health points of the enemy.
     * @param maxHp The maximum health points of the enemy.
     */
        void updateEnemyStatus(String name, int hp, int maxHp) {
            enemyNameLabel.setText(name);
            enemyHpBar.setMaximum(maxHp);
            enemyHpBar.setValue(hp);
            enemyHpBar.setString("HP: " + hp + "/" + maxHp);
        }

        private JProgressBar createStyledBar(int min, int max, java.awt.Color color) {
            JProgressBar bar = new JProgressBar(min, max); 
            bar.setStringPainted(true);
            bar.setFont(UI_FONT.deriveFont(12f));
            bar.setForeground(color);
            bar.setBackground(new java.awt.Color(PANEL_BACKGROUND_COLOR.getRed(), PANEL_BACKGROUND_COLOR.getGreen(), PANEL_BACKGROUND_COLOR.getBlue(), PANEL_BACKGROUND_COLOR.getAlpha()).brighter());
            bar.setBorder(new LineBorder(BORDER_COLOR));
            return bar;
        }

    /**
     * Creates a panel for displaying status effect icons.
     *
     * @return A JPanel for displaying status effect icons.
     */
        private JPanel createStatusEffectIconPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
            panel.setOpaque(false);
            // Example Icon
            // panel.add(new JLabel(new ImageIcon("path/to/poison_icon.png")));
            return panel;
        }
    }

    /**
     * A panel for displaying game messages and logs.
     */
    private class MessageLog extends JPanel {

        private JTextArea logArea;

        MessageLog() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setOpaque(false);

            this.logArea = new JTextArea("Game starts!\nAn enemy appears.");
            logArea.setEditable(false);
            logArea.setLineWrap(true);
            logArea.setWrapStyleWord(true);
            logArea.setFont(UI_FONT);
            logArea.setForeground(UI_TEXT_COLOR);
            logArea.setBackground(PANEL_BACKGROUND_COLOR);
            logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);

            add(scrollPane, BorderLayout.CENTER);
            setPreferredSize(new Dimension(400, 120));
        }

    /**
     * Adds a message to the combat log.
     *
     * @param message The message to add to the combat log.
     */
    void addMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
    }

    /**
     * Clears the combat log.
     */
    void clearLog() {
        logArea.setText("");
    }
    }

    /**
     * A panel containing the player's action buttons.
     */
    private class ActionPanel extends JPanel {
        ActionPanel() {
            setOpaque(false);
            setLayout(new GridLayout(2, 2, 10, 10)); // 2x2 grid for buttons
            setBorder(new EmptyBorder(10, 10, 10, 10));

            add(createStyledButton("Attack"));
            add(createStyledButton("Spell"));
            add(createStyledButton("Item"));
            add(createStyledButton("Defend"));
            
            setPreferredSize(new Dimension(300, 120));
        }
    }

    // --- Helper methods for creating styled components ---

    /**
     * Creates a styled JPanel with a background color and border.
     *
     * @return A styled JPanel.
     */
    private JPanel createStyledPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BACKGROUND_COLOR);
        panel.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        // Style the labels inside
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(UI_TEXT_COLOR);
                ((JLabel) comp).setFont(UI_FONT);
            }
        }
        return panel;
    }

    /**
     * Creates a styled JButton with custom styling for the game.
     *
     * @param text The text for the button.
     * @return A styled JButton.
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT.deriveFont(Font.BOLD));
        button.setForeground(UI_TEXT_COLOR);
        button.setBackground(new java.awt.Color(80, 80, 95));
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(BORDER_COLOR, 1));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        return button;
    }

    /**
     * Example of how to integrate this panel into a JFrame.
     */
    /**
     * Example of how to integrate this panel into a JFrame.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ProgEscpss Game Screen");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);

            frame.setVisible(true);
        });
    }
}