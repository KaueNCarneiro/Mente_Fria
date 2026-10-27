package com.mentefria.backend.produto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

/** Os casos da tabela do sql_repositorios.md §4 (9 linhas, 12 nomes), mais o null. */
class RegraAcaiTest {
    @ParameterizedTest
    @ValueSource(strings = {"Açaí (polpa)", "AÇAÍ ZERO", "polpa de acai", "Polpa de açaí", "  Açaí  ", "Açaí"})
    void ehAcai(String nome) { assertThat(RegraAcai.ehIngredienteAcai(nome)).isTrue(); }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Granola", "Leite condensado", "Copo descartável 500ml", "Assaí"})   // "Assaí": limitação conhecida
    void naoEhAcai(String nome) { assertThat(RegraAcai.ehIngredienteAcai(nome)).isFalse(); }

    @Test
    void composicaoPrecisaDeAoMenosUmAcai() {
        assertThat(RegraAcai.composicaoTemAcai(List.of("Granola", "Açaí (polpa)"))).isTrue();
        assertThat(RegraAcai.composicaoTemAcai(List.of("Granola", "Banana"))).isFalse();
    }
}
