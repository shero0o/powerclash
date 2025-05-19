package at.fhv.spiel_backend.ws;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {
    private String type;
    private Object data;
}
