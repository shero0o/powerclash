package at.fhv.shop_catalogue.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    private ShopItemType itemType;

    private Long itemId;

    public Purchase() {}

    public Purchase(UUID playerId, ShopItemType itemType, Long itemId) {
        this.playerId = playerId;
        this.itemType = itemType;
        this.itemId = itemId;
    }

}
