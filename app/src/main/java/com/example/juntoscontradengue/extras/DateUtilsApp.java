package com.example.juntoscontradengue.extras;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtilsApp {

    /**
     * Converte timestamp (Long) do Firebase para data legível
     * Exemplo: 1705400000000 → 15/01/2026 14:32
     */
    public static String ConverteDataTimeStampLegivel(Long timestamp) {

        if (timestamp == null) {
            return "Data não informada";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
            );

            Date date = new Date(timestamp);
            return sdf.format(date);

        } catch (Exception e) {
            return "Data inválida";
        }
    }
}
