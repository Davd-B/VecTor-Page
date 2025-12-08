package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MatrixAssignerDialog extends JDialog {
    private boolean confirmed = false;
    private MatrixData resultData;
    private JTextField txtRows, txtCols;
    private JPanel gridPanel;
    private JScrollPane scrollPane;
    private List<JTextField> cellInputs = new ArrayList<>();

    public MatrixAssignerDialog(Frame parent, String varName) {
        super(parent, "Assegna: " + varName, true);
        setSize(650, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));

        // Apply theme colors
        Color bgColor = MathEditorApp.getThemeColor("bg");
        Color cardColor = MathEditorApp.getThemeColor("card");
        Color textColor = MathEditorApp.getThemeColor("text");
        Color borderColor = MathEditorApp.getThemeColor("border");

        getContentPane().setBackground(bgColor);

        // Header Panel con titolo e descrizione
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBackground(cardColor);
        headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel titleLabel = new JLabel("Definisci Matrice: " + varName);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(textColor);

        JLabel subtitleLabel = new JLabel("Imposta le dimensioni e i valori della matrice");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 180));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 5));
        titlePanel.setBackground(cardColor);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Pannello Dimensioni con design moderno
        JPanel dimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        dimPanel.setBackground(cardColor);
        dimPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel dimLabel = new JLabel("Dimensioni (n × m):");
        dimLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        dimLabel.setForeground(textColor);
        dimPanel.add(dimLabel);

        // Styled text fields per dimensioni
        txtRows = createStyledTextField("2", 50);
        txtCols = createStyledTextField("2", 50);

        JLabel rowLabel = new JLabel("Righe:");
        rowLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rowLabel.setForeground(textColor);
        dimPanel.add(rowLabel);
        dimPanel.add(txtRows);

        JLabel colLabel = new JLabel("Colonne:");
        colLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        colLabel.setForeground(textColor);
        dimPanel.add(colLabel);
        dimPanel.add(txtCols);

        contentPanel.add(dimPanel, BorderLayout.NORTH);

        // Griglia Dinamica con scroll
        gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(cardColor);

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBackground(cardColor);
        scrollPane.getViewport().setBackground(cardColor);
        scrollPane.setBorder(new LineBorder(borderColor, 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Bottom Panel con bottoni moderni
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setBackground(cardColor);
        bottomPanel.setBorder(new EmptyBorder(15, 25, 20, 25));

        JButton btnCancel = createStyledButton("Annulla", false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createStyledButton("Salva Matrice", true);
        btnSave.addActionListener(e -> {
            saveData();
            confirmed = true;
            dispose();
        });

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listener aggiornamento griglia
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateGrid(); }
            public void removeUpdate(DocumentEvent e) { updateGrid(); }
            public void changedUpdate(DocumentEvent e) { updateGrid(); }
        };
        txtRows.getDocument().addDocumentListener(dl);
        txtCols.getDocument().addDocumentListener(dl);

        // Inizializza la griglia
        updateGrid();
    }

    private JTextField createStyledTextField(String defaultValue, int width) {
        JTextField field = new JTextField(defaultValue);
        field.setPreferredSize(new Dimension(width, 32));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setHorizontalAlignment(JTextField.CENTER);

        Color textColor = MathEditorApp.getThemeColor("text");
        Color cardColor = MathEditorApp.getThemeColor("card");
        Color borderColor = MathEditorApp.getThemeColor("border");

        field.setBackground(cardColor);
        field.setForeground(textColor);
        field.setCaretColor(textColor);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));

        return field;
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(MathEditorApp.Theme.ACCENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(MathEditorApp.Theme.ACCENT_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(MathEditorApp.Theme.ACCENT);
            }
        });

        return btn;
    }

    private void updateGrid() {
        gridPanel.removeAll();
        cellInputs.clear();
        String rS = txtRows.getText().trim();
        String cS = txtCols.getText().trim();

        Color cardColor = MathEditorApp.getThemeColor("card");
        Color textColor = MathEditorApp.getThemeColor("text");
        Color borderColor = MathEditorApp.getThemeColor("border");

        if (rS.matches("\\d+") && cS.matches("\\d+")) {
            try {
                int r = Integer.parseInt(rS);
                int c = Integer.parseInt(cS);

                if (r > 0 && c > 0 && r * c <= 100) {
                    JPanel inner = new JPanel(new GridLayout(r, c, 8, 8));
                    inner.setBackground(cardColor);
                    inner.setBorder(new EmptyBorder(15, 15, 15, 15));

                    for (int i = 0; i < r * c; i++) {
                        JTextField f = new JTextField("");
                        f.setPreferredSize(new Dimension(65, 45));
                        f.setFont(new Font("Monospaced", Font.PLAIN, 14));
                        f.setHorizontalAlignment(JTextField.CENTER);
                        f.setBackground(cardColor);
                        f.setForeground(textColor);
                        f.setCaretColor(textColor);
                        f.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(borderColor, 1, true),
                                new EmptyBorder(5, 5, 5, 5)
                        ));

                        cellInputs.add(f);
                        inner.add(f);
                    }
                    gridPanel.add(inner);
                } else {
                    JLabel msgLabel = new JLabel("Dimensioni non valide o troppo grandi (max 100 celle)");
                    msgLabel.setForeground(new Color(255, 100, 100));
                    msgLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    gridPanel.add(msgLabel);
                }
            } catch (NumberFormatException ex) {
                JLabel errLabel = new JLabel("Errore: numero troppo grande");
                errLabel.setForeground(new Color(255, 100, 100));
                errLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
                gridPanel.add(errLabel);
            }
        } else {
            JLabel symLabel = new JLabel("Matrice Simbolica / Parametrica");
            symLabel.setForeground(textColor);
            symLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
            gridPanel.add(symLabel);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void saveData() {
        resultData = new MatrixData();
        resultData.rowsStr = txtRows.getText();
        resultData.colsStr = txtCols.getText();

        if (resultData.rowsStr.matches("\\d+") && resultData.colsStr.matches("\\d+")) {
            int r = Integer.parseInt(resultData.rowsStr);
            int c = Integer.parseInt(resultData.colsStr);
            resultData.values = new String[r][c];
            resultData.isSymbolic = false;

            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    int idx = i * c + j;
                    if (idx < cellInputs.size()) {
                        String text = cellInputs.get(idx).getText().trim();
                        if (text.isEmpty()) {
                            resultData.values[i][j] = "0";
                        } else {
                            resultData.values[i][j] = text;
                        }
                    }
                }
            }
        } else {
            resultData.isSymbolic = true;
        }
    }

    public boolean isConfirmed() { return confirmed; }
    public MatrixData getMatrixData() { return resultData; }
}