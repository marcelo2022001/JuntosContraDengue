package com.example.juntoscontradengue;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
import com.example.juntoscontradengue.extras.TopicHelper;
import com.example.juntoscontradengue.extras.ValidaCpf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
    private String cpf, cpfLimpo, nome, email;
    private Long dataCadastro, updatedAt;
    private String  emailSharedPrefers;
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

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
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

        TextView txtRecupEmailAgentes = loguinAgentesBinding.txtRecupEmailAgentes;
        txtRecupEmailAgentes.setOnClickListener(v -> {

            // Instancia o fragmento que criamos
            RecuperarContaFragment fragment = RecuperarContaFragment.newInstance("index_email_agentes");


            // Inicia a transição de tela para exibir o Fragment
            getSupportFragmentManager().beginTransaction()
                    // R.id.fragment_container deve ser o ID do container de layout na sua activity_login (ex: FrameLayout)
                    // Se você não tiver um container específico, pode usar o id do layout raiz da Activity
                    .replace(R.id.fragment_container_recup_email_agentes, fragment)
                    // Adiciona na pilha para que, se o usuário clicar no botão "Voltar" do celular, ele retorne para a tela de login
                    .addToBackStack(null)
                    .commit();
        });

       Button continuar_pre_cadastro_agentes = loguinAgentesBinding.btnContinuarPreCadastroAgentes;
       continuar_pre_cadastro_agentes.setOnClickListener(v -> pre_cadastro());

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

                    if (!task.isSuccessful()) {
                        hideLoading();
                        tratarErroLogin(task.getException());
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        hideLoading();
                        Toast.makeText(this, "Erro ao obter os dados do usuário.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // E-mail realmente usado/validado pelo Auth (já normalizado)
                    String emailLogado = user.getEmail() != null ? user.getEmail() : emailAgente;

                    sincronizarPerfilAdmin(user.getUid(), emailLogado);
                });
    }

    private void sincronizarPerfilAdmin(String uid, String emailLogado) {

        databaseMunicipio.getReference()
                .child("logins").child("Agentes").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            falhaAposAutenticar("Esta conta não possui perfil de administrador.");
                            return;
                        }

                        nome      = snapshot.child("nome").getValue(String.class);
                        cpf       = snapshot.child("cpf").getValue(String.class);
                        email     = snapshot.child("email").getValue(String.class);
                        Long dc = snapshot.child("dataCadastro").getValue(Long.class);
                        Long up = snapshot.child("updatedAt").getValue(Long.class);
                        dataCadastro = dc != null ? dc : 0L;
                        updatedAt     = up != null ? up : 0L;

                        // Compara o e-mail do login com o salvo no banco
                        boolean emailMudou = email == null || !email.equalsIgnoreCase(emailLogado);

                        if (emailMudou) {
                            atualizarEmailNoBanco(uid, emailLogado, cpf);
                            email    = emailLogado;
                            updatedAt = System.currentTimeMillis();
                        }

                        concluirLogin();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        falhaAposAutenticar("Erro ao carregar seu perfil: " + error.getMessage());
                    }
                });
    }

    private void atualizarEmailNoBanco(String uid, String novoEmail, String cpfDoAgente) {

        Map<String, Object> updates = new HashMap<>();

        // 1) logins/agentes/{uid}
        String base = "logins/agentes/" + uid + "/";
        updates.put(base + "email", novoEmail);
        updates.put(base + "novoEmail", null);              // limpa pendência, se existir
        updates.put(base + "updatedAt", ServerValue.TIMESTAMP);

        // 2) index_email/{cpf} (chave sem máscara)
        if (!TextUtils.isEmpty(cpfDoAgente)) {
            String cpfChave = cpfDoAgente.replaceAll("[^0-9]", "");
            if (!cpfChave.isEmpty()) {
                updates.put("index_email_agentes/" + cpfChave + "/email", novoEmail);
                updates.put("index_email_agentes/" + cpfChave + "/novo_email", null);
            }
        }

        // Update multi-path a partir da raiz: grava tudo junto ou nada.
        // Se falhar, não trava o login: no próximo login o e-mail ainda vai diferir e tenta de novo.
        databaseMunicipio.getReference().updateChildren(updates)
                .addOnFailureListener(e ->
                        Log.e("LoginAdmin", "Falha ao sincronizar e-mail: " + e.getMessage()));
    }

    private void concluirLogin() {
        salvarDadosLocalmente();
        hideLoading();

        String saudacao = "Bem vindo, " + (!TextUtils.isEmpty(nome) ? nome : "Agente");
        Toast.makeText(this, saudacao, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, AdminActivity.class));
    }

    private void falhaAposAutenticar(String msg) {
        hideLoading();
        mAuth.signOut();   // não deixa uma sessão do Auth aberta sem perfil admin válido
        Alertas.showAlertDialog(this, "Falha no login", msg);
    }

    private void tratarErroLogin(Exception e) {
        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Alertas.showAlertDialog(this, "Falha no login", "E-mail ou senha incorretos.");
        } else if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            Alertas.showAlertDialog(this, "Conta não encontrada",
                    "Não existe uma conta cadastrada com este e-mail.");
        } else {
            Alertas.showAlertDialog(this, "Erro",
                    e != null ? e.getMessage() : "Não foi possível realizar o login.");
        }
    }

    private void salvarDadosLocalmente() {

        SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nome", nome);
        editor.putString("cpf", cpf);
        editor.putString("email", emailSharedPrefers);
        editor.putLong("dataCadastro", dataCadastro);
        editor.putLong("updateAt", updatedAt);
        editor.putString("perfil", "agentes");
        editor.apply();
        TopicHelper.inscreverNoTopicoDoPerfil(this, "agentes");

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear(); // Impede memory leaks
    }

}