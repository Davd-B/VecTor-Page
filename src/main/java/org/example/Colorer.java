package org.example;

import org.example.MatrixData;
import org.example.VariableManager;

class Colorer {

    public static String colorize(String varName) {
        // Se la variabile non esiste nel DB, nessun colore (nero default)
        if (!VariableManager.exists(varName)) {
            return varName;
        }

        MatrixData data = VariableManager.get(varName);

        if (data.isMatrix()) {
            // Matrice (nxm) -> Rosso
            return "\\textcolor{red}{" + varName + "}";
        }
        else if (data.isVector()) {
            // Vettore (1xm o nx1) -> Blu
            return "\\textcolor{purple}{" + varName + "}";
        }

        // Scalare o altro -> Nero
        return varName;
    }
}