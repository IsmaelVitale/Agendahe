package com.helizahair.service;

import com.helizahair.db.AgendamentoDAO;
import com.helizahair.model.Agendamento;
import com.helizahair.util.DateUtil;

public final class ConfiguracaoService {

    private ConfiguracaoService() {}

    public static void validarExpediente(int minutoAbertura, int minutoFechamento) {
        if (minutoAbertura < 0 || minutoFechamento > 24 * 60 || minutoAbertura >= minutoFechamento) {
            throw new RegraNegocioException("A abertura deve ser menor que o fechamento.");
        }
        if (minutoAbertura % 30 != 0 || minutoFechamento % 30 != 0) {
            throw new RegraNegocioException("Os horários devem respeitar intervalos de 30 minutos.");
        }

        for (Agendamento agendamento : AgendamentoDAO.listarPorPeriodo("0000-01-01", "9999-12-31")) {
            int inicio = DateUtil.horaParaMinutos(agendamento.getHoraInicio());
            int fim = DateUtil.horaParaMinutos(agendamento.getHoraFim());
            if (inicio < minutoAbertura || fim > minutoFechamento) {
                throw new RegraNegocioException(
                        "Não é possível alterar.\nConflito: " + agendamento.getCliente()
                                + " às " + agendamento.getHoraInicio()
                                + " em " + DateUtil.formatarDataBR(agendamento.getData()) + "."
                );
            }
        }
    }
}
