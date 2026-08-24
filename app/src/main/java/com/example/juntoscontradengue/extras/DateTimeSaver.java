package com.example.juntoscontradengue.extras;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeSaver {

    // Retorna timestamp em millissegundos
    public long saveCurrentDateTime() {
        return System.currentTimeMillis();
    }

    // Método adicional se você precisar da data formatada
    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}