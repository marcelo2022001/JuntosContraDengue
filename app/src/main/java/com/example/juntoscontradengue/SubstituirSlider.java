package com.example.juntoscontradengue;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class SubstituirSlider extends AppCompatActivity {

    FloatingActionButton fab_AddSlider, fabSalvarSlider;
    ExtendedFloatingActionButton mAddFab;
    TextView txt_fabAddSlider, txt_fabEnviaSlider;

    ProgressBar pgBarSubsSlider;

    // to check whether sub FABs are visible or not
    Boolean isAllFabsVisible;

    ImageView imgSubstImagem;
    String photoUrl, id_slider, estado, municipio;
    private Uri imageUri;
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    ActivitySubstituirSliderBinding activitySubstituirSliderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_substituir_slider);


        activitySubstituirSliderBinding = ActivitySubstituirSliderBinding.inflate(getLayoutInflater());
        setContentView(activitySubstituirSliderBinding.getRoot());


        Toolbar toolbarSubstSliders = findViewById(R.id.toolbarSubsSliders);
        setSupportActionBar(toolbarSubstSliders);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            photoUrl = extras.getString("urlImagem");
            id_slider = extras.getString("idSlider");

            Log.d("id_slider", "recebido apos extras: " + id_slider);
            // LOGS PARA DEBUG
            Log.d("SUBSTITUIR_SLIDER", "photoUrl recebido: " + photoUrl);
            Log.d("SUBSTITUIR_SLIDER", "id_slider recebido: " + id_slider);

            imgSubstImagem = activitySubstituirSliderBinding.imgViewSubsSliders;

            Glide.with(SubstituirSlider.this).load(photoUrl).into(imgSubstImagem);
        }

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        Log.d("SUBSTITUIR_SLIDER", "Estado: " + estado + ", Município: " + municipio);

        // Verifica se os dados estão OK
        if (estado == null || municipio == null) {
            Toast.makeText(this, "Erro: Estado ou município não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pgBarSubsSlider = activitySubstituirSliderBinding.pBarSubstSlider;


        mAddFab = activitySubstituirSliderBinding.addFabExtSlider;
        fab_AddSlider = activitySubstituirSliderBinding.fabAddSlider;
        fabSalvarSlider = activitySubstituirSliderBinding.fabSalvarSlider;

        txt_fabAddSlider = activitySubstituirSliderBinding.txtAddSlider;
        txt_fabEnviaSlider = activitySubstituirSliderBinding.txtAddSalvarSlider;


        // Agora defina todos os FABs e todos os textos de nome de ação como GONE

        fab_AddSlider.setVisibility(View.GONE);
        fabSalvarSlider.setVisibility(View.GONE);

        txt_fabAddSlider.setVisibility(View.GONE);
        txt_fabEnviaSlider.setVisibility(View.GONE);

        // torne a variável booleana falsa, pois todos os
        // textos de nome de ação e todos os sub FABs são
        // invisíveis
        isAllFabsVisible = false;

        // Defina o botão de ação flutuante estendido para
        // estado reduzido inicialmente
        mAddFab.shrink();

        // Faremos com que todos os FABs e textos de nomes de ações
        // sejam visíveis somente quando o botão FAB pai for clicado. Então
        // temos que manipular o botão FAB pai primeiro,
        // usando setOnClickListener, você pode ver abaixo
        mAddFab.setOnClickListener(
                view -> {
                    if (!isAllFabsVisible) {
                        // quando isAllFabsVisible se torna
                        // verdadeiro, torna todos os textos de nomes de ação // e FABs VISÍVEIS.
                        fab_AddSlider.show();
                        fabSalvarSlider.show();

                        txt_fabAddSlider.setVisibility(View.VISIBLE);
                        txt_fabEnviaSlider.setVisibility(View.VISIBLE);

                        // Agora estenda o FAB pai, conforme o usuário clica no FAB pai reduzido
                        // mAddFab.extend();
                        mAddFab.setIcon(ContextCompat.getDrawable(SubstituirSlider.this, R.drawable.baseline_arrow_downward));

                        // torne a variável booleana verdadeira como
                        // definimos os sub FABs
                        // visibilidade para GONE
                        isAllFabsVisible = true;
                    } else {
                        // quando isAllFabsVisible se torna
                        // verdadeiro, todos os nomes de ação
                        // textos e FABs desaparecem.
                        fab_AddSlider.hide();
                        fabSalvarSlider.hide();

                        txt_fabAddSlider.setVisibility(View.GONE);
                        txt_fabEnviaSlider.setVisibility(View.GONE);

                        // Defina o FAB para encolher após o usuário
                        // fechar todos os sub FABs
                        // mAddFab.shrink();
                        mAddFab.setIcon(ContextCompat.getDrawable(SubstituirSlider.this, R.drawable.baseline_add));

                        // torne a variável booleana falsa
                        // pois definimos os sub FABs
                        // visibilidade como GONE
                        isAllFabsVisible = false;
                    }
                });

        // abaixo está a ação de exemplo para manipular adicionar imagem
        // Somente quando estiver visível e somente
        // quando o usuário clicar nele
        fab_AddSlider.setOnClickListener( view -> {
                    Intent photoPicker = new Intent();
                    photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                    photoPicker.setType("image/*");
                    activityResultLauncher.launch(photoPicker);
                } );

        // abaixo está a ação de exemplo para manipular salvar imagem
        // Somente quando estiver visível e somente
        // quando o usuário clicar nele
        fabSalvarSlider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageUri != null){
                    uploadToFirebase(imageUri);
                } else  {
                    Toast toast = Toast.makeText(SubstituirSlider.this, "Por favor, selecione uma imagem", Toast.LENGTH_SHORT);
                    Objects.requireNonNull(toast.getView()).setBackgroundColor(Color.parseColor("#F6AE2D"));
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
                
            }

            private void uploadToFirebase(Uri uri) {
                // Verifica e loga com segurança
                Log.d("SUBSTITUIR_SLIDER", "ID do slider: " + (id_slider != null ? id_slider : "null"));
                Log.d("SUBSTITUIR_SLIDER", "Estado: " + (estado != null ? estado : "null") + ", Município: " + (municipio != null ? municipio : "null"));

                // Verifica se os dados necessários estão presentes
                if (id_slider == null || estado == null || municipio == null) {
                    pgBarSubsSlider.setVisibility(View.GONE);
                    Toast.makeText(SubstituirSlider.this,
                            "Erro: Dados de localização não encontrados",
                            Toast.LENGTH_SHORT).show();
                    Log.e("SUBSTITUIR_SLIDER", "Dados nulos - id_slider: " + id_slider + ", estado: " + estado + ", municipio: " + municipio);
                    return;
                }

                pgBarSubsSlider.setVisibility(View.VISIBLE);

                // Nome do campo no Realtime Database
                String fieldName = "image_" + id_slider; // image_1, image_2, etc.

                // Nome do arquivo no Storage (apenas o ID)
               // String fileName = id_slider + ".jpg";

                Log.d("SUBSTITUIR_SLIDER", "FieldName: " + fieldName);
               // Log.d("SUBSTITUIR_SLIDER", "FileName: " + fileName);

                final StorageReference imageReference = storageReference
                        .child(estado)
                        .child(municipio)
                        .child("sliders_main")
                        .child(fieldName); // ← Usa apenas o ID como nome do arquivo

                // Faz o upload da imagem
                UploadTask uploadTask = imageReference.putFile(uri);

                uploadTask.addOnProgressListener(taskSnapshot -> {
                    // Opcional: Mostrar progresso
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d("SUBSTITUIR_SLIDER", "Upload progress: " + progress + "%");
                });

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Log.d("SUBSTITUIR_SLIDER", "Upload concluído com sucesso!");

                    // Pega a URL da imagem
                    imageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String downloadUrl = downloadUri.toString();
                        Log.d("SUBSTITUIR_SLIDER", "Nova URL: " + downloadUrl);

                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("cadastros")
                                .child(estado)
                                .child(municipio)
                                .child("config")
                                .child("sliders_main")
                                .child(id_slider);

                                    // Atualiza também o campo url_imagem
                                    ref.child(fieldName).setValue(downloadUrl)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("SUBSTITUIR_SLIDER", "url_imagem atualizado com sucesso!");

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
                                                Log.e("SUBSTITUIR_SLIDER", "Erro ao atualizar url_imagem", e);
                                                Toast.makeText(SubstituirSlider.this,
                                                        "Erro ao atualizar URL",
                                                        Toast.LENGTH_SHORT).show();
                                            });
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
                    });

            }


        });
}

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    assert data != null;
                    imageUri = data.getData();
                    imgSubstImagem.setImageURI(imageUri);
                } else {
                    Toast.makeText(SubstituirSlider.this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show();
                }
            }
    );
}