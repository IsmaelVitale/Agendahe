package com.helizahair.db;

import com.helizahair.model.FechamentoCaixa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FechamentoCaixaDAO {

    public static FechamentoCaixa buscarPorData(String data) {
        String sql = "SELECT * FROM fechamentos_caixa WHERE data = ?";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, data);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar fechamento: " + e.getMessage());
        }
        return null;
    }

    public static void salvar(FechamentoCaixa f) {
        String sql = "INSERT INTO fechamentos_caixa (data, fechado, total, extras, concluidos) VALUES (?,?,?,?,?) " +
                     "ON CONFLICT(data) DO UPDATE SET fechado=excluded.fechado, total=excluded.total, " +
                     "extras=excluded.extras, concluidos=excluded.concluidos";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, f.getData());
            ps.setInt(2, f.isFechado() ? 1 : 0);
            ps.setDouble(3, f.getTotal());
            ps.setDouble(4, f.getExtras());
            ps.setInt(5, f.getConcluidos());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao salvar fechamento: " + e.getMessage());
        }
    }

    public static void reabrir(String data) {
        String sql = "UPDATE fechamentos_caixa SET fechado = 0 WHERE data = ?";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, data);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reabrir fechamento: " + e.getMessage(), e);
        }
    }

    /** anoMes no formato "yyyy-MM" */
    public static List<FechamentoCaixa> listarFechadosDoMes(String anoMes) {
        List<FechamentoCaixa> lista = new ArrayList<>();
        String sql = "SELECT * FROM fechamentos_caixa WHERE data LIKE ? AND fechado = 1 ORDER BY data";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, anoMes + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar fechamentos do mes: " + e.getMessage());
        }
        return lista;
    }

    private static FechamentoCaixa fromResultSet(ResultSet rs) throws SQLException {
        return new FechamentoCaixa(
            rs.getString("data"),
            rs.getInt("fechado") == 1,
            rs.getDouble("total"),
            rs.getDouble("extras"),
            rs.getInt("concluidos")
        );
    }
}
