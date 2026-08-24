package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterExcluirAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassAgentes;
import com.example.juntoscontradengue.databinding.ActivityExcluirAgentesBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcluirAgentesActivity extends AppCompatActivity {
    private String estado, municipio, local_database, db_total_cadastro;
    private final List<ClassAgentes> classAgentesExcluir = new ArrayList<>();
    private AdapterExcluirAgentes adapterAgentesExcluir;
    private DatabaseReference databaseReferenceExcluirAgentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityExcluirAgentesBinding bindingExcluirAgentes = ActivityExcluirAgentesBinding.inflate(getLayoutInflater());
        setContentView(bindingExcluirAgentes.getRoot());

        Toolbar toolbarAgentesExcluir = bindingExcluirAgentes.tbAgentesExcluir;
        setSupportActionBar(toolbarAgentesExcluir);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Pega os dados trazidos da intent ExcluirProfissionaisPreCadastro
        Intent intent = getIntent();
        local_database = intent.getStringExtra("local_db"); // P


        // Recupera localização dos SharedPreferences
        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        // Configuração do RecyclerView e do Listener de Clique do Adapter
        RecyclerView recyclerView = bindingExcluirAgentes.rvAgentesExcluir;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializa o adapter (ajustado para o novo construtor se necessário)
        adapterAgentesExcluir = new AdapterExcluirAgentes(classAgentesExcluir);
        recyclerView.setAdapter(adapterAgentesExcluir);

        // CONFIGURAÇÃO DO CLIQUE PARA EXCLUSÃO
        adapterAgentesExcluir.setOnAgenteDeleteListener((agente, position) -> {
            // Chama o diálogo de confirmação passando o objeto do agente
            confirmarExclusao(agente);
        });

        // Referência do Realtime Database
        databaseReferenceExcluirAgentes = FirebaseDatabase.getInstance().getReference("cadastros")
                .child(estado).child(municipio).child(local_database);

        ouvirEventosBanco();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void confirmarExclusao(ClassAgentes agente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmação");
        builder.setMessage("Deseja excluir permanentemente o agente " + agente.getNome() + "?");

        builder.setPositiveButton("Excluir", (dialog, which) -> executarExclusaoCompleta(agente));

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void executarExclusaoCompleta(ClassAgentes agente) {
        String uuid = agente.getUuid(); // Captura o UUID do objeto

        // 1. EXCLUIR DO AUTHENTICATION (Via Cloud Function)
        // É necessário criar a função 'recursiveDelete' ou 'deleteUser' no Firebase Functions
        FirebaseFunctions.getInstance()
                .getHttpsCallable("deleteUser")
                .call(uuid)
                .addOnFailureListener(e -> Log.e("AUTH_DELETE", "Erro Auth: " + e.getMessage()));

        // 2. EXCLUIR DO REALTIME DATABASE
        databaseReferenceExcluirAgentes.child(uuid).removeValue()
                .addOnSuccessListener(aVoid -> {

                    // 3. EXCLUIR DO STORAGE
                    // Caminho conforme sua regra: estado/municipio/imgAgentes/uuid
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                            .child(estado)
                            .child(municipio)
                            .child("imgAgentes")
                            .child(uuid);

                    storageRef.delete().addOnCompleteListener(task -> Toast.makeText(this, "Agente removido com sucesso!", Toast.LENGTH_SHORT).show());

                    if (local_database.equals("pre_cadastro_admins")) {
                        db_total_cadastro = "total_admins";
                    } else {
                        db_total_cadastro = "total_agentes";
                    }

                    DatabaseReference preCadastroSoma = FirebaseDatabase.getInstance()
                            .getReference("cadastros")
                            .child(estado)
                            .child(municipio)
                            .child(local_database)
                            .child(db_total_cadastro);

                    preCadastroSoma.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer valorAtual = currentData.getValue(Integer.class);

                            if (valorAtual == null) {
                                return Transaction.success(currentData);
                            }

                            currentData.setValue(valorAtual - 1);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error,
                                               boolean committed,
                                               @Nullable DataSnapshot currentData) {

                            if (committed) {
                                startActivity(new Intent(ExcluirAgentesActivity.this, ExcluirProfissionaisPreCadastro.class));
                                Log.d("TOTAL_UPDATE", "Total atualizado com sucesso");
                            } else {
                                Log.e("TOTAL_UPDATE", "Erro ao atualizar total");
                            }

                        }
                    });
                });
    }

    private void ouvirEventosBanco() {
        databaseReferenceExcluirAgentes.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClassAgentes agente = snapshot.getValue(ClassAgentes.class);
                if (agente != null) {
                    classAgentesExcluir.add(agente);
                    adapterAgentesExcluir.notifyItemInserted(classAgentesExcluir.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClassAgentes agenteEditado = snapshot.getValue(ClassAgentes.class);
                int index = findAgenteIndexById(snapshot.getKey());
                if (index != -1 && agenteEditado != null) {
                    classAgentesExcluir.set(index, agenteEditado);
                    adapterAgentesExcluir.notifyItemChanged(index);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = findAgenteIndexById(snapshot.getKey());
                if (index != -1) {
                    classAgentesExcluir.remove(index);
                    adapterAgentesExcluir.notifyItemRemoved(index);
                }
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private int findAgenteIndexById(String key) {
        for (int i = 0; i < classAgentesExcluir.size(); i++) {
            if (classAgentesExcluir.get(i).getUuid().equals(key)) {
                return i;
            }
        }
        return -1;
    }
}