package com.helizahair.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public class CorPaleta {

    private static final Map<String, String[]> CORES = new LinkedHashMap<>();
    static {
        CORES.put("pink",   new String[]{"#ec4899", "#be185d"});
        CORES.put("blue",   new String[]{"#3b82f6", "#1d4ed8"});
        CORES.put("purple", new String[]{"#a855f7", "#7e22ce"});
        CORES.put("red",    new String[]{"#ef4444", "#b91c1c"});
        CORES.put("teal",   new String[]{"#14b8a6", "#0f766e"});
        CORES.put("orange", new String[]{"#f97316", "#c2410c"});
        CORES.put("gray",   new String[]{"#4b5563", "#1f2937"});
    }

    public static Map<String, String[]> todas() { return CORES; }

    public static String fundo(String chave) {
        return CORES.getOrDefault(chave, CORES.get("gray"))[0];
    }

    public static String borda(String chave) {
        return CORES.getOrDefault(chave, CORES.get("gray"))[1];
    }
}
