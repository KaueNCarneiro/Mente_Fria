package com.mentefria.backend.ingrediente;

import com.mentefria.backend.config.ApiException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredienteService {
    private final IngredienteRepository ingredientes;
    public IngredienteService(IngredienteRepository ingredientes) { this.ingredientes = ingredientes; }

    /** Todos os campos inválidos de uma vez (US7), com as mensagens do mapa_constraints.md. */
    static Map<String, String> validar(Ingrediente.Dados d) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (d.nome() == null || d.nome().isBlank()) erros.put("nome", "Informe o nome do ingrediente.");
        if (d.unidadeMedida() == null || d.unidadeMedida().isBlank()) erros.put("unidade_medida", "Selecione a unidade de medida.");
        if (d.custoUnitario() == null) erros.put("custo_unitario", "Informe o custo.");
        else if (d.custoUnitario().signum() < 0) erros.put("custo_unitario", "O custo não pode ser negativo.");          // CT10
        if (d.porcaoPadrao() == null) erros.put("porcao_padrao", "Informe a porção.");
        else if (d.porcaoPadrao().signum() <= 0) erros.put("porcao_padrao", "A porção deve ser maior que zero.");       // CT11
        if (d.quantidadeMinima() != null && d.quantidadeMinima().signum() < 0)
            erros.put("quantidade_minima", "A quantidade mínima não pode ser negativa.");
        return erros;
    }

    private static Ingrediente.Dados normalizar(Ingrediente.Dados d) {
        Map<String, String> erros = validar(d);
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        return new Ingrediente.Dados(d.nome().trim(), d.unidadeMedida().trim(), d.custoUnitario(), d.porcaoPadrao(),
                d.quantidadeMinima() == null ? java.math.BigDecimal.ZERO : d.quantidadeMinima());
    }

    @Transactional(readOnly = true)
    public List<Ingrediente> listar() { return ingredientes.listar(); }

    @Transactional
    public Ingrediente criar(Ingrediente.Dados d) { return ingredientes.inserir(normalizar(d)); }  // unidade inexistente: FK -> 400

    @Transactional
    public Ingrediente atualizar(int id, Ingrediente.Dados d) {
        return ingredientes.atualizar(id, normalizar(d))
                .orElseThrow(() -> new ApiException(404, "Ingrediente não encontrado.", null));
    }
}
