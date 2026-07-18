package com.helizahair.ui.dialogs;

import com.helizahair.model.Agendamento;
import com.helizahair.model.Procedimento;
import com.helizahair.service.AgendamentoService;
import com.helizahair.service.RegraNegocioException;
import com.helizahair.state.AppState;
import com.helizahair.ui.Estilos;
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

    private static final String ID_PERSONALIZADO = "__personalizado__";

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
    private final Procedimento personalizado =
            new Procedimento(ID_PERSONALIZADO, "Personalizado / Outro", 60, 0, "gray");

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
        comboProcedimento.getItems().add(personalizado);
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
            comboProcedimento.setValue(
                    estado.buscarProcedimento(existente.getProcId()).orElse(personalizado)
            );
        }

        comboProcedimento.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo != null && !ID_PERSONALIZADO.equals(novo.getId())) {
                campoValor.setText(String.valueOf(novo.getPreco()));
                atualizarFimAutomatico(novo);
            } else if (novo != null) {
                campoValor.clear();
            }
        });
        campoHoraInicio.textProperty().addListener((obs, antigo, novo) -> {
            Procedimento procedimento = comboProcedimento.getValue();
            if (procedimento != null && !ID_PERSONALIZADO.equals(procedimento.getId())) {
                atualizarFimAutomatico(procedimento);
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

        Scene cena = new Scene(raiz, 460, 580);
        Estilos.aplicar(cena, estado.getTema());
        stage.setScene(cena);
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

            Agendamento a = existente != null ? existente : new Agendamento();
            a.setCliente(campoCliente.getText().trim());
            Procedimento selecionado = comboProcedimento.getValue();
            a.setProcId(selecionado == null || ID_PERSONALIZADO.equals(selecionado.getId())
                    ? null
                    : selecionado.getId());
            a.setData(DateUtil.formatar(campoData.getValue()));
            a.setHoraInicio(inicio);
            a.setHoraFim(fim);
            a.setValor(Double.parseDouble(campoValor.getText().replace(",", ".")));
            a.setStatus(statusAtual);

            AgendamentoService.salvar(a);
            estado.notificarMudanca();
            stage.close();
            if (aoSalvar != null) aoSalvar.run();
        } catch (RegraNegocioException ex) {
            alerta(ex.getMessage());
        } catch (NumberFormatException | NullPointerException ex) {
            alerta("Valor inv\u00E1lido.");
        }
    }

    private void excluir() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir permanentemente este agendamento?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    AgendamentoService.excluir(existente);
                    estado.notificarMudanca();
                    stage.close();
                    if (aoSalvar != null) aoSalvar.run();
                } catch (RegraNegocioException ex) {
                    alerta(ex.getMessage());
                }
            }
        });
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public void mostrar() {
        String data = existente != null
                ? existente.getData()
                : DateUtil.formatar(campoData.getValue());
        if (AgendamentoService.diaFechado(data)) {
            alerta("O caixa deste dia está fechado. Use Gerenciar Dia para atualizar ou reabrir o caixa.");
            return;
        }
        stage.showAndWait();
    }

    private void atualizarFimAutomatico(Procedimento procedimento) {
        String inicio = campoHoraInicio.getText();
        if (inicio != null && inicio.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) {
            campoHoraFim.setText(DateUtil.somarMinutos(inicio, procedimento.getDuracaoMin()));
        }
    }
}
