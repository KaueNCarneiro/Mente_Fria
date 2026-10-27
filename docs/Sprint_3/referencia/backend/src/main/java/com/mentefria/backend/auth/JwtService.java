package com.mentefria.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private final SecretKey chave;
    private final long validadeMs;

    public JwtService(@Value("${app.jwt.secret}") String segredo, @Value("${app.jwt.validade-horas:2}") long horas) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres.");
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.validadeMs = horas * 60 * 60 * 1000;
    }
    public String gerarToken(int usuarioId, String perfil) {
        Date agora = new Date();
        return Jwts.builder().subject(String.valueOf(usuarioId)).claim("perfil", perfil)
                .issuedAt(agora).expiration(new Date(agora.getTime() + validadeMs)).signWith(chave).compact();
    }
    public Claims validarEExtrair(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
    }
}
