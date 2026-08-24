package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterExcluirTelefone;
import com.example.juntoscontradengue.database.classes_database.ClassTelefonesUteis;
import com.example.juntoscontradengue.databinding.ActivityExcluirTelefonesUteisBinding;
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

public class ExcluirTelefonesUteisActivity extends AppCompatActivity {
    private ActivityExcluirTelefonesUteisBinding bindingExcluirFone;
    private DatabaseReference databaseReferenceExcluirFone;
    private ChildEventListener childEventListener;
    private AdapterExcluirTelefone adapterExcluirTelefone;
    RecyclerView recyclerViewExcluirFone;
    private final List<ClassTelefonesUteis> telefoneListExcluir = new ArrayList<>();
    private final List<ClassTelefonesUteis> filteredListExcluir = new ArrayList<>();
    private String lastQuery = "";
    String estado, municipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingExcluirFone = ActivityExcluirTelefonesUteisBinding.inflate(getLayoutInflater());
        setContentView(bindingExcluirFone.getRoot());

        boolean isConnected = NetworkUtils.isNetworkAvailable(ExcluirTelefonesUteisActivity.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "excluir_telefones_uteis");
            startActivity(itente);

        }


        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);


        setupToolbar();
        initializeFirebase();
        setupRecyclerView();
        setupSearch();
    }

    private void setupToolbar() {
        Toolbar toolbar = bindingExcluirFone.toolbarTelefoneExcluir;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getTitle());
    }

    private void initializeFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceExcluirFone = database
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("telefones_uteis");
    }

    private void setupRecyclerView() {
        recyclerViewExcluirFone = bindingExcluirFone.rvTelefonesExcluir;
        recyclerViewExcluirFone.setLayoutManager(new LinearLayoutManager(this));
        adapterExcluirTelefone = new AdapterExcluirTelefone(this);
        recyclerViewExcluirFone.setAdapter(adapterExcluirTelefone);
    }

    private void setupSearch() {
        bindingExcluirFone.edtPesquisaFoneExcluir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(lastQuery)) {
                    lastQuery = query;
                    filterContacts(query);
                }
            }
        });
    }

    private void filterContacts(String query) {
        if (query.isEmpty()) {
            adapterExcluirTelefone.updateListExcluir(telefoneListExcluir);
            return;
        }

        filteredListExcluir.clear();
        String lowerCaseQuery = query.toLowerCase();

        for (ClassTelefonesUteis contact : telefoneListExcluir) {
            if (contact.getLocal() != null &&
                    contact.getLocal().toLowerCase().contains(lowerCaseQuery)) {
                filteredListExcluir.add(contact);
            }
        }
        adapterExcluirTelefone.updateListExcluir(filteredListExcluir);
    }
    private void loadInitialData() {
        databaseReferenceExcluirFone.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                telefoneListExcluir.clear(); // Limpa apenas uma vez no início
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ClassTelefonesUteis contact = childSnapshot.getValue(ClassTelefonesUteis.class);
                    if (contact != null) {
                        contact.setId(childSnapshot.getKey());
                        telefoneListExcluir.add(contact);
                    }
                }
                filterContacts(lastQuery);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExcluirTelefonesUteisActivity.this,
                        "Erro ao carregar dados: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        telefoneListExcluir.clear();
        attachChildListener();
    }

    private void attachChildListener() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassTelefonesUteis contact = snapshot.getValue(ClassTelefonesUteis.class);
                if (contact != null) {
                    contact.setId(snapshot.getKey());
                    telefoneListExcluir.add(contact);
                    filterContacts(lastQuery);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassTelefonesUteis updated = snapshot.getValue(ClassTelefonesUteis.class);
                if (updated == null) return;

                updated.setId(snapshot.getKey());

                for (int i = 0; i < telefoneListExcluir.size(); i++) {
                    if (telefoneListExcluir.get(i).getId().equals(updated.getId())) {
                        telefoneListExcluir.set(i, updated);
                        break;
                    }
                }
                filterContacts(lastQuery);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedId = snapshot.getKey();

                for (int i = 0; i < telefoneListExcluir.size(); i++) {
                    if (telefoneListExcluir.get(i).getId().equals(removedId)) {
                        telefoneListExcluir.remove(i);
                        break;
                    }
                }

                filterContacts(lastQuery);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExcluirTelefonesUteisActivity.this,
                        "Erro: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        databaseReferenceExcluirFone.addChildEventListener(childEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListener != null) {
            databaseReferenceExcluirFone.removeEventListener(childEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bindingExcluirFone = null; // Prevent memory leaks
    }

    public void deletarTelefonePeloId(String id) {

        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                ExcluirTelefonesUteisActivity.this);

        alertDialog2.setTitle("Confirmar exclusão...");

// Definindo a mensagem da caixa de diálogo
        alertDialog2.setMessage("Tem certeza de que deseja excluir este número de telefone?");

// Definindo o ícone da caixa de diálogo
        alertDialog2.setIcon(R.drawable.alert_warning_triangulo);

// Definindo o botão positivo "Sim"
        alertDialog2.setPositiveButton("SIM",
                (dialog, which) -> deletarNumeroTelefone(id));
// Definindo o botão negativo "NÃO"
        alertDialog2.setNegativeButton("NÃO",
                (dialog, which) -> {
                    // Escreva seu código aqui para ser executado após o diálogo
                    Toast.makeText(getApplicationContext(),
                                    "Você clicou em NÃO", Toast.LENGTH_SHORT)
                            .show();
                    dialog.cancel();

                });

// Exibindo o diálogo de alerta
        alertDialog2.show();

    }

    private void deletarNumeroTelefone(String id) {
        if (id != null && !id.isEmpty()) {
            DatabaseReference telefoneRef = databaseReferenceExcluirFone.child(id);
            telefoneRef.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Telefone deletado com sucesso!", Toast.LENGTH_SHORT).show())

                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao deletar telefone: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "ID inválido", Toast.LENGTH_SHORT).show();
        }
    }


}