package com.helizahair.ui.dialogs;

import com.helizahair.db.ProcedimentoDAO;
import com.helizahair.model.Procedimento;
import com.helizahair.service.ConfiguracaoService;
import com.helizahair.service.RegraNegocioException;
import com.helizahair.state.AppState;
import com.helizahair.ui.CorPaleta;
import com.helizahair.ui.Estilos;
import com.helizahair.ui.IdentidadeVisual;
import com.helizahair.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsDialog {

    private final AppState estado;
    private final Runnable aoFechar;
    private final Stage stage = new Stage();
    private final VBox listaProcedimentos = new VBox(6);

    private ComboBox<String> comboAbertura;
    private ComboBox<String> comboFechamento;
    private final TextField campoNome = new TextField();
    private final ComboBox<Integer> comboDuracao =
            new ComboBox<>(FXCollections.observableArrayList(15, 30, 45, 60, 90, 120, 180));
    private final TextField campoPreco = new TextField();
    private final ComboBox<String> comboCor =
            new ComboBox<>(FXCollections.observableArrayList(CorPaleta.todas().keySet()));
    private final Button btnSalvarProcedimento = new Button("Adicionar");
    private final Button btnCancelarEdicao = new Button("Cancelar");
    private Procedimento procedimentoEmEdicao;

    public SettingsDialog(AppState estado, Runnable aoFechar) {
        this.estado = estado;
        this.aoFechar = aoFechar;
        montar();
    }

    private void montar() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Configura\u00E7\u00F5es do Sistema");
        IdentidadeVisual.aplicarIcone(stage);

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
            b.setOnAction(e -> {
                estado.setTema(tema);
                Estilos.aplicarTema(stage.getScene(), tema);
            });
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
        for (int minuto = 0; minuto <= 24 * 60; minuto += 30) {
            String horario = DateUtil.minutosParaHora(minuto);
            if (minuto < 24 * 60) {
                comboAbertura.getItems().add(horario);
            }
            if (minuto > 0) {
                comboFechamento.getItems().add(horario);
            }
        }
        comboAbertura.setValue(DateUtil.minutosParaHora(estado.getMinutoAbertura()));
        comboFechamento.setValue(DateUtil.minutosParaHora(estado.getMinutoFechamento()));
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

        btnSalvarProcedimento.getStyleClass().add("btn-verde");
        btnSalvarProcedimento.setOnAction(e -> salvarProcedimento());
        btnCancelarEdicao.getStyleClass().add("btn-secundario");
        btnCancelarEdicao.setVisible(false);
        btnCancelarEdicao.setManaged(false);
        btnCancelarEdicao.setOnAction(e -> limparFormularioProcedimento());

        campoNome.setPrefWidth(190);
        comboDuracao.setPrefWidth(85);
        campoPreco.setPrefWidth(90);
        comboCor.setPrefWidth(110);
        FlowPane formProc = new FlowPane(8, 8, campoNome, comboDuracao, campoPreco, comboCor,
                btnCancelarEdicao, btnSalvarProcedimento);
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

        Scene cena = new Scene(scrollGeral, 720, 700);
        Estilos.aplicar(cena, estado.getTema());
        stage.setScene(cena);
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
        int novaAbertura = DateUtil.horaParaMinutos(comboAbertura.getValue());
        int novoFechamento = DateUtil.horaParaMinutos(comboFechamento.getValue());
        try {
            ConfiguracaoService.validarExpediente(novaAbertura, novoFechamento);
            estado.setHorario(novaAbertura, novoFechamento);
            alertaInformativo("Horários da grade atualizados com sucesso!");
        } catch (RegraNegocioException e) {
            alerta(e.getMessage());
        }
    }

    private void salvarProcedimento() {
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            alerta("Informe o nome do servi\u00E7o.");
            return;
        }
        if (comboDuracao.getValue() == null || comboCor.getValue() == null) {
            alerta("Informe duração e cor do serviço.");
            return;
        }
        double preco;
        try {
            preco = Double.parseDouble(campoPreco.getText().replace(",", "."));
        } catch (Exception e) {
            alerta("Informe um preço válido.");
            return;
        }
        if (preco < 0) {
            alerta("O preço não pode ser negativo.");
            return;
        }

        String id = procedimentoEmEdicao == null
                ? "proc_" + System.currentTimeMillis()
                : procedimentoEmEdicao.getId();
        Procedimento p = new Procedimento(id, nome,
                comboDuracao.getValue(), preco, comboCor.getValue());
        ProcedimentoDAO.salvar(p);
        estado.recarregarProcedimentos();
        limparFormularioProcedimento();
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

            Button editar = new Button("Editar");
            editar.getStyleClass().add("btn-secundario");
            editar.setOnAction(e -> editarProcedimento(p));

            Button excluir = new Button("Excluir");
            excluir.getStyleClass().add("btn-perigo");
            excluir.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Excluir este procedimento do catálogo? Agendamentos antigos serão mantidos como personalizados.",
                        ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        ProcedimentoDAO.excluir(p.getId());
                        estado.recarregarProcedimentos();
                        renderizarProcedimentos();
                    }
                });
            });

            linha.getChildren().addAll(bolinha, lbl, esp, editar, excluir);
            listaProcedimentos.getChildren().add(linha);
        }
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void alertaInformativo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void editarProcedimento(Procedimento procedimento) {
        procedimentoEmEdicao = procedimento;
        campoNome.setText(procedimento.getNome());
        comboDuracao.setValue(procedimento.getDuracaoMin());
        campoPreco.setText(String.valueOf(procedimento.getPreco()));
        comboCor.setValue(procedimento.getCor());
        btnSalvarProcedimento.setText("Salvar");
        btnCancelarEdicao.setVisible(true);
        btnCancelarEdicao.setManaged(true);
        campoNome.requestFocus();
    }

    private void limparFormularioProcedimento() {
        procedimentoEmEdicao = null;
        campoNome.clear();
        campoPreco.clear();
        comboDuracao.setValue(30);
        comboCor.setValue("pink");
        btnSalvarProcedimento.setText("Adicionar");
        btnCancelarEdicao.setVisible(false);
        btnCancelarEdicao.setManaged(false);
    }

    public void mostrar() { stage.showAndWait(); }
}
