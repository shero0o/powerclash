package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_level")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
@IdClass(PlayerLevelId.class)
public class PlayerLevel {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @Id
    @Column(name = "level_id")
    private Long levelId;
}
