package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterReclamacaoUsuarios;
import com.example.juntoscontradengue.database.classes_database.ClassReclamacoes;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListDenunciasUsuarios extends AppCompatActivity {
    private DatabaseReference databaseDenunciasUsers;
    private ChildEventListener childEventListenerDenunciasUsers;
    private AdapterReclamacaoUsuarios adapterDenunciasUsers;

    RecyclerView recyclerViewDenunciasUsers;
    private com.example.juntoscontradengue.databinding.ActivityListDenunciasUsuariosBinding bindigDenunciasUsers;
    private final List<ClassReclamacoes> classReclamacoes = new ArrayList<>();
    String estado, municipio;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        bindigDenunciasUsers = com.example.juntoscontradengue.databinding.ActivityListDenunciasUsuariosBinding.inflate(getLayoutInflater());
        setContentView(bindigDenunciasUsers.getRoot());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        // Exemplo em Java
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String valor = extras.getString("CHAVE_DADO"); // Recupera o dado pela chave
        }

        setupToolbarDenunciasUsers();
        initializeFirebaseDenunciasUsers();
        setupRecyclerViewDenunciasUsers();
        loadInitialData(); //

    }

    private void setupToolbarDenunciasUsers() {
        Toolbar toolbar = bindigDenunciasUsers.toolbarDenunciasAgentes;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Reclamacoes");
    }

    private void initializeFirebaseDenunciasUsers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDenunciasUsers = database.getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("reclamacoes");
    }

    private void setupRecyclerViewDenunciasUsers() {
        recyclerViewDenunciasUsers = bindigDenunciasUsers.rvVisualizarDenuncias;
        recyclerViewDenunciasUsers.setLayoutManager(new LinearLayoutManager(this));
  }

    private void loadInitialData() {
        databaseDenunciasUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classReclamacoes.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ClassReclamacoes contact = childSnapshot.getValue(ClassReclamacoes.class);
                    if (contact != null) {
                        contact.setIdReclamacao(childSnapshot.getKey());
                        classReclamacoes.add(contact);
                    }
                }

                // CORREÇÃO: Notificar o adapter que os dados mudaram
                if (adapterDenunciasUsers != null) {
                  //  adapterDenunciasUsers.updateListExcluir(classReclamacoes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(ListDenunciasUsuarios.this,
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
        childEventListenerDenunciasUsers = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassReclamacoes contact = snapshot.getValue(ClassReclamacoes.class);
                if (contact != null) {
                    contact.setIdReclamacao(snapshot.getKey());
                    classReclamacoes.add(contact);
                    if (adapterDenunciasUsers != null) {
                     //   adapterDenunciasUsers.updateListExcluir(classReclamacoes);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                ClassReclamacoes updated = snapshot.getValue(ClassReclamacoes.class);
                if (updated == null) return;

                updated.setConjunto_reclamacao(snapshot.getKey());

                for (int i = 0; i < classReclamacoes.size(); i++) {
                    if (classReclamacoes.get(i).getReclamante().equals(updated.getReclamante())) {
                        classReclamacoes.set(i, updated);
                        if (adapterDenunciasUsers != null) {
                           // adapterDenunciasUsers.updateListExcluir(classReclamacoes);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedId = snapshot.getKey();

                for (int i = 0; i < classReclamacoes.size(); i++) {
                    if (classReclamacoes.get(i).getReclamante().equals(removedId)) {
                        classReclamacoes.remove(i);
                        if (adapterDenunciasUsers != null) {
                        //   adapterDenunciasUsers.updateListExcluir(classReclamacoes);
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
                    Toast.makeText(ListDenunciasUsuarios.this,
                            "Erro: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        databaseDenunciasUsers.addChildEventListener(childEventListenerDenunciasUsers);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListenerDenunciasUsers != null) {
            databaseDenunciasUsers.removeEventListener(childEventListenerDenunciasUsers);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bindigDenunciasUsers = null;
    }

    private void deletarCadastroDenuncia(String id) {
        if (id != null && !id.isEmpty()) {
            DatabaseReference deletarUsuarios = databaseDenunciasUsers.child(id);
            deletarUsuarios.removeValue()
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

    public void deletarLocalDescartePeloId(String id) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);

        alertDialog2.setTitle("Confirmar exclusão...");
        alertDialog2.setMessage("Tem certeza de que deseja excluir este local de descarte?");
        alertDialog2.setIcon(R.drawable.alerts_sirene);

        alertDialog2.setPositiveButton("SIM",
                (dialog, which) -> deletarCadastroDenuncia(id));

        alertDialog2.setNegativeButton("NÃO",
                (dialog, which) -> dialog.cancel());

        try {
            alertDialog2.show();
        } catch (Exception e) {
            Log.e("Erro alertialog", "Erro ao vissualizar alertialog" + e);
        }
    }
}

