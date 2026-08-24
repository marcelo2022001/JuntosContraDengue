package com.example.juntoscontradengue.database.adapters;

import android.app.Activity;
import android.content.Context;
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
import com.example.juntoscontradengue.database.classes_database.ClassTrabAgentes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterExcluirTrabAgentes extends RecyclerView.Adapter<AdapterExcluirTrabAgentes.AgenteViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private final List<ClassTrabAgentes> listExcluirTrabAgentes;
    private final Context context_excluir_trab_agentes;
    private final Set<String> selectedIds = new HashSet<>();
    private final OnSelectionChangedListener selectionChangedListener;

    public AdapterExcluirTrabAgentes(Context context_excluir_trab_agentes,
                                     List<ClassTrabAgentes> listExcluirTrabAgentes,
                                     OnSelectionChangedListener selectionChangedListener) {
        this.listExcluirTrabAgentes = listExcluirTrabAgentes;
        this.context_excluir_trab_agentes = context_excluir_trab_agentes;
        this.selectionChangedListener = selectionChangedListener;
    }

    @NonNull
    @Override
    public AgenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trab_agentes_excluir, parent, false);
        return new AgenteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgenteViewHolder holder, int position) {
        ClassTrabAgentes excluirTrabAgente = listExcluirTrabAgentes.get(position);

        if (context_excluir_trab_agentes instanceof Activity
                && ((Activity) context_excluir_trab_agentes).isDestroyed()) {
            Log.e("AdapterContext", "Activity is destroyed");
            return;
        }

        holder.captionExcluirTrabAgente.setText(excluirTrabAgente.getTitulo());

        boolean isSelected = excluirTrabAgente.getId() != null
                && selectedIds.contains(excluirTrabAgente.getId());
        holder.btnSelecionarTrabAgente.setImageResource(
                isSelected ? R.drawable.ic_check_circle_selected : R.drawable.ic_radio_unselected);

        holder.btnSelecionarTrabAgente.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            toggleSelection(listExcluirTrabAgentes.get(adapterPosition).getId(), adapterPosition);
        });

        Glide.with(context_excluir_trab_agentes)
                .load(excluirTrabAgente.getUrlMidia())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imgExcluirTrabAgente);
    }

    private void toggleSelection(String id, int position) {
        if (id == null) return;
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        notifyItemChanged(position);
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedIds.size());
        }
    }

    /** Chame depois de excluir os selecionados no Firebase. */
    public void clearSelection() {
        if (selectedIds.isEmpty()) return;
        for (int i = 0; i < listExcluirTrabAgentes.size(); i++) {
            ClassTrabAgentes item = listExcluirTrabAgentes.get(i);
            if (item.getId() != null && selectedIds.contains(item.getId())) {
                notifyItemChanged(i);
            }
        }
        selectedIds.clear();

        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(0);
        }
    }

    public List<ClassTrabAgentes> getSelectedItems() {
        List<ClassTrabAgentes> selecionados = new ArrayList<>();
        for (ClassTrabAgentes item : listExcluirTrabAgentes) {
            if (item.getId() != null && selectedIds.contains(item.getId())) {
                selecionados.add(item);
            }
        }
        return selecionados;
    }

    @Override
    public int getItemCount() {
        return listExcluirTrabAgentes.size();
    }

    public static class AgenteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgExcluirTrabAgente;
        TextView captionExcluirTrabAgente;
        ImageView btnSelecionarTrabAgente;

        public AgenteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExcluirTrabAgente = itemView.findViewById(R.id.img_trab_agente_excluir);
            captionExcluirTrabAgente = itemView.findViewById(R.id.caption_trab_agente_excluir);
            btnSelecionarTrabAgente = itemView.findViewById(R.id.btn_selecionar_trab_agente);
        }
    }
}