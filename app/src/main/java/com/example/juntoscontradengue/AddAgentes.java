package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.DateTimeSaver;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.example.juntoscontradengue.extras.ValidaCpf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddAgentes extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton rbSelecione, rbAdmin, rbAgente, rbOutro;
    CheckBox chkBoxMostrarImagemAgente;
    EditText edtCpfAgenteCad, edtOutroAgenteCad, edtNomeCad;
    TextView imgCadastroApareceraTelaAgentes;
    String estado, municipio, db_salvar_pre_cadastro, sCpf, sNomeCad;
    String decrementarOnde, verifica_outro_pre_cadastro, funcao_pre_cadastro;
    Long data_atual;
    Boolean autoriza_uso_imagem, radioButtomsel = false;
    Long totalAdminPodeCadastrar, totalAgentePodeCadastrar;
    private String sFuncao_agente = ""; // agente ou admin
    private FirebaseDatabase databaseMunicipio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_agentes);

        checkNetworkConnection();

        Toolbar toolbarAddAggentes = findViewById(R.id.toolbarAddAgentes);
        setSupportActionBar(toolbarAddAggentes);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + ".firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);


        edtCpfAgenteCad = findViewById(R.id.edtCpfCadAgentes);
        edtCpfAgenteCad.addTextChangedListener(MaskEditUtil.mask(MaskEditUtil.FORMAT_CPF));
        edtNomeCad = findViewById(R.id.edtNomeCadAgentes);

        // No firebase verifica se total_admins e total_agentes, outros é igual a 0, se não pode cadastrar
        verificarPossibilidadeNovoCadastroAdmin();
        verificarPossibilidadeNovoCadastroAgentes();


        radioGroup = findViewById(R.id.rdGroup);
        rbSelecione = findViewById(R.id.radioButtonSelOpcao);
        rbAdmin = findViewById(R.id.radioButtonAdmin);
        rbAgente = findViewById(R.id.radioButtonAgente);
        rbOutro = findViewById(R.id.radioButtonOutros);
        edtOutroAgenteCad = findViewById(R.id.edtOutrosCadAgentes);
        chkBoxMostrarImagemAgente = findViewById(R.id.chekBoxMostrarImagemAgente);
        imgCadastroApareceraTelaAgentes = findViewById(R.id.msgSobreFotoSeAparecera);

        // Define o ouvinte de mudança de seleção
        radioGroup.setOnCheckedChangeListener( (group, checkedId) -> {
            // checkedId é o ID do RadioButton selecionado

            if(checkedId == R.id.radioButtonSelOpcao) {

                chkBoxMostrarImagemAgente.setChecked(false);
                chkBoxMostrarImagemAgente.setVisibility(View.GONE);
                edtOutroAgenteCad.setVisibility(View.GONE);
                radioButtomsel = false;

            } else if(checkedId == R.id.radioButtonAdmin) {

                edtOutroAgenteCad.setVisibility(View.GONE);
                chkBoxMostrarImagemAgente.setVisibility(View.GONE);
                edtOutroAgenteCad.setVisibility(View.GONE);
                    db_salvar_pre_cadastro = "pre_cadastro_admins";
                    decrementarOnde = "total_admins";
                     radioButtomsel = false;

            } else if(checkedId == R.id.radioButtonAgente) {

                chkBoxMostrarImagemAgente.setVisibility(View.VISIBLE);
                chkBoxMostrarImagemAgente.setChecked(true);
                   chkBoxMostrarImagemAgente.setEnabled(false);
                   edtOutroAgenteCad.setVisibility(View.GONE);
                   db_salvar_pre_cadastro = "pre_cadastro_agentes";
                   decrementarOnde = "total_agentes";
                   radioButtomsel = false;

            } else if (checkedId == R.id.radioButtonOutros) {

                edtOutroAgenteCad.setVisibility(View.VISIBLE);
                chkBoxMostrarImagemAgente.setVisibility(View.VISIBLE);
                   chkBoxMostrarImagemAgente.setChecked(false);
                   chkBoxMostrarImagemAgente.setEnabled(true);
                  imgCadastroApareceraTelaAgentes.setVisibility(View.VISIBLE);
                db_salvar_pre_cadastro = "pre_cadastro_agentes";
                   decrementarOnde = "total_agentes";
                   radioButtomsel = true;
               }
            });

        Button btnAddAgente = findViewById(R.id.btnCadAgentes);

        Button btnCancelAgente = findViewById(R.id.btnCancelCadAgentes);
        btnCancelAgente.setOnClickListener(v -> {
            Intent itentVoltaTelaAdmin = new Intent(AddAgentes.this, AdminActivity.class);
            startActivity(itentVoltaTelaAdmin);
        });

        btnAddAgente.setOnClickListener(v -> salvarUsuario());
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_add_agentes_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_info) {
            new AlertDialog.Builder(this)
                    .setTitle("Informações")
                    .setMessage("Total de cadastro de administrador possível: " + totalAdminPodeCadastrar + "\n" +
                            "Total de cadastro de agentes ou outros possível: " + totalAgentePodeCadastrar )
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void verificarPossibilidadeNovoCadastroAdmin() {

            String pathConfig = "/config/total_admins";

            databaseMunicipio.getReference(pathConfig)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                totalAdminPodeCadastrar = snapshot.getValue(Long.class);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

    }

    private void verificarPossibilidadeNovoCadastroAgentes() {

            String pathConfig = "/config/total_agentes";

        databaseMunicipio.getReference(pathConfig)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                totalAgentePodeCadastrar = snapshot.getValue(Long.class);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

    private void checkNetworkConnection() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);
        if (!isConnected) {
            Intent intent = new Intent(this, SemInternetActivity.class);
            intent.putExtra("id_Activity", "cadastro_agente");
            startActivity(intent);
        }
    }

    private void salvarUsuario() {

        if (rbAdmin.isChecked() && totalAdminPodeCadastrar <= 0) {
            // Não pode cadastrar administrador
            AlertDialog alertDialog = new AlertDialog.Builder(AddAgentes.this).create();

            alertDialog.setTitle("Limite atingido");
            alertDialog.setMessage("O limite de agentes foi atingido.\n\n" +
                    "Remova um profissional ou um pré-cadastro não finalizado.\n\n" +
                    "Deseja conhecer nossos planos para cadastrar novos administradores?");

            // Botão SIM
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SIM",
                    (dialog, which) -> {
                        Intent intent = new Intent(AddAgentes.this, SolicitacaoPlano.class);
                        startActivity(intent);
                        dialog.dismiss();
                    });

            // Botão NÃO
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NÃO",
                    (dialog, which) -> dialog.dismiss());

            alertDialog.show();

            return;
        }

        if ((rbAgente.isChecked() || rbOutro.isChecked()) && totalAgentePodeCadastrar <= 0) {
            // Não pode cadastrar agente/outro
            AlertDialog alertDialog = new AlertDialog.Builder(AddAgentes.this).create();

            alertDialog.setTitle("Limite atingido");
            alertDialog.setMessage("O limite de agentes foi atingido.\n\n" +
                    "Remova um profissional ou um pré-cadastro não finalizado.\n\n" +
                    "Deseja conhecer nossos planos para realizar novos cadastros?");

            // Botão SIM
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SIM",
                    (dialog, which) -> {
                        Intent intent = new Intent(AddAgentes.this, SolicitacaoPlano.class);
                        startActivity(intent);
                        dialog.dismiss();
                    });

            // Botão NÃO
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NÃO",
                    (dialog, which) -> dialog.dismiss());

            alertDialog.show();

            return;
        }


        // Só prossegue se validaDados() retornar true
        if (!validaDados()) {
            return; // Interrompe a execução aqui se o CPF for inválido
        }

        // Verifica se já tem cpf no pre cadastro
        verificaPreCadastro(new CadastroCallback() {
            @Override
            public void onExists() {
                // CPF já cadastrado
            }

            @Override
            public void onNotExists() {

               verificaOutroPreCadastro(new CadastroCallback() {
                   @Override
                   public void onExists() {

                   }

                   @Override
                   public void onNotExists() {
                       autoriza_uso_imagem = chkBoxMostrarImagemAgente.isChecked();
                       salvar_dados_db();
                   }

                   @Override
                   public void onError(String errorMessage) {

                   }
               });

                          }

            @Override
            public void onError(String errorMessage) {
                // Tratar erro
                Toast.makeText(AddAgentes.this, "Erro: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void verificaPreCadastro(CadastroCallback callback) {
        sCpf = edtCpfAgenteCad.getText().toString().trim();
        sCpf = sCpf.replaceAll("[^0-9]", "");

        if("pre_cadastro_admins".equals(db_salvar_pre_cadastro)) {
            verifica_outro_pre_cadastro = "pre_cadastro_agentes";
        } else {
            verifica_outro_pre_cadastro = "pre_cadastro_admins";
        }


        databaseMunicipio.getReference()
                .child("config")
                .child(db_salvar_pre_cadastro)
                .child(sCpf)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String mensagem;

                            if (snapshot.child("funcao_pre_cadastro").exists()) {
                                funcao_pre_cadastro = snapshot.child("funcao_pre_cadastro")
                                        .getValue(String.class);

                                 mensagem = "Este CPF já está pré cadastrado como: " +  funcao_pre_cadastro;
                            } else {
                                 mensagem = "Este CPF já está pré cadastrado.";

                            }

                            Alertas.showAlertDialog(AddAgentes.this, "Aviso", mensagem);
                            callback.onExists();
                        } else {
                            callback.onNotExists();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Alertas.showAlertDialog(AddAgentes.this, "Aviso", "Erro: " + error);
                        callback.onError(error.getMessage());
                    }
                });
    }

    private void verificaOutroPreCadastro(CadastroCallback callback) {

        databaseMunicipio.getReference()
                .child("config")
                .child(verifica_outro_pre_cadastro)
                .child(sCpf)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String mensagem;

                            if (snapshot.child("funcao_pre_cadastro").exists()) {
                                funcao_pre_cadastro = snapshot.child("funcao_pre_cadastro")
                                        .getValue(String.class);

                                mensagem = "Este CPF já está pré cadastrado como: " +  funcao_pre_cadastro;
                            } else {
                                mensagem = "Este CPF já está pré cadastrado.";

                            }

                            Alertas.showAlertDialog(AddAgentes.this, "Aviso", mensagem);
                            callback.onExists();
                        } else {
                            callback.onNotExists();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Alertas.showAlertDialog(AddAgentes.this, "Aviso", "Erro: " + error);
                        callback.onError(error.getMessage());
                    }
                });
    }

    interface CadastroCallback {
        void onExists();
        void onNotExists();
        void onError(String errorMessage);
    }

    private boolean validaDados() {

        // Validate CPF
        sCpf = edtCpfAgenteCad.getText().toString().trim();
        sCpf = sCpf.replaceAll("[^0-9]", "");

        sNomeCad = edtNomeCad.getText().toString();

        if (sCpf.isEmpty() ) {
            edtCpfAgenteCad.setError("Por favor, preencha este campo.");
            return false;
        } else if (sNomeCad.isEmpty()) {
            edtNomeCad.setError("Por favor, preencha este campo.");
            return false;
    } else if (!ValidaCpf.validaCPF(sCpf)) {
            edtCpfAgenteCad.setError("CPF inválido.");
            return false;
        }

        // Validate function selection
        else if (rbSelecione.isChecked()) {
            Alertas.showAlertDialog(AddAgentes.this, "Aviso", "Por favor, selecione uma função.");
            return false;

        } else if (rbOutro.isChecked()) {

            String outroTexto = edtOutroAgenteCad.getText().toString().trim();
            if (outroTexto.isEmpty()) {
                edtOutroAgenteCad.setError("Por favor, preencher o campo 'Outro'.");
                return false;
            }
        }  // Se passar em tudo, define as variáveis de função e retorna true
        if (rbAdmin.isChecked()) {
            sFuncao_agente = "Administrador";
            db_salvar_pre_cadastro = "pre_cadastro_admins";
        } else if (rbAgente.isChecked()) {
            sFuncao_agente = "Agente de Endemias";
            db_salvar_pre_cadastro = "pre_cadastro_agentes";
        } else if (rbOutro.isChecked()) {
            sFuncao_agente = edtOutroAgenteCad.getText().toString().trim();
            db_salvar_pre_cadastro = "pre_cadastro_agentes";
        }

        return true; // Retorno final de sucesso


    }

    private void salvar_dados_db() {
        // 2. Salvar Dados no Realtime Database
        DateTimeSaver dateSaver = new DateTimeSaver();
        data_atual = dateSaver.saveCurrentDateTime();

        DatabaseReference userRef = databaseMunicipio.getReference()
                .child("config")
                .child(db_salvar_pre_cadastro)
                .child(sCpf);

        Map<String, Object> dados = new HashMap<>();
        dados.put("nome_pre_cadastro", sNomeCad);
        dados.put("funcao_pre_cadastro", sFuncao_agente);
        dados.put("autoriza_uso_imagem", autoriza_uso_imagem);
        dados.put("data_pre_cadastro", data_atual);

        userRef.setValue(dados).addOnSuccessListener(aVoid -> {
            decrementarContador();
            edtCpfAgenteCad.setText("");
            edtNomeCad.setText("");
            edtOutroAgenteCad.setText("");
            radioGroup.check(R.id.radioButtonSelOpcao);
            //rbSelecione.setSelected(true);
            edtOutroAgenteCad.setVisibility(View.GONE);
            chkBoxMostrarImagemAgente.setChecked(false);
            chkBoxMostrarImagemAgente.setEnabled(true);

            // Zera as variaveis, pois nos testes mesmo o editText em branco
            //Mostrava a msg "este CPF já esta cadastrado.
            sCpf = "";
            sNomeCad = "";

        }).addOnFailureListener(e -> Toast.makeText(AddAgentes.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void decrementarContador() {

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + ".firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

        DatabaseReference configRef = databaseMunicipio.getReference()
                .child("config")
                .child(decrementarOnde);

        configRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                Long valor = currentData.getValue(Long.class);
                if (valor != null && valor > 0) {
                    currentData.setValue(valor - 1);
                }
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                Toast.makeText(AddAgentes.this, "Dados salvo com sucesso", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
