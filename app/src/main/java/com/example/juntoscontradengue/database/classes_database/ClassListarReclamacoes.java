package com.example.juntoscontradengue.database.classes_database;

public class ClassListarReclamacoes {

    private Long data_envio;
    private String data_resposta;
    private String reclamacao;
    private String status;
    private String respondida_por;
    private String uid;
    private String idReclamacao;
    private String tokenUsuario;

    public ClassListarReclamacoes() {
    }

    public ClassListarReclamacoes(Long data_envio, String data_resposta, String reclamacao, String status, String respondida_por, String uid, String idReclamacao, String tokenUsuario) {
        this.data_envio = data_envio;
        this.data_resposta = data_resposta;
        this.reclamacao = reclamacao;
        this.status = status;
        this.respondida_por = respondida_por;
        this.uid = uid;
        this.idReclamacao = idReclamacao;
        this.tokenUsuario = tokenUsuario;
    }

    public Long getData_envio() {
        return data_envio;
    }

    public void setData_envio(Long data_envio) {
        this.data_envio = data_envio;
    }

    public String getData_resposta() {
        return data_resposta;
    }

    public void setData_resposta(String data_resposta) {
        this.data_resposta = data_resposta;
    }

    public String getReclamacao() {
        return reclamacao;
    }

    public void setReclamacao(String reclamacao) {
        this.reclamacao = reclamacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRespondida_por() {
        return respondida_por;
    }

    public void setRespondida_por(String respondida_por) {
        this.respondida_por = respondida_por;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIdReclamacao() {
        return idReclamacao;
    }

    public void setIdReclamacao(String idReclamacao) {
        this.idReclamacao = idReclamacao;
    }

    public String getTokenUsuario() {
        return tokenUsuario;
    }

    public void setTokenUsuario(String tokenUsuario) {
        this.tokenUsuario = tokenUsuario;
    }
}