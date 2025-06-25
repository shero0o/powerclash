package at.fhv.shop_catalogue.controller;

import at.fhv.shop_catalogue.model.*;
import at.fhv.shop_catalogue.service.ShopCatalogueService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "localhost:5173")
@RestController
@RequestMapping("/api/shop_catalogue")
@RequiredArgsConstructor
public class ShopCatalogueController {

    private final ShopCatalogueService shopCatalogueService;

    @GetMapping("/items")
    public ResponseEntity<List<ShopItemDTO>> getAllShopItems(@RequestParam String playerId) {
        return ResponseEntity.ok(shopCatalogueService.getAllShopItems(UUID.fromString(playerId)));
    }

    @PostMapping("/brawler/buy")
    public ResponseEntity<Void> buyBrawler(@RequestParam String playerId, @RequestParam Long brawlerId) {
        shopCatalogueService.buyBrawler(UUID.fromString(playerId), brawlerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/gadget/buy")
    public ResponseEntity<Void> buyGadget(@RequestParam String playerId, @RequestParam Long gadgetId) {
        shopCatalogueService.buyGadget(UUID.fromString(playerId), gadgetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/level/buy")
    public ResponseEntity<Void> buyLevel(@RequestParam String playerId, @RequestParam Long levelId) {
        shopCatalogueService.buyLevel(UUID.fromString(playerId), levelId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/purchases")
    public ResponseEntity<List<Purchase>> getPurchases(@RequestParam String playerId) {
        return ResponseEntity.ok(shopCatalogueService.getPurchasesForPlayer(UUID.fromString(playerId)));
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
