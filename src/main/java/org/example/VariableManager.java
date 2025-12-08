package org.example;

import java.util.HashMap;
import java.util.Map;

// File: VariableManager.java
public class VariableManager {
    // La mappa che contiene le variabili (es. "A" -> Oggetto MatrixData)
    private static Map<String, MatrixData> variables = new HashMap<>();

    // Salva una variabile
    public static void save(String name, MatrixData data) {
        variables.put(name, data);
    }

    // Recupera l'oggetto MatrixData
    public static MatrixData get(String name) {
        return variables.get(name);
    }

    // Controlla se esiste
    public static boolean exists(String name) {
        return variables.containsKey(name);
    }

    // Serve a SmartMathParser per sapere quali parole colorare di blu o sostituire
    public static Map<String, MatrixData> getAll() {
        return variables;
    }
}
