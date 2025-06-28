package at.fhv.account.controller;

import at.fhv.account.model.Player;
import at.fhv.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/players")
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(accountService.getAllPlayers());
    }

    @GetMapping("/player")
    public ResponseEntity<Player> getPlayerById(@RequestParam String id) {
        return accountService.getPlayerById(UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/createPlayer")
    public ResponseEntity<?> createPlayer(@RequestParam String name) {
        try {
            Player created = accountService.createPlayer(name);
            return ResponseEntity.ok(created);
        }catch (DataIntegrityViolationException ex){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Name already in use");
        }

    }

    @PutMapping("/updatePlayerName")
    public ResponseEntity<Player> updatePlayerName(
            @RequestParam String id,
            @RequestParam String name) {
        Player updated = accountService.updatePlayerName(UUID.fromString(id), name);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/playerByName")
    public ResponseEntity<Player> getPlayer(@RequestParam String name) {
        return ResponseEntity.ok(accountService.getPlayerByName(name));
    }

}
