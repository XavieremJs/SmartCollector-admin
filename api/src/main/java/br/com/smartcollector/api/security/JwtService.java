package br.com.smartcollector.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey chave;
    private final Duration validade;

    public JwtService(@Value("${security.jwt.secret}") String segredo,
                      @Value("${security.jwt.expiration-minutes}") long minutos) {

        if (segredo == null || segredo.isBlank()) {
            // Sem segredo configurado a aplicacao ainda sobe, mas com uma chave
            // efemera: os tokens deixam de valer a cada restart. Serve para
            // desenvolvimento e evita que um segredo real fique no repositorio.
            this.chave = Jwts.SIG.HS256.key().build();
            log.warn("JWT_SECRET nao definido: usando chave aleatoria. "
                     + "Os tokens serao invalidados no proximo restart.");
        } else {
            this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        }

        this.validade = Duration.ofMinutes(minutos);
    }

    public String gerarToken(String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                   .subject(email)
                   .issuedAt(Date.from(agora))
                   .expiration(Date.from(agora.plus(validade)))
                   .signWith(chave)
                   .compact();
    }

    public Instant expiracaoDe(String token) {
        return extrairClaims(token).getExpiration().toInstant();
    }

    /**
     * Devolve o email do token, ou null se a assinatura for invalida ou o
     * prazo tiver vencido.
     */
    public String emailValidado(String token) {
        try {
            return extrairClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token rejeitado: {}", ex.getMessage());
            return null;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                   .verifyWith(chave)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }
}
