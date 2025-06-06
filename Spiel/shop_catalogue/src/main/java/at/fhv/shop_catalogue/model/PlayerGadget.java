package at.fhv.shop_catalogue.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_gadget")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
@IdClass(PlayerGadgetId.class)
public class PlayerGadget {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @Id
    @Column(name = "gadget_id")
    private Long gadgetId;
}
