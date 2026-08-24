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
import com.example.juntoscontradengue.database.classes_database.ClassDescarteConsciente;

import java.util.List;

public class AdapterResiduosPneus extends RecyclerView.Adapter<AdapterResiduosPneus.ViewHolder> {

    private final List<ClassDescarteConsciente> descarteConscienteClass;
    private final ClickResiduosPneus clickResiduosPneus;

    public AdapterResiduosPneus(Context context, List<ClassDescarteConsciente> descarteConscienteClass, ClickResiduosPneus clickResiduosPneus) {
        this.descarteConscienteClass = descarteConscienteClass;
        this.clickResiduosPneus = clickResiduosPneus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_descarte_consciente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ClassDescarteConsciente descarteConcienteClass1 = descarteConscienteClass.get(position);

        holder.local.setText(String.format("  %s", descarteConcienteClass1.getLocal() != null ? descarteConcienteClass1.getLocal() : ""));
        holder.endereco.setText(String.format("  %s", descarteConcienteClass1.getEndereco() != null ? descarteConcienteClass1.getEndereco() : ""));
        holder.fone.setText(String.format("  %s", descarteConcienteClass1.getFone() != null ? descarteConcienteClass1.getFone() : ""));
        holder.horario.setText(String.format("  %s", descarteConcienteClass1.getHorario() != null ? descarteConcienteClass1.getHorario() : ""));

        holder.cardView.setOnClickListener(view -> {
            if (clickResiduosPneus != null) {
                clickResiduosPneus.click_DescartePneus(descarteConcienteClass1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return descarteConscienteClass.size();
    }

    public interface ClickResiduosPneus {
        void click_DescartePneus(ClassDescarteConsciente descartePneusClass);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView local, endereco, fone, horario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewDescartePneus);
            local = itemView.findViewById(R.id.local_descarte_consciente);
            endereco = itemView.findViewById(R.id.endereco_descarte_consciente);
            fone = itemView.findViewById(R.id.fone_descarte_consciente);
            horario = itemView.findViewById(R.id.horario_descarte_consciente);
        }
    }
}