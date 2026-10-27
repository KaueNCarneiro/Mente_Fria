package com.mentefria.backend.produto;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;

/**
 * Regra do açaí (US5 / CT15), decidida em 19/09/2026 (sql_repositorios.md §4): um ingrediente conta como açaí quando o nome,
 * sem acentos e sem diferenciar maiúsculas de minúsculas, contém "acai". Isolada aqui: se um dia houver a coluna eh_base, só isto muda.
 */
public final class RegraAcai {
    private RegraAcai() {}

    public static boolean ehIngredienteAcai(String nome) {
        if (nome == null) return false;
        String semAcentos = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT).contains("acai");
    }

    public static boolean composicaoTemAcai(Collection<String> nomesDosIngredientes) {
        return nomesDosIngredientes.stream().anyMatch(RegraAcai::ehIngredienteAcai);
    }
}
