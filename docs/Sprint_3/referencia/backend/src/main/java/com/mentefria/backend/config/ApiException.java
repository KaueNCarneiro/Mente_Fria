package com.mentefria.backend.config;

import java.util.Map;

public class ApiException extends RuntimeException {
    private final int status;
    private final Map<String, String> campos;
    public ApiException(int status, String mensagem, Map<String, String> campos) {
        super(mensagem); this.status = status; this.campos = campos;
    }
    public static ApiException campo(int status, String campo, String texto) {
        return new ApiException(status, "Dados inválidos", Map.of(campo, texto));
    }
    public int status() { return status; }
    public Map<String, String> campos() { return campos; }
}
