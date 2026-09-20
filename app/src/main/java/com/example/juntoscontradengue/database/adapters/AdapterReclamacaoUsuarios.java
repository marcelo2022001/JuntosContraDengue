package com.example.juntoscontradengue.database.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.juntoscontradengue.ActivityVisualizarDenunciasUsuario;
import com.example.juntoscontradengue.R;
import com.example.juntoscontradengue.database.classes_database.ClassListarReclamacoesUsuarios;
import com.example.juntoscontradengue.extras.DateUtilsApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterReclamacaoUsuarios extends RecyclerView.Adapter<AdapterReclamacaoUsuarios.MyViewHolder> {

    // CORREÇÃO: Use uma lista simples de objetos, não ArrayList<ArrayList<...>>
    private final ArrayList<ClassListarReclamacoesUsuarios> listaReclamacoes;

    public AdapterReclamacaoUsuarios(ArrayList<ClassListarReclamacoesUsuarios> listaReclamacoes) {
        this.listaReclamacoes = listaReclamacoes;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_reclamacoes_usuarios, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ClassListarReclamacoesUsuarios objetoReclamacao = listaReclamacoes.get(position);
String data = DateUtilsApp.ConverteDataTimeStampLegivel(objetoReclamacao.getData_envio());

        holder.data_reclamacao.setText(DateUtilsApp.ConverteDataTimeStampLegivel(objetoReclamacao.getData_envio()));
        holder.reclamacao.setText(objetoReclamacao.getReclamacao());
        holder.status_reclamacao.setText(objetoReclamacao.getStatus());

        String quemrespondeu = objetoReclamacao.getRespondida_por();
        String dataResposta = objetoReclamacao.getData_resposta();

        // Verifica primeiro se NÃO é nulo E DEPOIS se NÃO está vazio
        if (quemrespondeu != null && !quemrespondeu.trim().isEmpty()) {

            holder.txtRespondidoPor.setVisibility(View.VISIBLE);
            holder.txt_respondido_por.setVisibility(View.VISIBLE);
            holder.txt_respondido_por.setText(String.format("%s em: %s", quemrespondeu, dataResposta));

        } else {
            // BOA PRÁTICA: Se o card for reciclado pelo RecyclerView,
            // precisamos garantir que os campos sumam caso a próxima reclamação não tenha resposta.
            holder.txtRespondidoPor.setVisibility(View.GONE);
            holder.txt_respondido_por.setVisibility(View.GONE);
        }


        String opcaoStatus = objetoReclamacao.getStatus();

        holder.abrir_reclamacao.setOnClickListener(v -> {

            Context context = v.getContext();
            Intent intent = getIntent(context, objetoReclamacao);


            // 4. Iniciar a activity
            context.startActivity(intent);
        });

        switch (objetoReclamacao.getStatus()) {
            case "Aguardando resposta":
                // Amarelo claro para atenção
                holder.cardReclamacoesUsuario.setCardBackgroundColor(Color.parseColor("#FFF9C4"));
                holder.status_reclamacao.setTextColor(Color.parseColor("#FBC02D"));
                break;

            case "Resolvido":
                // Verde claro para concluído
                holder.cardReclamacoesUsuario.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
                holder.status_reclamacao.setTextColor(Color.parseColor("#388E3C"));
                break;

            case "Não Resolvido":
                // Vermelho claro para não concluído
                holder.cardReclamacoesUsuario.setCardBackgroundColor(Color.parseColor("#F26D52"));
                holder.status_reclamacao.setTextColor(Color.parseColor("#F5F5F5"));
                break;

            default:
                // Cor padrão (Branco ou Cinza)
                //holder.cardReclamacoesUsuario.setCardBackgroundColor(Color.WHITE);
                holder.cardReclamacoesUsuario.setBackgroundColor(Color.parseColor("#F0ECEB"));
                holder.status_reclamacao.setTextColor(Color.BLACK); // Resetar a cor do texto também
                break;
        }

        holder.excluir_reclamacao.setOnClickListener(v -> {
            int positionAdapter = holder.getBindingAdapterPosition();
            if (positionAdapter != RecyclerView.NO_POSITION) {
                excluirReclamacao(v, objetoReclamacao, holder);
            }
        });
    }

    private static Intent getIntent(Context context, ClassListarReclamacoesUsuarios objetoReclamacao) {
        Intent intent = new Intent(context, ActivityVisualizarDenunciasUsuario.class);

        // Adicione esta flag se o context não for uma Activity (comum em Adapters)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra("ID", objetoReclamacao.getIdReclamacao());
        intent.putExtra("UUID", objetoReclamacao.getUid());
        intent.putExtra("STATUS_RECLAMACAO", objetoReclamacao.getStatus());
        intent.putExtra("RESPONDIDO_POR", objetoReclamacao.getRespondida_por());
        return intent;
    }

    @Override
    public int getItemCount() {
        return listaReclamacoes.size();
    }

    private void excluirReclamacao(View view, ClassListarReclamacoesUsuarios reclamacao, MyViewHolder holder) {
        Context context = view.getContext();
        new AlertDialog.Builder(context)
                .setTitle("Excluir Reclamação")
                .setMessage("Deseja realmente excluir?")
                .setPositiveButton("Sim", (dialog, which) -> {

                    SharedPreferences prefs = context.getSharedPreferences("configApp", Context.MODE_PRIVATE);
                    String estado = prefs.getString("estado", "");
                    String municipio = prefs.getString("municipio", "");
                    String idUser = reclamacao.getUid();
                    String idDoc = reclamacao.getIdReclamacao();

                    // 1. URL do banco específico do município
                    String urlBanco = "https://juntos-contra-dengue-" + estado + "-" + municipio + "-db.firebaseio.com/";
                    FirebaseDatabase databaseMunicipio = FirebaseDatabase.getInstance(urlBanco);

                    // 2. Referência da Reclamação
                    DatabaseReference refDB = databaseMunicipio.getReference()
                            .child("reclamacoes")
                            .child(idUser)
                            .child(idDoc);

                    // 3. Referência do Storage
                    StorageReference refStorage = FirebaseStorage.getInstance().getReference()
                            .child(estado).child(municipio).child("reclamacoesUsuarios").child(idUser).child(idDoc);
                    // --- EXECUÇÃO ---

                    // Passo A: Deletar a reclamação do banco
                    refDB.removeValue().addOnSuccessListener(unused -> {

                    // Passo B: Limpar Storage
                        refStorage.listAll().addOnSuccessListener(listResult -> {
                            for (StorageReference file : listResult.getItems()) { file.delete(); }
                            refStorage.child("midia").listAll().addOnSuccessListener(midiaRes -> {
                                for (StorageReference video : midiaRes.getItems()) { video.delete(); }
                            });
                        });

                        // ATENÇÃO: NÃO use finish() ou Intent aqui.
                        // Como você está usando ChildEventListener na Activity,
                        // o Firebase vai disparar o onChildRemoved automaticamente
                        // e a lista vai atualizar sozinha sem fechar a tela.

                        Toast.makeText(context, "Reclamação excluída com sucesso!", Toast.LENGTH_SHORT).show();

                    }).addOnFailureListener(e -> Toast.makeText(context, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView abrir_reclamacao, excluir_reclamacao;
        TextView data_reclamacao, reclamacao, status_reclamacao, txt_respondido_por;
        TextView txtRespondidoPor;
        CardView cardReclamacoesUsuario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            abrir_reclamacao = itemView.findViewById(R.id.img_verificar_reclamacaoUser);
            excluir_reclamacao = itemView.findViewById(R.id.img_excluir_reclamacaoUser);
            data_reclamacao = itemView.findViewById(R.id.txt_dt_reclamacaoUser);
            reclamacao = itemView.findViewById(R.id.txt_titulo_reclamacaoUser);
            status_reclamacao = itemView.findViewById(R.id.txt_status_reclamacaoUser);
            txtRespondidoPor = itemView.findViewById(R.id.txt_respondidoPorViewUser);
            txt_respondido_por = itemView.findViewById(R.id.txt_respondido_por);
            cardReclamacoesUsuario = itemView.findViewById(R.id.cardView_reclamacoes_usuarios);
        }
    }
}