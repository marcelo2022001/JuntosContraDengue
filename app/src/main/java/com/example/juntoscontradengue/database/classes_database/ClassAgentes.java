package com.example.juntoscontradengue.database.classes_database;

public class ClassAgentes {

    private String uuid;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String funcao;
    private String urlImagem;
    private Object dataCadastro;  // Mudar de Long para Object
    private Object updatedAt;      // Mudar de Long para Object

    // Construtor vazio obrigatório para o Firebase
    public ClassAgentes() {
    }

    public ClassAgentes(String uuid, String nome, String cpf, String telefone,
                        String email, String funcao, String urlImagem,
                        Object dataCadastro, Object updatedAt) {
        this.uuid = uuid;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.funcao = funcao;
        this.urlImagem = urlImagem;
        this.dataCadastro = dataCadastro;
        this.updatedAt = updatedAt;
    }

    // Getters e Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public Object getDataCadastro() {
        return dataCadastro;
    }

    // Método auxiliar para obter como Long
    public Long getDataCadastroAsLong() {
        if (dataCadastro instanceof Long) {
            return (Long) dataCadastro;
        } else if (dataCadastro instanceof String) {
            try {
                return Long.parseLong((String) dataCadastro);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    // Método auxiliar para obter como String
    public String getDataCadastroAsString() {
        return dataCadastro != null ? dataCadastro.toString() : "";
    }

    public void setDataCadastro(Object dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    // Método auxiliar para obter updatedAt como Long
    public Long getUpdatedAtAsLong() {
        if (updatedAt instanceof Long) {
            return (Long) updatedAt;
        } else if (updatedAt instanceof String) {
            try {
                return Long.parseLong((String) updatedAt);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    public void setUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt;
    }
}