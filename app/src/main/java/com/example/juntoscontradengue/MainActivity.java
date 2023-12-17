package com.example.juntoscontradengue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ImageView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ViewFlipper v_flipper;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,  R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawerLayout.setDrawerListener(toggle);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
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
        v_flipper.setFlipInterval(5000); // 5 segundos
        v_flipper.setAutoStart(true);

        // Animation
        v_flipper.setInAnimation(this, android.R.anim.slide_in_left);
        v_flipper.setOutAnimation(this, android.R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            Toast.makeText(getApplicationContext(), "Configurações", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_minhas_reclamacoes) {
            // Handle the camera action
            Toast.makeText(getApplicationContext(), "Reclamações is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_minhas_avaliacoes) {
            Toast.makeText(getApplicationContext(), "Avaliações is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_telefones_uteis) {
            Toast.makeText(getApplicationContext(), "Telefones is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_descarte_pneus) {
            Toast.makeText(getApplicationContext(), "Descarte pneus is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_descarte_eletronicos) {
            Toast.makeText(getApplicationContext(), "Lixo eletrônico is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_terms) {
            Toast.makeText(getApplicationContext(), "Termos is clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_send) {
            Toast.makeText(getApplicationContext(), "Send is clicked", Toast.LENGTH_SHORT).show();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}