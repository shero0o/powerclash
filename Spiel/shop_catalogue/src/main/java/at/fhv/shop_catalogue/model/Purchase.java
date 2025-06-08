package at.fhv.shop_catalogue.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String playerId;

    @Enumerated(EnumType.STRING)
    private ShopItemType itemType;

    private Long itemId;

    public Purchase() {}

    public Purchase(String playerId, ShopItemType itemType, Long itemId) {
        this.playerId = playerId;
        this.itemType = itemType;
        this.itemId = itemId;
    }
}
