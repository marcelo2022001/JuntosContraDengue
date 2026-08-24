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

import com.example.juntoscontradengue.database.adapters.AdapterExcluirLocaisDescartesPneus;
import com.example.juntoscontradengue.database.classes_database.ClassDescarteConsciente;
import com.example.juntoscontradengue.databinding.ActivityExcluirLocaisDescartesPneusBinding;
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

public class ExcluirLocaisDescartesPneus extends AppCompatActivity {

    private ActivityExcluirLocaisDescartesPneusBinding bindingDescartesPneus;
    private DatabaseReference databaseDescartesPneus;
    private ChildEventListener childEventListenerDescartesPneus;
    private AdapterExcluirLocaisDescartesPneus adapterDescartesPneus;
    RecyclerView recyclerViewDescartesPneus;
    private final List<ClassDescarteConsciente> ListDescartesPneus = new ArrayList<>();
    String estado, municipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingDescartesPneus = ActivityExcluirLocaisDescartesPneusBinding.inflate(getLayoutInflater());
        setContentView(bindingDescartesPneus.getRoot());

        boolean isConnected = NetworkUtils.isNetworkAvailable(ExcluirLocaisDescartesPneus.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "excluir_locais_pneus");
            startActivity(itente);

        }

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        setupToolbarDescartesPneus();
        initializeFirebaseDescartesPneus();
        setupRecyclerViewDescartesPneus();
        loadInitialData(); // Adicionado para carregar dados iniciais
    }

    private void setupToolbarDescartesPneus() {
        Toolbar toolbar = bindingDescartesPneus.toolbarExcluirLocaisDescartesPneus;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Excluir locais de descarte de pneus");
    }

    private void initializeFirebaseDescartesPneus() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDescartesPneus = database.getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("descarte_pneus");
    }

    private void setupRecyclerViewDescartesPneus() {
        recyclerViewDescartesPneus = bindingDescartesPneus.rvExcluirLocaisDescartesPneus;
        recyclerViewDescartesPneus.setLayoutManager(new LinearLayoutManager(this));

        // CORREÇÃO: Usando a variável de instância corretamente
        adapterDescartesPneus = new AdapterExcluirLocaisDescartesPneus(this, ListDescartesPneus);
        recyclerViewDescartesPneus.setAdapter(adapterDescartesPneus);
    }

    private void loadInitialData() {
        databaseDescartesPneus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ListDescartesPneus.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ClassDescarteConsciente contact = childSnapshot.getValue(ClassDescarteConsciente.class);
                    if (contact != null) {
                        contact.setId(childSnapshot.getKey());
                        ListDescartesPneus.add(contact);
                    }
                }
                // CORREÇÃO: Notificar o adapter que os dados mudaram
                if (adapterDescartesPneus != null) {
                    adapterDescartesPneus.updateListExcluir(ListDescartesPneus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ExcluirLocaisDescartesPneus.this,
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
        childEventListenerDescartesPneus = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassDescarteConsciente contact = snapshot.getValue(ClassDescarteConsciente.class);
                if (contact != null) {
                    contact.setId(snapshot.getKey());
                    ListDescartesPneus.add(contact);
                    if (adapterDescartesPneus != null) {
                        adapterDescartesPneus.updateListExcluir(ListDescartesPneus);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassDescarteConsciente updated = snapshot.getValue(ClassDescarteConsciente.class);
                if (updated == null) return;

                updated.setId(snapshot.getKey());

                for (int i = 0; i < ListDescartesPneus.size(); i++) {
                    if (ListDescartesPneus.get(i).getId().equals(updated.getId())) {
                        ListDescartesPneus.set(i, updated);
                        if (adapterDescartesPneus != null) {
                            adapterDescartesPneus.updateListExcluir(ListDescartesPneus);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedId = snapshot.getKey();

                for (int i = 0; i < ListDescartesPneus.size(); i++) {
                    if (ListDescartesPneus.get(i).getId().equals(removedId)) {
                        ListDescartesPneus.remove(i);
                        if (adapterDescartesPneus != null) {
                            adapterDescartesPneus.updateListExcluir(ListDescartesPneus);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ExcluirLocaisDescartesPneus.this,
                            "Erro: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        databaseDescartesPneus.addChildEventListener(childEventListenerDescartesPneus);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListenerDescartesPneus != null) {
            databaseDescartesPneus.removeEventListener(childEventListenerDescartesPneus);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bindingDescartesPneus = null;
    }

    private void deletarCadastroEntregaPneus(String id) {
        if (id != null && !id.isEmpty()) {
            DatabaseReference deletarDescartePneus = databaseDescartesPneus.child(id);
            deletarDescartePneus.removeValue()
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

    public void deletarLocalDescartePneusPeloId(String id) {
        // CORREÇÃO: Verificar se a Activity ainda está ativa
        if (isFinishing() || isDestroyed()) {
            return;
        }

        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);

        alertDialog2.setTitle("Confirmar exclusão...");
        alertDialog2.setMessage("Tem certeza de que deseja excluir este local de descarte?");
        alertDialog2.setIcon(R.drawable.alerts_sirene);

        alertDialog2.setPositiveButton("SIM",
                (dialog, which) -> deletarCadastroEntregaPneus(id));

        alertDialog2.setNegativeButton("NÃO",
                (dialog, which) -> dialog.cancel());

        try {
            alertDialog2.show();
        } catch (Exception e) {
            Log.e("Erro exibição alertDialog", "Erro ao exibir o alertDialog", e);
        }
    }
}