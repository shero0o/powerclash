package at.fhv.wallet.controller;

import at.fhv.wallet.model.Brawler;
import at.fhv.wallet.model.Gadget;
import at.fhv.wallet.model.PlayerCoins;
import at.fhv.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        walletService.buyBrawler(playerId, brawlerId);
        return ResponseEntity.ok("Brawler gekauft");
    }

    // -----------------------
    // Gadget-Endpunkte
    // -----------------------

    @GetMapping("/gadgets")
    public ResponseEntity<List<Gadget>> getAllGadgets() {
        return ResponseEntity.ok(walletService.getAllGadgets());
    }

    @GetMapping("/gadgets/player/")
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

    @GetMapping("/coins/")
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
}
