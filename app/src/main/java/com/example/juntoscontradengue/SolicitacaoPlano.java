package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.juntoscontradengue.database.classes_database.ClassPrecos;
import com.example.juntoscontradengue.databinding.ActivitySolicitacaoPlanoBinding;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class SolicitacaoPlano extends AppCompatActivity {

    private ActivitySolicitacaoPlanoBinding solicitacaoPlanoBinding;
    private Double vAdmIndividual, vAgenteIndividual;
    private Double vAdmimBasico, vAgenteBasico;
    private Double vAdmimMunicipal, vAgenteMunicipal;
    private String eMail;
    private String gerarMensagem() {

        String plano = "";

        if (solicitacaoPlanoBinding.rbIndividual.isChecked()) {
            plano = "Individual";
        } else if (solicitacaoPlanoBinding.rbBasico.isChecked()) {
            plano = "Básico";
        } else if (solicitacaoPlanoBinding.rbMunicipal.isChecked()) {
            plano = "Municipal";
        }

        String adminsExtras;
        String agentesExtras;


            adminsExtras =
                    solicitacaoPlanoBinding.edtAdminsExtras.getText().toString();

            agentesExtras =
                    solicitacaoPlanoBinding.edtAgentesExtras.getText().toString();



        return
                "SOLICITAÇÃO DE CONTRATAÇÃO\n\n" +

                        "Município: " +
                        solicitacaoPlanoBinding.edtMunicipio.getText() + "\n" +

                        "Estado: " +
                        solicitacaoPlanoBinding.edtEstado.getText() + "\n\n" +

                        "Responsável: " +
                        solicitacaoPlanoBinding.edtResponsavel.getText() + "\n" +

                        "Cargo: " +
                        solicitacaoPlanoBinding.edtCargo.getText() + "\n" +

                        "Telefone: " +
                        solicitacaoPlanoBinding.edtTelefone.getText() + "\n" +

                        "E-mail: " +
                        solicitacaoPlanoBinding.edtEmail.getText() + "\n\n" +

                        "Plano: " + plano + "\n" +
                      //  "Período: " + periodo + "\n" +

                        "Admins Extras: " + adminsExtras + "\n" +
                        "Agentes Extras: " + agentesExtras + "\n\n" +

                        "Valor Estimado: " +
                        solicitacaoPlanoBinding.txtValorEstimado.getText() + "\n\n" +

                        "Observações:\n" +
                        solicitacaoPlanoBinding.edtObservacoes.getText();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isConnected = NetworkUtils.isNetworkAvailable(SolicitacaoPlano.this);

        if (!isConnected) {

            Intent itente = new Intent(this, SemInternetActivity.class);
            itente.putExtra("id_activity", "solicitar_plano");
            startActivity(itente);

        }


        solicitacaoPlanoBinding = ActivitySolicitacaoPlanoBinding.inflate(getLayoutInflater());
        setContentView(solicitacaoPlanoBinding.getRoot());

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        String estado = prefs.getString("estado", null);
        String municipio = prefs.getString("municipio", null);

        SharedPreferences prefsUser = getSharedPreferences("UserData", MODE_PRIVATE);
        String nome_usuario = prefsUser.getString("nome", null);

        busca_email_app();
        buscaValoresPlanos();

        EditText edtMunicipio = solicitacaoPlanoBinding.edtMunicipio;
        EditText edtEstado = solicitacaoPlanoBinding.edtEstado;
        EditText edtResponsavel = solicitacaoPlanoBinding.edtResponsavel;
        EditText edtAdminExtras = solicitacaoPlanoBinding.edtAdminsExtras;
        EditText edtAgenteExtras = solicitacaoPlanoBinding.edtAgentesExtras;

        if (municipio != null) {
            edtMunicipio.setText(municipio.toUpperCase());
            edtMunicipio.setEnabled(false);
        }

        if (estado != null) {
            edtEstado.setText(estado.toUpperCase());
            edtEstado.setEnabled(false);
        }

        if(nome_usuario != null){
            edtResponsavel.setText(nome_usuario.toUpperCase());
            edtResponsavel.setEnabled(false);
        }

        solicitacaoPlanoBinding.edtAdminsExtras.addTextChangedListener(textWatcher);
        solicitacaoPlanoBinding.edtAgentesExtras.addTextChangedListener(textWatcher);

        Button enviarSolicitacao = solicitacaoPlanoBinding.btnEnviarSolicitacao;
        enviarSolicitacao.setOnClickListener(v -> enviar_solicitacao());

        Button sairSolicitacao = solicitacaoPlanoBinding.btnSairEnviarSolicitacao;
        sairSolicitacao.setOnClickListener(v -> finish());

        RadioGroup radioPlano = solicitacaoPlanoBinding.radioPlano;

       radioPlano.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.rbIndividual) {

                edtAdminExtras.setText("");
                edtAgenteExtras.setText("");

                calcularValor();


            } else if (checkedId == R.id.rbBasico){

                edtAdminExtras.setText("");
                edtAgenteExtras.setText("");

                calcularValor();
                //solicitacaoPlanoBinding.layoutExtras.setVisibility(View.VISIBLE);

            } else if (checkedId == R.id.rbMunicipal){

                edtAdminExtras.setText("");
                edtAgenteExtras.setText("");

                calcularValor();

            }

            calcularValor();
        });

        solicitacaoPlanoBinding.rbIndividual.setChecked(true);

    }

    private void busca_email_app() {
        // 1. Aponta diretamente para o nó "email" dentro de "config_app"
        DatabaseReference busca_email = FirebaseDatabase.getInstance()
                .getReference("config_app")
                .child("email");

        busca_email.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 2. Verifica se o nó existe e não está vazio
                if (snapshot.exists() && snapshot.getValue() != null) {
                    // 3. Converte o valor recuperado explicitamente para String
                    eMail = snapshot.getValue(String.class);

                    // Use a variável eMail aqui ou atualize sua UI
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 4. Trata possíveis erros de permissão ou conexão
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }



    private void buscaValoresPlanos() {

        // Individual
        DatabaseReference dbPlanoIndividual = FirebaseDatabase.getInstance()
                .getReference("config_precos")
                .child("plano_individual");
        dbPlanoIndividual.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // 3. Converte os dados do Firebase diretamente para a sua classe
                ClassPrecos precosIndividual = snapshot.getValue(ClassPrecos.class);

                if (precosIndividual != null) {
                    // Recupera os valores de forma segura (como Double ou Double-convertido)
                    vAdmIndividual = precosIndividual.getValorAdmIndividual();
                    vAgenteIndividual = precosIndividual.getValorAgenteIndividual();

                    // CHAMA O CÁLCULO: Atualiza a tela assim que o dado chegar da internet
                    calcularValor();

                    // Evita erros de NullPointerException se o nó estiver incompleto no banco
                    if (vAdmIndividual != null && vAgenteIndividual != null) {
                        // Aqui você já tem os valores em tempo real!
                        Log.d("FirebasePrecos", "Admim: " + vAdmIndividual + " | Agente: " + vAgenteIndividual);

                        // Exemplo de como enviar para sua interface ou callback de sucesso:
                        // callback.onSucesso(valorAnual, valorMensal);
                    }
                } else {
                    Log.d("FirebasePrecos", "Nenhum preço encontrado no banco.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebasePrecos", "Erro ao ler banco: " + error.getMessage());
            }
        });

        // Básico
        DatabaseReference dbPlanoBasico = FirebaseDatabase.getInstance()
                .getReference("config_precos")
                .child("plano_basico");
        dbPlanoBasico.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 3. Converte os dados do Firebase diretamente para a sua classe
                ClassPrecos precosBasico = snapshot.getValue(ClassPrecos.class);
                if (precosBasico != null) {
                    // Recupera os valores de forma segura (como Double ou Double-convertido)
                    vAdmimBasico = precosBasico.getValorAdmBasico();
                    vAgenteBasico = precosBasico.getValorAgenteBasico();

                    Log.d("FirebasePrecos", "Basico OK -> Adm: " + vAdmimBasico + " | Agente: " + vAgenteBasico);

                    // CHAMA O CÁLCULO: Atualiza a tela assim que o dado chegar da internet
                    calcularValor();

                    // Evita erros de NullPointerException se o nó estiver incompleto no banco
                    if (vAdmimBasico != null && vAgenteBasico != null) {
                        // Aqui você já tem os valores em tempo real!
                        Log.d("FirebasePrecos", "AdmBasico: " + vAdmimBasico + " | AgenteBasico: " + vAgenteBasico);

                        // Exemplo de como enviar para sua interface ou callback de sucesso:
                        // callback.onSucesso(valorAnual, valorMensal);
                    }
                } else {
                    Log.d("FirebasePrecos", "Nenhum preço encontrado no banco.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebasePrecos", "Erro ao ler banco: " + error.getMessage());
            }
        });

        // Municipal
        DatabaseReference dbPlanoMunicipal = FirebaseDatabase.getInstance()
                .getReference("config_precos")
                .child("plano_municipal");
        dbPlanoMunicipal.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 3. Converte os dados do Firebase diretamente para a sua classe
                    ClassPrecos precosMunicipal = snapshot.getValue(ClassPrecos.class);
                    // Recupera os valores de forma segura (como Double ou Double-convertido)
                    assert precosMunicipal != null;
                    vAdmimMunicipal = precosMunicipal.getValorAdmMunicipal();
                    vAgenteMunicipal = precosMunicipal.getValorAgenteMunicipal();

                    Log.d("FirebasePrecos", "Municipal OK -> Adm: " + vAdmimMunicipal + " | Agente: " + vAgenteMunicipal);

                    // CHAMA O CÁLCULO: Atualiza a tela assim que o dado chegar da internet
                    calcularValor();

                    // Evita erros de NullPointerException se o nó estiver incompleto no banco
                    if (vAdmimMunicipal != null && vAgenteMunicipal != null) {
                        // Aqui você já tem os valores em tempo real!
                        Log.d("FirebasePrecos", "AdmMunicipal: " + vAdmimMunicipal + " | AgenteMunicipal: " + vAgenteMunicipal);

                        // Exemplo de como enviar para sua interface ou callback de sucesso:
                        // callback.onSucesso(valorAnual, valorMensal);
                    }
                } else {
                    Log.d("FirebasePrecos", "Nenhum preço encontrado no banco.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebasePrecos", "Erro ao ler banco: " + error.getMessage());
            }
        });

    }

    private void enviar_solicitacao() {

        if(solicitacaoPlanoBinding.edtMunicipio.getText().toString().isEmpty() ){

            solicitacaoPlanoBinding.edtMunicipio.setError("Este campo é obrigatório!");
            solicitacaoPlanoBinding.edtMunicipio.requestFocus();

        } else if (solicitacaoPlanoBinding.edtEstado.getText().toString().isEmpty()){

            solicitacaoPlanoBinding.edtEstado.setError("Este campo é obrigatório!");
            solicitacaoPlanoBinding.edtEstado.requestFocus();

        } else if (solicitacaoPlanoBinding.edtResponsavel.getText().toString().isEmpty() ){

            solicitacaoPlanoBinding.edtResponsavel.setError("Este campo é obrigatório!");
            solicitacaoPlanoBinding.edtResponsavel.requestFocus();

        } else if(solicitacaoPlanoBinding.edtCargo.getText().toString().isEmpty() ){

            solicitacaoPlanoBinding.edtCargo.setError("Este campo é obrigatório!");
            solicitacaoPlanoBinding.edtCargo.requestFocus();

        } else if(solicitacaoPlanoBinding.edtTelefone.getText().toString().isEmpty() ){

            solicitacaoPlanoBinding.edtTelefone.setError("Este campo é obrigatório!");
            solicitacaoPlanoBinding.edtTelefone.requestFocus();

        } else /*(solicitacaoPlanoBinding.edtEmail.getText().toString().isEmpty() )*/{

            solicitacaoPlanoBinding.edtEmail.setError("Este campo é obrigatório!");
            solicitacaoPlanoBinding.edtEmail.requestFocus();        }

        String mensagem = gerarMensagem();

        Intent email = new Intent(Intent.ACTION_SEND);

        email.setType("message/rfc822");

        email.putExtra(
                Intent.EXTRA_EMAIL,
                new String[]{eMail}
        );

        email.putExtra(
                Intent.EXTRA_SUBJECT,
                "Solicitação de Contratação"
        );

        email.putExtra(
                Intent.EXTRA_TEXT,
                mensagem
        );

        startActivity(
                Intent.createChooser(
                        email,
                        "Enviar Solicitação"
                )
        );
    }

    private void calcularValor() {

        if (solicitacaoPlanoBinding.rbIndividual.isChecked() && (vAdmIndividual == null || vAgenteIndividual == null)) {
            solicitacaoPlanoBinding.txtValorEstimado.setText(R.string.carregando_precos);

            //Muda de Administradores e Agentes Extras para Administradores e agentes
            solicitacaoPlanoBinding.txtAdmExtras.setText(R.string.administradores);
            solicitacaoPlanoBinding.txtAgentesExtras.setText(R.string.agente_de_endemias);
            return;
        }

        // 1. Proteção: Se os valores do plano selecionado ainda não chegaram da internet, interrompe o cálculo para não quebrar o app
        if (solicitacaoPlanoBinding.rbBasico.isChecked() && (vAdmimBasico == null || vAgenteBasico == null)) {
            solicitacaoPlanoBinding.txtValorEstimado.setText(R.string.carregando_precos);

            //Muda de Administradores e Agentes Extras para Administradores e agentes
            solicitacaoPlanoBinding.txtAdmExtras.setText(R.string.administradores_extras);
            solicitacaoPlanoBinding.txtAgentesExtras.setText(R.string.agentes_extras);
            return;
        }

        if (solicitacaoPlanoBinding.rbMunicipal.isChecked() && (vAdmimMunicipal == null || vAgenteMunicipal == null)) {
            solicitacaoPlanoBinding.txtValorEstimado.setText(R.string.carregando_precos);

            solicitacaoPlanoBinding.txtAdmExtras.setText(R.string.administradores_extras);
            solicitacaoPlanoBinding.txtAgentesExtras.setText(R.string.agentes_extras);
            return;
        }

        double valor = 0;

        int admins = 0;
        int agentes = 0;

        try {
            admins = Integer.parseInt(
                    solicitacaoPlanoBinding.edtAdminsExtras.getText().toString());
        } catch (Exception ignored) {
        }

        try {
            agentes = Integer.parseInt(
                    solicitacaoPlanoBinding.edtAgentesExtras.getText().toString());
        } catch (Exception ignored) {
        }

      //  boolean anual = solicitacaoPlanoBinding.rbAnual.isChecked();

        String resumo = "";

        if (solicitacaoPlanoBinding.rbIndividual.isChecked()) {

            valor = (admins * vAdmIndividual)
                    + (agentes * vAgenteIndividual);

            resumo =
                    "Plano Individual\n" +
                            admins + " Administradores\n" +
                            agentes + " Agentes";

        }

        else if (solicitacaoPlanoBinding.rbBasico.isChecked()) {

            //valor = anual ? vMensalBasico : vAnualBasico;

                valor = (2*vAdmimBasico + 6*vAgenteBasico);
                valor += admins * (vAdmIndividual);  // Pega valor individual mais caro para os extras
                valor += agentes * (vAgenteIndividual);



            resumo =
                    "Plano Básico\n" +
                            "2 Administradores\n" +
                            "6 Agentes\n" +
                            "+" + admins + " Admin Extras\n" +
                            "+" + agentes + " Agentes Extras";

        }

        else if (solicitacaoPlanoBinding.rbMunicipal.isChecked()) {

                valor = (3*vAdmimMunicipal) + (10*vAgenteMunicipal);
                valor += admins * (vAdmimMunicipal);
                valor += agentes * (vAgenteMunicipal);

            resumo =
                    "Plano Municipal\n" +
                            "3 Administradores\n" +
                            "10 Agentes\n" +
                            "+" + admins + " Admin Extras\n" +
                            "+" + agentes + " Agentes Extras";
        }

        solicitacaoPlanoBinding.txtValorEstimado.setText(
                String.format(
                        Locale.forLanguageTag("pt-BR"),
                        "R$ %.2f",
                        valor
                )
        );

        solicitacaoPlanoBinding.txtResumoPlano.setText(resumo);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            calcularValor();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}