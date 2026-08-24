package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.ExcluirTelefonesUteisActivity;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassTelefonesUteis;

import java.util.ArrayList;
import java.util.List;

public class AdapterExcluirTelefone
        extends RecyclerView.Adapter<AdapterExcluirTelefone.TelefoneViewHolder> {

    private final Context context;
    private final List<ClassTelefonesUteis> telefoneList = new ArrayList<>();
    public AdapterExcluirTelefone(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public TelefoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.recycler_view_telefone_excluir, parent, false);
        return new TelefoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TelefoneViewHolder holder, int position) {
        ClassTelefonesUteis telefoneDelete = telefoneList.get(position);
        holder.textViewLocal.setText(telefoneDelete.getLocal());
        holder.textViewTelefone.setText(telefoneDelete.getTelefone());

        holder.itemView.setOnClickListener(v -> {
            if (telefoneDelete.getId() != null) {
                ((ExcluirTelefonesUteisActivity) context)
                        .deletarTelefonePeloId(telefoneDelete.getId());
            }
        });
    }


    @Override
    public int getItemCount() {
        return telefoneList.size();
    }

    public void updateListExcluir(List<ClassTelefonesUteis> newList) {
        this.telefoneList.clear();
        this.telefoneList.addAll(newList);
        notifyDataSetChanged();
    }


    static class TelefoneViewHolder extends RecyclerView.ViewHolder {
        ImageView deleteTelefones;
        TextView textViewLocal, textViewTelefone;

        public TelefoneViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteTelefones = itemView.findViewById(R.id.img_telefone_lixeira_excluir);
            textViewLocal = itemView.findViewById(R.id.telefone_local_excluir);
            textViewTelefone = itemView.findViewById(R.id.telefone_fone_excluir);
        }
    }

}