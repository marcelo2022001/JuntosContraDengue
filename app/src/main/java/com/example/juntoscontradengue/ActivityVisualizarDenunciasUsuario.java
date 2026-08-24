package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterVisualizarDenunciasAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassReclamacoes;
import com.example.juntoscontradengue.databinding.ActivityVisualizarDenunciasUsuarioBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityVisualizarDenunciasUsuario extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private String uuid;
    private String id;
    private String status_reclamacao, RESPONDIDO_POR;
    private ExoPlayer exoPlayer;
    private ActivityVisualizarDenunciasUsuarioBinding bindingUsers;
    private AdapterVisualizarDenunciasAgentes midiasAdapterUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingUsers = ActivityVisualizarDenunciasUsuarioBinding.inflate(getLayoutInflater());
        setContentView(bindingUsers.getRoot());

        // Configura o RecyclerView
        setupRecyclerView();

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        id = extras.getString("ID");
        uuid = extras.getString("UUID");
        status_reclamacao = extras.getString("STATUS_RECLAMACAO");
        RESPONDIDO_POR = extras.getString("RESPONDIDO_POR");

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        String estado = prefs.getString("estado", null);
        String municipio = prefs.getString("municipio", null);

        Button visualizar_resposta_reclamacao = bindingUsers.btnVisualizarRespostaReclamacao;

        if(!(status_reclamacao == null && status_reclamacao.isEmpty())) {

            if (!status_reclamacao.equals("Aguardando Resposta")) {

                visualizar_resposta_reclamacao.setVisibility(View.VISIBLE);
                visualizar_resposta_reclamacao.setOnClickListener(v -> {

                    Intent visualizar_resposta = new Intent(ActivityVisualizarDenunciasUsuario.this, VisualizarResposta.class);
                    visualizar_resposta.putExtra("STATUS_RECLAMACAO", status_reclamacao);
                    visualizar_resposta.putExtra("ID", id);
                    visualizar_resposta.putExtra("UUID", uuid);
                    visualizar_resposta.putExtra("RESPONDIDO_POR", RESPONDIDO_POR);
                    startActivity(visualizar_resposta);
                });

            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        setSupportActionBar(bindingUsers.toolbarVisualReclaUsuario);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (uuid == null || uuid.isEmpty()) {
            Toast.makeText(this, "UUID inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        assert estado != null;
        assert municipio != null;

        databaseReference = FirebaseDatabase.getInstance().getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("reclamacoes")
                .child(uuid)
                .child(id);

        populaDados();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupRecyclerView() {
        // CORRIGIDO: Use o RecyclerView correto ( o seu XML deve ter um RecyclerView com este ID)
        RecyclerView rvMidiasUsuario = bindingUsers.rvMidiasUsuario;  // ← Certifique-se que seu XML tem um RecyclerView com id "rvMidiasUsuario"

        // Grid com 2 colunas
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvMidiasUsuario.setLayoutManager(gridLayoutManager);

        midiasAdapterUsuario = new AdapterVisualizarDenunciasAgentes();
        rvMidiasUsuario.setAdapter(midiasAdapterUsuario);

        midiasAdapterUsuario.setOnMidiaClickListener(new AdapterVisualizarDenunciasAgentes.OnMidiaClickListener() {
            @Override
            public void onImagemClick(String url) {
                Log.d("CLICK", "Imagem clicada: " + url);
                Intent intent = new Intent(ActivityVisualizarDenunciasUsuario.this, FullscreenActivity.class);
                intent.putExtra("urlMidia", url);
                intent.putExtra("tipo", "imagem");
                startActivity(intent);
            }

            @OptIn(markerClass = UnstableApi.class) @Override
            public void onVideoClick(String url) {
                Log.d("VIDEO_CLICK", "URL do vídeo: " + url);
                Toast.makeText(ActivityVisualizarDenunciasUsuario.this, "Abrindo vídeo...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ActivityVisualizarDenunciasUsuario.this, FullScreenVisualizarMidiaReclamacoes.class);
                intent.putExtra("urlMidia", url);
                startActivity(intent);
            }
        });
    }

    private void populaDados() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ActivityVisualizarDenunciasUsuario.this,
                            "Denúncia não encontrada no servidor", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ClassReclamacoes r = snapshot.getValue(ClassReclamacoes.class);
                if (r != null) {
                    bindingUsers.txtVisualizarReclamacaoUsuario.setText(r.getReclamacao());
                    bindingUsers.txtEndReclamacaoUsuario.setText(
                            String.format("%s, %s", r.getEndereco_reclamacao(), r.getNum_casa_reclamacao()));
                    bindingUsers.txtReferenciaEndReclamacaoUsuario.setText(r.getReferencia());
                }

                // Busca as URLs das mídias
                DataSnapshot midiaSnapshot = snapshot.child("midia_reclamacoes");
                if (midiaSnapshot.exists()) {
                    List<AdapterVisualizarDenunciasAgentes.MidiaItem> listaMidias = new ArrayList<>();

                    // Adiciona imagem 1 (midia_0)
                    String urlImagem1 = midiaSnapshot.child("midia_0").getValue(String.class);
                    if (urlImagem1 != null && !urlImagem1.isEmpty()) {
                        listaMidias.add(new AdapterVisualizarDenunciasAgentes.MidiaItem(urlImagem1, false, null));
                    }

                    // Adiciona imagem 2 (midia_1)
                    String urlImagem2 = midiaSnapshot.child("midia_1").getValue(String.class);
                    if (urlImagem2 != null && !urlImagem2.isEmpty()) {
                        listaMidias.add(new AdapterVisualizarDenunciasAgentes.MidiaItem(urlImagem2, false, null));
                    }

                    // Adiciona imagem 3 (midia_2)
                    String urlImagem3 = midiaSnapshot.child("midia_2").getValue(String.class);
                    if (urlImagem3 != null && !urlImagem3.isEmpty()) {
                        listaMidias.add(new AdapterVisualizarDenunciasAgentes.MidiaItem(urlImagem3, false, null));
                    }

                    // Adiciona vídeo (midia_3) + thumbnail (midia_3_thumb)
                    String urlVideo = midiaSnapshot.child("midia_3").getValue(String.class);
                    if (urlVideo != null && !urlVideo.isEmpty()) {
                        String urlThumb = midiaSnapshot.child("midia_3_thumb").getValue(String.class);
                        listaMidias.add(new AdapterVisualizarDenunciasAgentes.MidiaItem(urlVideo, true, urlThumb));
                    }

                    // LOGS para depuração
                    Log.d("MIDIAS", "Total de mídias: " + listaMidias.size());
                    for (AdapterVisualizarDenunciasAgentes.MidiaItem item : listaMidias) {
                        Log.d("MIDIAS", "Item: " + item.getUrl() + " isVideo: " + item.isVideo());
                    }

                    // Só mostra o RecyclerView se houver mídias
                    if (!listaMidias.isEmpty()) {
                        midiasAdapterUsuario.setMidias(listaMidias);
                        bindingUsers.rvMidiasUsuario.setVisibility(View.VISIBLE);
                    } else {
                        bindingUsers.rvMidiasUsuario.setVisibility(View.GONE);
                    }
                } else {
                    bindingUsers.rvMidiasUsuario.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", error.getMessage());
                Toast.makeText(ActivityVisualizarDenunciasUsuario.this,
                        "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            }
        });

        Toolbar toolbar = bindingUsers.toolbarVisualReclaUsuario;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }


}