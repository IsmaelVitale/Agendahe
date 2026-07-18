package com.helizahair;

import com.helizahair.db.BancoDeDados;
import com.helizahair.state.AppState;
import com.helizahair.ui.Estilos;
import com.helizahair.ui.IdentidadeVisual;
import com.helizahair.ui.MainView;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage palco) {
        BancoDeDados.inicializar();

        AppState estado = new AppState();
        MainView mainView = new MainView(estado);

        Scene cena = new Scene(mainView.getRaiz());

        Estilos.aplicar(cena, estado.getTema());
        estado.aoAlterar(() -> Estilos.aplicarTema(cena, estado.getTema()));

        Rectangle2D area = Screen.getPrimary().getVisualBounds();

        double largura = Math.min(1300, area.getWidth() * 0.92);
        double altura = Math.min(820, area.getHeight() * 0.90);

        palco.setScene(cena);
        palco.setTitle(IdentidadeVisual.NOME_APLICACAO);
        IdentidadeVisual.aplicarIcone(palco);

        palco.setFullScreen(false);
        palco.setMaximized(false);

        palco.setWidth(largura);
        palco.setHeight(altura);
        palco.setMinWidth(Math.min(900, largura));
        palco.setMinHeight(Math.min(600, altura));

        palco.show();
        palco.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
