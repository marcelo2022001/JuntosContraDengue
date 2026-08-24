package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityAddTelefonesUteisBinding;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddTelefonesUteis extends AppCompatActivity {

    private ActivityAddTelefonesUteisBinding bindingAddTelefonesuteis;
    private EditText local_add_telefone, telefone_add_telefone;
    String estado, municipio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        boolean isConnected = NetworkUtils.isNetworkAvailable(AddTelefonesUteis.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "add_fone");
            startActivity(itente);

        }

        bindingAddTelefonesuteis = ActivityAddTelefonesUteisBinding.inflate(getLayoutInflater());
        setContentView(bindingAddTelefonesuteis.getRoot());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        setupToolbar();
        initializeViews();
        applyMasks();
    }

    private void applyMasks() {
        telefone_add_telefone.addTextChangedListener(MaskEditUtil.maskTelefone());
    }

    private void setupToolbar() {
        Toolbar toolbar = bindingAddTelefonesuteis.toolbarAddTelefonesUteis;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getTitle());
    }

    private void initializeViews() {
        local_add_telefone = bindingAddTelefonesuteis.edtTxtAddTelefonesUteisLocal;
        telefone_add_telefone = bindingAddTelefonesuteis.edtTxtAddTelefonesUteisTelefone;

        Button btnSalvar_add_telefone = bindingAddTelefonesuteis.btnEnviarAddAddTelefoneUteis;
        Button btnCancelar_add_telefone = bindingAddTelefonesuteis.btnCancelarAddTelefoneUteis;

        btnCancelar_add_telefone.setOnClickListener(v -> voltaMainAdmin());

        btnSalvar_add_telefone.setOnClickListener(v -> salvar_telefone());

    }

    private void voltaMainAdmin() {
        
        startActivity(new Intent(AddTelefonesUteis.this, AdminActivity.class));
        
    }

    private void salvar_telefone() {

        String local = local_add_telefone.getText().toString().trim();
        String telefone = telefone_add_telefone.getText().toString().trim();

        if (!(local.isEmpty()) && !(telefone.isEmpty())) {

            FirebaseDatabase mdatabaseTelefonesUteis = FirebaseDatabase.getInstance();

            DatabaseReference ref_telefones_uteis = mdatabaseTelefonesUteis
                    .getReference("cadastros")
                    .child(estado)
                    .child(municipio)
                    .child("telefones_uteis");

            //pegar info. salvar no real time database
            Map<String, Object> map_telefones_uteis = new HashMap<>();
            map_telefones_uteis.put("local", local);
            map_telefones_uteis.put("telefone", telefone);

            ref_telefones_uteis.push().setValue(map_telefones_uteis);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Aviso!");
            builder.setMessage("Dados salvos com sucesso! Deseja adicionar mais números?");

// Botão OK
            builder.setPositiveButton("OK", (dialog, which) -> {
                // Limpar campos
                local_add_telefone.setText("");
                telefone_add_telefone.setText("");
            });

// Botão Cancelar
            builder.setNegativeButton("Cancelar", (dialog, which) -> {
                // Voltar para MainActivity
                Intent intent = new Intent(AddTelefonesUteis.this, MainActivity.class);
                startActivity(intent);
                finish();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }
    }

}