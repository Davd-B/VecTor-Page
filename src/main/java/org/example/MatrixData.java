package org.example;

public class MatrixData {
    public String rowsStr, colsStr;
    public boolean isSymbolic;
    public String[][] values; // I valori grezzi presi dalle caselle di testo o dal calcolo

    public String[][] getValues() {
        return values;
    }

    // --- METODO POTENZIATO ---
    // Ora è in grado di capire che "2 + 4" vale 6.0
    public double[][] getDoubleValues() {
        if (values == null) return null;

        int r = values.length;
        int c = values[0].length;
        double[][] doubles = new double[r][c];

        try {
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    // Chiama la nuova funzione di valutazione
                    doubles[i][j] = evaluateSimpleExpression(values[i][j]);
                }
            }
            return doubles;
        } catch (Exception e) {
            // Se c'è una lettera (es "2x + 4") fallisce e ritorna null, corretto.
            return null;
        }
    }

    /**
     * Risolve stringhe come "2 + 4", "10 - 2", "3.5 + 1" restituendo il double.
     */
    private double evaluateSimpleExpression(String input) {
        if (input == null || input.trim().isEmpty()) return 0.0;

        // 1. Pulizia: rimuovi spazi e uniforma virgole
        String clean = input.replace(",", ".").replaceAll("\\s+", "");

        // 2. Trick con Regex: Splitta mantenendo i delimitatori (+ e -) come parte del numero successivo
        // La regex (?=[+-]) è un "lookahead": taglia PRIMA di un + o un -
        // Es: "2+4-1" diventa ["2", "+4", "-1"]
        // Es: "-5+2" diventa ["-5", "+2"]
        String[] terms = clean.split("(?=[+-])");

        double sum = 0.0;
        for (String term : terms) {
            if (term.isEmpty()) continue;
            // Gestione del caso in cui lo split crea stringhe vuote o solo segni (raro ma possibile)
            if (term.equals("+") || term.equals("-")) continue;

            sum += Double.parseDouble(term);
        }
        return sum;
    }

    private boolean isOne(String s) {
        return "1".equals(s.trim());
    }

    public String toLatex() {
        if (isSymbolic && values == null) {
            return "\\begin{bmatrix} \\dots \\end{bmatrix}_{" + rowsStr + " \\times " + colsStr + "}";
        }
        // Se values esiste (anche se simbolico), lo stampiamo
        if (values == null) return "";

        StringBuilder sb = new StringBuilder("\\begin{pmatrix}");
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                sb.append(values[i][j]);
                if (j < values[i].length - 1) sb.append(" & ");
            }
            if (i < values.length - 1) sb.append(" \\\\ ");
        }
        sb.append("\\end{pmatrix}");
        return sb.toString();
    }

    public boolean isVector() {
        return (isOne(rowsStr) && !isOne(colsStr)) || (!isOne(rowsStr) && isOne(colsStr));
    }

    public boolean isMatrix() {
        return !isOne(rowsStr) && !isOne(colsStr);
    }
}