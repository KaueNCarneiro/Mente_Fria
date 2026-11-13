package com.mentefria.backend.unidade;

import com.mentefria.backend.config.ApiException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US26 (Contrato §2.11): cadastrar unidade e alterar a porção padrão. Alterar o padrão NÃO muda os ingredientes. */
@Service
public class UnidadeMedidaService {
    static final String MSG_UNIDADE = "Informe a unidade de medida (até 20 caracteres).";
    static final String MSG_PORCAO_AUSENTE = "Informe a porção.";
    static final String MSG_PORCAO_ZERO = "A porção deve ser maior que zero.";
    static final String MSG_PORCAO_FORMATO = "Use no máximo 3 casas decimais e um valor menor que 10.000.000.";
    /** A coluna é DECIMAL(10,3): o maior valor é 9.999.999,999. */
    private static final BigDecimal LIMITE = new BigDecimal("10000000");

    private final UnidadeMedidaRepository unidades;
    public UnidadeMedidaService(UnidadeMedidaRepository unidades) { this.unidades = unidades; }

    /** Como o e-mail: sem espaços nas pontas e em minúsculas (" G " vira "g"). A FK de ingrediente diferencia maiúsculas (T14). */
    static String normalizar(String unidade) {
        return unidade == null ? null : unidade.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * A porção é validada antes de gravar: o PostgreSQL arredondaria sem avisar o que tem mais de 3 casas,
     * e um valor grande demais daria "numeric field overflow" (500).
     */
    static void validarPorcao(BigDecimal porcao, Map<String, String> erros) {
        if (porcao == null) erros.put("porcao_padrao", MSG_PORCAO_AUSENTE);
        else if (porcao.signum() <= 0) erros.put("porcao_padrao", MSG_PORCAO_ZERO);
        else if (porcao.stripTrailingZeros().scale() > 3 || porcao.compareTo(LIMITE) >= 0) erros.put("porcao_padrao", MSG_PORCAO_FORMATO);
    }

    @Transactional(readOnly = true)
    public List<UnidadeMedida> listar() { return unidades.listar(); }

    @Transactional
    public UnidadeMedida criar(UnidadeMedida.Nova nova) {
        String unidade = normalizar(nova.unidadeMedida());
        Map<String, String> erros = new LinkedHashMap<>();
        if (unidade == null || unidade.isEmpty() || unidade.length() > 20) erros.put("unidade_medida", MSG_UNIDADE);
        validarPorcao(nova.porcaoPadrao(), erros);
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        return unidades.inserir(unidade, nova.porcaoPadrao());       // repetida: pkey -> 409 (GlobalExceptionHandler)
    }

    /** O corpo é validado ANTES de procurar a unidade: corpo inválido dá 400 mesmo que ela não exista (§2.11). */
    @Transactional
    public UnidadeMedida atualizarPorcao(String unidadeDaUrl, UnidadeMedida.NovaPorcao corpo) {
        Map<String, String> erros = new LinkedHashMap<>();
        validarPorcao(corpo.porcaoPadrao(), erros);
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        return unidades.atualizarPorcao(normalizar(unidadeDaUrl), corpo.porcaoPadrao())
                .orElseThrow(() -> new ApiException(404, "Unidade de medida não encontrada.", null));
    }
}
