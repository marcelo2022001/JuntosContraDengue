package com.example.juntoscontradengue.extras;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BuscaNomeEmailCadastrado {
    private final DatabaseReference mDatabase;
    private OnUserDataFetchListener mListener;

    public interface OnUserDataFetchListener {
        void onUserDataFetched(String nome, String email);

        void onUserNotFound();

        void onError(String errorMessage);
    }

    public BuscaNomeEmailCadastrado() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void fetchUserDataByCpf(String cpf, String estado, String cidade, String tipoConta, OnUserDataFetchListener listener) {
        this.mListener = listener;

        DatabaseReference userRef = mDatabase.child("cadastros")
                .child(estado)
                .child(cidade)
                .child(tipoConta)
                .child(cpf);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        String nome = snapshot.child("nome").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        if (nome != null && email != null) {
                            mListener.onUserDataFetched(nome, email);
                        } else {
                            mListener.onError("Dados incompletos para o CPF: " + cpf);
                        }
                    } catch (Exception e) {
                        mListener.onError("Erro ao processar os dados: " + e.getMessage());
                    }
                } else {
                    mListener.onUserNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mListener.onError("Erro de acesso: " + databaseError.getMessage());
            }
        });
    }
}

