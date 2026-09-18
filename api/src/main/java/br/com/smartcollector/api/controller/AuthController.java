package br.com.smartcollector.api.controller;

import br.com.smartcollector.api.dto.LoginRequest;
import br.com.smartcollector.api.dto.LoginResponse;
import br.com.smartcollector.api.model.Usuario;
import br.com.smartcollector.api.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest requisicao) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requisicao.email(), requisicao.senha()));

        Usuario usuario = (Usuario) autenticacao.getPrincipal();
        String token = jwtService.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(new LoginResponse(
                token, usuario.getNome(), usuario.getFuncao(), jwtService.expiracaoDe(token)));
    }
}
