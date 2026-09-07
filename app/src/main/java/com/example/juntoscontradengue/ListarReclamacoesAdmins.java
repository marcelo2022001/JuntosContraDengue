package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.juntoscontradengue.database.adapters.AdapterReclamacaoAdmins;
import com.example.juntoscontradengue.database.classes_database.ClassReclamacoesAdminsAgentes;
import com.example.juntoscontradengue.databinding.ActivityVerificarReclamacoesAdminsBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListarReclamacoesAdmins extends AppCompatActivity
        implements AdapterReclamacaoAdmins.OnSelectionChangedListener {

    private @NonNull ActivityVerificarReclamacoesAdminsBinding binding;
    private AdapterReclamacaoAdmins adapter;
    private Boolean isAdmin = false;
    private final List<ClassReclamacoesAdminsAgentes> listaCompleta = new ArrayList<>();
    private FirebaseDatabase databaseMunicipio;
    // TextView
    TextView txtTotal, txtRespondidas, txtAguardandoUsuarioAvaliar, txtAguardandoResposta, txtResolvidas, txtNaoResolvidas;

    Boolean isConnected;

    // Menu (ação de ocultar aparece só quando há seleção)
    private Menu menu;
    private int selecionadosAtual = 0;

    // Contadores
    int total = 0;
    int respondidas = 0;
    int resolvidas = 0;
    int naoResolvidas = 0;
    int aguardando = 0;

    public ListarReclamacoesAdmins() {
        binding = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerificarReclamacoesAdminsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        String estado = prefs.getString("estado", "");
        String municipio = prefs.getString("municipio", "");

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + ".firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);


        SharedPreferences prefUser = getSharedPreferences("UserData", MODE_PRIVATE);
        String perfil = prefUser.getString("perfil", "");
        isAdmin = "admins".equalsIgnoreCase(perfil) || "admin".equalsIgnoreCase(perfil);

        isConnected = NetworkUtils.isNetworkAvailable(ListarReclamacoesAdmins.this);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reclamacoes_agentes, menu);
        this.menu = menu;
        atualizarVisibilidadeAcaoOcultar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_ocultar_selecionadas) {
            confirmarOcultarSelecionadas();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void atualizarVisibilidadeAcaoOcultar() {
        if (menu == null) return;
        MenuItem acaoOcultar = menu.findItem(R.id.action_ocultar_selecionadas);
        if (acaoOcultar != null) {
            boolean podeMostrar = isAdmin && selecionadosAtual > 0;
            acaoOcultar.setVisible(podeMostrar);
            acaoOcultar.setTitle(getString(R.string.excluir_selecionadas_com_total, selecionadosAtual));
        }
    }

    // ================= SELEÇÃO =================

    @Override
    public void onSelectionChanged(int selectedCount) {
        selecionadosAtual = selectedCount;
        atualizarVisibilidadeAcaoOcultar();
    }

    private void confirmarOcultarSelecionadas() {
        if (!isAdmin) {
            Toast.makeText(this, "Apenas administradores podem excluir reclamações.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ClassReclamacoesAdminsAgentes> selecionadas = adapter.getSelectedItems();
        if (selecionadas.isEmpty()) return;

        List<ClassReclamacoesAdminsAgentes> elegiveis = new ArrayList<>();
        int ignoradas = 0;
        for (ClassReclamacoesAdminsAgentes r : selecionadas) {
            String status = r.getStatus() == null ? "" : r.getStatus().trim();
            if (status.equalsIgnoreCase("Resolvido") || status.equalsIgnoreCase("Não Resolvido")) {
                elegiveis.add(r);
            } else {
                ignoradas++;
            }
        }

        if (elegiveis.isEmpty()) {
            Toast.makeText(this, "Só é possível excluir reclamações já resolvidas ou não resolvidas.", Toast.LENGTH_LONG).show();
            return;
        }

        String mensagem = getString(R.string.excluir_reclamacoes_mensagem, elegiveis.size());
        if (ignoradas > 0) {
            mensagem += "\n\n" + ignoradas + " reclamação(ões) selecionada(s) ainda aguarda(m) resposta e não será(ão) excluida(s).";
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.excluir_reclamacoes_titulo)
                .setMessage(mensagem)
                .setPositiveButton(R.string.excluir, (dialog, which) -> ocultarReclamacoesSelecionadas(elegiveis))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    /**
     * Não exclui a reclamação do banco: apenas marca visivel_agente = false
     * em cadastros/{estado}/{municipio}/reclamacoes/{idUsuario}/{idReclamacao}.
     */
    private void ocultarReclamacoesSelecionadas(List<ClassReclamacoesAdminsAgentes> selecionadas) {

        DatabaseReference ref = databaseMunicipio.getReference()
                .child("reclamacoes");

        Map<String, Object> updates = new HashMap<>();
        for (ClassReclamacoesAdminsAgentes r : selecionadas) {
            if (r.getIdUsuario() == null || r.getIdReclamacao() == null) continue;
            updates.put(r.getIdUsuario() + "/" + r.getIdReclamacao() + "/visivel_agente", false);
        }

        if (updates.isEmpty()) return;

        ref.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.reclamacoes_excluidas_sucesso, Toast.LENGTH_SHORT).show();
                    adapter.clearSelection();
                    // A lista é atualizada automaticamente pelo ValueEventListener em carregarReclamacoes()
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Erro ao ocultar reclamações: " + e.getMessage());
                    Toast.makeText(this, R.string.error_excluir_reclamacoes, Toast.LENGTH_SHORT).show();
                });
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
        txtTotal.setText(R.string.total_de_reclamacoes);
        txtRespondidas.setText(R.string.reclamacoes_respondidas);
        txtAguardandoResposta.setText(R.string.aguardando_resposta);
        txtResolvidas.setText(R.string.resolvidas);
        txtNaoResolvidas.setText(R.string.nao_resolvidas);
    }

    // ================= RECYCLER =================

    private void setupRecycler() {
        adapter = new AdapterReclamacaoAdmins(new ArrayList<>(), this);
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
        DatabaseReference dbRef = databaseMunicipio.getReference()
                .child("reclamacoes");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCompleta.clear();

                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {
                    String idUsuario = usuarioSnap.getKey();

                    for (DataSnapshot reclamacaoSnap : usuarioSnap.getChildren()) {
                        ClassReclamacoesAdminsAgentes reclamacao =
                                reclamacaoSnap.getValue(ClassReclamacoesAdminsAgentes.class);

                        // ❌ Reclamação ocultada
                        // (visivel_agente: false) não entra na lista
                        if (reclamacao != null && !reclamacao.isVisivelAgente()) {
                            continue;
                        }

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

        for (ClassReclamacoesAdminsAgentes r : listaCompleta) {
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

        txtTotal.setText(getString(R.string.total_de_reclamacoes_contagem, total));
        txtRespondidas.setText(getString(R.string.reclamacoes_respondidas_contagem, total_respondidas));
        txtAguardandoUsuarioAvaliar.setText(getString(R.string.aguardando_avaliacao_usuario_contagem, respondidas));
        txtAguardandoResposta.setText(getString(R.string.aguardando_resposta_contagem, aguardando));
        txtResolvidas.setText(getString(R.string.resolvidas_contagem, resolvidas));
        txtNaoResolvidas.setText(getString(R.string.nao_resolvidas_contagem, naoResolvidas));

    }
}