package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_coins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class PlayerCoins {
    @Id
    @Column(name = "player_id")
    private Long playerId;

    @Column(nullable = false)
    private Integer coins;
}
