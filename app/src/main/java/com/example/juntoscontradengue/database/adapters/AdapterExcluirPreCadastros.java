package com.example.juntoscontradengue.database.adapters;

import static com.example.juntoscontradengue.extras.DateUtilsApp.ConverteDataTimeStampLegivel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassDeletarPreCadastro;

import java.util.List;

public class AdapterExcluirPreCadastros
        extends RecyclerView.Adapter<AdapterExcluirPreCadastros.PreCadastroViewHolder> {

    private final List<ClassDeletarPreCadastro> lista;
    private OnAgenteDeleteListener deleteListener;

    public interface OnAgenteDeleteListener {
        void onDeleteClick(ClassDeletarPreCadastro cadastro, int position);
    }

    public void setOnAgenteDeleteListener(OnAgenteDeleteListener listener) {
        this.deleteListener = listener;
    }

    public AdapterExcluirPreCadastros(List<ClassDeletarPreCadastro> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public PreCadastroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_excluir_pre_cadastro, parent, false);
        return new PreCadastroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreCadastroViewHolder holder, int position) {

        ClassDeletarPreCadastro cadastro = lista.get(position);

        holder.nome.setText(cadastro.getNome_pre_cadastro());
        holder.funcao.setText(cadastro.getFuncao_pre_cadastro());
        holder.data.setText(ConverteDataTimeStampLegivel(cadastro.getData_pre_cadastro()) );

        holder.btnExcluir.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(cadastro, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class PreCadastroViewHolder extends RecyclerView.ViewHolder {

        TextView nome, funcao, data;
        Button btnExcluir;

        public PreCadastroViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.txt_exclui_nome_pre_cadastro);
            funcao = itemView.findViewById(R.id.txt_exclui_funcao_pre_cadastros);
            data = itemView.findViewById(R.id.txt_excluir_pre_cadastros_data_cadastro);
            btnExcluir = itemView.findViewById(R.id.btn_excluir_pre_cadastros);
        }
    }
}
