package com.example.juntoscontradengue;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityCriarCadastroAgenteBinding;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CriarCadastroAgente extends AppCompatActivity {
        private androidx.appcompat.app.AlertDialog loadingDialog;
        private ActivityResultLauncher<Intent> imagePickerLauncher;
        private boolean isImageSelected = false;
        private String nome_agente, cpf_agente, telefone_agente;
        private String email_agente, senha_agente, sFuncao_agente;
        private String estado, municipio, uuid;
        private EditText nomeAgente, cpfAgente, telefoneAgente, emailAgente, cEmailAgente;
        private EditText senhaAgente;
        private EditText cSenhaAgente;
        private Button btnCadAgente;
        ImageView img_agente;
        private Uri imageUri;
        private FirebaseAuth mAuth;
        String cpf, nome, funcao, cpfLimpo, telLimpo;
        final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            ActivityCriarCadastroAgenteBinding cadastro_binding = ActivityCriarCadastroAgenteBinding.inflate(getLayoutInflater());
            setContentView(cadastro_binding.getRoot());

            Intent intent = getIntent();
            cpf = intent.getStringExtra("cpf");
            nome = intent.getStringExtra("nome");
            funcao = intent.getStringExtra("funcao");

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
            telefoneAgente = cadastro_binding.edtTelefoneCriarCadAgenteAdmin;
            telefoneAgente.addTextChangedListener(MaskEditUtil.mask(MaskEditUtil.FORMAT_FONE));

            senhaAgente = cadastro_binding.edtSenhaCriarCadAgenteAdmin;
            cSenhaAgente = cadastro_binding.edtConfirmarSenhaCriarCadAgenteAdmin;
            setupPasswordVisibilityToggle(senhaAgente);
            setupPasswordVisibilityToggle(cSenhaAgente);

            btnCadAgente = cadastro_binding.btnCriarContaCadAgenteAdmin;
            TextView ler_termos_cad_agentes = cadastro_binding.lerTermosCadAgentes;

            img_agente.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });

            cadastro_binding.checkBoxCriarCadAgenteAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> btnCadAgente.setEnabled(isChecked));

            ler_termos_cad_agentes.setOnClickListener(v -> startActivity(new Intent(CriarCadastroAgente.this, TermosDeUsoActivity.class)));

            btnCadAgente.setOnClickListener(view -> verificarCampo());
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
            telefone_agente = telefoneAgente.getText().toString().trim();
            senha_agente = senhaAgente.getText().toString();
            String cSenha_agente = cSenhaAgente.getText().toString();

            if (nome_agente.isEmpty() || cpf_agente.isEmpty() || email_agente.isEmpty() ||
                    cEmail_agente.isEmpty() || telefone_agente.isEmpty() ||
                    senha_agente.isEmpty() || cSenha_agente.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_LONG).show();
                return;
            }

            if (!ValidaCpf.validaCPF(cpf_agente)) {
                Toast.makeText(this, "Digite um número de CPF válido!", Toast.LENGTH_LONG).show();
                return;
            }

            if (!email_agente.equals(cEmail_agente)) {
                Toast.makeText(this, "Os emails não conferem!", Toast.LENGTH_LONG).show();
                return;
            }

            if (senha_agente.length() < 6) {
                Toast.makeText(this, "A senha deve ter 6 ou mais caracteres!", Toast.LENGTH_LONG).show();
                return;
            }

            if (!isImageSelected) {
                new AlertDialog.Builder(this)
                        .setTitle("Imagem de Perfil")
                        .setMessage("Deseja prosseguir sem escolher uma imagem de perfil?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.error_image);
                            validarCpfETelefoneNoFirebase();
                        })
                        .setNegativeButton("Não", (dialog, which) -> img_agente.performClick())
                        .show();
            } else {
                btnCadAgente.setEnabled(false);
                validarCpfETelefoneNoFirebase();
            }
        }

        private void validarCpfETelefoneNoFirebase() {
            showLoading();

            verificarCpfTelefoneAntesCadastro(new TelaDeCadastro.CadastroCallback() {
                @Override
                public void onLiberado() {
                    criarUsuarioFirebase(new AuthCallback() {
                        @Override
                        public void onSuccess(String uid) {
                            uuid = uid;
                            salvarDadosUsuario();
                        }

                        @Override
                        public void onFailure(String erro) {
                            if (erro.contains("already in use")) {
                                Alertas.showAlertDialog(CriarCadastroAgente.this, "Erro", "Este e-mail já está em uso!");
                            } else {
                                Toast.makeText(CriarCadastroAgente.this, erro, Toast.LENGTH_SHORT).show();
                            }
                            hideLoading();
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

        private void verificarCpfTelefoneAntesCadastro(TelaDeCadastro.CadastroCallback callback) {
            cpfLimpo = cpf_agente.replaceAll("[^0-9]", "");
            telLimpo = telefone_agente.replaceAll("[^0-9]", "");

            DatabaseReference baseRef = FirebaseDatabase.getInstance()
                    .getReference("cadastros")
                    .child(estado)
                    .child(municipio);

            // Verifica CPF
            baseRef.child("cpf_index").child(cpfLimpo)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                callback.onErro("Este CPF já está cadastrado.");
                                hideLoading();                                return;
                            }

                            // Verifica telefone
                            baseRef.child("telefone_index").child(telLimpo)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap) {
                                            if (snap.exists()) {
                                                callback.onErro("Este telefone já está cadastrado.");
                                                hideLoading();                                            } else {
                                                callback.onLiberado();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            callback.onErro(error.getMessage());
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onErro(error.getMessage());
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
                        callback.onFailure(e.getMessage());
                    });
        }

        private void salvarDadosUsuario() {
            showLoading();
            // 1. Upload da Imagem para o Storage
            String fileName = nome_agente.replace(" ", "_").toLowerCase() + "_" + uuid;
            StorageReference fileRef = storageReference.child(estado + "/" + municipio + "/imgAgentes/" + fileName);

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String urlImagemFinal = uri.toString();

                Calendar c = Calendar.getInstance();
                long dataCadastro = c.getTimeInMillis();

                // 2. Salvar Dados no Realtime Database
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("cadastros")
                        .child(estado)
                        .child(municipio)
                        .child("logins")
                        .child("agentes")
                        .child(uuid);

                Map<String, Object> dados = new HashMap<>();
                dados.put("nome", nome_agente);
                dados.put("cpf", cpf_agente);
                dados.put("telefone", telefone_agente);
                dados.put("email", email_agente);
                dados.put("funcao", sFuncao_agente);
                dados.put("urlImagem", urlImagemFinal);
                dados.put("dataCadastro", dataCadastro);
                dados.put("updatedAt", null);
                dados.put("uuid", uuid);

                userRef.setValue(dados).addOnSuccessListener(aVoid -> {
                    // Salvar dados do usuário no SharedPreferences
                    SharedPreferences prefUser = getSharedPreferences("UserData", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefUser.edit();
                    editor.putString("perfil", "agentes");
                    editor.putString("nome_usuario", nome_agente);
                    editor.putString("cpf", cpf_agente);
                    editor.putString("user_id", uuid);
                    editor.apply();

                    // CORREÇÃO AQUI: Usar cpfLimpo em vez de cpf (que é a variável da intent)
                    DatabaseReference baseRef = FirebaseDatabase.getInstance()
                            .getReference("cadastros")
                            .child(estado)
                            .child(municipio);

                    // Criar índices
                    Map<String, Object> indices = new HashMap<>();
                    indices.put("cpf_index/" + cpfLimpo, uuid);
                    indices.put("telefone_index/" + telLimpo, uuid);

                    baseRef.updateChildren(indices).addOnSuccessListener(aVoid1 -> {
                        // Mostrar AlertDialog de sucesso
                        new AlertDialog.Builder(CriarCadastroAgente.this)
                                .setTitle("Sucesso")
                                .setMessage("Dados salvos com sucesso!")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    deletar_pre_cadastro_agente();
                                    // Ir para tela de login
                                    Intent intent = new Intent(CriarCadastroAgente.this, ActivityLoginAgentes.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .setCancelable(false)
                                .show();

                        hideLoading();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(CriarCadastroAgente.this, "Erro ao criar índices: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        hideLoading();                        });

                }).addOnFailureListener(e -> {
                    Toast.makeText(CriarCadastroAgente.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    hideLoading();                    });
            })).addOnFailureListener(e -> {
                hideLoading();
                Toast.makeText(this, "Erro no upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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


    private void deletar_pre_cadastro_agente() {
            cpf = cpf.replaceAll("[^0-9]", "");
            DatabaseReference usersRef = FirebaseDatabase.getInstance()
                    .getReference( "cadastros")
                    .child(estado)
                    .child(municipio)
                    .child("config")
                    .child("pre_cadastro_agentes")
                    .child(cpf);

            usersRef.removeValue();

    }

    public interface AuthCallback {
            void onSuccess(String uid);
            void onFailure(String erro);
        }
    }