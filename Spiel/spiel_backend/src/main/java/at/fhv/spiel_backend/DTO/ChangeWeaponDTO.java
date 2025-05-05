package at.fhv.spiel_backend.DTO;

import at.fhv.spiel_backend.model.ProjectileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeWeaponDTO {

    private String roomId;
    private String playerId;
    private ProjectileType projectileType;
}
