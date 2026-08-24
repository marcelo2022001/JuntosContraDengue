package com.example.juntoscontradengue.extras;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void checkNetworkAndExecute(Context context, Runnable task) {
        if (isNetworkAvailable(context)) {
            try {
                task.run(); // Executa a tarefa se a conexão estiver disponível
            } catch (Exception e) {
                // Tratamento de erro para a tarefa
                Toast.makeText(context, "Ocorreu um erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            // Mensagem de erro para falta de conexão
            Toast.makeText(context, "Sem conexão com a internet. Verifique sua conexão e tente novamente.", Toast.LENGTH_LONG).show();
        }
    }
}

