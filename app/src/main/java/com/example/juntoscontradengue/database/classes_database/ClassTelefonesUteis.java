package com.example.juntoscontradengue.database.classes_database;


public class ClassTelefonesUteis {

    private String id;
    private String local;
    private String telefone;

    public ClassTelefonesUteis() {
    }

    public ClassTelefonesUteis(String id, String local, String telefone) {
        this.id = id;
        this.local = local;
        this.telefone = telefone;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
