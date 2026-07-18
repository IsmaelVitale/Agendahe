package com.helizahair.db;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaminhosAplicacaoTest {

    @Test
    void propriedadeExplicitaTemPrioridade() {
        Path resultado = CaminhosAplicacao.resolverDiretorio(
                "dados-teste", "local-app-data", "usuario"
        );
        assertEquals(Path.of("dados-teste").toAbsolutePath().normalize(), resultado);
    }

    @Test
    void usaLocalAppDataQuandoDisponivel() {
        Path resultado = CaminhosAplicacao.resolverDiretorio(
                null, "local-app-data", "usuario"
        );
        assertEquals(
                Path.of("local-app-data", "AgendaElizaHair").toAbsolutePath().normalize(),
                resultado
        );
    }

    @Test
    void possuiAlternativaForaDoWindows() {
        Path resultado = CaminhosAplicacao.resolverDiretorio(null, null, "usuario");
        assertEquals(
                Path.of("usuario", ".agenda-eliza-hair").toAbsolutePath().normalize(),
                resultado
        );
    }
}
