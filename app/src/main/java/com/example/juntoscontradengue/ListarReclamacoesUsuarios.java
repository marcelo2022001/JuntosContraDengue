package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterReclamacaoUsuarios;
import com.example.juntoscontradengue.database.classes_database.ClassListarReclamacoes;
import com.example.juntoscontradengue.databinding.ActivityReclamacoesUsuariosBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class ListarReclamacoesUsuarios extends AppCompatActivity {

    ArrayList<ClassListarReclamacoes> listClassReclamacoes;
    AdapterReclamacaoUsuarios adapterReclamacaoUsuarios;
    String uid, estado, municipio;
    DatabaseReference databaseReference;
    ImageView imageViewSemReclamacao;
    ImageButton btnFazerReclamacao;
    RecyclerView recyclerViewReclamacaoUsers;
    Boolean isConnected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);


        ActivityReclamacoesUsuariosBinding bindingUsers = ActivityReclamacoesUsuariosBinding.inflate(getLayoutInflater());
         setContentView(bindingUsers.getRoot());


        isConnected = NetworkUtils.isNetworkAvailable(ListarReclamacoesUsuarios.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "reclamacoes_usuarios");
            startActivity(itente);

        }


        Toolbar toolbar = bindingUsers.toolbarReclamacoesUsers;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerViewReclamacaoUsers = bindingUsers.recyclerViewReclamacoesUsers;
        imageViewSemReclamacao = bindingUsers.imageViewSemReclamacao;
        btnFazerReclamacao = bindingUsers.imageButtonSemReclamacao;
        btnFazerReclamacao.setOnClickListener(v -> startActivity(new Intent(ListarReclamacoesUsuarios.this, Denunciar.class)));

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(ListarReclamacoesUsuarios.this, TelaLoguin.class));
            finish();
            return;
        }

        uid = user.getUid();
        listClassReclamacoes = new ArrayList<>();

        // Setup Toolbar
        Toolbar toolbarAgentes = bindingUsers.toolbarReclamacoesUsers;
        setSupportActionBar(toolbarAgentes);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        RecyclerView recyclerViewReclamacoes = bindingUsers.recyclerViewReclamacoesUsers;
        recyclerViewReclamacoes.setLayoutManager(new LinearLayoutManager(this));
        adapterReclamacaoUsuarios = new AdapterReclamacaoUsuarios(listClassReclamacoes);
        recyclerViewReclamacoes.setAdapter(adapterReclamacaoUsuarios);

        // Initialize database reference
        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("reclamacoes")
                .child(uid);

        // Add child event listener for granular updates
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    ClassListarReclamacoes complaint = snapshot.getValue(ClassListarReclamacoes.class);
                    if (complaint != null) {
                        imageViewSemReclamacao.setVisibility(View.GONE);
                        btnFazerReclamacao.setVisibility(View.GONE);
                        recyclerViewReclamacaoUsers.setVisibility(View.VISIBLE);
                        complaint.setIdReclamacao(snapshot.getKey());
                        listClassReclamacoes.add(complaint);
                        adapterReclamacaoUsuarios.notifyItemInserted(listClassReclamacoes.size() - 1);
                    }
                } catch (DatabaseException e) {

                    Log.e("FirebaseError", "Error parsing complaint: " + e.getMessage());

                    // Handle string values by creating a basic complaint
                    if (snapshot.getValue() instanceof String) {
                        ClassListarReclamacoes tempComplaint = new ClassListarReclamacoes();
                        tempComplaint.setIdReclamacao(snapshot.getKey());
                        tempComplaint.setReclamacao(snapshot.getValue(String.class));
                        tempComplaint.setStatus("Pending");
                        listClassReclamacoes.add(tempComplaint);
                        adapterReclamacaoUsuarios.notifyItemInserted(listClassReclamacoes.size() - 1);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    ClassListarReclamacoes complaint = snapshot.getValue(ClassListarReclamacoes.class);
                    if (complaint != null) {
                        imageViewSemReclamacao.setVisibility(View.GONE);
                        btnFazerReclamacao.setVisibility(View.GONE);
                        recyclerViewReclamacaoUsers.setVisibility(View.VISIBLE);
                        complaint.setIdReclamacao(snapshot.getKey());
                        int index = findComplaintIndexById(snapshot.getKey());
                        if (index != -1) {
                            listClassReclamacoes.set(index, complaint);
                            adapterReclamacaoUsuarios.notifyItemChanged(index);
                        }
                    }
                } catch (DatabaseException e) {
                    Log.e("FirebaseError", "Error updating complaint: " + e.getMessage());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = findComplaintIndexById(snapshot.getKey());
                if (index != -1) {
                    imageViewSemReclamacao.setVisibility(View.VISIBLE);
                    btnFazerReclamacao.setVisibility(View.VISIBLE);
                    recyclerViewReclamacaoUsers.setVisibility(View.GONE);
                    listClassReclamacoes.remove(index);
                    adapterReclamacaoUsuarios.notifyItemRemoved(index);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle item reordering if needed
                imageViewSemReclamacao.setVisibility(View.VISIBLE);
                btnFazerReclamacao.setVisibility(View.VISIBLE);
                recyclerViewReclamacaoUsers.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database error: " + error.getMessage());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    private int findComplaintIndexById(String key) {
        for (int i = 0; i < listClassReclamacoes.size(); i++) {
            if (listClassReclamacoes.get(i).getIdReclamacao().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
