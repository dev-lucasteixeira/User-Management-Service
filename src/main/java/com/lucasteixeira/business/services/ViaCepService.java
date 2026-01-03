package com.lucasteixeira.business.services;

import com.lucasteixeira.infrastructure.client.ViaCepClient;
import com.lucasteixeira.business.dto.ViaCepDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ViaCepService {

    private final ViaCepClient viaCepClient;

    public ViaCepDTO buscaDadosDeEndereco(String cep){
        try{
            return viaCepClient.buscaDadosDeEndereco(removeCaracteresEspeciais(cep));
        }
        catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Erro: ", e);
        }
        catch (Exception e){
            throw new RuntimeException("Erro ao buscar dados do cep: ", e);
        }
    }

    private String removeCaracteresEspeciais(String cep){
        String cepFormatado = cep.replace(" ", "").replace("-", "");

        if(!cepFormatado.matches("\\d+") || !Objects.equals(cepFormatado.length(), 8)){
            throw new IllegalArgumentException("O cep contém caracteres inválidos, favor verificar!");
        }

        return cepFormatado;
    }
}
