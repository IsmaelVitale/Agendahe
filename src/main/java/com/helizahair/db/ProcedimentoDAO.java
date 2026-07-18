package com.helizahair.db;

import com.helizahair.model.Procedimento;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProcedimentoDAO {

    public static List<Procedimento> listarTodos() {
        List<Procedimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM procedimentos ORDER BY nome";
        try (Statement st = BancoDeDados.getConexao().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(fromResultSet(rs));
        } catch (SQLException e) {
            System.out.println("Erro ao listar procedimentos: " + e.getMessage());
        }
        return lista;
    }

    public static void salvar(Procedimento p) {
        String sql = "INSERT INTO procedimentos (id, nome, duracao_min, preco, cor) VALUES (?,?,?,?,?) " +
                "ON CONFLICT(id) DO UPDATE SET nome=excluded.nome, duracao_min=excluded.duracao_min, " +
                "preco=excluded.preco, cor=excluded.cor";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getNome());
            ps.setInt(3, p.getDuracaoMin());
            ps.setDouble(4, p.getPreco());
            ps.setString(5, p.getCor());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao salvar procedimento: " + e.getMessage());
        }
    }

    public static void excluir(String id) {
        String sql = "DELETE FROM procedimentos WHERE id = ?";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao excluir procedimento: " + e.getMessage());
        }
    }

    private static Procedimento fromResultSet(ResultSet rs) throws SQLException {
        return new Procedimento(
            rs.getString("id"),
            rs.getString("nome"),
            rs.getInt("duracao_min"),
            rs.getDouble("preco"),
            rs.getString("cor")
        );
    }
}
