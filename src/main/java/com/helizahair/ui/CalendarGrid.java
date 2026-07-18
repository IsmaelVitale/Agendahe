package com.helizahair.ui;

import com.helizahair.db.AgendamentoDAO;
import com.helizahair.db.FechamentoCaixaDAO;
import com.helizahair.model.Agendamento;
import com.helizahair.model.Procedimento;
import com.helizahair.state.AppState;
import com.helizahair.ui.dialogs.AppointmentDialog;
import com.helizahair.ui.dialogs.CheckoutDialog;
import com.helizahair.util.DateUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.DoubleConsumer;

public class CalendarGrid {

    private static final double SLOT_HEIGHT = 32;
    private static final double PX_POR_MINUTO = SLOT_HEIGHT / 30.0;
    private static final double COL_HORA_LARGURA = 60;

    private final AppState estado;
    private double larguraColuna = 180;
    private final ScrollPane scroll = new ScrollPane();
    private final VBox conteudo = new VBox();
    private DoubleConsumer aoAtualizarTotal = t -> {};

    public CalendarGrid(AppState estado) {
        this.estado = estado;
        scroll.setContent(conteudo);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.getStyleClass().add("area-calendario");
        estado.aoAlterar(this::renderizar);
        renderizar();
    }

    public Node getNo() { return scroll; }

    public void setLarguraColuna(double largura) {
        this.larguraColuna = largura;
        renderizar();
    }

    public void setAoAtualizarTotal(DoubleConsumer callback) {
        this.aoAtualizarTotal = callback;
    }

    public void renderizar() {
        conteudo.getChildren().clear();

        LocalDate inicioSemana = DateUtil.inicioDaSemana(estado.getDataAtual());
        List<LocalDate> dias = new ArrayList<>();
        for (int i = 0; i < 7; i++) dias.add(inicioSemana.plusDays(i));

        conteudo.getChildren().add(construirCabecalhoDias(dias));

        HBox corpo = new HBox();
        corpo.getChildren().add(construirColunaHoras());

        double totalSemanal = 0;
        Map<LocalDate, List<Agendamento>> porDia = new LinkedHashMap<>();
        for (LocalDate dia : dias) {
            List<Agendamento> ags = AgendamentoDAO.listarPorData(DateUtil.formatar(dia));
            porDia.put(dia, ags);
            for (Agendamento a : ags) {
                if (a.getStatus().equals("agendado") || a.getStatus().equals("concluido")) {
                    totalSemanal += a.getValor();
                }
            }
        }
        for (LocalDate dia : dias) {
            corpo.getChildren().add(construirColunaDia(dia, porDia.get(dia)));
        }

        conteudo.getChildren().add(corpo);
        aoAtualizarTotal.accept(totalSemanal);
    }

