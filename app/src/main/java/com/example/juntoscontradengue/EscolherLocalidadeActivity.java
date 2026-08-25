package com.example.juntoscontradengue;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.juntoscontradengue.databinding.ActivityEscolherLocalidadeBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EscolherLocalidadeActivity extends AppCompatActivity {

    private static final String TAG = "EscolherLocalidade";

    private ActivityEscolherLocalidadeBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private ExecutorService executorService;

    // Permission launcher
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (fineLocationGranted || coarseLocationGranted) {
                    verificarLocalizacaoDoUsuario();
                } else {
                    showPermissionDeniedDialog();
                }
            });

    // GPS enable launcher
    private final ActivityResultLauncher<Intent> gpsSettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> verificarLocalizacaoDoUsuario());

    private Spinner spinnerEstados, spinnerMunicipios;
    private ProgressBar progressBar;
    private MaterialButton btnSalvarLocalidade;
    private TextView tvLocalizacaoAutomatica;

    private String estadoSelecionado, municipioSelecionado;
    private final List<String> listaEstados = new ArrayList<>();
    private final List<String> listaMunicipios = new ArrayList<>();
    private ArrayAdapter<String> adapterEstados, adapterMunicipios;
    private DatabaseReference databaseRef;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEscolherLocalidadeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tratarNotificacaoRecebida(getIntent());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();

        if (verificarLocalizacaoSalva()) {
            return; // já foi redirecionado, não precisa montar telas nem chamar Firebase
        }

        initViews();
        setupSpinners();
        setupListeners();
        carregarEstados();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        tratarNotificacaoRecebida(intent);
    }
    private void tratarNotificacaoRecebida(Intent intent) {
        if (intent == null) return;
        String titulo = intent.getStringExtra("titulo");
        String mensagem = intent.getStringExtra("mensagem");
        if (titulo != null || mensagem != null) {
            Intent it = new Intent(this, ActivityVisualizarNotificacao.class);
            it.putExtra(ActivityVisualizarNotificacao.EXTRA_TITULO, titulo);
            it.putExtra(ActivityVisualizarNotificacao.EXTRA_MENSAGEM, mensagem);
            startActivity(it);
            intent.removeExtra("titulo");
            intent.removeExtra("mensagem");

        }
    }

    // agora retorna boolean: true = já redirecionou
    private boolean verificarLocalizacaoSalva() {
        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        String estadoSalvo = prefs.getString("estado", null);
        String municipioSalvo = prefs.getString("municipio", null);

        if (estadoSalvo != null && municipioSalvo != null) {
            startActivity(new Intent(this, VideoIniciarAppActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void initViews() {
        spinnerEstados = binding.spinnerEstadosBrasil;
        spinnerMunicipios = binding.spinnerMunicipios;
        progressBar = binding.progressBar;
        btnSalvarLocalidade = binding.btnSalvarLocalidade;
        tvLocalizacaoAutomatica = binding.tvLocalizacaoAutomatica;
    }

    private void setupSpinners() {
        adapterEstados = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                listaEstados
        );
        adapterEstados.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerEstados.setAdapter(adapterEstados);

        adapterMunicipios = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                listaMunicipios
        );
        adapterMunicipios.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerMunicipios.setAdapter(adapterMunicipios);
    }

    private void setupListeners() {
        spinnerEstados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    estadoSelecionado = listaEstados.get(position).toLowerCase();
                    carregarMunicipios(estadoSelecionado);
                } else {
                    estadoSelecionado = null;
                    listaMunicipios.clear();
                    listaMunicipios.add(getString(R.string.selecione_municipio));
                    adapterMunicipios.notifyDataSetChanged();
                }
                atualizarBotoes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMunicipios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    municipioSelecionado = listaMunicipios.get(position).toLowerCase();
                } else {
                    municipioSelecionado = null;
                }
                atualizarBotoes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSalvarLocalidade.setOnClickListener(v -> salvarDados());
    }

    private void atualizarBotoes() {
        boolean estadoValido = estadoSelecionado != null && !estadoSelecionado.isEmpty();
        boolean municipioValido = municipioSelecionado != null && !municipioSelecionado.isEmpty();
        btnSalvarLocalidade.setEnabled(estadoValido && municipioValido && !isLoading);
    }

    private void carregarEstados() {
        databaseRef = FirebaseDatabase.getInstance().getReference("config_estados_municipios");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaEstados.clear();
                listaEstados.add(getString(R.string.selecione_estado));
                for (DataSnapshot estadoSnapshot : snapshot.getChildren()) {
                    if (estadoSnapshot.getKey() != null) {
                        listaEstados.add(estadoSnapshot.getKey().toUpperCase());
                    }
                }
                adapterEstados.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EscolherLocalidadeActivity.this,
                        getString(R.string.erro_carregar_estados, error.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void carregarMunicipios(String estado) {
        databaseRef.child(estado).child("municipios")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaMunicipios.clear();
                        listaMunicipios.add(getString(R.string.selecione_municipio));
                        for (DataSnapshot m : snapshot.getChildren()) {
                            String nome = m.getKey();
                            if (nome != null) {
                                listaMunicipios.add(capitalize(nome));
                            }
                        }
                        adapterMunicipios.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EscolherLocalidadeActivity.this,
                                getString(R.string.erro_carregar_municipios, error.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void salvarDados() {
        if (estadoSelecionado == null || municipioSelecionado == null) {
            Toast.makeText(this, getString(R.string.selecione_estado_municipio), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.sem_conexao_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        checarPermissoesEProsseguir();
    }

    private void checarPermissoesEProsseguir() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            verificarLocalizacaoDoUsuario();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void verificarLocalizacaoDoUsuario() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            showPermissionDeniedDialog();
            return;
        }

        if (!isGPSEnabled()) {
            showGPSEnalbeDialog();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.validacao_pode_falhar_sem_internet), Toast.LENGTH_LONG).show();
        }

        getLocationWithFallback();
    }

    private void getLocationWithFallback() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            showPermissionDeniedDialog();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        validarCidadePorCoordenadas(location.getLatitude(), location.getLongitude());
                    } else {
                        // getLastLocation() pode voltar null (aparelho recém ligou o GPS,
                        // nunca teve fix antes, etc.) — nesse caso pedimos uma atualização
                        // ativa em vez de deixar o fluxo travado no loading.
                        solicitarAtualizacaoLocalizacao();
                    }
                })
                .addOnFailureListener(e -> solicitarAtualizacaoLocalizacao());
    }

    /**
     * Fallback quando não há última localização conhecida: pede uma atualização
     * ativa de GPS (uma única leitura de alta precisão) via requestLocationUpdates.
     */
    private void solicitarAtualizacaoLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            showPermissionDeniedDialog();
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    validarCidadePorCoordenadas(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(EscolherLocalidadeActivity.this,
                            getString(R.string.nao_foi_possivel_obter_localizacao), Toast.LENGTH_LONG).show();
                    setLoading(false);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @SuppressWarnings("deprecation") // getFromLocation(double,double,int) só é usado no ramo < TIRAMISU;
    // a alternativa assíncrona (com GeocodeListener) só existe a partir da API 33.
    private void validarCidadePorCoordenadas(double latitude, double longitude) {
        tvLocalizacaoAutomatica.setVisibility(View.VISIBLE);
        tvLocalizacaoAutomatica.setText(getString(R.string.validando_localizacao));

        executorService.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1, this::processarEnderecos);
            } else {
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    processarEnderecos(addresses);
                } catch (IOException e) {
                    Log.e(TAG, "Erro ao validar endereço via Geocoder", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.erro_validar_endereco, e.getMessage()), Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    });
                }
            }
        });
    }

    private void processarEnderecos(List<Address> addresses) {
        runOnUiThread(() -> {
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String cidadeAtual = address.getLocality();
                if (cidadeAtual == null || cidadeAtual.isEmpty()) {
                    cidadeAtual = address.getSubAdminArea();
                }
                if (cidadeAtual == null || cidadeAtual.isEmpty()) {
                    cidadeAtual = address.getAdminArea();
                }

                String estadoAtual = address.getAdminArea();

                tvLocalizacaoAutomatica.setText(getString(
                        R.string.localizacao_detectada,
                        cidadeAtual != null ? cidadeAtual : getString(R.string.desconhecida),
                        estadoAtual != null ? estadoAtual : getString(R.string.desconhecido)
                ));

                if (cidadeAtual != null && !cidadeAtual.isEmpty()) {
                    String cidadeSelecionada = normalizeString(municipioSelecionado);
                    String cidadeDetectada = normalizeString(cidadeAtual);

                    if (cidadeDetectada.equals(cidadeSelecionada)) {
                        executarFluxoDeCadastro();
                    } else {
                        showCidadeDiferenteDialog(cidadeAtual);
                        setLoading(false);
                    }
                } else {
                    if (estadoAtual != null && !estadoAtual.isEmpty()) {
                        String estadoSelecionadoNormalizado = normalizeString(estadoSelecionado);
                        String estadoDetectadoNormalizado = normalizeString(estadoAtual);

                        if (estadoDetectadoNormalizado.equals(estadoSelecionadoNormalizado)) {
                            Toast.makeText(this,
                                    getString(R.string.estado_confirmado, estadoAtual),
                                    Toast.LENGTH_LONG).show();
                            executarFluxoDeCadastro();
                        } else {
                            showLocalizacaoDiferenteDialog();
                        }
                    } else {
                        showLocalizacaoDiferenteDialog();
                    }
                    setLoading(false);
                }
            } else {
                Toast.makeText(this, getString(R.string.nao_foi_possivel_identificar_localizacao), Toast.LENGTH_LONG).show();
                setLoading(false);
            }
        });
    }

    private void showCidadeDiferenteDialog(String cidadeAtual) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.titulo_localizacao_diferente))
                .setMessage(getString(
                        R.string.mensagem_localizacao_diferente,
                        cidadeAtual != null ? cidadeAtual : getString(R.string.localidade_desconhecida),
                        capitalize(municipioSelecionado)
                ))
                .setPositiveButton(getString(R.string.sim), (dialog, which) -> {
                    if (cidadeAtual != null) {
                        String cidadeAtualNormalizada = normalizeString(cidadeAtual);
                        for (int i = 0; i < listaMunicipios.size(); i++) {
                            if (normalizeString(listaMunicipios.get(i)).equals(cidadeAtualNormalizada)) {
                                spinnerMunicipios.setSelection(i);
                                setLoading(false);
                                return;
                            }
                        }
                    }
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.titulo_municipio_nao_cadastrado))
                            .setMessage(getString(R.string.mensagem_municipio_nao_cadastrado))
                            .setPositiveButton(getString(R.string.ok), (d, w) -> setLoading(false))
                            .setCancelable(false)
                            .show();
                })
                .setNegativeButton(getString(R.string.nao), (dialog, which) -> setLoading(false))
                .setCancelable(false)
                .show();
    }

    private void showLocalizacaoDiferenteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.titulo_localizacao_nao_confirmada))
                .setMessage(getString(R.string.mensagem_localizacao_nao_confirmada))
                .setPositiveButton(getString(R.string.continuar_dialog), (dialog, which) -> executarFluxoDeCadastro())
                .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> setLoading(false))
                .setCancelable(false)
                .show();
    }

    private String normalizeString(String input) {
        if (input == null) return "";
        String normalized = input.toLowerCase().trim();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized;
    }

    private void executarFluxoDeCadastro() {
        SharedPreferences.Editor editor =
                getSharedPreferences("configApp", MODE_PRIVATE).edit();

        editor.putString("estado", estadoSelecionado.toLowerCase().trim());
        editor.putString("municipio", municipioSelecionado.toLowerCase().trim());
        editor.apply();

        Toast.makeText(this,
                getString(R.string.local_salvo, municipioSelecionado, estadoSelecionado.toUpperCase()),
                Toast.LENGTH_SHORT).show();

        setLoading(false);
        startActivity(new Intent(this, VideoIniciarAppActivity.class));
        finish();
    }

    private boolean isGPSEnabled() {
        android.location.LocationManager locationManager =
                (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    private void showGPSEnalbeDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.titulo_ativar_gps))
                .setMessage(getString(R.string.mensagem_ativar_gps))
                .setPositiveButton(getString(R.string.configurar), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    gpsSettingsLauncher.launch(intent);
                })
                .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> setLoading(false))
                .setCancelable(false)
                .show();
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.titulo_permissao_localizacao))
                .setMessage(getString(R.string.mensagem_permissao_localizacao))
                .setPositiveButton(getString(R.string.configuracoes), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> setLoading(false))
                .setCancelable(false)
                .show();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        runOnUiThread(() -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSalvarLocalidade.setEnabled(!loading &&
                    estadoSelecionado != null &&
                    municipioSelecionado != null);
            btnSalvarLocalidade.setText(loading ? getString(R.string.validando) : getString(R.string.continuar));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isFinishing()) return; // já foi redirecionado no onCreate, evita disparo duplo

        if (!NetworkUtils.isNetworkAvailable(this)) {

            SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
           String estado = prefs.getString("estado", null);

            if(TextUtils.isEmpty(estado)){
                Alertas.showAlertDialog(EscolherLocalidadeActivity.this, "Alerta", "Para o primeiro uso é necessário internet para configurar o aplicativo.");
             return;
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }
    }
}