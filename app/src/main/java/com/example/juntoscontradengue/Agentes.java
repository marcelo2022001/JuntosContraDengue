package com.example.juntoscontradengue;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.juntoscontradengue.database.adapters.AdapterAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassAgentes;
import com.example.juntoscontradengue.databinding.ActivityAgentesBinding;
import com.example.juntoscontradengue.extras.AppConfig;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class Agentes extends AppCompatActivity implements AdapterAgentes.OnImageResultListener {
    private static final long TIMEOUT_VERIFICACAO_IMAGENS_MS = 4000;

    private AdapterAgentes adapterAgentes;
    private DatabaseReference agentesRef;
    private ValueEventListener agentesListener;
    private ActivityAgentesBinding binding;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutVerificacaoImagens;
    private boolean redirecionadoSemInternet = false;
    private final Set<Integer> imagensProcessadas = new HashSet<>();
    private boolean algumaImagemCarregada = false;
    private int totalEsperadoImagens = 0;
    private boolean verificandoImagensOffline = false;

    String estado, municipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        binding = ActivityAgentesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        estado = AppConfig.getEstado(this);
        municipio = AppConfig.getMunicipio(this);

        atualizarBannerOffline();

        List<ClassAgentes> listAgentes = new ArrayList<>();

        Toolbar toolbarAgentes = binding.tbAgentes;
        setSupportActionBar(toolbarAgentes);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = binding.rvAgentes;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapterAgentes = new AdapterAgentes(listAgentes, this, this);
        recyclerView.setAdapter(adapterAgentes);

        if (estado != null && municipio != null) {
            agentesRef = FirebaseDatabase.getInstance()
                    .getReference("cadastros")
                    .child(estado)
                    .child(municipio)
                    .child("logins")
                    .child("agentes");

            agentesRef.keepSynced(true);

            carregarAgentes();
        } else {
            Toast.makeText(this, "Configuração de Estado/Município ausente.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registrarNetworkCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removerNetworkCallback();
        cancelarVerificacaoImagens();
    }

    private void registrarNetworkCallback() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> {
                    atualizarBannerOffline();
                    cancelarVerificacaoImagens();
                    recarregarAgentes();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(Agentes.this::atualizarBannerOffline);
            }
        };

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private void removerNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException e) {
                // callback já não estava registrado; ignora
            }
        }
    }

    private void atualizarBannerOffline() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);
        binding.txtAvisoOffline.setVisibility(isConnected ? View.GONE : View.VISIBLE);
    }

    private void recarregarAgentes() {
        if (agentesRef == null) return;

        if (agentesListener != null) {
            agentesRef.removeEventListener(agentesListener);
        }
        redirecionadoSemInternet = false;
        carregarAgentes();
    }

    private void carregarAgentes() {
        agentesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                atualizarBannerOffline();
                boolean isConnected = NetworkUtils.isNetworkAvailable(Agentes.this);

                List<ClassAgentes> newListAgentes = new ArrayList<>();

                if (!snapshot.exists()) {
                    if (isConnected) {
                        Toast.makeText(Agentes.this, "Nenhum agente encontrado.", Toast.LENGTH_SHORT).show();
                        adapterAgentes.updateList(newListAgentes);
                    } else {
                        irParaSemInternet();
                    }
                    return;
                }

                for (DataSnapshot agenteSnapshot : snapshot.getChildren()) {
                    ClassAgentes agentes = agenteSnapshot.getValue(ClassAgentes.class);

                    if (agentes != null) {
                        String uidDoAgente = agenteSnapshot.getKey();
                        if (uidDoAgente != null) {
                            agentes.setUuid(uidDoAgente);
                        }
                        newListAgentes.add(agentes);
                    }
                }

                if (isConnected) {
                    preCarregarImagens(newListAgentes);
                }

                adapterAgentes.updateList(newListAgentes);

                if (!isConnected) {
                    iniciarVerificacaoImagensOffline(newListAgentes.size());
                } else {
                    cancelarVerificacaoImagens();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Erro ao buscar agentes: " + error.getMessage());

                if (!NetworkUtils.isNetworkAvailable(Agentes.this)) {
                    irParaSemInternet();
                }
            }
        };

        agentesRef.addValueEventListener(agentesListener);
    }

    // Esquenta o cache do Glide para todas as fotos da lista, mesmo as fora da tela,
    // enquanto ainda há internet — melhora a chance delas aparecerem depois offline
    private void preCarregarImagens(List<ClassAgentes> lista) {
        for (ClassAgentes agente : lista) {
            String url = agente.getUrlImagem();
            if (url != null && !url.isEmpty()) {
                Glide.with(Agentes.this)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }
    }

    private void iniciarVerificacaoImagensOffline(int totalItens) {
        cancelarVerificacaoImagens();

        if (totalItens == 0) return;

        imagensProcessadas.clear();
        algumaImagemCarregada = false;
        totalEsperadoImagens = totalItens;
        verificandoImagensOffline = true;

        timeoutVerificacaoImagens = () -> {
            if (verificandoImagensOffline && !algumaImagemCarregada
                    && !NetworkUtils.isNetworkAvailable(Agentes.this)) {
                irParaSemInternet();
            }
            verificandoImagensOffline = false;
        };
        handler.postDelayed(timeoutVerificacaoImagens, TIMEOUT_VERIFICACAO_IMAGENS_MS);
    }

    private void cancelarVerificacaoImagens() {
        verificandoImagensOffline = false;
        if (timeoutVerificacaoImagens != null) {
            handler.removeCallbacks(timeoutVerificacaoImagens);
        }
    }

    @Override
    public void onImageResult(int position, boolean success) {
        if (!verificandoImagensOffline) return;

        if (success) {
            algumaImagemCarregada = true;
            cancelarVerificacaoImagens();
            return;
        }

        imagensProcessadas.add(position);

        boolean todasProcessadas = imagensProcessadas.size() >= totalEsperadoImagens;
        if (todasProcessadas && !algumaImagemCarregada
                && !NetworkUtils.isNetworkAvailable(Agentes.this)) {
            cancelarVerificacaoImagens();
            irParaSemInternet();
        }
    }

    private void irParaSemInternet() {
        if (redirecionadoSemInternet) return;
        redirecionadoSemInternet = true;

        Intent intent = new Intent(Agentes.this, SemInternetActivity.class);
        intent.putExtra("id_activity", "agentes");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelarVerificacaoImagens();
        if (agentesRef != null && agentesListener != null) {
            agentesRef.removeEventListener(agentesListener);
        }
        Log.d("LIFECYCLE", getClass().getSimpleName() + " onDestroy");
    }
}