    private HBox construirCabecalhoDias(List<LocalDate> dias) {
        HBox header = new HBox();
        header.getStyleClass().add("cabecalho-dias");

        Label rotuloHora = new Label("Hora");
        rotuloHora.setPrefWidth(COL_HORA_LARGURA);
        rotuloHora.setMinWidth(COL_HORA_LARGURA);
        rotuloHora.setAlignment(Pos.CENTER);
        rotuloHora.getStyleClass().add("celula-cabecalho-hora");
        header.getChildren().add(rotuloHora);

        for (LocalDate dia : dias) {
            String dataStr = DateUtil.formatar(dia);
            boolean isHoje = dia.equals(LocalDate.now());
            boolean fechado = Optional.ofNullable(FechamentoCaixaDAO.buscarPorData(dataStr))
                    .map(com.helizahair.model.FechamentoCaixa::isFechado).orElse(false);

            VBox celula = new VBox(2);
            celula.setAlignment(Pos.CENTER);
            celula.setPrefWidth(larguraColuna);
            celula.setMinWidth(larguraColuna);
            celula.setPadding(new Insets(8, 4, 8, 4));
            celula.getStyleClass().add("celula-cabecalho-dia");
            if (isHoje) celula.getStyleClass().add("celula-cabecalho-hoje");

            String nomeDia = dia.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"))
                    .toUpperCase().replace(".", "");
            Label lblDiaSemana = new Label(nomeDia + (fechado ? "  \uD83D\uDD12" : ""));
            lblDiaSemana.getStyleClass().add("rotulo-dia-semana");

            Label lblNumero = new Label(String.valueOf(dia.getDayOfMonth()));
            lblNumero.getStyleClass().add("rotulo-numero-dia");

            Button btnGerenciar = new Button(fechado ? "Ver Caixa Fechado" : "Gerenciar Dia");
            btnGerenciar.getStyleClass().add(fechado ? "btn-gerenciar-fechado" : "btn-gerenciar-aberto");
            btnGerenciar.setVisible(false);
            btnGerenciar.setManaged(false);
            btnGerenciar.setOnAction(e -> new CheckoutDialog(estado, dataStr, this::renderizar).mostrar());

            celula.setOnMouseEntered(e -> { btnGerenciar.setVisible(true); btnGerenciar.setManaged(true); });
            celula.setOnMouseExited(e -> { btnGerenciar.setVisible(false); btnGerenciar.setManaged(false); });

            celula.getChildren().addAll(lblDiaSemana, lblNumero, btnGerenciar);
            header.getChildren().add(celula);
        }
        return header;
    }

    private VBox construirColunaHoras() {
        VBox coluna = new VBox();
        coluna.getStyleClass().add("coluna-horas");
        coluna.setPrefWidth(COL_HORA_LARGURA);
        coluna.setMinWidth(COL_HORA_LARGURA);
        for (int h = estado.getHoraAbertura(); h < estado.getHoraFechamento(); h++) {
            for (String m : new String[]{"00", "30"}) {
                Label lbl = new Label(String.format("%02d:%s", h, m));
                lbl.getStyleClass().add(m.equals("00") ? "rotulo-hora-cheia" : "rotulo-hora-meia");
                lbl.setPrefSize(COL_HORA_LARGURA, SLOT_HEIGHT);
                lbl.setMinHeight(SLOT_HEIGHT);
                lbl.setAlignment(Pos.TOP_RIGHT);
                lbl.setPadding(new Insets(2, 6, 0, 0));
                coluna.getChildren().add(lbl);
            }
        }
        return coluna;
    }

    private Pane construirColunaDia(LocalDate dia, List<Agendamento> agendamentos) {
        String dataStr = DateUtil.formatar(dia);
        int slots = (estado.getHoraFechamento() - estado.getHoraAbertura()) * 2;
        double alturaTotal = Math.max(slots, 1) * SLOT_HEIGHT;

        Pane pane = new Pane();
        pane.getStyleClass().add("coluna-dia");
        pane.setPrefSize(larguraColuna, alturaTotal);
        pane.setMinSize(larguraColuna, alturaTotal);

        boolean fechado = Optional.ofNullable(FechamentoCaixaDAO.buscarPorData(dataStr))
                .map(com.helizahair.model.FechamentoCaixa::isFechado).orElse(false);
        if (fechado) pane.getStyleClass().add("coluna-dia-fechada");

        int idx = 0;
        for (int h = estado.getHoraAbertura(); h < estado.getHoraFechamento(); h++) {
            for (String m : new String[]{"00", "30"}) {
                String horaStr = String.format("%02d:%s", h, m);
                Rectangle slot = new Rectangle(larguraColuna, SLOT_HEIGHT);
                slot.setLayoutY(idx * SLOT_HEIGHT);
                slot.getStyleClass().add("slot-horario");

                if (m.equals("00")) {
                    slot.getStyleClass().add("slot-hora-cheia");
                }
                slot.setOnMouseClicked(e -> new AppointmentDialog(estado, dia, horaStr, null, this::renderizar).mostrar());
                pane.getChildren().add(slot);
                idx++;
            }
        }

        List<Agendamento> lista = new ArrayList<>(agendamentos);
        lista.sort(Comparator.comparingInt(a -> DateUtil.horaParaMinutos(a.getHoraInicio())));
        List<List<Agendamento>> grupos = agruparConflitos(lista);
        int gridInicioMin = estado.getHoraAbertura() * 60;
        int gridFimMin = estado.getHoraFechamento() * 60;

        for (List<Agendamento> grupo : grupos) {
            int cols = grupo.size();
            for (int i = 0; i < grupo.size(); i++) {
                Agendamento a = grupo.get(i);
                int inicioMin = DateUtil.horaParaMinutos(a.getHoraInicio());
                int fimMin = DateUtil.horaParaMinutos(a.getHoraFim());
                if (inicioMin >= gridFimMin || fimMin <= gridInicioMin) continue;

                int visInicio = Math.max(inicioMin, gridInicioMin);
                int visFim = Math.min(fimMin, gridFimMin);

                double topo = (visInicio - gridInicioMin) * PX_POR_MINUTO;
                double altura = Math.max((visFim - visInicio) * PX_POR_MINUTO, 16);
                double largura = larguraColuna / cols - 4;
                double esquerda = (larguraColuna / cols) * i + 2;

                pane.getChildren().add(construirCard(a, topo, altura, largura, esquerda));
            }
        }
        return pane;
    }

