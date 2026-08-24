package com.example.juntoscontradengue.extras;

import android.content.Context;
import android.content.SharedPreferences;

public class TopicHelper {

    public static String getUsuariosTopic(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences("configApp", Context.MODE_PRIVATE);

        String estado = prefs.getString("estado", "");
        String municipio = prefs.getString("municipio", "");

        return (estado + "_" + municipio + "_usuarios")
                .toLowerCase()
                .replace(" ", "_");
    }

    public static String getAgentesTopic(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences("configApp", Context.MODE_PRIVATE);

        String estado = prefs.getString("estado", "");
        String municipio = prefs.getString("municipio", "");

        return (estado + "_" + municipio + "_agentes")
                .toLowerCase()
                .replace(" ", "_");
    }

    public static String getAdminsTopic(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences("configApp", Context.MODE_PRIVATE);

        String estado = prefs.getString("estado", "");
        String municipio = prefs.getString("municipio", "");

        return (estado + "_" + municipio + "_admins")
                .toLowerCase()
                .replace(" ", "_");
    }
    public static String getPerfilTopic(Context context) {

        SharedPreferences prefsConfig =
                context.getSharedPreferences("configApp", Context.MODE_PRIVATE);

        SharedPreferences prefsUser =
                context.getSharedPreferences("UserData", Context.MODE_PRIVATE);

        String estado = prefsConfig.getString("estado", "").toLowerCase();
        String municipio = prefsConfig.getString("municipio", "").toLowerCase();
        String perfil = prefsUser.getString("perfil", "usuarios").toLowerCase();

        return (estado + "_" + municipio + "_" + perfil)
                .replace(" ", "_");
    }
}