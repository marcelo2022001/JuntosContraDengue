package com.example.juntoscontradengue;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.databinding.ActivitySubstituirSliderBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class SubstituirSlider extends AppCompatActivity {

    FloatingActionButton fab_AddSlider, fabSalvarSlider;
    ExtendedFloatingActionButton mAddFab;
    TextView txt_fabAddSlider, txt_fabEnviaSlider;

    ProgressBar pgBarSubsSlider;

    // para saber se os sub FABs estão visíveis
    Boolean isAllFabsVisible;

    ImageView imgSubstImagem;
    String photoUrl, id_slider, estado, municipio;
    private Uri imageUri;
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseDatabase databaseMunicipio;
    ActivitySubstituirSliderBinding activitySubstituirSliderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // (removido o setContentView(R.layout...) duplicado: só o binding)
        activitySubstituirSliderBinding = ActivitySubstituirSliderBinding.inflate(getLayoutInflater());
        setContentView(activitySubstituirSliderBinding.getRoot());

        Toolbar toolbarSubstSliders = findViewById(R.id.toolbarSubsSliders);
        setSupportActionBar(toolbarSubstSliders);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        imgSubstImagem = activitySubstituirSliderBinding.imgViewSubsSliders;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            photoUrl = extras.getString("urlImagem");   // URL da imagem ATUAL (a que será substituída)
            id_slider = extras.getString("idSlider");

            Log.d("SUBSTITUIR_SLIDER", "photoUrl recebido: " + photoUrl);
            Log.d("SUBSTITUIR_SLIDER", "id_slider recebido: " + id_slider);

            Glide.with(SubstituirSlider.this).load(photoUrl).into(imgSubstImagem);
        }

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        if (estado == null || municipio == null) {
            Toast.makeText(this, "Erro: Estado ou município não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        Log.d("SUBSTITUIR_SLIDER", "Estado: " + estado + ", Município: " + municipio);

        pgBarSubsSlider = activitySubstituirSliderBinding.pBarSubstSlider;

        mAddFab = activitySubstituirSliderBinding.addFabExtSlider;
        fab_AddSlider = activitySubstituirSliderBinding.fabAddSlider;
        fabSalvarSlider = activitySubstituirSliderBinding.fabSalvarSlider;

        txt_fabAddSlider = activitySubstituirSliderBinding.txtAddSlider;
        txt_fabEnviaSlider = activitySubstituirSliderBinding.txtAddSalvarSlider;

        // Começa com os sub FABs e seus textos escondidos
        fab_AddSlider.setVisibility(View.GONE);
        fabSalvarSlider.setVisibility(View.GONE);
        txt_fabAddSlider.setVisibility(View.GONE);
        txt_fabEnviaSlider.setVisibility(View.GONE);

        isAllFabsVisible = false;
        mAddFab.shrink();

        mAddFab.setOnClickListener(view -> {
            if (!isAllFabsVisible) {
                fab_AddSlider.show();
                fabSalvarSlider.show();

                txt_fabAddSlider.setVisibility(View.VISIBLE);
                txt_fabEnviaSlider.setVisibility(View.VISIBLE);

                mAddFab.setIcon(ContextCompat.getDrawable(SubstituirSlider.this, R.drawable.baseline_arrow_downward));
                isAllFabsVisible = true;
            } else {
                fab_AddSlider.hide();
                fabSalvarSlider.hide();

                txt_fabAddSlider.setVisibility(View.GONE);
                txt_fabEnviaSlider.setVisibility(View.GONE);

                mAddFab.setIcon(ContextCompat.getDrawable(SubstituirSlider.this, R.drawable.baseline_add));
                isAllFabsVisible = false;
            }
        });

        // Escolher imagem
        fab_AddSlider.setOnClickListener(view -> {
            Intent photoPicker = new Intent();
            photoPicker.setAction(Intent.ACTION_GET_CONTENT);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        // Salvar imagem
        fabSalvarSlider.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadToFirebase(imageUri);
            } else {
                Snackbar snackbar = Snackbar.make(
                        activitySubstituirSliderBinding.getRoot(),
                        "Por favor, selecione uma imagem",
                        Snackbar.LENGTH_SHORT);
                snackbar.setBackgroundTint(Color.parseColor("#F6AE2D"));
                snackbar.setTextColor(Color.BLACK);
                snackbar.show();
            }
        });
    }

    // =====================================================
    // UPLOAD
    // =====================================================
    private void uploadToFirebase(Uri uri) {
        Log.d("SUBSTITUIR_SLIDER", "ID do slider: " + id_slider);

        if (id_slider == null || estado == null || municipio == null) {
            pgBarSubsSlider.setVisibility(View.GONE);
            Toast.makeText(SubstituirSlider.this,
                    "Erro: Dados de localização não encontrados",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        pgBarSubsSlider.setVisibility(View.VISIBLE);

        // Nome do campo no Realtime Database e do arquivo no Storage: image_0, image_1...
        final String fieldName = "image_" + id_slider;

        final StorageReference imageReference = storageReference
                .child(estado)
                .child(municipio)
                .child("sliders_main")
                .child(fieldName);

        // O arquivo não tem extensão, então informamos o tipo para o Storage
        String tipo = getContentResolver().getType(uri);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(tipo != null ? tipo : "image/jpeg")
                .build();

        UploadTask uploadTask = imageReference.putFile(uri, metadata);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d("SUBSTITUIR_SLIDER", "Upload progress: " + progress + "%");
        });

        uploadTask.addOnSuccessListener(taskSnapshot ->
                imageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                    String downloadUrl = downloadUri.toString();
                    Log.d("SUBSTITUIR_SLIDER", "Nova URL: " + downloadUrl);

                    DatabaseReference ref = databaseMunicipio
                            .getReference("config/sliders_main/" + id_slider);

                    ref.child(fieldName).setValue(downloadUrl)
                            .addOnSuccessListener(aVoid -> {
                                // Só apaga o arquivo antigo DEPOIS de o banco já apontar para o novo
                                apagarArquivoAntigoSeDiferente(imageReference);

                                pgBarSubsSlider.setVisibility(View.GONE);
                                Toast.makeText(SubstituirSlider.this,
                                        "Imagem atualizada com sucesso!",
                                        Toast.LENGTH_SHORT).show();

                                Intent itent = new Intent(SubstituirSlider.this, AddSlidersMain.class);
                                itent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(itent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                pgBarSubsSlider.setVisibility(View.GONE);
                                Log.e("SUBSTITUIR_SLIDER", "Erro ao atualizar " + fieldName, e);
                                Toast.makeText(SubstituirSlider.this,
                                        "Erro ao atualizar imagem: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });

                }).addOnFailureListener(e -> {
                    pgBarSubsSlider.setVisibility(View.GONE);
                    Log.e("SUBSTITUIR_SLIDER", "Erro ao obter URL", e);
                    Toast.makeText(SubstituirSlider.this,
                            "Erro ao obter URL da imagem",
                            Toast.LENGTH_SHORT).show();
                })
        ).addOnFailureListener(e -> {
            pgBarSubsSlider.setVisibility(View.GONE);
            Log.e("SUBSTITUIR_SLIDER", "Erro no upload", e);
            Toast.makeText(SubstituirSlider.this,
                    "Erro ao enviar imagem: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }

    /**
     * O arquivo antigo (ex.: image_0.png) tem nome diferente do novo (image_0, sem extensão),
     * por isso o upload não o sobrescreve. Aqui ele é apagado pela URL antiga.
     * Se o caminho for o mesmo, o upload já sobrescreveu e não há nada a apagar.
     */
    private void apagarArquivoAntigoSeDiferente(StorageReference novoRef) {
        if (photoUrl == null || photoUrl.isEmpty()) return;

        try {
            StorageReference antigo = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);

            if (!antigo.getPath().equals(novoRef.getPath())) {
                antigo.delete()
                        .addOnSuccessListener(v ->
                                Log.d("SUBSTITUIR_SLIDER", "Arquivo antigo apagado: " + antigo.getPath()))
                        .addOnFailureListener(e ->
                                Log.w("SUBSTITUIR_SLIDER", "Não foi possível apagar o antigo: " + e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            Log.w("SUBSTITUIR_SLIDER", "URL antiga inválida: " + photoUrl);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imgSubstImagem.setImageURI(imageUri);
                } else {
                    Toast.makeText(SubstituirSlider.this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show();
                }
            }
    );
}