package progescps;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Full GameUI.java updated to use GameManager.loadGameFromAsync(...) to avoid race conditions
 * when loading saves from the GUI. The UI shows a small modal progress dialog while the
 * DB load + game-thread startup complete, then switches to the GAME card and refreshes stats.
 *
 * This is the full code for GameUI (copied and updated from your provided code).
 */
public class GameUI {

    private final JFrame frame;
    private final InputProvider inputProvider;
    private final GameManager manager;

    private final JPanel rootPanel;
    private final CardLayout cardLayout;

    private final JPanel loadingPanel;
    private final JPanel mainMenuPanel;
    private final JPanel gamePanel;

    private JProgressBar loadingBar;

    private JButton menuStartBtn;
    private JButton menuLoadBtn;
    private JButton menuQuitBtn;

    private CharacterPortrait characterPortrait;
    private JLabel classLabel;
    private JLabel hpLabel;
    private JLabel manaLabel;
    private JLabel goldLabel;
    private JTextPane consoleArea;
    private JTextField inputField;

    // --- [NEW] ---
    // Label to show database connection status
    private javax.swing.JLabel dbStatusLabel;
    // --- [END NEW] ---

    private javax.swing.JLabel statusLabel;

    private java.util.List<String> inputHistory = new java.util.ArrayList<>();
    private int historyIndex = -1;

    private boolean darkTheme = true;
    private int consoleFontSize = 12;
    private int spriteMaxSize = 110; // target sprite size in pixels
    private javax.swing.JButton themeBtn;
    private javax.swing.JButton fontIncBtn;
    private javax.swing.JButton fontDecBtn;
    private javax.swing.JPanel gameContainerPanel;
    private javax.swing.JSplitPane gameSplitPane;
    private javax.swing.JPanel gameLeftPanel;
    private javax.swing.JPanel gameRightPanel;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JPanel consoleToolbarPanel;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JPanel bottomStackPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel headerTitle;
    private javax.swing.JComponent statHpBar;
    private javax.swing.JComponent statManaBar;
    private javax.swing.JLabel loadingTextLabel;
    private javax.swing.JLabel loadingTipLabel;
    private javax.swing.Timer loadingDotsTimer;
    private javax.swing.JButton menuContinueBtn;

    private String currentSpriteClass;

    private static final java.awt.Color BG_DARK = new java.awt.Color(25, 25, 35);
    private static final java.awt.Color ACCENT_LIGHT = new java.awt.Color(220, 220, 255);
    private static final java.awt.Color FOOTER = new java.awt.Color(190, 190, 220);
    private static final java.awt.Color BUTTON_BG = new java.awt.Color(45, 62, 92);
    private static final java.awt.Color BUTTON_BORDER = new java.awt.Color(160, 180, 220);
    private static final java.awt.Color GAME_LEFT_BG = new java.awt.Color(28, 34, 45);

    private static final java.awt.Color CONSOLE_BG = BG_DARK;
    private static final java.awt.Color CONSOLE_TEXT = java.awt.Color.WHITE;

