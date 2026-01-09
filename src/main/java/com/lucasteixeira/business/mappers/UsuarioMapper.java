package com.lucasteixeira.business.mappers;

import com.lucasteixeira.business.dto.EnderecoDTO;
import com.lucasteixeira.business.dto.TelefoneDTO;
import com.lucasteixeira.business.dto.UsuarioDTO;
import com.lucasteixeira.infrastructure.entity.Endereco;
import com.lucasteixeira.infrastructure.entity.Telefone;
import com.lucasteixeira.infrastructure.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    // Mapeamento de campos com nomes diferentes entre DTO e Entity
    @Mapping(target = "enderecos", source = "endereco")
    @Mapping(target = "telefones", source = "telefone")
    Usuario paraUsuario(UsuarioDTO usuarioDTO);

    @Mapping(target = "endereco", source = "enderecos")
    @Mapping(target = "telefone", source = "telefones")
    UsuarioDTO paraUsuarioDTO(Usuario usuario);

    // Mapeamentos individuais (automaticamente usados nas listas)
    Endereco paraEndereco(EnderecoDTO enderecoDTO);
    EnderecoDTO paraEnderecoDTO(Endereco endereco);

    Telefone paraTelefone(TelefoneDTO telefoneDTO);
    TelefoneDTO paraTelefoneDTO(Telefone telefone);

    // Listas (O MapStruct implementa o loop por você)
    List<Endereco> paraListaEndereco(List<EnderecoDTO> enderecosDTO);
    List<Telefone> paraListaTelefones(List<TelefoneDTO> telefonesDTO);

    // Atualização (Update) - O BeanMapping ignora nulos se configurado
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Mantém o ID da entidade original
    void updateUsuario(UsuarioDTO dto, @MappingTarget Usuario entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEndereco(EnderecoDTO dto, @MappingTarget Endereco entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateTelefone(TelefoneDTO dto, @MappingTarget Telefone entity);

    // Métodos específicos com parâmetros extras (Uso de @Context ou lógica customizada)
    @Mapping(target = "usuario_Id", source = "idUsuario")
    Endereco paraEnderecoEntity(EnderecoDTO dto, Long idUsuario);

    @Mapping(target = "usuario_Id", source = "idUsuario")
    Telefone paraTelefoneEntity(TelefoneDTO dto, Long idUsuario);
}
