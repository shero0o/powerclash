package at.fhv.wallet.controller;

import at.fhv.wallet.model.Brawler;
import at.fhv.wallet.model.Gadget;
import at.fhv.wallet.model.PlayerCoins;
import at.fhv.wallet.service.WalletService;
import at.fhv.wallet.model.Level;
import at.fhv.wallet.model.Selected;
import at.fhv.wallet.model.Weapon;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // -----------------------
    // Brawler-Endpunkte
    // -----------------------

    @GetMapping("/brawlers")
    public ResponseEntity<List<Brawler>> getAllBrawlers() {
        return ResponseEntity.ok(walletService.getAllBrawlers());
    }

    @GetMapping("/brawlers/player")
    public ResponseEntity<List<Brawler>> getBrawlersOwned(@RequestParam Long playerId) {
        return ResponseEntity.ok(walletService.getAllBrawlersOwnedByPlayer(playerId));
    }

    @GetMapping("/brawlers/player/notOwned")
    public ResponseEntity<List<Brawler>> getBrawlersNotOwned(@RequestParam Long playerId) {
        return ResponseEntity.ok(walletService.getAllBrawlersNotOwnedByPlayer(playerId));
    }

    @GetMapping("/brawlers/player/owns")
    public ResponseEntity<Boolean> isBrawlerOwned(
            @RequestParam Long playerId,
            @RequestParam Long BrawlerId) {
        return ResponseEntity.ok(walletService.isBrawlerOwnedByPlayer(playerId, BrawlerId));
    }

    @PostMapping("/brawlers/buy")
    public ResponseEntity<String> buyBrawler(
            @RequestParam Long playerId,
            @RequestParam Long brawlerId) {
            try {
                walletService.buyBrawler(playerId, brawlerId);
                return ResponseEntity.ok("Brawler gekauft");
            } catch (IllegalArgumentException | IllegalStateException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
    }

    // -----------------------
    // Gadget-Endpunkte
    // -----------------------

    @GetMapping("/gadgets")
    public ResponseEntity<List<Gadget>> getAllGadgets() {
        return ResponseEntity.ok(walletService.getAllGadgets());
    }

    @GetMapping("/gadgets/player")
    public ResponseEntity<List<Gadget>> getGadgetsOwned(@RequestParam Long playerId) {
        return ResponseEntity.ok(walletService.getAllGadgetsOwnedByPlayer(playerId));
    }

    @GetMapping("/gadgets/player/notOwned")
    public ResponseEntity<List<Gadget>> getGadgetsNotOwned(@RequestParam Long playerId) {
        return ResponseEntity.ok(walletService.getAllGadgetsNotOwnedByPlayer(playerId));
    }

    @GetMapping("/gadgets/player/owns")
    public ResponseEntity<Boolean> isGadgetOwned(
            @RequestParam Long playerId,
            @RequestParam Long gadgetId) {
        return ResponseEntity.ok(walletService.isBrawlerOwnedByPlayer(playerId, gadgetId));
    }

    @PostMapping("/gadgets/buy")
    public ResponseEntity<String> buyGadget(
            @RequestParam Long playerId,
            @RequestParam Long gadgetId) {
        walletService.buyGadget(playerId, gadgetId);
        return ResponseEntity.ok("Gadget gekauft");
    }

    // -----------------------
    // Coins-Endpunkte
    // -----------------------

    @GetMapping("/coins")
    public ResponseEntity<Integer> getCoinsByPlayer(@RequestParam Long playerId) {
        return ResponseEntity.ok(walletService.getCoinsForPlayer(playerId));
    }

    @PostMapping("/coins/add")
    public ResponseEntity<String> addCoins(
            @RequestParam Long playerId,
            @RequestParam Integer amount) {
        walletService.addCoinsForPlayer(playerId, amount);
        return ResponseEntity.ok("Coins hinzugefügt");
    }

    @GetMapping("/coins/all")
    public ResponseEntity<List<PlayerCoins>> getAllCoins() {
        return ResponseEntity.ok(walletService.getAllCoins());
    }


    // -----------------------
    // Level-Endpunkte
    // -----------------------

    @GetMapping("/levels")
    public ResponseEntity<List<Level>> getAllLevels() {
        return ResponseEntity.ok(walletService.getAllLevels());
    }

    @GetMapping("/levels/player")
    public ResponseEntity<List<Level>> getLevelsOwned(@RequestParam Long playerId) {
        return ResponseEntity.ok(walletService.getAllLevelsOwnedByPlayer(playerId));
    }

    @GetMapping("/levels/player/notOwned")
    public ResponseEntity<List<Level>> getLevelsNotOwned(@RequestParam Long playerId) {
        return ResponseEntity.ok(walletService.getAllLevelsNotOwnedByPlayer(playerId));
    }

    @GetMapping("/levels/player/owns")
    public ResponseEntity<Boolean> isLevelOwned(
            @RequestParam Long playerId,
            @RequestParam Long levelId) {
        return ResponseEntity.ok(walletService.isLevelOwnedByPlayer(playerId, levelId));
    }

    @PostMapping("/levels/buy")
    public ResponseEntity<String> buyLevel(
            @RequestParam Long playerId,
            @RequestParam Long levelId) {
        walletService.buyLevel(playerId, levelId);
        return ResponseEntity.ok("Level gekauft");
    }

    // -----------------------
    // Selected-Endpunkte
    // -----------------------

    @PostMapping("/selected/weapon")
    public ResponseEntity<String> selectWeapon(
            @RequestParam Long playerId,
            @RequestParam Long weaponId) {
        walletService.selectWeapon(playerId, weaponId);
        return ResponseEntity.ok("Auswahl aktualisiert: Brawler");
    }

    @PostMapping("/selected/gadget")
    public ResponseEntity<String> selectGadget(
            @RequestParam Long playerId,
            @RequestParam Long gadgetId) {
        walletService.selectGadget(playerId, gadgetId);
        return ResponseEntity.ok("Auswahl aktualisiert: Gadget");
    }

    @PostMapping("/selected/level")
    public ResponseEntity<String> selectLevel(
            @RequestParam Long playerId,
            @RequestParam Long levelId) {
        walletService.selectLevel(playerId, levelId);
        return ResponseEntity.ok("Auswahl aktualisiert: Level");
    }

    @GetMapping("/selected")
    public ResponseEntity<Selected> getSelected(@RequestParam Long playerId) {
        return walletService.getSelectedForPlayer(playerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
