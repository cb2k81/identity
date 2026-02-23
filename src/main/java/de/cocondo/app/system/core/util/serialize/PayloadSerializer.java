package de.cocondo.app.system.core.util.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PayloadSerializer {

    private final ObjectMapper objectMapper;

    public PayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> String serializePayload(T payload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(payload);
    }

    public <T> T deserializePayload(String payload, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(payload, valueType);
    }
}
