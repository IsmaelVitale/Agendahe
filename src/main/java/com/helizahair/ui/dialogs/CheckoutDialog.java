package com.helizahair.ui.dialogs;

import com.helizahair.db.AgendamentoDAO;
import com.helizahair.db.FechamentoCaixaDAO;
import com.helizahair.model.Agendamento;
import com.helizahair.model.FechamentoCaixa;
import com.helizahair.model.Procedimento;
import com.helizahair.state.AppState;
import com.helizahair.ui.Estilos;
import com.helizahair.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutDialog {

    private final AppState estado;
    private final Runnable aoFechar;
    private String dataAlvo;
    private final Stage stage = new Stage();
    private final BorderPane raiz = new BorderPane();

    private final Map<Integer, ComboBox<String>> comboStatus = new HashMap<>();
    private final Map<Integer, Double> precoPorId = new HashMap<>();
    private final TextField campoExtras = new TextField("0.00");
    private final Label totalLabel = new Label("R$ 0,00");

    public CheckoutDialog(AppState estado, String dataAlvo, Runnable aoFechar) {
        this.estado = estado;
        this.dataAlvo = dataAlvo;
        this.aoFechar = aoFechar;
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Gerenciar Caixa");
        raiz.getStyleClass().add("modal-conteudo");
        Scene cena = new Scene(raiz, 760, 620);
        Estilos.aplicar(cena, estado.getTema());
        stage.setScene(cena);
        stage.setOnHidden(e -> { if (aoFechar != null) aoFechar.run(); });
        campoExtras.textProperty().addListener((obs, anterior, atual) -> recalcularTotal());
        if (dataAlvo == null) mostrarSelecao(); else mostrarDetalhe(dataAlvo);
    }

    public void mostrar() { stage.showAndWait(); }

    private void mostrarSelecao() {
        VBox conteudo = new VBox(14);
        conteudo.setPadding(new Insets(24));

        Label titulo = new Label("Dias com agendamentos pendentes");
        titulo.getStyleClass().add("titulo-modal");
        Label subtitulo = new Label("Selecione um dia para validar o faturamento.");
        subtitulo.getStyleClass().add("rotulo-secao");

        List<String> datas = AgendamentoDAO.listarDatasComAgendamentos();
        List<String> abertos = datas.stream()
                .filter(d -> {
                    FechamentoCaixa f = FechamentoCaixaDAO.buscarPorData(d);
                    return f == null || !f.isFechado();
                })
                .sorted().toList();

        VBox lista = new VBox(8);
        if (abertos.isEmpty()) {
            Label vazio = new Label("Tudo em dia! Nenhum caixa pendente.");
            vazio.getStyleClass().add("rotulo-secao");
            lista.getChildren().add(vazio);
        } else {
            for (String d : abertos) {
                int qtd = AgendamentoDAO.listarPorData(d).size();
                Button btn = new Button(DateUtil.formatarDataBR(d) + "   \u2014   " + qtd + " agendamento(s)");
                btn.getStyleClass().add("item-dia-aberto");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> mostrarDetalhe(d));
                lista.getChildren().add(btn);
            }
        }
        conteudo.getChildren().addAll(titulo, subtitulo, lista);
        raiz.setCenter(new ScrollPane(conteudo));
    }

    private void mostrarDetalhe(String data) {
        this.dataAlvo = data;
        comboStatus.clear();
        precoPorId.clear();

        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24));

        HBox topo = new HBox(14);
        topo.setAlignment(Pos.CENTER_LEFT);
        Button btnVoltar = new Button("\u2190 Voltar");
        btnVoltar.getStyleClass().add("btn-secundario");
        btnVoltar.setOnAction(e -> mostrarSelecao());
        Label lblData = new Label("Dia: " + DateUtil.formatarDataBR(data));
        lblData.getStyleClass().add("titulo-modal");
        topo.getChildren().addAll(btnVoltar, lblData);

        FechamentoCaixa ledger = FechamentoCaixaDAO.buscarPorData(data);
        boolean fechado = ledger != null && ledger.isFechado();

        VBox listaAgendamentos = new VBox(6);
        listaAgendamentos.getStyleClass().add("lista-checkout");
        List<Agendamento> ags = AgendamentoDAO.listarPorData(data);
        ags.sort(Comparator.comparing(Agendamento::getHoraInicio));

        if (ags.isEmpty()) {
            listaAgendamentos.getChildren().add(new Label("Nenhum agendamento."));
        }
        for (Agendamento a : ags) {
            Procedimento p = estado.buscarProcedimento(a.getProcId()).orElse(null);
            String nomeProc = p != null ? p.getNome() : "Outro";

            HBox linha = new HBox(14);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.getStyleClass().add("linha-checkout");
            linha.setPadding(new Insets(8, 10, 8, 10));

            Label hora = new Label(a.getHoraInicio());
            hora.setPrefWidth(55);
            Label cliente = new Label(a.getCliente());
            cliente.setPrefWidth(150);
            Label servico = new Label(nomeProc);
            servico.setPrefWidth(150);
            Label valor = new Label(String.format("R$ %.2f", a.getValor()));
            valor.setPrefWidth(90);

            String statusPadrao = (a.getStatus().equals("agendado") && !fechado) ? "concluido" : a.getStatus();
            ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(
                    "agendado", "concluido", "cancelado", "falta"));
            combo.setValue(statusPadrao);
            combo.valueProperty().addListener((obs, an, no) -> recalcularTotal());
            comboStatus.put(a.getId(), combo);
            precoPorId.put(a.getId(), a.getValor());

            linha.getChildren().addAll(hora, cliente, servico, valor, combo);
            listaAgendamentos.getChildren().add(linha);
        }

        campoExtras.setText(String.format("%.2f", fechado ? ledger.getExtras() : 0.0));
        campoExtras.getStyleClass().add("input-padrao");

        totalLabel.getStyleClass().add("total-checkout");

        VBox extrasBox = new VBox(4, rotuloSecao("Entradas Extras (R$)"), campoExtras);
        VBox totalBox = new VBox(4, rotuloSecao("Faturamento Validado"), totalLabel);
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox rodapeValores = new HBox(20, extrasBox, espaco, totalBox);
        rodapeValores.getStyleClass().add("card-resumo");
        rodapeValores.setPadding(new Insets(16));

        Button btnSalvar = new Button("Salvar Fechamento");
        btnSalvar.getStyleClass().add("btn-verde");
        btnSalvar.setOnAction(e -> salvarFechamento());

        HBox acoes = new HBox(10);
        if (fechado) {
            bloquearCampos(true);
            Button btnReabrir = new Button("Reabrir Caixa para Edição");
            btnReabrir.getStyleClass().add("btn-azul");
            btnReabrir.setOnAction(e -> reabrirCaixa());
            acoes.getChildren().add(btnReabrir);
        } else {
            acoes.getChildren().add(btnSalvar);
        }

        conteudo.getChildren().addAll(topo, listaAgendamentos, rodapeValores, acoes);
        raiz.setCenter(new ScrollPane(conteudo));
        recalcularTotal();
    }

    private Label rotuloSecao(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("rotulo-secao");
        return l;
    }

    private void recalcularTotal() {
        double total = 0;
        try { total += Double.parseDouble(campoExtras.getText().replace(",", ".")); } catch (Exception ignored) {}
        for (var entry : comboStatus.entrySet()) {
            if ("concluido".equals(entry.getValue().getValue())) {
                total += precoPorId.getOrDefault(entry.getKey(), 0.0);
            }
        }
        totalLabel.setText(String.format("R$ %.2f", total));
    }

    private void salvarFechamento() {
        double extras;
        try { extras = Double.parseDouble(campoExtras.getText().replace(",", ".")); } catch (Exception e) { extras = 0; }

        double total = extras;
        int concluidos = 0;
        for (var entry : comboStatus.entrySet()) {
            String status = entry.getValue().getValue();
            AgendamentoDAO.atualizarStatus(entry.getKey(), status);
            if ("concluido".equals(status)) {
                total += precoPorId.getOrDefault(entry.getKey(), 0.0);
                concluidos++;
            }
        }

        FechamentoCaixa f = new FechamentoCaixa(dataAlvo, true, total, extras, concluidos);
        FechamentoCaixaDAO.salvar(f);
        estado.notificarMudanca();
        stage.close();

        Alert ok = new Alert(Alert.AlertType.INFORMATION,
                "Caixa de " + DateUtil.formatarDataBR(dataAlvo) + " validado: R$ " + String.format("%.2f", total),
                ButtonType.OK);
        ok.showAndWait();
    }

    private void bloquearCampos(boolean bloquear) {
        campoExtras.setDisable(bloquear);
        comboStatus.values().forEach(combo -> combo.setDisable(bloquear));
    }

    private void reabrirCaixa() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Reabrir este caixa? O dia voltará a aceitar alterações na agenda.",
                ButtonType.YES, ButtonType.NO);
        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.YES) {
                FechamentoCaixaDAO.reabrir(dataAlvo);
                estado.notificarMudanca();
                mostrarDetalhe(dataAlvo);
            }
        });
    }
}
