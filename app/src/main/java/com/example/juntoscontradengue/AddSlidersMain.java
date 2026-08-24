package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterSliders;
import com.example.juntoscontradengue.database.classes_database.ClassAddSliders;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddSlidersMain extends AppCompatActivity {

    private final List<ClassAddSliders> classAddSliders = new ArrayList<>();
    private AdapterSliders adapterSliders; // Make adapter a field to access it throughout the class
    String estado, municipio;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sliders_main);

        Toolbar toolbarAddSlider = findViewById(R.id.toolbarAddSlider);
        setSupportActionBar(toolbarAddSlider);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        isConnected = NetworkUtils.isNetworkAvailable(AddSlidersMain.this);
        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "add_slider");
            startActivity(itente);

        }


        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);


        RecyclerView recyclerView = findViewById(R.id.rvAddSliders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterSliders = new AdapterSliders(this, (ArrayList<ClassAddSliders>) classAddSliders);
        recyclerView.setAdapter(adapterSliders);

        DatabaseReference databaseReferenceAddSliders = FirebaseDatabase
                .getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("config")
                .child("sliders_main");

        databaseReferenceAddSliders.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Valida se o snapshot possui filhos/estrutura de objeto e não é apenas uma String solta
                if (snapshot.exists() && snapshot.hasChildren()) {
                    ClassAddSliders sliders = snapshot.getValue(ClassAddSliders.class);
                    if (sliders != null) {
                        try {
                            String key = snapshot.getKey();
                            if (key != null) {
                                sliders.setId(Long.valueOf(key));
                            }
                        } catch (NumberFormatException e) {
                            Log.e("FirebaseError", "Chave não numérica: " + snapshot.getKey());
                        }

                        classAddSliders.add(sliders);
                        adapterSliders.notifyItemInserted(classAddSliders.size() - 1);

                        Log.d("IMG_DEBUG EXCLUIR", "URL da imagem carregada com sucesso.");
                    }
                } else {
                    Log.w("FirebaseError", "O nó " + snapshot.getKey() + " não é um objeto válido para ClassAddSliders.");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    ClassAddSliders slidersExcluir = snapshot.getValue(ClassAddSliders.class);
                    if (slidersExcluir != null) {
                        int index = findAgenteIndexById(snapshot.getKey());
                        if (index != -1) {
                            classAddSliders.set(index, slidersExcluir);
                            adapterSliders.notifyItemChanged(index);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = findAgenteIndexById(snapshot.getKey());
                if (index != -1) {
                    classAddSliders.remove(index);
                    adapterSliders.notifyItemRemoved(index);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not implemented
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Erro ao ler os dados: " + error.getMessage());
                Toast.makeText(AddSlidersMain.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Volta para a MainActivity
        Intent intent = new Intent(this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    private int findAgenteIndexById(String key) {
        if (key == null) return -1;
        try {
            Long keyLong = Long.valueOf(key);
            for (int i = 0; i < classAddSliders.size(); i++) {
                if (classAddSliders.get(i).getId() != null && classAddSliders.get(i).getId().equals(keyLong)) {
                    return i;
                }
            }
        } catch (NumberFormatException e) {
            Log.e("FirebaseError", "Chave inválida para conversão: " + key);
        }
        return -1;
    }



}
