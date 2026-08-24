package com.example.juntoscontradengue;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.juntoscontradengue.databinding.ActivityVideoIniciarAppBinding;
import com.example.juntoscontradengue.extras.AppConfig;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VideoIniciarAppActivity extends AppCompatActivity {
    private static final String TAG = "VideoIniciarApp";
    private androidx.appcompat.app.AlertDialog loadingDialog;

    private static final int FALLBACK_TIME_OUT = 180000; // 3 minutos
    private static final int TEMPO_ATIVAR_BOTAO = 10000; // 10 segundos

    private FloatingActionButton btnFecharVideo;
    private VideoView videoIn;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference refVideo;

    // HANDLERS CONTROLADOS
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable fallbackRunnable;
    private Runnable showButtonRunnable;
    private boolean isVideoCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityVideoIniciarAppBinding binding =
                ActivityVideoIniciarAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnFecharVideo = binding.btnFecharVideo;
        videoIn = binding.videoViewInitial;

        // Configurações iniciais
        configurarVideoView();
        configurarBotaoFlutuante();

        boolean isConnected = NetworkUtils.isNetworkAvailable(this);

        if (AppConfig.temLocalidadeSalva(this)) {
            String estado = AppConfig.getEstado(this).toLowerCase();
            String municipio = AppConfig.getMunicipio(this).toLowerCase();

            if (!isConnected) {
                // Sem internet, vai direto para MainActivity
                iniciarMainActivity();
            } else {
                // Com internet, busca o vídeo do Firebase
                refVideo = database.getReference(
                        "cadastros/" + estado + "/" + municipio + "/config/video_inicia_app"
                );
                carregarVideoDoFirebase();
                agendarFallback();

                // Agenda o botão para aparecer após 10 segundos
                agendarBotaoPular();
            }
        } else {
            iniciarEscolherLocalidade();
        }
    }

    private void agendarFallback() {
        fallbackRunnable = this::iniciarMainActivity;
        handler.postDelayed(fallbackRunnable, FALLBACK_TIME_OUT);
    }

    private void cancelarFallback() {
        if (fallbackRunnable != null) {
            handler.removeCallbacks(fallbackRunnable);
            fallbackRunnable = null;
        }
    }

    private void cancelarBotaoPular() {
        if (showButtonRunnable != null) {
            handler.removeCallbacks(showButtonRunnable);
            showButtonRunnable = null;
        }
    }

    private void configurarBotaoFlutuante() {
        // Configura o clique do botão
        btnFecharVideo.setOnClickListener(v -> {
            // Animação de saída
            btnFecharVideo.animate()
                    .alpha(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setDuration(200)
                    .withEndAction(this::iniciarMainActivity)
                    .start();
        });

        // O botão começa invisível
        btnFecharVideo.setVisibility(View.GONE);
        btnFecharVideo.setAlpha(0f);
        btnFecharVideo.setScaleX(0.5f);
        btnFecharVideo.setScaleY(0.5f);
    }

    private void configurarVideoView() {
        videoIn.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            mp.setVolume(1f, 1f);
            videoIn.start();
            hideLoading();

            // Cancela o fallback se o vídeo começou a tocar
            cancelarFallback();
        });

        // QUANDO O VÍDEO TERMINAR, VAI PARA MAINACTIVITY
        videoIn.setOnCompletionListener(mp -> {
            isVideoCompleted = true;

            // Mostra o botão se ainda não foi mostrado
            if (btnFecharVideo.getVisibility() != View.VISIBLE) {
                mostrarBotaoPular();
            }

            // Vai para MainActivity após 1.5 segundos
            handler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    iniciarMainActivity();
                }
            }, 1500);
        });

        videoIn.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Erro no VideoView. what: " + what + ", extra: " + extra);
            iniciarMainActivity();
            return true;
        });
    }

    private void agendarBotaoPular() {
        // Cancela qualquer agendamento anterior
        cancelarBotaoPular();

        showButtonRunnable = () -> {
            // Só mostra se a Activity ainda estiver ativa e o vídeo NÃO tiver terminado ainda
            if (!isFinishing() && !isDestroyed() && !isVideoCompleted) {
                mostrarBotaoPular();
            } else if (!isFinishing() && !isDestroyed()) {
                // Se o vídeo já terminou, mostra o botão mesmo assim (caso não tenha sido mostrado)
                if (btnFecharVideo.getVisibility() != View.VISIBLE) {
                    mostrarBotaoPular();
                }
            }
        };

        handler.postDelayed(showButtonRunnable, TEMPO_ATIVAR_BOTAO);
    }

    private void mostrarBotaoPular() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            btnFecharVideo.setVisibility(View.VISIBLE);
            btnFecharVideo.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .start();
        });
    }

    private void carregarVideoDoFirebase() {
        showLoading();

        refVideo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String urlVideo = snapshot.getValue(String.class);

                if (urlVideo != null && !urlVideo.isEmpty()) {
                    try {
                        videoIn.setVideoURI(Uri.parse(urlVideo));
                        Log.d(TAG, "Vídeo carregado com sucesso: " + urlVideo);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao carregar vídeo", e);
                        hideLoading();
                        iniciarMainActivity();
                    }
                } else {
                    Log.w(TAG, "URL do vídeo vazia ou nula");
                    hideLoading();
                    iniciarMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro ao buscar vídeo do Firebase: " + error.getMessage());
                hideLoading();
                iniciarMainActivity();
            }
        });
    }

    private void iniciarMainActivity() {
        limparRecursos();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

        // Substituindo overridePendingTransition deprecated
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            // Android 14+ usa o novo sistema de animações
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            // Versões anteriores continuam usando o método antigo
            // É seguro usar com suppress warnings para versões antigas
            //noinspection deprecation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void iniciarEscolherLocalidade() {
        limparRecursos();
        Intent intent = new Intent(this, EscolherLocalidadeActivity.class);
        startActivity(intent);
        finish();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            //noinspection deprecation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void showLoading() {
        if (isFinishing() || isDestroyed()) return;

        if (loadingDialog == null) {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(view);
            builder.setCancelable(false);
            loadingDialog = builder.create();
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    @Override
    protected void onPause() {
        super.onPause();
        if (videoIn != null && videoIn.isPlaying()) {
            videoIn.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se o vídeo estava pausado e não terminou, continua
        if (videoIn != null && !videoIn.isPlaying() && !isVideoCompleted) {
            videoIn.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        limparRecursos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limparRecursos();
    }

    private void limparRecursos() {
        hideLoading();
        cancelarFallback();
        cancelarBotaoPular();
        handler.removeCallbacksAndMessages(null);

        if (videoIn != null) {
            try {
                videoIn.stopPlayback();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao parar playback do vídeo", e);
            }
        }
    }
}