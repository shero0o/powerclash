package at.fhv.spiel_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttackRequestDTO {
    private String roomId;
    private String playerId;
    private float dirX;
    private float dirY;
    private float angle;
}
