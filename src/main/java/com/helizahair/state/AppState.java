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
    private int minutoAbertura = lerHorarioConfigurado("hora_abertura", "8", 8 * 60);
    private int minutoFechamento = lerHorarioConfigurado("hora_fechamento", "20", 20 * 60);
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

    public int getMinutoAbertura() { return minutoAbertura; }
    public int getMinutoFechamento() { return minutoFechamento; }

    public void setHorario(int aberturaEmMinutos, int fechamentoEmMinutos) {
        this.minutoAbertura = aberturaEmMinutos;
        this.minutoFechamento = fechamentoEmMinutos;
        ConfigDAO.set("hora_abertura", String.valueOf(aberturaEmMinutos));
        ConfigDAO.set("hora_fechamento", String.valueOf(fechamentoEmMinutos));
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

    private static int lerHorarioConfigurado(String chave, String valorPadrao, int minutoPadrao) {
        String valor = ConfigDAO.get(chave, valorPadrao);
        try {
            if (valor.contains(":")) {
                String[] partes = valor.split(":");
                return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
            }
            int numero = Integer.parseInt(valor);
            // Compatibilidade com a versão anterior, que armazenava somente a hora (8, 20 etc.).
            return numero <= 24 ? numero * 60 : numero;
        } catch (RuntimeException e) {
            return minutoPadrao;
        }
    }
}
