package com.mentefria.backend.produto;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.ingrediente.IngredienteRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {
    private final ProdutoRepository produtos;
    private final IngredienteRepository ingredientes;

    public ProdutoService(ProdutoRepository produtos, IngredienteRepository ingredientes) {
        this.produtos = produtos; this.ingredientes = ingredientes;
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() { return produtos.listarComComposicao(); }

    /** Valida tudo ANTES de gravar; produto e composição na MESMA transação (se a composição falhar, o produto some também). */
    @Transactional
    public Produto.Criado criar(Produto.Novo req) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (req.nome() == null || req.nome().isBlank()) erros.put("nome", "Informe o nome do produto.");
        if (req.precoVenda() == null || req.precoVenda().signum() <= 0) erros.put("preco_venda", "O preço deve ser maior que zero.");  // CT17
        List<Produto.ItemComposicao> composicao = req.composicao();
        if (composicao == null || composicao.isEmpty()) {
            erros.put("composicao", "Adicione ao menos um ingrediente.");                                               // CT13
        } else {
            List<Integer> ids = composicao.stream().map(Produto.ItemComposicao::ingredienteId).toList();
            if (ids.stream().anyMatch(Objects::isNull)) erros.put("composicao", "Ingrediente não encontrado.");
            else if (ids.stream().distinct().count() < ids.size())                                           // antes do "não encontrado"
                erros.put("composicao", "Este ingrediente já foi adicionado ao produto.");
            else if (composicao.stream().anyMatch(i -> i.quantidadeUtilizada() == null || i.quantidadeUtilizada().signum() <= 0))
                erros.put("composicao", "A quantidade utilizada deve ser maior que zero.");                                // CT14
            else {
                Map<Integer, String> nomes = ingredientes.buscarNomesPorIds(ids);
                if (nomes.size() < ids.size()) erros.put("composicao", "Ingrediente não encontrado.");
                else if (!RegraAcai.composicaoTemAcai(nomes.values()))
                    erros.put("composicao", "O produto precisa ter o açaí entre os ingredientes.");                       // CT15
            }
        }
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        String nome = req.nome().trim();
        int id = produtos.inserir(nome, req.precoVenda());
        produtos.inserirComposicao(id, composicao);
        return new Produto.Criado(id, nome, req.precoVenda(), true);
    }

    /** Só Administrador (SecurityConfig). Preço <= 0 -> 400; produto inexistente -> 404 (CT16, CT17). */
    @Transactional
    public Produto.PrecoAtualizado atualizarPreco(int id, BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) throw ApiException.campo(400, "preco_venda", "O preço deve ser maior que zero.");
        if (produtos.atualizarPreco(id, preco) == 0) throw new ApiException(404, "Produto não encontrado.", null);
        return new Produto.PrecoAtualizado(id, preco);
    }
}
