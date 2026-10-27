package com.mentefria.backend.usuario;

/** Formatos de entrada e saída de usuários (Contrato §2.1 e §2.3). Em snake_case pela configuração global. */
public final class UsuarioDtos {
    private UsuarioDtos() {}
    public record NovoUsuario(String nome, String email, String senha) {}
    public record UsuarioCriado(int id, String nome, String email, String perfil) {}
    public record UsuarioResumo(int id, String nome, String email, boolean ativo) {}
}
