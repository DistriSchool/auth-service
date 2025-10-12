package edu.unifor.br.distrischool.authservice.service;

import edu.unifor.br.distrischool.authservice.dto.UserEvent;
import edu.unifor.br.distrischool.authservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public void publishUserEvent(String eventType, User user) {
        try {
            UserEvent event = UserEvent.builder()
                    .eventType(eventType)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .timestamp(LocalDateTime.now().format(formatter))
                    .build();

            kafkaTemplate.send(eventType, event);
            log.info("Evento publicado no Kafka: {} para usuário: {}", eventType, user.getEmail());
        } catch (Exception e) {
            log.error("Erro ao publicar evento no Kafka: {}", e.getMessage(), e);
        }
    }
}
