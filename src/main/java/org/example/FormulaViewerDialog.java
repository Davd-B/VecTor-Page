package org.example;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormulaViewerDialog extends JDialog {

    private final String rawInput;
    private final JSplitPane mainSplitPane;
    private JPanel resultPanelContainer;
    private JPanel resultCardPanel;

    // Data for 2D Visualization
    private VisualizationData detectedVisData = null;

    public FormulaViewerDialog(JFrame owner, String rawInput) {
        super(owner, "Formula Viewer", true);
        this.rawInput = rawInput;

        // --- 1. Intelligent Input Analysis ---
        this.detectedVisData = StructureAnalyzer.analyze(rawInput);

        setSize(950, 750);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 0));

        // --- Theme Colors ---
        Color bgColor = MathEditorApp.getThemeColor("bg");
        Color cardColor = MathEditorApp.getThemeColor("card");
        Color textColor = MathEditorApp.getThemeColor("text");

        getContentPane().setBackground(bgColor);

        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBackground(cardColor);
        headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel titleLabel = new JLabel("Visualizzatore Formula");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(textColor);

        JLabel subtitleLabel = new JLabel("Analisi simbolica, sostituzione variabili e calcolo");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 180));

        JPanel titleContainer = new JPanel(new BorderLayout(0, 5));
        titleContainer.setBackground(cardColor);
        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.CENTER);

        headerPanel.add(titleContainer, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- Central Area with SplitPane ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new EmptyBorder(15, 15, 5, 15));

        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setBorder(null);
        mainSplitPane.setBackground(bgColor);

        // 1. Top Panel: Symbolic Formula
        JPanel topPanel = createStyledFormulaCard("Formula Simbolica", rawInput, false);

        // 2. Middle Panel: Value Substitution
        JPanel middlePanel = createStyledFormulaCard("Sostituzione Valori", rawInput, true);

        JSplitPane contentSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        contentSplit.setResizeWeight(0.5);
        contentSplit.setBorder(null);
        contentSplit.setBackground(bgColor);
        contentSplit.setTopComponent(topPanel);
        contentSplit.setBottomComponent(middlePanel);

        mainSplitPane.setTopComponent(contentSplit);

        // 3. Result Panel
        resultPanelContainer = new JPanel(new BorderLayout());
        resultPanelContainer.setBackground(bgColor);
        resultPanelContainer.setMinimumSize(new Dimension(0, 0));

        resultCardPanel = new JPanel(new BorderLayout());
        styleCardPanel(resultCardPanel, "Risultato Calcolo");
        resultCardPanel.setVisible(false);

        resultPanelContainer.add(resultCardPanel, BorderLayout.CENTER);
        mainSplitPane.setBottomComponent(resultPanelContainer);

        contentPanel.add(mainSplitPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // --- Bottom Panel (Buttons) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setBackground(cardColor);
        bottomPanel.setBorder(new EmptyBorder(15, 25, 20, 25));

        // -- SMART BUTTON: Only appears if detectedVisData is valid --
        if (detectedVisData != null && detectedVisData.isValid) {
            JButton btnVisualizer = createStyledButton("Visualizza 2D", false);

            btnVisualizer.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            Color visualizerColor = new Color(99, 102, 241);
            Color visualizerHover = new Color(79, 82, 221);

            btnVisualizer.setForeground(Color.WHITE);
            btnVisualizer.setBackground(visualizerColor);

            btnVisualizer.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btnVisualizer.setBackground(visualizerHover);
                }
                public void mouseExited(MouseEvent e) {
                    btnVisualizer.setBackground(visualizerColor);
                }
            });

            btnVisualizer.addActionListener(this::onOpenVisualizer);
            bottomPanel.add(btnVisualizer);
        }

        JButton btnClose = createStyledButton("Chiudi", false);
        btnClose.addActionListener(e -> dispose());

        JButton btnCalculate = createStyledButton("Calcola Risultato", true);
        btnCalculate.addActionListener(this::onCalculate);

        bottomPanel.add(btnClose);
        bottomPanel.add(btnCalculate);
        add(bottomPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> mainSplitPane.setDividerLocation(1.0));
    }

    private void onOpenVisualizer(ActionEvent e) {
        if (detectedVisData == null) return;

        SwingUtilities.invokeLater(() -> {
            VectorVisualizer2D viewer = new VectorVisualizer2D(this);
            // Apply the transformation detected by StructureAnalyzer
            viewer.setTransformationData(
                    detectedVisData.m00, detectedVisData.m10,
                    detectedVisData.m01, detectedVisData.m11,
                    detectedVisData.vx, detectedVisData.vy
            );
            viewer.setVisible(true);
        });
    }

    private void onCalculate(ActionEvent e) {
        BorderLayout layout = (BorderLayout) resultCardPanel.getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        Component southComp = layout.getLayoutComponent(BorderLayout.SOUTH);
        if (centerComp != null) resultCardPanel.remove(centerComp);
        if (southComp != null) resultCardPanel.remove(southComp);

        resultCardPanel.setVisible(true);

        Color textColor = MathEditorApp.getThemeColor("text");
        Color cardColor = MathEditorApp.getThemeColor("card");

        try {
            MathEvaluator evaluator = new MathEvaluator(rawInput);
            String[][] resultMatrix = evaluator.evaluate();
            String resultLatex = MatrixUtils.toLatex(resultMatrix);

            TeXFormula formula = new TeXFormula(resultLatex);
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 30f);
            icon.setForeground(textColor);

            AutoResizingFormulaPanel resultView = new AutoResizingFormulaPanel(icon);
            resultView.setBackground(cardColor);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(cardColor);
            wrapper.add(resultView, BorderLayout.CENTER);

            resultCardPanel.add(wrapper, BorderLayout.CENTER);

            JLabel infoLabel = new JLabel("Dimensione matrice: " + resultMatrix.length + "x" + resultMatrix[0].length);
            infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            infoLabel.setForeground(textColor);
            infoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            infoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            resultCardPanel.add(infoLabel, BorderLayout.SOUTH);

        } catch (Exception ex) {
            JLabel errorLabel = new JLabel("<html><center><b>Errore durante il calcolo:</b><br>" + ex.getMessage() + "</center></html>");
            errorLabel.setForeground(new Color(220, 50, 50));
            errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            resultCardPanel.add(errorLabel, BorderLayout.CENTER);
        }

        resultCardPanel.revalidate();
        resultCardPanel.repaint();
        mainSplitPane.setDividerLocation(0.60);
    }

    private JPanel createStyledFormulaCard(String title, String rawInput, boolean isExpanded) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        styleCardPanel(cardPanel, title);

        Color textColor = MathEditorApp.getThemeColor("text");
        Color cardColor = MathEditorApp.getThemeColor("card");

        try {
            String latex;
            if (isExpanded) {
                latex = SmartMathParser.parseToExpandedLatex(rawInput);
            } else {
                latex = SmartMathParser.parseToColoredLatex(rawInput);
            }

            TeXFormula formula = new TeXFormula(latex);
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 30f);
            icon.setForeground(textColor);

            AutoResizingFormulaPanel formulaPanel = new AutoResizingFormulaPanel(icon);
            formulaPanel.setBackground(cardColor);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(cardColor);
            wrapper.add(formulaPanel, BorderLayout.CENTER);

            cardPanel.add(wrapper, BorderLayout.CENTER);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("<html><center>Errore parsing:<br>" + e.getMessage() + "</center></html>");
            errorLabel.setForeground(Color.RED);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cardPanel.add(errorLabel, BorderLayout.CENTER);
        }

        return cardPanel;
    }

    private void styleCardPanel(JPanel panel, String titleText) {
        Color cardColor = MathEditorApp.getThemeColor("card");
        Color borderColor = MathEditorApp.getThemeColor("border");
        Color textColor = MathEditorApp.getThemeColor("text");

        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 0, 5, 0),
                BorderFactory.createCompoundBorder(
                        new LineBorder(borderColor, 1, true),
                        new EmptyBorder(0, 0, 15, 0)
                )
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(cardColor);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(textColor);

        header.add(title, BorderLayout.WEST);

        JSeparator sep = new JSeparator();
        sep.setForeground(borderColor);
        sep.setBackground(borderColor);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(cardColor);
        topContainer.add(header, BorderLayout.NORTH);
        topContainer.add(sep, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        if (isPrimary) {
            btn.setForeground(Color.WHITE);
            btn.setBackground(MathEditorApp.Theme.ACCENT);
        } else {
            if (MathEditorApp.isDarkMode()) {
                btn.setForeground(MathEditorApp.Theme.DARK_TEXT);
                btn.setBackground(MathEditorApp.Theme.DARK_SECONDARY);
            } else {
                btn.setForeground(Color.WHITE);
                btn.setBackground(MathEditorApp.Theme.LIGHT_SECONDARY);
            }
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isPrimary) {
                    btn.setBackground(MathEditorApp.Theme.ACCENT_HOVER);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (isPrimary) {
                    btn.setBackground(MathEditorApp.Theme.ACCENT);
                }
            }
        });

        return btn;
    }

    private static class StructureAnalyzer {

        public static VisualizationData analyze(String rawInput) {
            System.out.println("\n--- [DEBUG] ANALISI SEMPLIFICATA ---");
            VisualizationData data = new VisualizationData();

            if (rawInput == null) return data;

            // 1. Pulizia base
            String text = rawInput.trim();
            System.out.println("[DEBUG] Input: '" + text + "'");

            String varLeft = null;
            String varRight = null;

            // 2. STRATEGIA A: Cerca separatori espliciti (Spazi o Asterischi)
            // Divide se trova uno o più spazi OPPURE uno o più asterischi
            String[] parts = text.split("[\\s*]+");

            if (parts.length == 2) {
                System.out.println("[DEBUG] Trovati separatori. Parti: " + parts[0] + ", " + parts[1]);
                varLeft = parts[0];
                varRight = parts[1];
            }
            // 3. STRATEGIA B: Nessun separatore, controlla caso "Av" (2 lettere attaccate)
            else if (parts.length == 1) {
                String singleChunk = parts[0];
                // Se è lungo esattamente 2 caratteri E sono entrambi lettere (non numeri)
                if (singleChunk.length() == 2
                        && Character.isLetter(singleChunk.charAt(0))
                        && Character.isLetter(singleChunk.charAt(1))) {

                    varLeft = singleChunk.substring(0, 1);
                    varRight = singleChunk.substring(1, 2);
                    System.out.println("[DEBUG] Rilevato pattern 2 caratteri uniti: " + varLeft + " e " + varRight);
                }
            }

            // 4. Se abbiamo trovato due potenziali variabili, verifichiamo i dati
            if (varLeft != null && varRight != null) {
                MatrixData leftData = VariableManager.get(varLeft);
                MatrixData rightData = VariableManager.get(varRight);

                if (leftData != null && rightData != null) {
                    double[][] leftM = leftData.getDoubleValues();
                    double[][] rightM = rightData.getDoubleValues();

                    if (leftM != null && rightM != null) {
                        // Check dimensioni: A(2x2) * v(2x1)
                        boolean is2x2 = (leftM.length == 2 && leftM[0].length == 2);
                        boolean is2x1 = (rightM.length == 2 && rightM[0].length == 1);

                        if (is2x2 && is2x1) {
                            System.out.println("[DEBUG] SUCCESS: Matrice 2x2 e Vettore 2x1 validi.");
                            data.m00 = leftM[0][0]; data.m01 = leftM[0][1];
                            data.m10 = leftM[1][0]; data.m11 = leftM[1][1];
                            data.vx = rightM[0][0]; data.vy = rightM[1][0];
                            data.hasMatrix = true; data.hasVector = true; data.isValid = true;
                            return data; // Ritorna subito se successo
                        } else {
                            System.out.println("[DEBUG] Dimensioni non corrette per visualizzazione 2D.");
                        }
                    }
                } else {
                    System.out.println("[DEBUG] Una delle variabili non esiste in memoria.");
                }
            } else {
                System.out.println("[DEBUG] Non sono state identificate due variabili distinte.");
            }

            // --- 5. FALLBACK (Se l'analisi simbolica fallisce, prova a valutare il risultato numerico) ---
            System.out.println("[DEBUG] Avvio Fallback numerico...");
            try {
                MathEvaluator evaluator = new MathEvaluator(rawInput);
                String[][] symbolicResult = evaluator.evaluate();

                if (symbolicResult != null) {
                    int rows = symbolicResult.length;
                    int cols = (rows > 0) ? symbolicResult[0].length : 0;
                    double[][] numericResult = new double[rows][cols];
                    boolean isFullyNumeric = true;

                    for(int i=0; i<rows; i++) {
                        for(int j=0; j<cols; j++) {
                            try {
                                numericResult[i][j] = evalExpr(symbolicResult[i][j]);
                            } catch (Exception e) {
                                isFullyNumeric = false;
                            }
                        }
                    }

                    if (isFullyNumeric) {
                        if (rows == 2 && cols == 2) {
                            // Risultato è una matrice
                            data.m00 = numericResult[0][0]; data.m01 = numericResult[0][1];
                            data.m10 = numericResult[1][0]; data.m11 = numericResult[1][1];
                            data.vx = 1; data.vy = 1;
                            data.hasMatrix = true; data.isValid = true;
                        } else if (rows == 2 && cols == 1) {
                            // Risultato è un vettore colonna
                            data.vx = numericResult[0][0];
                            data.vy = numericResult[1][0];
                            data.hasVector = true; data.isValid = true;
                        } else if (rows == 1 && cols == 2) {
                            // Risultato è un vettore riga
                            data.vx = numericResult[0][0];
                            data.vy = numericResult[0][1];
                            data.hasVector = true; data.isValid = true;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore fallback errors
            }

            return data;
        }

        private static double evalExpr(String input) {
            if (input == null || input.trim().isEmpty()) return 0.0;
            String clean = input.replace(",", ".").replaceAll("\\s+", "");
            String[] terms = clean.split("(?=[+-])");
            double sum = 0.0;
            for (String term : terms) {
                if (term.isEmpty() || term.equals("+") || term.equals("-")) continue;
                sum += Double.parseDouble(term);
            }
            return sum;
        }
    }


    private static class VisualizationData {
        boolean isValid = false;
        boolean hasMatrix = false;
        boolean hasVector = false;

        double m00 = 1, m01 = 0;
        double m10 = 0, m11 = 1;

        double vx = 1, vy = 1;
    }

    private static class AutoResizingFormulaPanel extends JPanel {
        private final TeXIcon icon;

        public AutoResizingFormulaPanel(TeXIcon icon) {
            this.icon = icon;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (icon == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            int padding = 40;

            double scaleX = (double) (panelWidth - padding) / iconWidth;
            double scaleY = (double) (panelHeight - padding) / iconHeight;
            double scale = Math.min(scaleX, scaleY);

            if (scale < 0.1) scale = 0.1;

            int x = (int) ((panelWidth - (iconWidth * scale)) / 2);
            int y = (int) ((panelHeight - (iconHeight * scale)) / 2);

            AffineTransform oldTransform = g2.getTransform();
            g2.translate(x, y);
            g2.scale(scale, scale);
            icon.paintIcon(this, g2, 0, 0);
            g2.setTransform(oldTransform);
        }
    }
}