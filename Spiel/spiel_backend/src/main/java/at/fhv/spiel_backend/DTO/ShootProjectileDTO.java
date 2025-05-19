package at.fhv.spiel_backend.DTO;

import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.model.ProjectileType;
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
