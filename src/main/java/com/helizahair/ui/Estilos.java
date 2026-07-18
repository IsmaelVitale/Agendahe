package com.helizahair.ui;

import javafx.scene.Scene;

import java.net.URL;

public final class Estilos {

    private Estilos() {}

    public static void aplicar(Scene cena, String tema) {
        URL css = Estilos.class.getResource("/estilo.css");
        if (css == null) {
            throw new IllegalStateException("estilo.css não encontrado em src/main/resources");
        }
        String folha = css.toExternalForm();
        if (!cena.getStylesheets().contains(folha)) {
            cena.getStylesheets().add(folha);
        }
        aplicarTema(cena, tema);
    }

    public static void aplicarTema(Scene cena, String tema) {
        cena.getRoot().getStyleClass().removeIf(classe -> classe.startsWith("tema-"));
        cena.getRoot().getStyleClass().add("tema-" + tema);
    }
}
