package com.example.juntoscontradengue.database.classes_database;

public class ClassUploadImgAgentes {
    private String nome_agente, funcao_agente,  url_img_agente;

    public ClassUploadImgAgentes() {
    }

    public ClassUploadImgAgentes(String nome_agente, String funcao_agente, String url_img_agente) {
        this.nome_agente = nome_agente;
        this.funcao_agente = funcao_agente;
        this.url_img_agente = url_img_agente;
    }

    public String getNome_agente() {
        return nome_agente;
    }

    public void setNome_agente(String nome_agente) {
        this.nome_agente = nome_agente;
    }

    public String getFuncao_agente() {
        return funcao_agente;
    }

    public void setFuncao_agente(String funcao_agente) {
        this.funcao_agente = funcao_agente;
    }

    public String getUrl_img_agente() {
        return url_img_agente;
    }

    public void setUrl_img_agente(String url_img_agente) {
        this.url_img_agente = url_img_agente;
    }
}
