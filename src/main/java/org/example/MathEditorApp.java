package org.example;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MathEditorApp {
    private static float currentFontSize = 20f;
    private static final List<HistoryEntry> historyEntries = new ArrayList<>();
    private static boolean isDarkMode = false;

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    // Colori moderni
    public static class Theme {
        // Light Mode
        static final Color LIGHT_BG = new Color(248, 249, 250);
        static final Color LIGHT_CARD = Color.WHITE;
        static final Color LIGHT_TEXT = new Color(33, 37, 41);
        static final Color LIGHT_SECONDARY = new Color(108, 117, 125);
        static final Color LIGHT_BORDER = new Color(222, 226, 230);
        static final Color LIGHT_INPUT_BG = Color.WHITE;

        // Dark Mode
        static final Color DARK_BG = new Color(26, 28, 32);
        static final Color DARK_CARD = new Color(40, 44, 52);
        static final Color DARK_TEXT = new Color(229, 231, 235);
        static final Color DARK_SECONDARY = new Color(156, 163, 175);
        static final Color DARK_BORDER = new Color(55, 60, 70);
        static final Color DARK_INPUT_BG = new Color(32, 36, 42);

        // Accent
        static final Color ACCENT = new Color(99, 102, 241);
        static final Color ACCENT_HOVER = new Color(79, 82, 221);
    }

    public static Color getThemeColor(String type) {
        if (isDarkMode) {
            switch (type) {
                case "bg": return Theme.DARK_BG;
                case "card": return Theme.DARK_CARD;
                case "text": return Theme.DARK_TEXT;
                case "border": return Theme.DARK_BORDER;
                default: return Theme.DARK_TEXT;
            }
        } else {
            switch (type) {
                case "bg": return Theme.LIGHT_BG;
                case "card": return Theme.LIGHT_CARD;
                case "text": return Theme.LIGHT_TEXT;
                case "border": return Theme.LIGHT_BORDER;
                default: return Theme.LIGHT_TEXT;
            }
        }
    }

    private static class HistoryEntry {
        String latex;
        String rawInput;
        String info;

        HistoryEntry(String latex, String rawInput, String info) {
            this.latex = latex;
            this.rawInput = rawInput;
            this.info = info;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(MathEditorApp::createAndShowGUI);
    }

    static void createAndShowGUI() {
        JFrame frame = new JFrame("VecTor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 800);
        frame.setLayout(new BorderLayout());

        // 1. PRIMA creiamo il pannello storia e lo scroll (così abbiamo i riferimenti)
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(historyPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        // 2. POI creiamo la TopBar passando i riferimenti corretti
        // Nota: Passiamo anche historyPanel e scrollPane
        JPanel topBar = createTopBar(frame, historyPanel, scrollPane);

        // --- Pannello Input Moderno ---
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        inputField.putClientProperty("JTextField.placeholderText", "Es: A = (assegnazione) oppure A * x");
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LIGHT_BORDER, 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JButton sendButton = createModernButton("Invia");
        sendButton.setPreferredSize(new Dimension(100, 45));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // --- LOGICA DI INVIO ---
        ActionListener action = e -> {
            String rawInput = inputField.getText().trim();
            if (rawInput.isEmpty()) return;

            String assignmentResult = AssignmentHandler.tryHandleAssignment(rawInput, frame);
            if (assignmentResult != null) {
                String varName = rawInput.split("=")[0].trim();
                addFormulaToHistory(assignmentResult, rawInput, "Definita: " + varName,
                        historyPanel, scrollPane, frame);
                inputField.setText("");
            } else {
                try {
                    String latex = SmartMathParser.parseToColoredLatex(rawInput);
                    addFormulaToHistory(latex, rawInput, "Input: " + rawInput,
                            historyPanel, scrollPane, frame);
                    inputField.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        inputField.addActionListener(action);
        sendButton.addActionListener(action);

        // 3. Setup controlli zoom (che ora richiedono la label che è dentro topBar...
        // ma per semplicità passiamo null o gestiamo l'aggiornamento UI dentro changeZoom in modo diverso)
        // Per ora, chiamiamo setupZoomControls dopo aver aggiunto tutto.

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Recuperiamo la label dello zoom dalla topBar per passarla ai controlli da tastiera
        JPanel rightPanel = (JPanel) topBar.getComponent(1);
        JLabel zoomLabel = (JLabel) rightPanel.getComponent(1);

        setupZoomControls(frame, scrollPane, historyPanel);

        applyTheme(frame, topBar, historyPanel, contentWrapper, scrollPane, inputPanel, inputField, sendButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        inputField.requestFocus();
    }

    private static JPanel createTopBar(JFrame frame, JPanel historyPanel, JScrollPane scrollPane) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("VecTor");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton zoomOutBtn = createIconButton("-");
        JButton zoomInBtn = createIconButton("+");
        JLabel zoomLabel = new JLabel(String.format("%.0f%%", (currentFontSize / 20f) * 100));
        zoomLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Listener corretti: usano i riferimenti diretti e aggiornano la label
        zoomOutBtn.addActionListener(e -> {
            changeZoom(-2f, historyPanel, scrollPane, frame);
            zoomLabel.setText(String.format("%.0f%%", (currentFontSize / 20f) * 100));
        });

        zoomInBtn.addActionListener(e -> {
            changeZoom(2f, historyPanel, scrollPane, frame);
            zoomLabel.setText(String.format("%.0f%%", (currentFontSize / 20f) * 100));
        });

        JButton themeToggle = createIconButton("☀");
        themeToggle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        themeToggle.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            themeToggle.setText("☀");
            applyThemeToAllComponents(frame);
        });

        rightPanel.add(zoomOutBtn);
        rightPanel.add(zoomLabel);
        rightPanel.add(zoomInBtn);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(themeToggle);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        // Esempio da inserire in MathEditorApp.java dentro createTopBar
        JButton vectorBtn = createIconButton("2D");
        vectorBtn.setToolTipText("Visualizzatore Vettori 2D");
// AGGIUNTO .setVisible(true)
        vectorBtn.addActionListener(e -> new VectorVisualizer2D(frame).setVisible(true));
        rightPanel.add(vectorBtn);
        // Esempio da inserire in MathEditorApp.java dentro createTopBar
        JButton vector3Btn = createIconButton("3D");
        vector3Btn.setToolTipText("Visualizzatore Vettori 3D");
        vector3Btn.addActionListener(e -> new VectorVisualizer3D(frame).setVisible(true));
        rightPanel.add(vector3Btn);

        return topBar;
    }

    private static JButton createIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);

        // --- AGGIUNTA FONDAMENTALE ---
        // Rimuove i margini interni (padding) affinché il testo entri nel box 40x40
        btn.setMargin(new Insets(0, 0, 0, 0));
        // -----------------------------

        btn.setPreferredSize(new Dimension(40, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });

        return btn;
    }

    private static JButton createModernButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Theme.ACCENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(Theme.ACCENT_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Theme.ACCENT);
            }
        });

        return btn;
    }

    private static void applyTheme(JFrame frame, JPanel topBar, JPanel historyPanel,
                                   JPanel contentWrapper, JScrollPane scrollPane,
                                   JPanel inputPanel, JTextField inputField, JButton sendButton) {
        Color bg = isDarkMode ? Theme.DARK_BG : Theme.LIGHT_BG;
        Color cardBg = isDarkMode ? Theme.DARK_CARD : Theme.LIGHT_CARD;
        Color textColor = isDarkMode ? Theme.DARK_TEXT : Theme.LIGHT_TEXT;
        Color inputBg = isDarkMode ? Theme.DARK_INPUT_BG : Theme.LIGHT_INPUT_BG;
        Color border = isDarkMode ? Theme.DARK_BORDER : Theme.LIGHT_BORDER;

        frame.getContentPane().setBackground(bg);
        topBar.setBackground(cardBg);
        historyPanel.setBackground(bg);
        contentWrapper.setBackground(bg);
        scrollPane.getViewport().setBackground(bg);
        inputPanel.setBackground(bg);
        inputField.setBackground(inputBg);
        inputField.setForeground(textColor);
        inputField.setCaretColor(textColor);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));

        for (Component comp : topBar.getComponents()) {
            if (comp instanceof JLabel) {
                comp.setForeground(textColor);
            } else if (comp instanceof JPanel) {
                comp.setBackground(cardBg);
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel) {
                        subComp.setForeground(textColor);
                    } else if (subComp instanceof JButton && !((JButton) subComp).getText().equals("Invia")) {
                        subComp.setForeground(textColor);
                    }
                }
            }
        }
    }

    private static void applyThemeToAllComponents(JFrame frame) {
        Component[] components = frame.getContentPane().getComponents();
        JPanel topBar = (JPanel) components[0];
        JScrollPane scrollPane = (JScrollPane) components[1];
        JPanel inputPanel = (JPanel) components[2];

        JPanel contentWrapper = (JPanel) scrollPane.getViewport().getView();
        JPanel historyPanel = (JPanel) contentWrapper.getComponent(0);
        JTextField inputField = (JTextField) inputPanel.getComponent(0);
        JButton sendButton = (JButton) inputPanel.getComponent(1);

        applyTheme(frame, topBar, historyPanel, contentWrapper, scrollPane, inputPanel, inputField, sendButton);
        renderHistoryPanel(historyPanel, scrollPane, frame);
    }

    private static void addFormulaToHistory(String latex, String rawInput, String info,
                                            JPanel historyPanel, JScrollPane scrollPane, JFrame parentFrame) {
        historyEntries.add(new HistoryEntry(latex, rawInput, info));
        renderHistoryPanel(historyPanel, scrollPane, parentFrame);
        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum())
        );
    }

    private static void renderHistoryPanel(JPanel historyPanel, JScrollPane scrollPane, JFrame parentFrame) {
        historyPanel.removeAll();

        Color cardBg = isDarkMode ? Theme.DARK_CARD : Theme.LIGHT_CARD;
        Color textColor = isDarkMode ? Theme.DARK_TEXT : Theme.LIGHT_TEXT;
        Color secondaryColor = isDarkMode ? Theme.DARK_SECONDARY : Theme.LIGHT_SECONDARY;
        Color border = isDarkMode ? Theme.DARK_BORDER : Theme.LIGHT_BORDER;

        for (HistoryEntry entry : historyEntries) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBackground(cardBg);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, border),
                    new EmptyBorder(20, 20, 20, 20)
            ));
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            itemPanel.addMouseListener(new MouseAdapter() {
                Color originalBg = cardBg;

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        new FormulaViewerDialog(parentFrame, entry.rawInput).setVisible(true);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    itemPanel.setBackground(isDarkMode ?
                            new Color(50, 54, 62) : new Color(248, 249, 250));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    itemPanel.setBackground(originalBg);
                }
            });

            itemPanel.setToolTipText("Doppio click per espandere la formula");
            itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            try {
                TeXFormula formula = new TeXFormula(entry.latex);
                TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, currentFontSize);

                icon.setForeground(textColor);

                JLabel label = new JLabel(icon);
                itemPanel.add(label, BorderLayout.CENTER);

                JLabel infoLabel = new JLabel(entry.info);
                infoLabel.setForeground(secondaryColor);
                infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
                itemPanel.add(infoLabel, BorderLayout.SOUTH);
            } catch (Exception ex) {
                JLabel errLabel = new JLabel("<html><span style='color:#ef4444;'>Errore: " +
                        ex.getMessage() + "</span></html>");
                itemPanel.add(errLabel, BorderLayout.CENTER);
            }

            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, itemPanel.getPreferredSize().height));
            historyPanel.add(itemPanel);
        }

        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private static void setupZoomControls(JFrame frame, JScrollPane scrollPane, JPanel historyPanel) {
        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                e.consume();
                if (e.getWheelRotation() < 0) {
                    changeZoom(2f, historyPanel, scrollPane, frame);
                } else {
                    changeZoom(-2f, historyPanel, scrollPane, frame);
                }
            }
        });

        JPanel rootPanel = (JPanel) frame.getContentPane();
        InputMap inputMap = rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPanel.getActionMap();

        KeyStroke ctrlPlus = KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlPlusStd = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlEquals = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        inputMap.put(ctrlPlus, "zoomIn");
        inputMap.put(ctrlPlusStd, "zoomIn");
        inputMap.put(ctrlEquals, "zoomIn");

        KeyStroke ctrlMinus = KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlMinusStd = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK);

        inputMap.put(ctrlMinus, "zoomOut");
        inputMap.put(ctrlMinusStd, "zoomOut");

        actionMap.put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeZoom(2f, historyPanel, scrollPane, frame);
            }
        });

        actionMap.put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeZoom(-2f, historyPanel, scrollPane, frame);
            }
        });
    }

    private static void changeZoom(float delta, JPanel historyPanel, JScrollPane scrollPane, JFrame frame) {
        float newSize = currentFontSize + delta;
        if (newSize >= 10f && newSize <= 100f) {
            currentFontSize = newSize;
            renderHistoryPanel(historyPanel, scrollPane, frame);

            // Aggiorna label zoom nella topbar
            JPanel topBar = (JPanel) frame.getContentPane().getComponent(0);
            JPanel rightPanel = (JPanel) topBar.getComponent(1);
            JLabel zoomLabel = (JLabel) rightPanel.getComponent(1);
            zoomLabel.setText(String.format("%.0f%%", (currentFontSize / 20f) * 100));
        }
    }
}