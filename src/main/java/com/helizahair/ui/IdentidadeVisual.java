package com.helizahair.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Objects;

/** Recursos visuais oficiais compartilhados por todas as janelas. */
public final class IdentidadeVisual {

    public static final String NOME_APLICACAO = "Agenda Eliza Hair";

    private static final Image ICONE = carregarImagem("/images/eh-shield.png");
    private static final Image LOGOTIPO = carregarImagem("/images/eliza-hair-studio.png");

    private IdentidadeVisual() {
    }

    public static void aplicarIcone(Stage stage) {
        stage.getIcons().setAll(ICONE);
    }

    public static ImageView criarLogotipo(double largura) {
        ImageView visualizacao = new ImageView(LOGOTIPO);
        visualizacao.setFitWidth(largura);
        visualizacao.setPreserveRatio(true);
        visualizacao.setSmooth(true);
        visualizacao.setAccessibleText("Eliza Hair Studio");
        return visualizacao;
    }

    private static Image carregarImagem(String caminho) {
        InputStream arquivo = Objects.requireNonNull(
                IdentidadeVisual.class.getResourceAsStream(caminho),
                "Recurso visual não encontrado: " + caminho
        );
        return new Image(arquivo);
    }
}
