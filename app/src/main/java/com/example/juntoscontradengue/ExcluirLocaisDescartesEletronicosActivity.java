package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterExcluirLocaisDescartesEletronicos;
import com.example.juntoscontradengue.database.classes_database.ClassDescarteConsciente;
import com.example.juntoscontradengue.databinding.ActivityExcluirLocaisDescartesEletronicosBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcluirLocaisDescartesEletronicosActivity extends AppCompatActivity {
    private ActivityExcluirLocaisDescartesEletronicosBinding bindingDescartesEletronicos;
    private DatabaseReference databaseDescartesEletronicos;
    private ChildEventListener childEventListenerDescartesEletronicos;
    private AdapterExcluirLocaisDescartesEletronicos adapterDescartesEletronicos;
    private final List<ClassDescarteConsciente> listDescartesEletronicos = new ArrayList<>();
    private String estado, municipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingDescartesEletronicos = ActivityExcluirLocaisDescartesEletronicosBinding.inflate(getLayoutInflater());
        setContentView(bindingDescartesEletronicos.getRoot());

        boolean isConnected = NetworkUtils.isNetworkAvailable(ExcluirLocaisDescartesEletronicosActivity.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "excluir_locais_eletronicos");
            startActivity(itente);

        }


        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        initializeFirebaseDescartesEletronicos();
        setupRecyclerViewDescartesEletronicos();
        loadInitialData();
        Toolbar toolbar = bindingDescartesEletronicos.toolbarExcluirLocaisDescartesEletronico;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initializeFirebaseDescartesEletronicos() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDescartesEletronicos = database.getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("descarte_eletronicos");
    }

    private void setupRecyclerViewDescartesEletronicos() {
        RecyclerView recyclerViewDescartesEletronicos = bindingDescartesEletronicos.rvExcluirLocaisDescartesEletronico;
        recyclerViewDescartesEletronicos.setLayoutManager(new LinearLayoutManager(this));
        adapterDescartesEletronicos = new AdapterExcluirLocaisDescartesEletronicos(this, listDescartesEletronicos);
        recyclerViewDescartesEletronicos.setAdapter(adapterDescartesEletronicos);
    }

    private void loadInitialData() {
        databaseDescartesEletronicos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listDescartesEletronicos.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ClassDescarteConsciente contact = childSnapshot.getValue(ClassDescarteConsciente.class);
                    if (contact != null) {
                        contact.setId(childSnapshot.getKey());
                        listDescartesEletronicos.add(contact);
                    }
                }
                if (adapterDescartesEletronicos != null) {
                    adapterDescartesEletronicos.updateListExcluir(listDescartesEletronicos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ExcluirLocaisDescartesEletronicosActivity.this,
                            "Erro ao carregar dados: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachChildListener();
    }

    private void attachChildListener() {
        childEventListenerDescartesEletronicos = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassDescarteConsciente contact = snapshot.getValue(ClassDescarteConsciente.class);
                if (contact != null) {
                    contact.setId(snapshot.getKey());
                    listDescartesEletronicos.add(contact);
                    if (adapterDescartesEletronicos != null) {
                        adapterDescartesEletronicos.updateListExcluir(listDescartesEletronicos);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassDescarteConsciente updated = snapshot.getValue(ClassDescarteConsciente.class);
                if (updated == null) return;

                updated.setId(snapshot.getKey());

                for (int i = 0; i < listDescartesEletronicos.size(); i++) {
                    if (listDescartesEletronicos.get(i).getId().equals(updated.getId())) {
                        listDescartesEletronicos.set(i, updated);
                        if (adapterDescartesEletronicos != null) {
                            adapterDescartesEletronicos.notifyItemChanged(i);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedId = snapshot.getKey();

                for (int i = 0; i < listDescartesEletronicos.size(); i++) {
                    if (listDescartesEletronicos.get(i).getId().equals(removedId)) {
                        listDescartesEletronicos.remove(i);
                        if (adapterDescartesEletronicos != null) {
                            adapterDescartesEletronicos.notifyItemRemoved(i);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Para simplificar, recarregamos os dados quando houver movimento
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ExcluirLocaisDescartesEletronicosActivity.this,
                            "Erro: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        databaseDescartesEletronicos.addChildEventListener(childEventListenerDescartesEletronicos);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListenerDescartesEletronicos != null) {
            databaseDescartesEletronicos.removeEventListener(childEventListenerDescartesEletronicos);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bindingDescartesEletronicos = null;
    }

    private void deletarCadastroEntregaEletronicos(String id) {
        if (id != null && !id.isEmpty()) {
            DatabaseReference deletarDescarteEletronicos = databaseDescartesEletronicos.child(id);
            deletarDescarteEletronicos.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, "Local deletado com sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, "Erro ao deletar local: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, "ID inválido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deletarLocalDescarteEletronicosPeloId(String id) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Confirmar exclusão...");
        alertDialog.setMessage("Tem certeza de que deseja excluir este local de descarte?");
        alertDialog.setIcon(R.drawable.alerts_sirene);

        alertDialog.setPositiveButton("SIM", (dialog, which) -> deletarCadastroEntregaEletronicos(id));

        alertDialog.setNegativeButton("NÃO", (dialog, which) -> dialog.cancel());

        try {
            alertDialog.show();
        } catch (Exception e) {
            Log.e("Erro ao exibir alertDialog", "Erro ao exibir o alertDialog", e);        }
    }
}