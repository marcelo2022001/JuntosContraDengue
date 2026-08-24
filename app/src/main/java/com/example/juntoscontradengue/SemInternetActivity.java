package com.example.juntoscontradengue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.juntoscontradengue.databinding.ActivitySemInternetBinding;

public class SemInternetActivity extends AppCompatActivity {

    Button btnVoltar_sem_internet;
    String id_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySemInternetBinding activitySemInternetBinding = ActivitySemInternetBinding.inflate(getLayoutInflater());
        setContentView(activitySemInternetBinding.getRoot());

        id_activity = getIntent().getStringExtra("id_activity");

        if (id_activity == null || id_activity.isEmpty()) {
            id_activity = "main";
        }

        btnVoltar_sem_internet = activitySemInternetBinding.btnSemWifi;
        btnVoltar_sem_internet.setOnClickListener(v -> voltar_tela_anterior());
    }

    private void voltar_tela_anterior() {
        switch (id_activity) {

            case "main":
            case "agentes":
            case "telefones_uteis":
            case "trabalhos_agentes":
            case "denunciar":
            case "profile":
            case "reclamacoes_usuarios":
            case "descarte_pneus_eletronicos":
            case "termos":
                Intent intent = new Intent(SemInternetActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                break;

            case "reclamacoes_agentes":
            case "cadastro_agente":
            case "excluir_pre_cadastro":
            case "upload_trab_agentes_admin":
            case "excluir_trab_agentes":
            case "substituir_video_inicia_app":
            case "profile_admin":
            case "add_slider":
            case "add_fone":
            case "add_locais_descartes":
            case "excluir_telefones_uteis":
            case "descarte_eletronicos":
            case "excluir_locais_eletronicos":
            case "descarte_pneus":
            case "excluir_locais_pneus":
            case "solicitar_plano":
                Intent intentAdmin = new Intent(SemInternetActivity.this, AdminActivity.class);
                intentAdmin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                break;

            case "agentes_reclamacoes":
            case "upload_trab_agentes_agentes":
            case "agentes_excluir_trab":
            case "profile_admin_agentes":
                Intent intentAgentes = new Intent(SemInternetActivity.this, AgentesMainActivity.class);
                intentAgentes.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                break;
        }

        finish();
    }
}