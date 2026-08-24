package com.example.juntoscontradengue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class DenunciaIdentificacaoFragment extends Fragment {
    private Button btnIdentificar, btnAnonimo;
    private ImageView gifIdentificar;

    public DenunciaIdentificacaoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Usa o layout correto do fragmento
        View view = inflater.inflate(R.layout.fragment_denuncia_identificacao, container, false);

        btnIdentificar = view.findViewById(R.id.btnIdentificar);
        btnAnonimo = view.findViewById(R.id.btnAnonimo);
        gifIdentificar = view.findViewById(R.id.gifIdentificar);

        carregarGif();
        configurarCliques();

        return view;
    }

    private void carregarGif() {
        Glide.with(this)
                .asGif()
                .load(R.drawable.identificacao_gif)
                .placeholder(R.drawable.camera)
                .into(gifIdentificar);
    }

    private void configurarCliques() {
        btnIdentificar.setOnClickListener(v -> finalizarEscolha(true));

        btnAnonimo.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Modo anônimo ativado", Toast.LENGTH_SHORT).show();
            finalizarEscolha(false);
        });
    }

    private void finalizarEscolha(boolean identificar) {
        if (getActivity() instanceof Denunciar) {
            Denunciar activity = (Denunciar) getActivity();
            activity.usuarioEscolheuIdentificar(identificar);
            activity.mostrarFormulario();

            // Remove o fragmento da tela
            getParentFragmentManager().beginTransaction().remove(this).commit();
        }
    }

}