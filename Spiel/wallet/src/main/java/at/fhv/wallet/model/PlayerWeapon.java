package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_brawler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@IdClass(PlayerBrawlerId.class)
public class PlayerWeapon {
    @Id
    @Column(name = "player_id")
    private Long playerId;

    @Id
    @Column(name = "weapon_id")
    private Long brawlerId;
}
