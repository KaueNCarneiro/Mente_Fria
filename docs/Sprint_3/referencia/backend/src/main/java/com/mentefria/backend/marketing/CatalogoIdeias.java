package com.mentefria.backend.marketing;

import java.util.List;

/**
 * Banco de ideias de campanha (roteiro v4.6, Apêndice B), em linguagem simples. {produto} vira o nome do produto.
 * Os textos são redação da equipe a partir de blogs da Saipos (não é pesquisa acadêmica): reler com o cliente antes da entrega.
 * O sistema NUNCA publica nada; só sugere (critério da US18).
 */
public final class CatalogoIdeias {
    private CatalogoIdeias() {}

    public record Ideia(String titulo, String texto) {
        public String para(String produto) { return texto.replace("{produto}", produto); }
    }

    public static final List<Ideia> IDEIAS = List.of(
        new Ideia("Destaque o tamanho", "Mostre em destaque o tamanho do copo de {produto}. Poste uma foto com uma colher ao lado."),
        new Ideia("Mostre o que vai dentro", "Poste uma foto ou um vídeo curto mostrando cada ingrediente do {produto}."),
        new Ideia("Convide para uma pausa", "Convide os clientes para uma pausa com {produto} no meio da tarde."),
        new Ideia("Foto bonita", "Monte o {produto} em camadas coloridas e tire uma foto para postar. Copo bonito faz o cliente postar também."),
        new Ideia("Combo para dividir", "Monte um combo do {produto} para duas ou mais pessoas, com preço melhor que comprar separado."),
        new Ideia("Combo com extra", "Junte ao {produto} um suco ou uma sobremesa por um preço fechado."),
        new Ideia("Desconto na primeira compra", "Dê um desconto para quem pedir o {produto} pela primeira vez."),
        new Ideia("Cartão fidelidade", "A cada certo número de compras, a próxima sai grátis. Pode começar pelo {produto}."),
        new Ideia("Edição limitada", "Anuncie o {produto} como 'só até acabar' ou 'especial do mês'."),
        new Ideia("Cupom na embalagem", "Coloque um cupom para o próximo pedido do {produto} junto da entrega ou no panfleto."),
        new Ideia("Enquete ou sorteio", "Pergunte nos stories qual {produto} o cliente prefere, ou sorteie um para quem marcar um amigo."),
        new Ideia("Data ou clima", "Em dias frios ou datas especiais, ofereça o {produto} com cobertura quente ou em combo temático."),
        // 13 a 18: acrescentadas em 27/10 (A20), redação da equipe com práticas comuns de lojas de alimentação. Reler com o cliente.
        new Ideia("Parceria pós-treino", "Combine com uma academia perto da loja: quem mostrar a carteirinha ganha desconto no {produto}."),
        new Ideia("Horário mais vazio", "Dê um desconto no {produto} no horário em que a loja costuma ficar mais vazia."),
        new Ideia("Adicional por conta da casa", "Nesta semana, quem pedir o {produto} ganha um adicional à escolha."),
        new Ideia("Indique um amigo", "Quem indicar um amigo que compre o {produto} ganha um adicional no próximo pedido."),
        new Ideia("Peça uma avaliação", "Peça para quem comprou o {produto} avaliar a loja no Google ou no aplicativo de entrega."),
        new Ideia("Reposte os clientes", "Reposte as fotos que os clientes publicarem com o {produto}, sempre pedindo permissão antes.")
    );
}
