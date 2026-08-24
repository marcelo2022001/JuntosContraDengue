package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassTelefonesUteis;

import java.util.ArrayList;
import java.util.List;

public class AdapterTelefone extends RecyclerView.Adapter<AdapterTelefone.TelefoneViewHolder> {

    private final Context context;
    private final List<ClassTelefonesUteis> telefoneList;
    private final ClickTelefones clickTelefones;

    public AdapterTelefone(Context context, List<ClassTelefonesUteis> telefoneList, ClickTelefones clickTelefones) {
        this.context = context;
        this.telefoneList = new ArrayList<>(telefoneList); // Cópia defensiva
        this.clickTelefones = clickTelefones;
    }

    @NonNull
    @Override
    public TelefoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_telefones, parent, false);
        return new TelefoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TelefoneViewHolder holder, int position) {
        ClassTelefonesUteis telefone = telefoneList.get(position);

        // Verificação de nulos
        if (telefone != null) {
            holder.textViewLocal.setText(telefone.getLocal() != null ? telefone.getLocal() : "");
            holder.textViewTelefone.setText(telefone.getTelefone() != null ? telefone.getTelefone() : "");

            holder.itemView.setOnClickListener(v -> {
                if (clickTelefones != null) {
                    clickTelefones.click_Telefones(telefone);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return telefoneList != null ? telefoneList.size() : 0;
    }

    // Método melhorado para atualizar a lista
    public void updateList(List<ClassTelefonesUteis> newList) {
        if (newList != null) {
            this.telefoneList.clear();
            this.telefoneList.addAll(newList);
            notifyDataSetChanged();
        }
    }

    /*  public void updateList(List<ClassTelefonesUteis> newList) {
        Log.d("AdapterTelefone", "Atualizando lista com " + (newList != null ? newList.size() : 0) + " itens");
        if (newList != null) {
            Log.d("AdapterTelefone", "Conteúdo da nova lista: " + newList.toString());
            this.telefoneList.clear();
            this.telefoneList.addAll(newList);
            notifyDataSetChanged();
        } else {
            this.telefoneList.clear();
            notifyDataSetChanged();
        }
        Log.d("AdapterTelefone", "Lista atualizada com " + telefoneList.size() + " itens");
    }*/

    // Otimização do ViewHolder
    static class TelefoneViewHolder extends RecyclerView.ViewHolder {
        final CardView cardViewTelefones;
        final TextView textViewLocal;
        final TextView textViewTelefone;

        public TelefoneViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewTelefones = itemView.findViewById(R.id.cardViewTelefones);
            textViewLocal = itemView.findViewById(R.id.local);
            textViewTelefone = itemView.findViewById(R.id.fone);
        }
    }

    // Interface simplificada
    public interface ClickTelefones {
        void click_Telefones(ClassTelefonesUteis telefonesClass);
    }
}