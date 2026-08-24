package com.example.juntoscontradengue;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.databinding.ActivityFullscreenBinding;

import java.util.Objects;

public class FullscreenActivity extends AppCompatActivity {

    private ActivityFullscreenBinding binding;
    private ImageView imageViewFullScreen;
    private VideoView videoFullScreen;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );


         binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarFullScreen;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        imageViewFullScreen = binding.imgViewFullScreen;
        videoFullScreen = binding.videoFullScreen;

        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoFullScreen);

        configurarVideoListeners();

        String urlMidia = getIntent().getStringExtra("urlMidia");
        String tipo = getIntent().getStringExtra("tipo");

        exibirLegendaSeHouver();

        if ("video".equals(tipo)) {
            exibirVideo(urlMidia);
        } else {
            exibirImagem(urlMidia);
        }
    }

    private void exibirLegendaSeHouver() {
        String nome = getIntent().getStringExtra("nome");
        String funcao = getIntent().getStringExtra("funcao");

        boolean temNome = !TextUtils.isEmpty(nome);
        boolean temFuncao = !TextUtils.isEmpty(funcao);

        if (!temNome && !temFuncao) {
            binding.painelInfoAgente.setVisibility(View.GONE);
            return;
        }

        binding.txtNomeFullscreen.setVisibility(temNome ? View.VISIBLE : View.GONE);
        if (temNome) binding.txtNomeFullscreen.setText(nome);

        binding.txtFuncaoFullscreen.setVisibility(temFuncao ? View.VISIBLE : View.GONE);
        if (temFuncao) binding.txtFuncaoFullscreen.setText(funcao);

        binding.painelInfoAgente.setVisibility(View.VISIBLE);
    }

    private void configurarVideoListeners() {

        videoFullScreen.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            mp.setVolume(1f, 1f);

            videoFullScreen.requestFocus();
            videoFullScreen.start();
        });

        videoFullScreen.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Erro ao reproduzir vídeo", Toast.LENGTH_LONG).show();
            return true;
        });
    }

    private void exibirImagem(String url) {
        videoFullScreen.setVisibility(View.GONE);
        imageViewFullScreen.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(url)
                //.placeholder(R.drawable.todos_contra_dengue) // Imagem mostrada durante o carregamento
                .error(R.drawable.error_image) // Imagem mostrada se houver erro
                .into(imageViewFullScreen);
    }

    private void exibirVideo(String url) {
        imageViewFullScreen.setVisibility(View.GONE);
        videoFullScreen.setVisibility(View.VISIBLE);

        // Vídeo não tem legenda de nome/função — esconde o painel mesmo se os
        // extras vierem preenchidos por engano.
        binding.painelInfoAgente.setVisibility(View.GONE);

        videoFullScreen.setMediaController(mediaController);
        videoFullScreen.setVideoURI(Uri.parse(url));
        videoFullScreen.requestFocus();
        videoFullScreen.start();
    }
}
