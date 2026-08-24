package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.google.firebase.auth.FirebaseAuth;

public class AgentesMainActivity extends AppCompatActivity {

    private String nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.juntoscontradengue.databinding.ActivityMainAgentesBinding bidingAgentes = com.example.juntoscontradengue.databinding.ActivityMainAgentesBinding.inflate(getLayoutInflater());
        setContentView(bidingAgentes.getRoot());


        // 3️⃣ Configuração da Toolbar
        Toolbar toolbar = bidingAgentes.toolbarMainAgentes;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false   );
        }
        toolbar.setTitle(getTitle());

        // 4️⃣ Recuperação do Nome do Usuário
        TextView textView = bidingAgentes.txtNomeAgenteTelaMain;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            nome = extras.getString("nome_usuario");
        }

        if (!TextUtils.isEmpty(nome)){
            textView.setText(nome);
        } else {
            SharedPreferences prefUser = getSharedPreferences("UserData", MODE_PRIVATE);
            nome = prefUser.getString("nome", "");
            if (!TextUtils.isEmpty(nome)) {
                textView.setText(nome.toUpperCase());
            } else {
                textView.setText(R.string.agente_de_saude);
            }
        }

        // 5️⃣ Mapeamento e Cliques dos ImageButtons
        ImageButton reclamacao = bidingAgentes.btnTelaAgentesAcompReclamacoes;
        ImageButton add_trab_agentes = bidingAgentes.btnAddTrabAgentes;
        ImageButton excluir_trab_agentes = bidingAgentes.btnExcluiTrabAgente;
        ImageButton config_conta = bidingAgentes.btnConfigAgentes;
        ImageButton termosUso = bidingAgentes.btnTermosTelaAgentes;
        ImageButton sair_conta = bidingAgentes.btnSairAgentes;

        reclamacao.setOnClickListener(v -> verifica_reclamacao());
        add_trab_agentes.setOnClickListener(v -> adicionarTrabCampo());
        excluir_trab_agentes.setOnClickListener(v -> excluirTrabCampo());
        config_conta.setOnClickListener(v -> config_conta());
        termosUso.setOnClickListener( v ->  termos_uso_privacidade());
        sair_conta.setOnClickListener(v -> sairConta());

        // 6️⃣ Interceptação do botão físico de voltar do Android
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AgentesMainActivity.this, "Use o botão Sair.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Trava de Segurança em tempo real
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToMainActivity();
        }
    }

    private void verifica_reclamacao() {
        Intent it = new Intent(this, ListarReclamacoesAgentes.class);
        startActivity(it);
    }

    private void adicionarTrabCampo() {
        startActivity(new Intent(this, UploadTrabAgentes.class));
    }

    private void excluirTrabCampo() {
        startActivity(new Intent(this, ExcluirTrabAgentesActivity.class));
    }

    private void config_conta() {
        Intent itent_agentes = new Intent(this, ProfileActivity.class);
        startActivity(itent_agentes);
    }

    private void termos_uso_privacidade() {
        Intent itent_agentes = new Intent(this, TermosDeUsoActivity.class);
        startActivity(itent_agentes);
    }

    private void sairConta() {
        FirebaseAuth.getInstance().signOut();

        CredentialManager credentialManager = CredentialManager.create(this);
        ClearCredentialStateRequest request = new ClearCredentialStateRequest();

        credentialManager.clearCredentialStateAsync(request, null, Runnable::run, new CredentialManagerCallback<Void, ClearCredentialException>() {
            @Override
            public void onResult(Void result) {
                goToMainActivity();
            }

            @Override
            public void onError(@NonNull ClearCredentialException e) {
                goToMainActivity();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(AgentesMainActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
        Toast.makeText(this, "Usuário deslogado. Você pode entrar com outro login se preferir.", Toast.LENGTH_LONG).show();
    }
}