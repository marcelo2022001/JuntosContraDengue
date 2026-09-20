package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.VisualizarDenunciasAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassReclamacoesAdminsAgentes;
import com.example.juntoscontradengue.extras.DateUtilsApp;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdapterReclamacaoAgentes
        extends RecyclerView.Adapter<AdapterReclamacaoAgentes.ViewHolder> {

    private final List<ClassReclamacoesAdminsAgentes> listaOriginal = new ArrayList<>();
    private final List<ClassReclamacoesAdminsAgentes> listaFiltrada = new ArrayList<>();

    public AdapterReclamacaoAgentes(List<ClassReclamacoesAdminsAgentes> lista) {
        listaOriginal.addAll(lista);
        listaFiltrada.addAll(lista);
    }

    public void updateList(List<ClassReclamacoesAdminsAgentes> novaLista) {
        listaOriginal.clear();
        listaOriginal.addAll(novaLista);
        atualizarListaFiltrada(novaLista);
    }

    public void filtrarPorStatus(String status) {
        List<ClassReclamacoesAdminsAgentes> novaListaFiltrada = new ArrayList<>();

        if (status.equalsIgnoreCase("Todos")) {
            novaListaFiltrada.addAll(listaOriginal);
        } else {
            for (ClassReclamacoesAdminsAgentes r : listaOriginal) {
                if (r.getStatus() != null && r.getStatus().equalsIgnoreCase(status)) {
                    novaListaFiltrada.add(r);
                }
            }
        }

        atualizarListaFiltrada(novaListaFiltrada);
    }

    private void atualizarListaFiltrada(List<ClassReclamacoesAdminsAgentes> novaListaFiltrada) {

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return listaFiltrada.size();
            }

            @Override
            public int getNewListSize() {
                return novaListaFiltrada.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return listaFiltrada.get(oldItemPosition).getIdReclamacao()
                        .equals(novaListaFiltrada.get(newItemPosition).getIdReclamacao());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ClassReclamacoesAdminsAgentes oldItem = listaFiltrada.get(oldItemPosition);
                ClassReclamacoesAdminsAgentes newItem = novaListaFiltrada.get(newItemPosition);

                boolean statusIguais = oldItem.getStatus() != null
                        && oldItem.getStatus().equals(newItem.getStatus());

                boolean respondidoIguais = (oldItem.getRespondida_por() == null && newItem.getRespondida_por() == null)
                        || (oldItem.getRespondida_por() != null
                        && oldItem.getRespondida_por().equals(newItem.getRespondida_por()));

                return statusIguais && respondidoIguais;
            }
        });

        listaFiltrada.clear();
        listaFiltrada.addAll(novaListaFiltrada);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_reclamacoes_agentes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassReclamacoesAdminsAgentes r = listaFiltrada.get(position);

        holder.data.setText(DateUtilsApp.ConverteDataTimeStampLegivel(r.getData_envio()));
        holder.reclamacao.setText(r.getReclamacao());
        holder.reclamante.setText(r.getReclamante());
        holder.telefone.setText(r.getTelefone());
        holder.status.setText(r.getStatus());

        String respondido = r.getRespondida_por();
        if (!TextUtils.isEmpty(respondido)) {
            holder.viewRespondido_por.setVisibility(View.VISIBLE);
            holder.respondido_por.setVisibility(View.VISIBLE);
            holder.respondido_por.setText(respondido);
        } else {
            holder.viewRespondido_por.setVisibility(View.GONE);
            holder.respondido_por.setVisibility(View.GONE);
        }

        aplicarCorPorStatus(holder, r.getStatus());

        String opcaoStatus = r.getStatus();
        holder.abrir.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, VisualizarDenunciasAgentes.class);
            intent.putExtra("UUID", r.getIdUsuario());
            intent.putExtra("ID", r.getIdReclamacao());
            intent.putExtra("status_reclamacao", opcaoStatus);
            context.startActivity(intent);
        });
    }

    private void aplicarCorPorStatus(ViewHolder holder, String statusBruto) {
        String status = statusBruto == null ? "" : statusBruto.trim();

        if (status.equalsIgnoreCase("Aguardando resposta")) {
            holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#FFF9C4"));
            holder.status.setTextColor(Color.parseColor("#FBC02D"));

        } else if (status.equalsIgnoreCase("Resolvido")) {
            holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
            holder.status.setTextColor(Color.parseColor("#388E3C"));

        } else if (status.equalsIgnoreCase("Não Resolvido")) {
            holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#F26D52"));
            holder.status.setTextColor(Color.parseColor("#F5F5F5"));

        } else if (status.equalsIgnoreCase("Respondido")) {
            holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#0D47A1"));
            holder.status.setTextColor(Color.parseColor("#F5F5F5"));

        } else {
            holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#F0ECEB"));
            holder.status.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView abrir;
        TextView data, reclamacao, reclamante, telefone, status, viewRespondido_por, respondido_por;
        MaterialCardView cardReclamacoesAgentes;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            abrir = itemView.findViewById(R.id.img_verificar_reclamacao);
            data = itemView.findViewById(R.id.txt_dt_reclamacao_agentes);
            reclamacao = itemView.findViewById(R.id.txt_titulo_reclamacao_agentes);
            reclamante = itemView.findViewById(R.id.txt_reclamante_agentes);
            telefone = itemView.findViewById(R.id.txt_fone_reclamante_agentes);
            status = itemView.findViewById(R.id.txt_status_reclamacao_agentes);
            viewRespondido_por = itemView.findViewById(R.id.txtView_respondido_por_agentes);
            respondido_por = itemView.findViewById(R.id.txt_respondido_por_agentes);
            cardReclamacoesAgentes = itemView.findViewById(R.id.cardView_reclamacoes_agentes);
        }
    }
}