package com.example.juntoscontradengue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.databinding.ActivityEscorpionismoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Escorpionismo extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    String url = "file:///android_asset/escorpionismo/index_escorpionismo.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_escorpionismo);

        ActivityEscorpionismoBinding bindingEscorpionismo = ActivityEscorpionismoBinding.inflate(getLayoutInflater());
        setContentView(bindingEscorpionismo.getRoot());

        webView = bindingEscorpionismo.webViewEscorpiao;
        progressBar = bindingEscorpionismo.progressEscorpiao;

        Toolbar toolbar = bindingEscorpionismo.toolbarEscorpiao;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setupWebView();

        if (!isNetworkAvailable()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        loadContent();
    }

    // Internet moderno
    private boolean isNetworkAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());

        return caps != null &&
                (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadContent() {

        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("config_app_material_educativo")
                .child("escorpiao")
                .child("html_content");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String html = snapshot.getValue(String.class);

                if (html != null) {

                    webView.loadDataWithBaseURL(
                            null,
                            html,
                            "text/html",
                            "UTF-8",
                            null
                    );

                } else {
                    carregarAssets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                carregarAssets();
            }
        });
    }

    private void carregarAssets() {
        webView.loadUrl(url);
    }
}