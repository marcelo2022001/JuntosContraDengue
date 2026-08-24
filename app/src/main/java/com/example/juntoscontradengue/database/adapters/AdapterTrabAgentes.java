package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.FullscreenActivity;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassTrabAgentes;

import java.util.List;

public class AdapterTrabAgentes extends RecyclerView.Adapter<AdapterTrabAgentes.AgenteViewHolder> {
    private final List<ClassTrabAgentes> agenteList;
    private final Context context;

    public AdapterTrabAgentes(List<ClassTrabAgentes> agenteList, Context context) {
        this.agenteList = agenteList;
        this.context = context;
    }

    @NonNull
    @Override
    public AgenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trab_agente, parent, false);
        return new AgenteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgenteViewHolder holder, int position) {
        ClassTrabAgentes agente = agenteList.get(position);

        holder.captionAgente.setText(agente.getTitulo());

        String url = agente.getUrlMidia();
            // IMAGEM
            holder.imgAgente.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(holder.imgAgente);

            holder.imgAgente.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullscreenActivity.class);
                intent.putExtra("urlMidia", url);
                intent.putExtra("tipo", "imagem");
                context.startActivity(intent);
            });
        }

    @Override
    public int getItemCount() {

        Log.d("Adapter", "Número de itens na lista: " + agenteList.size());
        return agenteList.size();
    }

    public static class AgenteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAgente;
        TextView captionAgente;

        public AgenteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAgente = itemView.findViewById(R.id.img_agente);
            captionAgente = itemView.findViewById(R.id.caption_agente);

            // Log para verificar se os views estão sendo encontrados
            if (imgAgente == null) {
                Log.e("AdapterError", "ImageView imgAgente não encontrado");
            }
            if (captionAgente == null) {
                Log.e("AdapterError", "TextView captionAgente não encontrado");
            }
        }
    }
}