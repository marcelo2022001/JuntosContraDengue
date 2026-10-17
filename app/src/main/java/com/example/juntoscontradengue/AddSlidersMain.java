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
    private AdapterSliders adapterSliders;
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

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        FirebaseDatabase databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        RecyclerView recyclerView = findViewById(R.id.rvAddSliders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterSliders = new AdapterSliders(this, (ArrayList<ClassAddSliders>) classAddSliders);
        recyclerView.setAdapter(adapterSliders);

        DatabaseReference databaseReferenceAddSliders = databaseMunicipio.getReference()
                .child("config")
                .child("sliders_main");

        databaseReferenceAddSliders.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String url = extrairUrl(snapshot);
                if (url == null) {
                    Log.w("FirebaseError", "Slider sem URL no nó " + snapshot.getKey());
                    return;
                }

                ClassAddSliders sliders = new ClassAddSliders();
                sliders.setUrl_imagem(url); // ajuste para o setter real da sua classe
                sliders.setId(idDaChave(snapshot.getKey()));

                classAddSliders.add(sliders);
                adapterSliders.notifyItemInserted(classAddSliders.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String url = extrairUrl(snapshot);
                int index = findAgenteIndexById(snapshot.getKey());
                if (url != null && index != -1) {
                    classAddSliders.get(index).setUrl_imagem(url); // mesmo setter de cima
                    adapterSliders.notifyItemChanged(index);
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

    // Lê a URL do nó, seja o próprio valor ou o primeiro filho String (image_1, image_2...)
    private String extrairUrl(DataSnapshot snapshot) {
        Object direto = snapshot.getValue();
        if (direto instanceof String) return (String) direto;

        for (DataSnapshot filho : snapshot.getChildren()) {
            Object v = filho.getValue();
            if (v instanceof String && !((String) v).isEmpty()) return (String) v;
        }
        return null;
    }

    // Pega só os dígitos da chave: "3" -> 3, "image_3" -> 3
    private Long idDaChave(String key) {
        if (key == null) return null;
        String digitos = key.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : Long.valueOf(digitos);
    }

    private int findAgenteIndexById(String key) {
        Long id = idDaChave(key);
        if (id == null) return -1;
        for (int i = 0; i < classAddSliders.size(); i++) {
            ClassAddSliders item = classAddSliders.get(i);
            if (item != null && Objects.equals(item.getId(), id)) return i;
        }
        return -1;
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }
}