package com.example.juntoscontradengue;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class JuntosContraDengueApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}