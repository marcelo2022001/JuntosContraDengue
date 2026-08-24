package com.example.juntoscontradengue.extras;

import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

public class EmailValidator {
    private static EmailValidator sInstance;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Mapa de erros comuns para correções
    private static final Map<String, String> COMMON_TYPO_CORRECTIONS = new HashMap<String, String>() {{
        // Hotmail
        put("hhotmail.com", "hotmail.com");
        put("@@hotmail.com", "hotmail.com");
        put("hotmal.com", "hotmail.com");
        put("homtail.com", "hotmail.com");
        put("hotmaiil.com", "hotmail.com");
        put("hotmaill.com", "hotmail.com");
        put("hotmial.com", "hotmail.com");
        put("hotmil.com", "hotmail.com");
        put("hotmail.co", "hotmail.com");
        put("hotmail.cm", "hotmail.com");
        put("hotmai.com", "hotmail.com");

        // Gmail
        put("gmial.com", "gmail.com");
        put("gmal.com", "gmail.com");
        put("gmaill.com", "gmail.com");
        put("gmil.com", "gmail.com");
        put("gmail.co", "gmail.com");
        put("gmail.cm", "gmail.com");
        put("gamail.com", "gmail.com");

        // Yahoo
        put("yaho.com", "yahoo.com");
        put("yahooo.com", "yahoo.com");
        put("yahoov.com", "yahoo.com");
        put("yahoo.co", "yahoo.com");
        put("yhaoo.com", "yahoo.com");

        // Outlook
        put("outlok.com", "outlook.com");
        put("outllok.com", "outlook.com");
        put("outlook.co", "outlook.com");
        put("outlok.com.br", "outlook.com");

        // Outlook alternativos
        put("outlook.com.br", "outlook.com");

        // Bol
        put("bol.co", "bol.com.br");
        put("bol.com", "bol.com.br");
        put("bol.com.b", "bol.com.br");

        // Uol
        put("uol.co", "uol.com.br");
        put("uol.com", "uol.com.br");
        put("uol.com.b", "uol.com.br");

        // IG
        put("ig.co", "ig.com.br");
        put("ig.com", "ig.com.br");
        put("ig.com.b", "ig.com.br");
    }};

    public static EmailValidator getInstance() {
        if (sInstance == null) {
            sInstance = new EmailValidator();
        }
        return sInstance;
    }

    public boolean validate(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailLower = email.toLowerCase().trim();

        if (!EMAIL_PATTERN.matcher(emailLower).matches()) {
            return false;
        }

        String domain = emailLower.substring(emailLower.indexOf('@') + 1);

        // Verifica duplicação no início (como "hhotmail")
        if (domain.matches("^([a-z])\\1.*")) {
            return false;
        }

        // Verifica domínio começando com número
        if (domain.matches("^[0-9].*")) {
            return false;
        }

        return true;
    }

    // Retorna o email corrigido se houver sugestão
    public String getCorrectedEmail(final String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }

        String emailLower = email.toLowerCase().trim();
        String localPart = emailLower.substring(0, emailLower.indexOf('@'));
        String domain = emailLower.substring(emailLower.indexOf('@') + 1);

        // 1. Verifica no mapa de correções
        if (COMMON_TYPO_CORRECTIONS.containsKey(domain)) {
            return localPart + "@" + COMMON_TYPO_CORRECTIONS.get(domain);
        }

        // 2. Verifica duplicação no início (ex: "hhotmail.com")
        if (domain.matches("^([a-z])\\1.*")) {
            String correctedDomain = domain.substring(1); // Remove a letra duplicada
            // Verifica se o domínio corrigido existe no mapa
            if (COMMON_TYPO_CORRECTIONS.containsValue(correctedDomain)) {
                return localPart + "@" + correctedDomain;
            }
        }

        return null;
    }

    // Método para obter sugestão de correção
    public String getSuggestion(final String email) {
        String corrected = getCorrectedEmail(email);
        if (corrected != null) {
            return corrected.substring(corrected.indexOf('@') + 1);
        }
        return null;
    }
}