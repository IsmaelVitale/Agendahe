package com.helizahair.db;

import com.helizahair.model.Agendamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgendamentoDAO {

    public static List<Agendamento> listarPorPeriodo(String dataInicio, String dataFim) {
        List<Agendamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM agendamentos WHERE data BETWEEN ? AND ? ORDER BY hora_inicio";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, dataInicio);
            ps.setString(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar agendamentos: " + e.getMessage());
        }
        return lista;
    }

    public static List<Agendamento> listarPorData(String data) {
        return listarPorPeriodo(data, data);
    }

    public static List<String> listarDatasComAgendamentos() {
        List<String> datas = new ArrayList<>();
        String sql = "SELECT DISTINCT data FROM agendamentos ORDER BY data";
        try (Statement st = BancoDeDados.getConexao().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) datas.add(rs.getString("data"));
        } catch (SQLException e) {
            System.out.println("Erro ao listar datas: " + e.getMessage());
        }
        return datas;
    }

    public static int salvar(Agendamento a) {
        if (a.getId() > 0) {
            String sql = "UPDATE agendamentos SET cliente=?, proc_id=?, data=?, hora_inicio=?, hora_fim=?, valor=?, status=? WHERE id=?";
            try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
                preencher(ps, a);
                ps.setInt(8, a.getId());
                ps.executeUpdate();
                return a.getId();
            } catch (SQLException e) {
                System.out.println("Erro ao atualizar agendamento: " + e.getMessage());
            }
        } else {
            String sql = "INSERT INTO agendamentos (cliente, proc_id, data, hora_inicio, hora_fim, valor, status) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preencher(ps, a);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int novoId = keys.getInt(1);
                        a.setId(novoId);
                        return novoId;
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao inserir agendamento: " + e.getMessage());
            }
        }
        return -1;
    }

    public static void excluir(int id) {
        String sql = "DELETE FROM agendamentos WHERE id = ?";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao excluir agendamento: " + e.getMessage());
        }
    }

    public static void atualizarStatus(int id, String status) {
        String sql = "UPDATE agendamentos SET status=? WHERE id=?";
        try (PreparedStatement ps = BancoDeDados.getConexao().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar status: " + e.getMessage());
        }
    }

    private static void preencher(PreparedStatement ps, Agendamento a) throws SQLException {
        ps.setString(1, a.getCliente());
        ps.setString(2, a.getProcId());
        ps.setString(3, a.getData());
        ps.setString(4, a.getHoraInicio());
        ps.setString(5, a.getHoraFim());
        ps.setDouble(6, a.getValor());
        ps.setString(7, a.getStatus());
    }

    private static Agendamento fromResultSet(ResultSet rs) throws SQLException {
        return new Agendamento(
            rs.getInt("id"),
            rs.getString("cliente"),
            rs.getString("proc_id"),
            rs.getString("data"),
            rs.getString("hora_inicio"),
            rs.getString("hora_fim"),
            rs.getDouble("valor"),
            rs.getString("status")
        );
    }
}
