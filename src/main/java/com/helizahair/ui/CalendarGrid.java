package com.helizahair.ui;

import com.helizahair.db.AgendamentoDAO;
import com.helizahair.db.FechamentoCaixaDAO;
import com.helizahair.model.Agendamento;
import com.helizahair.model.Procedimento;
import com.helizahair.service.AgendamentoService;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleConsumer;

public class CalendarGrid {

    private static final double SLOT_HEIGHT = 45;
    private static final double PX_POR_MINUTO = SLOT_HEIGHT / 30.0;
    private static final double COL_HORA_LARGURA = 60;
    private static final double CABECALHO_ALTURA = 70;
    private static final double AREA_ATALHO = 32;

    private final AppState estado;
    private double larguraColuna = 180;
    private final BorderPane raiz = new BorderPane();
    private final ScrollPane scrollCabecalho = new ScrollPane();
    private final ScrollPane scrollHoras = new ScrollPane();
    private final ScrollPane scrollDias = new ScrollPane();
    private DoubleConsumer aoAtualizarTotal = total -> {};

    public CalendarGrid(AppState estado) {
        this.estado = estado;
        configurarEstrutura();
        estado.aoAlterar(this::renderizar);
        renderizar();
    }

    private void configurarEstrutura() {
        raiz.getStyleClass().add("grade-calendario");

        scrollCabecalho.getStyleClass().add("scroll-cabecalho");
        scrollCabecalho.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCabecalho.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCabecalho.setFitToHeight(true);
        scrollCabecalho.setMinHeight(CABECALHO_ALTURA);
        scrollCabecalho.setPrefHeight(CABECALHO_ALTURA);
        scrollCabecalho.setMaxHeight(CABECALHO_ALTURA);

        scrollHoras.getStyleClass().add("scroll-horas");
        scrollHoras.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollHoras.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollHoras.setFitToWidth(true);
        scrollHoras.setPrefWidth(COL_HORA_LARGURA);
        scrollHoras.setMinWidth(COL_HORA_LARGURA);
        scrollHoras.setMaxWidth(COL_HORA_LARGURA);

        scrollDias.getStyleClass().add("area-calendario");
        scrollDias.setFitToWidth(false);
        scrollDias.setFitToHeight(false);

        scrollCabecalho.hvalueProperty().bind(scrollDias.hvalueProperty());
        scrollHoras.vvalueProperty().bind(scrollDias.vvalueProperty());

        Label rotuloHora = new Label("Hora");
        rotuloHora.getStyleClass().add("celula-cabecalho-hora");
        rotuloHora.setAlignment(Pos.CENTER);
        rotuloHora.setMinSize(COL_HORA_LARGURA, CABECALHO_ALTURA);
        rotuloHora.setPrefSize(COL_HORA_LARGURA, CABECALHO_ALTURA);

        BorderPane cabecalho = new BorderPane();
        cabecalho.getStyleClass().add("cabecalho-dias");
        cabecalho.setLeft(rotuloHora);
        cabecalho.setCenter(scrollCabecalho);

        BorderPane corpo = new BorderPane();
        corpo.setLeft(scrollHoras);
        corpo.setCenter(scrollDias);

        raiz.setTop(cabecalho);
        raiz.setCenter(corpo);
    }

    public Node getNo() {
        return raiz;
    }

    public void setLarguraColuna(double largura) {
        this.larguraColuna = largura;
        renderizar();
    }

    public void setAoAtualizarTotal(DoubleConsumer callback) {
        this.aoAtualizarTotal = callback;
        renderizar();
    }

    public void renderizar() {
        LocalDate inicioSemana = DateUtil.inicioDaSemana(estado.getDataAtual());
        List<LocalDate> dias = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dias.add(inicioSemana.plusDays(i));
        }

        scrollCabecalho.setContent(construirCabecalhoDias(dias));
        scrollHoras.setContent(construirColunaHoras());

        double totalSemanal = 0;
        Map<LocalDate, List<Agendamento>> porDia = new LinkedHashMap<>();
        for (LocalDate dia : dias) {
            List<Agendamento> agendamentos = AgendamentoDAO.listarPorData(DateUtil.formatar(dia));
            porDia.put(dia, agendamentos);
            for (Agendamento agendamento : agendamentos) {
                if ("agendado".equals(agendamento.getStatus()) || "concluido".equals(agendamento.getStatus())) {
                    totalSemanal += agendamento.getValor();
                }
            }
        }

