package com.example.juntoscontradengue;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private ViewFlipper v_flipper;
    private String estado;
    private String municipio;
    private String nome_usuario;
    String perfil;
    int tutorial;
    private TextView txtNomeUsuario, txtLocalidadeUsuario;
    private ImageView brasaoMunicipio;
    private final int[] imagensOffline = {
            R.drawable.image_1,
            R.drawable.image_2,
            R.drawable.image_3,
            R.drawable.image_4,
             R.drawable.image_5
    };

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ImageButton btn_agentes, btn_trab_agentes, btn_denuncias, btn_dengue, btn_escorpiao;

    // =====================================================
    // onCreate
    // =====================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_main);

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = Objects.requireNonNull(prefs.getString("estado", "")).toLowerCase();
        municipio = Objects.requireNonNull(prefs.getString("municipio", "")).toLowerCase();
        tutorial = prefs.getInt("tutorial", 1);

        if (!prefs.getBoolean("tutorialFull", false)) {

            mostrarTutorial();

        }

        SharedPreferences prefUser = getSharedPreferences("UserData", MODE_PRIVATE);
        perfil = Objects.requireNonNull(prefUser.getString("perfil", "")).toLowerCase();
        nome_usuario = Objects.requireNonNull(prefUser.getString("nome", "")).toUpperCase();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        criarCanalNotificacao();
        configurarTopicosFCM();

        v_flipper = findViewById(R.id.v_flipper);

        configurarBotoes();
        configurarToolbarEDrawer();

        if (NetworkUtils.isNetworkAvailable(this)) {
            carregarImagensFirebase();
        } else {
            fallbackOuCarregarOffline();
        }
    }

    private void criarCanalNotificacao() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // O ID "default" deve ser o mesmo que usamos na Cloud Function
            NotificationChannel channel = new NotificationChannel(
                    "default",
                    "Alertas de Dengue",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações de mutirões e avisos urgentes");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void configurarTopicosFCM() {


        if (estado == null || municipio == null || perfil == null) return;

        if (estado.isEmpty() || municipio.isEmpty() || perfil.isEmpty()) return;

        String topicoUsuarios = estado + "_" + municipio + "_usuarios";
        String topicoAdmins   = estado + "_" + municipio + "_admins";
        String topicoAgentes  = estado + "_" + municipio + "_agentes";

        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicoUsuarios);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicoAdmins);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicoAgentes);

        switch (perfil) {

            case "usuarios":
                FirebaseMessaging.getInstance().subscribeToTopic(topicoUsuarios);
                Log.d("FCM", "Inscrito em " + topicoUsuarios);
                break;

            case "admins":
                FirebaseMessaging.getInstance().subscribeToTopic(topicoAdmins);
                Log.d("FCM", "Inscrito em " + topicoAdmins);
                break;

            case "agentes":
                FirebaseMessaging.getInstance().subscribeToTopic(topicoAgentes);
                Log.d("FCM", "Inscrito em " + topicoAgentes);
                break;
        }
    }

    // =====================================================
    // UI
    // =====================================================
    private void configurarBotoes() {
        btn_agentes = findViewById(R.id.imageButtonAgentes);
        btn_trab_agentes = findViewById(R.id.imageButtonTrabAgentes);
        btn_denuncias = findViewById(R.id.imageButtonDenuncias);
        btn_dengue = findViewById(R.id.imageButtonDengue);
        btn_escorpiao = findViewById(R.id.imageButtonEscorpiao);

        btn_agentes.setOnClickListener(this);
        btn_trab_agentes.setOnClickListener(this);
        btn_denuncias.setOnClickListener(this);
        btn_dengue.setOnClickListener(this);
        btn_escorpiao.setOnClickListener(this);
    }

    private void configurarToolbarEDrawer() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        brasaoMunicipio = header.findViewById(R.id.brasaoMunicipio);
        txtNomeUsuario = header.findViewById(R.id.txtNomeUsuario);
        txtLocalidadeUsuario = header.findViewById(R.id.txtLocalidadeUsuario);

        String estado_maiusculo = estado.toUpperCase();
        String municipio_maiusculo = municipio.toUpperCase();

        txtLocalidadeUsuario.setText(String.format("%s/%s", municipio_maiusculo, estado_maiusculo));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // Usuário está logado. Ir para a tela principal.
            carregarBrasao();
        } else {
            // Usuário não está logado. Ir para a tela de login.
            brasaoMunicipio.setImageResource(R.drawable.ic_launcher);

        }


    }

    private void carregarBrasao() {
        DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference("cadastros")
                .child(estado)
                .child(municipio)
                .child("config")
                .child("imagem_brasao_municipio");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String urlImagem = snapshot.getValue(String.class);

                    if (urlImagem != null && !urlImagem.isEmpty()) {

                        Glide.with(MainActivity.this)
                                .load(urlImagem)
                                .placeholder(R.drawable.todos_contra_dengue) // opcional
                                .error(R.drawable.pessoa) // opcional
                                .into(brasaoMunicipio);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // =====================================================
    // FLIPPER
    // =====================================================
    private void carregarImagensFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("cadastros/" + estado + "/" + municipio + "/config/sliders_main");

        ref.get().addOnSuccessListener(snapshot -> {
            List<String> urls = new ArrayList<>();

            int x = 1;
            for (DataSnapshot s : snapshot.getChildren()) {
                String u = s.child("image_" + x).getValue(String.class);
                if (u != null) urls.add(u);
                x = x + 1;
            }

            if (urls.isEmpty()) {
                fallbackOuCarregarOffline();
                return;
            }

            salvarUrlsLocais(urls);
            carregarUrlsParaFlipper(urls);

        }).addOnFailureListener(e -> fallbackOuCarregarOffline());
    }

    private void carregarUrlsParaFlipper(List<String> urls) {
        if (v_flipper == null) return;
        v_flipper.removeAllViews();

        AtomicInteger count = new AtomicInteger(0);
        int total = urls.size();

        for (String url : urls) {
            Glide.with(getApplicationContext())
                    .load(url)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.error_image)
                    .into(new CustomTarget<Drawable>() {

                        @Override
                        public void onResourceReady(@NonNull Drawable resource,
                                                    @Nullable Transition<? super Drawable> transition) {
                            adicionarImagem(resource, url, count.incrementAndGet(), total);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            adicionarImagem(errorDrawable, url, count.incrementAndGet(), total);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
    }

    private void adicionarImagem(Drawable drawable, String url, int atual, int total) {
        if (v_flipper == null) return;
        ImageView img = new ImageView(this);
        img.setLayoutParams(new ViewFlipper.LayoutParams(
                ViewFlipper.LayoutParams.MATCH_PARENT,
                ViewFlipper.LayoutParams.MATCH_PARENT));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageDrawable(drawable);

        img.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FullscreenImageActivity.class);
            intent.putExtra("urlMidia", url);
            startActivity(intent);
        });

        v_flipper.addView(img);

        if (atual == total) iniciarFlipper();
    }

    private void iniciarFlipper() {
        if (v_flipper != null && v_flipper.getChildCount() > 1) {
            v_flipper.setFlipInterval(3500);
            v_flipper.startFlipping();
        }

    }

    private void mostrarTutorial() {

        if (tutorial <= 2) {

            tutorial = tutorial + 1;

            // 1. Criar ou abrir o arquivo de preferências
            SharedPreferences sharedPref = getSharedPreferences("configApp", Context.MODE_PRIVATE);
// 2. Salvar dados
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("tutorial", tutorial);
            editor.apply(); // Salva de forma rápida em segundo plano

        } else{
            SharedPreferences sharedPref = getSharedPreferences("configApp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("tutorialFull", true);
            editor.apply();

        }
            View tutorial = findViewById(R.id.layoutTutorial);
            if (tutorial == null) return;

            tutorial.setVisibility(View.VISIBLE);
            tutorial.setAlpha(0f);
            tutorial.setScaleX(0.7f);
            tutorial.setScaleY(0.7f);

            tutorial.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .withEndAction(() -> {

                        ImageView hand = findViewById(R.id.imgHand);
                        if (hand == null) return;

                        hand.animate()
                                .translationY(18)
                                .setDuration(250)
                                .withEndAction(() ->
                                        hand.animate()
                                                .translationY(0)
                                                .setDuration(250)
                                                .withEndAction(() ->

                                                        tutorial.postDelayed(() ->
                                                                tutorial.animate()
                                                                        .alpha(0f)
                                                                        .setDuration(600)
                                                                        .withEndAction(() -> tutorial.setVisibility(View.GONE))
                                                                        .start(), 2000)

                                                ).start())
                                .start();

                    }).start();

    }

    // =====================================================
    // OFFLINE
    // =====================================================
    private void fallbackOuCarregarOffline() {
        List<String> cache = carregarUrlsLocais();
        if (!cache.isEmpty()) carregarUrlsParaFlipper(cache);
        else carregarImagensOffline();
    }

    private void carregarImagensOffline() {
        if (v_flipper == null) return;
        v_flipper.removeAllViews();
        for (int img : imagensOffline) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

            Glide.with(this)
                    .load(img)
                    .into(iv);

            iv.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, FullscreenImageActivity.class);
                intent.putExtra("resourceId", img);
                startActivity(intent);
            });

            v_flipper.addView(iv);
        }
        iniciarFlipper();
    }

    private void salvarUrlsLocais(List<String> urls) {
        getSharedPreferences("configApp", MODE_PRIVATE)
                .edit()
                .putString("cached_flipper_urls", TextUtils.join(",", urls))
                .apply();
    }

    private List<String> carregarUrlsLocais() {
        String s = getSharedPreferences("configApp", MODE_PRIVATE)
                .getString("cached_flipper_urls", null);
        return s == null ? new ArrayList<>() : Arrays.asList(s.split(","));
    }

    // =====================================================
    // LOGOUT (CORRIGIDO)
    // =====================================================
    private void logout() {

        // 1️⃣ Logout Firebase
        FirebaseAuth.getInstance().signOut();

        // 2️⃣ Limpa credenciais do Google (Credential Manager)
        CredentialManager credentialManager = CredentialManager.create(this);

        ClearCredentialStateRequest request =
                new ClearCredentialStateRequest();

        credentialManager.clearCredentialStateAsync(
                request,
                null,
                Runnable::run,
                new CredentialManagerCallback<Void, ClearCredentialException>() {

                    @Override
                    public void onResult(Void result) {
                        Log.d("LOGOUT", "Logout sucesso!");
                        goToLogin();
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        // Mesmo se falhar, continua o logout
                        goToLogin();
                    }
                }
        );
    }


    private void goToLogin() {

        Intent intent = new Intent(MainActivity.this, TelaLoguin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();

        Toast.makeText(
                this,
                "Usuário deslogado. Você pode entrar com outra conta.",
                Toast.LENGTH_LONG
        ).show();
    }


    // =====================================================
    // LISTENERS
    // =====================================================
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageButtonAgentes)
            startActivity(new Intent(this, Agentes.class));
        else if (v.getId() == R.id.imageButtonTrabAgentes)
            startActivity(new Intent(this, TrabalhosAgentes.class));
        else if (v.getId() == R.id.imageButtonDenuncias)
            startActivity(new Intent(this, Denunciar.class));
        else if (v.getId() == R.id.imageButtonDengue)
            startActivity(new Intent(this, Dengue.class));
        else if (v.getId() == R.id.imageButtonEscorpiao)
            startActivity(new Intent(this, Escorpionismo.class));
    }

    // ... (Método onNavigationItemSelected completo)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Seu código original completo para o Drawer (mantido)
        int id = item.getItemId();
        if (id == R.id.nav_configuracoes) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_minhas_reclamacoes) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent intent = new Intent(MainActivity.this, ListarReclamacoesUsuarios.class);
                startActivity(intent);
            } else {
                startActivity(new Intent(MainActivity.this, TelaLoguin.class));
            }
        } else if (id == R.id.nav_telefones_uteis) {
            Intent itents = new Intent(MainActivity.this, TelefonesUteis.class);
            startActivity(itents);
        } else if (id == R.id.nav_descarte_pneus) {
            Intent itentes = new Intent(MainActivity.this, DescartePneus.class);
            startActivity(itentes);
        } else if (id == R.id.nav_descarte_eletronicos) {
            Intent it = new Intent(MainActivity.this, DescarteEletronicos.class);
            startActivity(it);
        } else if (id == R.id.nav_sair) {
            logout();
        } else if (id == R.id.nav_terms) {
            Intent it = new Intent(MainActivity.this, TermosDeUsoActivity.class);
            startActivity(it);
        } else if (id == R.id.nav_send) {
            avaliar_app();
        } else if (id == R.id.nav_sobre) {
            Intent it = new Intent(MainActivity.this, SobreAplicativo.class);
            startActivity(it);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void avaliar_app() {
        String appId = getPackageName();
        Intent intentPlayerStore = new Intent(Intent.ACTION_VIEW);
        try {
            intentPlayerStore.setData(Uri.parse("market://details?id=" + appId));
            startActivity(intentPlayerStore);
        } catch (android.content.ActivityNotFoundException anfe) {
            intentPlayerStore.setData(Uri.parse("htpp://google.com " + appId));
            startActivity(intentPlayerStore);
        }
    }

    // =====================================================
    // CICLO DE VIDA
    // =====================================================
    @Override
    protected void onPause() {
        super.onPause();
        if (v_flipper != null) v_flipper.stopFlipping();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (v_flipper != null && v_flipper.getChildCount() > 1) {
            v_flipper.startFlipping();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String sMunicipio = municipio.toUpperCase();
        String sEstado = estado.toUpperCase();
        txtLocalidadeUsuario.setText(String.format("%s / %s", sMunicipio, sEstado));

        String usuario = (nome_usuario == null || nome_usuario.isEmpty()) ? "Usuário" : nome_usuario;
        txtNomeUsuario.setText(usuario);

        // VERIFICAÇÃO DE REDIRECIONAMENTO COM TRAVA DE RETORNO
        // Verifica se o usuário veio pelo botão voltar da tela de agentes
        boolean veioPeloBotaoVoltar = getIntent().getBooleanExtra("VEIO_PELO_VOLTAR", false);

        if (!veioPeloBotaoVoltar) {

            // Inicialize o FirebaseAuth
            FirebaseAuth mAuth = FirebaseAuth.getInstance();

// Verifique o usuário atual
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {

                if (perfil.equals("admins")) {

                    startActivity(new Intent(MainActivity.this, AdminActivity.class));

                } else if (perfil.equals("agentes")) {

                    startActivity(new Intent(MainActivity.this, AgentesMainActivity.class));

                }
            } else if (perfil.equals("usuarios")) {

                startActivity(new Intent(MainActivity.this, MainActivity.class));

            }
        } else{
            // Se veio pelo botão voltar, nós "limpamos" a flag para que na próxima vez
            // que o app abrir ele funcione normalmente do zero
            getIntent().putExtra("VEIO_PELO_VOLTAR", false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LIFECYCLE", getClass().getSimpleName() + " onDestroy");
    }

}
