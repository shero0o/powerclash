package at.fhv.spiel_service.dto;

import lombok.Data;

@Data
public class MoveRequestDTO {
    private String roomId;
    private String playerId;
    private float dirX;
    private float dirY;
    private float angle;
}
