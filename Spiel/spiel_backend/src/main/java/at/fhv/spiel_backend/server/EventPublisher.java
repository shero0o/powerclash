package at.fhv.spiel_backend.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public String toJson(Object o) {
        return mapper.writeValueAsString(o);
    }
}
