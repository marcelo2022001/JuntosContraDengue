package com.example.juntoscontradengue;

import static android.view.View.INVISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.EscolherPerfilLoginBinding;

public class EscolherPerfilLogin extends AppCompatActivity {

    private EscolherPerfilLoginBinding bindingEscolhaLogin;
    TextView estado_municipio;
    String estado, municipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        bindingEscolhaLogin = EscolherPerfilLoginBinding.inflate(getLayoutInflater());
        setContentView(bindingEscolhaLogin.getRoot());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

         if(!TextUtils.isEmpty(estado) && !TextUtils.isEmpty(municipio)){
              estado_municipio = bindingEscolhaLogin.municipioEstado;
             estado_municipio.setText(String.format("%s/%s", municipio.toUpperCase(), estado.toUpperCase()));
         }  else{
             // Não tem dados - mantém texto padrão ou exibe mensagem
             estado_municipio.setVisibility(INVISIBLE);
    }



        // Configura a Toolbar para que o botão de voltar (home) funcione
        setupToolbar();

        // Tratamento único e correto do botão voltar físico/gesto
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                voltarParaMain();
            }
        });

        Button cidadao = bindingEscolhaLogin.btnCidadao;
        Button agente = bindingEscolhaLogin.btnAgente;
        Button admin = bindingEscolhaLogin.btnAdmin;

        cidadao.setOnClickListener(v -> loginUsuario());
        admin.setOnClickListener(v -> loginAdmin());
        agente.setOnClickListener(v -> loginAgentesEndemias());
    }

    private void setupToolbar() {

        Toolbar toolbarOpcaoLogin = bindingEscolhaLogin.topAppBar;
        setSupportActionBar(toolbarOpcaoLogin);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Trata o clique da seta voltar da Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            voltarParaMain();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Método centralizado para garantir que ambos os botões façam a mesma ação
    private void voltarParaMain() {
        Intent intent = new Intent(EscolherPerfilLogin.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loginUsuario() {
        startActivity(new Intent(EscolherPerfilLogin.this, TelaLoguin.class));
    }

    private void loginAdmin() {
        startActivity(new Intent(EscolherPerfilLogin.this, ActivityLoginAdmin.class));
    }

    private void loginAgentesEndemias() {
        startActivity(new Intent(EscolherPerfilLogin.this, ActivityLoginAgentes.class));
    }
}