    private List<List<Agendamento>> agruparConflitos(List<Agendamento> agendamentos) {
        List<List<Agendamento>> grupos = new ArrayList<>();
        List<Agendamento> atual = new ArrayList<>();
        for (Agendamento a : agendamentos) {
            int inicio = DateUtil.horaParaMinutos(a.getHoraInicio());
            boolean sobrepoe = atual.stream().anyMatch(item -> inicio < DateUtil.horaParaMinutos(item.getHoraFim()));
            if (sobrepoe || atual.isEmpty()) {
                atual.add(a);
            } else {
                grupos.add(atual);
                atual = new ArrayList<>(List.of(a));
            }
        }
        if (!atual.isEmpty()) grupos.add(atual);
        return grupos;
    }

    private VBox construirCard(Agendamento a, double topo, double altura, double largura, double esquerda) {
        Procedimento proc = estado.buscarProcedimento(a.getProcId()).orElse(null);
        String corChave = proc != null ? proc.getCor() : "gray";
        String nomeProc = proc != null ? proc.getNome() : "Personalizado";

        VBox card = new VBox(1);
        card.setLayoutX(esquerda);
        card.setLayoutY(topo);
        card.setPrefSize(largura, altura);
        card.setMaxSize(largura, altura);
        card.setPadding(new Insets(3, 6, 3, 6));
        card.getStyleClass().add("card-agendamento");

        String estilo;
        if (a.getStatus().equals("cancelado") || a.getStatus().equals("falta")) {
            estilo = "-fx-background-color: #6b7280; -fx-background-radius: 6; -fx-opacity: 0.65;";
        } else if (a.getStatus().equals("concluido")) {
            estilo = "-fx-background-color: " + CorPaleta.fundo(corChave) + "; -fx-background-radius: 6; " +
                     "-fx-border-color: #22c55e transparent transparent transparent; -fx-border-width: 0 0 0 4; " +
                     "-fx-background-insets: 0, 0 0 0 4;";
        } else {
            estilo = "-fx-background-color: " + CorPaleta.fundo(corChave) + "; -fx-background-radius: 6;";
        }
        card.setStyle(estilo);

        Label nome = new Label(a.getCliente());
        nome.getStyleClass().add("card-nome-cliente");
        Label servico = new Label(nomeProc);
        servico.getStyleClass().add("card-nome-servico");
        Label horario = new Label(a.getHoraInicio() + " - " + a.getHoraFim());
        horario.getStyleClass().add("card-horario");

        card.getChildren().addAll(nome, servico, horario);
        card.setOnMouseClicked(e -> {
            e.consume();
            new AppointmentDialog(estado, null, null, a, this::renderizar).mostrar();
        });
        return card;
    }
}
