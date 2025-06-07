package at.fhv.wallet.model;

import lombok.*;

import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PlayerWeaponId implements Serializable {
    private Long playerId;
    private Long weaponId;
}
