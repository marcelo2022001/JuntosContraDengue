package com.example.juntoscontradengue;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.databinding.ActivityResponderDenunciaBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ResponderDenunciaActivity extends AppCompatActivity {
    private boolean tokenCarregado = false;
    private ActivityResponderDenunciaBinding binding;
    private TextInputLayout inputLayoutRespReclamacao;
    private TextInputEditText inputEditTextRespReclamacao;
    private ProgressBar pBResponderReclamacao;
    private ImageView img_1, img_2, img_3, img_4;
    private Uri imgUri1, imgUri2, imgUri3, imgUri4;
    private Uri currentPhotoUri; // URI temporária para a foto da câmera
    private ImageView imagemAtiva; // Referência da View que recebeu o clique
    private Button btnEnviarRespostaReclamacao;
    String estado, municipio, status_reclamacao, reclamacao, uuid, id, nome, token, dataFormatada;
    Long dataReclamacao;

    // PERMISSÃO
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            takePhotoWithLowMemoryUsage();
                        } else {
                            Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show();
                        }
                    });

    // LAUNCHER PARA GALERIA
    // LAUNCHER PARA GALERIA — Photo Picker (só mostra imagens)
    private final ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            aplicarImagemNaEscada(uri);
                        }
                    });

    // LAUNCHER PARA CÂMERA
    private final ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {

                    if (currentPhotoUri != null) {

                        aplicarImagemNaEscada(currentPhotoUri);

                    } else {

                        Toast.makeText(
                                this,
                                "Erro ao carregar foto",
                                Toast.LENGTH_SHORT
                        ).show();

                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResponderDenunciaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inicializarComponentes();
        configurarBasicos();
        buscaToken();
        setListeners();
        verificarStatus(status_reclamacao);
        Toolbar toolbar = binding.toolbarVisualReclaUsuario;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void configurarBasicos() {
        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);
        nome = prefs.getString("nome", null);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            status_reclamacao = extras.getString("status_reclamacao");
            uuid = extras.getString("UUID");
            id = extras.getString("ID");
        }
    }

    private void buscaToken() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("reclamacoes")
                .child(uuid)
                .child(id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                token = snapshot.child("tokenFCM").getValue(String.class);
                tokenCarregado = token != null && !token.isEmpty();

                if(tokenCarregado){
                    btnEnviarRespostaReclamacao.setEnabled(true);
                    btnEnviarRespostaReclamacao.setOnClickListener(v -> salvarResposta());

                }

                dataReclamacao = snapshot.child("data_envio").getValue(Long.class);
                reclamacao = snapshot.child("resposta_reclamacao").getValue(String.class);
                if (reclamacao != null && !reclamacao.isEmpty()) {

                    inputEditTextRespReclamacao.setText(reclamacao);

                }
                Log.d("TOKEN", token);
                if (dataReclamacao != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dataFormatada = sdf.format(new Date(dataReclamacao));

                    Log.d("DATA", dataFormatada);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        inputLayoutRespReclamacao = binding.txtInputLayoutRespostaReclamacao;
        inputEditTextRespReclamacao = binding.txtInputEditRespostaReclamacao;

        pBResponderReclamacao = binding.pbRespostaReclamacao;

        img_1 = binding.image1ResponderReclamacao;
        img_2 = binding.image2ResponderReclamacao;
        img_3 = binding.image3ResponderReclamacao;
        img_4 = binding.image4ResponderReclamacao;
        btnEnviarRespostaReclamacao = binding.btnResponderReclamacaoUsuario;
    }

    private void setListeners() {
        View.OnClickListener imageClickListener = v -> {
            ImageView clicada = (ImageView) v;
            // Descobre qual URI pertence à imagem clicada
            Uri uriAtual = null;
            if (clicada.getId() == img_1.getId()) uriAtual = imgUri1;
            else if (clicada.getId() == img_2.getId()) uriAtual = imgUri2;
            else if (clicada.getId() == img_3.getId()) uriAtual = imgUri3;
            else if (clicada.getId() == img_4.getId()) uriAtual = imgUri4;

            processarCliqueImagem(uriAtual, clicada);
        };

        img_1.setOnClickListener(imageClickListener);
        img_2.setOnClickListener(imageClickListener);
        img_3.setOnClickListener(imageClickListener);
        img_4.setOnClickListener(imageClickListener);

        btnEnviarRespostaReclamacao.setEnabled(false);

    }

    private void processarCliqueImagem(Uri uriAtual, ImageView imageViewClicada) {

        this.imagemAtiva = imageViewClicada;

        if (uriAtual != null) {

            new AlertDialog.Builder(this)
                    .setItems(
                            new String[]{
                                    "Visualizar Foto",
                                    "Trocar Foto"
                            },
                            (dialog, which) -> {

                                if (which == 0) {
                                    abrirFullscreen(uriAtual);
                                } else {
                                    showSeletorMidia();
                                }

                            })
                    .show();

        } else {

            showSeletorMidia();

        }
    }

    private void showSeletorMidia() {

        String[] options = {
                "Tirar Foto",
                "Escolher da Galeria"
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(options, (dialogo, which) -> {

                    if (which == 0) {

                        verificarPermissaoEAbrirCamera();

                    } else {

                        abrirGaleria();

                    }

                })
                .create();

        dialog.setOnShowListener(d -> dialog.getWindow()
                .setDimAmount(0.4f));

        dialog.show();
    }

    private void aplicarImagemNaEscada(Uri uri) {
        if (uri != null && imagemAtiva != null) {
            Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(imagemAtiva);

            // Salva a URI e libera a próxima imagem da fila
            if (imagemAtiva.getId() == img_1.getId()) {
                imgUri1 = uri;
                img_2.setVisibility(View.VISIBLE);
            } else if (imagemAtiva.getId() == img_2.getId()) {
                imgUri2 = uri;
                img_3.setVisibility(View.VISIBLE);
            } else if (imagemAtiva.getId() == img_3.getId()) {
                imgUri3 = uri;
                img_4.setVisibility(View.VISIBLE);
            } else if (imagemAtiva.getId() == img_4.getId()) {
                imgUri4 = uri;
            }
        }
    }

    private void verificarPermissaoEAbrirCamera() {
        boolean temCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        if (!temCamera) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        takePhotoWithLowMemoryUsage();
    }

    private void takePhotoWithLowMemoryUsage() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;

        try {

            String timeStamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
            ).format(new Date());


            File storageDir = getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES
            );

                if (storageDir != null && !storageDir.exists()) {
                    boolean criado = storageDir.mkdirs();

                    if (!criado) {
                        Log.e("CAMERA", "Não foi possível criar pasta");
                    }
                }


            photoFile = File.createTempFile(
                    "JPEG_" + timeStamp + "_",
                    ".jpg",
                    storageDir
            );


        } catch (IOException ex) {

            Log.e(
                    "CAMERA",
                    "Erro ao criar arquivo",
                    ex
            );

        }


        if (photoFile != null) {


            currentPhotoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );


            cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    currentPhotoUri
            );


            // Permite a câmera gravar no arquivo
            cameraIntent.addFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
            );


            takePhotoLauncher.launch(cameraIntent);
        }
    }

    private void abrirGaleria() {
        pickImageLauncher.launch(new androidx.activity.result.PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }


    private void abrirFullscreen(Uri uri) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra("urlMidia", uri.toString());
        intent.putExtra("tipo", "image");
        startActivity(intent);
    }

    private void verificarStatus(String status) {
        if (status != null && !status.equals("Aguardando resposta")) {
            inputLayoutRespReclamacao.setEnabled(false);

            // Desabilita cliques nas imagens se já foi respondido
            img_1.setClickable(false);
            img_2.setClickable(false);
            img_3.setClickable(false);
            img_4.setClickable(false);
        }
    }

    private void salvarResposta() {

        btnEnviarRespostaReclamacao.setEnabled(false);

        if (!tokenCarregado) {

            Toast.makeText(
                    this,
                    "Aguarde carregar os dados do usuário.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (!"Aguardando resposta".equals(status_reclamacao)) {
            Toast.makeText(this, "Reclamação já respondida!", Toast.LENGTH_LONG).show();
            btnEnviarRespostaReclamacao.setEnabled(false);
            return;
        }
        String respostaText = Objects.requireNonNull(inputEditTextRespReclamacao.getText()).toString();

        if (respostaText.isEmpty()) {
            Toast.makeText(this, "Escreva uma resposta antes de enviar.", Toast.LENGTH_SHORT).show();
            return;
        }

        pBResponderReclamacao.setVisibility(View.VISIBLE);


        // Lista de URIs para upload (apenas as que não são nulas)
        Uri[] uris = {imgUri1, imgUri2, imgUri3, imgUri4};
        Map<String, Object> urlsMap = new HashMap<>();
        AtomicInteger uploadsConcluidos = new AtomicInteger(0);
        int totalParaUpload;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            totalParaUpload = (int) Arrays.stream(uris).filter(Objects::nonNull).count();
        } else {
            totalParaUpload = 0;
        }

        if (totalParaUpload == 0) {
            buscarDadosAutorESalvar(respostaText, urlsMap);
        } else {
            for (int i = 0; i < uris.length; i++) {
                if (uris[i] != null) {
                    String nomeArquivo = "img_" + (i + 1) + ".jpg";
                    // Caminho: estado/municipio/reclamacoesusuarios/UUID/ID/nomeArquivo
                    StorageReference ref = FirebaseStorage.getInstance().getReference()
                            .child(estado).child(municipio).child("reclamacoesUsuarios")
                            .child(uuid).child(id).child(nomeArquivo);

                    int finalI = i;
                    ref.putFile(uris[i]).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        urlsMap.put("img_" + (finalI + 1), uri.toString());
                        if (uploadsConcluidos.incrementAndGet() == totalParaUpload) {
                            buscarDadosAutorESalvar(respostaText, urlsMap);
                        }
                    })).addOnFailureListener(e -> Log.e("STORAGE", "Falha no upload", e));
                }
            }
        }
    }

    private void buscarDadosAutorESalvar(String resposta, Map<String, Object> urlsMap) {
        String meuUid = FirebaseAuth.getInstance().getUid();

        if (meuUid == null) {
            pBResponderReclamacao.setVisibility(View.GONE);
            btnEnviarRespostaReclamacao.setEnabled(true);
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("cadastros")
                .child(estado).child(municipio).child("logins");

        // Tenta buscar em ADMINS. Busca nome e a função agente/admin
        rootRef.child("admins").child(meuUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nome = snapshot.child("nome").getValue(String.class);
                    String funcao = snapshot.child("funcao").getValue(String.class);
                    gravarNoBancoFinal(resposta, nome, funcao, urlsMap);
                } else {
                    // Se não é admin, busca em AGENTES
                    rootRef.child("agentes").child(meuUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String nome = snapshot.child("nome").getValue(String.class);
                                String funcao = snapshot.child("funcao").getValue(String.class);
                                gravarNoBancoFinal(resposta, nome, funcao, urlsMap);
                            } else {
                                // NENHUM dos dois — precisa avisar e parar o spinner
                                pBResponderReclamacao.setVisibility(View.GONE);
                                btnEnviarRespostaReclamacao.setEnabled(true);
                                Toast.makeText(ResponderDenunciaActivity.this,
                                        "Erro: usuário não encontrado como admin ou agente.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            pBResponderReclamacao.setVisibility(View.GONE);
                            btnEnviarRespostaReclamacao.setEnabled(true);
                            Toast.makeText(ResponderDenunciaActivity.this,
                                    "Erro ao buscar dados: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pBResponderReclamacao.setVisibility(View.GONE);
                btnEnviarRespostaReclamacao.setEnabled(true);
                Toast.makeText(ResponderDenunciaActivity.this,
                        "Erro ao buscar dados: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void gravarNoBancoFinal(String resposta, String nome, String funcao, Map<String, Object> urlsMap) {
        DatabaseReference reclamacaoRef = FirebaseDatabase.getInstance().getReference("cadastros")
                .child(estado).child(municipio).child("reclamacoes").child(uuid).child(id);

        Map<String, Object> dadosAtualizacao = new HashMap<>();
        dadosAtualizacao.put("resposta_reclamacao", resposta);
        dadosAtualizacao.put("status", "Respondido");
        dadosAtualizacao.put("respondida_por", nome);
        dadosAtualizacao.put("funcao_agente", funcao);
        dadosAtualizacao.put("data_resposta", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        // Adiciona as URLs das imagens de visita se houver
        if (!urlsMap.isEmpty()) {
            dadosAtualizacao.put("midias_resposta", urlsMap);
        }

        reclamacaoRef.updateChildren(dadosAtualizacao).addOnSuccessListener(aVoid -> {
            binding.pbRespostaReclamacao.setVisibility(View.GONE);
            Toast.makeText(this, "Resposta enviada com sucesso!", Toast.LENGTH_SHORT).show();
            enviarPushPorToken(token);

            Intent intent = new Intent(ResponderDenunciaActivity.this, ListarReclamacoesAgentes.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }).addOnFailureListener(e -> {
            binding.pbRespostaReclamacao.setVisibility(View.GONE);
            btnEnviarRespostaReclamacao.setEnabled(true);
            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Modifique o método para receber o token do cidadão que fez a reclamação
    private void enviarPushPorToken(String tokenFCMUsuario) {

        if (tokenFCMUsuario == null || tokenFCMUsuario.isEmpty()) {
            Toast.makeText(this, "Erro: Token do usuário não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setView(new ProgressBar(this))
                .setMessage("Enviando comando para o servidor...")
                .setCancelable(false)
                .show();

        // Referência exata vigiada pela sua Cloud Function
        DatabaseReference refGatilho = FirebaseDatabase.getInstance().getReference("notifications_queue");

        HashMap<String, Object> notificacao = new HashMap<>();
        notificacao.put("titulo", "Resposta sobre reclamação");
        notificacao.put("mensagem", "Sua reclamação feita em: " + dataFormatada + " foi respondida");

        // Troca o nó "topico" por "token" para a Cloud Function saber que é individual
        notificacao.put("token", tokenFCMUsuario);
        Log.d("FCM_ENVIO", "Token: " + tokenFCMUsuario);
        Log.d("FCM_ENVIO", "Data: " + dataFormatada);
        refGatilho.push().setValue(notificacao).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(ResponderDenunciaActivity.this, "Sucesso! Cidadão será notificado.", Toast.LENGTH_LONG).show();
                Log.d("FCM_ENVIO", "Registro criado na fila");
            } else {
                Toast.makeText(ResponderDenunciaActivity.this, "Erro ao gravar no banco.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


