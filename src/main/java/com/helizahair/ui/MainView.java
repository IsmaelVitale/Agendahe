package com.helizahair.ui;

import com.helizahair.state.AppState;
import com.helizahair.ui.dialogs.AppointmentDialog;
import com.helizahair.ui.dialogs.CheckoutDialog;
import com.helizahair.ui.dialogs.ReportsDialog;
import com.helizahair.ui.dialogs.SettingsDialog;
import com.helizahair.util.DateUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainView {

    private final AppState estado;
    private final BorderPane raiz = new BorderPane();
    private final CalendarGrid grade;
    private Label rotuloMes;
    private Label totalSemanalLabel;
    private DatePicker seletorData;

    public MainView(AppState estado) {
        this.estado = estado;
        this.grade = new CalendarGrid(estado);
        raiz.getStyleClass().add("app-root");
        raiz.setLeft(construirSidebar());
        raiz.setCenter(construirCentro());
        estado.aoAlterar(this::atualizarCabecalho);
        atualizarCabecalho();
    }

    public BorderPane getRaiz() { return raiz; }

    private ScrollPane construirSidebar() {
        VBox sidebar = new VBox(22);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);
        sidebar.setPadding(new Insets(24));

        VBox logoBox = new VBox(2);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        ImageView logotipo = IdentidadeVisual.criarLogotipo(210);
        logotipo.getStyleClass().add("logo-marca");
        Label titulo = new Label(IdentidadeVisual.NOME_APLICACAO);
        titulo.getStyleClass().add("titulo-app");
        logoBox.getChildren().addAll(logotipo, titulo);

        Label lblData = new Label("IR PARA DATA");
        lblData.getStyleClass().add("rotulo-secao");
        seletorData = new DatePicker(estado.getDataAtual());
        seletorData.getStyleClass().add("input-padrao");
        seletorData.setMaxWidth(Double.MAX_VALUE);
        seletorData.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) estado.setDataAtual(novo);
        });

        Label lblLargura = new Label("LARGURA DAS COLUNAS");
        lblLargura.getStyleClass().add("rotulo-secao");
        Slider sliderLargura = new Slider(120, 400, 180);
        sliderLargura.valueProperty().addListener((obs, a, novo) -> grade.setLarguraColuna(novo.doubleValue()));

        VBox espaco = new VBox();
        VBox.setVgrow(espaco, Priority.ALWAYS);

        VBox cardResumo = new VBox(10);
        cardResumo.getStyleClass().add("card-resumo");
        cardResumo.setPadding(new Insets(16));
        Label lblResumo = new Label("\uD83D\uDCB0 Estimativa Semanal");
        lblResumo.getStyleClass().add("rotulo-resumo");
        totalSemanalLabel = new Label("R$ 0,00");
        totalSemanalLabel.getStyleClass().add("total-semanal");

        Button btnCaixa = new Button("\uD83E\uDDFE Fechar Caixa");
        btnCaixa.getStyleClass().add("btn-verde");
        btnCaixa.setMaxWidth(Double.MAX_VALUE);
        btnCaixa.setOnAction(e -> new CheckoutDialog(estado, null, grade::renderizar).mostrar());

        Button btnRelatorios = new Button("\uD83D\uDCCA Relat\u00F3rios");
        btnRelatorios.getStyleClass().add("btn-secundario");
        btnRelatorios.setMaxWidth(Double.MAX_VALUE);
        btnRelatorios.setOnAction(e -> new ReportsDialog(estado).mostrar());

        Button btnConfig = new Button("\u2699 Configura\u00E7\u00F5es");
        btnConfig.getStyleClass().add("btn-secundario");
        btnConfig.setMaxWidth(Double.MAX_VALUE);
        btnConfig.setOnAction(e -> new SettingsDialog(estado, grade::renderizar).mostrar());

        cardResumo.getChildren().addAll(lblResumo, totalSemanalLabel, btnCaixa, btnRelatorios, btnConfig);

        sidebar.getChildren().addAll(logoBox, new Separator(), lblData, seletorData,
                lblLargura, sliderLargura, espaco, cardResumo);

        grade.setAoAtualizarTotal(total -> totalSemanalLabel.setText(formatarMoeda(total)));
        ScrollPane rolagem = new ScrollPane(sidebar);
        rolagem.setFitToWidth(true);
        rolagem.setFitToHeight(true);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rolagem.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rolagem.setPrefViewportWidth(280);
        rolagem.getStyleClass().add("sidebar-scroll");

        return rolagem;
    }

    private BorderPane construirCentro() {
        BorderPane centro = new BorderPane();
        centro.getStyleClass().add("area-central");

        HBox header = new HBox(16);
        header.getStyleClass().add("header-topo");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));

        Button btnHoje = new Button("Hoje");
        btnHoje.getStyleClass().add("btn-secundario");
        btnHoje.setOnAction(e -> {
            estado.setDataAtual(LocalDate.now());
            seletorData.setValue(LocalDate.now());
        });

        Button btnAnterior = new Button("\u25C0");
        Button btnProximo = new Button("\u25B6");
        btnAnterior.getStyleClass().add("btn-nav-semana");
        btnProximo.getStyleClass().add("btn-nav-semana");
        btnAnterior.setOnAction(e -> mudarSemana(-1));
        btnProximo.setOnAction(e -> mudarSemana(1));
        HBox navSemana = new HBox(btnAnterior, btnProximo);
        navSemana.getStyleClass().add("nav-semana-box");

        rotuloMes = new Label();
        rotuloMes.getStyleClass().add("rotulo-mes");

        Region espacoHeader = new Region();
        HBox.setHgrow(espacoHeader, Priority.ALWAYS);

        Button btnNovo = new Button("\uFF0B Novo Agendamento");
        btnNovo.getStyleClass().add("btn-primario");
        btnNovo.setOnAction(e -> new AppointmentDialog(
                estado,
                estado.getDataAtual(),
                DateUtil.minutosParaHora(estado.getMinutoAbertura()),
                null,
                grade::renderizar
        ).mostrar());

        header.getChildren().addAll(btnHoje, navSemana, rotuloMes, espacoHeader, btnNovo);

        centro.setTop(header);
        centro.setCenter(grade.getNo());
        return centro;
    }

    private void mudarSemana(int offset) {
        LocalDate nova = estado.getDataAtual().plusWeeks(offset);
        estado.setDataAtual(nova);
        seletorData.setValue(nova);
    }

    private void atualizarCabecalho() {
        LocalDate inicioSemana = DateUtil.inicioDaSemana(estado.getDataAtual());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("pt", "BR"));
        String texto = inicioSemana.format(fmt);
        rotuloMes.setText(texto.substring(0, 1).toUpperCase() + texto.substring(1));
    }

    public static String formatarMoeda(double valor) {
        return String.format(new Locale("pt", "BR"), "R$ %,.2f", valor);
    }
}
