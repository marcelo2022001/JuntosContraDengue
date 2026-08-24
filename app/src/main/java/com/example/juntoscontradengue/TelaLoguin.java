package com.example.juntoscontradengue;

import android.annotation.SuppressLint;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityTelaLoguinBinding;
import com.example.juntoscontradengue.extras.AppConfig;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.ValidaCpf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TelaLoguin extends AppCompatActivity {

    private androidx.appcompat.app.AlertDialog loadingDialog;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;
    private EditText cpfUser, senhaAuth;
    Button btnEntrarTelaLoguin;
    TextView txtRecupSenhaUsuario, criar_conta;
    ProgressBar progressBar;
    private ActivityTelaLoguinBinding loguinBinding;
    String estado, municipio, nomeShared, mensagem, emailPendente;

    //shared prefers dados usuario estiver null
    String nome, email,endereco, cpf, cpf_prefes, num_casa, conjunto, dataCadastro, telefone, updateAt;

    // ✅ Construtor padrão obrigatório para Activities
    public TelaLoguin() {
        // O Android precisa deste construtor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loguinBinding = ActivityTelaLoguinBinding.inflate(getLayoutInflater());
        setContentView(loguinBinding.getRoot());

        Toolbar toolbar = loguinBinding.toolbarLoguin;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sem conexão de internet!", Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth = FirebaseAuth.getInstance();

        initializeViews();

        // ✅ CONFIGURA O CALLBACK PARA O BOTÃO VOLTAR (MODERNO)
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToMainActivity();
            }
        };

        // Adiciona o callback ao dispatcher
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    // ✅ PARA A SETA DE VOLTAR DA TOOLBAR
    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToMainActivity();
        return true;
    }

    // ✅ MÉTODO PARA NAVEGAR DE VOLTA
    private void navigateBackToMainActivity() {
        Intent intent = new Intent(TelaLoguin.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeViews() {
        estado = AppConfig.getEstado(this);
        municipio = AppConfig.getMunicipio(this);

        SharedPreferences prefsUser = getSharedPreferences("UserData", MODE_PRIVATE);
        cpf_prefes = prefsUser.getString("cpf", null);
        nomeShared = prefsUser.getString("nome", null);

        cpfUser = loguinBinding.edtTxtCpfTelaLoguin;
        cpfUser.addTextChangedListener(MaskEditUtil.mask(MaskEditUtil.FORMAT_CPF));
        senhaAuth = loguinBinding.edtTxtSenhaTelaLoguin;

        btnEntrarTelaLoguin = loguinBinding.btnEntrarTelaLoguin;
        btnEntrarTelaLoguin.setOnClickListener(v -> autenticarPorCpf(AuthAction.LOGIN));

        txtRecupSenhaUsuario = loguinBinding.txtRecupSenhaUsuario;
        txtRecupSenhaUsuario.setOnClickListener(v -> autenticarPorCpf(AuthAction.RECOVER_PASSWORD));

        criar_conta = loguinBinding.txtCriarContaUsuario;
        criar_conta.setOnClickListener(v -> criar_conta_usuario());

        progressBar = loguinBinding.pgLoadingUsuario;

        senhaAuth.setOnTouchListener((v, event) -> {
            final android.graphics.drawable.Drawable drawableEnd = senhaAuth.getCompoundDrawables()[2];

            if (drawableEnd != null) {
                int areaIcone = drawableEnd.getBounds().width() + senhaAuth.getPaddingEnd();

                if (event.getRawX() >= (senhaAuth.getRight() - areaIcone)) {
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

    private void criar_conta_usuario() {
        Intent intent = new Intent(this, TelaDeCadastro.class);
        intent.putExtra("tipo_conta", "usuarios");
        startActivity(intent);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            senhaAuth.setTransformationMethod(PasswordTransformationMethod.getInstance());
            senhaAuth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_closed, 0);
        } else {
            senhaAuth.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            senhaAuth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_open, 0);
        }
        isPasswordVisible = !isPasswordVisible;
        senhaAuth.setSelection(senhaAuth.getText().length());
    }

    private void recuperarSenhaComEmail(String emailDestino) {
        mAuth.sendPasswordResetEmail(emailDestino)
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "E-mail de recuperação enviado para:\n" + emailDestino, Toast.LENGTH_LONG).show();
                    } else {
                        tratarErroFirebase(task.getException());
                    }
                });
    }

    private void tratarErroFirebase(Exception exception) {
        if (exception == null) {
            Toast.makeText(this, "Erro desconhecido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Credenciais inválidas", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authEx = (FirebaseAuthException) exception;
            if ("ERROR_TOO_MANY_REQUESTS".equals(authEx.getErrorCode())) {
                Toast.makeText(this, "Muitas tentativas. Tente novamente mais tarde.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Erro de autenticação: " + authEx.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Erro inesperado: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void autenticarPorCpf(AuthAction action) {

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sem conexão de internet!", Toast.LENGTH_SHORT).show();
            return;
        }

        cpf = removerPontuacaoCPF(cpfUser.getText().toString().trim());
        String senha = senhaAuth.getText().toString().trim();

        if (TextUtils.isEmpty(cpf)) {
            Toast.makeText(this, "Informe o CPF", Toast.LENGTH_SHORT).show();
            return;
        }

        if (action == AuthAction.LOGIN && TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Informe a senha", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidaCpf.validaCPF(cpf)) {
            cpfUser.setError("CPF inválido");
            return;
        }

        showLoading();

        buscarEmailPorCpf(cpf, new EmailCallback() {
            @Override
            public void onEmailEncontrado(String emailEncontrado, String nomeEncontrado) {
                if (action == AuthAction.LOGIN) {
                    loginComEmail(emailEncontrado, senha);
                } else {
                    recuperarSenhaComEmail(emailEncontrado);
                }
            }

            @Override
            public void onErro(String erro) {
                hideLoading();
                Log.d("ERRO LOGIN", erro);
                Toast.makeText(TelaLoguin.this, erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginComEmail(String emailLogin, String senha) {
        mAuth.signInWithEmailAndPassword(emailLogin, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        salvarDadosLocalmente();
                        irParaActivityPrincipal(nome);
                    } else {
                        // Se falhou o login com o email principal, tenta autenticar usando o novoEmail pendente (se houver)
                        if (emailPendente != null && !emailPendente.isEmpty() && !emailPendente.equals(emailLogin)) {
                            mAuth.signInWithEmailAndPassword(emailPendente, senha)
                                    .addOnCompleteListener(taskNovo -> {
                                        hideLoading();
                                        if (taskNovo.isSuccessful()) {
                                            // Atualiza o banco principal efetivando a troca de email e limpando o novoEmail
                                            atualizarEmailNoBancoEEfetivar(emailPendente);
                                        } else {
                                            tratarErroFirebase(taskNovo.getException());
                                        }
                                    });
                        } else {
                            hideLoading();
                            tratarErroFirebase(task.getException());
                        }
                    }
                });
    }

    private void atualizarEmailNoBancoEEfetivar(String novoEmailEfetivado) {
        DatabaseReference refCpfIndex = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("cpf_index");

        Query query = refCpfIndex.orderByChild("cpf").equalTo(cpf);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String uidEncontrado = userSnapshot.getKey();
                    if (uidEncontrado != null) {
                        Map<String, Object> atualizacao = new HashMap<>();
                        atualizacao.put("email", novoEmailEfetivado);
                        atualizacao.put("novoEmail", null);

                        refCpfIndex.child(uidEncontrado).updateChildren(atualizacao);

                        // Atualiza a variável local e salva nas preferências
                        email = novoEmailEfetivado;
                        salvarDadosLocalmente();
                    }
                }
                irParaActivityPrincipal(nome);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                irParaActivityPrincipal(nome);
            }
        });
    }

    private void irParaActivityPrincipal(String nomeOpcional) {
        hideLoading();
        mensagem = "Bem vindo, ";

        if (nomeOpcional != null && !nomeOpcional.isEmpty()) {
            mensagem += nomeOpcional;
        } else if (nomeShared != null && !nomeShared.isEmpty()) {
            mensagem += nomeShared;
        } else {
            mensagem += "Usuário";
        }

        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
        editor.putString("perfil", "usuarios");
        editor.apply();
    }

    private void buscarEmailPorCpf(String cpf_user, EmailCallback callback) {
        DatabaseReference refCpfIndex = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("cpf_index")
                .child(cpf_user); // Acessa diretamente a chave do CPF

        refCpfIndex.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // O valor armazenado diretamente é o UID
                    String uidEncontrado = snapshot.getValue(String.class);
                    if (uidEncontrado != null) {
                        buscarDadosUsuarioPorUid(uidEncontrado, callback);
                    } else {
                        hideLoading();
                        callback.onErro("UID não encontrado para este CPF.");
                    }
                } else {
                    hideLoading();
                    callback.onErro("Usuário não encontrado na base de dados.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                callback.onErro(error.getMessage());
            }
        });
    }

    private void buscarDadosUsuarioPorUid(String uidEncontrado, EmailCallback callback) {
        DatabaseReference refUsuario = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("logins")
                .child("usuarios")
                .child(uidEncontrado);

        refUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    cpf = userSnapshot.child("cpf").getValue(String.class);
                    nome = userSnapshot.child("nome").getValue(String.class);
                    email = userSnapshot.child("email").getValue(String.class);
                    endereco = userSnapshot.child("endereco").getValue(String.class);
                    num_casa = userSnapshot.child("num_casa").getValue(String.class);
                    conjunto = userSnapshot.child("conjunto").getValue(String.class);
                    telefone = userSnapshot.child("telefone").getValue(String.class);
                    emailPendente = userSnapshot.child("novoEmail").getValue(String.class);

                    Long dataCadastroLong = userSnapshot.child("dataCadastro").getValue(Long.class);
                    dataCadastro = (dataCadastroLong != null) ? String.valueOf(dataCadastroLong) : null;

                    Long updateAtLong = userSnapshot.child("updateAt").getValue(Long.class);
                    updateAt = (updateAtLong != null) ? String.valueOf(updateAtLong) : null;

                    callback.onEmailEncontrado(email, nome);
                } else {
                    hideLoading();
                    callback.onErro("Dados do usuário não encontrados.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                callback.onErro(error.getMessage());
            }
        });
    }

    private String removerPontuacaoCPF(String cpfStr) {
        if (cpfStr == null) return "";
        return cpfStr.replaceAll("[.-]", "");
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

    public interface EmailCallback {
        void onEmailEncontrado(String email, String nome);
        void onErro(String erro);
    }

    public enum AuthAction {
        LOGIN,
        RECOVER_PASSWORD
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}