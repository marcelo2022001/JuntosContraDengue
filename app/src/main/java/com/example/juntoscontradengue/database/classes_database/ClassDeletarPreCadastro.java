package com.example.juntoscontradengue.database.classes_database;

public class ClassDeletarPreCadastro {
    String cpf_pre_cadastro;
    String nome_pre_cadastro;
    String funcao_pre_cadastro;
    Long data_pre_cadastro;

    public ClassDeletarPreCadastro() {
    }

    public ClassDeletarPreCadastro(String cpf_pre_cadastro, String nome_pre_cadastro, String funcao_pre_cadastro, Long data_pre_cadastro) {
        this.cpf_pre_cadastro = cpf_pre_cadastro;
        this.nome_pre_cadastro = nome_pre_cadastro;
        this.funcao_pre_cadastro = funcao_pre_cadastro;
        this.data_pre_cadastro = data_pre_cadastro;
    }

    public String getCpf_pre_cadastro() {
        return cpf_pre_cadastro;
    }

    public void setCpf_pre_cadastro(String cpf_pre_cadastro) {
        this.cpf_pre_cadastro = cpf_pre_cadastro;
    }

    public String getNome_pre_cadastro() {
        return nome_pre_cadastro;
    }

    public void setNome_pre_cadastro(String nome_pre_cadastro) {
        this.nome_pre_cadastro = nome_pre_cadastro;
    }

    public String getFuncao_pre_cadastro() {
        return funcao_pre_cadastro;
    }

    public void setFuncao_pre_cadastro(String funcao_pre_cadastro) {
        this.funcao_pre_cadastro = funcao_pre_cadastro;
    }

    public Long getData_pre_cadastro() {
        return data_pre_cadastro;
    }

    public void setData_pre_cadastro(Long data_pre_cadastro) {
        this.data_pre_cadastro = data_pre_cadastro;
    }
}
