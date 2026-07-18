package com.helizahair.ui.dialogs;

import com.helizahair.db.FechamentoCaixaDAO;
import com.helizahair.model.FechamentoCaixa;
import com.helizahair.state.AppState;
import com.helizahair.ui.Estilos;
import com.helizahair.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportsDialog {

    private final AppState estado;
    private final Stage stage = new Stage();
    private ComboBox<String> comboMes;
    private final VBox listaDias = new VBox(6);
    private final Label totalMesLabel = new Label("R$ 0,00");

    public ReportsDialog(AppState estado) {
        this.estado = estado;
        montar();
    }

    private void montar() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Relat\u00F3rio Mensal");

        VBox raiz = new VBox(18);
        raiz.getStyleClass().add("modal-conteudo");
        raiz.setPadding(new Insets(24));

        Label titulo = new Label("Relat\u00F3rio Mensal");
        titulo.getStyleClass().add("titulo-modal");

        LocalDate hoje = estado.getDataAtual();
        List<String> meses = new ArrayList<>();
        for (int i = -12; i <= 12; i++) {
            LocalDate m = hoje.plusMonths(i);
            meses.add(String.format("%04d-%02d", m.getYear(), m.getMonthValue()));
        }
        comboMes = new ComboBox<>(FXCollections.observableArrayList(meses));
        comboMes.setValue(String.format("%04d-%02d", hoje.getYear(), hoje.getMonthValue()));
        comboMes.getStyleClass().add("input-padrao");
        comboMes.valueProperty().addListener((obs, a, n) -> carregar());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        VBox mesBox = new VBox(4, rotulo("M\u00EAs base"), comboMes);
        VBox totalBox = new VBox(4, rotulo("Total Fechado (M\u00EAs)"), totalMesLabel);
        totalMesLabel.getStyleClass().add("total-checkout");

        HBox topo = new HBox(24, mesBox, espaco, totalBox);
        topo.setAlignment(Pos.CENTER_LEFT);
        topo.getStyleClass().add("card-resumo");
        topo.setPadding(new Insets(16));

        ScrollPane scroll = new ScrollPane(listaDias);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        raiz.getChildren().addAll(titulo, topo, scroll);
        Scene cena = new Scene(raiz, 680, 580);
        Estilos.aplicar(cena, estado.getTema());
        stage.setScene(cena);
        carregar();
    }

    private Label rotulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("rotulo-secao");
        return l;
    }

    private void carregar() {
        listaDias.getChildren().clear();
        List<FechamentoCaixa> fechamentos = FechamentoCaixaDAO.listarFechadosDoMes(comboMes.getValue());
        double totalMes = 0;

        if (fechamentos.isEmpty()) {
            listaDias.getChildren().add(new Label("Nenhum caixa fechado neste m\u00EAs."));
        }
        for (FechamentoCaixa f : fechamentos) {
            totalMes += f.getTotal();
            HBox linha = new HBox(16);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.getStyleClass().add("linha-relatorio");
            linha.setPadding(new Insets(10));
            linha.getChildren().addAll(
                    new Label("\u2713 " + DateUtil.formatarDataBR(f.getData())),
                    new Label(f.getConcluidos() + " servi\u00E7o(s)"),
                    new Label(String.format("Extras: R$ %.2f", f.getExtras())),
                    new Label(String.format("Total: R$ %.2f", f.getTotal()))
            );
            listaDias.getChildren().add(linha);
        }
        totalMesLabel.setText(String.format(new Locale("pt", "BR"), "R$ %,.2f", totalMes));
    }

    public void mostrar() { stage.showAndWait(); }
}
