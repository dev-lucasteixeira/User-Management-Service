package com.lucasteixeira.business.services;


import com.lucasteixeira.business.converter.UsuarioConverter;
import com.lucasteixeira.business.dto.EnderecoDTO;
import com.lucasteixeira.business.dto.TelefoneDTO;
import com.lucasteixeira.business.dto.UsuarioDTO;
import com.lucasteixeira.infrastructure.entity.Endereco;
import com.lucasteixeira.infrastructure.entity.Telefone;
import com.lucasteixeira.infrastructure.entity.Usuario;
import com.lucasteixeira.infrastructure.exceptions.ConflictException;
import com.lucasteixeira.infrastructure.exceptions.ResourceNotFoundException;
import com.lucasteixeira.infrastructure.repository.EnderecoRepository;
import com.lucasteixeira.infrastructure.repository.TelefoneRepository;
import com.lucasteixeira.infrastructure.repository.UsuarioRepository;
import com.lucasteixeira.producers.UserProducer;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;
    private final UserProducer userProducer;


    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8085")
                .realm("task-scheduler") // Seu realm
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId("task-scheduler-client")
                .clientSecret("sayD7JaC4ycNqNm7MB7LBuQFwSU8cPri")
                .build();

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(usuarioDTO.getEmail());
        user.setEmail(usuarioDTO.getEmail());
        user.setFirstName(usuarioDTO.getNome());
        user.setLastName("User");
        user.setEmailVerified(true);


        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(usuarioDTO.getSenha());
        user.setCredentials(Collections.singletonList(passwordCred));

        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        usuario = usuarioRepository.save(usuario);
        userProducer.publishMessageEmail(usuario);



        Response response = keycloak.realm("task-scheduler").users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Erro ao criar usuário no Keycloak: " + response.getStatus());
        }
        return usuarioConverter.paraUsuarioDTO(usuario);
    }

    public void emailExiste(String email){
        if (verificaEmailExistente(email)) {
            throw new ConflictException("Email já cadastrado: " + email);
        }
    }


    //Ele chama o método la na repository
    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }


    @Cacheable(value = "users", key = "#email")
    public UsuarioDTO buscaUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email)
                .map(usuarioConverter::paraUsuarioDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o email: " + email));
    }

    @CacheEvict(value = "users", key = "#email")
    public void deletaUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    @CachePut(value = "users", key = "#email")
    public UsuarioDTO atualizaUsuario(String email, UsuarioDTO dto){
        //busca o usuario pelo token para tirar a obrigatoriaedade do email


        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        //busca os dados usuario no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow( () ->
                new ResourceNotFoundException("Email não encontrado" + email));

        //mesclou os dados que recebemos na requisição com os dados do banco de dados
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);


        //salvou os dados do usuario convertidos e depois pegoui o retorno e converteu para usuarioDTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    @CacheEvict(value = "users", key = "#email")
    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO, String email){
        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idEndereco));
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);
        Endereco enderecoSalvo = enderecoRepository.save(endereco);
        return usuarioConverter.paraEnderecoDTO(enderecoSalvo);
    }

    @CacheEvict(value = "users", key = "#email")
    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO dto, String email) {
        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);
        Telefone telefoneSalvo = telefoneRepository.save(telefone);

        return usuarioConverter.paraTelefoneDTO(telefoneSalvo);
    }

    @CacheEvict(value = "users", key = "#email")
    public EnderecoDTO cadastraEndereco(String email, EnderecoDTO dto){
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não encontrado" + email));

        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto, usuario.getId());
        Endereco enderecoEntity = enderecoRepository.save(endereco);
        return usuarioConverter.paraEnderecoDTO(enderecoEntity);
    }

    @CacheEvict(value = "users", key = "#email")
    public TelefoneDTO cadastraTelefone(String email, TelefoneDTO dto){
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não encontrado" + email));

        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());
        Telefone enderecoEntity = telefoneRepository.save(telefone);
        return usuarioConverter.paraTelefoneDTO(enderecoEntity);
    }
}
