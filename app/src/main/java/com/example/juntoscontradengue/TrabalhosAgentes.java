package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.juntoscontradengue.database.adapters.AdapterTrabAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassTrabAgentes;
import com.example.juntoscontradengue.databinding.ActivityTrabalhosAgentesBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class TrabalhosAgentes extends AppCompatActivity {
    private ArrayList<ClassTrabAgentes> listTrabAgentes;
    private AdapterTrabAgentes adapterTrabAgentes;
    String estado, municipio;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_trabalhos_agentes);

        // Configuração do ViewBinding
        ActivityTrabalhosAgentesBinding trabalhosAgentesBinding = ActivityTrabalhosAgentesBinding.inflate(getLayoutInflater());
        setContentView(trabalhosAgentesBinding.getRoot());

        TextView txt_nada_para_ver = trabalhosAgentesBinding.txtNadaParaVer;

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
         estado = prefs.getString("estado", null);
         municipio = prefs.getString("municipio", null);

          isConnected = NetworkUtils.isNetworkAvailable(TrabalhosAgentes.this);
        if (!isConnected){
            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "trabalhos_agentes");
            startActivity(itente);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("cadastros/" + estado + "/" + municipio + "/trabalhos_agentes");

        listTrabAgentes = new ArrayList<>();

        // Configuração da Toolbar
        Toolbar toolbarTrabAgentes = trabalhosAgentesBinding.toolbarTrabAgentes;
        setSupportActionBar(toolbarTrabAgentes);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerViewTrabAgentes = trabalhosAgentesBinding.recyclerViewTrabAgentes;
        // Configuração do RecyclerView
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager (2, StaggeredGridLayoutManager.VERTICAL);
         recyclerViewTrabAgentes.setLayoutManager(staggeredGridLayoutManager);


        // Inicialização do adapter
        adapterTrabAgentes = new AdapterTrabAgentes(listTrabAgentes, this);
        recyclerViewTrabAgentes.setAdapter(adapterTrabAgentes);


        // Leitura dos dados do Firebase
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClassTrabAgentes classTrabAgentes = snapshot.getValue(ClassTrabAgentes.class);
                if (classTrabAgentes != null) {
                    recyclerViewTrabAgentes.setVisibility(View.VISIBLE);
                    txt_nada_para_ver.setVisibility(View.GONE);

                    listTrabAgentes.add(classTrabAgentes);
                    adapterTrabAgentes.notifyItemInserted(listTrabAgentes.size() - 1); // Notifica o Adapter


                    // Log para verificar os dados recebidos
                    Log.d("FirebaseData", "Caption: " + classTrabAgentes.getTitulo());
                    Log.d("FirebaseData", "Image URL: " + classTrabAgentes.getUrlMidia());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ClassTrabAgentes classTrabAgentes = snapshot.getValue(ClassTrabAgentes.class);
                if (classTrabAgentes != null) {
                    recyclerViewTrabAgentes.setVisibility(View.VISIBLE);
                    txt_nada_para_ver.setVisibility(View.GONE);
                    int index = findIndexById(snapshot.getKey());
                    if (index != -1) {
                        listTrabAgentes.set(index, classTrabAgentes);
                        adapterTrabAgentes.notifyItemInserted(listTrabAgentes.size() - 1);                     }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = findIndexById(snapshot.getKey());
                if (index != -1) {
                    recyclerViewTrabAgentes.setVisibility(View.VISIBLE);
                    txt_nada_para_ver.setVisibility(View.GONE);
                    listTrabAgentes.remove(index);
                    adapterTrabAgentes.notifyItemRemoved(index);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Implemente se necessário
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Erro ao ler os dados: " + error.getMessage());
            }
        });
    }

    private int findIndexById(String id) {
        for (int i = 0; i < listTrabAgentes.size(); i++) {
            ClassTrabAgentes agente = listTrabAgentes.get(i);
        }
        return -1; // Retorna -1 se o item não for encontrado
    }
}