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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityLoginAdminBinding;
import com.example.juntoscontradengue.extras.Alertas;
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

public class ActivityLoginAdmin extends AppCompatActivity {
    private androidx.appcompat.app.AlertDialog loadingDialog;
    private  FirebaseAuth mAuth;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private boolean isPasswordVisible = false;
    private EditText edt_txt_email_admin, edt_txt_senha_admin, edt_txt_pre_cadastro;
    private ActivityLoginAdminBinding loguinAdminBinding;
    private String  emailAdmin;
    private String senhaAdmin;
    private String cpfLimpo;
    private  String nome_cadastrado, cpf_cadastrado;
    private String nome, cpf, email;
    private Long dataCadastro, updateAt;
    boolean isConnected;
    private FirebaseDatabase databaseMunicipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loguinAdminBinding = ActivityLoginAdminBinding.inflate(getLayoutInflater());
        setContentView(loguinAdminBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        String estado = prefs.getString("estado", "");
        String municipio = prefs.getString("municipio", "");

        SharedPreferences prefsUser = getSharedPreferences("UserData", MODE_PRIVATE);
        nome_cadastrado = prefsUser.getString("nome", null);

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        setupToolbar();
        initializeViews();

    }

    private void setupToolbar() {
        Toolbar toolbar = loguinAdminBinding.toolbarLoguinAdmin;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeViews() {

        edt_txt_email_admin = loguinAdminBinding.edtTxtEmailTelaLoguinAdmin;
        edt_txt_senha_admin = loguinAdminBinding.edtTxtSenhaTelaLoguinAdmin;

        edt_txt_pre_cadastro = loguinAdminBinding.edtTxtConcluirPreCadastro;

        edt_txt_pre_cadastro.addTextChangedListener(MaskEditUtil.mask(MaskEditUtil.FORMAT_CPF));


        Button btnEntrarTelaLoguinAdmin = loguinAdminBinding.btnEntrarTelaLoguinAdmin;

        btnEntrarTelaLoguinAdmin.setOnClickListener(v -> entrar_conta_admin());

        TextView txtRecuperarSenhaAdmin = loguinAdminBinding.txtRecupSenhaLoguinAdmin;
        txtRecuperarSenhaAdmin.setOnClickListener(v -> recuperarSenhaAdmin());

        TextView txtRecupEmailAdmin = loguinAdminBinding.txtRecupEmailAdmin;
        txtRecupEmailAdmin.setOnClickListener(v -> {

            // Instancia o fragmento que criamos
            RecuperarContaFragment fragment = RecuperarContaFragment.newInstance("index_email_admin");

            // Inicia a transição de tela para exibir o Fragment
            getSupportFragmentManager().beginTransaction()
                    // R.id.fragment_container deve ser o ID do container de layout na sua activity_login (ex: FrameLayout)
                    // Se você não tiver um container específico, pode usar o id do layout raiz da Activity
                    .replace(R.id.fragment_container_recup_email_admin, fragment)
                    // Adiciona na pilha para que, se o usuário clicar no botão "Voltar" do celular, ele retorne para a tela de login
                    .addToBackStack(null)
                    .commit();
        });

        Button continuar_pre_cadastro_admin = loguinAdminBinding.btnContinuarPreCadastroAdmin;
        continuar_pre_cadastro_admin.setOnClickListener(v -> pre_cadastro());

        Button cancelar_pre_cadastro_admin = loguinAdminBinding.btnCancelarPreCadastroAdmin;
        cancelar_pre_cadastro_admin.setOnClickListener(v -> sair_pre_cadastro());

        edt_txt_senha_admin.setOnTouchListener((v, event) -> {
            final android.graphics.drawable.Drawable drawableEnd = edt_txt_senha_admin.getCompoundDrawables()[2];

            if (drawableEnd != null) {
                int areaIcone = drawableEnd.getBounds().width() + edt_txt_senha_admin.getPaddingEnd();

                if (event.getRawX() >= (edt_txt_senha_admin.getRight() - areaIcone)) {
                    // Consome TODO o toque na área do ícone (DOWN e UP),
                    // não só o UP — é isso que impede o teclado de abrir
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        togglePasswordVisibility();
                    }
                    return true;
                }
            }

            return false; // fora da área do ícone: comportamento normal do campo
        });
    }

