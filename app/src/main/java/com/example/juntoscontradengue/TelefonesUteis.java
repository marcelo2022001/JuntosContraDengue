package com.example.juntoscontradengue;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.juntoscontradengue.database.adapters.AdapterTelefone;
import com.example.juntoscontradengue.database.classes_database.ClassTelefonesUteis;
import com.example.juntoscontradengue.databinding.ActivityTelefonesUteisBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TelefonesUteis extends AppCompatActivity implements AdapterTelefone.ClickTelefones {

    private static final int CALL_PHONE_REQUEST_CODE = 1;
    private ActivityTelefonesUteisBinding binding;
    private DatabaseReference databaseReference;
    private ValueEventListener telefonesListener;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private AdapterTelefone adapterTelefone;
    private final List<ClassTelefonesUteis> telefoneList = new ArrayList<>();
    private final List<ClassTelefonesUteis> filteredList = new ArrayList<>();
    private String lastQuery = "", estado, municipio, lastPhoneNumber;
    private boolean redirecionadoSemInternet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTelefonesUteisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = Objects.requireNonNull(prefs.getString("estado", null)).toLowerCase();
        municipio = Objects.requireNonNull(prefs.getString("municipio", null)).toLowerCase();

        atualizarBannerOffline();
        setupToolbar();
        initializeFirebase();
        setupRecyclerView();
        setupSearch();
        carregarTelefones();
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
                    recarregarTelefones();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(TelefonesUteis.this::atualizarBannerOffline);
            }
        };

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private void removerNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException e) {
                // callback já não estava registrado; ignora
            }
        }
    }

    private void atualizarBannerOffline() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);
        binding.txtAvisoOfflineTelefones.setVisibility(isConnected ? View.GONE : View.VISIBLE);
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbarTelefoneUteis;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getTitle());
    }

    private void initializeFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("cadastros/" + estado + "/" + municipio + "/telefones_uteis");

        // Mantém este nó sincronizado ativamente enquanto há internet.
        // A persistência em disco em si já é garantida globalmente pelo
        // setPersistenceEnabled(true) na JuntosContraDengueApp.
        databaseReference.keepSynced(true);
    }

    private void setupRecyclerView() {
        binding.rvTelefones.setLayoutManager(new LinearLayoutManager(this));
        adapterTelefone = new AdapterTelefone(this, telefoneList, this);
        binding.rvTelefones.setAdapter(adapterTelefone);
    }

    private void setupSearch() {
        binding.edtPesquisaFone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(lastQuery)) {
                    lastQuery = query;
                    filterContacts(query);
                }
            }
        });
    }

    private void filterContacts(String query) {
        if (query.isEmpty()) {
            adapterTelefone.updateList(telefoneList);
            return;
        }

        filteredList.clear();
        String lowerCaseQuery = query.toLowerCase();

        for (ClassTelefonesUteis contact : telefoneList) {
            if (contact.getLocal() != null &&
                    contact.getLocal().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(contact);
            }
        }
        adapterTelefone.updateList(filteredList);
    }

    @Override
    public void click_Telefones(ClassTelefonesUteis telefonesClass) {
        String numero = telefonesClass.getTelefone();
        if (numero != null && !numero.isEmpty()) {
            lastPhoneNumber = numero;
            makePhoneCall(numero);
        }
    }

    private void makePhoneCall(String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            requestCallPermission();
        }
    }

    private void requestCallPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CALL_PHONE},
                CALL_PHONE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PHONE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (lastPhoneNumber != null) {
                    makePhoneCall(lastPhoneNumber);
                }
            } else {
                Toast.makeText(this,
                        "Permissão para ligar foi negada",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void recarregarTelefones() {
        if (databaseReference == null) return;

        if (telefonesListener != null) {
            databaseReference.removeEventListener(telefonesListener);
        }
        redirecionadoSemInternet = false;
        carregarTelefones();
    }

    private void carregarTelefones() {
        telefonesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                atualizarBannerOffline();
                boolean isConnected = NetworkUtils.isNetworkAvailable(TelefonesUteis.this);

                telefoneList.clear();

                if (!snapshot.exists()) {
                    if (isConnected) {
                        adapterTelefone.updateList(telefoneList);
                    } else {
                        irParaSemInternet();
                    }
                    return;
                }

                for (DataSnapshot telefoneSnapshot : snapshot.getChildren()) {
                    ClassTelefonesUteis telefone = telefoneSnapshot.getValue(ClassTelefonesUteis.class);
                    if (telefone != null) {
                        telefone.setId(telefoneSnapshot.getKey());
                        telefoneList.add(telefone);
                    }
                }

                filterContacts(lastQuery);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TELEFONE_FIREBASE", "Erro ao carregar lista de telefones: " + error.getMessage());

                if (!NetworkUtils.isNetworkAvailable(TelefonesUteis.this)) {
                    irParaSemInternet();
                }
            }
        };

        databaseReference.addValueEventListener(telefonesListener);
    }

    private void irParaSemInternet() {
        if (redirecionadoSemInternet) return;
        redirecionadoSemInternet = true;

        Intent intent = new Intent(TelefonesUteis.this, SemInternetActivity.class);
        intent.putExtra("id_activity", "telefones_uteis");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null && telefonesListener != null) {
            databaseReference.removeEventListener(telefonesListener);
        }
        binding = null;
    }
}