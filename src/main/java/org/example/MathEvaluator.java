package org.example;

public class MathEvaluator {
    private final String expression;
    private int pos = -1, ch;

    public MathEvaluator(String expression) {
        this.expression = expression;
    }

    // Punto di ingresso: restituisce una matrice di stringhe (simbolica)
    public String[][] evaluate() {
        nextChar();
        String[][] x = parseExpression();
        if (pos < expression.length()) {
            throw new RuntimeException("Carattere inatteso alla posizione " + pos + ": '" + (char)ch + "'");
        }
        return x;
    }

    private void nextChar() {
        ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
    }

    // Mangia spazi e controlla se il carattere corrente corrisponde a quello atteso
    private boolean eat(int charToEat) {
        while (ch == ' ') nextChar();
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }

    // Parsing Espressione: Gestione Somma (+)
    private String[][] parseExpression() {
        String[][] x = this.parseTerm();

        while (true) {
            if (this.eat(43)) { // '+'
                x = MatrixUtils.addSymbolic(x, this.parseTerm());
            } else if (this.eat(45)) { // '-'
                // Assicurati che MatrixUtils abbia il metodo subtractSymbolic
                x = MatrixUtils.subtractSymbolic(x, this.parseTerm());
            } else {
                return x;
            }
        }
    }

    // AGGIORNATO: Ora gestisce * (42), / (47) e la moltiplicazione implicita
    private String[][] parseTerm() {
        String[][] x = this.parseFactor();

        while(true) {
            // Gestione moltiplicazione esplicita (*, ·, ecc)
            if (this.eat(42) || this.eat(183) || this.eat(8901)) {
                x = MatrixUtils.multiplySymbolic(x, this.parseFactor());
                continue;
            }

            // Gestione divisione (/)
            if (this.eat(47)) { // '/'
                // Assicurati che MatrixUtils abbia il metodo divideSymbolic
                x = MatrixUtils.divideSymbolic(x, this.parseFactor());
                continue;
            }

            // Logica per la moltiplicazione implicita (es. "2x" o "A B")
            int startPos = this.pos;
            while(this.ch == 32) {
                this.nextChar();
            }

            // Se non è una parentesi aperta, né una lettera, né un numero,
            // allora non è un nuovo fattore, quindi abbiamo finito con questo termine.
            if (this.ch != 40 && !Character.isLetter(this.ch) && !Character.isDigit(this.ch)) {
                this.pos = startPos - 1;
                this.nextChar();
                return x;
            }

            // Se siamo qui, c'è un fattore implicito, quindi moltiplichiamo
            this.pos = startPos - 1;
            this.nextChar();
            x = MatrixUtils.multiplySymbolic(x, this.parseFactor());
        }
    }


    // Parsing Fattore: Gestione Potenze / Trasposte (^T)
    private String[][] parseFactor() {
        if (this.eat(45)) { // 45 è il codice ASCII per '-'
            // Se troviamo un meno, parsiamo il fattore successivo e lo neghiamo
            return MatrixUtils.negateSymbolic(this.parseFactor());
        }

        String[][] x = parseAtom();

        // Gestione esponenti (supportiamo solo la trasposta T o t)
        while (eat('^')) {
            while (ch == ' ') nextChar();
            if (ch == 'T' || ch == 't') {
                nextChar();
                x = MatrixUtils.transposeSymbolic(x);
            } else {
                // Opzionale: gestire potenze numeriche se necessario
                throw new RuntimeException("Esponente non supportato: previsto 'T' per la trasposta.");
            }
        }
        return x;
    }

    // Parsing Atomo: Parentesi, Numeri, Variabili
    private String[][] parseAtom() {
        while (ch == ' ') nextChar();

        // 1. Parentesi
        if (eat('(')) {
            String[][] x = parseExpression();
            if (!eat(')')) throw new RuntimeException("Manca parentesi chiusa ')'");
            return x;
        }

        // 2. Numeri (Gestiti come matrici 1x1 contenenti la stringa del numero)
        if ((ch >= '0' && ch <= '9') || ch == '.') {
            StringBuilder sb = new StringBuilder();
            while ((ch >= '0' && ch <= '9') || ch == '.') {
                sb.append((char)ch);
                nextChar();
            }
            return new String[][]{{sb.toString()}};
        }

        // 3. Variabili (Lettere)
        if (Character.isLetter(ch)) {
            StringBuilder sb = new StringBuilder();
            // Leggiamo tutto il blocco di lettere/numeri (es. "A", "Av", "matrix1")
            while (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') {
                sb.append((char)ch);
                nextChar();
            }
            String varName = sb.toString();

            // CASO A: Variabile Esatta (es. "A" esiste)
            MatrixData data = VariableManager.get(varName);
            if (data != null) {
                return data.getValues();
            }

            // CASO B: FIX PER "Av" (Variabili attaccate)
            // Se "Av" non esiste, controlliamo se è composto da "A" e "v"
            if (varName.length() > 1) {
                for (int i = 1; i < varName.length(); i++) {
                    String part1 = varName.substring(0, i);
                    String part2 = varName.substring(i);

                    // Se entrambe le parti esistono come variabili separate
                    if (VariableManager.get(part1) != null && VariableManager.get(part2) != null) {

                        // IMPORTANTE: Rewind del Parser
                        // Abbiamo letto troppo (es. abbiamo letto "Av", ma volevamo solo "A").
                        // Dobbiamo restituire "A" e rimettere "v" nel flusso per il prossimo ciclo.

                        // 'pos' attualmente è all'indice dopo la fine di "Av".
                        // Torniamo indietro della lunghezza della seconda parte.
                        pos -= part2.length();

                        // Aggiorniamo 'ch' al carattere corrente dopo il rewind (che sarà l'inizio di part2, es 'v')
                        ch = expression.charAt(pos);

                        // Ritorniamo il valore della prima parte ("A")
                        return VariableManager.get(part1).getValues();
                    }
                }
            }

            // Gestione Costanti Matematiche
            if (varName.equalsIgnoreCase("pi")) return new String[][]{{"\\pi"}};
            if (varName.equalsIgnoreCase("e")) return new String[][]{{"e"}};

            throw new RuntimeException("Variabile non definita: " + varName);
        }

        throw new RuntimeException("Carattere inatteso: '" + (char)ch + "'");
    }
}