package at.fhv.wallet.controller;

import at.fhv.wallet.model.*;
import at.fhv.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<List<Brawler>> getBrawlersOwned(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getAllBrawlersOwnedByPlayer(UUID.fromString(playerId)));
    }

    @GetMapping("/brawlers/player/notOwned")
    public ResponseEntity<List<Brawler>> getBrawlersNotOwned(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getAllBrawlersNotOwnedByPlayer(UUID.fromString(playerId)));
    }

    @PostMapping("/brawlers/buy")
    public ResponseEntity<String> buyBrawler(
            @RequestParam String playerId,
            @RequestParam Long brawlerId) {
            try {
                walletService.buyBrawler(UUID.fromString(playerId), brawlerId);
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
    public ResponseEntity<List<Gadget>> getGadgetsOwned(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getAllGadgetsOwnedByPlayer(UUID.fromString(playerId)));
    }

    @GetMapping("/gadgets/player/notOwned")
    public ResponseEntity<List<Gadget>> getGadgetsNotOwned(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getAllGadgetsNotOwnedByPlayer(UUID.fromString(playerId)));
    }

    @PostMapping("/gadgets/buy")
    public ResponseEntity<String> buyGadget(
            @RequestParam String playerId,
            @RequestParam Long gadgetId) {
        walletService.buyGadget(UUID.fromString(playerId), gadgetId);
        return ResponseEntity.ok("Gadget gekauft");
    }

    // -----------------------
    // Weapon-Endpunkte
    // -----------------------

    @GetMapping("/weapons")
    public ResponseEntity<List<Weapon>> getAllWeapons() {
        return ResponseEntity.ok(walletService.getAllWeapons());
    }


    // -----------------------
    // Coins-Endpunkte
    // -----------------------

    @GetMapping("/coins")
    public ResponseEntity<Integer> getCoinsByPlayer(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getCoinsForPlayer(UUID.fromString(playerId)));
    }

    @PostMapping("/coins/add")
    public ResponseEntity<String> addCoins(
            @RequestParam String playerId,
            @RequestParam Integer amount) {
        walletService.addCoinsForPlayer(UUID.fromString(playerId), amount);
        return ResponseEntity.ok("Coins hinzugefügt");
    }


    // -----------------------
    // Level-Endpunkte
    // -----------------------

    @GetMapping("/levels")
    public ResponseEntity<List<Level>> getAllLevels() {
        return ResponseEntity.ok(walletService.getAllLevels());
    }

    @GetMapping("/levels/player")
    public ResponseEntity<List<Level>> getLevelsOwned(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getAllLevelsOwnedByPlayer(UUID.fromString(playerId)));
    }

    @GetMapping("/levels/player/notOwned")
    public ResponseEntity<List<Level>> getLevelsNotOwned(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getAllLevelsNotOwnedByPlayer(UUID.fromString(playerId)));
    }

    @PostMapping("/levels/buy")
    public ResponseEntity<String> buyLevel(
            @RequestParam String playerId,
            @RequestParam Long levelId) {
        walletService.buyLevel(UUID.fromString(playerId), levelId);
        return ResponseEntity.ok("Level gekauft");
    }

    // -----------------------
    // Selected-Endpunkte
    // -----------------------

    @PostMapping("/selected/brawler")
    public ResponseEntity<String> selectBrawler(
            @RequestParam String playerId,
            @RequestParam Long brawlerId) {
        walletService.selectBrawler(UUID.fromString(playerId), brawlerId);
        return ResponseEntity.ok("Auswahl aktualisiert: Brawler");
    }

    @PostMapping("/selected/weapon")
    public ResponseEntity<String> selectWeapon(
            @RequestParam String playerId,
            @RequestParam Long weaponId) {
        walletService.selectWeapon(UUID.fromString(playerId), weaponId);
        return ResponseEntity.ok("Auswahl aktualisiert: Weapon");
    }

    @PostMapping("/selected/gadget")
    public ResponseEntity<String> selectGadget(
            @RequestParam String playerId,
            @RequestParam Long gadgetId) {
        walletService.selectGadget(UUID.fromString(playerId), gadgetId);
        return ResponseEntity.ok("Auswahl aktualisiert: Gadget");
    }

    @PostMapping("/selected/level")
    public ResponseEntity<String> selectLevel(
            @RequestParam String playerId,
            @RequestParam Long levelId) {
        walletService.selectLevel(UUID.fromString(playerId), levelId);
        return ResponseEntity.ok("Auswahl aktualisiert: Level");
    }

    @GetMapping("/selected")
    public ResponseEntity<Selected> getSelected(@RequestParam String playerId) {
        return ResponseEntity.ok(walletService.getSelectedForPlayer(UUID.fromString(playerId)));
    }

    @PostMapping("/createPlayer")
    public ResponseEntity<Void> createPlayer(@RequestParam String playerId) {
        walletService.createPlayer(UUID.fromString(playerId));
        return ResponseEntity.ok().build();
    }
}
