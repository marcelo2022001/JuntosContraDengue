package com.example.juntoscontradengue;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.juntoscontradengue.databinding.ActivityCriarCadastroAgenteBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.AppConfig;
import com.example.juntoscontradengue.extras.EmailValidator;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.TopicHelper;
import com.example.juntoscontradengue.extras.ValidaCpf;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CriarCadastroAgente extends AppCompatActivity {

        private androidx.appcompat.app.AlertDialog loadingDialog;
        private ActivityResultLauncher<Intent> imagePickerLauncher;
        private boolean isImageSelected = false;
        private String nome_agente, cpf_agente;
        private String email_agente, senha_agente, sFuncao_agente;
        private String estado, municipio, uuid;
        private EditText nomeAgente, cpfAgente, emailAgente, cEmailAgente;
        private EditText senhaAgente;
        private EditText cSenhaAgente;
        private CheckBox checkBoxCriarCadAgente;
        private Button btnCadAgente;
        ImageView img_agente;
        private Uri imageUri;
        private boolean usaImagem;
        private FirebaseAuth mAuth;
        private String cpf;
    private String nome;
    private String funcao;
    private String cpfLimpo;
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        private FirebaseDatabase databaseMunicipio;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            ActivityCriarCadastroAgenteBinding cadastro_binding = ActivityCriarCadastroAgenteBinding.inflate(getLayoutInflater());
            setContentView(cadastro_binding.getRoot());

            Intent intent = getIntent();
            cpf = intent.getStringExtra("cpf");
            nome = intent.getStringExtra("nome");
            funcao = intent.getStringExtra("funcao");
            String uso_imagem = intent.getStringExtra("uso_imagem");
            usaImagem = "true".equals(uso_imagem);

            // Inicialize o FirebaseAuth aqui
            mAuth = FirebaseAuth.getInstance();

            initializeComponents(cadastro_binding);
            checkNetworkConnection();
            setupUI(cadastro_binding);

            // No onCreate, inicialize o launcher
            imagePickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            cadastro_binding.imgCriarCadAgenteAdmin.setImageURI(imageUri);
                            isImageSelected = true;
                        }
                    }
            );
        }

        private void initializeComponents(ActivityCriarCadastroAgenteBinding cadastro_binding) {
            Toolbar toolbar = cadastro_binding.tbCriarCadAgenteAdmin;
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

        private void setupUI(ActivityCriarCadastroAgenteBinding cadastro_binding) {

            estado = AppConfig.getEstado(this);
            municipio = AppConfig.getMunicipio(this);


            String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
            databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

            CardView card_img_agente = cadastro_binding.cardViewAgentesCad;
            img_agente = cadastro_binding.imgCriarCadAgenteAdmin;

            nomeAgente = cadastro_binding.edtNomeCriarCadAgenteAdmin;
            nomeAgente.setText(nome);
            nomeAgente.setEnabled(false);

            cpfAgente = cadastro_binding.edtCpfCriarCadAgenteAdmin;
            cpfAgente.setText(cpf);
            cpfAgente.setEnabled(false);

            EditText edtFuncaoAgente = cadastro_binding.edtFuncaoCadAgenteAdmin;
            edtFuncaoAgente.setText(funcao);
            edtFuncaoAgente.setEnabled(false);
            sFuncao_agente = edtFuncaoAgente.getText().toString();

            emailAgente = cadastro_binding.edtEmailCriarCadAgenteAdmin;
            cEmailAgente = cadastro_binding.edtConfirmeEmailCriarCadAgenteAdmin;

            senhaAgente = cadastro_binding.edtSenhaCriarCadAgenteAdmin;
            cSenhaAgente = cadastro_binding.edtConfirmarSenhaCriarCadAgenteAdmin;
            setupPasswordVisibilityToggle(senhaAgente);
            setupPasswordVisibilityToggle(cSenhaAgente);

            checkBoxCriarCadAgente = cadastro_binding.checkBoxCriarCadAgenteAdmin;

            btnCadAgente = cadastro_binding.btnCriarContaCadAgenteAdmin;
            TextView ler_termos_cad_agentes = cadastro_binding.lerTermosCadAgentes;


            if (!usaImagem) {
                card_img_agente.setVisibility(View.GONE);
            }

            img_agente.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });

            checkBoxCriarCadAgente.setOnCheckedChangeListener((buttonView,
            isChecked) -> {
                        btnCadAgente.setEnabled(isChecked);

                if (isChecked) {
                    desativar_edit_text();
                } else {
                    checkBoxCriarCadAgente.setChecked(false);
                    ativar_edit_text();
                }

                    });

            ler_termos_cad_agentes.setOnClickListener(v -> startActivity(new Intent(CriarCadastroAgente.this, TermosDeUsoActivity.class)));

            btnCadAgente.setOnClickListener(view -> verificarCampo());
        }

    private void desativar_edit_text() {
        emailAgente.setEnabled(false);
        cEmailAgente.setEnabled(false);
        senhaAgente.setEnabled(false);
        cSenhaAgente.setEnabled(false);
    }

    @SuppressLint("ClickableViewAccessibility")
        private void setupPasswordVisibilityToggle(final EditText editText) {
            editText.setOnTouchListener((v, event) -> {
                final int DRAWABLE_RIGHT = 2;
                if (editText.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int areaIcone = editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() + editText.getPaddingEnd();

                    if (event.getX() >= (editText.getWidth() - areaIcone)) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            v.performClick();
                            togglePasswordVisibility(editText);
                        }
                        return true;
                    }
                }
                return false;
            });
        }

        private void togglePasswordVisibility(EditText editText) {
            boolean isCurrentlyVisible = editText.getTransformationMethod() instanceof HideReturnsTransformationMethod;
            if (isCurrentlyVisible) {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_closed, 0);
            } else {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_open, 0);
            }
            editText.setSelection(editText.getText().length());
        }

        private void verificarCampo() {
            nome_agente = nomeAgente.getText().toString().trim();
            cpf_agente = cpfAgente.getText().toString().trim();
            email_agente = emailAgente.getText().toString().trim();
            String cEmail_agente = cEmailAgente.getText().toString().trim();
            senha_agente = senhaAgente.getText().toString();
            String cSenha_agente = cSenhaAgente.getText().toString();

            if (nome_agente.isEmpty() || cpf_agente.isEmpty() || email_agente.isEmpty() ||
                    cEmail_agente.isEmpty() || senha_agente.isEmpty() || cSenha_agente.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_LONG).show();

                if(nome_agente.isEmpty()){
                    nomeAgente.setEnabled(true);

                } else if (cpf_agente.isEmpty()){
                    cpfAgente.setEnabled(true);
                }
                
                return;
            }

            if (!ValidaCpf.validaCPF(cpf_agente)) {
                Toast.makeText(this, "Digite um número de CPF válido!", Toast.LENGTH_LONG).show();
                cpfAgente.setEnabled(true);
                return;
            }

            if (!EmailValidator.getInstance().validate(email_agente)) {
                Toast.makeText(this, "Digite um endereço de e-mail válido", Toast.LENGTH_LONG).show();
                checkBoxCriarCadAgente.setChecked(false);
                ativar_edit_text();
                return;
            }

            if (!email_agente.equals(cEmail_agente)) {
                Toast.makeText(this, "Os emails não conferem!", Toast.LENGTH_LONG).show();
                checkBoxCriarCadAgente.setChecked(false);
                ativar_edit_text();
                return;
            }

            if (!senha_agente.equals(cSenha_agente)) {
                Toast.makeText(this, "As senhas não conferem!", Toast.LENGTH_LONG).show();
                checkBoxCriarCadAgente.setChecked(false);
                ativar_edit_text();
                return;
            }

            if (senha_agente.length() < 6) {
                Toast.makeText(this, "A senha deve ter 6 ou mais caracteres!", Toast.LENGTH_LONG).show();
                checkBoxCriarCadAgente.setChecked(false);
                ativar_edit_text();
                return;
            }

            if (usaImagem && !isImageSelected) {
                new AlertDialog.Builder(this)
                        .setTitle("Imagem de Perfil")
                        .setMessage("Deseja prosseguir sem escolher uma imagem de perfil?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.error_image);
                            verificarCpfAntesCadastro();
                        })
                        .setNegativeButton("Não", (dialog, which) -> img_agente.performClick())
                        .show();
            } else {
                btnCadAgente.setEnabled(false);
                verificarCpfAntesCadastro();
            }
        }

    private void ativar_edit_text() {
        emailAgente.setEnabled(true);
        cEmailAgente.setEnabled(true);
        senhaAgente.setEnabled(true);
        cSenhaAgente.setEnabled(true);
    }

    private void verificarCpfAntesCadastro() {
            showLoading();

            verifCpfAntesCadastro(new TelaDeCadastro.CadastroCallback() {
                @Override
                public void onLiberado() {
                    
                   desativar_edit_text();
                   
                    criarUsuarioFirebase(new AuthCallback() {
                        @Override
                        public void onSuccess(String uid) {
                            uuid = uid;
                            salvarDadosUsuario();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            hideLoading();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                Alertas.showAlertDialog(CriarCadastroAgente.this, "Erro", "Este e-mail já está em uso!");
                                checkBoxCriarCadAgente.setChecked(false);
                                ativar_edit_text();
                            } else {
                                btnCadAgente.setEnabled(true);   // permite corrigir e tentar de novo
                                Toast.makeText(CriarCadastroAgente.this, mensagemErroAuth(e), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                @Override
                public void onErro(String msg) {
                    Alertas.showAlertDialog(CriarCadastroAgente.this, "Alerta", msg);
                   hideLoading();
                }
            });
        }

    private String mensagemErroAuth(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Digite um endereço de e-mail válido";
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            return "A senha deve ter 6 ou mais caracteres!";
        } else if (e instanceof FirebaseNetworkException) {
            return "Sem conexão de internet! Ative o wifi ou dados móveis!";
        }
        return "Não foi possível criar a conta. Tente novamente.";
    }

    private void verifCpfAntesCadastro(TelaDeCadastro.CadastroCallback callback) {
            cpfLimpo = cpf_agente.replaceAll("[^0-9]", "");

            DatabaseReference baseRef = databaseMunicipio.getReference();

            // Verifica CPF
            baseRef.child("cpf_index").child(cpfLimpo)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                callback.onErro("Este CPF já está cadastrado.");
                                hideLoading();
                                checkBoxCriarCadAgente.setChecked(false);
                                ativar_edit_text();

                            }else {
                                callback.onLiberado();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onErro(error.getMessage());
                            checkBoxCriarCadAgente.setChecked(false);
                            ativar_edit_text();
                        }
                    });
        }

        private void criarUsuarioFirebase(AuthCallback callback) {
            showLoading();
            mAuth.createUserWithEmailAndPassword(email_agente, senha_agente)
                    .addOnSuccessListener(authResult -> {
                        if (authResult.getUser() != null) {
                            callback.onSuccess(authResult.getUser().getUid());
                        }
                    })
                    .addOnFailureListener(e -> {
                        hideLoading();
                        checkBoxCriarCadAgente.setChecked(false);
                        callback.onFailure(e);
                    });
        }

    private void salvarDadosUsuario() {
        showLoading();

        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome_agente);
        dados.put("cpf", cpfLimpo);
        dados.put("email", email_agente);
        dados.put("funcao", sFuncao_agente);
        dados.put("uuid", uuid);
        dados.put("uso_imagem", usaImagem);


        DatabaseReference baseRef = databaseMunicipio.getReference();

        // 1) Dados do agente (ainda sem imagem)
        baseRef.child("logins").child("agentes").child(uuid).setValue(dados)
                .addOnSuccessListener(aVoid -> {

                    SharedPreferences.Editor editor =
                            getSharedPreferences("UserData", MODE_PRIVATE).edit();
                    editor.putString("perfil", "agentes");
                    editor.putString("nome_usuario", nome_agente);
                    editor.putString("cpf", cpfLimpo);
                    editor.putString("user_id", uuid);
                    editor.apply();

                    // 2) Índices (um único updateChildren atômico)
                    Map<String, Object> indices = new HashMap<>();
                    indices.put("cpf_index/" + cpfLimpo, uuid);
                    indices.put("index_email_agentes/" + cpfLimpo + "/email", email_agente);

                    if (usaImagem) {
                        indices.put("agentes_publico/" + uuid + "/nome", nome_agente);
                        indices.put("agentes_publico/" + uuid + "/funcao", sFuncao_agente);
                    }

                    baseRef.updateChildren(indices)
                            .addOnSuccessListener(aVoid1 -> {
                                TopicHelper.inscreverNoTopicoDoPerfil(this, "agentes");

                                // 3) Sucesso: apaga o pré-cadastro agora, sem depender do clique no OK
                                deletar_pre_cadastro_agente();

                                // 4) Imagem só se uso_imagem == "true"
                                if (usaImagem) {
                                    salvarImagemPerfil();
                                } else {
                                    hideLoading();
                                    mostrarSucesso();
                                }
                            })
                            .addOnFailureListener(e -> {
                                hideLoading();
                                checkBoxCriarCadAgente.setChecked(false);
                                Toast.makeText(this, "Erro ao criar índices: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    checkBoxCriarCadAgente.setChecked(false);
                    Toast.makeText(this, "Erro ao salvar dados: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void salvarImagemPerfil() {
        showLoading();

        String fileName = nome_agente.replace(" ", "_").toLowerCase() + "_" + uuid;
        StorageReference fileRef = storageReference
                .child(estado + "/" + municipio + "/imgAgentes/" + fileName);

        fileRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
                    return fileRef.getDownloadUrl();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
                    String url = task.getResult().toString();

                    Map<String, Object> up = new HashMap<>();
                    up.put("logins/agentes/" + uuid + "/urlImagem", url);
                    up.put("agentes_publico/" + uuid + "/urlImagem", url);

                    return databaseMunicipio.getReference().updateChildren(up);
                })
                .addOnSuccessListener(v -> {
                    hideLoading();
                    mostrarSucesso();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    mostrarFalhaImagem();
                });
    }

    private void mostrarSucesso() {
        new AlertDialog.Builder(this)
                .setTitle("Sucesso")
                .setMessage("Cadastro realizado com sucesso!")
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> irParaLogin())
                .show();
    }

    private void mostrarFalhaImagem() {
        new AlertDialog.Builder(this)
                .setTitle("Imagem não enviada")
                .setMessage("Seus dados foram salvos, mas não foi possível enviar a imagem de perfil.")
                .setCancelable(false)
                .setPositiveButton("Tentar novamente", (d, w) -> salvarImagemPerfil())
                .setNegativeButton("Continuar sem imagem", (d, w) -> irParaLogin())
                .show();
    }

    private void irParaLogin() {

        // 1) encerra a sessão criada no cadastro
        mAuth.signOut();

        // 2) monta a pilha: Main (base) -> Login (topo)
        Intent main  = new Intent(this, MainActivity.class);
        Intent login = new Intent(this, ActivityLoginAgentes.class);

        TaskStackBuilder.create(this)
                .addNextIntent(main)
                .addNextIntent(login)
                .startActivities();

        finish();
    }

    private void deletar_pre_cadastro_agente() {
        databaseMunicipio.getReference()
                .child("config").child("pre_cadastro_agentes").child(cpfLimpo)
                .removeValue()
                .addOnSuccessListener(v -> Log.d("CadAgente", "Pré-cadastro removido: " + cpfLimpo))
                .addOnFailureListener(e -> Log.e("CadAgente", "Falha ao remover pré-cadastro: " + e.getMessage()));
    }

    private void showLoading() {
        if (loadingDialog == null) {
            androidx.appcompat.app.AlertDialog
                    .Builder builder = new androidx.appcompat.app.
                    AlertDialog.Builder(this);
            View view = getLayoutInflater()
                    .inflate(R.layout.dialog_loading, null);
            builder.setView(view);
            builder.setCancelable(false);
            loadingDialog = builder.create();
        } loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss(); }
    }

    public interface AuthCallback {
            void onSuccess(String uid);
            void onFailure(Exception  e);
    }
    }