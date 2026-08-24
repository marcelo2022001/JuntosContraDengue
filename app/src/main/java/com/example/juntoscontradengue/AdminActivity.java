package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbarAdmin;
    DrawerLayout drawerLayoutAdmin;
    NavigationView navigationViewAdmin;
    ImageButton imgBtnEnviarPush;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout_admin);

        SharedPreferences prefUser = getSharedPreferences("UserData", MODE_PRIVATE);
        String nome_usuario = Objects.requireNonNull(prefUser.getString("nome", "")).toUpperCase();
        // Agora são Strings normais!
        String email = Objects.requireNonNull(prefUser.getString("email", "")).toLowerCase();

        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Sua lógica personalizada aqui
                mostrarDialogoSair();
            }
        };

        // Adicionar ao dispatcher
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    
        toolbarAdmin = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarAdmin);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayoutAdmin = findViewById(R.id.drawer_layout_admin);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayoutAdmin, toolbarAdmin,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        //drawerLayout.setDrawerListener(toggle);
        drawerLayoutAdmin.addDrawerListener(toggle);
        toggle.syncState();

        navigationViewAdmin = findViewById(R.id.nav_view_admin);
        navigationViewAdmin.setItemIconTintList(null);
        navigationViewAdmin.setNavigationItemSelectedListener(this);

        View header = navigationViewAdmin.getHeaderView(0);
        TextView txtHeaderNomeAdm = header.findViewById(R.id.txtHeaderNomeAdm);
        TextView txtHeaderEmailAdm = header.findViewById(R.id.txtxHeaderEmailAdm);

        // Exibe os dados do SharedPreferences na tela do Header
        if (txtHeaderNomeAdm != null) {
            txtHeaderNomeAdm.setText(nome_usuario);
        }
        if (txtHeaderEmailAdm != null) {
            txtHeaderEmailAdm.setText(email);
        }

        /*
        MENSAGEM
        PUSH
         */
        imgBtnEnviarPush = findViewById(R.id.btn_tela_admin_EnviarMsgTodosApp);
        imgBtnEnviarPush.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ActivityAdminPush.class)));
    }

    private void mostrarDialogoSair() {
        new AlertDialog.Builder(this)
                .setTitle("Atenção")
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Se realmente quiser sair, pode chamar finish()
                   // finish();
                    logout();
                    // Ou se quiser sair completamente do app:
                    // finishAffinity();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void logout() {
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
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();

        Toast.makeText(
                this,
                "Usuário deslogado. Você pode entrar com outra conta.",
                Toast.LENGTH_LONG
        ).show();
    }

    //ImagemButton da tela admin

    public void onClickAdminAcompanharReclamacoes(View v)
    {
        Intent it = new Intent(this, ListarReclamacoesAgentes.class);
        startActivity(it);
    }

    public void onClickAdminAddAgentes(View v)
    {
        Intent it = new Intent(this, AddAgentes.class);
        startActivity(it);
    }


    public void onClickAdminRemoveAgentes(View v)
    {
        Intent it = new Intent(this, ExcluirProfissionaisPreCadastro.class);
        startActivity(it);
    }

    public void onClickAdminAddTrabAgentes(View v)
    {
        Intent it = new Intent(this, UploadTrabAgentes.class);
        it.putExtra("id_activity", "agentes_upload_trab_admin");
        startActivity(it);
    }

    public void onClickAdminRemoveTrabAgentes(View v)
    {
        Intent it = new Intent(this, ExcluirTrabAgentesActivity.class);
        startActivity(it);
    }

    public void onClickAdminAddVideoInicioApp(View v)
    {
        Intent it = new Intent(this, SubstituirVideoInicialActivity.class);
        startActivity(it);
    }

    public void onClickAdminProfile(View v)
    {
        Intent itent_admin = new Intent(this, ProfileActivity.class);
        itent_admin.putExtra("tipo_conta", "admin");
        startActivity(itent_admin);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_add_sliders) {

            Intent it = new Intent(this, AddSlidersMain.class);
            startActivity(it);

        } else if (id == R.id.nav_add_fones_uteis) {

                Intent intent = new Intent( AdminActivity.this, AddTelefonesUteis.class );
                startActivity( intent );

        } else if (id == R.id.nav_locais_descartes) {

            startActivity(new Intent(AdminActivity.this, AddLocalDescartesPneusEletronicos.class));

        } else if (id == R.id.nav_excluir_telefones) {

            Intent itents = new Intent(AdminActivity.this, ExcluirTelefonesUteisActivity.class);
            startActivity(itents);

        } else if (id == R.id.nav_excluir_locais_eletronicos) {

            Intent itents = new Intent(AdminActivity.this, ExcluirLocaisDescartesEletronicosActivity.class);
            startActivity(itents);

        } else if (id == R.id.nav_excluir_locais_pneus) {

            Intent itents = new Intent(AdminActivity.this, ExcluirLocaisDescartesPneus.class);
            startActivity(itents);

        } else if (id == R.id.nav_sair_admin) {

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
                            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finishAffinity();
                        }

                        @Override
                        public void onError(@NonNull ClearCredentialException e) {
                            // Mesmo se falhar, continua o logout
                            startActivity(new Intent(AdminActivity.this, MainActivity.class));
                        }
                    }
            );
        } else if(id == R.id.nav_terms_adm){

            Intent it = new Intent(AdminActivity.this, TermosDeUsoActivity.class);
            startActivity(it);

        } else if(id == R.id.nav_send_adm){

            avaliar_app();

        } else if (id == R.id.nav_compras) {
            startActivity(new Intent(AdminActivity.this, SolicitacaoPlano.class));

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_admin);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private void avaliar_app() {
            String appId = getPackageName();
            Intent intentPlayerStore = new Intent(Intent.ACTION_VIEW);
            try{
                intentPlayerStore.setData(Uri.parse("market://details?id=" + appId));
                startActivity(intentPlayerStore);
            } catch (android.content.ActivityNotFoundException anfe) {
                intentPlayerStore.setData(Uri.parse("htpp://google.com " + appId));
                startActivity(intentPlayerStore);
            }
        }
    }



