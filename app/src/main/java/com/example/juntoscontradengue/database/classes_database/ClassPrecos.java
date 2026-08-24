package com.example.juntoscontradengue.database.classes_database;

public class ClassPrecos {
    Double valorAdmIndividual;
    Double valorAgenteIndividual;
    Double valorAdmBasico;
    Double valorAgenteBasico;
    Double valorAdmMunicipal;
    Double valorAgenteMunicipal;

    public ClassPrecos() {
    }

    public ClassPrecos(Double valorAdmIndividual, Double valorAgenteIndividual, Double valorAdmBasico, Double valorAgenteBasico, Double valorAdmMunicipal, Double valorAgenteMunicipal) {
        this.valorAdmIndividual = valorAdmIndividual;
        this.valorAgenteIndividual = valorAgenteIndividual;
        this.valorAdmBasico = valorAdmBasico;
        this.valorAgenteBasico = valorAgenteBasico;
        this.valorAdmMunicipal = valorAdmMunicipal;
        this.valorAgenteMunicipal = valorAgenteMunicipal;
    }

    public Double getValorAdmIndividual() {
        return valorAdmIndividual;
    }

    public void setValorAdmIndividual(Double valorAdmIndividual) {
        this.valorAdmIndividual = valorAdmIndividual;
    }

    public Double getValorAgenteIndividual() {
        return valorAgenteIndividual;
    }

    public void setValorAgenteIndividual(Double valorAgenteIndividual) {
        this.valorAgenteIndividual = valorAgenteIndividual;
    }

    public Double getValorAdmBasico() {
        return valorAdmBasico;
    }

    public void setValorAdmBasico(Double valorAdmBasico) {
        this.valorAdmBasico = valorAdmBasico;
    }

    public Double getValorAgenteBasico() {
        return valorAgenteBasico;
    }

    public void setValorAgenteBasico(Double valorAgenteBasico) {
        this.valorAgenteBasico = valorAgenteBasico;
    }

    public Double getValorAdmMunicipal() {
        return valorAdmMunicipal;
    }

    public void setValorAdmMunicipal(Double valorAdmMunicipal) {
        this.valorAdmMunicipal = valorAdmMunicipal;
    }

    public Double getValorAgenteMunicipal() {
        return valorAgenteMunicipal;
    }

    public void setValorAgenteMunicipal(Double valorAgenteMunicipal) {
        this.valorAgenteMunicipal = valorAgenteMunicipal;
    }
}