    public GameUI() {

        try {
            progescps.Color.USE_ANSI = true;
        } catch (Throwable ignored) {
        }

        this.inputProvider = new InputProvider();
        this.manager = new GameManager(inputProvider);
        this.manager.setUi(this); // Give GameManager a reference to the UI

        // Enable ANSI colors for console output
        progescps.Color.USE_ANSI = true;

        frame = new JFrame("Codeborne - UI Console");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(820, 520);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        frame.setContentPane(rootPanel);

        loadingPanel = createLoadingPanel();
        mainMenuPanel = createMainMenuPanel();
        gamePanel = createGamePanel();

        rootPanel.add(loadingPanel, "LOADING");
        rootPanel.add(mainMenuPanel, "MENU");
        rootPanel.add(gamePanel, "GAME");

        cardLayout.show(rootPanel, "LOADING");

        frame.setVisible(true);

        wireGlobalShortcuts();

        startLoadingAnimation();

        SwingUtilities.invokeLater(this::startLoadingSequence);

        javax.swing.Timer statsTimer = new javax.swing.Timer(500, evt -> refreshStats());
        statsTimer.setRepeats(true);
        statsTimer.start();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                statsTimer.stop();
                stopAllAnimations();
            }
        });

        // --- [NEW] ---
        // Test database connection on startup
        testAndSetDBStatus();
        // --- [END NEW] ---
    }

    private void stopAllAnimations() {
        if (loadingPanel.getComponent(1) instanceof AnimatedBackgroundPanel) {
            ((AnimatedBackgroundPanel) loadingPanel.getComponent(1)).stop();
        }
        if (mainMenuPanel.getComponent(1) instanceof AnimatedBackgroundPanel) {
            ((AnimatedBackgroundPanel) mainMenuPanel.getComponent(1)).stop();
        }
        stopLoadingAnimation();
    }


    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setBackground(BG_DARK);

        JLabel title = new JLabel("Codeborne: Programmed Escapist", SwingConstants.CENTER);
        title.setForeground(ACCENT_LIGHT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, BorderLayout.NORTH);

		JPanel center = new AnimatedBackgroundPanel();
        center.setOpaque(true);
        center.setBackground(BG_DARK);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(40, 80, 40, 80));

        loadingTextLabel = new JLabel("Loading");
        loadingTextLabel.setForeground(java.awt.Color.WHITE);
        loadingTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingTextLabel.setFont(loadingTextLabel.getFont().deriveFont(Font.PLAIN, 14f));
        center.add(loadingTextLabel);

        center.add(Box.createRigidArea(new Dimension(0, 12)));

        loadingBar = new JProgressBar(0, 100);
        loadingBar.setValue(0);
        loadingBar.setStringPainted(true);
        loadingBar.setPreferredSize(new Dimension(520, 28));
        loadingBar.setMaximumSize(new Dimension(520, 28));
        loadingBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingBar.setForeground(ACCENT_LIGHT);
        loadingBar.setBackground(BUTTON_BG.darker());
        center.add(loadingBar);

        center.add(Box.createRigidArea(new Dimension(0, 12)));
        loadingTipLabel = new JLabel("Tip: Use arrow keys to navigate menus.");
        loadingTipLabel.setForeground(FOOTER);
        loadingTipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingTipLabel.setFont(loadingTipLabel.getFont().deriveFont(Font.PLAIN, 12f));
        center.add(loadingTipLabel);

        panel.add(center, BorderLayout.CENTER);

        JLabel footer = new JLabel("A Tale of Code and Digital Adventures", SwingConstants.CENTER);
        footer.setForeground(FOOTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(BG_DARK);

        JLabel title = new JLabel("Codeborne: Odyssey of the Programmer", SwingConstants.CENTER);
        title.setForeground(ACCENT_LIGHT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, BorderLayout.NORTH);

		JPanel center = new AnimatedBackgroundPanel();
        center.setBorder(new EmptyBorder(24, 12, 24, 12));
        center.setOpaque(true);
        center.setBackground(BG_DARK);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        center.add(Box.createVerticalGlue());

        menuContinueBtn = new JButton("Continue");
        menuStartBtn = new JButton("Start Game");
        menuLoadBtn = new JButton("Load Game");
        menuQuitBtn = new JButton("Quit");

        Dimension btnSize = new Dimension(360, 44);
        menuContinueBtn.setMaximumSize(btnSize);
        menuStartBtn.setMaximumSize(btnSize);
        menuLoadBtn.setMaximumSize(btnSize);
        menuQuitBtn.setMaximumSize(btnSize);

        menuContinueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuStartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuLoadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuQuitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleMenuButton(menuContinueBtn);
        styleMenuButton(menuStartBtn);
        styleMenuButton(menuLoadBtn);
        styleMenuButton(menuQuitBtn);

        center.add(menuContinueBtn);
        center.add(Box.createRigidArea(new Dimension(0, 12)));
        center.add(menuStartBtn);
        center.add(Box.createRigidArea(new Dimension(0, 12)));
        center.add(menuLoadBtn);
        center.add(Box.createRigidArea(new Dimension(0, 12)));
        center.add(menuQuitBtn);

        center.add(Box.createVerticalGlue());

        panel.add(center, BorderLayout.CENTER);

        JLabel footer = new JLabel("A Tale of Code and Digital Adventures", SwingConstants.CENTER);
        footer.setForeground(FOOTER);
        panel.add(footer, BorderLayout.SOUTH);

        updateContinueAvailability();

        wireMenuKeyboardNavigation(new JButton[]{menuContinueBtn, menuStartBtn, menuLoadBtn, menuQuitBtn});

        menuContinueBtn.addActionListener(e -> loadLatestSave());
        menuStartBtn.addActionListener(e -> {
            manager.requestStopCurrentGame();
            inputProvider.clearPending();
            showClassSelectionDialog();
        });
        menuLoadBtn.addActionListener(e -> showLoadDialogFromMenu());
        menuQuitBtn.addActionListener(e -> {
            boolean ok = showConfirmation("Quit Game", "Are you sure you want to quit the game?");
            if (ok) {
                manager.uiQuitGame();
            }
        });

        return panel;
    }

    private void styleMenuButton(final JButton b) {
        b.setBackground(BUTTON_BG);
        b.setForeground(ACCENT_LIGHT);
        b.setFocusPainted(false);
        b.setOpaque(true);
		b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BUTTON_BORDER, 1), new EmptyBorder(5, 15, 5, 15)));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));

        final java.awt.Color base = BUTTON_BG;
        final java.awt.Color hover = base.brighter();
        final java.awt.Color press = base.darker();
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(base);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                b.setBackground(press);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                b.setBackground(hover);
            }
        });
    }

    private void styleHeaderButton(final JComponent b) {
        b.setOpaque(false);
        b.setForeground(darkTheme ? FOOTER : java.awt.Color.GRAY);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));

        if (b instanceof JButton) {
            JButton btn = (JButton) b;
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorder(new EmptyBorder(4, 6, 4, 6));
        } else if (b instanceof JLabel) {
            ((JLabel) b).setBorder(new EmptyBorder(4, 6, 4, 6));
        }

        final java.awt.Color base = darkTheme ? FOOTER : java.awt.Color.GRAY;
        final java.awt.Color hover = darkTheme ? ACCENT_LIGHT : java.awt.Color.BLACK;
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(base);
            }
        });
    }

    private void startLoadingSequence() {
        new Thread(() -> {
            try {
                String[] tips = {
                        "Tip: Use arrow keys to navigate menus.",
                        "Tip: Press Enter to select.",
                        "Tip: A+/A- adjusts console font size.",
                        "Tip: Theme toggle in top-right."
                };
                int ti = 0;
                for (int i = 0; i <= 100; i += 5) {
                    final int v = i;
                    final String tip = tips[ti % tips.length];
                    SwingUtilities.invokeLater(() -> {
                        loadingBar.setValue(v);
                        if (loadingTipLabel != null) {
                            loadingTipLabel.setText(tip);
                        }
                    });
                    Thread.sleep(90);
                    ti++;
                }
            } catch (InterruptedException ignored) {
            }
            SwingUtilities.invokeLater(() -> {
                loadingBar.setValue(100);
                stopLoadingAnimation();
                cardLayout.show(rootPanel, "MENU");
            });
        }, "Loading-Thread").start();
    }

    private JPanel createGamePanel() {

        gameContainerPanel = new JPanel(new BorderLayout(8, 8));
        gameContainerPanel.setBackground(darkTheme ? BG_DARK : java.awt.Color.WHITE);
        JPanel container = gameContainerPanel;

        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(darkTheme ? BG_DARK : new java.awt.Color(245, 245, 252));
        headerPanel.setBorder(new EmptyBorder(12, 14, 12, 14));
        headerTitle = new JLabel("Codeborne Adventure");
        headerTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerTitle.setForeground(darkTheme ? ACCENT_LIGHT : java.awt.Color.DARK_GRAY);
        headerTitle.setIcon(new CircleIcon(darkTheme ? ACCENT_LIGHT : java.awt.Color.GRAY));
        headerTitle.setIconTextGap(10);
        headerPanel.add(headerTitle, BorderLayout.WEST);

        JPanel headerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        headerControls.setOpaque(false);

        // --- [NEW] ---
        dbStatusLabel = new JLabel("DB Status: ?");
        dbStatusLabel.setToolTipText("Database Connection Status");
        styleHeaderButton(dbStatusLabel);
        headerControls.add(dbStatusLabel);
        // --- [END NEW] ---

        themeBtn = new JButton(darkTheme ? "Theme: Dark" : "Theme: Light");
        fontDecBtn = new JButton("A-");
        fontIncBtn = new JButton("A+");

        themeBtn.setToolTipText("Ctrl+T to toggle theme");
        fontDecBtn.setToolTipText("Ctrl+- to decrease console font");
        fontIncBtn.setToolTipText("Ctrl+= to increase console font");

        themeBtn.addActionListener(e -> {
            darkTheme = !darkTheme;
            themeBtn.setText(darkTheme ? "Theme: Dark" : "Theme: Light");
            applyTheme();
        });
        fontDecBtn.addActionListener(e -> {
            consoleFontSize = Math.max(10, consoleFontSize - 1);
            updateConsoleFontSize();
            inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, consoleFontSize)));
            statusLabel.setText("Font: " + consoleFontSize + "pt");
        });
        fontIncBtn.addActionListener(e -> {
            consoleFontSize = Math.min(24, consoleFontSize + 1);
            updateConsoleFontSize();
            inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, consoleFontSize)));
            statusLabel.setText("Font: " + consoleFontSize + "pt");
        });

        styleHeaderButton(themeBtn);
        styleHeaderButton(fontDecBtn);
        styleHeaderButton(fontIncBtn);
        headerControls.add(themeBtn);
        headerControls.add(fontDecBtn);
        headerControls.add(fontIncBtn);
        headerPanel.add(headerControls, BorderLayout.EAST);
        container.add(headerPanel, BorderLayout.NORTH);

        gameSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane split = gameSplitPane;
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        int leftWidth = Math.max(240, frame.getWidth() / 3);
        split.setDividerLocation(leftWidth);
        split.setResizeWeight(0.0);
        split.setBorder(null);

        gameLeftPanel = new JPanel(new BorderLayout(10, 10));
        JPanel left = gameLeftPanel;
        left.setBorder(new EmptyBorder(12, 12, 12, 12));
        left.setBackground(darkTheme ? GAME_LEFT_BG : new java.awt.Color(232, 236, 245));

        JPanel portraitContainer = new JPanel(new BorderLayout());
        portraitContainer.setOpaque(false);
        portraitContainer.setMinimumSize(new Dimension(160, 160));
        portraitContainer.setPreferredSize(new Dimension(240, 240));

        characterPortrait = new CharacterPortrait();
        portraitContainer.add(characterPortrait, BorderLayout.CENTER);
        left.add(portraitContainer, BorderLayout.CENTER);

        statsPanel = new RoundedPanel(16);
        JPanel stats = statsPanel;
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.setBackground(left.getBackground());
        stats.setBorder(new EmptyBorder(16, 16, 16, 16));
        classLabel = new JLabel("Class: -");
        classLabel.setForeground(darkTheme ? ACCENT_LIGHT : java.awt.Color.DARK_GRAY);
        classLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        hpLabel = new JLabel("HP: -");
        hpLabel.setForeground(darkTheme ? ACCENT_LIGHT : java.awt.Color.DARK_GRAY);
        hpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statHpBar = new StatBar(darkTheme ? new java.awt.Color(200, 85, 85) : new java.awt.Color(220, 90, 90));

        manaLabel = new JLabel("Mana: -");
        manaLabel.setForeground(darkTheme ? ACCENT_LIGHT : java.awt.Color.DARK_GRAY);
        manaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statManaBar = new StatBar(darkTheme ? new java.awt.Color(85, 140, 220) : new java.awt.Color(90, 150, 230));

        goldLabel = new BadgeLabel("Gold: -");
        goldLabel.setForeground(darkTheme ? ACCENT_LIGHT : java.awt.Color.DARK_GRAY);
        goldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statsContent = new JPanel();
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);

        statsContent.add(classLabel);
        statsContent.add(Box.createRigidArea(new Dimension(0, 8)));
        statsContent.add((JComponent) statHpBar);
        statsContent.add(Box.createRigidArea(new Dimension(0, 8)));
        statsContent.add((JComponent) statManaBar);
        statsContent.add(Box.createRigidArea(new Dimension(0, 8)));
        statsContent.add(goldLabel);

        stats.add(statsContent);
        left.add(stats, BorderLayout.NORTH);

        controlsPanel = new RoundedPanel(16);
        JPanel controls = controlsPanel;
        controls.setLayout(new GridLayout(2, 2, 10, 10));
        controls.setBackground(left.getBackground());
        controls.setBorder(new EmptyBorder(16, 16, 16, 16));
        JButton newBtn = new JButton("New Game");
        JButton saveBtn = new JButton("Save Game");
        JButton loadBtn = new JButton("Load Game");
        JButton quitBtn = new JButton("Main Menu");

        newBtn.setToolTipText("Start a new game");
        saveBtn.setToolTipText("Save current progress");
        loadBtn.setToolTipText("Load a saved game");
        quitBtn.setToolTipText("Return to Main Menu");

        styleMenuButton(newBtn);
        styleMenuButton(saveBtn);
        styleMenuButton(loadBtn);
        styleMenuButton(quitBtn);
        controls.add(newBtn);
        controls.add(saveBtn);
        controls.add(loadBtn);
        controls.add(quitBtn);
        left.add(controls, BorderLayout.SOUTH);

        newBtn.addActionListener(e -> {
            boolean ok = showConfirmation("Start New Game", "Start a new game? Current progress will be lost.");
            if (ok) {
                manager.requestStopCurrentGame();
                inputProvider.clearPending();
                consoleArea.setText("");
                showClassSelectionDialog();
            }
        });
        saveBtn.addActionListener(e -> showSaveDialog());
        loadBtn.addActionListener(e -> showLoadDialogFromGame());
        quitBtn.addActionListener(e -> {
            boolean ok = showConfirmation("Return to Main Menu", "Return to Main Menu? Current progress will continue in background.");
            if (ok) {
                cardLayout.show(rootPanel, "MENU");
            }
        });

        gameRightPanel = new JPanel(new BorderLayout(8, 8));
        JPanel right = gameRightPanel;
        right.setBorder(new EmptyBorder(12, 12, 12, 12));
        right.setBackground(darkTheme ? BG_DARK : java.awt.Color.WHITE);

        consoleArea = new JTextPane();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, consoleFontSize));
        consoleArea.setBackground(CONSOLE_BG);
        consoleArea.setForeground(CONSOLE_TEXT);
        ((javax.swing.text.DefaultCaret) consoleArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        StyledDocument doc = consoleArea.getStyledDocument();
        Style defaultStyle = consoleArea.addStyle("default", null);
        StyleConstants.setFontFamily(defaultStyle, Font.MONOSPACED);
        StyleConstants.setFontSize(defaultStyle, consoleFontSize);
        JScrollPane consoleScroll = new JScrollPane(consoleArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        consoleScroll.setBorder(BorderFactory.createLineBorder(FOOTER.darker(), 1));
        right.add(consoleScroll, BorderLayout.CENTER);

        consoleToolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JPanel consoleToolbar = consoleToolbarPanel;
        consoleToolbar.setBackground(darkTheme ? BG_DARK : java.awt.Color.WHITE);

        JButton clearBtn = new JButton("Clear");
        JButton copyBtn = new JButton("Copy");
        styleMenuButton(clearBtn);
        styleMenuButton(copyBtn);
        clearBtn.addActionListener(e -> consoleArea.setText(""));
        copyBtn.addActionListener(e -> {
            String text = consoleArea.getText();
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text), null);
        });

        inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBackground(darkTheme ? BG_DARK : java.awt.Color.WHITE);
        inputField = new JTextField();
        inputField.setBackground(java.awt.Color.WHITE);
        inputField.setForeground(java.awt.Color.BLACK);
        inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, consoleFontSize)));
        inputField.setPreferredSize(new Dimension(520, 36));
        inputPanel.add(inputField, BorderLayout.CENTER);
        JButton sendBtn = new JButton("Send");
        styleMenuButton(sendBtn);
        sendBtn.setPreferredSize(new Dimension(90, 36));
        inputPanel.add(sendBtn, BorderLayout.EAST);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(ACCENT_LIGHT);
        statusLabel.setBorder(new EmptyBorder(0, 8, 8, 8));
        bottomStackPanel = new JPanel();
        JPanel bottomStack = bottomStackPanel;
        bottomStack.setLayout(new BoxLayout(bottomStack, BoxLayout.Y_AXIS));
        bottomStack.setBackground(darkTheme ? BG_DARK : java.awt.Color.WHITE);
        bottomStack.add(inputPanel);
        bottomStack.add(statusLabel);
        right.add(bottomStack, BorderLayout.SOUTH);

        inputField.addActionListener(e -> submitFromInputField());
        sendBtn.addActionListener(e -> submitFromInputField());

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (!inputHistory.isEmpty()) {
                        if (historyIndex < 0) {
                            historyIndex = inputHistory.size() - 1;
                        } else {
                            historyIndex = Math.max(0, historyIndex - 1);
                        }
                        inputField.setText(inputHistory.get(historyIndex));
                    }
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!inputHistory.isEmpty()) {
                        if (historyIndex < 0) {
                            historyIndex = inputHistory.size() - 1;
                        } else {
                            historyIndex = Math.min(inputHistory.size() - 1, historyIndex + 1);
                        }
                        inputField.setText(inputHistory.get(historyIndex));
                    }
                    e.consume();
                }
            }
        });

        InputMap im = inputField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = inputField.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "submitCmd");
        am.put("submitCmd", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitFromInputField();
            }
        });

        InputMap gim = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap gam = container.getActionMap();
        gim.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "focusInput");
        gam.put("focusInput", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputField.requestFocusInWindow();
            }
        });

        gim.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK), "fontInc");
        gam.put("fontInc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fontIncBtn != null) {
                    fontIncBtn.doClick();
                }
            }
        });
        gim.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "fontDec");
        gam.put("fontDec", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fontDecBtn != null) {
                    fontDecBtn.doClick();
                }
            }
        });

        split.setLeftComponent(left);
        split.setRightComponent(right);
        container.add(split, BorderLayout.CENTER);

        ConsoleOutputInterceptor interceptor = new ConsoleOutputInterceptor(consoleArea);
        PrintStream ps = new PrintStream(interceptor, true);
        System.setOut(ps);
        System.setErr(ps);

        return container;
    }

    private void showClassSelectionDialog() {

        JDialog dialog = new JDialog(frame, "Choose Your Class", true);
        dialog.setSize(640, 320);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.getContentPane().setBackground(BG_DARK);

        JLabel header = new JLabel("Select your class", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setForeground(ACCENT_LIGHT);
        header.setBorder(new EmptyBorder(8, 8, 8, 8));
        dialog.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setBorder(new EmptyBorder(12, 12, 12, 12));
        grid.setOpaque(true);
        grid.setBackground(BG_DARK);

        JButton btnDebugger = new JButton("<html><center>Debugger<br/><small>High HP & Defense</small></center></html>");
        JButton btnHacker = new JButton("<html><center>Hacker<br/><small>High Mana & Exploits</small></center></html>");
        JButton btnTester = new JButton("<html><center>Tester<br/><small>Criticals & Debuffs</small></center></html>");
        JButton btnArchitect = new JButton("<html><center>Architect<br/><small>Design & Rally</small></center></html>");
        JButton btnPenTester = new JButton("<html><center>PenTester<br/><small>Stealth & Critical</small></center></html>");
        JButton btnSupport = new JButton("<html><center>Support<br/><small>Heals & Buffs</small></center></html>");

        styleMenuButton(btnDebugger);
        styleMenuButton(btnHacker);
        styleMenuButton(btnTester);
        styleMenuButton(btnArchitect);
        styleMenuButton(btnPenTester);
        styleMenuButton(btnSupport);

        btnDebugger.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateHeroSprite("Debugger");
            }
        });
        btnHacker.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateHeroSprite("Hacker");
            }
        });
        btnTester.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateHeroSprite("Tester");
            }
        });
        btnArchitect.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateHeroSprite("Architect");
            }
        });
        btnPenTester.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateHeroSprite("PenTester");
            }
        });
        btnSupport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updateHeroSprite("Support");
            }
        });

        grid.add(btnDebugger);
        grid.add(btnHacker);
        grid.add(btnTester);
        grid.add(btnArchitect);
        grid.add(btnPenTester);
        grid.add(btnSupport);

        dialog.add(grid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(BG_DARK);
        JButton cancel = new JButton("Cancel");
        styleMenuButton(cancel);
        cancel.setPreferredSize(new Dimension(100, 34));
        bottom.add(cancel);
        dialog.add(bottom, BorderLayout.SOUTH);

        btnDebugger.addActionListener(e -> {
            manager.requestStopCurrentGame();
            inputProvider.clearPending();
            manager.uiStartGameWithClass(1);
            updateHeroSprite("Debugger");
            cardLayout.show(rootPanel, "GAME");
            dialog.dispose();
        });
        btnHacker.addActionListener(e -> {
            manager.requestStopCurrentGame();
            inputProvider.clearPending();
            manager.uiStartGameWithClass(2);
            updateHeroSprite("Hacker");
            cardLayout.show(rootPanel, "GAME");
            dialog.dispose();
        });
        btnTester.addActionListener(e -> {
            manager.requestStopCurrentGame();
            inputProvider.clearPending();
            manager.uiStartGameWithClass(3);
            updateHeroSprite("Tester");
            cardLayout.show(rootPanel, "GAME");
            dialog.dispose();
        });

        btnArchitect.addActionListener(e -> {
            manager.requestStopCurrentGame();
            inputProvider.clearPending();
            manager.uiStartGameWithClass(4);
            updateHeroSprite("Architect");
            cardLayout.show(rootPanel, "GAME");
            dialog.dispose();
        });
        btnPenTester.addActionListener(e -> {
            manager.requestStopCurrentGame();
            inputProvider.clearPending();
            manager.uiStartGameWithClass(5);
            updateHeroSprite("PenTester");
            cardLayout.show(rootPanel, "GAME");
            dialog.dispose();
        });
        btnSupport.addActionListener(e -> {
            manager.requestStopCurrentGame();
            inputProvider.clearPending();
            manager.uiStartGameWithClass(6);
            updateHeroSprite("Support");
            cardLayout.show(rootPanel, "GAME");
            dialog.dispose();
        });
        cancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showSaveDialog() {
        if (manager.player == null) {
            JOptionPane.showMessageDialog(frame,
                    "No active game to save.",
                    "Save Game",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("Enter a name for your save slot:"), BorderLayout.NORTH);

        JTextField nameField = new JTextField(30);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        nameField.setText("Save Slot - " + timestamp);
        panel.add(nameField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(frame,
                panel,
                "Save Game to Database",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String slotName = nameField.getText();
            if (slotName == null || slotName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Invalid save name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            manager.uiSaveGameTo(slotName.trim());
        }
    }

    private void showLoadDialogFromMenu() {
        showLoadDialog(true);
    }

    private void showLoadDialogFromGame() {
        showLoadDialog(false);
    }

    /**
     * Updated showLoadDialog to use manager.loadGameFromAsync(...) and display a modal progress dialog
     * until load + game-thread startup complete.
     */
    private void showLoadDialog(boolean fromMainMenu) {
        JDialog loadingDialog = new JDialog(frame, "Loading Saves...", true);
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(frame);
        loadingDialog.add(new JLabel("Fetching save slots...", SwingConstants.CENTER));
        loadingDialog.setUndecorated(true);
        loadingDialog.pack();
        ((JPanel) loadingDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        new Thread(() -> {
            List<String> saves = manager.listSaveFiles();

            SwingUtilities.invokeLater(() -> {
                loadingDialog.dispose();

                if (saves == null || saves.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No save files found in database.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JList<String> list = new JList<>(saves.toArray(new String[0]));
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setSelectedIndex(0);
                JScrollPane scrollPane = new JScrollPane(list);
                scrollPane.setPreferredSize(new Dimension(300, 200));

                int result = JOptionPane.showConfirmDialog(frame,
                        scrollPane,
                        "Load Game",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String filename = list.getSelectedValue();
                    if (filename != null) {
                        if (!fromMainMenu) {
                            boolean ok = showConfirmation("Load Game", "Load game? Current progress will be lost.");
                            if (!ok) {
                                return;
                            }
                        }

                        manager.requestStopCurrentGame();
                        inputProvider.clearPending();
                        consoleArea.setText("");

                        // Show a small modal "Loading save..." while DB+game thread run
                        final JDialog loadProgress = new JDialog(frame, "Loading Save...", true);
                        loadProgress.setUndecorated(true);
                        loadProgress.add(new JLabel("Loading save, please wait...", SwingConstants.CENTER));
                        loadProgress.pack();
                        loadProgress.setLocationRelativeTo(frame);

                        // Start the async load; callbacks run on EDT
                        manager.loadGameFromAsync(filename,
                                () -> {
                                    loadProgress.dispose();
                                    cardLayout.show(rootPanel, "GAME");
                                    refreshStats();
                                },
                                (ex) -> {
                                    loadProgress.dispose();
                                    JOptionPane.showMessageDialog(frame,
                                            "Failed to load save: " + ex.getMessage(),
                                            "Load Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                        );

                        // Show the modal after starting the worker so UI remains blocked until load completes
                        loadProgress.setVisible(true);
                    }
                }
            });
        }, "Load-Saves-Thread").start();

        loadingDialog.setVisible(true);
    }

    private void loadLatestSave() {
        JDialog loadingDialog = new JDialog(frame, "Loading...", true);
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(frame);
        loadingDialog.add(new JLabel("Finding latest save...", SwingConstants.CENTER));
        loadingDialog.setUndecorated(true);
        loadingDialog.pack();
        ((JPanel) loadingDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        new Thread(() -> {
            List<String> saves = manager.listSaveFiles();

            SwingUtilities.invokeLater(() -> {
                loadingDialog.dispose();

                if (saves == null || saves.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No save files found.", "Continue", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String latestSave = saves.get(0);

                manager.requestStopCurrentGame();
                inputProvider.clearPending();
                consoleArea.setText("");

                final JDialog loadProgress = new JDialog(frame, "Loading Save...", true);
                loadProgress.setUndecorated(true);
                loadProgress.add(new JLabel("Loading save, please wait...", SwingConstants.CENTER));
                loadProgress.pack();
                loadProgress.setLocationRelativeTo(frame);

                manager.loadGameFromAsync(latestSave,
                        () -> {
                            loadProgress.dispose();
                            cardLayout.show(rootPanel, "GAME");
                            refreshStats();
                        },
                        (ex) -> {
                            loadProgress.dispose();
                            JOptionPane.showMessageDialog(frame,
                                    "Failed to load save: " + ex.getMessage(),
                                    "Load Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                );

                loadProgress.setVisible(true);
            });
        }, "Load-Latest-Thread").start();

        loadingDialog.setVisible(true);
    }

    private void updateContinueAvailability() {
        if (menuContinueBtn == null) {
            return;
        }
        menuContinueBtn.setEnabled(false);
        menuContinueBtn.setText("Checking for saves...");

        new Thread(() -> {
            List<String> saves = manager.listSaveFiles();
            final boolean hasSaves = (saves != null && !saves.isEmpty());

            SwingUtilities.invokeLater(() -> {
                menuContinueBtn.setEnabled(hasSaves);
                menuContinueBtn.setText(hasSaves ? "Continue Latest Save" : "Continue (No Saves)");
                menuContinueBtn.setToolTipText(hasSaves ? "Load the most recent save game" : "No save games found");
            });
        }, "Check-Saves-Thread").start();
    }

    private String getCurrentCardName() {
        for (Component comp : rootPanel.getComponents()) {
            if (comp.isVisible()) {
                if (comp == loadingPanel) {
                    return "LOADING";
                }
                if (comp == mainMenuPanel) {
                    return "MENU";
                }
                if (comp == gamePanel) {
                    return "GAME";
                }
            }
        }
        return "";
    }

    private void refreshStats() {
        try {
            Hero p = manager.player;
            if (p == null) {
                SwingUtilities.invokeLater(() -> {
                    classLabel.setText("Class: -");
                    hpLabel.setText("HP: -");
                    manaLabel.setText("Mana: -");
                    goldLabel.setText("Gold: -");
                    if (statHpBar instanceof StatBar) {
                        ((StatBar) statHpBar).setValues(0, 100, "HP: -");
                    }
                    if (statManaBar instanceof StatBar) {
                        ((StatBar) statManaBar).setValues(0, 100, "Mana: -");
                    }
                    updateHeroSprite(null);
                    statusLabel.setText("Time: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + " — Press Esc to focus input");

                    if ("MENU".equals(getCurrentCardName())) {
                        updateContinueAvailability();
                    }
                });
                return;
            }

            final String className = p.getClassName();
            final String hp = "HP: " + p.hp + " / " + p.maxHP;
            final String mana = "Mana: " + p.mana + " / " + p.maxMana;
            final String gold = "Gold: " + p.getGold();

            SwingUtilities.invokeLater(() -> {
                if (currentSpriteClass == null || !currentSpriteClass.equals(className)) {
                    updateHeroSprite(className);
                }

                if (characterPortrait != null) {
                    if (manager.isInCombat()) {
                        characterPortrait.setState(CharacterSprite.State.COMBAT);
                    } else if (manager.isMoving()) {
                        characterPortrait.setState(CharacterSprite.State.MOVING);
                    } else {
                        characterPortrait.setState(CharacterSprite.State.IDLE);
                    }
                }

                classLabel.setText("Class: " + className);
                hpLabel.setText(stripAnsi(hp));
                manaLabel.setText(stripAnsi(mana));
                goldLabel.setText(stripAnsi(gold));
                if (statHpBar instanceof StatBar) {
                    ((StatBar) statHpBar).setValues(p.hp, p.maxHP, stripAnsi(hp));
                }
                if (statManaBar instanceof StatBar) {
                    ((StatBar) statManaBar).setValues(p.mana, p.maxMana, stripAnsi(mana));
                }
                statusLabel.setText("Time: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + " — Press Esc to focus input");
            });
        } catch (Throwable ignored) {
        }
    }

    private void updateHeroSprite(String className) {
        this.currentSpriteClass = className;
        if (characterPortrait != null) {
            characterPortrait.setCharacterClass(className);
        }
    }

    private void updateConsoleFontSize() {
        consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, consoleFontSize));
        StyledDocument doc = consoleArea.getStyledDocument();
        Style defaultStyle = doc.getStyle("default");
        if (defaultStyle != null) {
            StyleConstants.setFontSize(defaultStyle, consoleFontSize);
        }
    }

    private static String stripAnsi(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\u001B\\[[0-9;]*m", "");
    }

    private void wireMenuKeyboardNavigation(JButton[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        buttons[(index + 1) % buttons.length].requestFocus();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        buttons[(index - 1 + buttons.length) % buttons.length].requestFocus();
                    }
                }
            });
        }
    }

    private void wireGlobalShortcuts() {
        JRootPane root = frame.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "toggleTheme");
        am.put("toggleTheme", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                darkTheme = !darkTheme;
                if (themeBtn != null) {
                    themeBtn.setText(darkTheme ? "Theme: Dark" : "Theme: Light");
                }
                applyTheme();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK), "fontInc");
        am.put("fontInc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleFontSize = Math.min(24, consoleFontSize + 1);
                updateConsoleFontSize();
                inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, consoleFontSize)));
                statusLabel.setText("Font: " + consoleFontSize + "pt");
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "fontDec");
        am.put("fontDec", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleFontSize = Math.max(10, consoleFontSize - 1);
                updateConsoleFontSize();
                inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(11, consoleFontSize)));
                statusLabel.setText("Font: " + consoleFontSize + "pt");
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "focusInput");
        am.put("focusInput", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inputField != null) {
                    inputField.requestFocusInWindow();
                }
                statusLabel.setText("Focus: input field");
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK), "clearConsole");
        am.put("clearConsole", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleArea.setText("");
                statusLabel.setText("Console cleared");
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK), "decSpriteSize");
        am.put("decSpriteSize", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spriteMaxSize = Math.max(64, spriteMaxSize - 8);
                statusLabel.setText("Sprite size: " + spriteMaxSize + "px");
                if (currentSpriteClass != null) {
                    updateHeroSprite(currentSpriteClass);
                }
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK), "incSpriteSize");
        am.put("incSpriteSize", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spriteMaxSize = Math.min(192, spriteMaxSize + 8);
                statusLabel.setText("Sprite size: " + spriteMaxSize + "px");
                if (currentSpriteClass != null) {
                    updateHeroSprite(currentSpriteClass);
                }
            }
        });
    }

    private void submitFromInputField() {
        try {
            if (inputField == null) {
                return;
            }
            String text = inputField.getText();
            if (text == null) {
                text = "";
            }
            inputHistory.add(text);
            historyIndex = -1;
            inputProvider.submitTrimmed(text);
            inputField.setText("");
            statusLabel.setText("Sent: " + (text.length() > 24 ? text.substring(0, 24) + "…" : text));
        } catch (Throwable t) {
            statusLabel.setText("Error sending input: " + t.getMessage());
        }
    }

    private void startLoadingAnimation() {
        if (loadingDotsTimer != null && loadingDotsTimer.isRunning()) {
            return;
        }
        loadingDotsTimer = new javax.swing.Timer(300, evt -> {
            if (loadingTextLabel == null) {
                return;
            }
            String base = "Loading";
            String text = loadingTextLabel.getText();
            int dots = (text.endsWith("...") ? 0 : (text.endsWith("..") ? 3 : (text.endsWith(".") ? 2 : 1)));
            StringBuilder sb = new StringBuilder(base);
            for (int i = 0; i < dots; i++) {
                sb.append('.');
            }
            loadingTextLabel.setText(sb.toString());
        });
        loadingDotsTimer.setRepeats(true);
        loadingDotsTimer.start();
    }

    private void stopLoadingAnimation() {
        if (loadingDotsTimer != null) {
            loadingDotsTimer.stop();
            loadingDotsTimer = null;
        }
    }

    private boolean showConfirmation(String title, String message) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG_DARK);
        JLabel msg = new JLabel("<html><div style='width:380px;'>" + message + "</div></html>");
        msg.setForeground(ACCENT_LIGHT);
        msg.setBorder(new EmptyBorder(8, 8, 8, 8));
        p.add(msg, BorderLayout.CENTER);

        String[] options = {"Yes", "No"};
        int res = JOptionPane.showOptionDialog(frame, p, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);
        return res == JOptionPane.YES_OPTION;
    }

    private void testAndSetDBStatus() {
        new Thread(() -> {
            boolean connected = GameDatabase.testConnection();
            SwingUtilities.invokeLater(() -> {
                if (dbStatusLabel == null) {
                    return;
                }
                if (connected) {
                    dbStatusLabel.setText("DB: Connected");
                    dbStatusLabel.setForeground(java.awt.Color.GREEN);
                    dbStatusLabel.setToolTipText("Database connection is healthy.");
                } else {
                    dbStatusLabel.setText("DB: OFFLINE");
                    dbStatusLabel.setForeground(java.awt.Color.RED);
                    dbStatusLabel.setToolTipText("CRITICAL: Database connection failed. Check XAMPP.");

                    JOptionPane.showMessageDialog(frame,
                            "CRITICAL ERROR: Could not connect to the database.\n\n"
                                    + "1. Is XAMPP running (Apache and MySQL)?\n"
                                    + "2. Is the database 'programmed_escapist' created?\n"
                                    + "3. Did you fix the password bug in GameDatabase.java?",
                            "Database Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }, "DB-Test-Thread").start();
    }

    public static void launch() {
        SwingUtilities.invokeLater(GameUI::new);
    }

    /**
     * Switches the view to the main menu. Can be called from any thread.
     */
    public void showMainMenu() {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(rootPanel, "MENU");
        });
    }
    private void applyTheme() {
        java.awt.Color bg = darkTheme ? BG_DARK : new java.awt.Color(245, 245, 252);
        java.awt.Color leftBg = darkTheme ? GAME_LEFT_BG : new java.awt.Color(232, 236, 245);
        java.awt.Color consoleBg = darkTheme ? CONSOLE_BG : java.awt.Color.WHITE;
        java.awt.Color consoleText = darkTheme ? CONSOLE_TEXT : java.awt.Color.BLACK;
        java.awt.Color accent = darkTheme ? ACCENT_LIGHT : java.awt.Color.DARK_GRAY;
        if (gameContainerPanel != null) {
            gameContainerPanel.setBackground(bg);
        }
        if (headerPanel != null) {
            headerPanel.setBackground(bg);
        }
        if (headerTitle != null) {
            headerTitle.setForeground(accent);
        }
        if (headerTitle != null && headerTitle.getIcon() instanceof CircleIcon) {
            ((CircleIcon) headerTitle.getIcon()).setColor(accent);
            headerTitle.repaint();
        }
        if (themeBtn != null) {
            themeBtn.setForeground(darkTheme ? FOOTER : java.awt.Color.GRAY);
            themeBtn.setText(darkTheme ? "Theme: Dark" : "Theme: Light");
        }
        if (fontDecBtn != null) {
            fontDecBtn.setForeground(darkTheme ? FOOTER : java.awt.Color.GRAY);
        }
        if (fontIncBtn != null) {
            fontIncBtn.setForeground(darkTheme ? FOOTER : java.awt.Color.GRAY);
        }
        if (dbStatusLabel != null) {
            if (dbStatusLabel.getForeground() == FOOTER || dbStatusLabel.getForeground() == java.awt.Color.GRAY) {
                dbStatusLabel.setForeground(darkTheme ? FOOTER : java.awt.Color.GRAY);
            }
        }

        if (gameLeftPanel != null) {
            gameLeftPanel.setBackground(leftBg);
        }
        if (gameRightPanel != null) {
            gameRightPanel.setBackground(bg);
        }
        if (statsPanel != null) {
            statsPanel.setBackground(leftBg);
        }
        if (controlsPanel != null) {
            controlsPanel.setBackground(leftBg);
        }
        if (consoleToolbarPanel != null) {
            consoleToolbarPanel.setBackground(bg);
        }
        if (bottomStackPanel != null) {
            bottomStackPanel.setBackground(bg);
        }
        if (inputPanel != null) {
            inputPanel.setBackground(bg);
        }
        if (consoleArea != null) {
            consoleArea.setBackground(consoleBg);
            consoleArea.setForeground(consoleText);
        }
        if (statusLabel != null) {
            statusLabel.setForeground(accent);
        }
        javax.swing.SwingUtilities.updateComponentTreeUI(frame);
    }

    private class RoundedPanel extends JPanel {

        private int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(new java.awt.Color(0, 0, 0, darkTheme ? 60 : 40));
            g2.fillRoundRect(3, 4, w - 3, h - 3, radius, radius);

            java.awt.Color bg = getBackground() != null ? getBackground() : new java.awt.Color(0, 0, 0, 0);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 3, h - 3, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class StatBar extends JComponent {

        private int value = 0;
        private int max = 100;
        private String caption = "";
        private java.awt.Color fillColor;
        private final int radius = 12;

        StatBar(java.awt.Color fillColor) {
            this.fillColor = fillColor;
            setPreferredSize(new Dimension(200, 24));
            setMinimumSize(new Dimension(100, 24));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void setValues(int value, int max, String caption) {
            this.value = Math.max(0, value);
            this.max = Math.max(1, max);
            this.caption = caption;
            repaint();
        }

        void updateValue(int value, int max) {
            setValues(value, max, this.caption);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            java.awt.Color track = darkTheme ? new java.awt.Color(40, 50, 65) : new java.awt.Color(230, 235, 245);
            java.awt.Color border = darkTheme ? new java.awt.Color(120, 140, 180) : new java.awt.Color(170, 190, 220);
            g2.setColor(track);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            double pct = Math.min(1.0, (double) value / (double) max);
            int fillW = (int) Math.round(w * pct);
            java.awt.Color fill = fillColor != null ? fillColor : (darkTheme ? new java.awt.Color(80, 150, 220) : new java.awt.Color(90, 140, 220));
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, fillW, h, radius, radius);

            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

            g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
            g2.setColor(darkTheme ? java.awt.Color.WHITE : java.awt.Color.DARK_GRAY);
            FontMetrics fm = g2.getFontMetrics();
            int tx = 8;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            String displayCaption = caption != null ? stripAnsi(caption) : "";
            g2.drawString(displayCaption, tx, ty);
            g2.dispose();
        }
    }

    private class BadgeLabel extends JLabel {

        BadgeLabel(String text) {
            super(text);
            setOpaque(false);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            setFont(getFont().deriveFont(Font.BOLD, 12f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Color bg = darkTheme ? new java.awt.Color(55, 70, 100) : new java.awt.Color(220, 230, 250);
            java.awt.Color border = darkTheme ? new java.awt.Color(120, 140, 180) : new java.awt.Color(180, 200, 230);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class CircleIcon implements Icon {

        private int size = 12;
        private java.awt.Color color;

        CircleIcon(java.awt.Color color) {
            this.color = color;
        }

        void setColor(java.awt.Color c) {
            this.color = c;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }
    }

    private class GradientPanel extends JPanel {

        private java.awt.Color startColor;
        private java.awt.Color endColor;

        GradientPanel(java.awt.Color startColor, java.awt.Color endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, h, endColor);
            g2.setPaint(gradient);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
