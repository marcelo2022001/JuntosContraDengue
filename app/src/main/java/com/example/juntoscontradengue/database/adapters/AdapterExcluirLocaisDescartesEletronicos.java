package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.ExcluirLocaisDescartesEletronicosActivity;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassDescarteConsciente;

import java.util.List;

public class AdapterExcluirLocaisDescartesEletronicos extends RecyclerView.Adapter<AdapterExcluirLocaisDescartesEletronicos.DescarteConscienteViewHolder> {

    private final Context context;
    private List<ClassDescarteConsciente> listDescarteConscienteEletronicos;

    public AdapterExcluirLocaisDescartesEletronicos(Context context, List<ClassDescarteConsciente> listDescarteConsciente) {
        this.context = context;
        this.listDescarteConscienteEletronicos = listDescarteConsciente;
    }

    @NonNull
    @Override
    public DescarteConscienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_excluir_locais_descartes_eletronicos_pneus, parent, false);
        return new DescarteConscienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DescarteConscienteViewHolder holder, int position) {
        ClassDescarteConsciente descarteConsciente = listDescarteConscienteEletronicos.get(position);
        holder.textViewLocalDescarte.setText(descarteConsciente.getLocal());
        holder.textViewEnderecoDescarte.setText(descarteConsciente.getEndereco());
        holder.textViewTelefoneDescarte.setText(descarteConsciente.getFone());
        holder.textViewHorarioDescarte.setText(descarteConsciente.getHorario());

        // Clique para deletar
        holder.itemView.setOnClickListener(v -> {
            if (descarteConsciente.getId() != null && context instanceof ExcluirLocaisDescartesEletronicosActivity) {
                ((ExcluirLocaisDescartesEletronicosActivity) context)
                        .deletarLocalDescarteEletronicosPeloId(descarteConsciente.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listDescarteConscienteEletronicos != null ? listDescarteConscienteEletronicos.size() : 0;
    }

    public void updateListExcluir(List<ClassDescarteConsciente> newList) {
        // Usar DiffUtil para atualizações eficientes
        this.listDescarteConscienteEletronicos = newList;
            notifyDataSetChanged();
    }

    static class DescarteConscienteViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLocalDescarte, textViewEnderecoDescarte, textViewTelefoneDescarte;
        TextView textViewHorarioDescarte;

        public DescarteConscienteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLocalDescarte = itemView.findViewById(R.id.excluir_local_descarte_consciente);
            textViewEnderecoDescarte = itemView.findViewById(R.id.excluir_endereco_descarte_consciente);
            textViewTelefoneDescarte = itemView.findViewById(R.id.excluir_fone_descarte_consciente);
            textViewHorarioDescarte = itemView.findViewById(R.id.excluir_horario_descarte_consciente);
        }
    }
}