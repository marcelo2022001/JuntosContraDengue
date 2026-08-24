package com.example.juntoscontradengue;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.database.adapters.AdapterResiduosEletronicos;
import com.example.juntoscontradengue.database.classes_database.ClassDescarteConsciente;
import com.example.juntoscontradengue.databinding.ActivityDescarteEletronicosBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DescarteEletronicos extends AppCompatActivity implements AdapterResiduosEletronicos.ClickResiduosEletronicos {

    private static final String TAG = "DescarteEletronicos";
    private static final long TIMEOUT_SEM_CACHE_MS = 3000; // 3s
    private ActivityDescarteEletronicosBinding binding;
    private TextView textView;
    private WebView webView;
    private RecyclerView recyclerView;
    private FirebaseDatabase database;
    private AdapterResiduosEletronicos adapterResiduosEletronicos;

    private final List<ClassDescarteConsciente> descarteConscienteList = new ArrayList<>();
    private final Map<String, Integer> itemPositionMap = new HashMap<>();

    private ChildEventListener childEventListener;
    private DatabaseReference databaseReference;

    private String estado, municipio;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean redirecionadoSemInternet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDescarteEletronicosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarDescarteEletronicos;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> navigateBackToMainActivity());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        textView = binding.txtlocalEntregaEletronicos;
        webView = binding.wvDescarteEletronicos;
        recyclerView = binding.rvDescarteEletronicos;

        database = FirebaseDatabase.getInstance();

        setupWebView();
        atualizarBannerOffline();
        loadContent();
        inciarRecyclerViewDescarteConsciente();

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    navigateBackToMainActivity();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registrarNetworkCallback();

        int previousSize = descarteConscienteList.size();
        if (previousSize > 0) {
            descarteConscienteList.clear();
            itemPositionMap.clear();
            if (adapterResiduosEletronicos != null) {
                adapterResiduosEletronicos.notifyItemRangeRemoved(0, previousSize);
            }
        }

        ouvinte();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removerNetworkCallback();
        if (childEventListener != null && databaseReference != null) {
            databaseReference.removeEventListener(childEventListener);
        }
    }

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
                    recarregarOuvinte();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(DescarteEletronicos.this::atualizarBannerOffline);
            }
        };

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private void removerNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException e) {
                // Callback unregistration safety
            }
        }
    }

    private void atualizarBannerOffline() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);
        binding.txtAvisoOfflineEletronicos.setVisibility(isConnected ? View.GONE : View.VISIBLE);
    }

    private void inciarRecyclerViewDescarteConsciente() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterResiduosEletronicos = new AdapterResiduosEletronicos(this, descarteConscienteList, this);
        recyclerView.setAdapter(adapterResiduosEletronicos);
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
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ajustarAlturaWebView(view);
            }
        });
    }

    private void ajustarAlturaWebView(WebView view) {
        view.evaluateJavascript(
                "(function() { return document.body ? document.body.scrollHeight : 0; })();",
                value -> {
                    try {
                        int alturaDp = Integer.parseInt(value);
                        float density = getResources().getDisplayMetrics().density;
                        int alturaPx = Math.round(alturaDp * density);

                        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
                        params.height = alturaPx;
                        view.setLayoutParams(params);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Erro ao ajustar altura do WebView: " + value, e);
                    }
                }
        );
    }

    private void loadContent() {
        DatabaseReference htmlReference = database.getReference("config_app_material_educativo")
                .child("e_lixo") // ou "pneus", conforme a tela
                .child("html_content");

        htmlReference.keepSynced(true);

        final boolean[] respondido = {false};
        final Handler handler = new Handler(Looper.getMainLooper());

        Runnable timeoutRunnable = () -> {
            if (!respondido[0]) {
                respondido[0] = true;
                mostrarAvisoSemConteudo();
            }
        };

        // Só ativa o timeout se já sabemos que está offline.
        // Se tiver internet, o Firebase deve responder (dado ou erro) rapidamente.
        if (!NetworkUtils.isNetworkAvailable(this)) {
            handler.postDelayed(timeoutRunnable, TIMEOUT_SEM_CACHE_MS);
        }

        htmlReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (respondido[0]) return; // timeout já disparou, ignora resposta tardia
                respondido[0] = true;
                handler.removeCallbacks(timeoutRunnable);

                String html = snapshot.getValue(String.class);
                if (html != null && !html.isEmpty()) {
                    atualizarBannerOffline(); // garante que o banner reflita o estado real da rede no momento da exibição
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                } else {
                    mostrarAvisoSemConteudo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (respondido[0]) return;
                respondido[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                mostrarAvisoSemConteudo();
                Log.e(TAG, "Erro ao carregar conteúdo do Firebase: " + error.getMessage());
            }
        });
    }

    private void mostrarAvisoSemConteudo() {
        webView.setVisibility(View.GONE);
        Intent intent = new Intent(DescarteEletronicos.this, SemInternetActivity.class);
        intent.putExtra("id_activity", "descarte_pneus_eletronicos");
        startActivity(intent);
        finish();
    }

    private void recarregarOuvinte() {
        if (databaseReference != null && childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
        }
        redirecionadoSemInternet = false;
        ouvinte();
    }

    private void ouvinte() {
        if (estado == null || municipio == null) {
            Log.e(TAG, "Estado ou município não configurados");
            return;
        }

        databaseReference = database.getReference()
                .child("cadastros")
                .child(estado.toLowerCase())
                .child(municipio.toLowerCase())
                .child("descarte_eletronicos");

        databaseReference.keepSynced(true);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                ClassDescarteConsciente descarteConsciente = snapshot.getValue(ClassDescarteConsciente.class);

                if (descarteConsciente != null && key != null) {
                    descarteConsciente.setId(key);

                    int position = 0;
                    if (previousChildName != null) {
                        Integer prevPosition = itemPositionMap.get(previousChildName);
                        if (prevPosition != null) {
                            position = prevPosition + 1;
                        }
                    }

                    if (position > descarteConscienteList.size()) {
                        position = descarteConscienteList.size();
                    }

                    descarteConscienteList.add(position, descarteConsciente);
                    updatePositionMapFrom(position);

                    if (adapterResiduosEletronicos != null) {
                        adapterResiduosEletronicos.notifyItemInserted(position);
                    }

                    if (!descarteConscienteList.isEmpty()) {
                        textView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                ClassDescarteConsciente updatedClass = snapshot.getValue(ClassDescarteConsciente.class);

                if (key != null && updatedClass != null) {
                    Integer position = itemPositionMap.get(key);
                    if (position != null) {
                        updatedClass.setId(key);
                        descarteConscienteList.set(position, updatedClass);

                        if (adapterResiduosEletronicos != null) {
                            adapterResiduosEletronicos.notifyItemChanged(position);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();

                if (key != null) {
                    Integer position = itemPositionMap.get(key);
                    if (position != null) {
                        descarteConscienteList.remove(position.intValue());
                        itemPositionMap.remove(key);
                        updatePositionMapFrom(position);

                        if (adapterResiduosEletronicos != null) {
                            adapterResiduosEletronicos.notifyItemRemoved(position);
                        }

                        if (descarteConscienteList.isEmpty()) {
                            textView.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                ClassDescarteConsciente movedClass = snapshot.getValue(ClassDescarteConsciente.class);

                if (key != null && movedClass != null) {
                    Integer oldPosition = itemPositionMap.get(key);

                    if (oldPosition != null) {
                        descarteConscienteList.remove(oldPosition.intValue());

                        if (adapterResiduosEletronicos != null) {
                            adapterResiduosEletronicos.notifyItemRemoved(oldPosition);
                        }

                        int newPosition = 0;
                        if (previousChildName != null) {
                            Integer prevPosition = itemPositionMap.get(previousChildName);
                            if (prevPosition != null) {
                                newPosition = prevPosition + 1;
                            }
                        }

                        if (newPosition > descarteConscienteList.size()) {
                            newPosition = descarteConscienteList.size();
                        }

                        movedClass.setId(key);
                        descarteConscienteList.add(newPosition, movedClass);
                        updatePositionMapFrom(Math.min(oldPosition, newPosition));

                        if (adapterResiduosEletronicos != null) {
                            adapterResiduosEletronicos.notifyItemMoved(oldPosition, newPosition);
                            adapterResiduosEletronicos.notifyItemChanged(newPosition);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro no Firebase: " + error.getMessage());

                if (!NetworkUtils.isNetworkAvailable(DescarteEletronicos.this)) {
                    irParaSemInternet();
                }
            }
        };

        databaseReference.addChildEventListener(childEventListener);
    }

    private void updatePositionMapFrom(int startPosition) {
        for (int i = startPosition; i < descarteConscienteList.size(); i++) {
            ClassDescarteConsciente item = descarteConscienteList.get(i);
            if (item.getId() != null) {
                itemPositionMap.put(item.getId(), i);
            }
        }
    }

    private void irParaSemInternet() {
        if (redirecionadoSemInternet) return;
        redirecionadoSemInternet = true;

        Intent intent = new Intent(DescarteEletronicos.this, SemInternetActivity.class);
        intent.putExtra("id_activity", "descarte_eletronicos");
        startActivity(intent);
        finish();
    }

    @Override
    public void click_DescarteEletronicos(ClassDescarteConsciente descarteEletronicosClass) {
        if (descarteEletronicosClass != null && descarteEletronicosClass.getFone() != null) {
            ligar(descarteEletronicosClass.getFone());
        }
    }

    private void ligar(String num) {
        if (ActivityCompat.checkSelfPermission(DescarteEletronicos.this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DescarteEletronicos.this,
                    new String[]{Manifest.permission.CALL_PHONE}, 1);

        } else {
            Intent intentLigar = new Intent(Intent.ACTION_CALL);
            intentLigar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentLigar.setData(Uri.parse("tel:" + num));
            startActivity(intentLigar);
        }
    }

    private void navigateBackToMainActivity() {
        Intent intent = new Intent(DescarteEletronicos.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        navigateBackToMainActivity();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null && databaseReference != null) {
            databaseReference.removeEventListener(childEventListener);
        }
        if (webView != null) {
            webView.destroy();
        }
        binding = null;
    }
}