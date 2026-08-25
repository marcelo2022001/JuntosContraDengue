package com.example.juntoscontradengue.database.classes_database;

public class ClassReclamacoes {

    private Boolean visivel_agente;
    private String idUsuario;
    private String idReclamacao;
    private Long data_envio;
    private String reclamante;
    private String telefone;
    private String endereco_reclamante;
    private String numero_casa_reclamante;
    private String conjunto_residencia_reclamante;
    // Sobre a reclamação
    private String endereco_reclamacao;
    private String num_casa_reclamacao;
    private String conjunto_reclamacao;
    private String referencia;
    private String reclamacao;
    private String imagem1;
    private String imagem2;
    private String imagem3;
    private String video;
    private String status;
    private int total_reclamacoes;
    private String respondida_por;

    public ClassReclamacoes() {
    }

    public ClassReclamacoes(String idUsuario, String idReclamacao, Long data_envio, String reclamante, String telefone, String endereco_reclamante, String numero_casa_reclamante, String conjunto_residencia_reclamante, String endereco_reclamacao, String num_casa_reclamacao, String conjunto_reclamacao, String referencia, String reclamacao, String imagem1, String imagem2, String imagem3, String video, String status, int total_reclamacoes, String respondida_por) {
        this.idUsuario = idUsuario;
        this.idReclamacao = idReclamacao;
        this.data_envio = data_envio;
        this.reclamante = reclamante;
        this.telefone = telefone;
        this.endereco_reclamante = endereco_reclamante;
        this.numero_casa_reclamante = numero_casa_reclamante;
        this.conjunto_residencia_reclamante = conjunto_residencia_reclamante;
        this.endereco_reclamacao = endereco_reclamacao;
        this.num_casa_reclamacao = num_casa_reclamacao;
        this.conjunto_reclamacao = conjunto_reclamacao;
        this.referencia = referencia;
        this.reclamacao = reclamacao;
        this.imagem1 = imagem1;
        this.imagem2 = imagem2;
        this.imagem3 = imagem3;
        this.video = video;
        this.status = status;
        this.total_reclamacoes = total_reclamacoes;
        this.respondida_por = respondida_por;
    }

    public boolean isVisivelAgente() {
        return visivel_agente == null || visivel_agente; // default true se não existir no banco
    }

    public void setVisivelAgente(Boolean visivel_agente) {
        this.visivel_agente = visivel_agente;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdReclamacao() {
        return idReclamacao;
    }

    public void setIdReclamacao(String idReclamacao) {
        this.idReclamacao = idReclamacao;
    }

    public Long getData_envio() {
        return data_envio;
    }

    public void setData_envio(Long data_envio) {
        this.data_envio = data_envio;
    }

    public String getReclamante() {
        return reclamante;
    }

    public void setReclamante(String reclamante) {
        this.reclamante = reclamante;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco_reclamante() {
        return endereco_reclamante;
    }

    public void setEndereco_reclamante(String endereco_reclamante) {
        this.endereco_reclamante = endereco_reclamante;
    }

    public String getNumero_casa_reclamante() {
        return numero_casa_reclamante;
    }

    public void setNumero_casa_reclamante(String numero_casa_reclamante) {
        this.numero_casa_reclamante = numero_casa_reclamante;
    }

    public String getConjunto_residencia_reclamante() {
        return conjunto_residencia_reclamante;
    }

    public void setConjunto_residencia_reclamante(String conjunto_residencia_reclamante) {
        this.conjunto_residencia_reclamante = conjunto_residencia_reclamante;
    }

    public String getEndereco_reclamacao() {
        return endereco_reclamacao;
    }

    public void setEndereco_reclamacao(String endereco_reclamacao) {
        this.endereco_reclamacao = endereco_reclamacao;
    }

    public String getNum_casa_reclamacao() {
        return num_casa_reclamacao;
    }

    public void setNum_casa_reclamacao(String num_casa_reclamacao) {
        this.num_casa_reclamacao = num_casa_reclamacao;
    }

    public String getConjunto_reclamacao() {
        return conjunto_reclamacao;
    }

    public void setConjunto_reclamacao(String conjunto_reclamacao) {
        this.conjunto_reclamacao = conjunto_reclamacao;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getReclamacao() {
        return reclamacao;
    }

    public void setReclamacao(String reclamacao) {
        this.reclamacao = reclamacao;
    }

    public String getImagem1() {
        return imagem1;
    }

    public void setImagem1(String imagem1) {
        this.imagem1 = imagem1;
    }

    public String getImagem2() {
        return imagem2;
    }

    public void setImagem2(String imagem2) {
        this.imagem2 = imagem2;
    }

    public String getImagem3() {
        return imagem3;
    }

    public void setImagem3(String imagem3) {
        this.imagem3 = imagem3;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotal_reclamacoes() {
        return total_reclamacoes;
    }

    public void setTotal_reclamacoes(int total_reclamacoes) {
        this.total_reclamacoes = total_reclamacoes;
    }

    public String getRespondida_por() {
        return respondida_por;
    }

    public void setRespondida_por(String respondida_por) {
        this.respondida_por = respondida_por;
    }
}