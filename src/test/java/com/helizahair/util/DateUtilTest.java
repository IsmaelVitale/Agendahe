package com.helizahair.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateUtilTest {

    @Test
    void semanaSempreComecaNaSegundaFeira() {
        assertEquals(LocalDate.of(2026, 7, 13),
                DateUtil.inicioDaSemana(LocalDate.of(2026, 7, 17)));
        assertEquals(LocalDate.of(2026, 7, 13),
                DateUtil.inicioDaSemana(LocalDate.of(2026, 7, 19)));
        assertEquals(LocalDate.of(2026, 7, 13),
                DateUtil.inicioDaSemana(LocalDate.of(2026, 7, 13)));
    }

    @Test
    void converteLimitesDoDiaEmMinutos() {
        assertEquals(0, DateUtil.horaParaMinutos("00:00"));
        assertEquals(1_410, DateUtil.horaParaMinutos("23:30"));
        assertEquals(1_440, DateUtil.horaParaMinutos("24:00"));
        assertEquals("24:00", DateUtil.minutosParaHora(1_440));
    }

    @Test
    void rejeitaHorarioInvalido() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.horaParaMinutos("24:30"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.horaParaMinutos("8:00"));
    }
}
