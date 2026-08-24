package com.example.juntoscontradengue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TermosDeUsoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_termos_de_uso);

        // 1. Redireciona e encerra se estiver sem internet
        boolean isConnected = NetworkUtils.isNetworkAvailable(TermosDeUsoActivity.this);
        if (!isConnected) {
           semInternet();
        }

        // 2. Busca o link no Firebase Realtime Database
        buscaLink();
    }

    private void buscaLink() {
        DatabaseReference buscaLink = FirebaseDatabase.getInstance()
                .getReference("config_app")
                .child("link_termos_uso");

        buscaLink.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    String url = snapshot.getValue(String.class);
                    abrirCustomTab(url);
                } else {
                    Log.e("Firebase", "Link de termos não encontrado");
                    Toast.makeText(TermosDeUsoActivity.this, "Termos indisponíveis no momento.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                Toast.makeText(TermosDeUsoActivity.this,
                        "Erro ao carregar termos. Tente novamente.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void abrirCustomTab(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();

                // Abre a URL na Custom Tab
                customTabsIntent.launchUrl(this, Uri.parse(url));

                // Encerra esta Activity para que o usuário volte direto à tela anterior ao fechar a aba
                finish();
            } catch (Exception e) {
                Log.e("CustomTabError", "Erro ao abrir Custom Tab: " + e.getMessage());
                Toast.makeText(this, "Erro ao abrir os termos de uso.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Link não disponível", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void semInternet() {

            Intent intent = new Intent(TermosDeUsoActivity.this, SemInternetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("id_activity", "termos");
            startActivity(intent);
            finish();
    }
}