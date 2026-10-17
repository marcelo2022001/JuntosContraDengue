package com.example.juntoscontradengue;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityTelaDeCadastroBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.AppConfig;
import com.example.juntoscontradengue.extras.EmailValidator;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.TelefoneUtil;
import com.example.juntoscontradengue.extras.TopicHelper;
import com.example.juntoscontradengue.extras.ValidaCpf;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TelaDeCadastro extends AppCompatActivity {
    private androidx.appcompat.app.AlertDialog loadingDialog;
    private boolean isPasswordVisible = false;
    private EditText nomeCad, cpfCad, emailCad, confEmailCad, telefoneCad;
    private EditText edtSenha, confSenhaCad, enderecoCad, num_casaCad, conjuntoCad;
    TextView termos;
    CheckBox checkBoxCad;
    private Button btnCad;
    private FirebaseAuth auth;
    private String nome;
    private String cpfLimpo;
    private String endereco;
    private String num_casa;
    private String conjunto;
    private String email;
    private String telLimpo;
    private String senha;
    private String uuid;
    private FirebaseDatabase databaseMunicipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //implements CodeVerificationFragment.CodeVerificationListener {
        com.example.juntoscontradengue.databinding.ActivityTelaDeCadastroBinding activityTelaDeCadastroBinding = ActivityTelaDeCadastroBinding.inflate(getLayoutInflater());
        setContentView(activityTelaDeCadastroBinding.getRoot());

        // Inicialize o FirebaseAuth aqui
        auth = FirebaseAuth.getInstance();

        String estado = AppConfig.getEstado(this);
        String municipio = AppConfig.getMunicipio(this);

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        initializeComponents(activityTelaDeCadastroBinding);
        checkNetworkConnection();
        setupUI(activityTelaDeCadastroBinding);

    }

    private void initializeComponents(ActivityTelaDeCadastroBinding binding) {
        Toolbar toolbar = binding.toolbarCad;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void checkNetworkConnection() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);
        if (!isConnected) {
            Intent intent = new Intent(this, SemInternetActivity.class);
            intent.putExtra("Id_Activity", "Activity_cad");
            startActivity(intent);
        }
    }

    // Adicione esta linha logo acima do método setupUI
    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private void setupUI(ActivityTelaDeCadastroBinding binding) {

        nomeCad = binding.edtNomeCad;
        cpfCad = binding.edtCpfCad;
        enderecoCad = binding.edtEnderecoCad;
        num_casaCad = binding.edtNumCasaCad;
        conjuntoCad = binding.edtConjuntoCad;
        telefoneCad = binding.edtTelefoneCad;
        emailCad = binding.edtEmailCad;
        confEmailCad = binding.edtConfirmeEmailCad;
        edtSenha = binding.edtTextSenha;
        confSenhaCad = binding.edtTextConfirmarSenha;
        checkBoxCad = binding.checkBoxCad;
        termos = binding.lerTermos;
        btnCad = binding.btnCriarConta;

        setupCheckBoxListener();

        cpfCad.addTextChangedListener(MaskEditUtil.mask(MaskEditUtil.FORMAT_CPF));
        telefoneCad.addTextChangedListener(MaskEditUtil.maskTelefone());

        termos.setOnClickListener(v -> startActivity(new Intent(TelaDeCadastro.this, TermosDeUsoActivity.class)));

        btnCad.setOnClickListener(view -> {
            if (!checkBoxCad.isChecked()) {
                Toast.makeText(TelaDeCadastro.this,
                        "Você precisa aceitar os termos de uso para continuar.",
                        Toast.LENGTH_LONG).show();
                btnCad.setEnabled(false); // garante consistência
                return;
            }

                criar_conta_email_senha();
        });

        edtSenha.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtSenha.getRight() - edtSenha.getCompoundDrawables()[2].getBounds().width())) {
                    senhaTogglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

        confSenhaCad.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (confSenhaCad.getRight() - confSenhaCad.getCompoundDrawables()[2].getBounds().width())) {
                    confSenhaTogglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

    }

    private void setupCheckBoxListener() {
        checkBoxCad.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                btnCad.setEnabled(false);

                nomeCad.setEnabled(true);
                cpfCad.setEnabled(true);
                enderecoCad.setEnabled(true);
                num_casaCad.setEnabled(true);
                conjuntoCad.setEnabled(true);
                telefoneCad.setEnabled(true);
                emailCad.setEnabled(true);
                confEmailCad.setEnabled(true);
                edtSenha.setEnabled(true);
                confSenhaCad.setEnabled(true);

                return;
            }
            // Se marcou, valida tudo. Só ativa o botão ao final, se tudo OK.
            btnCad.setEnabled(false); // garante desativado durante a validação
            verificarCampo();
        });
    }

    private void senhaTogglePasswordVisibility() {
        if (isPasswordVisible) {
            edtSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
            edtSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_closed, 0);
        } else {
            edtSenha.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            edtSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_open, 0);
        }
        isPasswordVisible = !isPasswordVisible;
        edtSenha.setSelection(edtSenha.getText().length());
    }

    private void confSenhaTogglePasswordVisibility() {
        if (isPasswordVisible) {
            confSenhaCad.setTransformationMethod(PasswordTransformationMethod.getInstance());
            confSenhaCad.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_closed, 0);
        } else {
            confSenhaCad.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            confSenhaCad.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_open, 0);
        }
        isPasswordVisible = !isPasswordVisible;
        confSenhaCad.setSelection(confSenhaCad.getText().length());
    }

    private void verificarCampo() {

        String nomeUsuario = nomeCad.getText().toString().trim();
        String cpfUsuario = cpfCad.getText().toString().trim();
        email = emailCad.getText().toString().trim();
        String confEmail = confEmailCad.getText().toString().trim();
        String telefone = telefoneCad.getText().toString().trim();
        senha = edtSenha.getText().toString().trim();
        String confSenha = confSenhaCad.getText().toString();
        endereco = enderecoCad.getText().toString().trim();
        num_casa = num_casaCad.getText().toString().trim();
        conjunto = conjuntoCad.getText().toString().trim();

        // ✅ Sempre recalcula
        cpfLimpo = cpfUsuario.replaceAll("[^0-9]", "");
        telLimpo = telefoneCad.getText().toString().trim().replaceAll("[^0-9]", "");


            if (nomeUsuario.isEmpty() || cpfUsuario.isEmpty() || email.isEmpty() || confEmail.isEmpty() || telefone.isEmpty() || senha.isEmpty() || confSenha.isEmpty()
                    || endereco.isEmpty() || num_casa.isEmpty() || conjunto.isEmpty()) {

                Toast.makeText(TelaDeCadastro.this, "Preencha todos os campos!", Toast.LENGTH_LONG).show();
                checkBoxCad.setChecked(false);
                return;
            } else {
                nome = nomeUsuario;
                cpfLimpo = cpfUsuario.replaceAll("[^0-9]", "");
                telLimpo = telefone.replaceAll("[^0-9]", "");
            }



        if (!ValidaCpf.validaCPF(cpfLimpo)) {
            Toast.makeText(TelaDeCadastro.this, "Digite um número de CPF válido!", Toast.LENGTH_LONG).show();
            checkBoxCad.setChecked(false);

        } else if (!EmailValidator.getInstance().validate(email)) {
            Toast.makeText(TelaDeCadastro.this, "Digite um email válido!", Toast.LENGTH_LONG).show();
            checkBoxCad.setChecked(false);

        } else if (!email.equals(confEmail)) {
            Toast.makeText(TelaDeCadastro.this, "Os emails não conferem!", Toast.LENGTH_LONG).show();
            checkBoxCad.setChecked(false);
        } else if (!senha.equals(confSenha)) {
            Toast.makeText(TelaDeCadastro.this, "As senhas não conferem!", Toast.LENGTH_LONG).show();
            checkBoxCad.setChecked(false);
        } else if (senha.length() < 6) {
            Toast.makeText(TelaDeCadastro.this, "A senha deve ter 6 ou mais caracteres!", Toast.LENGTH_LONG).show();
            checkBoxCad.setChecked(false);
        } else {

            verificarCpfAntesCadastro(new CadastroCallback() {
                @Override
                public void onLiberado() {
                    verificarFoneAntesCadastro();
                }

                @Override
                public void onErro(String msg) {
                    Alertas.showAlertDialog(TelaDeCadastro.this, "Alerta", msg);
                }
            });

        }
    }

    private void verificarFoneAntesCadastro() {

         if (!TelefoneUtil.telefoneValido(telefoneCad.getText().toString())) {
             Toast.makeText(this, "Telefone inválido", Toast.LENGTH_SHORT).show();
             checkBoxCad.setChecked(false);
             return;
         }

        verificarTelefoneAntesCadastro(new CadastroCallback() {
            @Override
            public void onLiberado() {
                Log.e("Liberado Fone", "Fone OK");

                btnCad.setEnabled(true);
                nomeCad.setEnabled(false);
                cpfCad.setEnabled(false);
                enderecoCad.setEnabled(false);
                num_casaCad.setEnabled(false);
                conjuntoCad.setEnabled(false);
                telefoneCad.setEnabled(false);
                emailCad.setEnabled(false);
                confEmailCad.setEnabled(false);
                edtSenha.setEnabled(false);
                confSenhaCad.setEnabled(false);

            }

            @Override
            public void onErro(String msg) {
                Alertas.showAlertDialog(TelaDeCadastro.this, "Alerta", msg, (dialog, which) -> {
                    checkBoxCad.setChecked(false);
                    btnCad.setEnabled(false);
                });
            }
        });
    }

    private void verificarTelefoneAntesCadastro(CadastroCallback cadastroCallback) {

        DatabaseReference baseRefFone = databaseMunicipio.getReference("telefone_index");

        baseRefFone.child(telLimpo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {

                        if (snap.exists()) {
                            checkBoxCad.setChecked(false); // Mantem checkbox deselecionado até conferir todos os campos

                            cadastroCallback.onErro("Este telefone já está cadastrado.");
                        } else {
                            cadastroCallback.onLiberado();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cadastroCallback.onErro(error.getMessage());
                    }
                });

    }

    private void verificarCpfAntesCadastro(CadastroCallback callback) {

        DatabaseReference baseRef = databaseMunicipio.getReference("cpf_index");

        baseRef.child(cpfLimpo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            checkBoxCad.setChecked(false); // Mantem checkbox deselecionado até conferir todos os campos

                            callback.onErro("Este CPF já está cadastrado.");

                        } else {
                            // ADICIONADO: Se o CPF não existe, libera para verificar o telefone
                            callback.onLiberado();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onErro(error.getMessage());
                    }
                });
    }

    private void criar_conta_email_senha() {

        showLoading();

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the authenticated user from the task result
                        FirebaseUser user = task.getResult().getUser();

                        // Check if the user object is not null
                        if (user != null) {
                            // Retrieve the UUID (UID in Firebase terms)
                            uuid = user.getUid();

                            salvarDadosUsuario();
                        }
                    } else {
                        handleRegistrationError(task);
                    }
                });
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

    private void salvarDadosUsuario() {

        DatabaseReference userRef =  databaseMunicipio.getReference("logins/usuarios")
                .child(uuid); // Já referencia o nó

        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome);
        dados.put("cpf", cpfLimpo);
        dados.put("email", email);
         dados.put("endereco", endereco);
        dados.put("num_casa", num_casa);
        dados.put("conjunto", conjunto);
        dados.put("telefone", telLimpo);
        dados.put("uuid", uuid);

        userRef.setValue(dados)
                .addOnSuccessListener(aVoid -> {

// 1. Crie um mapa com os caminhos relativos à raiz do banco
                    Map<String, Object> atualizacoes = new HashMap<>();
                    atualizacoes.put("cpf_index/" + cpfLimpo, uuid);

                    atualizacoes.put("telefone_index/" + telLimpo, uuid);


// 2. Grava os dois índices numa única operação atômica
                    databaseMunicipio.getReference().updateChildren(atualizacoes)
                    .addOnSuccessListener(v -> Log.d("Firebase", "Índices gravados"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Falha ao gravar índices", e));

                    TopicHelper.inscreverNoTopicoDoPerfil(this, "usuarios");


                    salvarSharedPrefsUsuarios();
                    hideLoading();
                    limparCampos();

                        Alertas.showSuccessDialog(TelaDeCadastro.this, "Sucesso", "Cadastro realizado com sucesso! Faça login para entrar na conta.", (dialog, which) -> {
                            // Roda APENAS quando clica em OK:
                            // 1) encerra a sessão criada no cadastro
                            auth.signOut();

                            // 2) monta a pilha: Main (base) -> Login (topo)
                            Intent main  = new Intent(this, MainActivity.class);
                            Intent login = new Intent(this, TelaLoguin.class);
                            TaskStackBuilder.create(this)
                                    .addNextIntent(main)
                                    .addNextIntent(login)
                                    .startActivities();

                            finish(); // Fecha a tela de cadastro
                        });


                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firebase", "Erro ao salvar", e);
                    checkBoxCad.setChecked(false);
                    finish();
                });
    }

    private void salvarSharedPrefsUsuarios() {
        // Salvar no SharedPreferences (AGORA, SIM, com dados carregados)
        SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nome", nome);
        editor.putString("cpf", cpfLimpo);
        editor.putString("email", email);
        editor.putString("endereco", endereco);
        editor.putString("num_casa", num_casa);
        editor.putString("conjunto", conjunto);
        editor.putString("telefone", telLimpo);

        editor.apply();
    }

    private void limparCampos() {

        nomeCad.setText("");
        cpfCad.setText("");
        enderecoCad.setText("");
        num_casaCad.setText("");
        conjuntoCad.setText("");
        telefoneCad.setText("");
        emailCad.setText("");
        confEmailCad.setText("");
        edtSenha.setText("");
        confSenhaCad.setText("");
    }

    private void handleRegistrationError(Task<AuthResult> task) {
        String error = Objects.requireNonNull(task.getException()).getMessage();
        if (error != null && error.contains("already in use")) {

            hideLoading();

            Toast.makeText(this, "Este e-mail já está cadastrado!", Toast.LENGTH_LONG).show();
            checkBoxCad.setChecked(false);
            btnCad.setEnabled(false);
            findViewById(R.id.fragment_container).setVisibility(View.GONE);

        } else {

            hideLoading();

            Toast.makeText(this, "Erro ao criar conta: " + error, Toast.LENGTH_SHORT).show();
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
        }
    }

    interface CadastroCallback {
        void onLiberado();

        void onErro(String msg);
    }

}