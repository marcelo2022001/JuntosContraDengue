package com.example.juntoscontradengue;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.juntoscontradengue.database.adapters.AdapterExcluirTrabAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassTrabAgentes;
import com.example.juntoscontradengue.databinding.ActivityExcluirTrabAgentesBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcluirTrabAgentesActivity extends AppCompatActivity {
    private final DatabaseReference dbContador = FirebaseDatabase.getInstance().getReference("cadastros");
    private final List<ClassTrabAgentes> classTrabAgentesExcluir = new ArrayList<>();
    private AdapterExcluirTrabAgentes adapterTrabAgentesExcluir; // Make adapter a field to access it throughout the class
    private DatabaseReference databaseReferenceExcluirTrabAgentes;
    private TextView txtNadaExcluir;
    String estado, municipio;
    private MenuItem menuItemExcluirSelecionados;
    private static final String TITULO_PADRAO = "EXCLUIR TRABALHOS DOS AGENTES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        boolean isConnected = NetworkUtils.isNetworkAvailable(ExcluirTrabAgentesActivity.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "agentes_excluir_trab");
            startActivity(itente);

        }

        // Initialize binding first before setting content view
        ActivityExcluirTrabAgentesBinding bindingExcluirTrabAgentes = ActivityExcluirTrabAgentesBinding.inflate(getLayoutInflater());
        setContentView(bindingExcluirTrabAgentes.getRoot());


        Toolbar toolbarTrabAgentesExcluir = bindingExcluirTrabAgentes.tbTrabAgentesExcluir;
        setSupportActionBar(toolbarTrabAgentesExcluir);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbarTrabAgentesExcluir.setNavigationOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        // Setup RecyclerView
        RecyclerView rcExcluirTrabAgentes = bindingExcluirTrabAgentes.recyclerViewExcluirTrabAgentes;
        // Configuração do RecyclerView
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager (2, StaggeredGridLayoutManager.VERTICAL);
        rcExcluirTrabAgentes.setLayoutManager(staggeredGridLayoutManager);

        adapterTrabAgentesExcluir = new AdapterExcluirTrabAgentes(
                this, classTrabAgentesExcluir, this::atualizarMenuSelecao);
        rcExcluirTrabAgentes.setAdapter(adapterTrabAgentesExcluir);

        txtNadaExcluir = bindingExcluirTrabAgentes.txtNadaParaExcluir;


        // Reference to database
        databaseReferenceExcluirTrabAgentes = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("trabalhos_agentes");

        databaseReferenceExcluirTrabAgentes.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

                Log.d("Firebase", "onChildAdded chamado");
                Log.d("Firebase", "Key = " + snapshot.getKey());
                Log.d("Firebase", "Existe = " + snapshot.exists());
                Log.d("Firebase", "Valor = " + snapshot.getValue());
              if(snapshot.exists()){

                  rcExcluirTrabAgentes.setVisibility(View.VISIBLE);
                  txtNadaExcluir.setVisibility(View.GONE);

              }

                ClassTrabAgentes classTrabAgentes =
                        snapshot.getValue(ClassTrabAgentes.class);

                Log.d("Firebase", "Objeto = " + classTrabAgentes);

                if (classTrabAgentes != null) {
                    classTrabAgentes.setId(snapshot.getKey());

                    // 1. Primeiro, adicione o item à sua fonte de dados
                    classTrabAgentesExcluir.add(classTrabAgentes);

                    Log.d("Firebase", "Total itens = " + classTrabAgentesExcluir.size());

                    // 2. Obtenha o índice do item recém-adicionado/excluido
                    int position = classTrabAgentesExcluir.size() - 1;

                    // 3. Notifique o adaptador sobre a inserção exata
                    adapterTrabAgentesExcluir.notifyItemInserted(position);

                 // 4. (Opcional, mas recomendado) Atualize as posições de layout dos itens
                    adapterTrabAgentesExcluir.notifyItemRangeChanged(position, classTrabAgentesExcluir.size());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                ClassTrabAgentes classTrabAgenteExcluir = snapshot.getValue(ClassTrabAgentes.class);
                if (classTrabAgenteExcluir != null) {
                    classTrabAgenteExcluir.setId(snapshot.getKey());
                    int index = findAgenteIndexById(snapshot.getKey());
                    if (index != -1) {

                        classTrabAgentesExcluir.set(index, classTrabAgenteExcluir);
                        adapterTrabAgentesExcluir.notifyItemChanged(index);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = findAgenteIndexById(snapshot.getKey());
                if (index != -1) {
                    classTrabAgentesExcluir.remove(index);
                    adapterTrabAgentesExcluir.notifyItemRemoved(index);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not implemented
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Erro ao ler os dados: " + error.getMessage());
                Toast.makeText(ExcluirTrabAgentesActivity.this, "Erro ao carregar dados dos agentes", LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_excluir_selecionados) {
            confirmarExclusaoSelecionados();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void atualizarMenuSelecao(int selectedCount) {
        if (menuItemExcluirSelecionados != null) {
            menuItemExcluirSelecionados.setVisible(selectedCount > 0);
        }
        getSupportActionBar().setTitle(
                selectedCount > 0 ? selectedCount + " selecionado(s)" : TITULO_PADRAO);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_excluir_trab_agentes, menu);
        menuItemExcluirSelecionados = menu.findItem(R.id.action_excluir_selecionados);
        return true;
    }
    private void confirmarExclusaoSelecionados() {
        List<ClassTrabAgentes> selecionados = adapterTrabAgentesExcluir.getSelectedItems();
        if (selecionados.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Excluir " + selecionados.size() + " item(ns) selecionado(s)?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirItensSelecionados(selecionados))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirItensSelecionados(List<ClassTrabAgentes> selecionados) {
        for (ClassTrabAgentes item : selecionados) {
            databaseReferenceExcluirTrabAgentes.child(item.getId()).removeValue()
                    .addOnSuccessListener(unused -> {
                        incrementaContadorImagem();
                        deletarImagemStorage(item.getUrlMidia());
                    })
                    .addOnFailureListener(e -> Toast.makeText(
                            ExcluirTrabAgentesActivity.this,
                            "Erro ao excluir: " + e.getMessage(),
                            LENGTH_SHORT).show());
        }
        adapterTrabAgentesExcluir.clearSelection();
    }

    private void deletarImagemStorage(String urlMidia) {
        if (urlMidia == null) return;
        FirebaseStorage.getInstance().getReferenceFromUrl(urlMidia)
                .delete()
                .addOnSuccessListener(unused -> Log.d("Imagem apagada", urlMidia));
    }

    private int findAgenteIndexById(String key) {
        if (key == null) return -1;
        for (int i = 0; i < classTrabAgentesExcluir.size(); i++) {
            ClassTrabAgentes item = classTrabAgentesExcluir.get(i);
            if (key.equals(item.getId())) {
                return i;
            }
        }
        return -1;
    }

    private void incrementaContadorImagem() {
        DatabaseReference totalRefImagens = dbContador
                .child(estado)
                .child(municipio)
                .child("config")
                .child("total_upload_imagens");

        totalRefImagens.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError,
                                   boolean committed,
                                   DataSnapshot dataSnapshot) {
                if (!committed) {
                    Toast.makeText(ExcluirTrabAgentesActivity.this,
                            "Erro ao atualizar contador",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}