package org.example;

public class MatrixUtils {

    // =============================================================
    // SEZIONE SIMBOLICA (String[][]) - FIX PER FORMULA VIEWER
    // =============================================================

    /**
     * Moltiplica due matrici contenenti stringhe (supporta simboli e numeri).
     * Gestisce:
     * 1. Matrice x Matrice
     * 2. Scalare (1x1) x Matrice
     * 3. Matrice x Scalare (1x1)
     */
    public static String[][] multiplySymbolic(String[][] A, String[][] B) {
        // Caso 1: A è scalare (1x1)
        if (A.length == 1 && A[0].length == 1) {
            return multiplyScalarSymbolic(B, A[0][0]);
        }
        // Caso 2: B è scalare (1x1)
        if (B.length == 1 && B[0].length == 1) {
            return multiplyScalarSymbolic(A, B[0][0]);
        }

        // Caso 3: Matrice x Matrice standard
        if (A[0].length != B.length) {
            throw new IllegalArgumentException("Dimensioni incompatibili: " + size(A) + " vs " + size(B));
        }

        int rows = A.length;
        int cols = B[0].length;
        int common = A[0].length;
        String[][] result = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                StringBuilder sum = new StringBuilder();
                boolean firstTerm = true;

                for (int k = 0; k < common; k++) {
                    String valA = A[i][k];
                    String valB = B[k][j];

                    // Calcola il prodotto del singolo termine (es. "2" * "x" -> "2x")
                    String product = multiplyTerms(valA, valB);

                    // Se il prodotto non è 0, lo aggiungiamo alla somma
                    if (!product.equals("0")) {
                        // Gestione segno "+" tra i termini
                        if (!firstTerm && !product.startsWith("-")) {
                            sum.append(" + ");
                        }
                        sum.append(product);
                        firstTerm = false;
                    }
                }
                // Se la somma è vuota (tutti 0), il risultato è 0
                result[i][j] = sum.length() == 0 ? "0" : sum.toString();
            }
        }
        return result;
    }

    private static String[][] multiplyScalarSymbolic(String[][] matrix, String scalar) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        String[][] result = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = multiplyTerms(scalar, matrix[i][j]);
            }
        }
        return result;
    }

    /**
     * Logica intelligente per moltiplicare due termini stringa.
     * Es: multiplyTerms("2", "x") -> "2x"
     * Es: multiplyTerms("0", "x") -> "0"
     * Es: multiplyTerms("1", "y") -> "y"
     */
    private static String multiplyTerms(String a, String b) {
        a = a.trim();
        b = b.trim();

        if (a.equals("0") || b.equals("0")) return "0";
        if (a.equals("1")) return b;
        if (b.equals("1")) return a;

        // Se entrambi sono numeri, calcoliamo il vero prodotto numerico
        if (isNumber(a) && isNumber(b)) {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            double res = da * db;
            return formatDouble(res);
        }

        // Logica estetica:
        // Se A è numero e B è lettera (es 2, x) -> "2x"
        // Se A è lettera e B è numero (es x, 2) -> "2x" (conviene riordinare, ma per ora facciamo semplice "x \cdot 2")
        // Se entrambi lettere -> "x \cdot y" o "xy"

        // Aggiungi parentesi se necessario (se ci sono + o -)
        boolean aComplex = a.contains("+") || (a.contains("-") && !a.startsWith("-"));
        boolean bComplex = b.contains("+") || (b.contains("-") && !b.startsWith("-"));

        String termA = aComplex ? "(" + a + ")" : a;
        String termB = bComplex ? "(" + b + ")" : b;

        if (isNumber(a) && !isNumber(b)) {
            return termA + termB; // 2x
        }

        return termA + " \\cdot " + termB; // x \cdot y
    }

    public static String[][] addSymbolic(String[][] A, String[][] B) {
        checkDimensions(A, B);
        int rows = A.length;
        int cols = A[0].length;
        String[][] result = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String valA = A[i][j].trim();
                String valB = B[i][j].trim();

                if (valA.equals("0")) result[i][j] = valB;
                else if (valB.equals("0")) result[i][j] = valA;
                else result[i][j] = valA + " + " + valB;
            }
        }
        return result;
    }

    public static String[][] transposeSymbolic(String[][] A) {
        int rows = A.length;
        int cols = A[0].length;
        String[][] result = new String[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = A[i][j];
            }
        }
        return result;
    }

    // Convertitore per String[][] (Questo risolve l'errore "Provided: String[][]")
    public static String toLatex(String[][] matrix) {
        if (matrix == null || matrix.length == 0) return "";
        StringBuilder sb = new StringBuilder("\\begin{pmatrix}");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(matrix[i][j]);
                if (j < matrix[i].length - 1) sb.append(" & ");
            }
            if (i < matrix.length - 1) sb.append(" \\\\ ");
        }
        sb.append("\\end{pmatrix}");
        return sb.toString();
    }

    private static boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    private static String formatDouble(double val) {
        if (val == (long) val) {
            return String.format("%d", (long) val);
        } else {
            return String.valueOf(val);
        }
    }

    // Helper dimensioni per String[][]
    private static void checkDimensions(String[][] A, String[][] B) {
        if (A.length != B.length || A[0].length != B[0].length) {
            throw new IllegalArgumentException("Dimensioni diverse: " + size(A) + " vs " + size(B));
        }
    }
    private static String size(String[][] A) { return "(" + A.length + "x" + A[0].length + ")"; }


    // =============================================================
    // SEZIONE NUMERICA (double[][]) - PER RETRO-COMPATIBILITÀ
    // =============================================================

    public static double[][] getTranspose(double[][] matrix) {
        if (matrix == null || matrix.length == 0) return matrix;
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    public static String toLatex(double[][] matrix) {
        StringBuilder sb = new StringBuilder("\\begin{pmatrix}");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(formatDouble(matrix[i][j]));
                if (j < matrix[i].length - 1) sb.append(" & ");
            }
            if (i < matrix.length - 1) sb.append(" \\\\ ");
        }
        sb.append("\\end{pmatrix}");
        return sb.toString();
    }

    public static double[][] add(double[][] A, double[][] B) {
        checkDimensions(A, B);
        int rows = A.length;
        int cols = A[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        return result;
    }

    public static double[][] subtract(double[][] A, double[][] B) {
        checkDimensions(A, B);
        int rows = A.length;
        int cols = A[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }
        return result;
    }

    public static double[][] multiply(double[][] A, double[][] B) {
        if (A.length == 1 && A[0].length == 1) return multiplyScalar(B, A[0][0]);
        if (B.length == 1 && B[0].length == 1) return multiplyScalar(A, B[0][0]);

        if (A[0].length != B.length) {
            throw new IllegalArgumentException("Dimensioni incompatibili: " + size(A) + " vs " + size(B));
        }

        int rows = A.length;
        int cols = B[0].length;
        int common = A[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < common; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }

    private static double[][] multiplyScalar(double[][] M, double s) {
        int rows = M.length;
        int cols = M[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = M[i][j] * s;
            }
        }
        return result;
    }

    private static void checkDimensions(double[][] A, double[][] B) {
        if (A.length != B.length || A[0].length != B[0].length) {
            throw new IllegalArgumentException("Dimensioni non corrispondenti: " + size(A) + " vs " + size(B));
        }
    }

    private static String size(double[][] A) {
        return "(" + A.length + "x" + A[0].length + ")";
    }

    /**
     * Esegue la sottrazione simbolica: A - B
     */
    public static String[][] subtractSymbolic(String[][] A, String[][] B) {
        checkDimensions(A, B);
        int rows = A.length;
        int cols = A[0].length;
        String[][] result = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String valA = A[i][j].trim();
                String valB = B[i][j].trim();

                // 1. Entrambi numeri
                if (isNumber(valA) && isNumber(valB)) {
                    double diff = Double.parseDouble(valA) - Double.parseDouble(valB);
                    result[i][j] = formatDouble(diff);
                }
                // 2. Se B è 0, resta A
                else if (valB.equals("0")) {
                    result[i][j] = valA;
                }
                // 3. Se A è 0, risultato è -B
                else if (valA.equals("0")) {
                    result[i][j] = negateTerm(valB);
                }
                // 4. Caso generico stringa
                else {
                    // Se B contiene operatori (+ o -), va messo tra parentesi per sottrarlo
                    // Es: A - (B + C)
                    boolean bComplex = valB.contains("+") || (valB.contains("-") && !valB.startsWith("-"));
                    String bFinal = bComplex ? "(" + valB + ")" : valB;

                    result[i][j] = valA + " - " + bFinal;
                }
            }
        }
        return result;
    }

    /**
     * Esegue la divisione simbolica: A / B.
     * Supporta principalmente Matrice / Scalare.
     */
    public static String[][] divideSymbolic(String[][] A, String[][] B) {
        // Supportiamo solo la divisione per uno scalare (o matrice 1x1)
        if (B.length != 1 || B[0].length != 1) {
            throw new IllegalArgumentException("Divisione supportata solo per scalari (denominatore deve essere 1x1).");
        }

        String denominator = B[0][0].trim();
        if (denominator.equals("0")) throw new ArithmeticException("Divisione per zero");

        int rows = A.length;
        int cols = A[0].length;
        String[][] result = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String numerator = A[i][j].trim();

                // 1. Numeri
                if (isNumber(numerator) && isNumber(denominator)) {
                    double val = Double.parseDouble(numerator) / Double.parseDouble(denominator);
                    result[i][j] = formatDouble(val);
                }
                // 2. Numeratore è 0
                else if (numerator.equals("0")) {
                    result[i][j] = "0";
                }
                // 3. Denominatore è 1
                else if (denominator.equals("1")) {
                    result[i][j] = numerator;
                }
                // 4. Caso generico: Frazione LaTeX
                else {
                    result[i][j] = "\\frac{" + numerator + "}{" + denominator + "}";
                }
            }
        }
        return result;
    }

    public static String[][] negateSymbolic(String[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        String[][] result = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = negateTerm(matrix[i][j]);
            }
        }
        return result;
    }

    /**
     * Logica per negare un singolo termine stringa.
     * Gestisce parentesi e segni esistenti.
     */
    private static String negateTerm(String term) {
        term = term.trim();

        // 1. Zero rimane zero
        if (term.equals("0")) return "0";

        // 2. Gestione Numerica
        if (isNumber(term)) {
            double val = Double.parseDouble(term);
            return formatDouble(-val);
        }

        // 3. Gestione Simbolica

        // Verifica se il termine è "complesso" (contiene operatori + o - non tra parentesi)
        // Nota: controlliamo se c'è un + o un - DOPO il primo carattere
        boolean isComplex = term.contains("+") || term.substring(1).contains("-");

        if (isComplex) {
            // Se è complesso (es. "x+y" o "-x+y"), mettiamo tutto tra parentesi col meno davanti
            // Es: "x+y" -> "-(x+y)"
            return "-(" + term + ")";
        } else {
            // È un termine semplice (es. "x", "-x", "5y", "-5y")
            if (term.startsWith("-")) {
                // Se ha già il meno, lo togliamo: "-x" -> "x"
                return term.substring(1);
            } else {
                // Se non ce l'ha, lo aggiungiamo: "x" -> "-x"
                return "-" + term;
            }
        }
    }
}