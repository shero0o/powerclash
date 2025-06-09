package at.fhv.shop_catalogue.controller;

import at.fhv.shop_catalogue.model.*;
import at.fhv.shop_catalogue.service.ShopCatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop_catalogue")
@RequiredArgsConstructor
public class ShopCatalogueController {

    private final ShopCatalogueService shopCatalogueService;

    @GetMapping("/items")
    public List<ShopItemDTO> getAllShopItems() {
        return shopCatalogueService.getAllShopItems();
    }

    @PostMapping("/brawler/{brawlerId}/buy")
    public void buyBrawler(@RequestParam String playerId, @PathVariable Long brawlerId) {
        shopCatalogueService.buyBrawler(playerId, brawlerId);
    }

    @PostMapping("/gadget/{gadgetId}/buy")
    public void buyGadget(@RequestParam String playerId, @PathVariable Long gadgetId) {
        shopCatalogueService.buyGadget(playerId, gadgetId);
    }

    @PostMapping("/level/{levelId}/buy")
    public void buyLevel(@RequestParam String playerId, @PathVariable Long levelId) {
        shopCatalogueService.buyLevel(playerId, levelId);
    }

    @GetMapping("/player/{playerId}/purchases")
    public List<Purchase> getPurchases(@PathVariable String playerId) {
        return shopCatalogueService.getPurchasesForPlayer(playerId);
    }

    @GetMapping("/brawlers")
    public ResponseEntity<List<Brawler>> getAllBrawlers() {
        return ResponseEntity.ok(shopCatalogueService.getAllBrawlers());
    }

    @GetMapping("/gadgets")
    public ResponseEntity<List<Gadget>> getAllGadgets() {
        return ResponseEntity.ok(shopCatalogueService.getAllGadgets());
    }

    @GetMapping("/level")
    public ResponseEntity<List<Level>> getAllLevel() {
        return ResponseEntity.ok(shopCatalogueService.getAllLevels());
    }
}
