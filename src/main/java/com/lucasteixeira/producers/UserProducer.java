package com.lucasteixeira.producers;

import com.lucasteixeira.business.dto.EmailDTO;
import com.lucasteixeira.infrastructure.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${mq.queues.emailcadastrousuarios-queue}")
    private String routingKey;

    public void publishMessageUsuarioCadastro(String email, Usuario usuario) {
        var emailDTO = EmailDTO.builder()
                .userId(usuario.getId())
                .emailTo(email)
                .subject("Bem-vindo ao Task Manager! 🚀")
                .text("Olá, " + usuario.getNome() + "!\n\n" +
                "É um prazer ter você conosco! Seu cadastro foi realizado com sucesso.\n\n" +
                "Agora você tem acesso a uma ferramenta poderosa para organizar sua rotina.\n\n" +
                "Bom trabalho,\n" +
                "Equipe Task Manager")
                .build();

        rabbitTemplate.convertAndSend("", routingKey, emailDTO);
    }
}
