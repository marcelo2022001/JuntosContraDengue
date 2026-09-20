package com.example.juntoscontradengue.extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

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

    /** Chame logo após login ou criação de conta bem-sucedidos. */
    public static void inscreverNoTopicoDoPerfil(Context context, String perfil) {
        SharedPreferences prefs = context.getSharedPreferences("configApp", Context.MODE_PRIVATE);
        String estado = prefs.getString("estado", "");
        String municipio = prefs.getString("municipio", "");

        if (estado.isEmpty() || municipio.isEmpty() || perfil == null || perfil.isEmpty()) {
            Log.e("FCM", "Não foi possível inscrever no tópico: estado/município/perfil vazio");
            return;
        }

        String topicoUsuarios = (estado + "_" + municipio + "_usuarios").toLowerCase().replace(" ", "_");
        String topicoAdmins   = (estado + "_" + municipio + "_admins").toLowerCase().replace(" ", "_");
        String topicoAgentes  = (estado + "_" + municipio + "_agentes").toLowerCase().replace(" ", "_");

        // Garante que não fica inscrito em mais de um por engano
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicoUsuarios);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicoAdmins);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicoAgentes);

        String topicoAlvo;
        switch (perfil.toLowerCase()) {
            case "admins":  topicoAlvo = topicoAdmins;  break;
            case "agentes": topicoAlvo = topicoAgentes; break;
            default:        topicoAlvo = topicoUsuarios; break;
        }

        FirebaseMessaging.getInstance().subscribeToTopic(topicoAlvo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Inscrito com sucesso em: " + topicoAlvo);
                    } else {
                        Log.e("FCM", "FALHA ao inscrever em: " + topicoAlvo, task.getException());
                    }
                });
    }

    /** Chame no logout, antes de ir pra tela de escolha de perfil. */
    public static void sairDoTopicoAtual(Context context) {
        SharedPreferences prefsConfig = context.getSharedPreferences("configApp", Context.MODE_PRIVATE);
        SharedPreferences prefsUser = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);

        String estado = prefsConfig.getString("estado", "");
        String municipio = prefsConfig.getString("municipio", "");
        String perfil = prefsUser.getString("perfil", "");

        if (estado.isEmpty() || municipio.isEmpty() || perfil.isEmpty()) return;

        String topico = (estado + "_" + municipio + "_" + perfil).toLowerCase().replace(" ", "_");

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topico)
                .addOnCompleteListener(task ->
                        Log.d("FCM", "Saiu do tópico " + topico + ": " + task.isSuccessful()));
    }
}