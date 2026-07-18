package com.helizahair.db;

import java.sql.*;

public class BancoDeDados {

    // Cria o arquivo dados_salao.db na raiz do projeto automaticamente
    private static final String URL = "jdbc:sqlite:dados_salao.db";
    private static Connection conexao;
    private static final String SQL_CRIAR_AGENDAMENTOS =
            "CREATE TABLE IF NOT EXISTS agendamentos (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "cliente TEXT NOT NULL," +
            "proc_id TEXT," +
            "data TEXT NOT NULL," +
            "hora_inicio TEXT NOT NULL," +
            "hora_fim TEXT NOT NULL," +
            "valor REAL NOT NULL DEFAULT 0," +
            "status TEXT NOT NULL DEFAULT 'agendado'," +
            "FOREIGN KEY (proc_id) REFERENCES procedimentos(id) ON DELETE SET NULL" +
            ");";

    public static Connection getConexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                conexao = DriverManager.getConnection(URL);
                try (Statement st = conexao.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON;");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
        return conexao;
    }

    public static void inicializar() {
        try (Statement comando = getConexao().createStatement()) {

            System.out.println("Conexao com o banco SQLite estabelecida!");

            comando.execute(
                "CREATE TABLE IF NOT EXISTS procedimentos (" +
                "id TEXT PRIMARY KEY," +
                "nome TEXT NOT NULL," +
                "duracao_min INTEGER NOT NULL," +
                "preco REAL NOT NULL," +
                "cor TEXT NOT NULL" +
                ");"
            );

            boolean migrarLegado = prepararMigracaoDeAgendamentos(comando);
            comando.execute(SQL_CRIAR_AGENDAMENTOS);
            if (migrarLegado) {
                comando.execute(
                        "INSERT INTO agendamentos " +
                        "(id, cliente, proc_id, data, hora_inicio, hora_fim, valor, status) " +
                        "SELECT id, cliente, NULL, CAST(data_agendamento AS TEXT), " +
                        "substr(hora_agendamento, 1, 5), " +
                        "time(substr(hora_agendamento, 1, 5), '+60 minutes'), valor, 'agendado' " +
                        "FROM agendamentos_legado"
                );
                System.out.println("Agendamentos antigos migrados. A tabela agendamentos_legado foi mantida como backup.");
            }

            comando.execute(
                "CREATE TABLE IF NOT EXISTS fechamentos_caixa (" +
                "data TEXT PRIMARY KEY," +
                "fechado INTEGER NOT NULL DEFAULT 0," +
                "total REAL NOT NULL DEFAULT 0," +
                "extras REAL NOT NULL DEFAULT 0," +
                "concluidos INTEGER NOT NULL DEFAULT 0" +
                ");"
            );

            comando.execute(
                "CREATE TABLE IF NOT EXISTS config (" +
                "chave TEXT PRIMARY KEY," +
                "valor TEXT" +
                ");"
            );

            System.out.println("Tabelas prontas para uso.");
            semearDadosIniciais();

        } catch (SQLException e) {
            System.out.println("Erro ao iniciar o banco de dados: " + e.getMessage());
        }
    }

    private static void semearDadosIniciais() throws SQLException {
        try (Statement check = getConexao().createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) AS n FROM procedimentos")) {
            if (rs.next() && rs.getInt("n") == 0) {
                String sql = "INSERT INTO procedimentos (id, nome, duracao_min, preco, cor) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = getConexao().prepareStatement(sql)) {
                    Object[][] padrao = {
                        {"corte_fem", "Corte Feminino", 60, 80.00, "pink"},
                        {"corte_masc", "Corte Masculino", 30, 40.00, "blue"},
                        {"progressiva", "Escova Progressiva", 120, 250.00, "purple"},
                        {"manicure", "Manicure", 60, 35.00, "orange"}
                    };
                    for (Object[] d : padrao) {
                        ps.setString(1, (String) d[0]);
                        ps.setString(2, (String) d[1]);
                        ps.setInt(3, (Integer) d[2]);
                        ps.setDouble(4, (Double) d[3]);
                        ps.setString(5, (String) d[4]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    private static boolean prepararMigracaoDeAgendamentos(Statement comando) throws SQLException {
        boolean tabelaExiste;
        try (ResultSet rs = comando.executeQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name='agendamentos'")) {
            tabelaExiste = rs.next();
        }
        if (!tabelaExiste) {
            return false;
        }

        boolean esquemaAtual = false;
        boolean esquemaLegado = false;
        try (ResultSet rs = comando.executeQuery("PRAGMA table_info(agendamentos)")) {
            while (rs.next()) {
                String coluna = rs.getString("name");
                esquemaAtual |= "data".equals(coluna);
                esquemaLegado |= "data_agendamento".equals(coluna);
            }
        }
        if (esquemaAtual || !esquemaLegado) {
            return false;
        }

        try (ResultSet rs = comando.executeQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name='agendamentos_legado'")) {
            if (rs.next()) {
                throw new SQLException(
                        "Migração pendente: a tabela de backup agendamentos_legado já existe."
                );
            }
        }
        comando.execute("ALTER TABLE agendamentos RENAME TO agendamentos_legado");
        return true;
    }
}
