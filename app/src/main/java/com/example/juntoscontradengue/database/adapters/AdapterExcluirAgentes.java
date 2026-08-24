package com.example.juntoscontradengue.database.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassAgentes;

import java.util.List;

public class AdapterExcluirAgentes extends RecyclerView.Adapter<AdapterExcluirAgentes.AgenteViewHolder> {

    private final List<ClassAgentes> lAgentesExcluir;
    private OnAgenteDeleteListener deleteListener;

    // Interface para comunicar o clique de exclusão com a Activity
    public interface OnAgenteDeleteListener {
        void onDeleteClick(ClassAgentes agente, int position);
    }

    public void setOnAgenteDeleteListener(OnAgenteDeleteListener listener) {
        this.deleteListener = listener;
    }

    public AdapterExcluirAgentes(List<ClassAgentes> lAgentesExcluir) {
        this.lAgentesExcluir = lAgentesExcluir;
    }

    @NonNull
    @Override
    public AgenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_agentes_excluir, parent, false);
        return new AgenteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgenteViewHolder holder, int position) {
        ClassAgentes agente = lAgentesExcluir.get(position);

        // Configuração dos textos com verificação de nulidade
        holder.nomeAgenteExcluir.setText(agente.getNome() != null ? agente.getNome() : "Nome não disponível");
        holder.funcaoAgenteExcluir.setText(agente.getFuncao() != null ? agente.getFuncao() : "Função não disponível");

        // Carregamento de imagem otimizado com Glide
        Glide.with(holder.itemView.getContext())
                .load(agente.getUrlImagem())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .circleCrop() // Opcional: deixa a imagem redonda se desejar
                .into(holder.imgAgenteExcluir);

        // Listener do botão de excluir capturando o UUID [cite: 1, 2]
        holder.excluirAgente.setOnClickListener(v -> {

            int pos = holder.getBindingAdapterPosition();

            if (pos == RecyclerView.NO_POSITION) return;

            ClassAgentes agenteSelecionado  = lAgentesExcluir.get(position);

            if (deleteListener != null && agente.getUuid() != null) {
                Log.d("ADAPTER", "Solicitando exclusão do UUID: " + agente.getUuid());
                deleteListener.onDeleteClick(agenteSelecionado, pos);
            }
        });

    }

    @Override
    public int getItemCount() {
        return lAgentesExcluir != null ? lAgentesExcluir.size() : 0;
    }

    public static class AgenteViewHolder extends RecyclerView.ViewHolder {
        final TextView nomeAgenteExcluir, funcaoAgenteExcluir, excluirAgente;
        final ImageView imgAgenteExcluir;

        public AgenteViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeAgenteExcluir = itemView.findViewById(R.id.txt_nome_agente_excluir);
            funcaoAgenteExcluir = itemView.findViewById(R.id.txt_funcao_agente_excluir);
            imgAgenteExcluir = itemView.findViewById(R.id.img_agentes_excluir);
            excluirAgente = itemView.findViewById(R.id.txt_lixeira_exclui_agente);
        }
    }
}