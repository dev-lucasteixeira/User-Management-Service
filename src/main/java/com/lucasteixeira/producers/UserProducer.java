package com.lucasteixeira.producers;

import com.lucasteixeira.business.dto.EmailDTO;
import com.lucasteixeira.infrastructure.entity.Usuario;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value(value = "${mq.queues.emailcadastro-queue}")
    private String routingKey;

    public void publishMessageEmail(Usuario usuario){
        var emailDTO = new EmailDTO();
        emailDTO.setUserId(usuario.getId());
        emailDTO.setEmailTo(usuario.getEmail());
        emailDTO.setSubject("Bem-vindo ao Task Manager! 📝");
        emailDTO.setText("Olá, " + usuario.getNome() + "! 🚀\n\n" +
                "Seja bem-vindo(a) ao seu novo Agendador de Tarefas!\n\n" +
                "Agradecemos o seu cadastro. A partir de agora, você tem a ferramenta ideal " +
                "para organizar sua rotina, listar seus compromissos e aumentar sua produtividade.\n\n" +
                "Aproveite a plataforma e comece a planejar seu dia agora mesmo!\n\n" +
                "Atenciosamente,\n" +
                "Equipe Task Manager");

        rabbitTemplate.convertAndSend("",routingKey, emailDTO);

    }
}
