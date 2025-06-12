package at.fhv.spiel_service.dto;

import at.fhv.spiel_service.domain.ProjectileType;
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
