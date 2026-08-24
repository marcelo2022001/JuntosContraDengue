package com.example.juntoscontradengue;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.juntoscontradengue.databinding.ActivityFullScreenVisualizarMidiaReclamacoesBinding;

@UnstableApi
public class FullScreenVisualizarMidiaReclamacoes extends AppCompatActivity {

    private ExoPlayer exoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tela cheia
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        com.example.juntoscontradengue.databinding.ActivityFullScreenVisualizarMidiaReclamacoesBinding binding = ActivityFullScreenVisualizarMidiaReclamacoesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String urlVideo = getIntent().getStringExtra("urlMidia");

        Log.d("FULLSCREEN", "URL recebida: " + urlVideo);  // ← LOG IMPORTANTE

        if (urlVideo == null || urlVideo.isEmpty()) {
            Toast.makeText(this, "URL do vídeo não encontrada", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding.btnCloseFullscreen.setOnClickListener(v -> finish());

        // Inicializa player
        exoPlayer = new ExoPlayer.Builder(this).build();
        binding.playerViewFullscreen.setPlayer(exoPlayer);
        binding.playerViewFullscreen.setUseController(true);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(urlVideo));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);  // ← AUTO PLAY

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(FullScreenVisualizarMidiaReclamacoes.this,
                        "Erro: " + error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}