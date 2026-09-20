package com.example.juntoscontradengue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecuperarContaFragment extends Fragment {

    private static final String ARG_NODO_INDICE = "nodo_indice";

    private EditText editCpf;
    private FirebaseDatabase databaseMunicipio;
    private String nodoIndice;   // "index_email_admin" ou "index_email_agente"

    public static RecuperarContaFragment newInstance(String nodoIndice) {
        RecuperarContaFragment fragment = new RecuperarContaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NODO_INDICE, nodoIndice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodoIndice = getArguments() != null ? getArguments().getString(ARG_NODO_INDICE) : null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recuperar_conta, container, false);

        editCpf = view.findViewById(R.id.editCpf);
        Button btnBuscar = view.findViewById(R.id.btnBuscar);

        Button btnFecharFragment = view.findViewById(R.id.btnFecharFragment);
        btnFecharFragment.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        inicializarBancoDeDados();

        btnBuscar.setOnClickListener(v -> {
            String cpfDigitado = editCpf.getText().toString().trim();
            if (!cpfDigitado.isEmpty()) {
                buscarEmailNoDatabase(cpfDigitado);
            } else {
                editCpf.setError("Digite o CPF");
            }
        });

        return view;
    }

    private void inicializarBancoDeDados() {
        SharedPreferences prefs = requireContext().getSharedPreferences("configApp", Context.MODE_PRIVATE);
        String estado = prefs.getString("estado", "");
        String municipio = prefs.getString("municipio", "");

        String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
        databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);
    }

    private void buscarEmailNoDatabase(String cpfDigitado) {
        String cpfLimpo = cpfDigitado.replaceAll("[^0-9]", "");

        if (databaseMunicipio == null || nodoIndice == null) {
            Log.e("RecuperarConta", "Banco ou nó do índice não definido");
            return;
        }

        // index_email_admin ou index_email_agente / {cpf sem máscara}
        databaseMunicipio.getReference(nodoIndice).child(cpfLimpo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;

                        if (!snapshot.exists()) {
                            Toast.makeText(requireContext(), "CPF não cadastrado.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String emailAtual = snapshot.child("email").getValue(String.class);
                        String emailNovo = snapshot.child("novo_email").getValue(String.class);

                        if (emailNovo == null || emailNovo.isEmpty()) {
                            exibirDialogRecuperacao(emailAtual);
                        } else {
                            exibirDoisEmailsRecuperado(emailAtual, emailNovo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseDB", error.getMessage());
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Erro ao buscar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void exibirDoisEmailsRecuperado(String emailOriginal, String emailNovo) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Conta Encontrada!")
                .setMessage("Identificamos os e-mails:\n\nAntigo: " + mascararEmail(emailOriginal)
                        + "\nNovo pendente: " + mascararEmail(emailNovo))
                .setPositiveButton("OK", (dialog, which) -> getParentFragmentManager().popBackStack())
                .show();
    }

    private void exibirDialogRecuperacao(String emailOriginal) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Conta Encontrada!")
                .setMessage("Identificamos o e-mail:\n\n" + mascararEmail(emailOriginal))
                .setPositiveButton("OK", (dialog, which) -> getParentFragmentManager().popBackStack())
                .show();
    }

    private String mascararEmail(String email) {
        if (email == null || email.isEmpty()) return "";

        int arroba = email.indexOf("@");

        String usuario = email.substring(0, arroba);
        String dominio = email.substring(arroba); // inclui @

        int len = usuario.length();

        if (len < 1){
            return "Email inválido no banco de dados";
        }
        if (len == 1) {
            return "*****" + dominio;
        }
        if (len == 2) {
            return usuario.charAt(0) + "*****" + dominio;
        }
        if (len == 3) {
            return usuario.charAt(0) + "*****" + usuario.charAt(2) + dominio;
        }
        if (len == 4) {
            return usuario.charAt(0) + "****" + usuario.charAt(3) + dominio;
        }
        if (len == 5) {
            return usuario.charAt(0) + "*****" + usuario.charAt(4) + dominio;
        }

        // len >= 6: 2 primeiras + (len-4) asteriscos + 2 últimas
        String inicio = usuario.substring(0, 2);
        String fim = usuario.substring(len - 2);
        String mascara = "*".repeat(len - 4);
        return inicio + mascara + fim + dominio;
    }
}