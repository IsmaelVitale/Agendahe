package com.helizahair.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public static String formatar(LocalDate data) {
        return data.format(ISO);
    }

    /** Retorna a segunda-feira da semana da data informada (igual ao getStartOfWeek do JS). */
    public static LocalDate inicioDaSemana(LocalDate data) {
        int diaSemana = data.getDayOfWeek().getValue(); // 1=segunda ... 7=domingo
        return data.minusDays(diaSemana - 1);
    }

    public static int horaParaMinutos(String hhmm) {
        if (hhmm == null || !hhmm.matches("(?:[01]\\d|2[0-3]):[0-5]\\d|24:00")) {
            throw new IllegalArgumentException("Horário inválido: " + hhmm);
        }
        String[] partes = hhmm.split(":");
        return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
    }

    public static String minutosParaHora(int totalMin) {
        if (totalMin == 24 * 60) {
            return "24:00";
        }
        int h = (totalMin / 60) % 24;
        int m = totalMin % 60;
        return String.format("%02d:%02d", h, m);
    }

    public static String somarMinutos(String hhmm, int minutos) {
        return minutosParaHora(horaParaMinutos(hhmm) + minutos);
    }

    public static String formatarDataBR(String isoData) {
        String[] p = isoData.split("-");
        return p[2] + "/" + p[1] + "/" + p[0];
    }
}
