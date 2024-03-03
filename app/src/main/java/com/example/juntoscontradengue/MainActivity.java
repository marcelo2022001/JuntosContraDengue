package com.example.juntoscontradengue;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    ChildEventListener childEventListenerMain;
    ViewFlipper v_flipper;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    ImageButton btn_agentes, btn_trab_agentes, btn_denuncias, btn_dengue, btn_escorpiao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         drawerLayout = findViewById(R.id.drawer_layout);
/*
        if(FirebaseAuth.getInstance().getCurrentUser() != null){

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            final String uid = user.getUid();

             databaseReference = database.getReference().child( "usuarios" ).child( uid );
            childEventListenerMain = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    ClassUsuarios classUsuarios = snapshot.getValue( ClassUsuarios.class );
                    String apelido_usuario = classUsuarios.getAlias();
                    Toast.makeText( getApplicationContext(), "Bem vindo! " + apelido_usuario, Toast.LENGTH_LONG ).show();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };

        }
*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,  R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawerLayout.setDrawerListener(toggle);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);


        int[] imagens = {R.drawable.image3_larvas, R.drawable.image5_acoes_contra_dengue, R.drawable.image6_dengue_pneus};

        v_flipper = findViewById(R.id.v_flipper);

//Loop

        // for(int i = 0; i < imagens.length; i++){
        //    flipperImagens(imagens[i]);
        // }

        // But I prefer foreach
        for (int image : imagens) {
            flipperImagens(image);
        }
    }


    public void flipperImagens(int image) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(image);

        v_flipper.addView(imageView);
        v_flipper.setFlipInterval(3000); // 3 segundos
        v_flipper.setAutoStart(true);

        // Animation
        v_flipper.setInAnimation(this, android.R.anim.slide_in_left);
        v_flipper.setOutAnimation(this, android.R.anim.slide_out_right);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                startActivity( new Intent(MainActivity.this, AdminMain.class) );
            }
            else{
                startActivity( new Intent(MainActivity.this, TelaLoguin.class) );
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_minhas_reclamacoes) {

            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                Intent intent = new Intent( MainActivity.this, MinhasDenuncias.class );
                startActivity( intent );
            }else{
                startActivity(new Intent( MainActivity.this, TelaLoguin.class ));
            }

        } else if (id == R.id.nav_minhas_avaliacoes) {
            Toast.makeText(getApplicationContext(), "Avaliações is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_telefones_uteis) {
            Intent itents = new Intent(MainActivity.this, TelefonesUteis.class);
            startActivity(itents);

            
        } else if (id == R.id.nav_descarte_pneus) {

            Intent itentes = new Intent(MainActivity.this, DescartePneus.class);
            startActivity(itentes);


        } else if (id == R.id.nav_descarte_eletronicos) {
            Intent it = new Intent(MainActivity.this, DescarteEletronicos.class);
            startActivity(it);

        } else if (id == R.id.nav_terms) {
            Toast.makeText(getApplicationContext(), "Termos is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_send) {
            Toast.makeText(getApplicationContext(), "Send is clicked", Toast.LENGTH_SHORT).show();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onClick(View view) {
        int v = view.getId();
        if(v == R.id.imageButtonAgentes){
            startActivity(new Intent(this, Agentes.class));


        } else if (v == R.id.imageButtonTrabAgentes)  {
            Intent it = new Intent(this, TrabalhosAgentes.class);
            startActivity(it);
        }

        else if (v == R.id.imageButtonDenuncias)  {
            Intent it = new Intent(this, Denuncias.class);
            startActivity(it);
        }

        else if (v == R.id.imageButtonDengue)  {
            Intent it = new Intent(this, Dengue.class);
            startActivity(it);
        }

        else if (v == R.id.imageButtonEscorpiao)  {
            Intent it = new Intent(this, Escorpionismo.class);
            startActivity(it);
        }

    }

}