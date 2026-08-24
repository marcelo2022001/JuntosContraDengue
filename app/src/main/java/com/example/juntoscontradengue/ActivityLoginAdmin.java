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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityLoginAdminBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.ValidaCpf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ActivityLoginAdmin extends AppCompatActivity {
    private androidx.appcompat.app.AlertDialog loadingDialog;
    private  FirebaseAuth mAuth;
    private  DatabaseReference usersRef;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private boolean isPasswordVisible = false;
    private EditText edt_txt_email_admin, edt_txt_senha_admin, edt_txt_pre_cadastro;
    private ActivityLoginAdminBinding loguinAdminBinding;
    private String  emailAdmin, senhaAdmin,  cpfLimpo, estado, municipio;
    private  String nome_cadastrado, cpf_cadastrado, emailShared;
    private String nome, cpf, email, endereco, num_casa, conjunto, telefone, dataCadastro, updateAt;
    boolean isConnected;
     String MENSAGEM = "Bem vindo, ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loguinAdminBinding = ActivityLoginAdminBinding.inflate(getLayoutInflater());
        setContentView(loguinAdminBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        usersRef = com.google.firebase.database.FirebaseDatabase
                .getInstance()
                .getReference("cadastros");

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", "");
        municipio = prefs.getString("municipio", "");

        SharedPreferences prefsUser = getSharedPreferences("UserData", MODE_PRIVATE);
        emailShared = prefsUser.getString("email", null);
        nome_cadastrado = prefsUser.getString("nome", null);

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

        TextView txtRecuperarSenha = loguinAdminBinding.txtRecupSenhaLoguinAdmin;
        txtRecuperarSenha.setOnClickListener(v -> recuperarSenhaAdmin());

        Button continuar_pre_cadastro = loguinAdminBinding.btnContinuarPreCadastro;
        continuar_pre_cadastro.setOnClickListener(v -> pre_cadastro());

        Button cancelar_pre_cadastro = loguinAdminBinding.btnCancelarPreCadastro;
        cancelar_pre_cadastro.setOnClickListener(v -> sair_pre_cadastro());

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

        DatabaseReference preCadastroRef = usersRef.child(Objects.requireNonNull(estado))
                .child(Objects.requireNonNull(municipio))
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
        usersRef.child(Objects.requireNonNull(estado))
                .child(Objects.requireNonNull(municipio))
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

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            hideLoading();
                            Toast.makeText(this,
                                    "Erro ao obter os dados do usuário.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!emailAdmin.equals(emailShared)) {

                            buscaDadosUsuario(emailAdmin, new EmailCallback() {
                                @Override
                                public void onEmailEncontrado(String emailResult, String nomeResult) {
                                    salvarDadosLocalmente();
                                    irParaActivityPrincipal(nomeResult);
                                }

                                @Override
                                public void onErro(String erro) {
                                    hideLoading();
                                    Toast.makeText(ActivityLoginAdmin.this,
                                            erro,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {

                            if (nome_cadastrado != null) {
                                MENSAGEM += nome_cadastrado;
                            } else {
                                MENSAGEM += "Administrador";
                            }

                            salvarDadosLocalmente();
                            hideLoading();
                            Toast.makeText(this, MENSAGEM, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, AdminActivity.class));
                        }

                    } else {

                        hideLoading();

                        Exception e = task.getException();

                        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {

                            Alertas.showAlertDialog(
                                    ActivityLoginAdmin.this,
                                    "Falha no login",
                                    "E-mail ou senha incorretos."
                            );

                        } else if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {

                            Alertas.showAlertDialog(
                                    ActivityLoginAdmin.this,
                                    "Conta não encontrada",
                                    "Não existe uma conta cadastrada com este e-mail."
                            );

                        } else {

                            Alertas.showAlertDialog(
                                    ActivityLoginAdmin.this,
                                    "Erro",
                                    e != null ? e.getMessage() : "Não foi possível realizar o login."
                            );
                        }
                    }
                });
    }

    private void buscaDadosUsuario(String emailAdmin, EmailCallback emailCallback) {

            usersRef = FirebaseDatabase.getInstance()
                    .getReference("cadastros")
                    .child(estado)
                    .child(municipio)
                    .child("logins")
                    .child("admins");

            // 1ª Tentativa: Busca pelo campo "email" tradicional
            Query queryEmail = usersRef.orderByChild("email").equalTo(emailAdmin);

            queryEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        processarDadosUsuario(snapshot, emailCallback);
                    } else {
                        // 2ª Tentativa: Se não achou pelo email, busca pelo "novoEmail"
                        Query queryNovoEmail = usersRef.orderByChild("novoEmail").equalTo(emailAdmin);

                        queryNovoEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshotNovo) {
                                if (snapshotNovo.exists()) {
                                    // Se encontrou pelo novo email, atualiza o banco principal para efetivar a troca
                                    for (DataSnapshot userSnapshot : snapshotNovo.getChildren()) {
                                        String uidEncontrado = userSnapshot.getKey();
                                        if (uidEncontrado != null) {

                                            // 1. Efetiva a troca no banco: o email principal vira o emailBuscado e limpa o pendente (novoEmail = null)
                                            Map<String, Object> atualizacao = new HashMap<>();
                                            atualizacao.put("email", emailAdmin);
                                            atualizacao.put("novoEmail", null);

                                            // Aplica a atualização diretamente no nó do usuário encontrado
                                            usersRef.child(uidEncontrado).updateChildren(atualizacao);

                                            // 2. Atualiza localmente no SharedPreferences o novo email
                                            SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("email", emailAdmin);
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


    private void irParaActivityPrincipal(String nomeOpcional) {
        hideLoading();

        // Tenta pegar o nome que veio do banco ou dos Extras
        if (nomeOpcional != null && !nomeOpcional.isEmpty()) {
            MENSAGEM += nomeOpcional;
        } else {
            if (nome_cadastrado != null) {
                MENSAGEM += nome_cadastrado;
            } else {
                MENSAGEM += "Administrador";
            }
        }
        Toast.makeText(this, MENSAGEM, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, AdminActivity.class));

    }

    private void salvarDadosLocalmente() {
        SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nome", nome);
        editor.putString("cpf", cpf);
        editor.putString("email", email);
        editor.putString("endereco", endereco);
        editor.putString("num_casa", num_casa);
        editor.putString("conjunto", conjunto);
        editor.putString("telefone", telefone);
        editor.putString("dataCadastro", dataCadastro);
        editor.putString("updateAt", updateAt);
        editor.putString("perfil", "admins");
        editor.apply();
    }



    // Método auxiliar para extrair os dados e evitar repetição de código
    private void processarDadosUsuario(DataSnapshot snapshot, ActivityLoginAdmin.EmailCallback callback) {
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            nome = userSnapshot.child("nome").getValue(String.class);
            cpf = userSnapshot.child("cpf").getValue(String.class);
            email = userSnapshot.child("email").getValue(String.class);
            endereco = userSnapshot.child("endereco").getValue(String.class);
            num_casa = userSnapshot.child("num_casa").getValue(String.class);
            conjunto = userSnapshot.child("conjunto").getValue(String.class);
            telefone = userSnapshot.child("telefone").getValue(String.class);

            Long dataLong = userSnapshot.child("dataCadastro").getValue(Long.class);
            Long updateLong = userSnapshot.child("updateAt").getValue(Long.class);

            dataCadastro = (dataLong != null) ? String.valueOf(dataLong) : "0";
            updateAt = (updateLong != null) ? String.valueOf(updateLong) : "0";

            callback.onEmailEncontrado(email, nome);
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