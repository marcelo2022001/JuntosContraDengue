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
import com.example.juntoscontradengue.database.classes_database.ClassTelefonesUteis;

import java.util.ArrayList;

public class AdapterFoneBusca extends RecyclerView.Adapter<AdapterFoneBusca.AdapterFoneBuscaViewolder> {

    public Context c;
    public ArrayList<ClassTelefonesUteis> arrayList;
public AdapterFoneBusca (Context c, ArrayList<ClassTelefonesUteis> arrayList)
{
this.c = c;
this.arrayList = arrayList;
}
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public AdapterFoneBuscaViewolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.recycler_view_telefones, parent, false);
    return new AdapterFoneBuscaViewolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFoneBuscaViewolder holder, int position) {
        ClassTelefonesUteis classTelefonesUteis = arrayList.get(position);

        holder.local.setText(classTelefonesUteis.getLocal());
        holder.phone.setText(classTelefonesUteis.getTelefone());
    }

    public static class AdapterFoneBuscaViewolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        TextView local, phone;
        public AdapterFoneBuscaViewolder(@NonNull View itemView) {
            super( itemView );
            cardView = itemView.findViewById(R.id.cardViewTelefones);
            local = itemView.findViewById(R.id.local);
            phone = itemView.findViewById(R.id.fone);
        }
    }
}
