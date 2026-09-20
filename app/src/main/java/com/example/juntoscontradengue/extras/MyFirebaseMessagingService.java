package com.example.juntoscontradengue.extras;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.juntoscontradengue.ActivityVisualizarNotificacao;
import com.example.juntoscontradengue.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String titulo = null;
        String mensagem = null;

        // Prioriza o payload "data" (chega mesmo com o app em foreground)
        Map<String, String> data = remoteMessage.getData();
        if (!TextUtils.isEmpty(data.toString())) {
            titulo = data.get("titulo");
            mensagem = data.get("mensagem");
        }

        // Fallback pro payload "notification", caso "data" não venha por algum motivo
        if (remoteMessage.getNotification() != null) {
            if (titulo == null) titulo = remoteMessage.getNotification().getTitle();
            if (mensagem == null) mensagem = remoteMessage.getNotification().getBody();
        }

        if (titulo != null || mensagem != null) {
            exibirNotificacaoVisual(titulo, mensagem);
        }
    }

    private void exibirNotificacaoVisual(String titulo, String mensagem) {
        String channelId = "default";

        // Agora abre direto a tela que exibe a mensagem, não mais a MainActivity crua
        Intent intent = new Intent(this, ActivityVisualizarNotificacao.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ActivityVisualizarNotificacao.EXTRA_TITULO, titulo);
        intent.putExtra(ActivityVisualizarNotificacao.EXTRA_MENSAGEM, mensagem);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, new Random().nextInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(new Random().nextInt(), builder.build());
        }
    }
}