    private void pre_cadastro() {

        isConnected = NetworkUtils.isNetworkAvailable(ActivityLoginAdmin.this);
        if (!isConnected) {
            Toast.makeText(ActivityLoginAdmin.this, "Sem conexão de internet! Ative o wifi ou dados móveis!", Toast.LENGTH_SHORT).show();
            return;
        }

        String stCpfPreCadastro = edt_txt_pre_cadastro.getText().toString().trim();
        // Limpa máscara do CPF
        cpfLimpo = stCpfPreCadastro.replaceAll("[.\\-]", "");


        if (TextUtils.isEmpty(edt_txt_pre_cadastro.getText().toString().trim())) {

            edt_txt_pre_cadastro.setError("Digite um cpf");

            edt_txt_pre_cadastro.requestFocus();

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
                    Alertas.showAlertDialog(ActivityLoginAdmin.this, "Alerta", msg);
                }
            });

        }

    }

    private void continuaCadastro(String cpfLimpo) {
        showLoading();

        DatabaseReference preCadastroRef = databaseMunicipio.getReference()
                .child("config")
                .child("pre_cadastro_admins");

        // Buscar todos os pré-cadastros e filtrar
        preCadastroRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean encontrado = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String cpfNoBanco = snapshot.getKey();

                    // Comparar ignorando formatação
                    if (cpfNoBanco != null &&
                            cpfNoBanco.replaceAll("[^0-9]", "").equals(cpfLimpo)) {

                        encontrado = true;
                        hideLoading();

                        cpf_cadastrado = edt_txt_pre_cadastro.getText().toString().trim();
                        nome_cadastrado = snapshot.child("nome_pre_cadastro").getValue(String.class);

                        Intent intent = new Intent(ActivityLoginAdmin.this, TelaDeCadastro.class);
                        intent.putExtra("tipo_conta", "admin");
                        intent.putExtra("cpf", cpf_cadastrado);
                        intent.putExtra("nome", nome_cadastrado);
                        startActivity(intent);
                        break;
                    }
                }

                if (!encontrado) {
                    hideLoading();
                    AlertDialog dialog = new AlertDialog.Builder(ActivityLoginAdmin.this)
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
                Toast.makeText(ActivityLoginAdmin.this,
                        "Erro: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
    private void sair_pre_cadastro() {

        Intent intent = new Intent(ActivityLoginAdmin.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Esconder a senha
            edt_txt_senha_admin.setTransformationMethod(PasswordTransformationMethod.getInstance());
            edt_txt_senha_admin.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_closed, 0); // Ícone de olho fechado
        } else {
            // Mostrar a senha
            edt_txt_senha_admin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            edt_txt_senha_admin.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_open, 0); // Ícone de olho aberto
        }
        isPasswordVisible = !isPasswordVisible;

        // Move o cursor para o final do texto
        edt_txt_senha_admin.setSelection(edt_txt_senha_admin.getText().length());
    }

    private void recuperarSenhaAdmin() {
        isConnected = NetworkUtils.isNetworkAvailable(ActivityLoginAdmin.this);
        if (!isConnected) {
            Toast.makeText(ActivityLoginAdmin.this, "Sem conexão de internet! Ative o wifi ou dados móveis!", Toast.LENGTH_SHORT).show();
            return;
        }
        if ( TextUtils.isEmpty(edt_txt_email_admin.getText().toString()) ) {
            Toast.makeText(ActivityLoginAdmin.this, "Favor preencha o email!", Toast.LENGTH_LONG).show();
        } else {
            emailAdmin = edt_txt_email_admin.getText().toString().trim();

            mAuth.sendPasswordResetEmail(emailAdmin).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    Toast.makeText(ActivityLoginAdmin.this, "Recuperação de acesso iniciada. Foi enviado um e-mail para " + emailAdmin + " verifique a caixa de entrada do e-mail ou spam.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityLoginAdmin.this, "Erro! Tente novamente", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void entrar_conta_admin() {

        // Chamada ao método da classe NetworkUtils
        isConnected = NetworkUtils.isNetworkAvailable(ActivityLoginAdmin.this);
        if (isConnected) {
            // A conexão está disponível, prossiga com o login

            showLoading();

            emailAdmin = edt_txt_email_admin.getText().toString();
            senhaAdmin= edt_txt_senha_admin.getText().toString();

            if ( TextUtils.isEmpty(emailAdmin) || (TextUtils.isEmpty(senhaAdmin))) {
                Toast.makeText(ActivityLoginAdmin.this, "Favor preencher todos os campos!", Toast.LENGTH_LONG).show();
                hideLoading();
            } else {
                loginAdmin();

            }
        } else {
            // Exiba uma mensagem de erro ou aviso
            Toast.makeText(ActivityLoginAdmin.this, "Sem conexão de internet! Ative o wifi ou dados móveis!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginAdmin() {
        mAuth.signInWithEmailAndPassword(emailAdmin, senhaAdmin)
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
                    String emailLogado = user.getEmail() != null ? user.getEmail() : emailAdmin;

                    sincronizarPerfilAdmin(user.getUid(), emailLogado);
                });
    }

    private void sincronizarPerfilAdmin(String uid, String emailLogado) {

        databaseMunicipio.getReference()
                .child("logins").child("admins").child(uid)
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
                        updateAt     = up != null ? up : 0L;

                        // Compara o e-mail do login com o salvo no banco
                        boolean emailMudou = email == null || !email.equalsIgnoreCase(emailLogado);

                        if (emailMudou) {
                            atualizarEmailNoBanco(uid, emailLogado, cpf);
                            email    = emailLogado;

                        }

                        concluirLogin();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        falhaAposAutenticar("Erro ao carregar seu perfil: " + error.getMessage());
                    }
                });
    }

    private void atualizarEmailNoBanco(String uid, String novoEmail, String cpfDoAdmin) {

        Map<String, Object> updates = new HashMap<>();

        // 1) logins/admins/{uid}
        String base = "logins/admins/" + uid + "/";
        updates.put(base + "email", novoEmail);
        updates.put(base + "novoEmail", null);              // limpa pendência, se existir
        updates.put(base + "updatedAt", ServerValue.TIMESTAMP);

        // 2) index_email/{cpf} (chave sem máscara)
        if (!TextUtils.isEmpty(cpfDoAdmin)) {
            String cpfChave = cpfDoAdmin.replaceAll("[^0-9]", "");
            if (!cpfChave.isEmpty()) {
                updates.put("index_email_admin/" + cpfChave + "/email", novoEmail);
                updates.put("index_email_admin/" + cpfChave + "/novo_email", null);
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

        String saudacao = "Bem vindo, " + (!TextUtils.isEmpty(nome) ? nome : "Administrador");
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
        editor.putString("email", email);
        editor.putLong("dataCadastro", dataCadastro);
        editor.putLong("updateAt", updateAt);
        editor.putString("perfil", "admins");
        editor.apply();

        TopicHelper.inscreverNoTopicoDoPerfil(this, "admins");
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