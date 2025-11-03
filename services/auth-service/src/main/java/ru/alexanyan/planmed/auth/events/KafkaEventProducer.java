package ru.alexanyan.planmed.auth.events;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> template;

    public void send(String topic, String key, Object event) {
        template.getProducerFactory()
                .updateConfigs(Map.of(JsonSerializer.ADD_TYPE_INFO_HEADERS, false));

        var record = new ProducerRecord<>(topic, key, event);
        template.send(record);
    }
}
