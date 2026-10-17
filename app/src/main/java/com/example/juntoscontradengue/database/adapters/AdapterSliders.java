package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.SubstituirSlider;
import com.example.juntoscontradengue.database.classes_database.ClassAddSliders;

import java.util.ArrayList;

public class AdapterSliders extends RecyclerView.Adapter<AdapterSliders.MyViewHolder> {
    private final ArrayList<ClassAddSliders> dataList;
    private final Context context;

    public AdapterSliders(Context context, ArrayList<ClassAddSliders> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_sliders, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);

        // único uso de 'position': imediatamente, para pegar o item
        ClassAddSliders slider = dataList.get(position);
        final String imageUrl = slider.getUrl_imagem();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            Log.e("GLIDE_ERROR", "Falha ao carregar: " + imageUrl, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.recyclerImage);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.recyclerImage.setImageResource(R.drawable.error_image);
            Log.e("DEBUG_URL", "URL vazia para o slider id " + slider.getId());
        }

        holder.sub_img.setOnClickListener(v -> {
            // posição atual, consultada só na hora do clique
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos >= dataList.size()) return;

            ClassAddSliders atual = dataList.get(pos);

            Intent intent = new Intent(context, SubstituirSlider.class);
            intent.putExtra("urlImagem", atual.getUrl_imagem());
            intent.putExtra("idSlider", String.valueOf(atual.getId()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView recyclerImage;
        Button sub_img;
        ProgressBar progressBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerImage = itemView.findViewById(R.id.imgViewAddSlider);
            sub_img = itemView.findViewById(R.id.btnSubstituirImagem);
            progressBar = itemView.findViewById(R.id.pBarAddSlider);
        }
    }
}