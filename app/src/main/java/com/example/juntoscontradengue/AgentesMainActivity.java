package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.example.juntoscontradengue.extras.AppConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AgentesMainActivity extends AppCompatActivity {

    private String nome, funcao;
    private TextView textView_nome_agente;
    private TextView textView_funcao;
    private FirebaseAuth mAuth;
    private FirebaseDatabase databaseMunicipio;
    private androidx.appcompat.app.AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.juntoscontradengue.databinding.ActivityMainAgentesBinding bidingAgentes =
                com.example.juntoscontradengue.databinding.ActivityMainAgentesBinding.inflate(getLayoutInflater());
        setContentView(bidingAgentes.getRoot());

        // Inicializa Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializa configurações do município
        String estado = AppConfig.getEstado(this);
        String municipio = AppConfig.getMunicipio(this);
        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        // Configuração da Toolbar
        Toolbar toolbar = bidingAgentes.toolbarMainAgentes;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        toolbar.setTitle(getTitle());

        // Referências dos TextViews
        textView_nome_agente = bidingAgentes.txtNomeAgenteTelaMain;
        textView_funcao = bidingAgentes.txtFuncaoAgenteTelaMainAgente;

        // Carrega os dados do usuário
        carregarDadosUsuario();

        // Configuração dos botões
        configurarBotoes(bidingAgentes);

        // Interceptação do botão físico de voltar
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AgentesMainActivity.this, "Use o botão Sair.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarDadosUsuario() {
        // Primeiro, tenta recuperar do SharedPreferences
        SharedPreferences prefUser = getSharedPreferences("UserData", MODE_PRIVATE);
        nome = prefUser.getString("nome", "");
        funcao = prefUser.getString("funcao", "");

        // Se tem dados no SharedPreferences, exibe
        if (!TextUtils.isEmpty(nome) && !TextUtils.isEmpty(funcao)) {
            atualizarTextViews(nome, funcao);
            return;
        }

        // Se não tem dados ou estão incompletos, busca do Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            buscarDadosUsuarioPorUid(uid);
        } else {
            // Usuário não autenticado, redireciona para login
            goToMainActivity();
        }
    }

    private void buscarDadosUsuarioPorUid(String uid) {
        showLoading();

        DatabaseReference usersRef = databaseMunicipio.getReference()
                .child("logins")
                .child("agentes");

        // Busca pelo UID do usuário
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideLoading();

                if (snapshot.exists()) {
                    // Extrai os dados
                    String nomeBanco = snapshot.child("nome").getValue(String.class);
                    String funcaoBanco = snapshot.child("funcao").getValue(String.class);
                    String cpfBanco = snapshot.child("cpf").getValue(String.class);
                    String emailBanco = snapshot.child("email").getValue(String.class);

                    // Busca timestamps
                    Long dataLong = snapshot.child("dataCadastro").getValue(Long.class);
                    Long updateLong = snapshot.child("updateAt").getValue(Long.class);
                    String dataCadastro = (dataLong != null) ? String.valueOf(dataLong) : "0";
                    String updateAt = (updateLong != null) ? String.valueOf(updateLong) : "0";

                    // Salva no SharedPreferences
                    salvarDadosLocalmente(
                            nomeBanco != null ? nomeBanco : "",
                            cpfBanco != null ? cpfBanco : "",
                            emailBanco != null ? emailBanco : "",
                            funcaoBanco != null ? funcaoBanco : "agentes",
                            dataCadastro,
                            updateAt
                    );

                    // Atualiza as variáveis e TextViews
                    nome = nomeBanco;
                    funcao = funcaoBanco;
                    atualizarTextViews(nome, funcao);

                } else {
                    // Tenta buscar em outra estrutura (fallback)
                    buscarDadosUsuarioPorEmail(usersRef, uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Toast.makeText(AgentesMainActivity.this,
                        "Erro ao buscar dados: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Fallback: tenta buscar por email
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.getEmail() != null) {
                    buscarDadosUsuarioPorEmail(usersRef, user.getEmail());
                } else {
                    // Mensagem padrão
                    textView_nome_agente.setText(R.string.bem_vindo);
                    textView_funcao.setText(R.string.agente_de_saude);
                }
            }
        });
    }

    private void buscarDadosUsuarioPorEmail(DatabaseReference usersRef, String emailOrUid) {
        // Busca pelo email
        usersRef.orderByChild("email").equalTo(emailOrUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hideLoading();

                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String nomeBanco = userSnapshot.child("nome").getValue(String.class);
                                String funcaoBanco = userSnapshot.child("funcao").getValue(String.class);
                                String cpfBanco = userSnapshot.child("cpf").getValue(String.class);
                                String emailBanco = userSnapshot.child("email").getValue(String.class);

                                // Salva no SharedPreferences
                                salvarDadosLocalmente(
                                        nomeBanco != null ? nomeBanco : "",
                                        cpfBanco != null ? cpfBanco : "",
                                        emailBanco != null ? emailBanco : "",
                                        funcaoBanco != null ? funcaoBanco : "agentes",
                                        "0",
                                        "0"
                                );

                                nome = nomeBanco;
                                funcao = funcaoBanco;
                                atualizarTextViews(nome, funcao);
                                return;
                            }
                        } else {
                            // Nenhum dado encontrado
                            textView_nome_agente.setText(R.string.bem_vindo);
                            textView_funcao.setText(R.string.agente_de_saude);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideLoading();
                        textView_nome_agente.setText(R.string.bem_vindo);
                        textView_funcao.setText(R.string.agente_de_saude);
                    }
                });
    }

    private void salvarDadosLocalmente(String nome, String cpf, String email, String funcao,
                                       String dataCadastro, String updateAt) {
        SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nome", nome);
        editor.putString("cpf", cpf);
        editor.putString("email", email);
        editor.putString("perfil", "agentes");
        editor.putString("funcao", funcao);
        editor.putString("dataCadastro", dataCadastro);
        editor.putString("updateAt", updateAt);
        editor.apply();
    }

    private void atualizarTextViews(String nome, String funcao) {
        if (!TextUtils.isEmpty(nome)) {
            textView_nome_agente.setText(nome.toUpperCase());
        } else {
            textView_nome_agente.setText(R.string.bem_vindo);
        }

        if (!TextUtils.isEmpty(funcao)) {
            textView_funcao.setText(funcao.toUpperCase());
        } else {
            textView_funcao.setText(R.string.agente_de_saude);
        }
    }

    private void configurarBotoes(com.example.juntoscontradengue.databinding.ActivityMainAgentesBinding binding) {
        ImageButton reclamacao = binding.btnTelaAgentesAcompReclamacoes;
        ImageButton add_trab_agentes = binding.btnAddTrabAgentes;
        ImageButton excluir_trab_agentes = binding.btnExcluiTrabAgente;
        ImageButton config_conta = binding.btnConfigAgentes;
        ImageButton termosUso = binding.btnTermosTelaAgentes;
        ImageButton sair_conta = binding.btnSairAgentes;

        reclamacao.setOnClickListener(v -> verifica_reclamacao());
        add_trab_agentes.setOnClickListener(v -> adicionarTrabCampo());
        excluir_trab_agentes.setOnClickListener(v -> excluirTrabCampo());
        config_conta.setOnClickListener(v -> config_conta());
        termosUso.setOnClickListener(v -> termos_uso_privacidade());
        sair_conta.setOnClickListener(v -> sairConta());
    }

    // Trava de Segurança em tempo real
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goToMainActivity();
        }
    }

    private void verifica_reclamacao() {
        Intent it = new Intent(this, ListarReclamacoesAgentes.class);
        startActivity(it);
    }

    private void adicionarTrabCampo() {
        startActivity(new Intent(this, UploadTrabAgentes.class));
    }

    private void excluirTrabCampo() {
        startActivity(new Intent(this, ExcluirTrabAgentesActivity.class));
    }

    private void config_conta() {
        Intent itent_agentes = new Intent(this, ProfileActivity.class);
        startActivity(itent_agentes);
    }

    private void termos_uso_privacidade() {
        Intent itent_agentes = new Intent(this, TermosDeUsoActivity.class);
        startActivity(itent_agentes);
    }

    private void sairConta() {
        FirebaseAuth.getInstance().signOut();

        // Limpa os dados salvos
        SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
        pref.edit().clear().apply();

        CredentialManager credentialManager = CredentialManager.create(this);
        ClearCredentialStateRequest request = new ClearCredentialStateRequest();

        credentialManager.clearCredentialStateAsync(request, null, Runnable::run,
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        goToMainActivity();
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        goToMainActivity();
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(AgentesMainActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
        Toast.makeText(this, "Usuário deslogado.", Toast.LENGTH_LONG).show();
    }

    private void showLoading() {
        if (loadingDialog == null) {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);
            android.view.View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(view);
            builder.setCancelable(false);
            loadingDialog = builder.create();
        }
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}