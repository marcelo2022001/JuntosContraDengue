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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.VisualizarDenunciasAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassReclamacoes;
import com.example.juntoscontradengue.extras.DateUtilsApp;

import java.util.ArrayList;
import java.util.List;

public class AdapterReclamacaoAgentes
        extends RecyclerView.Adapter<AdapterReclamacaoAgentes.ViewHolder> {

    private final List<ClassReclamacoes> listaOriginal = new ArrayList<>();
    private final List<ClassReclamacoes> listaFiltrada = new ArrayList<>();

    public AdapterReclamacaoAgentes(List<ClassReclamacoes> lista) {
        listaOriginal.addAll(lista);
        listaFiltrada.addAll(lista);
    }

    // ================= UPDATE =================

    public void updateList(List<ClassReclamacoes> novaLista) {
        listaOriginal.clear();
        listaOriginal.addAll(novaLista);

        atualizarListaFiltrada(novaLista);
    }

    // ================= FILTRO =================

    public void filtrarPorStatus(String status) {

        List<ClassReclamacoes> novaListaFiltrada = new ArrayList<>();

        if (status.equalsIgnoreCase("Todos")) {
            novaListaFiltrada.addAll(listaOriginal);
        } else {

            for (ClassReclamacoes r : listaOriginal) {

                if (r.getStatus() != null &&
                        r.getStatus().equalsIgnoreCase(status)) {

                    novaListaFiltrada.add(r);
                }
            }
        }

        atualizarListaFiltrada(novaListaFiltrada);
    }

    // ================= DIFFUTIL =================

    private void atualizarListaFiltrada(List<ClassReclamacoes> novaListaFiltrada) {

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
                ClassReclamacoes oldItem = listaFiltrada.get(oldItemPosition);
                ClassReclamacoes newItem = novaListaFiltrada.get(newItemPosition);
                return oldItem.getStatus() != null && oldItem.getStatus().equals(newItem.getStatus())
                        && oldItem.getRespondida_por() != null
                        ? oldItem.getRespondida_por().equals(newItem.getRespondida_por())
                        : newItem.getRespondida_por() == null;
            }
        });

        listaFiltrada.clear();
        listaFiltrada.addAll(novaListaFiltrada);
        diffResult.dispatchUpdatesTo(this);
    }

    // ================= ADAPTER =================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_reclamacoes_agentes, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        ClassReclamacoes r = listaFiltrada.get(position);

        holder.data.setText(DateUtilsApp.ConverteDataTimeStampLegivel(r.getData_envio()));
        holder.reclamacao.setText(r.getReclamacao());
        holder.reclamante.setText(r.getReclamante());
        holder.telefone.setText(r.getTelefone());
        holder.status.setText(r.getStatus());

        String respondido = r.getRespondida_por();
        if (!TextUtils.isEmpty(respondido)) {
            holder.viewRespondido_por.setVisibility(View.VISIBLE);
            holder.respondido_por.setText(respondido);
        } else {
            holder.viewRespondido_por.setVisibility(View.GONE);
        }

        String opcaoStatus = r.getStatus();

        holder.abrir.setOnClickListener(v -> {

            Context context = v.getContext();

            Intent intent =
                    new Intent(context, VisualizarDenunciasAgentes.class);

            intent.putExtra("UUID", r.getIdUsuario());
            intent.putExtra("ID", r.getIdReclamacao());
            intent.putExtra("status_reclamacao", opcaoStatus);

            context.startActivity(intent);
        });

        switch (r.getStatus()) {
            case "Aguardando resposta":
                // Amarelo claro para atenção
                holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#FFF9C4"));
                holder.status.setTextColor(Color.parseColor("#FBC02D"));
                break;

            case "Resolvido":
                // Verde claro para concluído
                holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
                holder.status.setTextColor(Color.parseColor("#388E3C"));
                break;

            case "Não Resolvido":
                // Vermelho claro para não concluído
                holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#F26D52"));
                holder.status.setTextColor(Color.parseColor("#F5F5F5"));
                break;

            case "Respondido":
                // Azul
                holder.cardReclamacoesAgentes.setCardBackgroundColor(Color.parseColor("#0D47A1"));
                holder.status.setTextColor(Color.parseColor("#F5F5F5"));
                break;

            default:
                // Cor padrão (Branco ou Cinza)
                //holder.cardReclamacoesUsuario.setCardBackgroundColor(Color.WHITE);
                holder.cardReclamacoesAgentes.setBackgroundColor(Color.parseColor("#F0ECEB"));
                holder.status.setTextColor(Color.BLACK); // Resetar a cor do texto também
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    // ================= HOLDER =================

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView abrir;
        TextView data, reclamacao, status, reclamante, telefone, viewRespondido_por, respondido_por;
        CardView cardReclamacoesAgentes;
        ViewHolder(@NonNull View itemView) {
            super(itemView);

            abrir = itemView.findViewById(R.id.img_verificar_reclamacao);
            data = itemView.findViewById(R.id.txt_dt_reclamacao);
            reclamacao = itemView.findViewById(R.id.txt_titulo_reclamacao);
            reclamante = itemView.findViewById(R.id.txt_reclamante);
            telefone = itemView.findViewById(R.id.txt_fone_reclamante);
            status = itemView.findViewById(R.id.txt_status_reclamacao);
            viewRespondido_por = itemView.findViewById(R.id.txtView_respondido_por);
            respondido_por = itemView.findViewById(R.id.txt_respondido_por);
            cardReclamacoesAgentes = itemView.findViewById(R.id.cardView_reclamacoes_agentes);

        }
    }
}