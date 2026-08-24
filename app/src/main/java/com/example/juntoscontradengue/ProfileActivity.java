package com.example.juntoscontradengue;

import static android.view.View.GONE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.database.classes_database.ClassUsuarios;
import com.example.juntoscontradengue.databinding.ActivityProfileBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.EmailValidator;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.TopicHelper;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    ActivityProfileBinding profileBinding;
    private Boolean img_trocada = false;
    private boolean isRedirecting = false;
    Boolean isConnected;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private FirebaseAuth.AuthStateListener authListener;
    private String estado, municipio;
    private String nomeSalvoSharedOuFirebase, cpfSalvoSharedOuFirebase, enderecoSalvoSharedOuFirebase;
    private String num_casaSalvoSharedOuFirebase, conjuntoSalvoSharedOuFirebase;
    private String telefoneSalvoSharedOuFirebase, emailSalvoSharedOuFirebase;
    private String uid;
    private String perfil, funcao;
    private Uri url_img_agente;
    private String trocaSenha;
    private String verificaEmail;
    private Long dataCadastro;
    private Long updatedAt;
    private TextView nomeUsers, tCpf, dataCadastroEm, cadastroAtualizadoEm, txtSuggestion;
    private EditText enderecoUsers, numCasaUsers, conjuntoEnderecoUsers, telefoneUsers, emailUsers;
   private ImageView img_profile;
    private LinearLayout suggestionLayout;
    private String suggestedEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isConnected = NetworkUtils.isNetworkAvailable(ProfileActivity.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "profile_admin_agentes");
            startActivity(itente);

        }


        isRedirecting = false;
        img_trocada = false;

         profileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(profileBinding.getRoot());

        Toolbar toolbarProfile = profileBinding.toolbarProfile;
        setSupportActionBar(toolbarProfile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("cadastros");

        initViews(profileBinding);

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        SharedPreferences prefsUser = getSharedPreferences("UserData", MODE_PRIVATE);
        perfil = prefsUser.getString("perfil", null);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("PROFILE", "user = " + currentUser);

        if (currentUser == null) {
            Log.e("AUTH", "Usuário não autenticado no onCreate");
            redirecionarLogin();
            return;
        }

        uid = currentUser.getUid();

        if(!TextUtils.isEmpty(perfil)){
            if(perfil.equals("agentes")){
                busca_imagem_agente();
            }
        }

        String perfilOriginal = perfil; // valor cru vindo do SharedPreferences

        boolean ehAdmin = "admin".equals(perfilOriginal) || "admins".equals(perfilOriginal);
        boolean ehAgente = "agente".equals(perfilOriginal) || "agentes".equals(perfilOriginal);
        boolean ehUsuario = "usuario".equals(perfilOriginal) || "usuarios".equals(perfilOriginal);

        if (ehAdmin) {
            perfil = "admins";
            enderecoUsers.setVisibility(GONE);
            numCasaUsers.setVisibility(GONE);
            conjuntoEnderecoUsers.setVisibility(GONE);
            telefoneUsers.setVisibility(GONE);

        } else if (ehAgente) {
            perfil = "agentes";
            enderecoUsers.setVisibility(GONE);
            numCasaUsers.setVisibility(GONE);
            conjuntoEnderecoUsers.setVisibility(GONE);
            telefoneUsers.setVisibility(GONE);

        } else if (ehUsuario) {
            perfil = "usuarios";
        }

        setupAuthListener();
        setupEmailValidation();
        buscarDadosUsuarioNoFirebase();

        // No onCreate, inicialize o launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        url_img_agente = result.getData().getData();
                        Glide.with(ProfileActivity.this)
                                .load(url_img_agente)
                                .circleCrop()
                                .placeholder(R.drawable.profile_icon) // opcional
                                .into(img_profile);
                        img_trocada = true;
                    }
                }
        );

    }

    private void busca_imagem_agente() {
        DatabaseReference buscar_img_agente = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("logins")
                .child("agentes")
                .child(uid)
                .child("urlImagem");
        buscar_img_agente.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obter a contagem de filhos
                    String urlImg = dataSnapshot.getValue(String.class);
                    if (urlImg != null && !urlImg.isEmpty()) {

                        Glide.with(ProfileActivity.this)
                                .load(urlImg)
                                .circleCrop()
                                .placeholder(R.drawable.profile_icon) // opcional
                                .into(img_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Lidar com possíveis erros
                Log.d( "Database Error: ", "Erro: " + databaseError);

            }
        });
    }


    private void initViews(ActivityProfileBinding b) {
        img_profile = b.imgProfile;
        nomeUsers = b.txtProfileNome;
        tCpf = b.txtProfileCpf;
        enderecoUsers = b.edtProfileEndereco;
        numCasaUsers = b.edtProfileNumcasa;
        conjuntoEnderecoUsers = b.edtProfileConjunto;
        telefoneUsers = b.edtProfileTelefone;
        emailUsers = b.edtProfileEmail;
        dataCadastroEm = b.dtcadastroEm;
        cadastroAtualizadoEm = b.updateAt;
        suggestionLayout = b.suggestionLayout;
        txtSuggestion = b.txtSuggestion;
        Button btnAcceptSuggestion = b.btnAcceptSuggestion;
        Button salvarDadosProfile = b.btnSalvarProfile;
        Button btnTrocarSenhaProfile = b.btnMudarSenhaProfile;
        Button btnExcluirConta = b.btnExcluirConta;

        img_profile.setOnClickListener(v -> {

            if("agentes".equals(perfil)) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
    });


        salvarDadosProfile.setOnClickListener(v -> salvar());
        btnTrocarSenhaProfile.setOnClickListener(v -> dispararEmailRedefinicaoSenha());
        btnExcluirConta.setOnClickListener(v -> conta_admin_cadastro());
        btnAcceptSuggestion.setOnClickListener(v -> acceptSuggestion());
    }

    private void dispararEmailRedefinicaoSenha() {
        if (isFinishing() || isDestroyed()) return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Alterar Senha");

        // Cria um container vertical para colocar os 3 campos de texto
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // Campo 1: Senha Atual
        final EditText inputSenhaAtual = new EditText(this);
        inputSenhaAtual.setHint("Senha atual");
        inputSenhaAtual.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputSenhaAtual);

        // Campo 2: Nova Senha
        final EditText inputNovaSenha = new EditText(this);
        inputNovaSenha.setHint("Nova senha (mínimo 6 caracteres)");
        inputNovaSenha.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 0);
        inputNovaSenha.setLayoutParams(params);
        layout.addView(inputNovaSenha);

        // Campo 3: Confirmar Nova Senha
        final EditText inputConfirmarSenha = new EditText(this);
        inputConfirmarSenha.setHint("Confirme a nova senha");
        inputConfirmarSenha.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputConfirmarSenha.setLayoutParams(params);
        layout.addView(inputConfirmarSenha);

        builder.setView(layout);

        builder.setPositiveButton("ALTERAR", (dialog, which) -> {
            String senhaAtual = inputSenhaAtual.getText().toString().trim();
            String novaSenha = inputNovaSenha.getText().toString().trim();
            String confirmarSenha = inputConfirmarSenha.getText().toString().trim();

            // Validações básicas antes de chamar o Firebase
            if (senhaAtual.isEmpty() || novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
                showToastSafe("Todos os campos são obrigatórios.");
                return;
            }
            if (novaSenha.length() < 6) {
                showToastSafe("A nova senha deve ter pelo menos 6 caracteres.");
                return;
            }
            if (!novaSenha.equals(confirmarSenha)) {
                showToastSafe("As novas senhas não coincidem.");
                return;
            }

            // Executa o processo de segurança e troca
            processarTrocaSenhaNoFirebase(senhaAtual, novaSenha);
        });

        builder.setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);
        builder.show();
    }

    private void processarTrocaSenhaNoFirebase(String senhaAtual, String novaSenha) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showToastSafe("Usuário não identificado.");
            return;
        }

        // 1. Reautentica o usuário com a senha atual por segurança
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), senhaAtual);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2. Se a reautenticação der certo, atualiza para a nova senha
                        user.updatePassword(novaSenha)
                                .addOnCompleteListener(passwordTask -> {
                                    if (passwordTask.isSuccessful()) {
                                        Alertas.showAlertDialog(this, "Sucesso", "Sua senha foi alterada com sucesso!");
                                    } else {
                                        showToastSafe("Erro ao atualizar senha: " + Objects.requireNonNull(passwordTask.getException()).getMessage());
                                    }
                                });
                    } else {
                        showToastSafe("Senha atual incorreta. Falha ao autenticar.");
                    }
                });
    }

    private void setupAuthListener() {
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null && !isRedirecting) {
                Log.d("AUTH", "Sessão encerrada pelo Firebase");
                redirecionarLogin();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth != null && authListener != null) {
            mAuth.addAuthStateListener(authListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth != null && authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    private void buscarDadosUsuarioNoFirebase() {
        if (TextUtils.isEmpty(estado) || TextUtils.isEmpty(municipio) || TextUtils.isEmpty(perfil) || TextUtils.isEmpty(uid)) {
            redirecionarLogin();
        }

        usersRef
                .child(estado)
                .child(municipio)
                .child("logins")
                .child(perfil)
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ClassUsuarios user = snapshot.getValue(ClassUsuarios.class);
                            if (user != null) {
                                nomeSalvoSharedOuFirebase = user.getNome();
                                cpfSalvoSharedOuFirebase = user.getCpf();
                                funcao = user.getFuncao();
                                emailSalvoSharedOuFirebase = user.getEmail();
                                enderecoSalvoSharedOuFirebase = user.getEndereco();
                                num_casaSalvoSharedOuFirebase = user.getNum_casa();
                                conjuntoSalvoSharedOuFirebase = user.getConjunto();
                                telefoneSalvoSharedOuFirebase = user.getTelefone();
                                dataCadastro = user.getDataCadastro();
                                updatedAt = user.getUpdatedAt();



                                // Atualiza as views
                                adicionaDadosUsuarioNaTela();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void adicionaDadosUsuarioNaTela() {
        nomeUsers.setText(nomeSalvoSharedOuFirebase != null ? nomeSalvoSharedOuFirebase : "Usuário");
        tCpf.setText(MaskEditUtil.FORMAT_CPF(cpfSalvoSharedOuFirebase));

        if ("usuarios".equals(perfil)) {
            enderecoUsers.setText(enderecoSalvoSharedOuFirebase);
            numCasaUsers.setText(num_casaSalvoSharedOuFirebase);
            conjuntoEnderecoUsers.setText(conjuntoSalvoSharedOuFirebase);

            if (!TextUtils.isEmpty(telefoneSalvoSharedOuFirebase)) {
                telefoneUsers.setText(MaskEditUtil.formatarTelefone(telefoneSalvoSharedOuFirebase));
            } else {
                telefoneUsers.setVisibility(GONE);
            }
        } else {
            // ✅ Admin e Agente: esconde campos de endereço
            enderecoUsers.setVisibility(GONE);
            numCasaUsers.setVisibility(GONE);
            conjuntoEnderecoUsers.setVisibility(GONE);
            telefoneUsers.setVisibility(GONE);
        }

        emailUsers.setText(emailSalvoSharedOuFirebase);

        // ✅ DATA DE CADASTRO (sempre existe)
        if (dataCadastro != null && dataCadastro > 0) {
            String dataCadastroFormatada = formatarDataLong(dataCadastro);
            dataCadastroEm.setText(String.format("Data do cadastro: %s", dataCadastroFormatada));
            dataCadastroEm.setVisibility(View.VISIBLE);
        } else {
            dataCadastroEm.setVisibility(GONE);
        }

        // ✅ UPDATED AT (0 = nunca atualizado)
        if (updatedAt != null && updatedAt > 0) {
            cadastroAtualizadoEm.setVisibility(View.VISIBLE);
            String dataFormatada = formatarDataLong(updatedAt);
            cadastroAtualizadoEm.setText(String.format("Cadastro atualizado em: %s", dataFormatada));
        } else {
            cadastroAtualizadoEm.setVisibility(GONE);  // ← Não mostra se for 0
        }

        verificaEmail = emailSalvoSharedOuFirebase;
    }

    private String formatarDataLong(Long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "";
        }
    }


    private void salvar() {
        String nEnd = enderecoUsers.getText().toString().trim();
        String nNum = numCasaUsers.getText().toString().trim();
        String nConj = conjuntoEnderecoUsers.getText().toString().trim();
        String nEmail = emailUsers.getText().toString().trim();
        String nFone = telefoneUsers.getText().toString().trim().replaceAll("\\D", "");

        if ("usuarios".equals(perfil)) {
            if (nEnd.isEmpty() || nNum.isEmpty() || nConj.isEmpty() || nFone.isEmpty() || nEmail.isEmpty()) {
                Toast.makeText(this, "Campos obrigatórios vazios.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if ("admins".equals(perfil) || "agentes".equals(perfil)) {
            if (nEmail.isEmpty()) {
                Toast.makeText(this, "Campos obrigatórios vazios.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Alertas.showAlertDialog(this, "Aviso", "Sem internet, ative os dados móveis");
            return;
        }

        conferirAlteracoesNoRealtimeAntesDeSalvar(nEnd, nNum, nConj, nFone, nEmail);
    }

    private void conferirAlteracoesNoRealtimeAntesDeSalvar(String end, String num, String conj, String fone, String mail) {

        DatabaseReference ref = usersRef
                .child(estado)
                .child(municipio)
                .child("logins")
                .child(perfil)
                .child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ClassUsuarios atual = snapshot.getValue(ClassUsuarios.class);
                if (atual == null) {
                    updateUserData(end, num, conj, fone, mail);
                    return;
                }

                if (semMudanca(atual, end, num, conj, fone, mail)) {
                    Alertas.showAlertDialog(ProfileActivity.this, "Aviso", "Nenhuma alteração detectada.");
                    return;
                }

                // Baseline atualizado com o valor real e atual do e-mail no banco
                verificaEmail = atual.getEmail();

                updateUserData(end, num, conj, fone, mail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateUserData(end, num, conj, fone, mail);
            }
        });
    }

    private boolean semMudanca(ClassUsuarios atual, String end, String num, String conj, String fone, String mail) {
        if ("usuarios".equals(perfil)) {
            return end.equals(nullToEmpty(atual.getEndereco()))
                    && num.equals(nullToEmpty(atual.getNum_casa()))
                    && conj.equals(nullToEmpty(atual.getConjunto()))
                    && fone.equals(nullToEmpty(atual.getTelefone()))
                    && mail.equals(nullToEmpty(atual.getEmail()));
        } else {
            return mail.equals(nullToEmpty(atual.getEmail())) && !img_trocada;
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void updateUserData(String end, String num, String conj, String fone, String mail) {

        DatabaseReference ref = usersRef
                .child(estado)
                .child(municipio)
                .child("logins")
                .child(perfil)
                .child(uid);

        Map<String, Object> updates = new HashMap<>();

        if ("usuarios".equals(perfil)) {
            // Usuário comum: salva os campos de endereço normalmente
            updates.put("endereco", end);
            updates.put("num_casa", num);
            updates.put("conjunto", conj);
            updates.put("telefone", fone);

            if (!mail.equals(verificaEmail)) {
                updates.put("novoEmail", mail);
            }

            updates.put("updatedAt", ServerValue.TIMESTAMP);

        } else if ("admins".equals(perfil) || "agentes".equals(perfil)) {

            boolean emailMudou = !mail.equals(verificaEmail);
            boolean imagemMudou = "agentes".equals(perfil) && img_trocada;

            if (!emailMudou && !imagemMudou) {
                // Segurança extra: não deveria chegar aqui, pois já é checado antes de chamar
                Alertas.showAlertDialog(this, "Aviso", "Nenhuma alteração detectada.");
                return;
            }

            if (emailMudou) {
                updates.put("novoEmail", mail);
            }

            if (imagemMudou) {
                String fileName = nomeSalvoSharedOuFirebase
                        .replace(" ", "_")
                        .toLowerCase(Locale.ROOT)
                        + "_" + uid;

                StorageReference fileRef = storageReference
                        .child(estado + "/" + municipio + "/imgAgentes/" + fileName);

                fileRef.putFile(url_img_agente)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                Exception exception = task.getException();
                                if (exception != null) throw exception;
                                throw new Exception("Erro desconhecido no upload.");
                            }
                            return fileRef.getDownloadUrl();
                        })
                        .addOnSuccessListener(uri -> {
                            updates.put("urlImagem", uri.toString());
                            updates.put("updatedAt", ServerValue.TIMESTAMP);
                            salvarUpdatesNoFirebase(ref, updates, end, num, conj, fone, mail);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Erro ao trocar imagem: " + e.getMessage(), Toast.LENGTH_LONG).show());

                // Sai aqui: o salvamento final acontece dentro do addOnSuccessListener acima,
                // só depois que a URL da nova imagem estiver disponível
                return;
            }

            updates.put("updatedAt", ServerValue.TIMESTAMP);
        }

        salvarUpdatesNoFirebase(ref, updates, end, num, conj, fone, mail);
    }

    private void salvarUpdatesNoFirebase(DatabaseReference ref, Map<String, Object> updates,
                                         String end, String num, String conj, String fone, String mail) {

        ref.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(ProfileActivity.this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();

            SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            if ("usuarios".equals(perfil)) {
                editor.putString("endereco", end);
                editor.putString("num_casa", num);
                editor.putString("conjunto", conj);
                editor.putString("telefone", fone);
            }

            if (!mail.equals(verificaEmail)) {
                editor.putString("email", mail);
            }
            editor.apply();

            if ("usuarios".equals(perfil)) {
                enderecoUsers.setText(end);
                enderecoUsers.clearFocus();
                numCasaUsers.setText(num);
                numCasaUsers.clearFocus();
                conjuntoEnderecoUsers.setText(conj);
                conjuntoEnderecoUsers.clearFocus();
                telefoneUsers.setText(fone);
                telefoneUsers.clearFocus();
            }

            if (!mail.equals(verificaEmail)) {
                Alertas.showConfirmDialog(
                        ProfileActivity.this,
                        "Mudar e-mail",
                        "Tem certeza que deseja mudar seu e-mail?\n\n" +
                                "Para confirmar, será necessário digitar sua senha atual.",
                        "CONTINUAR",
                        "CANCELAR",
                        this::pedirSenhaParaTrocaEmail,
                        () -> showToastSafe("Mudança de e-mail cancelada")
                );
            }
        }).addOnFailureListener(e ->
                Toast.makeText(ProfileActivity.this, "Erro ao atualizar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateFirebaseAuthEmailWithReauth(String novoEmail) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("Auth", "Nenhum usuário logado.");
            return;
        }

        // CORREÇÃO 2: Usar user.getEmail() (e-mail atual) para reautenticar, e não o novoEmail
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), trocaSenha);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Reautenticação deu certo! Envia o e-mail de verificação para o novo endereço
                        enviarVerificacaoNovoEmail(user, novoEmail);
                    } else {
                        // CORREÇÃO 3: Adicionado feedback visual caso a senha esteja incorreta
                        Log.e("Auth", "Falha na reautenticação", task.getException());
                        showToastSafe("Senha incorreta. Falha ao autenticar.");
                    }
                });
    }

    private void enviarVerificacaoNovoEmail(FirebaseUser user, String email) {
        user.verifyBeforeUpdateEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // 1. Salvamos no banco ANTES de deslogar e usando o UID correto
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().
                                getReference("cadastros");

                        mDatabase.child(estado)
                                .child(municipio)
                                .child("logins")
                                .child(perfil)
                                .child(uid) // O UID do usuário faltava aqui
                                .child("novoEmailCadastrado")
                                .setValue(email) // Usando a variável passada por parâmetro com segurança
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "E-mail salvo com sucesso!");

                                    // 2. Mostramos o alerta apenas após garantir que salvou
                                    Alertas.showAlertDialog(ProfileActivity.this, "Verificação Enviada",
                                            "Um link de confirmação foi enviado para: " + email + ".\n\nVocê será deslogado. Acesse seu e-mail, clique no link e faça o login com o novo endereço.",
                                            (dialog, which) -> {

                                                // Desloga do Firebase Auth por último
                                                if (mAuth != null) {
                                                    mAuth.signOut();
                                                }

                                                // Redireciona para a tela inicial
                                                Intent intent = new Intent(ProfileActivity.this, EscolherPerfilLogin.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Erro ao salvar novo email", e);
                                    Toast.makeText(ProfileActivity.this, "Erro ao salvar pendência no banco: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao enviar e-mail: " +
                                Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirecionarLogin() {
        if (isRedirecting) return;
        isRedirecting = true;

        Intent intent = new Intent(this, EscolherPerfilLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Métodos de validação de email e UI de sugestão permanecem similares...
    private void setupEmailValidation() {
        emailUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideSuggestion();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    validateEmailInRealTime(s.toString().trim());
                } else {
                    hideSuggestion();
                    emailUsers.setError(null);
                }
            }
        });

    }
    private void validateEmailInRealTime(String email) {
        EmailValidator validator = EmailValidator.getInstance();

        if (!validator.validate(email)) {
            String corrigido = validator.getCorrectedEmail(email);

            if (corrigido != null && !corrigido.equals(email)) {
                showSuggestion(corrigido);
            } else {
                hideSuggestion();
                emailUsers.setError("E-mail inválido");
            }
        } else {
            hideSuggestion();
            emailUsers.setError(null);
        }
    }

    private void showSuggestion(String correctedEmail) {
        String suggestionText = String.format("Você quis dizer: %s?", correctedEmail);
        txtSuggestion.setText(suggestionText);
        suggestionLayout.setVisibility(View.VISIBLE);
        this.suggestedEmail = correctedEmail;
    }

    private void hideSuggestion() {
        suggestionLayout.setVisibility(GONE);
        suggestedEmail = null;
    }

    private void acceptSuggestion() {
        if (suggestedEmail != null && !suggestedEmail.isEmpty()) {
            final String emailToSet = suggestedEmail;

            emailUsers.animate()
                    .alpha(0.5f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        emailUsers.setText(emailToSet);
                        emailUsers.setSelection(emailToSet.length());
                        emailUsers.animate().alpha(1f).setDuration(150).start();
                        emailUsers.setError(null);
                        hideSuggestion();
                        showToastSafe("E-mail corrigido");
                        emailSalvoSharedOuFirebase = emailToSet;
                    })
                    .start();
        } else {
            hideSuggestion();
        }
    }

    private void pedirSenhaParaTrocaEmail() {
        if (isFinishing() || isDestroyed()) return;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirmar Identidade");
        builder.setMessage("Para trocar seu e-mail, digite sua senha atual:");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        final EditText inputSenha = new EditText(this);
        inputSenha.setHint("Senha atual");
        inputSenha.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputSenha.setTextSize(16);
        layout.addView(inputSenha);
        builder.setView(layout);

        builder.setPositiveButton("CONFIRMAR", (dialog, which) -> {
            String senha = inputSenha.getText().toString().trim();
            if (!senha.isEmpty()) {
                trocaSenha = senha;
                // CORREÇÃO 1: Chamar o método de atualização passando o novo e-mail digitado no campo da tela
                String novoEmail = emailUsers.getText().toString().trim();
                updateFirebaseAuthEmailWithReauth(novoEmail);
            } else {
                showToastSafe("Digite sua senha");
            }
        });
        builder.setNegativeButton("CANCELAR", (dialog, which) -> {
            showToastSafe("Alteração cancelada");
            dialog.dismiss();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showToastSafe(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void excluir_conta() {

        if("admins".equals(perfil)) {
            conta_admin_cadastro();
        }

        Alertas.showConfirmDialog(
                ProfileActivity.this,
                "Excluir Conta",
                "Tem certeza que deseja excluir sua conta permanentemente?\n\n" +
                        "Todos os seus dados serão removidos.\n\n" +
                        "Para confirmar, será necessário digitar sua senha atual.",
                "EXCLUIR CONTA",
                "CANCELAR",
                this::pedirSenhaParaExclusao,
                () -> showToastSafe("Exclusão cancelada")
        );
    }

    private void conta_admin_cadastro() {

        DatabaseReference adminsRef = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("logins")
                .child("admins");

        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalAdmins = (int) snapshot.getChildrenCount();

                Log.d("TOTAL_ADMINS", "Quantidade: " + totalAdmins);

                if (totalAdmins == 1) {

                    Alertas.showAlertDialog(
                            ProfileActivity.this,
                            "Aviso",
                            "Não é possível excluir sua conta, pois você é o único administrador.\n\nCaso ainda deseje excluir a conta, entre em contato com o desenvolvedor."
                    );

                } else {

                    excluir_conta();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", error.getMessage());
            }
        });

    }

    private void pedirSenhaParaExclusao() {
        if (isFinishing() || isDestroyed()) return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirmar Identidade");
        builder.setMessage("Para excluir sua conta, digite sua senha atual:");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        final EditText inputSenha = new EditText(this);
        inputSenha.setHint("Senha atual");
        inputSenha.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputSenha.setTextSize(16);

        layout.addView(inputSenha);
        builder.setView(layout);

        builder.setPositiveButton("CONFIRMAR", (dialog, which) -> {
            String senha = inputSenha.getText().toString().trim();
            if (!senha.isEmpty()) {
                excluirContaComReautenticacao(senha);
            } else {
                showToastSafe("Digite sua senha");
            }
        });

        builder.setNegativeButton("CANCELAR", (dialog, which) -> {
            showToastSafe("Exclusão cancelada");
            dialog.dismiss();
        });

        builder.setCancelable(false);
        builder.show();
    }
    private void excluirContaComReautenticacao(String senha) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            showToastSafe("Usuário não encontrado");
            return;
        }

        androidx.appcompat.app.AlertDialog progressDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Excluindo conta...")
                .setMessage("Por favor, aguarde")
                .setCancelable(false)
                .show();

        com.google.firebase.auth.AuthCredential credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), senha);

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (!isFinishing() && !isDestroyed() && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    if (reauthTask.isSuccessful()) {
                        excluirDadosUsuario(user.getUid(), progressDialog);
                    } else {
                        showToastSafe("Senha incorreta");
                    }
                });
    }
    private void excluirDadosUsuario(String userId, androidx.appcompat.app.AlertDialog progressDialog) {
        if (isFinishing() || isDestroyed()) return;

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage("Excluindo dados do banco...");
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        updates.put("cadastros/" + estado + "/" + municipio + "/logins/usuarios/" + userId, null);
        updates.put("cadastros/" + estado + "/" + municipio + "/logins/admins/" + userId, null);
        updates.put("cadastros/" + estado + "/" + municipio + "/logins/agentes/" + userId, null);
        updates.put("cadastros/" + estado + "/" + municipio + "/reclamacoes/" + userId, null);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> excluirStorageUsuario(userId, progressDialog))
                .addOnFailureListener(e -> {
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, "Erro ao excluir dados", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void excluirStorageUsuario(String userId, androidx.appcompat.app.AlertDialog progressDialog) {
        if (isFinishing() || isDestroyed()) return;

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage("Excluindo fotos...");
        }

        try {
            com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String storagePath = estado + "/" + municipio + "/reclamacoesUsuario/" + userId;
            StorageReference userStorageRef = storageRef.child(storagePath);

            userStorageRef.listAll()
                    .addOnSuccessListener(listResult -> {
                        List<StorageReference> items = listResult.getItems();
                        if (items.isEmpty()) {
                            excluirAutenticacaoUsuario(progressDialog);
                            return;
                        }

                        final int[] contador = {0};
                        for (StorageReference item : items) {
                            item.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        contador[0]++;
                                        if (contador[0] == items.size()) {
                                            excluirAutenticacaoUsuario(progressDialog);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        contador[0]++;
                                        if (contador[0] == items.size()) {
                                            excluirAutenticacaoUsuario(progressDialog);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> excluirAutenticacaoUsuario(progressDialog));
        } catch (Exception e) {
            excluirAutenticacaoUsuario(progressDialog);
        }
    }

    private void excluirAutenticacaoUsuario(androidx.appcompat.app.AlertDialog progressDialog) {
        if (isFinishing() || isDestroyed()) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            return;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage("Finalizando exclusão...");
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        if (task.isSuccessful()) {
                            // Limpar SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                            prefs.edit().clear().apply();

                            if (!isFinishing() && !isDestroyed()) {
                                Alertas.showAlertDialog(
                                        ProfileActivity.this,
                                        "Conta Excluída",
                                        "Sua conta foi excluída com sucesso!\n\n" +
                                                "Todos os seus dados foram removidos do sistema.",
                                        (dialog, which) -> {

                                            if("agentes".equals(perfil)){
                                                msg_admin_exclusao_conta_agente();

                                            } else if ("admins".equals(perfil)){

                                                msg_admin_exclusao_conta_admin();

                                            }

                                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                );
                            }
                        } else {
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(this, "Erro ao excluir conta", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void msg_admin_exclusao_conta_admin() {

        // Antes de avisar os outros admins, tira ESTE usuário do tópico "admins"
        // e coloca no de "agentes" — assim ele não recebe o próprio aviso de exclusão
        String topicoAdmins = TopicHelper.getAdminsTopic(this);
        String topicoAgentes = TopicHelper.getAgentesTopic(this);

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicoAdmins);
        FirebaseMessaging.getInstance().subscribeToTopic(topicoAgentes);

        ArrayList<String> topicos = new ArrayList<>();
        topicos.add(topicoAdmins);

        HashMap<String, Object> notificacaoAdmin = new HashMap<>();
        notificacaoAdmin.put("titulo", "Exclusão de conta");
        notificacaoAdmin.put("mensagem", "Exclusão de conta de administrador: " + nomeSalvoSharedOuFirebase);
        notificacaoAdmin.put("topicos", topicos);

        FirebaseDatabase.getInstance()
                .getReference("notifications_queue")
                .push()
                .setValue(notificacaoAdmin);
    }

    private void msg_admin_exclusao_conta_agente() {
        String topicoAdmin = TopicHelper.getAdminsTopic(this);

        ArrayList<String> topicos = new ArrayList<>();
        topicos.add(topicoAdmin);

        HashMap<String, Object> notificacao = new HashMap<>();
        notificacao.put("titulo", "Exclusão de conta");

        // ✅ CORREÇÃO: Verifica se funcao existe antes de usar
        String mensagem;
        if (funcao != null && !funcao.isEmpty()) {
            mensagem = "Exclusão de conta: " + nomeSalvoSharedOuFirebase + ", " + funcao;
        } else {
            mensagem = "Exclusão de conta: " + nomeSalvoSharedOuFirebase;
        }
        notificacao.put("mensagem", mensagem);
        notificacao.put("topicos", topicos);

        FirebaseDatabase.getInstance()
                .getReference("notifications_queue")
                .push()
                .setValue(notificacao);
    }
}