package com.example.juntoscontradengue.database.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.databinding.ItemMidiaBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterVisualizarDenunciasAgentes extends RecyclerView.Adapter<AdapterVisualizarDenunciasAgentes.MidiaViewHolder> {

    private final List<MidiaItem> midias = new ArrayList<>();
    private OnMidiaClickListener listener;

    public interface OnMidiaClickListener {
        void onImagemClick(String url);
        void onVideoClick(String url);
    }

    public void setOnMidiaClickListener(OnMidiaClickListener listener) {
        this.listener = listener;
    }

    public void setMidias(List<MidiaItem> novasMidias) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return midias.size();
            }

            @Override
            public int getNewListSize() {
                return novasMidias.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return midias.get(oldItemPosition).getUrl()
                        .equals(novasMidias.get(newItemPosition).getUrl());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                MidiaItem oldItem = midias.get(oldItemPosition);
                MidiaItem newItem = novasMidias.get(newItemPosition);
                return oldItem.getUrl().equals(newItem.getUrl())
                        && oldItem.isVideo() == newItem.isVideo();
            }
        });

        this.midias.clear();
        this.midias.addAll(novasMidias);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public MidiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMidiaBinding binding = ItemMidiaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MidiaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MidiaViewHolder holder, int position) {
        MidiaItem item = midias.get(position);

        if (item.isVideo()) {
            holder.binding.playerView.setVisibility(View.GONE);
            holder.binding.imgMidia.setVisibility(View.VISIBLE);
            holder.binding.iconPlay.setVisibility(View.VISIBLE);

            if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty()) {
                Picasso.get()
                        .load(item.getThumbnailUrl())
                        .placeholder(R.drawable.camera_video)
                        .error(R.drawable.error_image)
                        .into(holder.binding.imgMidia);
            } else {
                holder.binding.imgMidia.setImageResource(R.drawable.camera_video);
            }

            View.OnClickListener abrirVideo = v -> {
                if (listener != null) {
                    listener.onVideoClick(item.getUrl());
                }
            };
            holder.binding.iconPlay.setOnClickListener(abrirVideo);
            holder.itemView.setOnClickListener(abrirVideo);

        } else {
            holder.binding.imgMidia.setVisibility(View.VISIBLE);
            holder.binding.playerView.setVisibility(View.GONE);
            holder.binding.iconPlay.setVisibility(View.GONE);

            Picasso.get()
                    .load(item.getUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(holder.binding.imgMidia);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImagemClick(item.getUrl());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return midias.size();
    }

    // ✅ Classe interna - NÃO coloque private, apenas static
    public static class MidiaViewHolder extends RecyclerView.ViewHolder {
        public ItemMidiaBinding binding;

        public MidiaViewHolder(ItemMidiaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // Classe para representar cada mídia
    public static class MidiaItem {
        private final String url;
        private final boolean isVideo;
        private final String thumbnailUrl; // null para imagens ou quando não há thumbnail

        public MidiaItem(String url, boolean isVideo, String thumbnailUrl) {
            this.url = url;
            this.isVideo = isVideo;
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getUrl() { return url; }
        public boolean isVideo() { return isVideo; }
        public String getThumbnailUrl() { return thumbnailUrl; }
    }
}