        HBox colunas = new HBox();
        for (LocalDate dia : dias) {
            colunas.getChildren().add(construirColunaDia(dia, porDia.get(dia)));
        }
        scrollDias.setContent(colunas);
        aoAtualizarTotal.accept(totalSemanal);
    }

    private HBox construirCabecalhoDias(List<LocalDate> dias) {
        HBox header = new HBox();
        for (LocalDate dia : dias) {
            String data = DateUtil.formatar(dia);
            boolean hoje = dia.equals(LocalDate.now());
            boolean fechado = AgendamentoService.diaFechado(data);

            StackPane celula = new StackPane();
            celula.setPrefSize(larguraColuna, CABECALHO_ALTURA);
            celula.setMinSize(larguraColuna, CABECALHO_ALTURA);
            celula.getStyleClass().add("celula-cabecalho-dia");
            if (hoje) {
                celula.getStyleClass().add("celula-cabecalho-hoje");
            }

            String nomeDia = dia.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"))
                    .toUpperCase()
                    .replace(".", "");
            Label lblDiaSemana = new Label(nomeDia + (fechado ? "  \uD83D\uDD12" : ""));
            lblDiaSemana.getStyleClass().add("rotulo-dia-semana");
            Label lblNumero = new Label(String.valueOf(dia.getDayOfMonth()));
            lblNumero.getStyleClass().add("rotulo-numero-dia");
            VBox textos = new VBox(2, lblDiaSemana, lblNumero);
            textos.setAlignment(Pos.CENTER);

            Button btnGerenciar = new Button(fechado ? "Ver Caixa Fechado" : "Gerenciar Dia");
            btnGerenciar.getStyleClass().add(fechado ? "btn-gerenciar-fechado" : "btn-gerenciar-aberto");
            btnGerenciar.setVisible(false);
            btnGerenciar.setOnAction(evento -> {
                evento.consume();
                new CheckoutDialog(estado, data, this::renderizar).mostrar();
            });

            celula.setOnMouseEntered(evento -> btnGerenciar.setVisible(true));
            celula.setOnMouseExited(evento -> btnGerenciar.setVisible(false));
            celula.getChildren().addAll(textos, btnGerenciar);
            header.getChildren().add(celula);
        }
        return header;
    }

    private VBox construirColunaHoras() {
        VBox coluna = new VBox();
        coluna.getStyleClass().add("coluna-horas");
        for (int minuto = estado.getMinutoAbertura(); minuto < estado.getMinutoFechamento(); minuto += 30) {
            Label label = new Label(DateUtil.minutosParaHora(minuto));
            label.getStyleClass().add(minuto % 60 == 0 ? "rotulo-hora-cheia" : "rotulo-hora-meia");
            label.setPrefSize(COL_HORA_LARGURA, SLOT_HEIGHT);
            label.setMinSize(COL_HORA_LARGURA, SLOT_HEIGHT);
            label.setAlignment(Pos.TOP_RIGHT);
            label.setPadding(new Insets(2, 6, 0, 0));
            coluna.getChildren().add(label);
        }
        return coluna;
    }

    private Pane construirColunaDia(LocalDate dia, List<Agendamento> agendamentos) {
        String data = DateUtil.formatar(dia);
        int slots = Math.max((estado.getMinutoFechamento() - estado.getMinutoAbertura()) / 30, 1);
        double alturaTotal = slots * SLOT_HEIGHT;
        boolean fechado = AgendamentoService.diaFechado(data);

        Pane coluna = new Pane();
        coluna.getStyleClass().add("coluna-dia");
        coluna.setPrefSize(larguraColuna, alturaTotal);
        coluna.setMinSize(larguraColuna, alturaTotal);
        if (fechado) {
            coluna.getStyleClass().add("coluna-dia-fechada");
        }

        int indice = 0;
        for (int minuto = estado.getMinutoAbertura(); minuto < estado.getMinutoFechamento(); minuto += 30) {
            String hora = DateUtil.minutosParaHora(minuto);
            StackPane slot = construirSlot(dia, data, hora, fechado, indice * SLOT_HEIGHT);
            coluna.getChildren().add(slot);
            indice++;
        }

        List<Agendamento> lista = new ArrayList<>(agendamentos);
        lista.sort(Comparator.comparingInt(item -> DateUtil.horaParaMinutos(item.getHoraInicio())));
        List<List<Agendamento>> grupos = agruparConflitos(lista);
        int inicioGrade = estado.getMinutoAbertura();
        int fimGrade = estado.getMinutoFechamento();
        double areaCards = Math.max(larguraColuna - AREA_ATALHO, 60);

        for (List<Agendamento> grupo : grupos) {
            int quantidadeColunas = grupo.size();
            for (int i = 0; i < grupo.size(); i++) {
                Agendamento agendamento = grupo.get(i);
                int inicio = DateUtil.horaParaMinutos(agendamento.getHoraInicio());
                int fim = DateUtil.horaParaMinutos(agendamento.getHoraFim());
                if (inicio >= fimGrade || fim <= inicioGrade) {
                    continue;
                }

                int inicioVisivel = Math.max(inicio, inicioGrade);
                int fimVisivel = Math.min(fim, fimGrade);
                double topo = (inicioVisivel - inicioGrade) * PX_POR_MINUTO;
                double altura = Math.max((fimVisivel - inicioVisivel) * PX_POR_MINUTO, 16);
                double largura = areaCards / quantidadeColunas - 4;
                double esquerda = (areaCards / quantidadeColunas) * i + 2;
                coluna.getChildren().add(construirCard(agendamento, topo, altura, largura, esquerda));
            }
        }
        return coluna;
    }

    private StackPane construirSlot(LocalDate dia, String data, String hora, boolean fechado, double topo) {
        StackPane slot = new StackPane();
        slot.setLayoutY(topo);
        slot.setPrefSize(larguraColuna, SLOT_HEIGHT);
        slot.setMinSize(larguraColuna, SLOT_HEIGHT);
        slot.setMaxSize(larguraColuna, SLOT_HEIGHT);
        slot.getStyleClass().add("slot-horario");
        if (DateUtil.horaParaMinutos(hora) % 60 == 0) {
            slot.getStyleClass().add("slot-hora-cheia");
        }

        Button atalho = new Button("+");
        atalho.getStyleClass().add("slot-add-btn");
        atalho.setVisible(!fechado);
        StackPane.setAlignment(atalho, Pos.CENTER_RIGHT);
        StackPane.setMargin(atalho, new Insets(0, 3, 0, 0));
        atalho.setOnAction(evento -> {
            evento.consume();
            new AppointmentDialog(estado, dia, hora, null, this::renderizar).mostrar();
        });

        slot.setOnMouseClicked(evento -> {
            if (evento.getTarget() == atalho) {
                return;
            }
            if (fechado) {
                new CheckoutDialog(estado, data, this::renderizar).mostrar();
            } else {
                new AppointmentDialog(estado, dia, hora, null, this::renderizar).mostrar();
            }
        });
        slot.getChildren().add(atalho);
        return slot;
    }

    private List<List<Agendamento>> agruparConflitos(List<Agendamento> agendamentos) {
        List<List<Agendamento>> grupos = new ArrayList<>();
        List<Agendamento> atual = new ArrayList<>();
        int maiorFimDoGrupo = -1;
        for (Agendamento agendamento : agendamentos) {
            int inicio = DateUtil.horaParaMinutos(agendamento.getHoraInicio());
            int fim = DateUtil.horaParaMinutos(agendamento.getHoraFim());
            if (!atual.isEmpty() && inicio >= maiorFimDoGrupo) {
                grupos.add(atual);
                atual = new ArrayList<>();
                maiorFimDoGrupo = -1;
            }
            atual.add(agendamento);
            maiorFimDoGrupo = Math.max(maiorFimDoGrupo, fim);
        }
        if (!atual.isEmpty()) {
            grupos.add(atual);
        }
        return grupos;
    }

    private VBox construirCard(Agendamento agendamento, double topo, double altura,
                                double largura, double esquerda) {
        Procedimento procedimento = estado.buscarProcedimento(agendamento.getProcId()).orElse(null);
        String cor = procedimento != null ? procedimento.getCor() : "gray";
        String nomeProcedimento = procedimento != null ? procedimento.getNome() : "Personalizado";

        VBox card = new VBox(1);
        card.setLayoutX(esquerda);
        card.setLayoutY(topo);
        card.setPrefSize(largura, altura);
        card.setMaxSize(largura, altura);
        card.setPadding(new Insets(3, 6, 3, 6));
        card.getStyleClass().add("card-agendamento");

        String estilo;
        if ("cancelado".equals(agendamento.getStatus()) || "falta".equals(agendamento.getStatus())) {
            estilo = "-fx-background-color: #6b7280; -fx-background-radius: 6; -fx-opacity: 0.65;";
        } else if ("concluido".equals(agendamento.getStatus())) {
            estilo = "-fx-background-color: " + CorPaleta.fundo(cor) + "; -fx-background-radius: 6; "
                    + "-fx-border-color: #22c55e transparent transparent transparent; "
                    + "-fx-border-width: 0 0 0 4;";
        } else {
            estilo = "-fx-background-color: " + CorPaleta.fundo(cor) + "; -fx-background-radius: 6;";
        }
        card.setStyle(estilo);

        Label nome = new Label(agendamento.getCliente());
        nome.getStyleClass().add("card-nome-cliente");
        Label servico = new Label(nomeProcedimento);
        servico.getStyleClass().add("card-nome-servico");
        Label horario = new Label(agendamento.getHoraInicio() + " - " + agendamento.getHoraFim());
        horario.getStyleClass().add("card-horario");
        card.getChildren().addAll(nome, servico, horario);

        card.setOnMouseClicked(evento -> {
            evento.consume();
            if (AgendamentoService.diaFechado(agendamento.getData())) {
                new CheckoutDialog(estado, agendamento.getData(), this::renderizar).mostrar();
            } else {
                new AppointmentDialog(estado, null, null, agendamento, this::renderizar).mostrar();
            }
        });
        return card;
    }
}
