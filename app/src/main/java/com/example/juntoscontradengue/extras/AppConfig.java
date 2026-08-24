package com.example.juntoscontradengue.extras;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfig {

    private static final String PREFS_NAME = "configApp";
    private static final String KEY_ESTADO = "estado";
    private static final String KEY_MUNICIPIO = "municipio";

    // 🔹 Salva estado e município
    public static void salvarLocalidade(Context context, String estado, String municipio) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_ESTADO, estado)
                .putString(KEY_MUNICIPIO, municipio)
                .apply();
    }

    // 🔹 Retorna o estado salvo
    public static String getEstado(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ESTADO, null);
    }

    // 🔹 Retorna o município salvo
    public static String getMunicipio(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MUNICIPIO, null);
    }

    // 🔹 Verifica se ambos estão salvos
    public static boolean temLocalidadeSalva(Context context) {
        return getEstado(context) != null && getMunicipio(context) != null;
    }

    // 🔹 Limpa dados (por exemplo, no logout)
    public static void limparLocalidade(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
