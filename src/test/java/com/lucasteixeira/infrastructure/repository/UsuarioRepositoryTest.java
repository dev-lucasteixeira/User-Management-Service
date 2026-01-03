package com.lucasteixeira.infrastructure.repository;

import com.lucasteixeira.infrastructure.entity.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Mock
    EntityManager entityManager;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Should get User from DB when user exists")
    void existsByEmail() {
        // ARRANGE
        String email = "existe@teste.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNome("Lucas");
        usuario.setSenha("1234");
        usuarioRepository.save(usuario); // <--- SALVE ANTES

        // ACT
        boolean exists = usuarioRepository.existsByEmail(email);

        // ASSERT
        assertThat(exists).isTrue(); // Agora não falha na linha 33
    }

    @Test
    @DisplayName("Should not get User from DB when user not exists")
    void notExistsByEmail() {

        String email = "teste@teste.com";

        boolean result = usuarioRepository.existsByEmail(email);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should get User successfully from DB")
    void findByEmail() {
        // 1. Arrange - Crie o objeto completo
        String email = "teste@teste.com";
        Usuario usuario = new Usuario();
        usuario.setNome("Lucas");
        usuario.setEmail(email);
        usuario.setSenha("1234");

        // 2. Act - Salve e GARANTA que o ID foi gerado
        usuarioRepository.save(usuario);

        // 3. Assert - Busque pelo email
        var result = usuarioRepository.findByEmail(email);

        assertThat(result.isPresent()).isTrue(); // Linha 58
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should delete User successfully from DB")
    void deleteByEmail() {

        String email = "teste@teste.com";
        createUser(email);

        usuarioRepository.deleteByEmail(email);

        var result = usuarioRepository.findByEmail(email);

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should delete User Unsuccessful from DB")
    void deleteByEmailSuccess() {
        // ARRANGE
        String email = "deletar@teste.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuarioRepository.save(usuario);

        // ACT
        usuarioRepository.deleteByEmail(email);
        var result = usuarioRepository.findByEmail(email);

        // ASSERT
        assertThat(result.isEmpty()).isTrue(); // Verifica se sumiu
    }

    private Usuario createUser(String email) {
        Usuario newUser = new Usuario();
        newUser.setNome("Lucas");
        newUser.setEmail(email);
        newUser.setSenha("123456");

        this.entityManager.persist(newUser);
        return newUser;
    }
}