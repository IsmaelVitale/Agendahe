package com.helizahair;

import com.helizahair.db.BancoDeDados;
import com.helizahair.state.AppState;
import com.helizahair.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage palco) {
        BancoDeDados.inicializar();

        AppState estado = new AppState();
        MainView mainView = new MainView(estado);

        Scene cena = new Scene(mainView.getRaiz(), 1300, 820);
        cena.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());

        aplicarTema(cena, estado.getTema());
        estado.aoAlterar(() -> aplicarTema(cena, estado.getTema()));

        palco.setScene(cena);
        palco.setTitle("BelezaFlow Desktop");
        palco.setMinWidth(1024);
        palco.setMinHeight(700);
        palco.show();
    }

    private void aplicarTema(Scene cena, String tema) {
        cena.getRoot().getStyleClass().removeIf(c -> c.startsWith("tema-"));
        cena.getRoot().getStyleClass().add("tema-" + tema);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
