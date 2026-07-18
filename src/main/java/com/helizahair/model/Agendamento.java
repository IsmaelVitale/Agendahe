package com.helizahair.model;

public class Agendamento {
    private int id;
    private String cliente;
    private String procId;
    private String data;        // yyyy-MM-dd
    private String horaInicio;  // HH:mm
    private String horaFim;     // HH:mm
    private double valor;
    private String status;      // agendado, concluido, cancelado, falta

    public Agendamento() {
        this.status = "agendado";
    }

    public Agendamento(int id, String cliente, String procId, String data,
                        String horaInicio, String horaFim, double valor, String status) {
        this.id = id;
        this.cliente = cliente;
        this.procId = procId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.valor = valor;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getProcId() { return procId; }
    public void setProcId(String procId) { this.procId = procId; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
