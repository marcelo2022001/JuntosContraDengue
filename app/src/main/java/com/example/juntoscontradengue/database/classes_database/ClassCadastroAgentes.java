package com.example.juntoscontradengue.database.classes_database;

public class ClassCadastroAgentes {

    private String nome_usuario;
    private String cpf;
    private String email;
    private String telefone;
    private Boolean autoriza_uso_imagem;

    // Construtor vazio obrigatório


    public ClassCadastroAgentes() {
    }

    public ClassCadastroAgentes(String nome_usuario, String cpf, String email, String telefone, Boolean autoriza_uso_imagem) {
        this.nome_usuario = nome_usuario;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.autoriza_uso_imagem = autoriza_uso_imagem;
    }

    public String getNome_usuario() {
        return nome_usuario;
    }

    public void setNome_usuario(String nome_usuario) {
        this.nome_usuario = nome_usuario;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Boolean getAutoriza_uso_imagem() {
        return autoriza_uso_imagem;
    }

    public void setAutoriza_uso_imagem(Boolean autoriza_uso_imagem) {
        this.autoriza_uso_imagem = autoriza_uso_imagem;
    }
}