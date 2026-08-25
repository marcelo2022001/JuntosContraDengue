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
import com.example.juntoscontradengue.database.classes_database.ClassReclamacoes;
import com.example.juntoscontradengue.extras.DateUtilsApp;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterReclamacaoAdmins
        extends RecyclerView.Adapter<AdapterReclamacaoAdmins.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private final Set<String> selectedIds = new HashSet<>();
    private final OnSelectionChangedListener selectionChangedListener;

    private final List<ClassReclamacoes> listaOriginal = new ArrayList<>();
    private final List<ClassReclamacoes> listaFiltrada = new ArrayList<>();

    public AdapterReclamacaoAdmins(List<ClassReclamacoes> lista,
                                    OnSelectionChangedListener selectionChangedListener) {
        listaOriginal.addAll(lista);
        listaFiltrada.addAll(lista);
        this.selectionChangedListener = selectionChangedListener;
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

    // ================= SELEÇÃO =================

    private void toggleSelection(String idReclamacao, int position) {
        if (idReclamacao == null) return;

        if (selectedIds.contains(idReclamacao)) {
            selectedIds.remove(idReclamacao);
        } else {
            selectedIds.add(idReclamacao);
        }

        notifyItemChanged(position);

        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedIds.size());
        }
    }

    /** Chame depois de aplicar a ação (ex.: ocultar do agente) nos itens selecionados. */
    public void clearSelection() {
        if (selectedIds.isEmpty()) return;

        for (int i = 0; i < listaFiltrada.size(); i++) {
            ClassReclamacoes item = listaFiltrada.get(i);
            if (item.getIdReclamacao() != null && selectedIds.contains(item.getIdReclamacao())) {
                notifyItemChanged(i);
            }
        }

        selectedIds.clear();

        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(0);
        }
    }

    /** Retorna os itens selecionados (para depois gravar visivel_agente: false no Firebase). */
    public List<ClassReclamacoes> getSelectedItems() {
        List<ClassReclamacoes> selecionados = new ArrayList<>();
        for (ClassReclamacoes item : listaFiltrada) {
            if (item.getIdReclamacao() != null && selectedIds.contains(item.getIdReclamacao())) {
                selecionados.add(item);
            }
        }
        return selecionados;
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

                boolean statusIguais = oldItem.getStatus() != null
                        && oldItem.getStatus().equals(newItem.getStatus());

                boolean respondidoIguais = (oldItem.getRespondida_por() == null && newItem.getRespondida_por() == null)
                        || (oldItem.getRespondida_por() != null
                        && oldItem.getRespondida_por().equals(newItem.getRespondida_por()));

                return statusIguais && respondidoIguais;
            }
        });

        // Remove da seleção qualquer item que tenha saído da lista filtrada
        // (ex.: já foi ocultado e o snapshot do Firebase atualizou a tela)
        Set<String> idsAtuais = new HashSet<>();
        for (ClassReclamacoes item : novaListaFiltrada) {
            if (item.getIdReclamacao() != null) idsAtuais.add(item.getIdReclamacao());
        }
        boolean selecaoMudou = selectedIds.retainAll(idsAtuais);

        listaFiltrada.clear();
        listaFiltrada.addAll(novaListaFiltrada);
        diffResult.dispatchUpdatesTo(this);

        if (selecaoMudou && selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedIds.size());
        }
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        // Cor SEMPRE pelo status — independente de seleção
        aplicarCorPorStatus(holder, r.getStatus());

        // Seleção: só desenha um contorno por cima, nunca troca a cor do card
        boolean isSelected = r.getIdReclamacao() != null
                && selectedIds.contains(r.getIdReclamacao());

        holder.btnSelecionar.setImageResource(
                isSelected ? R.drawable.ic_check_circle_selected : R.drawable.ic_radio_unselected);

        float density = holder.itemView.getResources().getDisplayMetrics().density;
        holder.cardReclamacoesAgentes.setStrokeWidth(isSelected ? Math.round(3 * density) : 0);
        holder.cardReclamacoesAgentes.setStrokeColor(Color.parseColor("#1165B8"));

        holder.btnSelecionar.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            toggleSelection(listaFiltrada.get(adapterPosition).getIdReclamacao(), adapterPosition);
        });

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

    // ================= HOLDER =================

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView abrir;
        ImageView btnSelecionar;
        TextView data, reclamacao, status, reclamante, telefone, viewRespondido_por, respondido_por;
        MaterialCardView cardReclamacoesAgentes;
        ViewHolder(@NonNull View itemView) {
            super(itemView);

            abrir = itemView.findViewById(R.id.img_verificar_reclamacao);
            btnSelecionar = itemView.findViewById(R.id.btn_selecionar_reclamacao);
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