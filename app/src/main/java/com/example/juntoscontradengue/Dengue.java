package com.example.juntoscontradengue;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.juntoscontradengue.databinding.ActivityDengueBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Dengue extends AppCompatActivity {

    private static final String TAG = "DENGUE";
    private static final long TIMEOUT_CARREGAMENTO_MS = 5000;
    private static final String ARQUIVO_CACHE_HTML = "dengue_html_cache.html";
    private static final Pattern PADRAO_IMG =
            Pattern.compile("<img[^>]+src\\s*=\\s*[\"'](https?://[^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private ActivityDengueBinding binding;
    private WebView webViewDengue;
    private ProgressBar progressBar;
    private DatabaseReference htmlRef;
    private ValueEventListener htmlListener;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutCarregamento;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private String htmlAtual = null;
    private boolean conteudoExibido = false;
    private boolean redirecionadoSemInternet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDengueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        webViewDengue = binding.webViewDengue;
        progressBar = binding.progressDengue;

        Toolbar toolbar = binding.toolbarDengue;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setupWebView();
        atualizarBannerOffline();

        htmlRef = FirebaseDatabase.getInstance()
                .getReference("config_app_material_educativo")
                .child("dengue")
                .child("html_content");
        htmlRef.keepSynced(true);

        progressBar.setVisibility(View.VISIBLE);

        // 1) cópia salva no aparelho (aparece na hora, inclusive reabrindo o app offline)
        mostrarCopiaLocalSeExistir();
        // 2) Firebase (fonte da verdade): atualiza sozinho quando o admin mudar ou a internet voltar
        carregarDoFirebase();
        iniciarTimeout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registrarNetworkCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removerNetworkCallback();
    }

    // ---------------------------------------------------------------- WebView

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webViewDengue.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(false); // não usa mais assets
        atualizarModoCacheWebView();

        webViewDengue.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            // Imagens do HTML: serve do cache do Glide (o mesmo que o preload aquece),
            // então aparecem mesmo sem internet
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("GET".equalsIgnoreCase(request.getMethod()) && ehImagem(uri)) {
                    try {
                        File arquivo = Glide.with(getApplicationContext())
                                .downloadOnly()
                                .load(uri.toString())
                                .submit()
                                .get();
                        return new WebResourceResponse(mimeDaImagem(uri), null, new FileInputStream(arquivo));
                    } catch (Exception e) {
                        // sem cache e sem rede: deixa o WebView tentar normalmente
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
    }

    private void atualizarModoCacheWebView() {
        boolean online = NetworkUtils.isNetworkAvailable(this);
        webViewDengue.getSettings().setCacheMode(
                online ? WebSettings.LOAD_DEFAULT : WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    private boolean ehImagem(Uri uri) {
        String path = uri.getPath();
        if (path == null) return false;
        String p = path.toLowerCase(Locale.ROOT);
        return p.endsWith(".png") || p.endsWith(".jpg") || p.endsWith(".jpeg")
                || p.endsWith(".webp") || p.endsWith(".gif");
    }

    private String mimeDaImagem(Uri uri) {
        String p = Objects.requireNonNull(uri.getPath()).toLowerCase(Locale.ROOT);
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".webp")) return "image/webp";
        if (p.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }

    // ---------------------------------------------------------------- Conteúdo

    private void carregarDoFirebase() {
        htmlListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cancelarTimeout();
                atualizarBannerOffline();

                String html = snapshot.getValue(String.class);
                boolean temHtml = html != null && !html.trim().isEmpty();
                boolean online = NetworkUtils.isNetworkAvailable(Dengue.this);

                if (temHtml) {
                    preCarregarImagens(html);
                    salvarCopiaLocal(html);
                    mostrarHtml(html);
                } else if (online) {
                    // conectado e o nó realmente não tem conteúdo: força o aviso
                    conteudoExibido = false;
                    apagarCopiaLocal();
                    mostrarAviso();
                } else if (!conteudoExibido) {
                    // offline, sem cache do Firebase e sem cópia local
                    irParaSemInternet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro ao buscar HTML: " + error.getMessage());
                cancelarTimeout();
                htmlListener = null; // permite tentar de novo quando a internet voltar

                if (conteudoExibido) return;

                if (NetworkUtils.isNetworkAvailable(Dengue.this)) {
                    mostrarAviso();
                } else {
                    irParaSemInternet();
                }
            }
        };

        htmlRef.addValueEventListener(htmlListener);
    }

    private void mostrarHtml(String html) {
        if (html.equals(htmlAtual)) return; // não recarrega a página (nem perde a rolagem) à toa
        htmlAtual = html;
        conteudoExibido = true;
        webViewDengue.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private void mostrarAviso() {
        htmlAtual = null;
        String aviso = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'></head>"
                + "<body style='font-family:sans-serif;text-align:center;padding:32px;color:#555'>"
                + "<h3>" + getString(R.string.conteudo_indisponivel_titulo) + "</h3>"
                + "<p>" + getString(R.string.conteudo_indisponivel_msg) + "</p>"
                + "</body></html>";
        webViewDengue.loadDataWithBaseURL(null, aviso, "text/html", "UTF-8", null);
    }

    // Esquenta o cache do Glide para as imagens do HTML enquanto há internet
    private void preCarregarImagens(String html) {
        if (!NetworkUtils.isNetworkAvailable(this)) return;

        Matcher m = PADRAO_IMG.matcher(html);
        while (m.find()) {
            Glide.with(getApplicationContext())
                    .load(m.group(1))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();
        }
    }

    // ---------------------------------------------------------------- Cópia local do HTML

    private void mostrarCopiaLocalSeExistir() {
        io.execute(() -> {
            String html = lerCopiaLocal();
            if (html == null) return;
            runOnUiThread(() -> {
                // só usa a cópia se o Firebase ainda não respondeu (evita sobrescrever dado mais novo)
                if (!isFinishing() && !isDestroyed() && !conteudoExibido) {
                    mostrarHtml(html);
                }
            });
        });
    }

    private void salvarCopiaLocal(String html) {
        io.execute(() -> {
            try (FileOutputStream fos = openFileOutput(ARQUIVO_CACHE_HTML, MODE_PRIVATE)) {
                fos.write(html.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                Log.e(TAG, "Erro ao salvar cópia local", e);
            }
        });
    }

    private void apagarCopiaLocal() {
        io.execute(() -> deleteFile(ARQUIVO_CACHE_HTML));
    }

    private String lerCopiaLocal() {
        File arquivo = new File(getFilesDir(), ARQUIVO_CACHE_HTML);
        if (!arquivo.exists()) return null;

        try (FileInputStream fis = new FileInputStream(arquivo);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int lidos;
            while ((lidos = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, lidos);
            }
            String html = bos.toString("UTF-8");
            return html.trim().isEmpty() ? null : html;
        } catch (IOException e) {
            Log.e(TAG, "Erro ao ler cópia local", e);
            return null;
        }
    }

    // ---------------------------------------------------------------- Timeout

    private void iniciarTimeout() {
        cancelarTimeout();
        timeoutCarregamento = () -> {
            if (conteudoExibido) return;

            if (NetworkUtils.isNetworkAvailable(Dengue.this)) {
                mostrarAviso(); // se o dado chegar depois, o listener substitui o aviso
            } else {
                irParaSemInternet();
            }
        };
        handler.postDelayed(timeoutCarregamento, TIMEOUT_CARREGAMENTO_MS);
    }

    private void cancelarTimeout() {
        if (timeoutCarregamento != null) {
            handler.removeCallbacks(timeoutCarregamento);
        }
    }

    // ---------------------------------------------------------------- Rede

    private void registrarNetworkCallback() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> {
                    atualizarBannerOffline();
                    atualizarModoCacheWebView();
                    if (htmlListener == null) { // listener caiu por erro: tenta de novo
                        carregarDoFirebase();
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> {
                    atualizarBannerOffline();
                    atualizarModoCacheWebView();
                });
            }
        };

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private void removerNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException e) {
                // já não estava registrado; ignora
            }
        }
    }

    private void atualizarBannerOffline() {
        boolean conectado = NetworkUtils.isNetworkAvailable(this);
        binding.txtAvisoOfflineDengue.setVisibility(conectado ? View.GONE : View.VISIBLE);
    }

    private void irParaSemInternet() {
        if (redirecionadoSemInternet) return;
        redirecionadoSemInternet = true;

        Intent intent = new Intent(Dengue.this, SemInternetActivity.class);
        intent.putExtra("id_activity", "material_educativo");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelarTimeout();
        if (htmlRef != null && htmlListener != null) {
            htmlRef.removeEventListener(htmlListener);
        }
        io.shutdown();
    }
}