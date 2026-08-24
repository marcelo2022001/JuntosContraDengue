package com.example.juntoscontradengue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.juntoscontradengue.databinding.ActivityVisualizarRespostasBinding;
import com.example.juntoscontradengue.extras.TopicHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VisualizarResposta extends AppCompatActivity {
    private ActivityVisualizarRespostasBinding bindingUser;
    private TextInputLayout inputLayout;
    private TextInputEditText inputEditText;
    private CardView cardViewImagensResposta;
    private List<ImageView> imageViews;

    private String UUID, ID, estado, municipio, STATUS_RECLAMACAO, RESPONDIDO_POR;
    private String respPor, dataResposta, respostaTxt;
    private String tokenDestinatario; // token FCM de quem fez a reclamação

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Primeiro infla o Binding
        bindingUser = ActivityVisualizarRespostasBinding.inflate(getLayoutInflater());
        setContentView(bindingUser.getRoot());

        // 2. Recupera os extras com segurança
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ID = extras.getString("ID");
            UUID = extras.getString("UUID");
            STATUS_RECLAMACAO = extras.getString("STATUS_RECLAMACAO");
            RESPONDIDO_POR = extras.getString("RESPONDIDO_POR");
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UUID = user != null ? user.getUid() : null;

        if (ID == null || UUID == null || STATUS_RECLAMACAO == null || RESPONDIDO_POR == null) {
            Toast.makeText(this, "Erro ao carregar ID/UUID da reclamação", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("configApp", MODE_PRIVATE);
        estado = prefs.getString("estado", null);
        municipio = prefs.getString("municipio", null);

        inputLayout = bindingUser.txtInputLayoutVisualizarRespostaReclamacao;
        inputLayout.setEnabled(false);
        inputEditText = bindingUser.txtInputEditVisualizarResposta;

        cardViewImagensResposta = bindingUser.cardViewScrollRespostaUsuario;
        ImageView img1 = bindingUser.image1ResponderReclamacaoUsers;
        ImageView img2 = bindingUser.image2ResponderReclamacaoUsers;
        ImageView img3 = bindingUser.image3ResponderReclamacaoUsers;
        ImageView img4 = bindingUser.image4ResponderReclamacaoUsers;
        imageViews = new ArrayList<>();
        imageViews.add(img1);
        imageViews.add(img2);
        imageViews.add(img3);
        imageViews.add(img4);

        Button btnEnviarVisualizarResposta = bindingUser.btnResponderReclamacaoUsers;
        btnEnviarVisualizarResposta.setVisibility(View.INVISIBLE);

        setSupportActionBar(bindingUser.tbVisualizarResposta);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reclamações");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (!Objects.requireNonNull(RESPONDIDO_POR).isEmpty()) {
            bindingUser.txtViewReclamacaoAtendidaPor.setText(RESPONDIDO_POR);
        }

        if (STATUS_RECLAMACAO.equals("Resolvido")) {
            bindingUser.radioButtonReclamaOResolvidaSim.setChecked(true);
            bindingUser.radioButtonReclamaOResolvidaNao.setEnabled(false);
        } else if (STATUS_RECLAMACAO.equals("Não Resolvido")) {
            bindingUser.radioButtonReclamaOResolvidaNao.setChecked(true);
            bindingUser.radioButtonReclamaOResolvidaSim.setEnabled(false);
        } else {
            btnEnviarVisualizarResposta.setVisibility(View.VISIBLE);
        }

        btnEnviarVisualizarResposta.setOnClickListener(v -> enviarEscolha());

        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > inputLayout.getCounterMaxLength()) {
                    inputLayout.setError("Tamanho de " + inputLayout.getCounterMaxLength() + " caracteres excedido!");
                } else {
                    inputLayout.setError(null);
                }
            }
        });

        buscarDadosFirebase();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    private void enviarEscolha() {
        String statusParaSalvar = "";
        if (bindingUser.radioButtonReclamaOResolvidaSim.isChecked()) {
            statusParaSalvar = "Resolvido";
        } else if (bindingUser.radioButtonReclamaOResolvidaNao.isChecked()) {
            statusParaSalvar = "Não Resolvido";
        }

        if (statusParaSalvar.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione se foi resolvido ou não.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference refStatus = FirebaseDatabase.getInstance().getReference()
                .child("cadastros")
                .child(estado)
                .child(municipio)
                .child("reclamacoes")
                .child(UUID)
                .child(ID);

        String finalStatus = statusParaSalvar;
        refStatus.child("status").setValue(statusParaSalvar).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(VisualizarResposta.this, "Status atualizado!", Toast.LENGTH_SHORT).show();
                enfileirarNotificacao(finalStatus);
                startActivity(new Intent(VisualizarResposta.this, ListarReclamacoesUsuarios.class));
            } else {
                Toast.makeText(VisualizarResposta.this, "Erro ao salvar status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * FIX Problema 2: usa o mesmo mecanismo já funcional do Denunciar.java —
     * grava um item em "notifications_queue" com título/mensagem/tópicos, e a
     * Cloud Function existente (sendPushNotification) é quem envia o push para
     * admins e agentes. Não fazemos mais chamada direta de FCM pelo app (nem à
     * Legacy API, que estava quebrada e com a key exposta).
     * A função aceita "token" (singular), "topico" (singular) ou "topicos" (array)
     * por item, em um if/else-if — por isso o push para o autor da reclamação vai
     * num item separado com "token", em vez de misturar com "topicos" no mesmo item.
     */
    private void enfileirarNotificacao(String novoStatus) {
        String topicoAgentes = TopicHelper.getAgentesTopic(this);
        String topicoAdmins = TopicHelper.getAdminsTopic(this);

        ArrayList<String> topicos = new ArrayList<>();
        topicos.add(topicoAgentes);
        topicos.add(topicoAdmins);

        Map<String, Object> notificacao = new HashMap<>();
        notificacao.put("titulo", "Reclamação atualizada");
        notificacao.put("mensagem", "Uma reclamação em " + municipio + " foi marcada como: " + novoStatus);
        notificacao.put("topicos", topicos);

        DatabaseReference filaRef = FirebaseDatabase.getInstance().getReference("notifications_queue");

        // A Cloud Function trata token/topico/topicos com if/else-if — ou seja, um único item
        // só é processado por UM desses caminhos (se "token" vier junto com "topicos" no mesmo
        // item, "topicos" seria ignorado). Por isso mandamos dois itens separados na fila.
        filaRef.push().setValue(notificacao);

        if (tokenDestinatario != null && !tokenDestinatario.isEmpty()) {
            Map<String, Object> notificacaoUsuario = new HashMap<>();
            notificacaoUsuario.put("titulo", "Sua Reclamação");
            notificacaoUsuario.put("mensagem", "Sua reclamação foi marcada como: " + novoStatus);
            notificacaoUsuario.put("token", tokenDestinatario);
            filaRef.push().setValue(notificacaoUsuario);
        }
    }

    private void buscarDadosFirebase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("cadastros")
                .child(estado)
                .child(municipio)
                .child("reclamacoes")
                .child(UUID)
                .child(ID);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                respPor = snapshot.child("respondida_por").getValue(String.class);
                dataResposta = snapshot.child("data_resposta").getValue(String.class);
                respostaTxt = snapshot.child("resposta_reclamacao").getValue(String.class);
                tokenDestinatario = snapshot.child("tokenFCM").getValue(String.class);

                bindingUser.txtViewReclamacaoAtendidaPor.setText(String.format("%s em: %s", respPor, dataResposta));
                inputEditText.setText(respostaTxt);

                // FIX Problema 1: as imagens de resposta ficam dentro do nó "midias_resposta",
                // e as chaves não são fixas (podem ser img_1/img_2, midia_0..midia_3, etc).
                // Por isso iteramos todos os filhos do nó em vez de ler chaves fixas.
                List<String> urlsResposta = new ArrayList<>();
                DataSnapshot midiasResposta = snapshot.child("midias_resposta");
                if (midiasResposta.exists()) {
                    for (DataSnapshot midia : midiasResposta.getChildren()) {
                        String url = midia.getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            urlsResposta.add(url);
                        }
                    }
                }

                exibirImagensResposta(urlsResposta);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VisualizarResposta.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Preenche as ImageViews disponíveis (até 4) com as URLs encontradas em "midias_resposta".
     * Se não houver nenhuma URL, o card inteiro continua oculto (visibility="gone" no XML).
     */
    private void exibirImagensResposta(List<String> urls) {
        boolean temImagem = !urls.isEmpty();

        for (int i = 0; i < imageViews.size(); i++) {
            ImageView imageView = imageViews.get(i);
            if (i < urls.size()) {
                String url = urls.get(i);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(urls.get(i)).into(imageView);
                imageView.setOnClickListener(v -> abrirImagemFullscreen(url));
            } else {
                imageView.setOnClickListener(null);
                imageView.setVisibility(View.GONE);
            }
        }

        cardViewImagensResposta.setVisibility(temImagem ? View.VISIBLE : View.GONE);
    }
    private void abrirImagemFullscreen(String url) {
        Intent intent = new Intent(VisualizarResposta.this, FullscreenActivity.class);
        intent.putExtra("urlMidia", url);
        intent.putExtra("tipo", "imagem");
        startActivity(intent);
    }
}
