package com.helizahair.service;

import com.helizahair.db.AgendamentoDAO;
import com.helizahair.db.FechamentoCaixaDAO;
import com.helizahair.model.Agendamento;
import com.helizahair.model.FechamentoCaixa;
import com.helizahair.util.DateUtil;

public final class AgendamentoService {

    private AgendamentoService() {}

    public static boolean diaFechado(String data) {
        FechamentoCaixa fechamento = FechamentoCaixaDAO.buscarPorData(data);
        return fechamento != null && fechamento.isFechado();
    }

    public static int salvar(Agendamento agendamento) {
        validar(agendamento);
        int id = AgendamentoDAO.salvar(agendamento);
        if (id < 0) {
            throw new RegraNegocioException("Não foi possível salvar o agendamento.");
        }
        return id;
    }

    public static void excluir(Agendamento agendamento) {
        validarDiaAberto(agendamento.getData());
        AgendamentoDAO.excluir(agendamento.getId());
    }

    private static void validar(Agendamento agendamento) {
        if (agendamento.getCliente() == null || agendamento.getCliente().isBlank()) {
            throw new RegraNegocioException("Informe o nome do cliente.");
        }
        if (agendamento.getData() == null || agendamento.getData().isBlank()) {
            throw new RegraNegocioException("Informe a data do agendamento.");
        }
        validarDiaAberto(agendamento.getData());

        int inicio;
        int fim;
        try {
            inicio = DateUtil.horaParaMinutos(agendamento.getHoraInicio());
            fim = DateUtil.horaParaMinutos(agendamento.getHoraFim());
        } catch (RuntimeException e) {
            throw new RegraNegocioException("Informe horários válidos no formato HH:mm.");
        }
        if (inicio >= fim) {
            throw new RegraNegocioException("O término deve ser posterior ao início.");
        }
        if (agendamento.getValor() < 0) {
            throw new RegraNegocioException("O valor não pode ser negativo.");
        }
    }

    private static void validarDiaAberto(String data) {
        if (diaFechado(data)) {
            throw new RegraNegocioException(
                    "O caixa deste dia está fechado. Reabra ou atualize o caixa antes de alterar a agenda."
            );
        }
    }
}
