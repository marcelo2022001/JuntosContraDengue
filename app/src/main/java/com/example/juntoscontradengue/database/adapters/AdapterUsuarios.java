package com.example.juntoscontradengue.database.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassUsuarios;

import java.util.List;

public class AdapterUsuarios  extends RecyclerView.Adapter<AdapterUsuarios.MyViewHolder> {

    private final List<ClassUsuarios> dataList;
    private final Context context;


    public AdapterUsuarios(Context baseContext, List<ClassUsuarios> dataList) {
        this.dataList = dataList;
        this.context = baseContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.nav_header_main, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

      //  holder.alias_usuario.setText(dataList.get( position ).getAlias());

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView alias_usuario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            alias_usuario = itemView.findViewById(R.id.txtNomeUsuario);
        }
    }

}






























