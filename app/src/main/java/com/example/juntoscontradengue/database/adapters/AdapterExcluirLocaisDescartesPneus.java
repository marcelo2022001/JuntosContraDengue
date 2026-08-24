package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.ExcluirLocaisDescartesPneus;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassDescarteConsciente;

import java.util.List;

public class AdapterExcluirLocaisDescartesPneus extends RecyclerView.Adapter<AdapterExcluirLocaisDescartesPneus.DescarteConscienteViewHolder> {

    private final Context context_descarte_pneus;
    private List<ClassDescarteConsciente> listDescarteConscientePneus;

    public AdapterExcluirLocaisDescartesPneus(Context context_descarte_pneus, List<ClassDescarteConsciente> listDescarteConsciente) {
        this.context_descarte_pneus = context_descarte_pneus;
        this.listDescarteConscientePneus = listDescarteConsciente;
    }

    @NonNull
    @Override
    public DescarteConscienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context_descarte_pneus).inflate(R.layout.recycler_view_excluir_locais_descartes_eletronicos_pneus, parent, false);
        return new DescarteConscienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DescarteConscienteViewHolder holder, int position) {
        ClassDescarteConsciente descarte_consciente = listDescarteConscientePneus.get(position);
        holder.textViewLocalDescarte.setText(descarte_consciente.getLocal());
        holder.textViewEnderecoDescarte.setText(descarte_consciente.getEndereco());
        holder.textViewTelefoneDescarte.setText(descarte_consciente.getFone());
        holder.textViewHorarioDescarte.setText(descarte_consciente.getHorario());

        // CORREÇÃO: Verificar se o contexto é uma instância válida
        holder.itemView.setOnClickListener(v -> {
            if (descarte_consciente.getId() != null &&
                    context_descarte_pneus instanceof ExcluirLocaisDescartesPneus) {
                ((ExcluirLocaisDescartesPneus) context_descarte_pneus)
                        .deletarLocalDescartePneusPeloId(descarte_consciente.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listDescarteConscientePneus != null ? listDescarteConscientePneus.size() : 0;
    }

    public void updateListExcluir(List<ClassDescarteConsciente> newList) {
        this.listDescarteConscientePneus = newList;
        notifyDataSetChanged(); // CORREÇÃO: Mudado de notifyAll() para notifyDataSetChanged()
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