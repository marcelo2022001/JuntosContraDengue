package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.juntoscontradengue.databinding.ActivityAddLocaisDescartesPneusEletronicosBinding;
import com.example.juntoscontradengue.extras.Alertas;
import com.example.juntoscontradengue.extras.MaskEditUtil;
import com.example.juntoscontradengue.extras.NetworkUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddLocalDescartesPneusEletronicos extends AppCompatActivity {

    EditText edtTxtDescartesLocais;
    EditText edtTxtDescartesEndereco;
    EditText edtTxtDescartesNumero;
    EditText edtTxtDescartesTelefone;
    EditText edtTxtDescartesHorarios;
    RadioButton rbDescarteEletronico;
    Button btnCancelDescartes;
    ActivityAddLocaisDescartesPneusEletronicosBinding bindingDescartes;
    String estado, municipio;
    private FirebaseDatabase databaseMunicipio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
       // setContentView(R.layout.activity_add_locais_descartes_pneus_eletronicos);

        bindingDescartes = ActivityAddLocaisDescartesPneusEletronicosBinding.inflate(getLayoutInflater());
        setContentView(bindingDescartes.getRoot());

        temInternet();

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
         estado = prefs.getString("estado", null);
         municipio = prefs.getString("municipio", null);

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

         edtTxtDescartesLocais = bindingDescartes.edtTxtDescartesLocais;
         edtTxtDescartesEndereco = bindingDescartes.edtTxtDescartesEndereco;
         edtTxtDescartesNumero = bindingDescartes.edtTxtDescartesNumero;
         edtTxtDescartesTelefone = bindingDescartes.edtTxtDescartesTelefone;
         edtTxtDescartesTelefone.addTextChangedListener(MaskEditUtil.maskTelefone());
         edtTxtDescartesHorarios = bindingDescartes.edtTxtDescartesHorarios;

         rbDescarteEletronico = bindingDescartes.radioButtonAddLocalEletronicos;
         btnCancelDescartes = bindingDescartes.btnCancelDescartes;

        btnCancelDescartes.setOnClickListener(v -> startActivity(new Intent(AddLocalDescartesPneusEletronicos.this, AdminActivity.class)));

        Button btnOkDescartes = bindingDescartes.btnOkDescartes;
        btnOkDescartes.setOnClickListener(v -> {

            if (rbDescarteEletronico.isChecked()) {
                cadastraEletronicos();
            } else {
                cadastraPneus();
            }
        });

    }

    private void temInternet() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(AddLocalDescartesPneusEletronicos.this);
        if (!isConnected) {
            Intent intent = new Intent(this, SemInternetActivity.class);
            intent.putExtra("Id_Activity", "add_locais_descartes");
            startActivity(intent);
            finish();
        }
    }

    private void cadastraPneus() {

        DatabaseReference ref_descartes = databaseMunicipio.getReference()
                .child("descarte_pneus");

        String local = edtTxtDescartesLocais.getText().toString();
        String endereco = edtTxtDescartesEndereco.getText().toString();
        String numero = edtTxtDescartesNumero.getText().toString().trim();
        String fone = edtTxtDescartesTelefone.getText().toString();
        String horario = edtTxtDescartesHorarios.getText().toString();


        if( (local.isEmpty()) || (endereco.isEmpty()) || (numero.isEmpty()) ||
                (fone.isEmpty()) || (horario.isEmpty()) ){
         Toast.makeText(AddLocalDescartesPneusEletronicos.this,
                 "Preencha todos os campos", Toast.LENGTH_LONG).show();
         return;
        }

        //pegar info. salvar no real time database
        Map<String, Object> cad_descartes = new HashMap<>();
        cad_descartes.put("local", local);
        cad_descartes.put("endereco", endereco);
        cad_descartes.put("numero", numero);
        cad_descartes.put("fone", fone);
        cad_descartes.put("horario", horario);

        ref_descartes.push().setValue(cad_descartes);

        edtTxtDescartesLocais.setText("");
        edtTxtDescartesEndereco.setText("");
        edtTxtDescartesNumero.setText("");
        edtTxtDescartesTelefone.setText("");
        edtTxtDescartesHorarios.setText("");

        Alertas.showAlertDialog(AddLocalDescartesPneusEletronicos.this,
                "Aviso", "Dados salvos com sucesso!");

    }

    private void cadastraEletronicos() {

        DatabaseReference ref_descartes = databaseMunicipio.getReference()
                .child("descarte_eletronicos");

        String local = edtTxtDescartesLocais.getText().toString();
        String endereco = edtTxtDescartesEndereco.getText().toString();
        String numero = edtTxtDescartesNumero.getText().toString().trim();
        String fone = edtTxtDescartesTelefone.getText().toString();
        String horario = edtTxtDescartesHorarios.getText().toString();

        if( (local.isEmpty()) || (endereco.isEmpty()) || (numero.isEmpty()) ||
                (fone.isEmpty()) || (horario.isEmpty()) ){
            Toast.makeText(AddLocalDescartesPneusEletronicos.this,
                    "Preencha todos os campos", Toast.LENGTH_LONG).show();
            return;
        }

        //pegar info. salvar no real time database
        Map<String, Object> cad_descartes = new HashMap<>();
        cad_descartes.put("local", local);
        cad_descartes.put("endereco", endereco);
        cad_descartes.put("numero", numero);
        cad_descartes.put("fone", fone);
        cad_descartes.put("horario", horario);

        ref_descartes.push().setValue(cad_descartes);

        edtTxtDescartesLocais.setText("");
        edtTxtDescartesEndereco.setText("");
        edtTxtDescartesNumero.setText("");
        edtTxtDescartesTelefone.setText("");
        edtTxtDescartesHorarios.setText("");

        Alertas.showAlertDialog(AddLocalDescartesPneusEletronicos.this,
                "Aviso", "Dados salvos com sucesso!");

    }

}