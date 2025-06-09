package at.fhv.shop_catalogue.model;

import lombok.Getter;

@Getter
public class ShopItemDTO {

    private Long id;
    private String name;
    private int price;
    private ShopItemType type;

    public ShopItemDTO(Long id, String name, int price, ShopItemType type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.type = type;
    }
}
