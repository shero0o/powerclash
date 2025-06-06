package at.fhv.shop_catalogue.controller;

import at.fhv.shop_catalogue.model.Brawler;
import at.fhv.shop_catalogue.model.Gadget;
import at.fhv.shop_catalogue.model.PlayerCoins;
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

    // -----------------------
    // Brawler-Endpunkte
    // -----------------------

    @GetMapping("/brawlers")
    public ResponseEntity<List<Brawler>> getAllBrawlers() {
        return ResponseEntity.ok(shopCatalogueService.getAllBrawlers());
    }

    @GetMapping("/brawlers/player/{playerId}")
    public ResponseEntity<List<Brawler>> getBrawlersOwned(@PathVariable Long playerId) {
        return ResponseEntity.ok(shopCatalogueService.getAllBrawlersOwnedByPlayer(playerId));
    }

    @GetMapping("/brawlers/player/{playerId}/notowned")
    public ResponseEntity<List<Brawler>> getBrawlersNotOwned(@PathVariable Long playerId) {
        return ResponseEntity.ok(shopCatalogueService.getAllBrawlersNotOwnedByPlayer(playerId));
    }

    @PostMapping("/brawlers/buy")
    public ResponseEntity<String> buyBrawler(
            @RequestParam Long playerId,
            @RequestParam Long brawlerId) {
        shopCatalogueService.buyBrawler(playerId, brawlerId);
        return ResponseEntity.ok("Brawler gekauft");
    }

    // -----------------------
    // Gadget-Endpunkte
    // -----------------------

    @GetMapping("/gadgets")
    public ResponseEntity<List<Gadget>> getAllGadgets() {
        return ResponseEntity.ok(shopCatalogueService.getAllGadgets());
    }

    @GetMapping("/gadgets/player/{playerId}")
    public ResponseEntity<List<Gadget>> getGadgetsOwned(@PathVariable Long playerId) {
        return ResponseEntity.ok(shopCatalogueService.getAllGadgetsOwnedByPlayer(playerId));
    }

    @GetMapping("/gadgets/player/{playerId}/notowned")
    public ResponseEntity<List<Gadget>> getGadgetsNotOwned(@PathVariable Long playerId) {
        return ResponseEntity.ok(shopCatalogueService.getAllGadgetsNotOwnedByPlayer(playerId));
    }

    @PostMapping("/gadgets/buy")
    public ResponseEntity<String> buyGadget(
            @RequestParam Long playerId,
            @RequestParam Long gadgetId) {
        shopCatalogueService.buyGadget(playerId, gadgetId);
        return ResponseEntity.ok("Gadget gekauft");
    }

    // -----------------------
    // Coins-Endpunkte
    // -----------------------

    @GetMapping("/coins/{playerId}")
    public ResponseEntity<Integer> getCoinsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(shopCatalogueService.getCoinsForPlayer(playerId));
    }

    @PostMapping("/coins/{playerId}/add")
    public ResponseEntity<String> addCoins(
            @PathVariable Long playerId,
            @RequestParam Integer amount) {
        shopCatalogueService.addCoinsForPlayer(playerId, amount);
        return ResponseEntity.ok("Coins hinzugefügt");
    }

    @GetMapping("/coins/all")
    public ResponseEntity<List<PlayerCoins>> getAllCoins() {
        return ResponseEntity.ok(shopCatalogueService.getAllCoins());
    }
}
