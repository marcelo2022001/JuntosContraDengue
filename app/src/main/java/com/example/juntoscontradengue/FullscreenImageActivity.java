package com.example.juntoscontradengue;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Objects;

public class FullscreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        Toolbar toolbar = findViewById(R.id.toolbar_fullscreen_image);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        PhotoView imgViewFullScreen = findViewById(R.id.imgViewFullScreen);
        String urlMidia = getIntent().getStringExtra("urlMidia");
        int resourceId = getIntent().getIntExtra("resourceId", -1);

        if (urlMidia != null) {
            Glide.with(this)
                    .load(urlMidia)
                    .into(imgViewFullScreen);
        } else if (resourceId != -1){
            Glide.with(this)
                    .load(resourceId)
                    .into(imgViewFullScreen);
        }
        // Tela cheia e manter a toolbar

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}