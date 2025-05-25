package at.fhv.spiel_backend.DTO;


import at.fhv.spiel_backend.model.ProjectileType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JoinRequestDTO {
    String playerId;
    private String brawlerId;
    private String levelId;
    private ProjectileType chosenWeapon;
    private String playerName;

}
