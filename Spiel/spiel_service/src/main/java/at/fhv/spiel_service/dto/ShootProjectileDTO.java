package at.fhv.spiel_service.dto;

import at.fhv.spiel_service.entities.ProjectileType;
import at.fhv.spiel_service.entities.Position;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShootProjectileDTO {

    private String roomId;
    private String playerId;
    private Position direction;
    private ProjectileType projectileType;

}
