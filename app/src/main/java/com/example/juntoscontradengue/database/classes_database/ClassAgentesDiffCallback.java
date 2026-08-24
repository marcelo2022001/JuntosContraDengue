package com.example.juntoscontradengue.database.classes_database;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

public class ClassAgentesDiffCallback extends DiffUtil.Callback {

    private final List<ClassAgentes> oldList;
    private final List<ClassAgentes> newList;

    public ClassAgentesDiffCallback(List<ClassAgentes> oldList, List<ClassAgentes> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // 🚨 CHAVE 1: Compara se o UID é o mesmo (o identificador único e mais seguro)
        return oldList.get(oldItemPosition).getUuid().equals(newList.get(newItemPosition).getUuid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // 🚨 CHAVE 2: Compara se o conteúdo mudou (usamos o CPF e o Email também na comparação,
        // pois eles são dados internos que podem mudar no Firebase, embora o UID permaneça)

        ClassAgentes oldAgente = oldList.get(oldItemPosition);
        ClassAgentes newAgente = newList.get(newItemPosition);

        return oldAgente.getNome().equals(newAgente.getNome()) &&
                oldAgente.getFuncao().equals(newAgente.getFuncao()) &&
                oldAgente.getTelefone().equals(newAgente.getTelefone()) &&
                oldAgente.getUrlImagem().equals(newAgente.getUrlImagem()) &&
                oldAgente.getCpf().equals(newAgente.getCpf()) && // Inclui o CPF
                oldAgente.getEmail().equals(newAgente.getEmail()); // Inclui o Email
    }
}