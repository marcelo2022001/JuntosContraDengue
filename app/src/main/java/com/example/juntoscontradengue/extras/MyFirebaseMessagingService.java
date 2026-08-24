package com.example.juntoscontradengue.extras;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.juntoscontradengue.MainActivity;
import com.example.juntoscontradengue.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Verifica se a mensagem veio com notificação
        if (remoteMessage.getNotification() != null) {
            exibirNotificacaoVisual(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    private void exibirNotificacaoVisual(String titulo, String mensagem) {
        String channelId = "default"; // O mesmo ID que usamos antes

        // Configura o que acontece ao clicar na notificação (abre a MainActivity)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Use o ícone do seu app
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Força o pop-up (Heads-up)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(new Random().nextInt(), builder.build());
        }
    }
}
