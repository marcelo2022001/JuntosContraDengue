package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivitySubstituirVideoInicialBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class SubstituirVideoInicialActivity extends AppCompatActivity {
    private androidx.appcompat.app.AlertDialog loadingDialog;
    private ActivitySubstituirVideoInicialBinding binding;
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private String estado, municipio; private Uri uri_video;
    private ActivityResultLauncher<Intent> videoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        boolean isConnected = NetworkUtils.isNetworkAvailable(SubstituirVideoInicialActivity.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "substituir_video_inicia_app");
            startActivity(itente);

        }

        binding = ActivitySubstituirVideoInicialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar tbSubstituirVideo = binding.tbSubstituirVideoInicial;
        setSupportActionBar(tbSubstituirVideo);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Recuperar dados do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", "");
        municipio = prefs.getString("municipio", "");

        // Configurar o seletor de vídeo
        videoLauncher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(),
                result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                uri_video = result.getData().getData();
            }
            exibirVideoSelecionado();
        }
    );

        binding.ltEscolherVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*"); videoLauncher.launch(intent); });

        binding.btnSalvarVideo.setOnClickListener(v -> {
        if (uri_video != null) { enviarVideoEAtualizarBanco();
        } else {
            Toast.makeText(this, "Selecione um vídeo primeiro",
                    Toast.LENGTH_SHORT).show(); } });
    binding.btnCancelSalvarVideo.setOnClickListener(v -> finish());
}
private void exibirVideoSelecionado() {
    binding.vvEscolherVideo.setVisibility(View.VISIBLE);
    binding.vvEscolherVideo.setVideoURI(uri_video);
    binding.vvEscolherVideo.start();
    binding.vvEscolherVideo.setOnPreparedListener(mp -> mp.setLooping(true));
}
    // Opcional: inicia o preview
    private void enviarVideoEAtualizarBanco() {

        String nomeArquivo = "video_inicia_app.mp4";

        StorageReference videoRef = storageReference
                .child(estado)
                .child(municipio)
                .child("videoIniciaApp")
                .child(nomeArquivo);

        showLoading();

        videoRef.putFile(uri_video)

                .continueWithTask(task -> {

                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    return videoRef.getDownloadUrl();
                })

                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String novaUrl = task.getResult().toString();
                        atualizarUrlNoRealtime(novaUrl);

                    } else {

                        hideLoading();

                        Toast.makeText(this,
                                "Erro ao enviar vídeo",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void atualizarUrlNoRealtime(String url) {
    // .setValue(url) substitui o valor antigo pela nova URL no banco de dados
        databaseReference.child("cadastros")
                .child(estado)
                .child(municipio)
                .child("config")
                .child("video_inicia_app")
                .setValue(url)
                .addOnSuccessListener(aVoid -> { hideLoading();
                    Toast.makeText(this, "Vídeo substituído com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                });
}
private void showLoading() {
    if (loadingDialog == null) {
        androidx.appcompat.app.AlertDialog
                .Builder builder = new androidx.appcompat.app.
                AlertDialog.Builder(this);
        View view = getLayoutInflater()
                .inflate(R.layout.dialog_loading_add_video, null);
        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();
    } loadingDialog.show();
}
private void hideLoading() {
    if (loadingDialog != null && loadingDialog.isShowing()) {
        loadingDialog.dismiss(); }
}


    }
