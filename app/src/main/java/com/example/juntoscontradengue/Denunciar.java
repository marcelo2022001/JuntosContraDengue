package com.example.juntoscontradengue;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.juntoscontradengue.databinding.ActivityDenunciarBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.TopicHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Denunciar extends AppCompatActivity {
    private ActivityResultLauncher<String[]> permissionsLauncher;
    private ActivityDenunciarBinding binding;
    private AlertDialog loadingDialog;
    private String estado;
    private String municipio;
    private String uid;
    private String tokenFCM;
    private boolean usuarioQuerSeIdentificar = false;
    private boolean isCapturingVideo = false;
    boolean isConnected;
    String nome, telefone;
    private Uri imgUri1, imgUri2, imgUri3, videoUri, imgUriTemp;
    private int imagemSelecionadaIndex = 0;
    private Bitmap thumbnailVideoBitmap;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> videoLauncher;
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseDatabase databaseMunicipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isConnected = NetworkUtils.isNetworkAvailable(Denunciar.this);

        verificarPermissoesIniciais();
        
        binding = ActivityDenunciarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar a Toolbar como ActionBar
        setSupportActionBar(binding.tbDenuncias);
        // Configurar o título e a seta de voltar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Denúncias"); // Título personalizado
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Mostra a seta de voltar
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        inicializarDados();
        configurarLaunchers();
        configurarCliques();
        configurarBackPressed();
        configPerfil();
    }

    private void verificarPermissoesIniciais() {
        boolean temCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        Log.d("Denunciar", "Permissão da câmera ao iniciar: " + temCamera);

        if (!temCamera) {
            // Solicita permissão antecipadamente
            verificarPermissoesEAbrirCamera(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mostrarDialogoSair(); // Ou finish() diretamente
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verificarPermissoesEAbrirCamera(boolean isForVideo) {
        this.isCapturingVideo = isForVideo;

        List<String> missingPermissions = new ArrayList<>();

        // PERMISSÃO DA CÂMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.CAMERA);
        }

        // PERMISSÃO DE ÁUDIO PARA VÍDEO
        if (isForVideo && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.RECORD_AUDIO);
        }

        // PERMISSÕES DE ARMAZENAMENTO POR VERSÃO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            // Para Android 13+, usa READ_MEDIA_IMAGES e READ_MEDIA_VIDEO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (isForVideo && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else { // Android 12 ou inferior
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Android 9 ou inferior
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        }

        if (missingPermissions.isEmpty()) {
            if (isForVideo) abrirGravadorVideo();
            else abrirCamera();
        } else {
            permissionsLauncher.launch(missingPermissions.toArray(new String[0]));
        }
    }

    private void inicializarDados() {
        uid = FirebaseAuth.getInstance().getUid();
        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = Objects.requireNonNull(prefs.getString("estado", "")).toLowerCase();
        municipio = Objects.requireNonNull(prefs.getString("municipio", "")).toLowerCase();

        SharedPreferences prefsData = getSharedPreferences("UserData", MODE_PRIVATE);
        nome = Objects.requireNonNull(prefsData.getString("nome", ""));
        telefone = Objects.requireNonNull(prefsData.getString("telefone", ""));

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + ".firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        Log.d("FIREBASE_DB", "Estado: " + estado);
        Log.d("FIREBASE_DB", "Município: " + municipio);
        Log.d("FIREBASE_DB", "URL banco: " + urlBanco);

        if (!isConnected){
            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "denunciar");
            startActivity(itente);

        } else if(uid == null) {
            Intent intent = new Intent(Denunciar.this, TelaLoguin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void configurarBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarDialogoSair();
            }
        });
    }

    private void configPerfil() {
        SharedPreferences prefsPerfil = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String perfil = prefsPerfil.getString("perfil", null);

        if (!"usuarios".equals(perfil)) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Denunciar.this, TelaLoguin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Busca o total de reclamações no banco
            verificaTotalReclamacoes();

            // LÓGICA CRÍTICA:
            // Se já existe qualquer imagem ou vídeo selecionado,
            // significa que o usuário já passou pela identificação e está preenchendo o formulário.
            if (imgUri1 != null || imgUri2 != null || imgUri3 != null || videoUri != null) {
                mostrarFormulario();
            } else {
                // Só mostra a identificação se for o início real da denúncia
                mostrarFragmentIdentificacao();
            }
        }
    }

    private void configurarLaunchers() {
        // Galeria de Imagens
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == Activity.RESULT_OK && r.getData() != null) {
                atribuirUri(r.getData().getData());
            }
        });

        // Câmera de Fotos
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // A foto foi salva em imgUriTemp
                        atribuirUri(imgUriTemp);
                    } else {
                        // Limpa o arquivo temporário se cancelou
                        if (imgUriTemp != null) {
                            try {
                                getContentResolver().delete(imgUriTemp, null, null);
                            } catch (Exception e) {
                                Log.e("Denunciar", "Erro ao deletar arquivo temporário", e);
                            }
                        }
                    }
                }
        );

        // Seleção/Gravação de Vídeo — registrado sempre, sem checagem de versão
        videoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == Activity.RESULT_OK && r.getData() != null) {
                videoUri = r.getData().getData();

                binding.videoReclamacao.setVisibility(View.GONE);
                binding.thumbnail.setVisibility(View.VISIBLE);
                binding.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

                gerarThumbnailVideo(videoUri);
            }
        });

        // Permissões múltiplas
        permissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Log.d("Denunciar", "Resultado das permissões: " + result);

                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        Log.d("Denunciar", "Permissão " + entry.getKey() + ": " + entry.getValue());
                        if (!entry.getValue()) {
                            allGranted = false;
                        }
                    }

                    if (allGranted) {
                        Log.d("Denunciar", "Todas as permissões concedidas");
                        if (isCapturingVideo) abrirGravadorVideo();
                        else abrirCamera();
                    } else {
                        Log.e("Denunciar", "Algumas permissões foram negadas");
                        // MOSTRA UM DIÁLOGO EXPLICATIVO
                        new AlertDialog.Builder(this)
                                .setTitle("Permissões necessárias")
                                .setMessage("Para usar a câmera, você precisa conceder as permissões solicitadas. " +
                                        "Vá em Configurações > Apps > Juntos Contra Dengue > Permissões e ative manualmente.")
                                .setPositiveButton("Configurações", (d, w) -> {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }
                });
    }

    private void gerarThumbnailVideo(Uri videoUri) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            try {
                retriever.setDataSource(this, videoUri); // <-- corrigido: era "uri"
                Bitmap bitmap = retriever.getFrameAtTime(1000000);
                thumbnailVideoBitmap = bitmap;
                binding.thumbnail.setImageBitmap(bitmap);
            } catch (Exception e) {
                thumbnailVideoBitmap = null;
                binding.thumbnail.setImageResource(R.drawable.camera_video);
            } finally {
                try {
                    retriever.release();
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void atribuirUri(Uri uri) {
        if (uri == null) return;
        if (imagemSelecionadaIndex == 1) {
            imgUri1 = uri;
            binding.imgCamReclamacao1.setImageURI(uri);
        } else if (imagemSelecionadaIndex == 2) {
            imgUri2 = uri;
            binding.imgCamReclamacao2.setImageURI(uri);
        } else if (imagemSelecionadaIndex == 3) {
            imgUri3 = uri;
            binding.imgCamReclamacao3.setImageURI(uri);
        }
    }

    private void configurarCliques() {
        binding.imgCamReclamacao1.setOnClickListener(v -> gerenciarCliqueImagem(1, imgUri1));
        binding.imgCamReclamacao2.setOnClickListener(v -> gerenciarCliqueImagem(2, imgUri2));
        binding.imgCamReclamacao3.setOnClickListener(v -> gerenciarCliqueImagem(3, imgUri3));

        binding.thumbnail.setOnClickListener(v -> {
            if (videoUri == null) showVideoPickerOptions();
            else alertaOpcoesVideo();
        });

        binding.btnEnviarReclamacao.setOnClickListener(v -> validarEEnviar());
        binding.btnCancelarReclamacao.setOnClickListener(v -> mostrarDialogoSair());
    }

    private void gerenciarCliqueImagem(int index, Uri uri) {
        imagemSelecionadaIndex = index;
        if (uri == null) showImagePickerOptions();
        else alertaOpcoesImagem(uri, index);
    }

    private void showImagePickerOptions() {
        String[] options = {"Tirar Foto", "Escolher da Galeria"};
        new AlertDialog.Builder(this).setTitle("Adicionar Imagem").setItems(options, (dialog, which) -> {
            if (which == 0) {
                verificarPermissoesEAbrirCamera(false);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                galleryLauncher.launch(intent);
            }
        }).show();
    }

    private void abrirCamera() {
        Log.d("Denunciar", "Tentando abrir a câmera...");

        // VERIFICA SE O DISPOSITIVO TEM CÂMERA
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "Seu dispositivo não possui câmera", Toast.LENGTH_SHORT).show();
            return;
        }

        // CRIA O ARQUIVO TEMPORÁRIO
        File photoFile;
        try {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null && !storageDir.exists()) {
                boolean dirCreated = storageDir.mkdirs();
                if (!dirCreated) {
                    Log.e("Denunciar", "Não foi possível criar o diretório: " + storageDir.getAbsolutePath());
                    Toast.makeText(this, "Erro ao criar diretório para fotos", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // File.createTempFile NUNCA retorna null - sempre lança IOException em caso de erro
            photoFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            Log.d("Denunciar", "Arquivo criado: " + photoFile.getAbsolutePath());

        } catch (IOException ex) {
            Log.e("Denunciar", "Erro ao criar arquivo: " + ex.getMessage());
            Toast.makeText(this, "Erro ao criar arquivo para foto", Toast.LENGTH_SHORT).show();
            return;
        }

        // USA FILE PROVIDER PARA OBTER A URI
        imgUriTemp = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".fileprovider",
                photoFile);

        Log.d("Denunciar", "URI da foto: " + imgUriTemp.toString());

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUriTemp);

        // CONCEDE PERMISSÃO TEMPORÁRIA
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // VERIFICA SE HÁ APP DE CÂMERA
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Nenhum aplicativo de câmera encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVideoPickerOptions() {
        String[] options = {"Gravar Vídeo", "Escolher da Galeria"};
        new AlertDialog.Builder(this).setTitle("Adicionar Vídeo").setItems(options, (d, w) -> {
            if (w == 0) {
                verificarPermissoesEAbrirCamera(true);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
                videoLauncher.launch(intent);
            }
        }).show();
    }

    private void abrirGravadorVideo() {
        Log.d("Denunciar", "Tentando abrir gravador de vídeo...");

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // LIMITA O TAMANHO DO VÍDEO (OPCIONAL)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60); // 60 segundos
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // Qualidade alta

        if (intent.resolveActivity(getPackageManager()) != null) {
            videoLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Nenhum aplicativo de vídeo encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void alertaOpcoesImagem(Uri uri, int index) {
        new AlertDialog.Builder(this).setTitle("Imagem Selecionada")
                .setPositiveButton("Visualizar", (d, w) -> abrirFullscreen(uri, "imagem"))
                .setNegativeButton("Trocar", (d, w) -> showImagePickerOptions())
                .setNeutralButton("Remover", (d, w) -> limparImagem(index)).show();
    }

    private void alertaOpcoesVideo() {
        new AlertDialog.Builder(this).setTitle("Vídeo Selecionado")
                .setPositiveButton("Visualizar", (d, w) -> abrirFullscreen(videoUri, "video"))
                .setNegativeButton("Trocar", (d, w) -> showVideoPickerOptions())
                .setNeutralButton("Remover", (d, w) -> {
                    videoUri = null;
                    binding.thumbnail.setImageResource(R.drawable.camera_video);
                    binding.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }).show();
    }

    private void limparImagem(int index) {
        if (index == 1) {
            imgUri1 = null;
            binding.imgCamReclamacao1.setImageResource(R.drawable.camera);
        } else if (index == 2) {
            imgUri2 = null;
            binding.imgCamReclamacao2.setImageResource(R.drawable.camera);
        } else if (index == 3) {
            imgUri3 = null;
            binding.imgCamReclamacao3.setImageResource(R.drawable.camera);
        }
    }

    private void abrirFullscreen(Uri uri, String tipo) {
        startActivity(new Intent(this, FullscreenActivity.class)
                .putExtra("urlMidia", uri.toString()).putExtra("tipo", tipo));
    }

    private void validarEEnviar() {
        String endereco = binding.edtEnderecoReclamacao.getText().toString().trim();
        String numero = binding.edtNumeroReclamacao.getText().toString().trim();
        String ref = binding.edtReferenciaReclamacao.getText().toString().trim();
        String desc = binding.edtReclamacao.getText().toString().trim();

        if (endereco.isEmpty() || numero.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();
        if (usuarioQuerSeIdentificar) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                tokenFCM = task.isSuccessful() ? task.getResult() : null;
                iniciarUploads(endereco, numero, ref, desc);
            });
        } else {
            //Mesmo não se identificando pego o token para responder
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                tokenFCM = task.isSuccessful() ? task.getResult() : null;
                iniciarUploads(endereco, numero, ref, desc);
            });
        }
    }

    private void iniciarUploads(String end, String num, String ref, String desc) {
        String idReclamacao = UUID.randomUUID().toString();

        // Posições FIXAS: 0=imagem1, 1=imagem2, 2=imagem3, 3=vídeo
        // (não compactar — as telas de visualização assumem que midia_3 é sempre o vídeo)
        Uri[] midiasPorPosicao = { imgUri1, imgUri2, imgUri3, videoUri };

        List<Integer> indicesParaUpload = new ArrayList<>();
        for (int i = 0; i < midiasPorPosicao.length; i++) {
            if (midiasPorPosicao[i] != null) indicesParaUpload.add(i);
        }

        if (indicesParaUpload.isEmpty()) {
            salvarNoRealtime(idReclamacao, new HashMap<>(), end, num, ref, desc);
            return;
        }

        Map<String, Object> urlsMap = new HashMap<>();

        int totalUploads = indicesParaUpload.size() + (videoUri != null ? 1 : 0);
        AtomicInteger concluidos = new AtomicInteger(0);

        Runnable checarFinalizacao = () -> {
            if (concluidos.incrementAndGet() == totalUploads) {
                salvarNoRealtime(idReclamacao, urlsMap, end, num, ref, desc);
            }
        };

        StorageReference pastaReclamacao = storageReference.child(estado).child(municipio)
                .child("reclamacoes/" + uid + "/" + idReclamacao);

        for (int index : indicesParaUpload) {
            Uri uri = midiasPorPosicao[index];
            boolean isVideo = (index == 3); // posição fixa do vídeo
            String extensao = isVideo ? ".mp4" : ".jpg";
            String chave = "midia_" + index; // agora sempre corresponde à posição original

            StorageReference fileRef = pastaReclamacao.child(UUID.randomUUID() + extensao);

            fileRef.putFile(uri).continueWithTask(task -> fileRef.getDownloadUrl()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    urlsMap.put(chave, task.getResult().toString());
                }
                checarFinalizacao.run();

                if (isVideo) {
                    uploadThumbnailVideo(pastaReclamacao, chave, urlsMap, checarFinalizacao);
                }
            });
        }
    }

    private void uploadThumbnailVideo(StorageReference pastaReclamacao, String chaveVideo,
                                      Map<String, Object> urlsMap, Runnable checarFinalizacao) {

        if (thumbnailVideoBitmap == null) {
            // Sem thumbnail gerada — não trava o envio, só segue sem ela
            checarFinalizacao.run();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnailVideoBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] dados = baos.toByteArray();

        StorageReference thumbRef = pastaReclamacao.child(chaveVideo + "_thumb.jpg");

        thumbRef.putBytes(dados)
                .continueWithTask(task -> thumbRef.getDownloadUrl())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        urlsMap.put(chaveVideo + "_thumb", task.getResult().toString());
                    }
                    checarFinalizacao.run();
                });
    }

    private void salvarNoRealtime(String id, Map<String, Object> urls, String end, String num, String ref, String desc) {

        if (estado == null || estado.isEmpty() || estado.equals("default")) {
        Toast.makeText(this, "Não foi possível salvar sua reclamação", Toast.LENGTH_SHORT).show();
        return;
        } else {
            String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + ".firebaseio.com/";
            Log.d("FIREBASE_DB", "URL para verificar reclamações: " + urlBanco);
        }

        DatabaseReference dbRef = databaseMunicipio.getReference()
                .child("reclamacoes")
                .child(uid)
                .child(id);

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("idReclamacao", id);
        data.put("data_envio", System.currentTimeMillis());
        data.put("endereco_reclamacao", end);
        data.put("num_casa_reclamacao", num);
        data.put("referencia", ref);
        data.put("reclamacao", desc);
        data.put("visivel_agente", true);
        data.put("status", "Aguardando resposta");
        data.put("midia_reclamacoes", urls);
        data.put("tokenFCM", tokenFCM);

        if (usuarioQuerSeIdentificar) {
            data.put("reclamante", nome);
            data.put("telefone_reclamante", telefone);

            // Adicionar dados do SharedPreferences se necessário aqui
        } else {
            data.put("reclamante", "Anônimo");
            data.put("telefone_reclamante", "Anônimo");
        }

        dbRef.setValue(data).addOnSuccessListener(aVoid -> {

            String topicoAgentes = TopicHelper.getAgentesTopic(this);
            String topicoAdmins = TopicHelper.getAdminsTopic(this);

            ArrayList<String> topicos = new ArrayList<>();
            topicos.add(topicoAgentes);
            topicos.add(topicoAdmins);

            HashMap<String, Object> notificacao = new HashMap<>();
            notificacao.put("titulo", "Nova Reclamação");
            notificacao.put("mensagem",  desc);
            notificacao.put("topicos", topicos);

            FirebaseDatabase.getInstance()
                    .getReference("notifications_queue")
                    .push()
                    .setValue(notificacao);

            hideLoading();
            Toast.makeText(this, "Enviado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            hideLoading();
            Toast.makeText(this, "Erro ao salvar.", Toast.LENGTH_SHORT).show();
        });
    }

    // Métodos de UI e Firebase
    private void mostrarFragmentIdentificacao() {
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.scrowReclamacao.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DenunciaIdentificacaoFragment()).commit();
    }

    public void mostrarFormulario() {
        binding.fragmentContainer.setVisibility(View.GONE);
        binding.scrowReclamacao.setVisibility(View.VISIBLE);
    }

    public void usuarioEscolheuIdentificar(boolean desejaIdentificar) {
        this.usuarioQuerSeIdentificar = desejaIdentificar;
    }

    private void verificaTotalReclamacoes() {
        if (estado == null || municipio == null || estado.isEmpty() || municipio.isEmpty() || uid == null) {
            Log.e("Denunciar", "Estado, município ou UID nulos ao tentar buscar total de reclamações.");
            return;
        }

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + ".firebaseio.com/";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(urlBanco)
                .getReference()
                .child("reclamacoes")
                .child(uid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long total = snapshot.getChildrenCount();

                        if (total >= 3) {
                            hideLoading();
                            Toast.makeText(Denunciar.this,
                                    "Você já possui o limite máximo de 3 denúncias. Exclua 1 ou mais denúncias para continuar.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> tokenFCM = task.isSuccessful() ? task.getResult() : null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideLoading();
                    }
                });
    }

    private void showLoading() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // mas o 'false' garante que o layout não seja anexado agora (o Builder fará isso).
            View view = getLayoutInflater().inflate(R.layout.dialog_loading, binding.getRoot(), false);

            builder.setView(view);
            builder.setCancelable(false);
            loadingDialog = builder.create();
        }
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
    }

    private void mostrarDialogoSair() {
        new AlertDialog.Builder(this).setTitle("Sair").setMessage("Deseja cancelar a denúncia?")
                .setPositiveButton("Sim", (d, w) -> finish()).setNegativeButton("Não", null).show();
    }

    // Pode colar logo abaixo do seu método inicializarDados() ou no fim da classe

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardamos as URIs para que elas não se percam quando a câmara abrir
        if (imgUri1 != null) outState.putString("imgUri1", imgUri1.toString());
        if (imgUri2 != null) outState.putString("imgUri2", imgUri2.toString());
        if (imgUri3 != null) outState.putString("imgUri3", imgUri3.toString());
        if (videoUri != null) outState.putString("videoUri", videoUri.toString());

        // Guardamos também se o utilizador já escolheu identificar-se
        outState.putBoolean("usuarioQuerSeIdentificar", usuarioQuerSeIdentificar);

        // SALVA A URI TEMPORÁRIA DA CÂMERA
        if (imgUriTemp != null) {
            outState.putString("imgUriTemp", imgUriTemp.toString());
        }

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey("imgUri1")) {
            imgUri1 = Uri.parse(savedInstanceState.getString("imgUri1"));
            binding.imgCamReclamacao1.setImageURI(imgUri1);
        }
        if (savedInstanceState.containsKey("imgUri2")) {
            imgUri2 = Uri.parse(savedInstanceState.getString("imgUri2"));
            binding.imgCamReclamacao2.setImageURI(imgUri2);
        }
        if (savedInstanceState.containsKey("imgUri3")) {
            imgUri3 = Uri.parse(savedInstanceState.getString("imgUri3"));
            binding.imgCamReclamacao3.setImageURI(imgUri3);
        }

        // RECUPERA A URI TEMPORÁRIA DA CÂMERA:
        if (savedInstanceState.containsKey("imgUriTemp")) {
            imgUriTemp = Uri.parse(savedInstanceState.getString("imgUriTemp"));
        }

        if (savedInstanceState.containsKey("videoUri")) {
            videoUri = Uri.parse(savedInstanceState.getString("videoUri"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                restaurarThumbnailVideo(videoUri);
            }
        }

        usuarioQuerSeIdentificar = savedInstanceState.getBoolean("usuarioQuerSeIdentificar");
    }

    // Método auxiliar para ajudar na restauração do vídeo
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void restaurarThumbnailVideo(Uri uri) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            try {
                retriever.setDataSource(this, uri);

                // Pega o frame de 1 segundo
                Bitmap bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                if (bitmap != null) {
                    thumbnailVideoBitmap = bitmap;
                    binding.thumbnail.setImageBitmap(bitmap);
                    binding.thumbnail.setVisibility(View.VISIBLE);
                    binding.videoReclamacao.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("DengueApp", "Erro ao carregar vídeo: " + e.getMessage());
                binding.thumbnail.setImageResource(R.drawable.camera_video);
            } finally {
                // O bloco finally sempre executa, garantindo que o recurso seja liberado
                try {
                    retriever.release();
                } catch (Exception e) {
                    // No API 23-28, o release pode não lançar IOException,
                    // mas usamos Exception genérica por segurança.
                    Log.e("DengueApp", "Erro ao liberar retriever", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpa arquivos temporários
        if (imgUriTemp != null) {
            try {
                getContentResolver().delete(imgUriTemp, null, null);
            } catch (Exception e) {
                Log.e("Denunciar", "Erro ao limpar arquivo temporário", e);
            }
        }
    }
}