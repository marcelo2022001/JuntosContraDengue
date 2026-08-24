package com.example.juntoscontradengue.extras;

import android.text.Editable;
import android.text.TextWatcher;

public class MaskEditUtil {

    public static final String FORMAT_CPF = "###.###.###-##";
    public static final String FORMAT_FONE = "(##) # ####-####"; // Para celular
    public static final String FORMAT_FONE_FIXO = "(##)####-####"; // Para fixo

    public static TextWatcher mask(final String mask) {
        return new TextWatcher() {
            private boolean isUpdating = false;
            private String oldText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(final Editable s) {
                if (isUpdating) {
                    return;
                }

                String currentText = s.toString();

                // Se o texto não mudou, sai
                if (currentText.equals(oldText)) {
                    return;
                }

                isUpdating = true;

                // Remove todos os caracteres não numéricos
                String apenasNumeros = currentText.replaceAll("\\D", "");

                // Aplica a máscara
                String textoMascarado = aplicarMascara(apenasNumeros, mask);

                // Verifica se o texto mudou para evitar loop infinito
                if (!currentText.equals(textoMascarado)) {
                    s.replace(0, s.length(), textoMascarado);
                }

                oldText = textoMascarado;
                isUpdating = false;
            }
        };
    }

    public static TextWatcher maskTelefone() {
        return new TextWatcher() {
            private boolean isUpdating = false;
            private String oldText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(final Editable s) {
                if (isUpdating) {
                    return;
                }

                String currentText = s.toString();

                if (currentText.equals(oldText)) {
                    return;
                }

                isUpdating = true;

                // Remove todos os caracteres não numéricos
                String apenasNumeros = currentText.replaceAll("\\D", "");

                // Limita a 11 dígitos (celular) ou 10 (fixo)
                if (apenasNumeros.length() > 11) {
                    apenasNumeros = apenasNumeros.substring(0, 11);
                }

                // Aplica a máscara correta baseada no tamanho e no primeiro dígito após DDD
                String textoMascarado = aplicarMascaraTelefone(apenasNumeros);

                if (!currentText.equals(textoMascarado)) {
                    s.replace(0, s.length(), textoMascarado);
                }

                oldText = textoMascarado;
                isUpdating = false;
            }
        };
    }

    private static String aplicarMascara(String texto, String mask) {
        StringBuilder resultado = new StringBuilder();
        int indiceTexto = 0;

        for (int i = 0; i < mask.length() && indiceTexto < texto.length(); i++) {
            char caractereMascara = mask.charAt(i);

            if (caractereMascara == '#') {
                resultado.append(texto.charAt(indiceTexto));
                indiceTexto++;
            } else {
                resultado.append(caractereMascara);
            }
        }

        return resultado.toString();
    }

    private static String aplicarMascaraTelefone(String numeros) {
        if (numeros.isEmpty()) {
            return "";
        }

        // Se tem menos de 2 dígitos, mostra apenas os números
        if (numeros.length() <= 2) {
            return "(" + numeros;
        }

        // Se tem DDD (2 dígitos) mas menos de 3 dígitos
        if (numeros.length() == 2) {
            return "(" + numeros + ")";
        }

        // Pega o DDD
        String ddd = numeros.substring(0, 2);
        String restante = numeros.substring(2);

        // Verifica se é celular (9 dígitos no total - 2 do DDD = 7 dígitos restantes)
        // Se tiver 9 dígitos ou mais, e o primeiro dígito após DDD for 9, é celular
        boolean isCelular = false;

        if (numeros.length() >= 11) {
            // Formato completo: (DD) 9XXXX-XXXX
            String primeiroDigito = numeros.substring(2, 3);
            isCelular = "9".equals(primeiroDigito);
        } else if (numeros.length() == 10) {
            // Formato: (DD) XXXX-XXXX (fixo)
            isCelular = false;
        } else if (numeros.length() == 9) {
            // Provável celular com 9 dígitos (sem o zero do DDD)
            // Ex: 44991031612 -> (44) 99103-1612
            // Mas vamos verificar se o primeiro dígito após DDD é 9
            String primeiroDigito = restante.substring(0, 1);
            isCelular = "9".equals(primeiroDigito);
        }

        // Aplica a máscara baseada no tipo
        StringBuilder resultado = new StringBuilder();
        resultado.append("(").append(ddd).append(") ");

        if (isCelular) {
            // Celular: (44) 9 9103-1612 ou (44) 99103-1612
            if (restante.length() >= 5) {
                // Se tem 9 dígitos (celular)
                if (restante.length() == 9) {
                    resultado.append(restante.substring(0, 5)).append("-").append(restante.substring(5));
                } else if (restante.length() >= 5) {
                    // Adiciona um espaço após o 9 para melhor visualização
                    if (restante.length() > 5) {
                        resultado.append(restante.substring(0, 5)).append("-").append(restante.substring(5));
                    } else {
                        resultado.append(restante);
                    }
                } else {
                    resultado.append(restante);
                }
            } else {
                resultado.append(restante);
            }
        } else {
            // Fixo: (44) 3684-1343
            if (restante.length() >= 4) {
                resultado.append(restante.substring(0, 4));
                if (restante.length() > 4) {
                    resultado.append("-").append(restante.substring(4));
                }
            } else {
                resultado.append(restante);
            }
        }

        return resultado.toString();
    }

    public static String formatarTelefone(String telefone) {
        if (telefone == null) return "";

        telefone = telefone.replaceAll("\\D", "");

        if (telefone.length() > 11) {
            telefone = telefone.substring(0, 11);
        }

        if (telefone.isEmpty()) return "";

        // Verifica se é celular (tem 11 dígitos ou começa com 9 após DDD)
        boolean isCelular = telefone.length() == 11 ||
                (telefone.length() == 10 && telefone.charAt(2) == '9');

        if (telefone.length() == 11) {
            return telefone.replaceFirst(
                    "(\\d{2})(\\d{5})(\\d+)",
                    "($1) $2-$3"
            );
        } else if (telefone.length() == 10) {
            return telefone.replaceFirst(
                    "(\\d{2})(\\d{4})(\\d+)",
                    "($1) $2-$3"
            );
        } else if (telefone.length() == 9) {
            return telefone.replaceFirst(
                    "(\\d{5})(\\d+)",
                    "$1-$2"
            );
        } else if (telefone.length() == 8) {
            return telefone.replaceFirst(
                    "(\\d{4})(\\d+)",
                    "$1-$2"
            );
        }

        return telefone;
    }

    public static String FORMAT_CPF(String cpf) {
        if (cpf == null || cpf.isEmpty()) return "";

        String apenasNumeros = cpf.replaceAll("\\D", "");

        if (apenasNumeros.length() != 11) return cpf;

        return apenasNumeros.substring(0, 3) + "." +
                apenasNumeros.substring(3, 6) + "." +
                apenasNumeros.substring(6, 9) + "-" +
                apenasNumeros.substring(9, 11);
    }

    public static String unmask(final String s) {
        if (s == null) return "";
        return s.replaceAll("[.\\-()/ ]", "");
    }
}