package com.helizahair.model;

public class FechamentoCaixa {
    private String data;      // yyyy-MM-dd
    private boolean fechado;
    private double total;
    private double extras;
    private int concluidos;

    public FechamentoCaixa() {}

    public FechamentoCaixa(String data, boolean fechado, double total, double extras, int concluidos) {
        this.data = data;
        this.fechado = fechado;
        this.total = total;
        this.extras = extras;
        this.concluidos = concluidos;
    }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public boolean isFechado() { return fechado; }
    public void setFechado(boolean fechado) { this.fechado = fechado; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getExtras() { return extras; }
    public void setExtras(double extras) { this.extras = extras; }

    public int getConcluidos() { return concluidos; }
    public void setConcluidos(int concluidos) { this.concluidos = concluidos; }
}
