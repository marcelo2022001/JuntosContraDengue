package com.example.juntoscontradengue;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityLoginAgentesBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.AppConfig;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.ValidaCpf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ActivityLoginAgentes extends AppCompatActivity {

    private androidx.appcompat.app.AlertDialog loadingDialog;
    private FirebaseAuth mAuth;
    private FirebaseDatabase databaseMunicipio;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private boolean isPasswordVisible = false;
    private EditText edt_txt_email_agente, edt_txt_senha_agente, edt_txt_pre_cadastro;
    private ActivityLoginAgentesBinding loguinAgentesBinding;
    private String  emailAgente, senhaAgente,  estado, municipio;
    private String cpf, cpfLimpo, mensagem;
    private String nome_usuario, emailSharedPrefers, endereco, num_casa, conjunto, telefone, dataCadastro, updateAt;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_login_agentes);

        loguinAgentesBinding = ActivityLoginAgentesBinding.inflate(getLayoutInflater());
        setContentView(loguinAgentesBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        SharedPreferences prefsUser = getSharedPreferences("UserData", MODE_PRIVATE);
        emailSharedPrefers = prefsUser.getString("email", null);

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + ".firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        setupToolbar();
        initializeViews();

    }

    private void setupToolbar() {
        Toolbar toolbar = loguinAgentesBinding.toolbarLoguinAgentes;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeViews() {
        estado = AppConfig.getEstado(this);
        municipio = AppConfig.getMunicipio(this);

        edt_txt_email_agente = loguinAgentesBinding.edtTxtEmailLoguinAgentes;
        edt_txt_senha_agente = loguinAgentesBinding.edtTxtSenhaTelaLoguinAgentes;
        edt_txt_pre_cadastro = loguinAgentesBinding.edtTxtConcluirPreCadastro;

        edt_txt_pre_cadastro.addTextChangedListener(MaskEditUtil.mask(MaskEditUtil.FORMAT_CPF));


        Button btnEntrarTelaLoguinAdmin = loguinAgentesBinding.btnEntrarTelaLoguinAgentes;
        btnEntrarTelaLoguinAdmin.setOnClickListener(v -> entrar_conta_admin());

        TextView txtRecuperarSenha = loguinAgentesBinding.txtRecupSenhaLoguinAgentes;
        txtRecuperarSenha.setOnClickListener(v -> recuperarSenhaAgente());

       Button continuar_pre_cadastro = loguinAgentesBinding.btnContinuarPreCadastro;
       continuar_pre_cadastro.setOnClickListener(v -> pre_cadastro());

       Button cancelar_pre_cadastro = loguinAgentesBinding.btnCancelarPreCadastro;
       cancelar_pre_cadastro.setOnClickListener(v -> sair_pre_cadastro());

        edt_txt_senha_agente.setOnTouchListener((v, event) -> {
            // Verifica se o toque foi no drawableEnd
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edt_txt_senha_agente.getRight() - edt_txt_senha_agente.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });
    }

    private void sair_pre_cadastro() {
        Intent intent = new Intent(ActivityLoginAgentes.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);    }

    private void pre_cadastro() {

        isConnected = NetworkUtils.isNetworkAvailable(ActivityLoginAgentes.this);
        if (!isConnected) {
              Toast.makeText(ActivityLoginAgentes.this, "Sem conexão de internet! Ative o wifi ou dados móveis!", Toast.LENGTH_SHORT).show();
        return;   }

        String stCpfPreCadastro = edt_txt_pre_cadastro.getText().toString().trim();
        // Limpa máscara do CPF
         cpfLimpo = stCpfPreCadastro.replaceAll("[.\\-]", "");


        if (stCpfPreCadastro.isEmpty()) {

            edt_txt_pre_cadastro.setError("Digite um cpf");

        } else if (!ValidaCpf.validaCPF(stCpfPreCadastro)) {

            edt_txt_pre_cadastro.setError("Digite um CPF válido");

        } else {

            buscaCadExiste(new CadastroCallback() {
                @Override
                public void onLiberado() {

                    continuaCadastro(cpfLimpo);
                }

                @Override
                public void onErro(String msg) {
                    Alertas.showAlertDialog(ActivityLoginAgentes.this, "Alerta", msg);
                }
            });

        }
    }

    private void buscaCadExiste(CadastroCallback callback) {
        showLoading();
        databaseMunicipio.getReference()
                .child("cpf_index")
                .child(cpfLimpo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        hideLoading();

                        if (snapshot.exists()) {

                            //encontrado
                            callback.onErro("Este CPF já está cadastrado.");

                        } else {
                            callback.onLiberado();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideLoading();
                        callback.onErro("Erro ao verificar CPF: " + error.getMessage());
                    }
                });
    }


    private void continuaCadastro(String cpfLimpo) {

            showLoading();

            // Primeiro, vamos verificar se o nó pre_cadastro_admins existe
        DatabaseReference preCadastroRef = databaseMunicipio.getReference()
                    .child("config")
                    .child("pre_cadastro_agentes");

            // Buscar especificamente pelo CPF
            preCadastroRef.child(cpfLimpo)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // CPF encontrado no pré-cadastro
                                hideLoading();

                                // Recuperar os dados do pré-cadastro
                                String nome = dataSnapshot.child("nome_pre_cadastro").getValue(String.class);
                                String funcao = dataSnapshot.child("funcao_pre_cadastro").getValue(String.class);

                                Intent intent = new Intent(ActivityLoginAgentes.this, CriarCadastroAgente.class);
                                intent.putExtra("cpf", edt_txt_pre_cadastro.getText().toString().trim());
                                intent.putExtra("nome", nome);
                                intent.putExtra("funcao", funcao);
                                intent.putExtra("tipo_conta", "agente");
                                startActivity(intent);

                            } else {
                                // Verificar se o problema é que o CPF está em outro formato
                                verificarFormatoAlternativo(preCadastroRef, cpfLimpo);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            hideLoading();
                            Toast.makeText(ActivityLoginAgentes.this,
                                    "Erro ao buscar servidor: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void verificarFormatoAlternativo(DatabaseReference preCadastroRef, String cpfLimpo) {

            // Tentar buscar sem formatação (caso o CPF no banco esteja sem pontos e traços)
            preCadastroRef.orderByKey().equalTo(cpfLimpo)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            hideLoading();

                            if (dataSnapshot.exists()) {
                                //encontrado

                                // Recuperar os dados do pré-cadastro
                                String nome = dataSnapshot.child("nome_pre_cadastro").getValue(String.class);
                                String funcao = dataSnapshot.child("funcao_pre_cadastro").getValue(String.class);

                                Intent intent = new Intent(ActivityLoginAgentes.this, CriarCadastroAgente.class);
                                // DICA: Geralmente você vai querer passar o dados para a próxima tela
                                intent.putExtra("tipo_conta", "agentes");
                                intent.putExtra("cpf", edt_txt_pre_cadastro.getText().toString().trim());
                                intent.putExtra("nome", nome);
                                intent.putExtra("funcao", funcao);
                                startActivity(intent);

                            } else {
                                // Realmente não encontrado
                                AlertDialog dialog = new AlertDialog.Builder(ActivityLoginAgentes.this)
                                        .setTitle("Não encontrado")
                                        .setMessage("Solicite ao administrador criar seu pré-cadastro")
                                        .setPositiveButton("OK", null)
                                        .create();
                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            hideLoading();
                            Toast.makeText(ActivityLoginAgentes.this,
                                    "Erro na verificação: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Esconder a senha
            edt_txt_senha_agente.setTransformationMethod(PasswordTransformationMethod.getInstance());
            edt_txt_senha_agente.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_closed, 0); // Ícone de olho fechado
        } else {
            // Mostrar a senha
            edt_txt_senha_agente.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            edt_txt_senha_agente.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_open, 0); // Ícone de olho aberto
        }
        isPasswordVisible = !isPasswordVisible;

        // Move o cursor para o final do texto
        edt_txt_senha_agente.setSelection(edt_txt_senha_agente.getText().length());
    }

    private void recuperarSenhaAgente() {
        isConnected = NetworkUtils.isNetworkAvailable(ActivityLoginAgentes.this);
        if (!isConnected) {
            Toast.makeText(ActivityLoginAgentes.this, "Sem conexão de internet! Ative o wifi ou dados móveis!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ( TextUtils.isEmpty(edt_txt_email_agente.getText().toString()) ) {
            Toast.makeText(ActivityLoginAgentes.this, "Favor preencha o email!", Toast.LENGTH_LONG).show();
        } else {
            emailAgente = edt_txt_email_agente.getText().toString().trim();

            mAuth.sendPasswordResetEmail(emailAgente).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    Toast.makeText(ActivityLoginAgentes.this, "Recuperação de acesso iniciada. Foi enviado um e-mail para " + emailAgente + " verifique a caixa de entrada do e-mail ou spam.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityLoginAgentes.this, "Erro! Tente novamente", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void entrar_conta_admin() {

        // Chamada ao método da classe NetworkUtils
        isConnected = NetworkUtils.isNetworkAvailable(ActivityLoginAgentes.this);
        if (isConnected) {
            // A conexão está disponível, prossiga com o login

            emailAgente = edt_txt_email_agente.getText().toString();
            senhaAgente = edt_txt_senha_agente.getText().toString();

            if ( TextUtils.isEmpty(emailAgente) || (TextUtils.isEmpty(senhaAgente))) {
                Alertas.showAlertDialog(ActivityLoginAgentes.this, "Aviso", "Favor preencher os campos email e senha.");
                return;

            }
                showLoading();

                loginAgentes();


        } else {
            // Exiba uma mensagem de erro ou aviso
            Toast.makeText(ActivityLoginAgentes.this, "Sem conexão de internet! Ative o wifi ou dados móveis!", Toast.LENGTH_SHORT).show();

        }
    }

    private void loginAgentes() {

        mAuth.signInWithEmailAndPassword(emailAgente, senhaAgente)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        // Salvar dados do usuário no SharedPreferences
                        // Shared dados usuario null ou vazio
                        if( !emailAgente.equals(emailSharedPrefers)){

                            buscaDadosUsuario(emailAgente, new EmailCallback() {
                                @Override
                                public void onEmailEncontrado(String emailResult, String nomeResult) {

                                    salvarDadosLocalmente(); // Função auxiliar para organizar o código
                                    irParaActivityPrincipal(nomeResult);

                                }
                                @Override
                                public void onErro(String erro) {
                                    hideLoading();
                                    Toast.makeText(ActivityLoginAgentes.this, erro, Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Bundle extras = getIntent().getExtras();
                            if (extras != null && extras.getString("nome") != null) {
                                mensagem += extras.getString("nome");
                            } else {
                                mensagem += "Agente de Endemias";
                            }
                            salvarDadosLocalmente();
                            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, ActivityLoginAgentes.class));
                        }
                    } else {

                        hideLoading();

                        Exception e = task.getException();

                        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {

                            Alertas.showAlertDialog(
                                    ActivityLoginAgentes.this,
                                    "Falha no login",
                                    "E-mail ou senha incorretos."
                            );

                        } else if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {

                            Alertas.showAlertDialog(
                                    ActivityLoginAgentes.this,
                                    "Conta não encontrada",
                                    "Não existe uma conta cadastrada com este e-mail."
                            );

                        } else {

                            Alertas.showAlertDialog(
                                    ActivityLoginAgentes.this,
                                    "Erro",
                                    e != null ? e.getMessage() : "Não foi possível realizar o login."
                            );
                        }
                    }
                });
    }

    private void irParaActivityPrincipal(String nomeOpcional) {
        hideLoading();

         mensagem = "Bem vindo, ";

        // Tenta pegar o nome que veio do banco ou dos Extras
        if (nomeOpcional != null) {
            mensagem += nomeOpcional;
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.getString("nome") != null) {
                mensagem += extras.getString("nome");
            } else {
                mensagem += "Agente de Endemias";
            }
        }
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, AgentesMainActivity.class));

    }

    private void salvarDadosLocalmente() {
        SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nome", nome_usuario);
        editor.putString("cpf", cpf);
        editor.putString("email", emailSharedPrefers);
        editor.putString("endereco", endereco);
        editor.putString("num_casa", num_casa);
        editor.putString("conjunto", conjunto);
        editor.putString("telefone", telefone);
        editor.putString("dataCadastro", dataCadastro);
        editor.putString("updateAt", updateAt);
        editor.putString("perfil", "agentes");
        editor.apply();
    }
    private void buscaDadosUsuario(String emailAgente, EmailCallback emailCallback) {

        DatabaseReference usersRef = databaseMunicipio.getReference()
                .child("logins")
                .child("agentes");

        Query query = usersRef.orderByChild("email").equalTo(emailAgente);

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    processarDadosUsuario(snapshot, emailCallback);
                } else {
                    // 2ª Tentativa: Se não achou pelo email, busca pelo "novoEmail"
                    Query queryNovoEmail = usersRef.orderByChild("novoEmail").equalTo(emailAgente);

                    queryNovoEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotNovo) {
                            if (snapshotNovo.exists()) {
                                // Se encontrou pelo novo email, atualiza o banco principal para efetivar a troca
                                for (DataSnapshot userSnapshot : snapshotNovo.getChildren()) {
                                    String uidEncontrado = userSnapshot.getKey();
                                    if (uidEncontrado != null) {

                                        // 1. Efetiva a troca no banco: o email principal vira o emailAgente (novo) e limpa o pendente (novoEmail = null)
                                        Map<String, Object> atualizacao = new HashMap<>();
                                        atualizacao.put("email", emailAgente);
                                        atualizacao.put("novoEmail", null);

                                        usersRef.child(uidEncontrado).updateChildren(atualizacao);

                                        // 2. Atualiza localmente no SharedPreferences o novo email
                                        SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("email", emailAgente);
                                        editor.putString("updateAt", updateAt);
                                        editor.apply();
                                    }
                                }
                                processarDadosUsuario(snapshotNovo, emailCallback);
                            } else {
                                hideLoading();
                                emailCallback.onErro("Usuário não encontrado na base de dados.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            hideLoading();
                            emailCallback.onErro(error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                emailCallback.onErro(error.getMessage());
            }
        });
    }

    private void processarDadosUsuario(DataSnapshot snapshot, EmailCallback callback) {
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            // Busca no db
            nome_usuario = userSnapshot.child("nome").getValue(String.class);
            cpf = userSnapshot.child("cpf").getValue(String.class);
            emailSharedPrefers = userSnapshot.child("email").getValue(String.class);
            endereco = userSnapshot.child("endereco").getValue(String.class);
            num_casa = userSnapshot.child("num_casa").getValue(String.class);
            conjunto = userSnapshot.child("conjunto").getValue(String.class);
            telefone = userSnapshot.child("telefone").getValue(String.class);

            // Busca como Long (o tipo real no banco)
            Long dataLong = userSnapshot.child("dataCadastro").getValue(Long.class);
            Long updateLong = userSnapshot.child("updateAt").getValue(Long.class);

            // Converte para String com segurança (evitando NullPointerException)
            dataCadastro = (dataLong != null) ? String.valueOf(dataLong) : "0";
            updateAt = (updateLong != null) ? String.valueOf(updateLong) : "0";

            callback.onEmailEncontrado(emailSharedPrefers, nome_usuario);
            return;
        }
    }

    private void showLoading() {
        if (loadingDialog == null) {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);

            View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
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

    interface CadastroCallback {
        void onLiberado();
        void onErro(String msg);
    }

    public interface EmailCallback {
        void onEmailEncontrado(String email, String nome);
        void onErro(String erro);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear(); // Impede memory leaks
    }

}