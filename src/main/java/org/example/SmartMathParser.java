package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartMathParser {
    private final String expression;
    private final Function<String, String> variableHandler;
    private int pos = -1, ch;

    public static final Map<String, String> GREEK_MAP = new HashMap<>();
    static {
        // Minuscole
        GREEK_MAP.put("alfa", "\\alpha");       GREEK_MAP.put("alpha", "\\alpha");
        GREEK_MAP.put("beta", "\\beta");
        GREEK_MAP.put("gamma", "\\gamma");
        GREEK_MAP.put("delta", "\\delta");
        GREEK_MAP.put("epsilon", "\\epsilon");
        GREEK_MAP.put("zeta", "\\zeta");
        GREEK_MAP.put("eta", "\\eta");
        GREEK_MAP.put("teta", "\\theta");       GREEK_MAP.put("theta", "\\theta");
        GREEK_MAP.put("iota", "\\iota");
        GREEK_MAP.put("kappa", "\\kappa");
        GREEK_MAP.put("lambda", "\\lambda");
        GREEK_MAP.put("mi", "\\mu");            GREEK_MAP.put("mu", "\\mu");
        GREEK_MAP.put("ni", "\\nu");            GREEK_MAP.put("nu", "\\nu");
        GREEK_MAP.put("csi", "\\xi");           GREEK_MAP.put("xi", "\\xi");
        GREEK_MAP.put("omicron", "o");
        GREEK_MAP.put("pi", "\\pi");
        GREEK_MAP.put("rho", "\\rho");
        GREEK_MAP.put("sigma", "\\sigma");
        GREEK_MAP.put("tau", "\\tau");
        GREEK_MAP.put("ipsilon", "\\upsilon");  GREEK_MAP.put("upsilon", "\\upsilon");
        GREEK_MAP.put("phi", "\\phi");          GREEK_MAP.put("fi", "\\phi");
        GREEK_MAP.put("chi", "\\chi");
        GREEK_MAP.put("psi", "\\psi");
        GREEK_MAP.put("omega", "\\omega");

        // Maiuscole (Quelle che differiscono dal latino usano il comando LaTeX,
        // quelle uguali usano la lettera latina es: Alfa -> A)
        GREEK_MAP.put("Alfa", "A");             GREEK_MAP.put("Alpha", "A");
        GREEK_MAP.put("Beta", "B");
        GREEK_MAP.put("Gamma", "\\Gamma");
        GREEK_MAP.put("Delta", "\\Delta");
        GREEK_MAP.put("Epsilon", "E");
        GREEK_MAP.put("Zeta", "Z");
        GREEK_MAP.put("Eta", "H");
        GREEK_MAP.put("Teta", "\\Theta");       GREEK_MAP.put("Theta", "\\Theta");
        GREEK_MAP.put("Iota", "I");
        GREEK_MAP.put("Kappa", "K");
        GREEK_MAP.put("Lambda", "\\Lambda");
        GREEK_MAP.put("Mi", "M");               GREEK_MAP.put("Mu", "M");
        GREEK_MAP.put("Ni", "N");               GREEK_MAP.put("Nu", "N");
        GREEK_MAP.put("Csi", "\\Xi");           GREEK_MAP.put("Xi", "\\Xi");
        GREEK_MAP.put("Omicron", "O");
        GREEK_MAP.put("Pi", "\\Pi");
        GREEK_MAP.put("Rho", "P");
        GREEK_MAP.put("Sigma", "\\Sigma");
        GREEK_MAP.put("Tau", "T");
        GREEK_MAP.put("Ipsilon", "\\Upsilon");  GREEK_MAP.put("Upsilon", "\\Upsilon");
        GREEK_MAP.put("Phi", "\\Phi");          GREEK_MAP.put("Fi", "\\Phi");
        GREEK_MAP.put("Chi", "X");
        GREEK_MAP.put("Psi", "\\Psi");
        GREEK_MAP.put("Omega", "\\Omega");
    }

    public static String parseToColoredLatex(String input) {
        // Usa la classe Colorer definita sotto
        return new SmartMathParser(input, Colorer::colorize).parse();
    }

    public static String parseToExpandedLatex(String rawInput) {
        String processed = rawInput.trim();

        // 1. Gestione Trasposta
        // Regex per catturare "Variabile^T" o "Variabile^t"
        String transposeRegex = "(?<![a-zA-Z_])([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\^\\s*[tT]\\b";
        Pattern pattern = Pattern.compile(transposeRegex);
        Matcher matcher = pattern.matcher(processed);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            MatrixData data = VariableManager.get(varName);

            if (data != null) {
                // Tenta di ottenere i valori numerici
                double[][] numericMatrix = data.getDoubleValues();

                String latexTransposed;
                if (numericMatrix != null) {
                    // CASO 1: Matrice Numerica
                    // Usa la tua classe MatrixUtils esistente
                    double[][] transposed = MatrixUtils.getTranspose(numericMatrix);
                    latexTransposed = MatrixUtils.toLatex(transposed);
                } else {
                    // CASO 2: Matrice Simbolica (contiene lettere o espressioni)
                    // Usiamo i valori stringa grezzi
                    String[][] stringMatrix = data.getValues();
                    latexTransposed = transposeStringMatrixToLatex(stringMatrix);
                }

                // Sostituzione nel testo (quoteReplacement è fondamentale per il LaTeX)
                matcher.appendReplacement(sb, Matcher.quoteReplacement(latexTransposed));
            } else {
                // Se la variabile non esiste, lascia il testo originale (es. x^T)
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        processed = sb.toString();

        // 2. Parser per sostituzione semplice (A -> matrice)
        Function<String, String> matrixSubstitutionHandler = (varName) -> {
            MatrixData data = VariableManager.get(varName);
            if (data != null) {
                return data.toLatex();
            }
            return varName;
        };

        return new SmartMathParser(processed, matrixSubstitutionHandler).parse();
    }

    // --- HELPER PER MATRICI SIMBOLICHE ---
    // Questo metodo serve se la matrice contiene "x", "y", ecc.
    // MatrixUtils accetta solo double[][], quindi facciamo qui la trasposta delle stringhe.
    private static String transposeStringMatrixToLatex(String[][] matrix) {
        if (matrix == null || matrix.length == 0) return "";

        int rows = matrix.length;
        int cols = matrix[0].length;

        StringBuilder sb = new StringBuilder();
        sb.append("\\begin{pmatrix}");

        // Nota lo scambio degli indici: scorriamo prima le colonne originali (righe nuove)
        for (int j = 0; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                sb.append(matrix[i][j]); // [i][j] diventa l'elemento corrente nella nuova riga
                if (i < rows - 1) {
                    sb.append(" & ");
                }
            }
            if (j < cols - 1) {
                sb.append(" \\\\ ");
            }
        }
        sb.append("\\end{pmatrix}");
        return sb.toString();
    }

    // --- METODI MANCANTI IMPLEMENTATI ---

    private static String replaceSimpleVariables(String text) {
        // Cerca parole intere che corrispondono a variabili salvate
        StringBuilder sb = new StringBuilder();
        // Regex: trova identificatori (lettere)
        Pattern p = Pattern.compile("\\b([a-zA-Z]+)\\b");
        Matcher m = p.matcher(text);

        while (m.find()) {
            String varName = m.group(1);
            MatrixData data = VariableManager.get(varName);
            if (data != null) {
                // Sostituisce il nome variabile con il LaTeX della matrice
                m.appendReplacement(sb, Matcher.quoteReplacement(data.toLatex()));
            } else {
                // Lascia invariato se non è una variabile nota (o è una funzione tipo sin/cos gestita altrove)
                // Nota: questo è un replace grezzo, il parser reale gestisce meglio le funzioni
                m.appendReplacement(sb, Matcher.quoteReplacement(varName));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String formatOperatorsToLatex(String latex) {
        // Sostituzioni finali di pulizia (opzionale, dato che il parser lo fa già)
        return latex.replace("*", " \\cdot ");
    }

    // --- COSTRUTTORE E PARSING ---

    private SmartMathParser(String expression, Function<String, String> variableHandler) {
        this.expression = expression;
        this.variableHandler = variableHandler;
    }

    private void nextChar() {
        ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
    }

    private boolean eat(int charToEat) {
        while (ch == ' ') nextChar();
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }

    private String parse() {
        nextChar();
        String x = parseExpression();
        if (pos < expression.length()) throw new RuntimeException("Carattere inatteso: " + (char)ch);
        return x;
    }

    private String parseExpression() {
        String x = parseTerm();
        for (;;) {
            if      (eat('+')) x = x + " + " + parseTerm();
            else if (eat('-')) x = x + " - " + parseTerm();
            else if (eat('=')) x = x + " = " + parseExpression();
            else return x;
        }
    }

    private String parseTerm() {
        String x = parseFactor();
        for (;;) {
            if (eat('*')) {
                x = x + " \\cdot " + parseFactor();
            } else {
                while (ch == ' ') nextChar();

                // MODIFICA QUI: Aggiungi "|| ch == '\\'" per supportare i blocchi LaTeX
                // che sono stati appena sostituiti dalla regex
                if (ch == '(' || Character.isLetter(ch) || ch == '\\') {
                    x = x + " \\cdot " + parseFactor();
                } else {
                    return x;
                }
            }
        }
    }

    private String parseFactor() {
        String x = parsePower();
        for (;;) {
            if (eat('/')) {
                String num = x;
                String den = parsePower();
                x = "\\frac{" + num + "}{" + den + "}";
            } else return x;
        }
    }

    private String parsePower() {
        String x = parseAtom();
        for (;;) {
            if (eat('^')) {
                String exp = parsePower();
                x = "{" + x + "}^{" + exp + "}";
            } else return x;
        }
    }

    private String parseAtom() {
        if (ch == '\\') {
            return parseLatexBlock();
        }

        while (ch == ' ') nextChar();
        if (eat('(')) {
            String x = parseExpression();
            if (!eat(')')) throw new RuntimeException("Manca ')'");
            return "\\left(" + x + "\\right)";
        }
        if ((ch >= '0' && ch <= '9') || ch == '.') {
            StringBuilder sb = new StringBuilder();
            while ((ch >= '0' && ch <= '9') || ch == '.') {
                sb.append((char)ch);
                nextChar();
            }
            return formatNumber(sb.toString());
        }

        // --- GESTIONE IDENTIFICATORI (Testo) ---
        if (Character.isLetter(ch)) {
            StringBuilder sb = new StringBuilder();
            while (Character.isLetter(ch)) {
                sb.append((char)ch);
                nextChar();
            }
            String str = sb.toString();

            // 1. Controllo Funzioni (sin, cos, etc)
            if (isFunction(str)) return "\\" + str.toLowerCase();

            // 2. Controllo Lettere Greche
            if (GREEK_MAP.containsKey(str)) {
                String latexSymbol = GREEK_MAP.get(str); // es. "\alpha"

                // IMPORTANTE: Controlliamo se questa lettera greca è ANCHE una variabile definita.
                // Passiamo "alfa" all'handler (che farà colorazione o espansione)
                String processed = variableHandler.apply(str);

                // Se l'handler ha modificato la stringa (quindi esiste come variabile)
                if (!processed.equals(str)) {

                    // CASO A: Espansione Matrice (Formula Viewer)
                    // Se restituisce una matrice LaTeX, la mostriamo interamente.
                    if (processed.startsWith("\\begin")) {
                        return processed;
                    }

                    // CASO B: Colorazione (Visualizzazione normale)
                    // processed è es: "\textcolor{red}{alfa}"
                    // Noi vogliamo visivamente: "\textcolor{red}{\alpha}"
                    if (processed.startsWith("\\textcolor")) {
                        // Sostituiamo il nome variabile (es "alfa") con il simbolo ("\alpha")
                        // all'interno del comando colore.
                        return processed.replace("{" + str + "}", "{" + latexSymbol + "}");
                    }
                }

                // Se non è una variabile, o l'handler non l'ha toccata, ritorna solo il simbolo greco
                return latexSymbol;
            }

            // 3. Gestione Variabili Standard (non greche, es. "x", "A")
            StringBuilder processedVars = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                if (i > 0) processedVars.append(" \\cdot ");
                String singleVar = String.valueOf(str.charAt(i));
                processedVars.append(variableHandler.apply(singleVar));
            }
            return processedVars.toString();
        }
        return "";
    }

    private boolean isFunction(String str) {
        return str.matches("(?i)log|ln|sin|cos|tan");
    }

    // Metodo per consumare un blocco LaTeX intero (es. \begin{pmatrix} ... \end{pmatrix})
    private String parseLatexBlock() {
        StringBuilder sb = new StringBuilder();

        // 1. Aggiungiamo il primo backslash già trovato
        sb.append((char)ch);
        nextChar();

        // 2. Leggiamo il nome del comando (es. "begin", "frac", "cdot")
        while (Character.isLetter(ch)) {
            sb.append((char)ch);
            nextChar();
        }

        String command = sb.toString();

        // 3. SE è un ambiente matrice (\begin), dobbiamo leggere tutto fino alla fine (\end)
        if (command.equals("\\begin")) {
            // Cerchiamo di capire quale ambiente è (es. {pmatrix})
            // Leggiamo finché non troviamo la chiusura della parentesi graffa
            StringBuilder envType = new StringBuilder();
            while (ch != -1 && ch != '}') {
                sb.append((char)ch);
                envType.append((char)ch);
                nextChar();
            }
            if (ch == '}') {
                sb.append('}');
                envType.append('}'); // ora envType è es: "{pmatrix}"
                nextChar();
            }

            // Costruiamo il tag di chiusura che ci aspettiamo (es. \end{pmatrix})
            String endTag = "\\end" + envType.toString();

            // Leggiamo tutto il contenuto della matrice finché non troviamo il tag di chiusura
            while (ch != -1) {
                sb.append((char)ch);

                // Controllo se abbiamo appena scritto il tag di chiusura
                if (sb.toString().endsWith(endTag)) {
                    nextChar(); // Consumiamo l'ultimo carattere e usciamo
                    break;
                }
                nextChar();
            }
        }
        // 4. SE è un altro comando (es. \frac, \sqrt), la gestione base qui è semplificata:
        // Assumiamo che se non è \begin, potrebbe essere un comando semplice.
        // Per sicurezza, se il carattere successivo è '{', leggiamo il blocco bilanciato.
        else {
            // Se c'è un blocco graffe subito dopo (es \frac{...}), lo leggiamo.
            // (Implementazione semplificata: legge finché trova caratteri validi o blocchi)
            while (ch == '{') {
                sb.append((char)ch);
                nextChar();
                int openBraces = 1;
                while (openBraces > 0 && ch != -1) {
                    if (ch == '{') openBraces++;
                    if (ch == '}') openBraces--;
                    sb.append((char)ch);
                    nextChar();
                }
            }
        }

        return sb.toString();
    }

    private String formatNumber(String rawNumber) {
        try {
            if (rawNumber.equals(".")) return "0";
            java.math.BigDecimal bd = new java.math.BigDecimal(rawNumber);
            return bd.stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return rawNumber;
        }
    }

    public static String convertVarToLatex(String varName) {
        if (GREEK_MAP.containsKey(varName)) {
            return GREEK_MAP.get(varName);
        }
        return varName;
    }
}