package com.example.juntoscontradengue.database.adapters;

import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.juntoscontradengue.FullscreenActivity;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassAgentes;
import com.example.juntoscontradengue.database.classes_database.ClassAgentesDiffCallback;

import java.util.ArrayList;
import java.util.List;

public class AdapterAgentes extends RecyclerView.Adapter<AdapterAgentes.AgenteViewHolder> {
    private final List<ClassAgentes> lAgentes;
    private final Context context;
    private final OnImageResultListener imageResultListener;

    // Avisa a Activity se a imagem de uma posição carregou (true) ou falhou (false)
    public interface OnImageResultListener {
        void onImageResult(int position, boolean success);
    }

    public AdapterAgentes(List<ClassAgentes> classAgentes, Context context) {
        this(classAgentes, context, null);
    }

    public AdapterAgentes(List<ClassAgentes> classAgentes, Context context,
                          OnImageResultListener imageResultListener) {
        lAgentes = classAgentes != null ? classAgentes : new ArrayList<>();
        this.context = context;
        this.imageResultListener = imageResultListener;
    }

    @NonNull
    @Override
    public AgenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AgenteViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_agentes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AgenteViewHolder holder, int position) {
        Log.d("ADAPTER_DEBUG", "Binding position: " + position);

        ClassAgentes agente = lAgentes.get(position);

        holder.nomeAgente.setText(agente.getNome() != null ?
                agente.getNome() : "Nome não disponível");

        holder.funcaoAgente.setText(agente.getFuncao() != null ?
                agente.getFuncao() : "Função não disponível");

        String imageUrl = agente.getUrlImagem();
        final int posicao = holder.getAdapterPosition();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            if (imageResultListener != null && posicao != RecyclerView.NO_POSITION) {
                                imageResultListener.onImageResult(posicao, false);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                       DataSource dataSource, boolean isFirstResource) {
                            if (imageResultListener != null && posicao != RecyclerView.NO_POSITION) {
                                imageResultListener.onImageResult(posicao, true);
                            }
                            return false;
                        }
                    })
                    .into(holder.imgAgente);
        } else {
            holder.imgAgente.setImageResource(R.drawable.placeholder);
            if (imageResultListener != null && posicao != RecyclerView.NO_POSITION) {
                imageResultListener.onImageResult(posicao, false);
            }
        }

        holder.imgAgente.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullscreenActivity.class);
            intent.putExtra("urlMidia", imageUrl);
            intent.putExtra("tipo", "imagem");
            intent.putExtra("nome", agente.getNome());
            intent.putExtra("funcao", agente.getFuncao());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lAgentes.size();
    }

    public void updateList(List<ClassAgentes> newList) {
        final ClassAgentesDiffCallback diffCallback = new ClassAgentesDiffCallback(this.lAgentes, newList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.lAgentes.clear();
        this.lAgentes.addAll(newList);

        diffResult.dispatchUpdatesTo(this);
    }

    public static class AgenteViewHolder extends ViewHolder {
        TextView nomeAgente, funcaoAgente;
        ImageView imgAgente;

        public AgenteViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeAgente = itemView.findViewById(R.id.txt_nome_agente);
            funcaoAgente = itemView.findViewById(R.id.txt_funcao_agente);
            imgAgente = itemView.findViewById(R.id.img_agentes);

            if (nomeAgente == null) {
                Log.e("AdapterError", "TextView nome do agente não encontrado");
            }
        }
    }
}