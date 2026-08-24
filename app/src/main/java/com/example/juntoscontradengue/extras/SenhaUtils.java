package com.example.juntoscontradengue.extras;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SenhaUtils {


    /**
     * Verifica se a senha (que deve ser composta apenas por números) contém sequências numéricas.
     *
     * @param senha A senha a ser verificada.
     * @return true se a senha contém sequências numéricas (ex: 123, 456, 789, 987, etc.),
     * false caso contrário ou se a senha não for composta apenas por números.
     */
    public static boolean temSequenciaNumerica(String senha) {
        // 1. Verificar se a senha é composta apenas por números
        if (!senha.matches("\\d+")) {
            return false; // Não é uma senha válida (apenas números)
        }

        // 2. Verificar sequências crescentes (ex: 123, 456)
        if (verificaSequenciaCrescente(senha)) {
            return true;
        }

        // 3. Verificar sequências decrescentes (ex: 321, 654)
        return verificaSequenciaDecrescente(senha);// Nenhuma sequência encontrada
    }

    private static boolean verificaSequenciaCrescente(String senha) {
        for (int i = 0; i < senha.length() - 2; i++) {
            int num1 = Character.getNumericValue(senha.charAt(i));
            int num2 = Character.getNumericValue(senha.charAt(i + 1));
            int num3 = Character.getNumericValue(senha.charAt(i + 2));

            if (num2 == num1 + 1 && num3 == num2 + 1) {
                return true; // Encontrou uma sequência crescente
            }
        }
        return false;
    }

    private static boolean verificaSequenciaDecrescente(String senha) {
        for (int i = 0; i < senha.length() - 2; i++) {
            int num1 = Character.getNumericValue(senha.charAt(i));
            int num2 = Character.getNumericValue(senha.charAt(i + 1));
            int num3 = Character.getNumericValue(senha.charAt(i + 2));

            if (num2 == num1 - 1 && num3 == num2 - 1) {
                return true; // Encontrou uma sequência decrescente
            }
        }
        return false;
    }


    /**
     * Uma versão mais robusta usando expressões regulares (RegEx)
     * para detectar sequências de 3 ou mais números.
     * Essa versão é mais flexível e pode detectar sequências
     * mesmo que não sejam consecutivas (ex: 135).  No entanto,
     * não trata sequências decrescentes.
     *
     * @param senha A senha a ser verificada.
     * @return true se a senha contém sequências crescentes numéricas de 3 ou mais dígitos,
     * false caso contrário ou se a senha não for composta apenas por números.
     */
    public static boolean temSequenciaNumericaRegex(String senha) {
        if (!senha.matches("\\d+")) {
            return false; // Não é uma senha válida (apenas números)
        }

        // Expressão regular para encontrar sequências crescentes (ex: 123, 456)
        // de pelo menos 3 dígitos.  A ideia aqui é criar um padrão dinamicamente
        // baseado no primeiro dígito e procurar por ocorrências desse padrão na senha.
        //
        // Exemplo: Se o primeiro dígito é '1', o padrão seria "123|234|345|456|567|678|789"
        // que procura por qualquer uma dessas sequências.

        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i <= 7; i++) { // Até 7 para que 789 seja a última sequência possível
            patternBuilder.append(i).append(i + 1).append(i + 2);
            if (i < 7) {
                patternBuilder.append("|"); // Adiciona o "OU" para as próximas sequências
            }
        }

        Pattern pattern = Pattern.compile(patternBuilder.toString());
        Matcher matcher = pattern.matcher(senha);

        return matcher.find(); // Retorna true se alguma sequência for encontrada
    }
}