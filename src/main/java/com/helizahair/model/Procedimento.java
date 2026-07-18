package com.helizahair.model;

public class Procedimento {
    private String id;
    private String nome;
    private int duracaoMin;
    private double preco;
    private String cor; // chave da paleta: pink, blue, purple, red, teal, orange, gray

    public Procedimento() {}

    public Procedimento(String id, String nome, int duracaoMin, double preco, String cor) {
        this.id = id;
        this.nome = nome;
        this.duracaoMin = duracaoMin;
        this.preco = preco;
        this.cor = cor;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(int duracaoMin) { this.duracaoMin = duracaoMin; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    @Override
    public String toString() { return nome; }
}
