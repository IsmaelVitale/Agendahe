package com.helizahair.state;

import com.helizahair.db.ConfigDAO;
import com.helizahair.db.ProcedimentoDAO;
import com.helizahair.model.Procedimento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Estado central da aplicacao. Equivale as variaveis globais (currentDate,
 * currentTheme, workingStartHour, workingEndHour, procedures) do agenda.html.
 * Componentes de UI se inscrevem via aoAlterar() e sao notificados a cada mudanca,
 * de forma parecida com as chamadas renderWeek() espalhadas pelo JS original.
 */
public class AppState {

    private LocalDate dataAtual = LocalDate.now();
    private String tema = ConfigDAO.get("tema", "light");
    private int horaAbertura = Integer.parseInt(ConfigDAO.get("hora_abertura", "8"));
    private int horaFechamento = Integer.parseInt(ConfigDAO.get("hora_fechamento", "20"));
    private List<Procedimento> procedimentos = ProcedimentoDAO.listarTodos();

    private final List<Runnable> ouvintes = new ArrayList<>();

    public void aoAlterar(Runnable ouvinte) {
        ouvintes.add(ouvinte);
    }

    private void notificar() {
        for (Runnable r : new ArrayList<>(ouvintes)) r.run();
    }

    public LocalDate getDataAtual() { return dataAtual; }
    public void setDataAtual(LocalDate d) { this.dataAtual = d; notificar(); }

    public String getTema() { return tema; }
    public void setTema(String tema) {
        this.tema = tema;
        ConfigDAO.set("tema", tema);
        notificar();
    }

    public int getHoraAbertura() { return horaAbertura; }
    public int getHoraFechamento() { return horaFechamento; }

    public void setHorario(int abertura, int fechamento) {
        this.horaAbertura = abertura;
        this.horaFechamento = fechamento;
        ConfigDAO.set("hora_abertura", String.valueOf(abertura));
        ConfigDAO.set("hora_fechamento", String.valueOf(fechamento));
        notificar();
    }

    public List<Procedimento> getProcedimentos() { return procedimentos; }

    public void recarregarProcedimentos() {
        procedimentos = ProcedimentoDAO.listarTodos();
        notificar();
    }

    public Optional<Procedimento> buscarProcedimento(String id) {
        if (id == null) return Optional.empty();
        return procedimentos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    /** Usado apos operacoes de CRUD de agendamentos para forcar nova renderizacao. */
    public void notificarMudanca() { notificar(); }
}
