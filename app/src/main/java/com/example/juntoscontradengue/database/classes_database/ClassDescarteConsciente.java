package com.example.juntoscontradengue.database.classes_database;

public class ClassDescarteConsciente {

    private String id;
    private String local;
    private String endereco;
    private String numero;
    private String fone;
    private String horario;

    public ClassDescarteConsciente() {
    }

    public ClassDescarteConsciente(String id, String local, String endereco, String numero, String fone, String horario) {
        this.id = id;
        this.local = local;
        this.endereco = endereco;
        this.numero = numero;
        this.fone = fone;
        this.horario = horario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getFone() {
        return fone;
    }

    public void setFone(String fone) {
        this.fone = fone;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
