package com.example.juntoscontradengue.database.classes_database;

public class ClassUsuarios {
    private String nome;
    private String cpf;
    private String funcao;
    private String email;
    private String telefone;
    private Long dataCadastro;
    private String endereco;
    private String num_casa;
    private String conjunto;
    private Long updatedAt;  // 0 = nunca atualizado, >0 = timestamp
    private String uuid;

    // Construtor vazio (OBRIGATÓRIO para Firebase)
    public ClassUsuarios() {
        this.updatedAt = 0L;  // ✅ Valor padrão
    }

    // Construtor completo
    public ClassUsuarios(String nome, String cpf, String funcao, String email,
                         String telefone, Long dataCadastro, String endereco,
                         String num_casa, String conjunto, Long updatedAt) {
        this.nome = nome;
        this.cpf = cpf;
        this.funcao = funcao;
        this.email = email;
        this.telefone = telefone;
        this.dataCadastro = dataCadastro;
        this.endereco = endereco;
        this.num_casa = num_casa;
        this.conjunto = conjunto;
        this.updatedAt = (updatedAt != null) ? updatedAt : 0L;  // ✅ Garante que nunca seja null
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
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

    public Long getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Long dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNum_casa() {
        return num_casa;
    }

    public void setNum_casa(String num_casa) {
        this.num_casa = num_casa;
    }

    public String getConjunto() {
        return conjunto;
    }

    public void setConjunto(String conjunto) {
        this.conjunto = conjunto;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = (updatedAt != null) ? updatedAt : 0L;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // ✅ MÉTODO UTILITÁRIO: Verifica se foi atualizado
    public boolean isUpdated() {
        return updatedAt != null && updatedAt > 0;
    }

    // ✅ MÉTODO UTILITÁRIO: Retorna o timestamp ou null se for 0
    public Long getUpdatedAtOrNull() {
        return (updatedAt != null && updatedAt > 0) ? updatedAt : null;
    }
}