package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String local_database;
    private String db_total_cadastro;
    private final List<ClassAgentes> classAgentesExcluir = new ArrayList<>();
    private AdapterExcluirAgentes adapterAgentesExcluir;
    private DatabaseReference databaseReferenceExcluirAgentes;
    private FirebaseDatabase databaseMunicipio;
    private String uidLogado, uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityExcluirAgentesBinding bindingExcluirAgentes = ActivityExcluirAgentesBinding.inflate(getLayoutInflater());
        setContentView(bindingExcluirAgentes.getRoot());

        Toolbar toolbarAgentesExcluir = bindingExcluirAgentes.tbAgentesExcluir;
        setSupportActionBar(toolbarAgentesExcluir);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FirebaseUser usuarioLogado = FirebaseAuth.getInstance().getCurrentUser();
        uidLogado = usuarioLogado != null ? usuarioLogado.getUid() : null;

        //Pega os dados trazidos da intent ExcluirProfissionaisPreCadastro
        Intent intent = getIntent();
        local_database = intent.getStringExtra("local_db"); // P


        // Recupera localização dos SharedPreferences
        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        String estado = prefs.getString("estado", null);
        String municipio = prefs.getString("municipio", null);

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

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
        databaseReferenceExcluirAgentes = databaseMunicipio.getReference().child(local_database);

        ouvirEventosBanco();
    }

    @Override
    public boolean onSupportNavigateUp() {
        irParaMain();
        return true;
    }

    private void irParaMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void confirmarExclusao(ClassAgentes agente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmação");
        builder.setMessage("Deseja excluir permanentemente o agente " + agente.getNome() + "?");

        builder.setPositiveButton("Excluir", (dialog, which) -> executarExclusaoCompleta(agente));

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private interface UrlCallback {
        void onUrl(String urlImagem);
    }

    private interface CpfCallback {
        void onCpf(String cpfSemMascara);
    }

    private void executarExclusaoCompleta(ClassAgentes agente) {
         uuid = agente.getUuid();

        if (ehOProprioAdmin(uuid)) return;

        boolean isAgente = "logins/agentes".equals(local_database);

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);

        java.util.Map<String, Object> dados = new java.util.HashMap<>();
        dados.put("uid", uuid);
        dados.put("estado", prefs.getString("estado", null));
        dados.put("municipio", prefs.getString("municipio", null));

        // Se a função estiver em outra região: FirebaseFunctions.getInstance("southamerica-east1")
        FirebaseFunctions.getInstance()
                .getHttpsCallable("deleteUser")
                .call(dados)
                .addOnSuccessListener(result -> {
                    // Auth apagado: agora limpa o resto
                    obterCpf(uuid, cpf ->
                            obterUrlImagem(agente, isAgente, urlImagem -> databaseReferenceExcluirAgentes.child(uuid).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        if (isAgente) {
                                            databaseMunicipio.getReference()
                                                    .child("agentes_publico").child(uuid).removeValue();
                                        }
                                        excluirIndices(cpf, isAgente);
                                        excluirImagemStorage(urlImagem);

                                        db_total_cadastro = local_database.equals("logins/admins")
                                                ? "total_admins" : "total_agentes";
                                        atualizarContador();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Erro ao excluir: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show()))
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e("AUTH_DELETE", "Erro Auth: " + e.getMessage(), e);
                    Toast.makeText(this, "Não foi possível excluir a conta de login: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void obterCpf(String uuid, CpfCallback callback) {
        databaseReferenceExcluirAgentes.child(uuid).child("cpf")
                .get()
                .addOnCompleteListener(task -> {
                    String cpf = null;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String bruto = task.getResult().getValue(String.class);
                        if (bruto != null) {
                            cpf = bruto.replaceAll("\\D", ""); // remove máscara
                        }
                    }
                    callback.onCpf(cpf);
                });
    }

    private void excluirIndices(String cpf, boolean isAgente) {
        if (TextUtils.isEmpty(cpf)) {
            Log.e("INDEX_DELETE", "CPF não encontrado, índices não removidos");
            return;
        }

        DatabaseReference raiz = databaseMunicipio.getReference();
        String noEmail = isAgente ? "index_email_agentes" : "index_email_admin";

        raiz.child("cpf_index").child(cpf).removeValue()
                .addOnFailureListener(e ->
                        Log.e("INDEX_DELETE", "Erro cpf_index: " + e.getMessage()));

        raiz.child(noEmail).child(cpf).removeValue()
                .addOnFailureListener(e ->
                        Log.e("INDEX_DELETE", "Erro " + noEmail + ": " + e.getMessage()));
    }

    private void obterUrlImagem(ClassAgentes agente, boolean isAgente, UrlCallback callback) {
        if (!TextUtils.isEmpty(agente.getUrlImagem())) {
            callback.onUrl(agente.getUrlImagem());
            return;
        }
        if (!isAgente) {          // admin não tem imagem
            callback.onUrl(null);
            return;
        }
        // Alternativa: agentes antigos podem ter a URL só em agentes_publico
        databaseMunicipio.getReference()
                .child("agentes_publico").child(agente.getUuid()).child("urlImagem")
                .get()
                .addOnCompleteListener(task -> {
                    String url = null;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        url = task.getResult().getValue(String.class);
                    }
                    callback.onUrl(url);
                });
    }

    private void excluirImagemStorage(String urlImagem) {
        if (TextUtils.isEmpty(urlImagem)) {
            excluirImagemPorUuid(uuid, () ->
                    Toast.makeText(this, "Agente removido com sucesso!", Toast.LENGTH_SHORT).show());
            return;
        }
        try {
            FirebaseStorage.getInstance().getReferenceFromUrl(urlImagem).delete()
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Agente removido com sucesso!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Log.e("STORAGE_DELETE", "Erro ao excluir imagem: " + e.getMessage());
                        Toast.makeText(this, "Agente removido, mas houve erro ao excluir a imagem",
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (IllegalArgumentException e) {
            Log.e("STORAGE_DELETE", "URL de imagem inválida: " + e.getMessage());
            Toast.makeText(this, "Agente removido, mas a URL da imagem é inválida",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirImagemPorUuid(String uuid, Runnable aoFinal) {
        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        String estado = prefs.getString("estado", null);
        String municipio = prefs.getString("municipio", null);

        FirebaseStorage.getInstance().getReference()
                .child(estado + "/" + municipio + "/imgAgentes")
                .listAll()
                .addOnSuccessListener(lista -> {
                    for (StorageReference item : lista.getItems()) {
                        if (item.getName().endsWith("_" + uuid)) {
                            item.delete()
                                    .addOnFailureListener(e -> Log.e("STORAGE_DELETE", e.getMessage()));
                        }
                    }
                    aoFinal.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("STORAGE_DELETE", "listAll falhou: " + e.getMessage());
                    aoFinal.run();
                });
    }

    private void atualizarContador() {
        databaseMunicipio.getReference().child("config").child(db_total_cadastro)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Long valorAtual = currentData.getValue(Long.class);
                        if (valorAtual == null) return Transaction.success(currentData);
                        currentData.setValue(valorAtual + 1);   // libera uma vaga
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed,
                                           @Nullable DataSnapshot currentData) {
                        if (committed) {
                            startActivity(new Intent(ExcluirAgentesActivity.this,
                                    ExcluirProfissionaisPreCadastro.class));
                        } else {
                            Log.e("TOTAL_UPDATE", "Erro ao atualizar total");
                        }
                    }
                });
    }

    private void ouvirEventosBanco() {
        databaseReferenceExcluirAgentes.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (ehOProprioAdmin(snapshot.getKey())) return;   // <- não mostra o próprio cadastro

                ClassAgentes agente = snapshot.getValue(ClassAgentes.class);
                if (agente != null) {
                    classAgentesExcluir.add(agente);
                    adapterAgentesExcluir.notifyItemInserted(classAgentesExcluir.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (ehOProprioAdmin(snapshot.getKey())) return;   // <- idem

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

    // Só esconde o próprio cadastro na lista de admins
    private boolean ehOProprioAdmin(String key) {
        return "logins/admins".equals(local_database)
                && uidLogado != null
                && uidLogado.equals(key);
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
