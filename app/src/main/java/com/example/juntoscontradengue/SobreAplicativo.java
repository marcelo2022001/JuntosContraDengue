package com.example.juntoscontradengue;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.juntoscontradengue.databinding.ActivitySobreAplicativoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SobreAplicativo extends AppCompatActivity {

    private TextView eMail_App, eContato;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre_aplicativo);

        com.example.juntoscontradengue.databinding.ActivitySobreAplicativoBinding bidingSobre = ActivitySobreAplicativoBinding.inflate(getLayoutInflater());
        setContentView(bidingSobre.getRoot());

        eMail_App = bidingSobre.txtEmailContato;
        eContato = bidingSobre.txtContato;
        busca_email_app();


        TextView txt_versao = bidingSobre.txtVersao;
        String versionName;
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                versionName = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.PackageInfoFlags.of(0)
                ).versionName;
            } else {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Erro_Package_NameNotFoundException", "Erro " + e);
            versionName = "Disponivel na loja";
        }

        txt_versao.setText(String.format("Versão: " + versionName));

        ImageButton btnVoltarSobre = bidingSobre.btnVoltarSobre;
        btnVoltarSobre.setOnClickListener(v -> finish());
    }

    private void busca_email_app() {
        // 1. Aponta diretamente para o nó "email" dentro de "config_app"
        DatabaseReference busca_email = FirebaseDatabase.getInstance()
                .getReference("config_app")
                .child("email");

        busca_email.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 2. Verifica se o nó existe e não está vazio
                if (snapshot.exists() && snapshot.getValue() != null) {
                    // 3. Converte o valor recuperado explicitamente para String
                    String eMailApp = snapshot.getValue(String.class);

                    // Use a variável eMail aqui ou atualize sua UI
                    eMail_App.setText(eMailApp);
                } else{

                    eMail_App.setVisibility(View.GONE);
                    eContato.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 4. Trata possíveis erros de permissão ou conexão
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

}