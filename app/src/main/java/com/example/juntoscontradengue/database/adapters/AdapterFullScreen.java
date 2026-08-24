package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassAddSliders;

import java.util.ArrayList;

public class AdapterFullScreen extends RecyclerView.Adapter<AdapterFullScreen.MyViewHolderFullScreen> {
private final ArrayList<ClassAddSliders> dataList;
private final Context context;

public AdapterFullScreen(Context context, ArrayList<ClassAddSliders> dataList) {
        this.context = context;
        this.dataList = dataList;
        }

@NonNull
@Override
public MyViewHolderFullScreen onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_full_screen, parent, false);
        return new MyViewHolderFullScreen(view);
        }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderFullScreen holder, int position) {

        Glide.with(context).load(dataList.get(position).getUrl_imagem()).into(holder.imgViewFullScreen);
        holder.pBarFullScreen.setVisibility(View.INVISIBLE);

        }

@Override
public int getItemCount() {
        return dataList.size();
        }

public static class MyViewHolderFullScreen extends RecyclerView.ViewHolder {
    ImageView imgViewFullScreen;
    Button btnSubsImgFullScreen;
    ProgressBar pBarFullScreen;

    public MyViewHolderFullScreen(@NonNull View itemView) {
        super(itemView);
        imgViewFullScreen = itemView.findViewById(R.id.imgViewFullScreen);
        btnSubsImgFullScreen = itemView.findViewById(R.id.btnSubsImgFullScreen);
        pBarFullScreen = itemView.findViewById(R.id.pBarFullScreen);
    }
}
}

