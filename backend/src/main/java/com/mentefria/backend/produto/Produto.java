package com.mentefria.backend.produto;

import java.math.BigDecimal;
import java.util.List;

/** Produto com a composição, como GET /api/produtos devolve (Contrato §2.3). */
public record Produto(int id, String nome, BigDecimal precoVenda, boolean ativo, List<Composicao> composicao) {

    public record Composicao(int ingredienteId, String ingredienteNome, BigDecimal quantidadeUtilizada, String unidadeMedida) {}

    /** Item da composição no POST: quantidade na unidade do ingrediente (B1: o frontend converte porções). */
    public record ItemComposicao(Integer ingredienteId, BigDecimal quantidadeUtilizada) {}

    public record Novo(String nome, BigDecimal precoVenda, List<ItemComposicao> composicao) {}
    public record Criado(int id, String nome, BigDecimal precoVenda, boolean ativo) {}
    public record NovoPreco(BigDecimal precoVenda) {}
    public record PrecoAtualizado(int id, BigDecimal precoVenda) {}

    /** O mínimo que pedido e recomendação precisam saber de um produto. */
    public record Basico(int id, String nome, BigDecimal precoVenda, boolean ativo) {}
}
