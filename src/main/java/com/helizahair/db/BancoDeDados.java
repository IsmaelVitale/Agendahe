package com.helizahair.db;

import java.sql.*;

public class BancoDeDados {

    // Cria o arquivo dados_salao.db na raiz do projeto automaticamente
    private static final String URL = "jdbc:sqlite:dados_salao.db";
    private static Connection conexao;

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

            comando.execute(
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
                ");"
            );

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
}
