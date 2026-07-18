package com.helizahair.ui.dialogs;

import com.helizahair.db.AgendamentoDAO;
import com.helizahair.db.FechamentoCaixaDAO;
import com.helizahair.model.Agendamento;
import com.helizahair.model.FechamentoCaixa;
import com.helizahair.model.Procedimento;
import com.helizahair.state.AppState;
import com.helizahair.util.DateUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class AppointmentDialog {

    private final AppState estado;
    private final Agendamento existente;
    private final Runnable aoSalvar;
    private final Stage stage = new Stage();

    private final TextField campoCliente = new TextField();
    private final ComboBox<Procedimento> comboProcedimento = new ComboBox<>();
    private final DatePicker campoData = new DatePicker();
    private final TextField campoHoraInicio = new TextField();
    private final TextField campoHoraFim = new TextField();
    private final TextField campoValor = new TextField();
    private final String statusAtual;

    public AppointmentDialog(AppState estado, LocalDate dataInicial, String horaInicial,
                              Agendamento existente, Runnable aoSalvar) {
        this.estado = estado;
        this.existente = existente;
        this.aoSalvar = aoSalvar;
        this.statusAtual = existente != null ? existente.getStatus() : "agendado";
        montarInterface(dataInicial, horaInicial);
    }

    private void montarInterface(LocalDate dataInicial, String horaInicial) {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(existente == null ? "Novo Agendamento" : "Editar Agendamento");

        VBox raiz = new VBox(12);
        raiz.getStyleClass().add("modal-conteudo");
        raiz.setPadding(new Insets(22));

        Label tituloModal = new Label(existente == null ? "Novo Agendamento" : "Editar Agendamento");
        tituloModal.getStyleClass().add("titulo-modal");

        campoCliente.setPromptText("Nome do cliente");
        campoCliente.getStyleClass().add("input-padrao");

        comboProcedimento.getItems().addAll(estado.getProcedimentos());
        comboProcedimento.setPromptText("Selecione um servi\u00E7o...");
        comboProcedimento.setMaxWidth(Double.MAX_VALUE);
        comboProcedimento.getStyleClass().add("input-padrao");
        comboProcedimento.setConverter(new StringConverter<>() {
            @Override
            public String toString(Procedimento p) {
                return p == null ? "" : String.format("%s (R$ %.2f - %dm)", p.getNome(), p.getPreco(), p.getDuracaoMin());
            }
            @Override
            public Procedimento fromString(String s) { return null; }
        });

        LocalDate dataBase = dataInicial != null ? dataInicial
                : (existente != null ? LocalDate.parse(existente.getData()) : LocalDate.now());
        campoData.setValue(dataBase);
        campoData.getStyleClass().add("input-padrao");

        campoHoraInicio.setText(horaInicial != null ? horaInicial
                : (existente != null ? existente.getHoraInicio() : "08:00"));
        campoHoraFim.setText(existente != null ? existente.getHoraFim()
                : DateUtil.somarMinutos(campoHoraInicio.getText(), 60));
        campoValor.setText(existente != null ? String.valueOf(existente.getValor()) : "");
        campoHoraInicio.getStyleClass().add("input-padrao");
        campoHoraFim.getStyleClass().add("input-padrao");
        campoValor.getStyleClass().add("input-padrao");

        if (existente != null) {
            campoCliente.setText(existente.getCliente());
            estado.buscarProcedimento(existente.getProcId()).ifPresent(comboProcedimento::setValue);
        }

        comboProcedimento.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoValor.setText(String.valueOf(novo.getPreco()));
                campoHoraFim.setText(DateUtil.somarMinutos(campoHoraInicio.getText(), novo.getDuracaoMin()));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.addRow(0, rotulo("Data"), campoData);
        grid.addRow(1, rotulo("In\u00EDcio"), campoHoraInicio);
        grid.addRow(2, rotulo("Fim"), campoHoraFim);
        grid.addRow(3, rotulo("Valor (R$)"), campoValor);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-secundario");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnSalvar = new Button("Salvar Agendamento");
        btnSalvar.getStyleClass().add("btn-primario");
        btnSalvar.setOnAction(e -> salvar());

        HBox botoes = new HBox(10, btnCancelar, btnSalvar);

        if (existente != null) {
            Button btnExcluir = new Button("Excluir");
            btnExcluir.getStyleClass().add("btn-perigo");
            btnExcluir.setOnAction(e -> excluir());
            HBox espacoBotoes = new HBox(btnExcluir);
            botoes.getChildren().add(0, espacoBotoes);
        }

        raiz.getChildren().addAll(
                tituloModal,
                rotulo("Nome do Cliente"), campoCliente,
                rotulo("Procedimento"), comboProcedimento,
                grid, botoes
        );

        stage.setScene(new Scene(raiz, 460, 580));
    }

    private Label rotulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("rotulo-campo");
        return l;
    }

    private void salvar() {
        try {
            String inicio = campoHoraInicio.getText().trim();
            String fim = campoHoraFim.getText().trim();

            if (DateUtil.horaParaMinutos(inicio) >= DateUtil.horaParaMinutos(fim)) {
                alerta("O t\u00E9rmino deve ser posterior ao in\u00EDcio.");
                return;
            }
            if (campoCliente.getText().isBlank()) {
                alerta("Informe o nome do cliente.");
                return;
            }

            Agendamento a = existente != null ? existente : new Agendamento();
            a.setCliente(campoCliente.getText().trim());
            a.setProcId(comboProcedimento.getValue() != null ? comboProcedimento.getValue().getId() : null);
            a.setData(DateUtil.formatar(campoData.getValue()));
            a.setHoraInicio(inicio);
            a.setHoraFim(fim);
            a.setValor(Double.parseDouble(campoValor.getText().replace(",", ".")));
            a.setStatus(statusAtual);

            AgendamentoDAO.salvar(a);
            estado.notificarMudanca();
            stage.close();
            if (aoSalvar != null) aoSalvar.run();
        } catch (NumberFormatException ex) {
            alerta("Valor inv\u00E1lido.");
        }
    }

    private void excluir() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir permanentemente este agendamento?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                AgendamentoDAO.excluir(existente.getId());
                estado.notificarMudanca();
                stage.close();
                if (aoSalvar != null) aoSalvar.run();
            }
        });
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public void mostrar() {
        if (existente == null) {
            String dataStr = DateUtil.formatar(campoData.getValue());
            FechamentoCaixa fech = FechamentoCaixaDAO.buscarPorData(dataStr);
            if (fech != null && fech.isFechado()) {
                alerta("Aviso: o caixa deste dia j\u00E1 est\u00E1 fechado.");
            }
        }
        stage.showAndWait();
    }
}
