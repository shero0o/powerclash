package at.fhv.wallet.controller;

import at.fhv.wallet.model.*;
import at.fhv.wallet.service.WalletService;
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

    @PostMapping("/gadgets/buy")
    public ResponseEntity<String> buyGadget(
            @RequestParam Long playerId,
            @RequestParam Long gadgetId) {
        walletService.buyGadget(playerId, gadgetId);
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

    @PostMapping("/selected/brawler")
    public ResponseEntity<String> selectBrawler(
            @RequestParam Long playerId,
            @RequestParam Long brawlerId) {
        walletService.selectBrawler(playerId, brawlerId);
        return ResponseEntity.ok("Auswahl aktualisiert: Brawler");
    }

    @PostMapping("/selected/weapon")
    public ResponseEntity<String> selectWeapon(
            @RequestParam Long playerId,
            @RequestParam Long weaponId) {
        walletService.selectWeapon(playerId, weaponId);
        return ResponseEntity.ok("Auswahl aktualisiert: Weapon");
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
        return ResponseEntity.ok(walletService.getSelectedForPlayer(playerId));
    }

    @PostMapping("/createPlayer")
    public ResponseEntity<Void> createPlayer(@RequestParam Long playerId) {
        walletService.createPlayer(playerId);
        return ResponseEntity.ok().build();
    }
}
