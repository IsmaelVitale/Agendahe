package com.helizahair.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigDAO {

    public static String get(String chave, String valorPadrao) {
        String sql = "SELECT valor FROM config WHERE chave = ?";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, chave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("valor");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao ler configuracao: " + e.getMessage());
        }
        return valorPadrao;
    }

    public static void set(String chave, String valor) {
        String sql = "INSERT INTO config (chave, valor) VALUES (?, ?) " +
                     "ON CONFLICT(chave) DO UPDATE SET valor = excluded.valor";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, chave);
            ps.setString(2, valor);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao salvar configuracao: " + e.getMessage());
        }
    }
}
