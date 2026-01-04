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
import com.lucasteixeira.infrastructure.exceptions.UnauhthorizedException;
import com.lucasteixeira.infrastructure.repository.EnderecoRepository;
import com.lucasteixeira.infrastructure.repository.TelefoneRepository;
import com.lucasteixeira.infrastructure.repository.UsuarioRepository;
import com.lucasteixeira.infrastructure.security.JwtUtil;
import com.lucasteixeira.producers.UserProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;
    private final AuthenticationManager authenticationManager;
    private final UserProducer userProducer;


    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        usuario = usuarioRepository.save(usuario);
        userProducer.publishMessageEmail(usuario);
        return usuarioConverter.paraUsuarioDTO(usuario);
    }

    public String autenticarUsuario(UsuarioDTO usuarioDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuarioDTO.getEmail(),
                            usuarioDTO.getSenha())
            );
            return "Bearer " + jwtUtil.generateToken(authentication.getName());

        } catch (BadCredentialsException | UsernameNotFoundException | AuthorizationDeniedException e) {
            throw new UnauhthorizedException("Usuário ou senha inválidos: ", e.getCause());
        }
    }


    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if(existe){
                throw new ConflictException("Email já cadastrado" + email);
            }
        }catch (ConflictException e ){
            throw new ConflictException("Email já cadrastado" + e.getCause());
        }
    }


    //Ele chama o método la na repository
    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }


    @Cacheable(value = "users", key = "#email")
    public UsuarioDTO buscaUsuarioPorEmail(String email){
        try {
            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email)
                            .orElseThrow(
                    () -> new ResourceNotFoundException("Email não encontrado" + email)
                            )
            );
        }catch (ResourceNotFoundException e ){
            throw new ResourceNotFoundException("Email não encontrado" + email);
        }
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
        // 1. Busca o endereço existente no banco
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
