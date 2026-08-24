package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.juntoscontradengue.database.adapters.AdapterReclamacaoAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassReclamacoes;
import com.example.juntoscontradengue.databinding.ActivityVerificarReclamacoesAgentesBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListarReclamacoesAgentes extends AppCompatActivity {

    private ActivityVerificarReclamacoesAgentesBinding binding;
    private AdapterReclamacaoAgentes adapter;

    private final List<ClassReclamacoes> listaCompleta = new ArrayList<>();

    // TextView
    TextView txtTotal, txtRespondidas, txtAguardandoUsuarioAvaliar, txtAguardandoResposta, txtResolvidas, txtNaoResolvidas;

    private String estado, municipio;

    Boolean isConnected;

    // Contadores
    int total = 0;
    int respondidas = 0;
    int resolvidas = 0;
    int naoResolvidas = 0;
    int aguardando = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerificarReclamacoesAgentesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", "");
        municipio = prefs.getString("municipio", "");

        isConnected = NetworkUtils.isNetworkAvailable(ListarReclamacoesAgentes.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "agentes_reclamacoes");
            startActivity(itente);

        }

        setupToolbar();
        setupTextViews();
        setupRecycler();
        setupSpinner();
        carregarReclamacoes();
    }

    // ================= TOOLBAR =================

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbarReclamacoes;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    // =================== TextView ==================
    private void setupTextViews() {
        txtTotal = binding.txtTotal;
        txtRespondidas = binding.txtRespondidas;
        txtAguardandoUsuarioAvaliar = binding.txtAguardandoAvaliacao;
        txtAguardandoResposta = binding.txtAguardando;
        txtResolvidas = binding.txtResolvidas;
        txtNaoResolvidas = binding.txtNaoResolvidas;

        // ✅ Inicializa com valores padrão
        txtTotal.setText("Total de reclamações: 0");
        txtRespondidas.setText("Reclamações respondidas: 0");
        txtAguardandoResposta.setText("Aguardando resposta: 0");
        txtResolvidas.setText("Resolvidas: 0");
        txtNaoResolvidas.setText("Não resolvidas: 0");
    }

    // ================= RECYCLER =================

    private void setupRecycler() {
        adapter = new AdapterReclamacaoAgentes(new ArrayList<>());
        binding.recyclerViewReclamacoes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewReclamacoes.setAdapter(adapter);
        binding.recyclerViewReclamacoes.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );
    }

    // ================= SPINNER =================

    private void setupSpinner() {
        binding.spinnerFiltroStatus.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id) {
                        String status = parent.getItemAtPosition(position).toString();
                        adapter.filtrarPorStatus(status);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    // ================= FIREBASE =================

    private void carregarReclamacoes() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("reclamacoes");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCompleta.clear();

                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {
                    String idUsuario = usuarioSnap.getKey();

                    for (DataSnapshot reclamacaoSnap : usuarioSnap.getChildren()) {
                        ClassReclamacoes reclamacao =
                                reclamacaoSnap.getValue(ClassReclamacoes.class);

                        if (reclamacao != null) {
                            reclamacao.setIdUsuario(idUsuario);
                            reclamacao.setIdReclamacao(reclamacaoSnap.getKey());
                            listaCompleta.add(reclamacao);
                        }
                    }
                }

                // ✅ Atualiza contadores
                atualizarContadores();

                // ✅ Atualiza adapter
                adapter.updateList(listaCompleta);

                // ✅ Mostra/oculta mensagem vazia
                binding.layoutEmpty.setVisibility(
                        listaCompleta.isEmpty() ? View.VISIBLE : View.GONE
                );

                // Logs para debug
                Log.d("CONTAGEM", "Total: " + total);
                Log.d("CONTAGEM", "Resolvidas: " + resolvidas);
                Log.d("CONTAGEM", "Não Resolvidas: " + naoResolvidas);
                Log.d("CONTAGEM", "Aguardando: " + aguardando);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", error.getMessage());
            }
        });
    }

    private void atualizarContadores() {
        // ✅ Zera os contadores
        total = listaCompleta.size();
        respondidas = 0;
        resolvidas = 0;
        naoResolvidas = 0;
        aguardando = 0;

        for (ClassReclamacoes r : listaCompleta) {
            String status = r.getStatus();

            if (status == null || status.isEmpty()) {
                aguardando++; // ✅ Se não tem status, considera como aguardando
                continue;
            }

            // ✅ Normaliza o status para comparação
            String statusNormalizado = status.trim().toLowerCase();

            // ✅ Verifica cada status específico
            switch (statusNormalizado) {
                case "resolvido":
                    resolvidas++;
                    break;
                case "não resolvido":
                    naoResolvidas++;
                    break;
                case "aguardando resposta":
                    aguardando++;
                    break;
                default:
                    respondidas++;
                    break;
            }
        }

        int total_respondidas = total - aguardando;

        // ✅ Atualiza as TextView
        txtTotal.setText("Total de reclamações: " + total);
        txtRespondidas.setText("Reclamações respondidas: " + total_respondidas);
        txtAguardandoUsuarioAvaliar.setText("Respondido/Aguardando usuário avaliar: " + respondidas);
        txtAguardandoResposta.setText("Aguardando resposta: " + aguardando);
        txtResolvidas.setText("Resolvidas: " + resolvidas);
        txtNaoResolvidas.setText("Não resolvidas: " + naoResolvidas);

    }
}