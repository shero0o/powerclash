package at.fhv.spiel_backend.ws;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrateState {
    private String id;
    private int x;
    private int y;
    private int hp;
}
