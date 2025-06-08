package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "selected")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Selected {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "brawler_id")
    private Long brawlerId;

    @Column(name = "gadget_id")
    private Long gadgetId;

    @Column(name = "level_id")
    private Long levelId;
}
