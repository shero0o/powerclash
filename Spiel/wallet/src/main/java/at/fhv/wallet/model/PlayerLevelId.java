package at.fhv.wallet.model;

import lombok.*;

import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PlayerLevelId implements Serializable {
    private Long playerId;
    private Long levelId;
}
