package at.fhv.account.controller;

import at.fhv.account.model.Player;
import at.fhv.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // -----------------------
    // Player-Endpunkte
    // -----------------------

    @GetMapping("/players")
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(accountService.getAllPlayers());
    }

    @GetMapping("/player")
    public ResponseEntity<Player> getPlayerById(@RequestParam Long id) {
        return accountService.getPlayerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/createPlayer")
    public ResponseEntity<Player> createPlayer(@RequestParam String name) {
        Player created = accountService.createPlayer(name);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/updatePlayerName")
    public ResponseEntity<Player> updatePlayerName(
            @RequestParam Long id,
            @RequestParam String name) {
        Player updated = accountService.updatePlayerName(id, name);
        return ResponseEntity.ok(updated);
    }
}
