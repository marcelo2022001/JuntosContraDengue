package com.example.juntoscontradengue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.juntoscontradengue.databinding.ActivityVisualizarNotificacaoBinding;

public class ActivityVisualizarNotificacao extends AppCompatActivity {

    public static final String EXTRA_TITULO = "EXTRA_TITULO";
    public static final String EXTRA_MENSAGEM = "EXTRA_MENSAGEM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_notificacao);

        ActivityVisualizarNotificacaoBinding binding = ActivityVisualizarNotificacaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarVisualizarNotificacao);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView txtTitulo = binding.txtTituloNotificacao;
        TextView txtMensagem = binding.txtMensagemNotificacao;
        Button btnOkVisualizarNotificacao = binding.buttonVisualizarNotificacao;
        btnOkVisualizarNotificacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityVisualizarNotificacao.this, MainActivity.class));
            }
        });

        txtTitulo.setText(getIntent().getStringExtra(EXTRA_TITULO));
        txtMensagem.setText(getIntent().getStringExtra(EXTRA_MENSAGEM));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;

    }
}