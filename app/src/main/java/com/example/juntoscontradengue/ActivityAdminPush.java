package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.juntoscontradengue.databinding.ActivityAdminPushBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.TopicHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ActivityAdminPush extends AppCompatActivity {

    private ActivityAdminPushBinding binding;
    String titulo, mensagem, estado, municipio;
    Boolean isConnected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminPushBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.tbAdminPush);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Painel de Notificações");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        binding.btnEnviarPushGlobal.setOnClickListener(v -> validarEnvio());
        binding.btnCancelarPushGlobal.setOnClickListener(v -> cancelarEnvio());
    }

    private void validarEnvio() {

        isConnected = NetworkUtils.isNetworkAvailable(ActivityAdminPush.this);

        if (!isConnected) {

            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Sem internet, ative o wifi ou os dados móveis.", Snackbar.LENGTH_INDEFINITE);
// Add an action button
            snackbar.setAction("OK", v -> {
                // Code to run when user clicks "OK"
            });

// Set custom background color (requires Material Components theme)
            snackbar.setBackgroundTint(Color.RED);
            snackbar.setTextColor(Color.WHITE);

            snackbar.show();

        }

        titulo = Objects.requireNonNull(binding.editTituloPush.getText()).toString().trim();
        mensagem = Objects.requireNonNull(binding.editMensagemPush.getText()).toString().trim();

        // Verificação de segurança para as variáveis de localização
        if (estado == null || municipio == null) {
            Toast.makeText(this, "Erro: Estado ou Município não configurados.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!titulo.isEmpty() && !mensagem.isEmpty()) {
            // AJUSTE: Tópicos DEVEM ser minúsculos e sem espaços no Firebase
            enviarPushPorTopico();
        } else {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarPushPorTopico() {

        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setView(new ProgressBar(this))
                .setMessage("Enviando comando para o servidor...")
                .setCancelable(false)
                .show();

        String topicoUsuarios = TopicHelper.getUsuariosTopic(this);
        String topicoAgentes = TopicHelper.getAgentesTopic(this);

        DatabaseReference refGatilho = FirebaseDatabase.getInstance()
                .getReference("notifications_queue");

        HashMap<String, Object> notificacao = new HashMap<>();

        notificacao.put("titulo", titulo);
        notificacao.put("mensagem", mensagem);

        ArrayList<String> topicos = new ArrayList<>();
        topicos.add(topicoUsuarios);
        topicos.add(topicoAgentes);

        notificacao.put("topicos", topicos);

        refGatilho.push().setValue(notificacao)
                .addOnCompleteListener(task -> {

                    progressDialog.dismiss();

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                ActivityAdminPush.this,
                                "Notificação enviada para usuários e equipe.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                    } else {

                        Toast.makeText(
                                ActivityAdminPush.this,
                                "Erro ao gravar no banco.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void cancelarEnvio() {
        finish(); // Apenas fecha esta activity e volta para a anterior
    }

    // Lógica para a seta de voltar na Toolbar funcionar
    @Override
    public boolean onSupportNavigateUp() {
        // Esta é a forma moderna de disparar o evento de voltar
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}