package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.juntoscontradengue.database.adapters.AdapterExcluirPreCadastros;
import com.example.juntoscontradengue.database.classes_database.ClassDeletarPreCadastro;
import com.example.juntoscontradengue.databinding.ActivityExcluirPreCadastroBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcluirPreCadastro extends AppCompatActivity {

    private final List<ClassDeletarPreCadastro> lista = new ArrayList<>();
        private AdapterExcluirPreCadastros adapter;
        String estado, municipio, localDB;
        private DatabaseReference reference;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            com.example.juntoscontradengue.databinding.ActivityExcluirPreCadastroBinding binding = ActivityExcluirPreCadastroBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            setSupportActionBar(binding.tbExcluirPreCadastro);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

            String local_database = getIntent().getStringExtra("local_db");
            assert local_database != null;
            if(local_database.equals("pre_cadastro_admins")){
                  localDB = "total_admins";
            } else {
                localDB = "total_agentes";
            }

            SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
             estado = prefs.getString("estado", null);
             municipio = prefs.getString("municipio", null);

            adapter = new AdapterExcluirPreCadastros(lista);

            binding.rvExcluirPreCadastro.setLayoutManager(new LinearLayoutManager(this));
            binding.rvExcluirPreCadastro.setAdapter(adapter);

            assert estado != null;
            assert municipio != null;
            reference = FirebaseDatabase.getInstance()
                    .getReference("cadastros")
                    .child(estado)
                    .child(municipio)
                    .child("config")
                    .child(local_database);

            adapter.setOnAgenteDeleteListener(this::confirmarExclusao);

            ouvirBanco();
        }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void ouvirBanco() {

            reference.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {

                    ClassDeletarPreCadastro cadastro = snapshot.getValue(ClassDeletarPreCadastro.class);

                    if (cadastro != null) {
                        cadastro.setCpf_pre_cadastro(snapshot.getKey());

                        lista.add(cadastro);
                        adapter.notifyItemInserted(lista.size() - 1);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    String cpf = snapshot.getKey();

                    for (int i = 0; i < lista.size(); i++) {
                        if (lista.get(i).getCpf_pre_cadastro().equals(cpf)) {

                            lista.remove(i);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {

                    String cpf = snapshot.getKey();

                    for (int i = 0; i < lista.size(); i++) {
                        if (lista.get(i).getCpf_pre_cadastro().equals(cpf)) {

                            ClassDeletarPreCadastro atualizado =
                                    snapshot.getValue(ClassDeletarPreCadastro.class);

                            if (atualizado != null) {
                                atualizado.setCpf_pre_cadastro(cpf);
                                lista.set(i, atualizado);
                                adapter.notifyItemChanged(i);
                            }
                            break;
                        }
                    }
                }

                @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String s) {}
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

        }

    private void confirmarExclusao(ClassDeletarPreCadastro cadastro, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar exclusão")
                .setMessage("Deseja excluir o pré-cadastro de:\n\n" + cadastro.getNome_pre_cadastro())
                .setIcon(R.drawable.alert_warning_triangulo)
                .setPositiveButton("SIM", (dialog, which) -> {

                    // 1. Remove o valor atual
                    reference.child(cadastro.getCpf_pre_cadastro())
                            .removeValue()
                            .addOnSuccessListener(unused -> {

                                // 2. Após excluir, incrementa o contador no caminho especificado
                                DatabaseReference configRef = FirebaseDatabase.getInstance().getReference()
                                        .child("cadastros")
                                        .child(estado)
                                        .child(municipio)
                                        .child("config")
                                        .child(localDB);

                                configRef.setValue(ServerValue.increment(1))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Pré-cadastro excluído e contador atualizado", Toast.LENGTH_SHORT).show();
                                            //startActivity(new Intent(ExcluirPreCadastro.this, ExcluirProfissionaisPreCadastro.class));

                                        });
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("NÃO", null)
                .show();
    }

    }
