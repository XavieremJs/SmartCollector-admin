package br.com.smartcollector.api.service;

import br.com.smartcollector.api.dto.UsuarioRequest;
import br.com.smartcollector.api.exception.RegraNegocioException;
import br.com.smartcollector.api.model.Usuario;
import br.com.smartcollector.api.model.UsuarioRole;
import br.com.smartcollector.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;

    @BeforeEach
    void preparar() {
        usuarioService = new UsuarioService(usuarioRepository, new BCryptPasswordEncoder());
    }

    private UsuarioRequest requisicao() {
        return new UsuarioRequest("Ana", "ana@smartcollector.br", "senha-secreta", UsuarioRole.USER);
    }

    @Test
    void naoDeveGravarSenhaEmTextoPuro() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));

        usuarioService.criar(requisicao());

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(capturado.capture());

        assertThat(capturado.getValue().getSenha())
                .isNotEqualTo("senha-secreta")
                .startsWith("$2a$");
    }

    @Test
    void deveRecusarEmailDuplicado() {
        when(usuarioRepository.existsByEmail("ana@smartcollector.br")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(requisicao()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ana@smartcollector.br");

        verify(usuarioRepository, never()).save(any());
    }
}
