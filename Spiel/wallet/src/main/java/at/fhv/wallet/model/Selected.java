package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "selected")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Selected {

    @Id
    @Column(name = "selected_id")
    @JoinColumn(name = "selected_id")
    private UUID selectedId;

    @Column(name = "brawler_id")
    private Long brawlerId;

    @Column(name = "gadget_id")
    private Long gadgetId;

    @Column(name = "level_id")
    private Long levelId;

    @Column(name = "weapon_id")
    private Long weaponId;

    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

}
