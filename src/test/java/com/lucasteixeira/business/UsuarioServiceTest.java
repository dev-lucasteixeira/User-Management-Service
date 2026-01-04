package com.lucasteixeira.business;

import com.lucasteixeira.business.converter.UsuarioConverter;
import com.lucasteixeira.business.dto.EnderecoDTO;
import com.lucasteixeira.business.dto.TelefoneDTO;
import com.lucasteixeira.business.dto.UsuarioDTO;
import com.lucasteixeira.business.services.UsuarioService;
import com.lucasteixeira.infrastructure.entity.Endereco;
import com.lucasteixeira.infrastructure.entity.Telefone;
import com.lucasteixeira.infrastructure.entity.Usuario;
import com.lucasteixeira.infrastructure.repository.EnderecoRepository;
import com.lucasteixeira.infrastructure.repository.TelefoneRepository;
import com.lucasteixeira.infrastructure.repository.UsuarioRepository;
import com.lucasteixeira.infrastructure.security.JwtUtil;
import com.lucasteixeira.producers.UserProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioConverter usuarioConverter;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private TelefoneRepository telefoneRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserProducer userProducer;

    @InjectMocks
    private UsuarioService usuarioService;


    @Test
    @DisplayName("Should save user in DB")
    void salvaUsuario() {
        UsuarioDTO requestDTO = new UsuarioDTO("Lucas", "teste@teste.com", "1234", null, null);
        Usuario usuarioEntity = new Usuario();
        usuarioEntity.setEmail("teste@teste.com");

        UsuarioDTO responseDTO = new UsuarioDTO("Lucas", "teste@teste.com", null, null, null);

        when(passwordEncoder.encode(anyString())).thenReturn("senha_encriptada");
        when(usuarioConverter.paraUsuario(any(UsuarioDTO.class))).thenReturn(usuarioEntity);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioEntity);

        when(usuarioConverter.paraUsuarioDTO(any(Usuario.class))).thenReturn(responseDTO);
        UsuarioDTO result = usuarioService.salvaUsuario(requestDTO);
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(requestDTO.getEmail());
        verify(userProducer, times(1)).publishMessageEmail(any());
    }

    @Test
    @DisplayName("Should return token when login success")
    void autenticarUsuario() {

        UsuarioDTO loginRequest = new UsuarioDTO("Lucas", "teste@teste.com", "1234", null, null);

        Authentication authMock = mock(Authentication.class);
        when(authMock.getName()).thenReturn("teste@teste.com");
        when(authenticationManager.authenticate(any())).thenReturn(authMock);

        String token = "token-gerado";
        when(jwtUtil.generateToken(loginRequest.getEmail())).thenReturn(token);

        String tokenResult = usuarioService.autenticarUsuario(loginRequest);

        assertThat(tokenResult).isNotNull();
        assertThat(tokenResult).isEqualTo("Bearer " + token);

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateToken(loginRequest.getEmail());

    }

    @Test
    @DisplayName("Should return false when email already exists")
    void emailExiste() {
        UsuarioDTO requestDTO = new UsuarioDTO("Lucas", "teste@teste.com", "1234", null, null);

        boolean result = usuarioService.verificaEmailExistente(requestDTO.getEmail());

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when email does not exists")
    void verificaEmailNaoExistente() {
        UsuarioDTO requestDTO = new UsuarioDTO("Lucas", "teste@teste.com", "1234", null, null);

        assertThat(usuarioService.verificaEmailExistente(requestDTO.getEmail())).isFalse();
    }


    @Test
    @DisplayName("Should update user in DB")
    void atualizaUsuario() {

        String email = "teste@teste.com";
        UsuarioDTO requestDTO = new UsuarioDTO("Lucas Novo", email, "1234", null, null);

        Usuario usuarioEntity = new Usuario();
        usuarioEntity.setEmail(email);

        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuarioEntity));

        when(usuarioRepository.save(any())).thenReturn(usuarioEntity);
        when(usuarioConverter.paraUsuarioDTO(any())).thenReturn(requestDTO);

        UsuarioDTO result = usuarioService.atualizaUsuario(email, requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);

    }

    @Test
    @DisplayName("Should update the Adress's data of User")
    void atualizaEndereco() {

        String email = "teste@teste.com";
        Long idEndereco = 1L;
        EnderecoDTO requestDTO = new EnderecoDTO();
        requestDTO.setRua("Rua Teste");
        requestDTO.setNumero(1000L);
        requestDTO.setCidade("Teste");
        requestDTO.setEstado("teste");
        requestDTO.setCep("12345-678");

        Endereco enderecoEntity = new Endereco();
        enderecoEntity.setId(idEndereco);
        enderecoEntity.setRua("Rua Antiga");

        Endereco enderecoAtualizado = new Endereco();
        enderecoAtualizado.setId(idEndereco);
        enderecoAtualizado.setRua(requestDTO.getRua());
        enderecoAtualizado.setNumero(requestDTO.getNumero());

        when(enderecoRepository.findById(idEndereco)).thenReturn(Optional.of(enderecoEntity));
        when(usuarioConverter.updateEndereco(requestDTO, enderecoEntity)).thenReturn(enderecoAtualizado);
        when(enderecoRepository.save(any())).thenReturn(enderecoAtualizado);
        when(usuarioConverter.paraEnderecoDTO(any())).thenReturn(requestDTO);

        EnderecoDTO result = usuarioService.atualizaEndereco(idEndereco, requestDTO, email);

        assertThat(result).isNotNull();
        assertThat(result.getRua()).isEqualTo(requestDTO.getRua());
        assertThat(result.getNumero()).isEqualTo(1000L);

        verify(enderecoRepository, times(1)).findById(idEndereco);
        verify(enderecoRepository, times(1)).save(any(Endereco.class));
    }

    @Test
    @DisplayName("Should update the phone's data of User")
    void atualizaTelefone() {
        String email = "teste@teste.com";
        Long idTelefone = 1L;
        TelefoneDTO requestDTO = new TelefoneDTO();
        requestDTO.setNumero("123456789");
        requestDTO.setDdd("11");

        Telefone telefoneEntity = new Telefone();
        telefoneEntity.setId(idTelefone);
        telefoneEntity.setNumero("123456789");
        telefoneEntity.setDdd("11");

        Telefone telefoneAtualizado = new Telefone();
        telefoneAtualizado.setId(idTelefone);
        telefoneAtualizado.setNumero(requestDTO.getNumero());
        telefoneAtualizado.setDdd(requestDTO.getDdd());

        when(telefoneRepository.findById(idTelefone)).thenReturn(Optional.of(telefoneEntity));
        when(usuarioConverter.updateTelefone(requestDTO, telefoneEntity)).thenReturn(telefoneAtualizado);
        when(telefoneRepository.save(any())).thenReturn(telefoneAtualizado);
        when(usuarioConverter.paraTelefoneDTO(any())).thenReturn(requestDTO);

        TelefoneDTO result = usuarioService.atualizaTelefone(idTelefone, requestDTO, email);

        assertThat(result).isNotNull();
        assertThat(result.getNumero()).isEqualTo(requestDTO.getNumero());
        assertThat(result.getDdd()).isEqualTo(requestDTO.getDdd());
    }

    @Test
    @DisplayName("Should save Address's user in DB")
    void cadastraEndereco() {

        Long idUsuario = 1L;
        String email = "teste@teste.com";
        Usuario userEntity = new Usuario(idUsuario,null, email, null, null, null);

        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setRua("Rua teste");
        enderecoDTO.setNumero(1000L);
        enderecoDTO.setEstado("teste");
        enderecoDTO.setCidade("teste");
        enderecoDTO.setCep("12345-678");

        Endereco enderecoEntity = new Endereco();
        enderecoEntity.setId(idUsuario);
        enderecoEntity.setRua(enderecoDTO.getRua());
        enderecoEntity.setNumero(enderecoDTO.getNumero());
        enderecoEntity.setEstado(enderecoDTO.getEstado());
        enderecoEntity.setCidade(enderecoDTO.getCidade());
        enderecoEntity.setCep(enderecoDTO.getCep());

        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        when(usuarioConverter.paraEnderecoDTO(any())).thenReturn(enderecoDTO);

        assertThat(usuarioService.cadastraEndereco(email, enderecoDTO)).isNotNull();




    }

    @Test
    @DisplayName("Should save Phone's user in DB")
    void cadastraTelefone() {
        Long idUsuario = 1L;
        String email = "teste@teste.com";
        Usuario userEntity = new Usuario(idUsuario,null, email, null, null, null);

        TelefoneDTO telefoneDTO = new TelefoneDTO();
        telefoneDTO.setNumero("123456789");
        telefoneDTO.setDdd("11");

        Telefone telefoneEntity = new Telefone();
        telefoneEntity.setId(idUsuario);
        telefoneEntity.setNumero(telefoneDTO.getNumero());
        telefoneEntity.setDdd(telefoneDTO.getDdd());

        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        when(usuarioConverter.paraTelefoneDTO(any())).thenReturn(telefoneDTO);

        assertThat(usuarioService.cadastraTelefone(email, telefoneDTO)).isNotNull();


    }

}