package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityExcluirProfissionaisPreCadastroBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ExcluirProfissionaisPreCadastro extends AppCompatActivity {

    private static final String TAG = "ContagemCadastros";
    private DatabaseReference databaseReference;
    Toolbar toolbarExcluirAgentes;
    private Button btnExcluirAdmin, btnExcluirAgentes, btnExcluirPreCadAdmins, btnExcluirPreCadAgentes;
    private ImageView exclamacaoButtonAdmin;
    String estado, municipio;
    Boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityExcluirProfissionaisPreCadastroBinding activityExcluirAgentesBinding = ActivityExcluirProfissionaisPreCadastroBinding.inflate(getLayoutInflater());
        setContentView(activityExcluirAgentesBinding.getRoot());

        isConnected = NetworkUtils.isNetworkAvailable(ExcluirProfissionaisPreCadastro.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "excluir_pre_cadastro");
            startActivity(itente);

        }

        toolbarExcluirAgentes = activityExcluirAgentesBinding.toolbarExcluirAgentes;
        setSupportActionBar(toolbarExcluirAgentes);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        btnExcluirAdmin = activityExcluirAgentesBinding.btExcluirAdmin;
        btnExcluirAdmin.setOnClickListener(v -> excluir_admin());

        exclamacaoButtonAdmin = activityExcluirAgentesBinding.exclamacaoExcluirAdmin;
        exclamacaoButtonAdmin.setOnClickListener(v -> exibirMensagem());

        btnExcluirAgentes = activityExcluirAgentesBinding.btExcluirAgentes;
        btnExcluirAgentes.setOnClickListener(v -> excluir_agentes());

        btnExcluirPreCadAdmins = activityExcluirAgentesBinding.btExcluirPreCadastroAdmins;
        btnExcluirPreCadAdmins.setOnClickListener(v -> excluir_pre_cad_admins());

        btnExcluirPreCadAgentes = activityExcluirAgentesBinding.btExcluirPreCadastroAgentes;
        btnExcluirPreCadAgentes.setOnClickListener(v -> excluir_pre_cad_agentes());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        contarCadastrosAdmins();
        contarCadastrosAgentes();
        contarPreCadastroAdmins();
        contarPreCadastroAgentes();

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void contarCadastrosAdmins() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("admins");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obter a contagem de filhos
                    long count = dataSnapshot.getChildrenCount();
                    Log.d(TAG, "Total de cadastros admins: " + count);
                    // Atualizar a UI com a contagem
                    if (count > 1) {
                        btnExcluirAdmin.setEnabled(true);
                        exclamacaoButtonAdmin.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Lidar com possíveis erros
                Log.d(TAG, "Database Error: " + databaseError);

                Toast.makeText(
                        ExcluirProfissionaisPreCadastro.this,
                        "Erro ao contar cadastros"
                                + databaseError.toException(),
                                Toast.LENGTH_LONG
                        ).show();

            }
        });
    }

    private void contarCadastrosAgentes() {
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("agentes");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obter a contagem de filhos
                    long count = dataSnapshot.getChildrenCount();
                    Log.d(TAG, "Total de cadastros agentes: " + count);
                    // Atualizar a UI com a contagem
                    if (count > 0) {
                        btnExcluirAgentes.setEnabled(true);
                    }
                } else{
                    btnExcluirAgentes.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Lidar com possíveis erros
                Log.d(TAG, "Database Error: " + databaseError);

                Toast.makeText(
                        ExcluirProfissionaisPreCadastro.this,
                        "Erro ao contar cadastros"
                                + databaseError.toException(),
                        Toast.LENGTH_LONG
                ).show();

            }
        });
    }

// Conta quantos cadastros/nós tem no bd
private void contarPreCadastroAdmins() {
    databaseReference = FirebaseDatabase.getInstance()
            .getReference("cadastros")
            .child(estado)
            .child(municipio)
            .child("config")
            .child("pre_cadastro_admins");
    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                // Obter a contagem de filhos
                long count = dataSnapshot.getChildrenCount();
                Log.d(TAG, "Total de Pre-cadastros admin: " + count);
                // Atualizar a UI com a contagem
                if (count >= 1) {
                    btnExcluirPreCadAdmins.setEnabled(true);
                }
            } else{
                btnExcluirPreCadAdmins.setEnabled(false);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Lidar com possíveis erros
            Log.d(TAG, "Database Error: " + databaseError);

            Toast.makeText(
                    ExcluirProfissionaisPreCadastro.this,
                    "Erro ao contar cadastros"
                            + databaseError.toException(),
                    Toast.LENGTH_LONG
            ).show();

        }
    });
}
//Conta quantos cadastros/nós tem no bd
    private void contarPreCadastroAgentes() {
        databaseReference = FirebaseDatabase.getInstance().getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("config")
                .child("pre_cadastro_agentes");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obter a contagem de filhos
                    long count = dataSnapshot.getChildrenCount();
                    Log.d(TAG, "Total de pre-cadastros agentes: " + count);
                    // Atualizar a UI com a contagem
                    if (count >= 1) {
                        btnExcluirPreCadAgentes.setEnabled(true);
                    }
                } else{
                    btnExcluirPreCadAgentes.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Lidar com possíveis erros
                Log.d(TAG, "Database Error: " + databaseError);

                Toast.makeText(
                        ExcluirProfissionaisPreCadastro.this,
                        "Erro ao contar cadastros"
                                + databaseError.toException(),
                        Toast.LENGTH_LONG
                ).show();

            }
        });
    }

    private void exibirMensagem() {

        Alertas.showAlertDialog(ExcluirProfissionaisPreCadastro.this,
                "Aviso", "Só poderá excluir administrador(es) se houver mais de um administrador cadastrado." +
                        " Caso queira deixar de ser administrador, entre em contato com o suporte.");
    }

    private void excluir_admin() {
        Intent itente = new Intent(this, ExcluirAgentesActivity.class);
        itente.putExtra("local_db", "admins");
        startActivity(itente);
    }
    private void excluir_agentes() {
        Intent it = new Intent(this, ExcluirAgentesActivity.class);
        it.putExtra("local_db", "agentes");
        startActivity(it);
    }
    private void excluir_pre_cad_admins() {
        Intent itents = new Intent(this, ExcluirPreCadastro.class);
        itents.putExtra("local_db", "pre_cadastro_admins");
        startActivity(itents);
    }

    private void excluir_pre_cad_agentes() {
        Intent ite = new Intent(this, ExcluirPreCadastro.class);
        ite.putExtra("local_db", "pre_cadastro_agentes");
        startActivity(ite);
    }


}