package com.mentefria.backend.usuario;

/** Linha da tabela usuario. Nunca sai em resposta HTTP (tem o hash da senha). */
public record Usuario(int id, String nome, String email, String senhaHash, String perfil, boolean ativo) {}
