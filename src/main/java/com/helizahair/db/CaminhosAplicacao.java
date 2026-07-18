package com.helizahair.db;

import java.nio.file.Path;

public final class CaminhosAplicacao {

    private static final String PROPRIEDADE_DIRETORIO = "agendahe.dataDir";

    private CaminhosAplicacao() {}

    public static Path diretorioDados() {
        return resolverDiretorio(
                System.getProperty(PROPRIEDADE_DIRETORIO),
                System.getenv("LOCALAPPDATA"),
                System.getProperty("user.home")
        );
    }

    static Path resolverDiretorio(String configurado, String localAppData, String pastaUsuario) {
        if (configurado != null && !configurado.isBlank()) {
            return Path.of(configurado).toAbsolutePath().normalize();
        }
        if (localAppData != null && !localAppData.isBlank()) {
            return Path.of(localAppData, "AgendaElizaHair").toAbsolutePath().normalize();
        }
        return Path.of(pastaUsuario, ".agenda-eliza-hair").toAbsolutePath().normalize();
    }
}
