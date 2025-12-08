package org.example;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class AssignmentHandler {

    // MODIFICA 1: La Regex ora accetta stringhe alfanumeriche (es. "alfa", "x1", "Beta_2")
    // [a-zA-Z_]       -> Il primo carattere deve essere una lettera o un underscore
    // [a-zA-Z0-9_]*   -> I successivi possono essere lettere, numeri o underscore
    private static final Pattern ASSIGN_PATTERN = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=$");

    public static String tryHandleAssignment(String rawInput, JFrame parentFrame) {
        Matcher matcher = ASSIGN_PATTERN.matcher(rawInput);

        if (matcher.matches()) {
            String varName = matcher.group(1); // Qui catturiamo "alfa" o "A"

            // Passiamo il nome "pulito" al dialog (es. "alfa")
            MatrixAssignerDialog dialog = new MatrixAssignerDialog(parentFrame, varName);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                MatrixData data = dialog.getMatrixData();

                // Salviamo in memoria con la chiave originale "alfa"
                VariableManager.save(varName, data);

                String latexName = SmartMathParser.convertVarToLatex(varName);

                return latexName + " = " + data.toLatex();
            }
        }
        return null;
    }

    /**
     * Recupera il valore numerico (double[][]) se disponibile, altrimenti null.
     */
    public static Object getVariable(String varName) {
        MatrixData data = VariableManager.get(varName);
        if (data != null) {
            return data.getDoubleValues();
        }
        return null;
    }

    public static Map<String, MatrixData> getVariablesMap() {
        return VariableManager.getAll();
    }
}