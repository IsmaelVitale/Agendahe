package com.helizahair.ui.dialogs;

import com.helizahair.db.AgendamentoDAO;
import com.helizahair.db.ProcedimentoDAO;
import com.helizahair.model.Agendamento;
import com.helizahair.model.Procedimento;
import com.helizahair.state.AppState;
import com.helizahair.ui.CorPaleta;
import com.helizahair.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class SettingsDialog {

    private final AppState estado;
    private final Runnable aoFechar;
    private final Stage stage = new Stage();
    private final VBox listaProcedimentos = new VBox(6);

    private ComboBox<Integer> comboAbertura;
    private ComboBox<Integer> comboFechamento;
    private final TextField campoNome = new TextField();
    private final ComboBox<Integer> comboDuracao =
            new ComboBox<>(FXCollections.observableArrayList(15, 30, 45, 60, 90, 120, 180));
    private final TextField campoPreco = new TextField();
    private final ComboBox<String> comboCor =
            new ComboBox<>(FXCollections.observableArrayList(CorPaleta.todas().keySet()));

    public SettingsDialog(AppState estado, Runnable aoFechar) {
        this.estado = estado;
        this.aoFechar = aoFechar;
        montar();
    }

    private void montar() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Configura\u00E7\u00F5es do Sistema");

        VBox raiz = new VBox(20);
        raiz.getStyleClass().add("modal-conteudo");
        raiz.setPadding(new Insets(24));

        Label titulo = new Label("Configura\u00E7\u00F5es do Sistema");
        titulo.getStyleClass().add("titulo-modal");

        // --- Tema ---
        VBox blocoTema = new VBox(10);
        blocoTema.getStyleClass().add("bloco-config");
        blocoTema.setPadding(new Insets(16));
        Label lblTema = new Label("Apar\u00EAncia (Tema)");
        lblTema.getStyleClass().add("subtitulo-config");
        HBox botoesTema = new HBox(10);
        for (String tema : new String[]{"light", "dark", "cream"}) {
            Button b = new Button(rotuloTema(tema));
            b.getStyleClass().add("btn-tema");
            b.setOnAction(e -> estado.setTema(tema));
            botoesTema.getChildren().add(b);
        }
        blocoTema.getChildren().addAll(lblTema, botoesTema);

        // --- Horários ---
        VBox blocoHorario = new VBox(10);
        blocoHorario.getStyleClass().add("bloco-config");
        blocoHorario.setPadding(new Insets(16));
        Label lblHorario = new Label("Hor\u00E1rio de Funcionamento (Grade)");
        lblHorario.getStyleClass().add("subtitulo-config");

        comboAbertura = new ComboBox<>();
        comboFechamento = new ComboBox<>();
        for (int i = 0; i <= 24; i++) {
            comboAbertura.getItems().add(i);
            comboFechamento.getItems().add(i);
        }
        comboAbertura.setValue(estado.getHoraAbertura());
        comboFechamento.setValue(estado.getHoraFechamento());
        comboAbertura.getStyleClass().add("input-padrao");
        comboFechamento.getStyleClass().add("input-padrao");

        Button btnAplicarHorario = new Button("Aplicar Hor\u00E1rios");
        btnAplicarHorario.getStyleClass().add("btn-azul");
        btnAplicarHorario.setOnAction(e -> salvarHorario());

        HBox horarioBox = new HBox(10,
                new VBox(2, new Label("Abertura"), comboAbertura),
                new VBox(2, new Label("Fechamento"), comboFechamento),
                btnAplicarHorario);
        horarioBox.setAlignment(Pos.BOTTOM_LEFT);
        blocoHorario.getChildren().addAll(lblHorario, horarioBox);

        // --- Procedimentos ---
        VBox blocoProc = new VBox(10);
        blocoProc.getStyleClass().add("bloco-config");
        blocoProc.setPadding(new Insets(16));
        Label lblProc = new Label("Cat\u00E1logo de Procedimentos");
        lblProc.getStyleClass().add("subtitulo-config");

        campoNome.setPromptText("Nome do servi\u00E7o");
        campoNome.getStyleClass().add("input-padrao");
        comboDuracao.setValue(30);
        comboDuracao.getStyleClass().add("input-padrao");
        campoPreco.setPromptText("Pre\u00E7o");
        campoPreco.getStyleClass().add("input-padrao");
        comboCor.setValue("pink");
        comboCor.getStyleClass().add("input-padrao");

        Button btnAdicionar = new Button("+");
        btnAdicionar.getStyleClass().add("btn-verde");
        btnAdicionar.setOnAction(e -> adicionarProcedimento());

        HBox formProc = new HBox(8, campoNome, comboDuracao, campoPreco, comboCor, btnAdicionar);
        renderizarProcedimentos();
        ScrollPane scrollProc = new ScrollPane(listaProcedimentos);
        scrollProc.setFitToWidth(true);
        scrollProc.setPrefHeight(180);

        blocoProc.getChildren().addAll(lblProc, formProc, scrollProc);

        Button btnFechar = new Button("Conclu\u00EDdo");
        btnFechar.getStyleClass().add("btn-secundario");
        btnFechar.setOnAction(e -> stage.close());

        raiz.getChildren().addAll(titulo, blocoTema, blocoHorario, blocoProc, btnFechar);

        ScrollPane scrollGeral = new ScrollPane(raiz);
        scrollGeral.setFitToWidth(true);

        stage.setScene(new Scene(scrollGeral, 660, 700));
        stage.setOnHidden(e -> { if (aoFechar != null) aoFechar.run(); });
    }

    private String rotuloTema(String t) {
        return switch (t) {
            case "dark" -> "Escuro M\u00E9dio";
            case "cream" -> "Tons Creme";
            default -> "Claro Padr\u00E3o";
        };
    }

    private void salvarHorario() {
        int novaAbertura = comboAbertura.getValue();
        int novoFechamento = comboFechamento.getValue();
        if (novaAbertura >= novoFechamento) {
            alerta("Abertura deve ser menor que o fechamento.");
            return;
        }

        List<Agendamento> todos = AgendamentoDAO.listarPorPeriodo("0000-01-01", "9999-12-31");
        for (Agendamento a : todos) {
            int ini = DateUtil.horaParaMinutos(a.getHoraInicio());
            int fim = DateUtil.horaParaMinutos(a.getHoraFim());
            if (ini < novaAbertura * 60 || fim > novoFechamento * 60) {
                alerta("N\u00E3o \u00E9 poss\u00EDvel alterar.\nConflito: " + a.getCliente() + " \u00E0s "
                        + a.getHoraInicio() + " em " + DateUtil.formatarDataBR(a.getData()));
                return;
            }
        }
        estado.setHorario(novaAbertura, novoFechamento);
        alerta("Hor\u00E1rios da grade atualizados com sucesso!");
    }

    private void adicionarProcedimento() {
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            alerta("Informe o nome do servi\u00E7o.");
            return;
        }
        double preco;
        try {
            preco = Double.parseDouble(campoPreco.getText().replace(",", "."));
        } catch (Exception e) {
            preco = 0;
        }

        Procedimento p = new Procedimento("proc_" + System.currentTimeMillis(), nome,
                comboDuracao.getValue(), preco, comboCor.getValue());
        ProcedimentoDAO.salvar(p);
        estado.recarregarProcedimentos();
        campoNome.clear();
        campoPreco.clear();
        renderizarProcedimentos();
    }

    private void renderizarProcedimentos() {
        listaProcedimentos.getChildren().clear();
        for (Procedimento p : estado.getProcedimentos()) {
            HBox linha = new HBox(10);
            linha.getStyleClass().add("linha-procedimento");
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.setPadding(new Insets(6, 10, 6, 10));

            Region bolinha = new Region();
            bolinha.setPrefSize(14, 14);
            bolinha.setStyle("-fx-background-radius: 7; -fx-background-color: " + CorPaleta.fundo(p.getCor()) + ";");

            Label lbl = new Label(String.format("%s \u2014 %d min \u2014 R$ %.2f",
                    p.getNome(), p.getDuracaoMin(), p.getPreco()));

            Region esp = new Region();
            HBox.setHgrow(esp, Priority.ALWAYS);

            Button excluir = new Button("Excluir");
            excluir.getStyleClass().add("btn-perigo");
            excluir.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Excluir este procedimento do cat\u00E1logo?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        ProcedimentoDAO.excluir(p.getId());
                        estado.recarregarProcedimentos();
                        renderizarProcedimentos();
                    }
                });
            });

            linha.getChildren().addAll(bolinha, lbl, esp, excluir);
            listaProcedimentos.getChildren().add(linha);
        }
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public void mostrar() { stage.showAndWait(); }
}
