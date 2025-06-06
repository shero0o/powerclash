package at.fhv.shop_catalogue.model;

import java.io.Serializable;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PlayerGadgetId implements Serializable {
    private Long playerId;
    private Long gadgetId;
}
