package com.example.juntoscontradengue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.database.classes_database.ClassTrabAgentes;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class UploadTrabAgentes extends AppCompatActivity {

    private final DatabaseReference dbContador = FirebaseDatabase.getInstance().getReference("cadastros");

    private androidx.appcompat.app.AlertDialog loadingDialog;

    String estado, municipio;

    private ImageView uploadImage;

    EditText uploadCaption;

    private Uri imageUri;

    private DatabaseReference totalRefImagens;

    private DatabaseReference databaseReference;

    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    String id_activity;
     private TextView totalUploadDeImagens;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        id_activity = getIntent().getStringExtra("id_activity");

        if (id_activity == null || id_activity.isEmpty()) {
                id_activity = "upload_trab_agentes_agentes";
            } else{
            id_activity = "upload_trab_agentes_admin";
        }


        boolean isConnected = NetworkUtils.isNetworkAvailable(UploadTrabAgentes.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", id_activity);
            startActivity(itente);

        }

        setContentView(R.layout.activity_upload_trab_agentes);

        Toolbar toolbarUploadTrabAgentes = findViewById(R.id.toolbarUploadTrabAgentes);

        setSupportActionBar(toolbarUploadTrabAgentes);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);

        estado = prefs.getString("estado", null);

        municipio = prefs.getString("municipio", null);

        databaseReference = FirebaseDatabase

                .getInstance()

                .getReference("cadastros/" + estado + "/" + municipio + "/trabalhos_agentes");


        verificaPossibilidadeUploadImagens();

        FloatingActionButton uploadButton = findViewById(R.id.uploadButton);

        uploadCaption = findViewById(R.id.uploadCaption);

        uploadImage = findViewById(R.id.uploadImage);

        totalUploadDeImagens = findViewById(R.id.totalUploadImagem);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(

                new ActivityResultContracts.StartActivityForResult(),

                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();

                        assert data != null;

                        imageUri = data.getData();

                        uploadImage.setImageURI(imageUri);

                    } else {

                        Toast.makeText(UploadTrabAgentes.this, "Nenhuma Imagem Selecionada", Toast.LENGTH_SHORT).show();

                    }

                }

        );

        uploadImage.setOnClickListener(view -> {

            caminhoTotalImagens();

            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);

        });

        uploadButton.setOnClickListener(view -> {

            if (imageUri != null) {
                if (TextUtils.isEmpty(uploadCaption.getText().toString().trim())) {

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "De um título a imagem.", Snackbar.LENGTH_LONG);

// Set custom background color (requires Material Components theme)
                    snackbar.setBackgroundTint(Color.RED);
                    snackbar.setTextColor(Color.WHITE);

                    snackbar.show();

                } else {
                    uploadToFirebase(imageUri);
                }
            } else {

                Toast.makeText(UploadTrabAgentes.this, "Por favor, selecione uma imagem", Toast.LENGTH_SHORT).show();

            }

        });

    }

    @Override

    public boolean onSupportNavigateUp() {
        finish();
        return true;

    }

    private void verificaPossibilidadeUploadImagens() {

        String pathConfig = "cadastros/" + estado + "/" + municipio + "/config/total_upload_imagens";

        FirebaseDatabase.getInstance().getReference(pathConfig)

                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override

                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long total = 0;

                        if (snapshot.exists()) {

                            Long valor = snapshot.getValue(Long.class);

                            total = (valor != null) ? valor : 0;

                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(UploadTrabAgentes.this).create();

                        if (total <= 0) {
                            alertDialog.setTitle("Limite Atingido");
                            alertDialog.setMessage("Limite de upload de 50 imagens atingido. Remova algumas imagens para ter mais espaço.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    (dialog, which) -> dialog.dismiss());
                            alertDialog.show();
                        } else {
                            totalUploadDeImagens.setText(String.format("Pode enviar um total de: %d de 50 imagens possíveis.", total));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

    }

//Outside onCreate

    private void uploadToFirebase(Uri uri){

        String caption = uploadCaption.getText().toString();
        String key = databaseReference.push().getKey();

        assert key != null;
        final StorageReference imageReference = storageReference

                .child(estado)

                .child(municipio)

                .child("trabalhos_agentes")

                .child(key);

        imageReference.putFile(uri)

                .addOnSuccessListener(taskSnapshot -> imageReference.getDownloadUrl()

                        .addOnSuccessListener(uri1 -> {

                            String urlImagem = uri1.toString();

                            ClassTrabAgentes dataClass = new ClassTrabAgentes();

                            dataClass.setTitulo(caption);

                            dataClass.setUrlMidia(urlImagem);

                            dataClass.setTipo("imagem");

                            dataClass.setDataUpload(System.currentTimeMillis());

                            dataClass.setId(key);

                            databaseReference.child(key).setValue(dataClass);

                            Toast.makeText(UploadTrabAgentes.this,

                                    "Enviado!", Toast.LENGTH_SHORT).show();

                            uploadCaption.setText("");

                            imageUri = null; // Limpa a URI selecionada

                            uploadImage.setImageResource(R.drawable.uploadbkg);

                            atualizaContadorImagem();

                            verificaPossibilidadeUploadImagens();

                        }))

                .addOnProgressListener(snapshot -> showLoading())

                .addOnFailureListener(e -> {

                    hideLoading();

                    Toast.makeText(UploadTrabAgentes.this,

                            "Não Logado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UploadTrabAgentes.this, ActivityLoginAdmin.class));

                });

    }

    private void caminhoTotalImagens() {

// Referência para o contador do usuário

        totalRefImagens = dbContador

                .child(estado)

                .child(municipio)

                .child("config")

                .child("total_upload_imagens");

    }

    private void atualizaContadorImagem() {

// Usar transação para garantir consistência

        totalRefImagens.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                mutableData.setValue(currentValue == null ? 1 : currentValue - 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                hideLoading();
                if (committed) {
                    verificaPossibilidadeUploadImagens(); // só releia aqui, depois do commit
                } else {
                    Log.e("UploadTrabalhos", "Erro ao atualizar contador");
                    Toast.makeText(UploadTrabAgentes.this, "Erro ao atualizar contador", Toast.LENGTH_SHORT).show();
                }
            }
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

}

