package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class VectorVisualizer2D extends JDialog {

    private double ix = 1.0, iy = 0.0;
    private double jx = 0.0, jy = 1.0;
    private double ax = 1.0, ay = 1.0;

    private CartesianPanel canvas;
    private JTextField txtIx, txtIy, txtJx, txtJy, txtAx, txtAy;
    private JButton btnPlay, btnStop;
    private JLabel lblDeterminant, lblTransformed;
    private boolean isUpdatingFromCode = false;
    private JCheckBox chkShowEigen;
    private Timer animTimer;
    private double animProgress = 0.0;
    private double animDirection = 0.01;
    private double snapIx, snapIy, snapJx, snapJy;

    private static final int SCALE = 50;

    public VectorVisualizer2D(Window owner) {
        // Se il genitore è un Dialog modale, anche questo deve esserlo per interagire
        super(owner, "VecTor - Visualizzatore Vettoriale 2D",
                (owner instanceof Dialog) ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);

        setSize(1200, 800);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        Color bgColor = MathEditorApp.getThemeColor("bg");
        Color cardColor = MathEditorApp.getThemeColor("card");
        getContentPane().setBackground(bgColor);

        JPanel leftPanel = createLeftPanel();
        leftPanel.setBackground(cardColor);
        add(leftPanel, BorderLayout.WEST);

        canvas = new CartesianPanel();
        add(canvas, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        bottomPanel.setBackground(cardColor);
        add(bottomPanel, BorderLayout.SOUTH);

        setupTimer();
    }

    private void setupTimer() {
        // Timer per l'animazione a ~60fps
        animTimer = new Timer(16, e -> {
            animProgress += animDirection;
            if (animProgress >= 1.0) {
                animProgress = 1.0;
                animDirection = -0.01;
            } else if (animProgress <= 0.0) {
                animProgress = 0.0;
                animDirection = 0.01;
            }

            // Easing function per rendere l'animazione più naturale
            double t = animProgress * animProgress * (3 - 2 * animProgress);

            // Interpolazione lineare (Lerp) tra il valore salvato (snap) e la matrice Identità
            ix = lerp(snapIx, 1.0, t);
            iy = lerp(snapIy, 0.0, t);
            jx = lerp(snapJx, 0.0, t);
            jy = lerp(snapJy, 1.0, t);

            updateTextFields();

            // NOTA: Non aggiorniamo le label testuali (Det, T(a)) durante l'animazione per performance
            // canvas.repaint() viene chiamato alla fine
            canvas.repaint();
        });
    }

    private double lerp(double start, double end, double t) { return start + (end - start) * t; }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        Color cardColor = MathEditorApp.getThemeColor("card");
        Color borderColor = MathEditorApp.getThemeColor("border");
        Color textColor = MathEditorApp.getThemeColor("text");

        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        infoPanel.setBackground(cardColor);

        lblDeterminant = createInfoLabel("Det: 1.00");
        lblTransformed = createInfoLabel("T(a) = (1.00, 1.00)");

        infoPanel.add(lblDeterminant);
        infoPanel.add(new JSeparator(SwingConstants.VERTICAL));
        infoPanel.add(lblTransformed);

        // Checkbox per mostrare/nascondere autovettori
        chkShowEigen = new JCheckBox("Mostra Autovettori");
        chkShowEigen.setBackground(cardColor);
        chkShowEigen.setForeground(textColor);
        chkShowEigen.setFont(new Font("SansSerif", Font.BOLD, 12));
        chkShowEigen.setFocusPainted(false);
        chkShowEigen.addActionListener(e -> canvas.repaint());
        infoPanel.add(new JSeparator(SwingConstants.VERTICAL));
        infoPanel.add(chkShowEigen);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(cardColor);

        btnPlay = createAnimButton("▶", "Avvia Animazione");
        btnStop = createAnimButton("■", "Ferma e Ripristina");
        btnStop.setEnabled(false);

        btnPlay.addActionListener(e -> startAnimation());
        btnStop.addActionListener(e -> stopAnimation());

        controlPanel.add(btnPlay);
        controlPanel.add(btnStop);

        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(controlPanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createInfoLabel(String text) {
        Color textColor = MathEditorApp.getThemeColor("text");
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospace", Font.BOLD, 13));
        label.setForeground(textColor);
        return label;
    }

    private JButton createAnimButton(String symbol, String tooltip) {
        Color accentColor = new Color(99, 102, 241);
        Color accentHover = new Color(79, 82, 221);
        JButton btn = new JButton(symbol);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(accentColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setToolTipText(tooltip);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(accentHover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(accentColor); }
        });
        return btn;
    }

    private void startAnimation() {
        if (animTimer.isRunning()) return;
        // Salva lo stato corrente per poterlo ripristinare
        snapIx = ix; snapIy = iy;
        snapJx = jx; snapJy = jy;
        setInputsEnabled(false);
        btnPlay.setEnabled(false);
        btnStop.setEnabled(true);
        animProgress = 0.0;
        animDirection = 0.015;
        animTimer.start();
    }

    private void stopAnimation() {
        if (!animTimer.isRunning() && btnPlay.isEnabled()) return;
        animTimer.stop();
        // Ripristina e aggiorna tutto un'ultima volta
        ix = snapIx; iy = snapIy;
        jx = snapJx; jy = snapJy;
        updateTextFields();
        updateInfoLabels();
        canvas.repaint();
        setInputsEnabled(true);
        btnPlay.setEnabled(true);
        btnStop.setEnabled(false);
    }

    private void setInputsEnabled(boolean enabled) {
        txtIx.setEnabled(enabled);
        txtIy.setEnabled(enabled);
        txtJx.setEnabled(enabled);
        txtJy.setEnabled(enabled);
        txtAx.setEnabled(enabled);
        txtAy.setEnabled(enabled);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(400, 600));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        Color bg = MathEditorApp.getThemeColor("bg");
        Color text = MathEditorApp.getThemeColor("text");
        Color card = MathEditorApp.getThemeColor("card");

        panel.setBackground(bg);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setBackground(card);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Trasformazioni");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(text);
        header.add(title);

        panel.add(header);
        panel.add(Box.createVerticalStrut(20));

        panel.add(createMatrixSection(text, card));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createVectorSection(text, card));
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createMatrixSection(Color text, Color card) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(card);
        Color bgColor = MathEditorApp.getThemeColor("bg");
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0,0,0,20), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        JLabel sectionTitle = new JLabel("Matrice T");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        sectionTitle.setForeground(text);
        JPanel matrixContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        matrixContainer.setBackground(card);
        matrixContainer.setBorder(new EmptyBorder(10, 0, 0, 0));
        JPanel matrixContent = new JPanel(new GridLayout(2, 2, 10, 10));
        matrixContent.setBackground(card);
        txtIx = createMatrixField(String.valueOf(ix), text, card);
        txtJx = createMatrixField(String.valueOf(jx), text, card);
        txtIy = createMatrixField(String.valueOf(iy), text, card);
        txtJy = createMatrixField(String.valueOf(jy), text, card);
        matrixContent.add(createFieldWithLabel(txtIx, "i·x", new Color(239, 68, 68), text, card));
        matrixContent.add(createFieldWithLabel(txtJx, "j·x", new Color(34, 197, 94), text, card));
        matrixContent.add(createFieldWithLabel(txtIy, "i·y", new Color(239, 68, 68), text, card));
        matrixContent.add(createFieldWithLabel(txtJy, "j·y", new Color(34, 197, 94), text, card));
        BracketPanel bracketPanel = new BracketPanel(matrixContent, bgColor, card);
        matrixContainer.add(bracketPanel);
        section.add(sectionTitle, BorderLayout.NORTH);
        section.add(matrixContainer, BorderLayout.CENTER);
        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateFromFields(); }
            public void removeUpdate(DocumentEvent e) { updateFromFields(); }
            public void changedUpdate(DocumentEvent e) { updateFromFields(); }
        };
        txtIx.getDocument().addDocumentListener(docListener);
        txtIy.getDocument().addDocumentListener(docListener);
        txtJx.getDocument().addDocumentListener(docListener);
        txtJy.getDocument().addDocumentListener(docListener);
        return section;
    }

    private JPanel createVectorSection(Color text, Color card) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(card);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0,0,0,20), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        JLabel sectionTitle = new JLabel("Vettore Input (a)");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        sectionTitle.setForeground(text);
        JPanel vectorContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        vectorContainer.setBackground(card);
        vectorContainer.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel vectorName = new JLabel("<html><i style='font-size:18px; font-family:serif'>a</i> =</html>");
        vectorName.setForeground(text);
        JPanel vectorContent = new JPanel(new GridLayout(2, 1, 0, 10));
        vectorContent.setBackground(card);
        txtAx = createMatrixField(String.valueOf(ax), text, card);
        txtAy = createMatrixField(String.valueOf(ay), text, card);
        vectorContent.add(createFieldWithLabel(txtAx, "x", new Color(59, 130, 246), text, card));
        vectorContent.add(createFieldWithLabel(txtAy, "y", new Color(59, 130, 246), text, card));
        BracketPanel bracketPanel = new BracketPanel(vectorContent, card, card);
        vectorContainer.add(vectorName);
        vectorContainer.add(bracketPanel);
        section.add(sectionTitle, BorderLayout.NORTH);
        section.add(vectorContainer, BorderLayout.CENTER);
        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateFromFields(); }
            public void removeUpdate(DocumentEvent e) { updateFromFields(); }
            public void changedUpdate(DocumentEvent e) { updateFromFields(); }
        };
        txtAx.getDocument().addDocumentListener(docListener);
        txtAy.getDocument().addDocumentListener(docListener);
        return section;
    }

    private JTextField createMatrixField(String val, Color textColor, Color cardColor) {
        JTextField f = new JTextField(val);
        f.setForeground(textColor);
        f.setCaretColor(textColor);
        boolean isDark = (cardColor.getRed() + cardColor.getGreen() + cardColor.getBlue()) / 3 < 128;
        Color inputBg = isDark ?
                new Color(cardColor.getRed()+20, cardColor.getGreen()+20, cardColor.getBlue()+20) :
                new Color(245, 245, 245);
        f.setBackground(inputBg);
        f.setPreferredSize(new Dimension(85, 45));
        f.setFont(new Font("Monospaced", Font.BOLD, 18));
        f.setHorizontalAlignment(JTextField.CENTER);
        Color borderColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 100);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                new EmptyBorder(8, 8, 8, 8)
        ));
        return f;
    }

    private JPanel createFieldWithLabel(JTextField field, String label, Color labelColor, Color textColor, Color bg) {
        JPanel container = new JPanel(new BorderLayout(10, 0));
        container.setBackground(bg);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(labelColor);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setPreferredSize(new Dimension(38, 35));
        container.add(lbl, BorderLayout.WEST);
        container.add(field, BorderLayout.CENTER);
        return container;
    }

    private void updateFromFields() {
        if (isUpdatingFromCode || (animTimer != null && animTimer.isRunning())) return;
        try {
            ix = parse(txtIx.getText());
            iy = parse(txtIy.getText());
            jx = parse(txtJx.getText());
            jy = parse(txtJy.getText());
            ax = parse(txtAx.getText());
            ay = parse(txtAy.getText());
            updateInfoLabels();

            if (canvas != null) {
                canvas.repaint();
            }
        } catch (NumberFormatException ignored) {}
    }

    private void updateTextFields() {
        isUpdatingFromCode = true;
        DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.US));
        txtIx.setText(df.format(ix));
        txtIy.setText(df.format(iy));
        txtJx.setText(df.format(jx));
        txtJy.setText(df.format(jy));
        txtAx.setText(df.format(ax));
        txtAy.setText(df.format(ay));
        isUpdatingFromCode = false;
    }

    private void updateInfoLabels() {
        double det = ix * jy - iy * jx;
        Point2D.Double transformed = getTransformedA();
        DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        lblDeterminant.setText("Det: " + df.format(det));
        lblTransformed.setText("T(a) = (" + df.format(transformed.x) + ", " + df.format(transformed.y) + ")");
    }

    private double parse(String s) {
        if (s == null || s.trim().isEmpty() || s.equals("-")) return 0;
        return Double.parseDouble(s.replace(",", "."));
    }

    private Point2D.Double getTransformedA() {
        double transX = ax * ix + ay * jx;
        double transY = ax * iy + ay * jy;
        return new Point2D.Double(transX, transY);
    }

    private class CartesianPanel extends JPanel {
        private String draggingVector = null;

        public CartesianPanel() {
            setBackground(MathEditorApp.isDarkMode() ? new Color(20, 22, 26) : Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (animTimer != null && animTimer.isRunning()) return;
                    checkClick(e.getX(), e.getY());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (animTimer != null && animTimer.isRunning()) return;
                    if (draggingVector != null) {
                        handleDrag(e.getX(), e.getY());
                        updateInfoLabels();
                        repaint();
                        updateTextFields();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    draggingVector = null;
                    if (animTimer == null || !animTimer.isRunning()) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private int centerX() { return getWidth() / 2; }
        private int centerY() { return getHeight() / 2; }
        private int toScreenX(double x) { return centerX() + (int)(x * SCALE); }
        private int toScreenY(double y) { return centerY() - (int)(y * SCALE); }
        private double toMathX(int x) { return (double)(x - centerX()) / SCALE; }
        private double toMathY(int y) { return (double)(centerY() - y) / SCALE; }

        private Point2D.Double getTransformedA() {
            double transX = ax * ix + ay * jx;
            double transY = ax * iy + ay * jy;
            return new Point2D.Double(transX, transY);
        }

        private void checkClick(int mx, int my) {
            if (isNear(mx, my, ix, iy)) draggingVector = "i";
            else if (isNear(mx, my, jx, jy)) draggingVector = "j";
            else {
                Point2D.Double posA = getTransformedA();
                if (isNear(mx, my, posA.x, posA.y)) draggingVector = "a";
            }
            if (draggingVector != null) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private boolean isNear(int mx, int my, double vx, double vy) {
            int sx = toScreenX(vx);
            int sy = toScreenY(vy);
            return Math.pow(mx - sx, 2) + Math.pow(my - sy, 2) < 225;
        }

        private void handleDrag(int mx, int my) {
            double mouseMathX = toMathX(mx);
            double mouseMathY = toMathY(my);

            // Snap leggero agli interi
            if (Math.abs(mouseMathX - Math.round(mouseMathX)) < 0.1) mouseMathX = Math.round(mouseMathX);
            if (Math.abs(mouseMathY - Math.round(mouseMathY)) < 0.1) mouseMathY = Math.round(mouseMathY);

            switch (draggingVector) {
                case "i": ix = mouseMathX; iy = mouseMathY; break;
                case "j": jx = mouseMathX; jy = mouseMathY; break;
                case "a":
                    double det = ix * jy - iy * jx;
                    if (Math.abs(det) > 0.0001) {
                        double newAx = (mouseMathX * jy - mouseMathY * jx) / det;
                        double newAy = (mouseMathX * -iy + mouseMathY * ix) / det;
                        if (Math.abs(newAx - Math.round(newAx)) < 0.1) newAx = Math.round(newAx);
                        if (Math.abs(newAy - Math.round(newAy)) < 0.1) newAy = Math.round(newAy);
                        ax = newAx; ay = newAy;
                    }
                    break;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();
            boolean darkMode = MathEditorApp.isDarkMode();

            // 1. Disegna Griglia
            g2.setStroke(new BasicStroke(0.8f));
            Color gridColor = darkMode ? new Color(50, 55, 65) : new Color(230, 232, 236);
            g2.setColor(gridColor);
            int range = 20;

            for (int k = -range; k <= range; k++) {
                if (k == 0) continue;
                double x1 = k * ix - range * jx;
                double y1 = k * iy - range * jy;
                double x2 = k * ix + range * jx;
                double y2 = k * iy + range * jy;
                g2.draw(new Line2D.Double(toScreenX(x1), toScreenY(y1), toScreenX(x2), toScreenY(y2)));
            }
            for (int k = -range; k <= range; k++) {
                if (k == 0) continue;
                double x1 = -range * ix + k * jx;
                double y1 = -range * iy + k * jy;
                double x2 = range * ix + k * jx;
                double y2 = range * iy + k * jy;
                g2.draw(new Line2D.Double(toScreenX(x1), toScreenY(y1), toScreenX(x2), toScreenY(y2)));
            }

            // 2. Assi Cartesiani
            g2.setColor(darkMode ? new Color(80, 85, 95) : new Color(180, 185, 190));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(0, centerY(), w, centerY());
            g2.drawLine(centerX(), 0, centerX(), h);

            // 3. Autovettori - OPTIMIZATION FIX
            // Controlla SE attivo E se l'animazione NON è in esecuzione
            if (chkShowEigen.isSelected() && (animTimer == null || !animTimer.isRunning())) {
                drawEigenvectors(g2, darkMode);
            }

            // 4. Box/Area trasformata
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{8.0f, 8.0f}, 0.0f));
            g2.setColor(new Color(239, 68, 68, 80));
            g2.draw(new Line2D.Double(toScreenX(-range*ix), toScreenY(-range*iy), toScreenX(range*ix), toScreenY(range*iy)));
            g2.setColor(new Color(34, 197, 94, 80));
            g2.draw(new Line2D.Double(toScreenX(-range*jx), toScreenY(-range*jy), toScreenX(range*jx), toScreenY(range*jy)));

            // 5. Vettori Base
            drawVector(g2, ix, iy, new Color(239, 68, 68), "i", darkMode);
            drawVector(g2, jx, jy, new Color(34, 197, 94), "j", darkMode);

            // 6. Vettore Input
            Point2D.Double posA = getTransformedA();
            drawVector(g2, posA.x, posA.y, new Color(59, 130, 246), "a", darkMode);

            // 7. Origine
            g2.setColor(darkMode ? new Color(200, 200, 200) : new Color(60, 60, 60));
            g2.fillOval(centerX()-4, centerY()-4, 8, 8);

            // 8. Indicatore Animazione
            if (animTimer != null && animTimer.isRunning()) {
                g2.setColor(new Color(239, 68, 68));
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.fillOval(w - 95, h - 25, 10, 10);
                g2.drawString("ANIMAZIONE", w - 80, h - 15);
            }
        }

        private void drawEigenvectors(Graphics2D g2, boolean darkMode) {
            // Calcolo Autovalori e Autovettori (Pesante, viene saltato durante l'animazione)
            double tr = ix + jy;
            double det = ix * jy - iy * jx;
            double delta = tr * tr - 4 * det;

            Color eigenColor = new Color(216, 180, 254);
            Color eigenVecColor = new Color(192, 132, 252);
            if (!darkMode) {
                eigenColor = new Color(147, 51, 234, 100);
                eigenVecColor = new Color(147, 51, 234);
            }

            if (delta < 0) {
                g2.setColor(darkMode ? Color.GRAY : Color.DARK_GRAY);
                g2.drawString("Autovalori complessi (rotazione)", 20, getHeight() - 50);
                return;
            }

            double lambda1 = (tr + Math.sqrt(delta)) / 2.0;
            double lambda2 = (tr - Math.sqrt(delta)) / 2.0;

            drawSingleEigen(g2, lambda1, eigenColor, eigenVecColor, "v1", darkMode);
            if (Math.abs(lambda1 - lambda2) > 0.0001) {
                drawSingleEigen(g2, lambda2, eigenColor, eigenVecColor, "v2", darkMode);
            }
        }

        private void drawSingleEigen(Graphics2D g2, double lambda, Color lineColor, Color vecColor, String label, boolean darkMode) {
            double a = ix - lambda;
            double b = jx;
            double c = iy;
            double d = jy - lambda;

            double vx, vy;
            if (Math.abs(b) > 0.0001 || Math.abs(a) > 0.0001) {
                vx = b; vy = -a;
            } else if (Math.abs(c) > 0.0001 || Math.abs(d) > 0.0001) {
                vx = d; vy = -c;
            } else {
                vx = 1; vy = 0;
            }

            double len = Math.hypot(vx, vy);
            if (len < 0.0001) return;
            vx /= len; vy /= len;

            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            int far = 100;
            g2.draw(new Line2D.Double(toScreenX(-vx*far), toScreenY(-vy*far), toScreenX(vx*far), toScreenY(vy*far)));
            drawVector(g2, vx, vy, vecColor, label + " (λ=" + String.format("%.2f", lambda) + ")", darkMode);
        }

        private void drawVector(Graphics2D g2, double vx, double vy, Color c, String label, boolean darkMode) {
            int x1 = centerX();
            int y1 = centerY();
            int x2 = toScreenX(vx);
            int y2 = toScreenY(vy);
            double angle = Math.atan2(y2 - y1, x2 - x1);
            double lineShortening = 6.0;
            int xLineEnd = (int) (x2 - lineShortening * Math.cos(angle));
            int yLineEnd = (int) (y2 - lineShortening * Math.sin(angle));

            g2.setColor(new Color(0, 0, 0, 30));
            g2.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x1 + 2, y1 + 2, xLineEnd + 2, yLineEnd + 2);

            g2.setColor(c);
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x1, y1, xLineEnd, yLineEnd);

            int arrowSize = 12;
            Path2D arrow = new Path2D.Double();
            arrow.moveTo(x2, y2);
            arrow.lineTo(x2 - arrowSize * Math.cos(angle - Math.PI / 6), y2 - arrowSize * Math.sin(angle - Math.PI / 6));
            arrow.lineTo(x2 - arrowSize * Math.cos(angle + Math.PI / 6), y2 - arrowSize * Math.sin(angle + Math.PI / 6));
            arrow.closePath();
            g2.fill(arrow);

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int labelW = fm.stringWidth(label);
            int labelH = fm.getHeight();
            int labelX = x2 + 12;
            int labelY = y2 - 8;

            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
            g2.fillRoundRect(labelX - 4, labelY - labelH + 4, labelW + 8, labelH, 6, 6);
            g2.setColor(c);
            g2.drawString(label, labelX, labelY);
        }
    }

    public void setTransformationData(double m00, double m10, double m01, double m11, double vecX, double vecY) {
        this.ix = m00;
        this.iy = m10;
        this.jx = m01;
        this.jy = m11;
        this.ax = vecX;
        this.ay = vecY;
        updateTextFields();
        updateInfoLabels();
        if (canvas != null) canvas.repaint();
    }

    class BracketPanel extends JPanel {
        private final JComponent content;
        private final Color bracketColor;
        public BracketPanel(JComponent content, Color bracketColor, Color backgroundColor) {
            this.content = content;
            this.bracketColor = new Color(216, 216, 216);
            setLayout(new BorderLayout());
            setBackground(backgroundColor);
            setBorder(new EmptyBorder(10, 20, 10, 20));
            add(content, BorderLayout.CENTER);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bracketColor);
            g2.setStroke(new BasicStroke(2.0f));
            int w = getWidth();
            int h = getHeight();
            int bW = 12;
            int gap = 1;
            g2.drawLine(gap, gap, gap, h - gap - 1);
            g2.drawLine(gap, gap, gap + bW, gap);
            g2.drawLine(gap, h - gap - 1, gap + bW, h - gap - 1);
            g2.drawLine(w - gap - 1, gap, w - gap - 1, h - gap - 1);
            g2.drawLine(w - gap - 1, gap, w - gap - 1 - bW, gap);
            g2.drawLine(w - gap - 1, h - gap - 1, w - gap - 1 - bW, h - gap - 1);
        }
    }
}