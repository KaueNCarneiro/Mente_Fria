package com.mentefria.backend.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private record Msg(int status, String campo, String texto) {}
    private static Map.Entry<String, Msg> m(String k, int st, String campo, String texto) { return Map.entry(k, new Msg(st, campo, texto)); }

    private static final Map<String, Msg> MAPA = Map.ofEntries(
        m("uq_usuario_email", 409, "email", "Este e-mail já está cadastrado."),
        m("chk_usuario_perfil", 400, "perfil", "Perfil de acesso inválido."),
        m("NN:usuario.nome", 400, "nome", "Informe o nome."),
        m("NN:usuario.email", 400, "email", "Informe o e-mail."),
        m("NN:usuario.senha_hash", 400, "senha", "Informe a senha."),
        m("NN:ingrediente.nome", 400, "nome", "Informe o nome do ingrediente."),
        m("NN:ingrediente.unidade_medida", 400, "unidade_medida", "Selecione a unidade de medida."),
        m("NN:ingrediente.custo_unitario", 400, "custo_unitario", "Informe o custo."),
        m("NN:ingrediente.porcao_padrao", 400, "porcao_padrao", "Informe a porção."),
        m("chk_ingrediente_custo_unitario", 400, "custo_unitario", "O custo não pode ser negativo."),
        m("chk_ingrediente_porcao_padrao", 400, "porcao_padrao", "A porção deve ser maior que zero."),
        m("chk_ingrediente_quantidade_minima", 400, "quantidade_minima", "A quantidade mínima não pode ser negativa."),
        m("chk_ingrediente_quantidade_estoque", 422, "quantidade", "Estoque insuficiente."),
        m("fk_ingrediente_unidade_medida", 400, "unidade_medida", "Selecione uma unidade de medida cadastrada."),
        m("NN:produto.nome", 400, "nome", "Informe o nome do produto."),
        m("NN:produto.preco_venda", 400, "preco_venda", "Informe o preço de venda."),
        m("chk_produto_preco_venda", 400, "preco_venda", "O preço deve ser maior que zero."),
        m("chk_produto_ingrediente_quantidade", 400, "composicao", "A quantidade utilizada deve ser maior que zero."),
        m("produto_ingrediente_pkey", 400, "composicao", "Este ingrediente já foi adicionado ao produto."),
        m("fk_produto_ingrediente_ingrediente", 400, "composicao", "Ingrediente não encontrado."),
        m("fk_produto_ingrediente_produto", 400, "composicao", "Produto não encontrado."),
        m("NN:cliente.nome", 400, "nome", "Informe o nome."),
        m("NN:cliente.contato", 400, "contato", "Informe o contato."),
        m("NN:cliente.endereco", 400, "endereco", "Informe o endereço."),
        m("fk_pedido_cliente", 400, "cliente_id", "Cliente não encontrado."),
        m("chk_pedido_produto_quantidade", 400, "itens", "A quantidade deve ser maior que zero."),
        m("pedido_produto_pkey", 400, "itens", "Este produto já foi adicionado ao pedido."),
        m("fk_pedido_produto_produto", 400, "itens", "Produto não encontrado."),
        m("chk_movimentacao_tipo", 400, "tipo", "Tipo de movimentação inválido."),
        m("chk_movimentacao_quantidade", 400, "quantidade", "A quantidade deve ser maior que zero."),
        m("chk_movimentacao_data_validade", 400, "data_validade", "A validade só pode ser informada em entradas."),
        m("chk_movimentacao_motivo", 400, "motivo", "Informe o motivo da saída."),
        m("fk_movimentacao_ingrediente", 400, "ingrediente_id", "Ingrediente não encontrado."),
        m("chk_unidade_porcao_padrao", 400, "porcao_padrao", "A porção deve ser maior que zero."),
        m("unidade_medida_porcao_padrao_pkey", 409, "unidade_medida", "Esta unidade já está cadastrada.")
    );

    private static ResponseEntity<Map<String, Object>> resposta(int status, String mensagem, Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", mensagem);
        if (campos != null && !campos.isEmpty()) corpo.put("campos", campos);
        return ResponseEntity.status(status).body(corpo);
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> api(ApiException e) { return resposta(e.status(), e.getMessage(), e.campos()); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException e) {
        if (e.getMostSpecificCause() instanceof PSQLException p && p.getServerErrorMessage() != null) {
            var s = p.getServerErrorMessage();
            String chave = s.getConstraint() != null ? s.getConstraint() : "NN:" + s.getTable() + "." + s.getColumn();
            Msg msg = MAPA.get(chave);
            if (msg != null) return resposta(msg.status(), msg.status() == 422 ? msg.texto() : "Dados inválidos", Map.of(msg.campo(), msg.texto()));
            log.warn("Constraint sem mapeamento: {}", chave);
        }
        log.error("Violação de integridade", e);
        return resposta(500, "Não foi possível concluir a operação.", null);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) { return resposta(400, "Requisição inválida.", null); }
    // Valor da URL com tipo errado (ex.: /api/produtos/abc/preco) ou parâmetro obrigatório ausente: erro do cliente (400), nunca 500.
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<Map<String, Object>> parametroInvalido(Exception e) { return resposta(400, "Requisição inválida.", null); }
    // Se um dia usarem @PreAuthorize, a negação chega aqui (e não no AccessDeniedHandler do filtro): mantém 403 em JSON.
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> semPermissao(AccessDeniedException e) { return resposta(403, "Você não tem permissão para esta ação.", null); }
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, Object>> naoEncontrado(NoResourceFoundException e) { return resposta(404, "Recurso não encontrado.", null); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, Object>> metodo(HttpRequestMethodNotSupportedException e) { return resposta(405, "Método não permitido.", null); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Erro inesperado", e);                          // o stack trace vai para o LOG, nunca para a resposta
        return resposta(500, "Não foi possível concluir a operação.", null);
    }
}
