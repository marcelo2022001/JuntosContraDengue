package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        FirebaseDatabase databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);
        databaseDescartesPneus = databaseMunicipio.getReference().child("descarte_pneus");

        setupToolbarDescartesPneus();
        setupRecyclerViewDescartesPneus();
    }

    private void setupToolbarDescartesPneus() {
        Toolbar toolbar = bindingDescartesPneus.toolbarExcluirLocaisDescartesPneus;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Excluir locais de descarte de pneus");
    }

    private void setupRecyclerViewDescartesPneus() {
        recyclerViewDescartesPneus = bindingDescartesPneus.rvExcluirLocaisDescartesPneus;
        recyclerViewDescartesPneus.setLayoutManager(new LinearLayoutManager(this));
        adapterDescartesPneus = new AdapterExcluirLocaisDescartesPneus(this, ListDescartesPneus);
        recyclerViewDescartesPneus.setAdapter(adapterDescartesPneus);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachChildListener();
    }

    private void attachChildListener() {
        // Evita itens duplicados ao voltar para a tela (onStop -> onStart)
        ListDescartesPneus.clear();
        if (adapterDescartesPneus != null) {
            adapterDescartesPneus.updateListExcluir(ListDescartesPneus);
        }

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
                    atualizarEstadoVazio();
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
                        atualizarEstadoVazio();
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

        // Dispara depois de todos os onChildAdded iniciais (inclusive com o nó vazio)
        databaseDescartesPneus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                atualizarEstadoVazio();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                atualizarEstadoVazio();
            }
        });
    }

    private void atualizarEstadoVazio() {
        if (bindingDescartesPneus == null) return;

        boolean vazio = ListDescartesPneus.isEmpty();
        bindingDescartesPneus.layoutEmptyState.setVisibility(vazio ? View.VISIBLE : View.GONE);
        bindingDescartesPneus.rvExcluirLocaisDescartesPneus.setVisibility(vazio ? View.GONE : View.VISIBLE);
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