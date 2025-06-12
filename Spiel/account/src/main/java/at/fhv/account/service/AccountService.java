package at.fhv.account.service;

import at.fhv.account.model.Player;
import at.fhv.account.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final PlayerRepository playerRepository;


    // -----------------------
    // ACCOUNT-FUNKTIONEN
    // -----------------------

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public  Optional<Player> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }


    @Transactional
    public Player createPlayer(String name) {
        // 1) Spieler in der Account-DB anlegen
        Player p = playerRepository.save(new Player(null, name));
//
//        // 2) WebClient-Aufruf an den Wallet-Service
//        WebClient client = WebClient.create("http://host.docker.internal:8092");
//        Mono<String> result = client.post()
//                .uri("/api/wallet/defaults/{playerId}", p.getId())
//                .retrieve()
//                .bodyToMono(String.class);       // oder bodyToMono(Void.class)
//        String body = result.block();         // synchron warten :contentReference[oaicite:0]{index=0}

//        // optional: auf result prüfen oder loggen
//        System.out.println("Assigned defaults for player " + p.getId() + ": " + body);

        return p;
    }

    public Player updatePlayerName(Long playerId, String newName) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player mit ID " + playerId + " nicht gefunden"));

        player.setName(newName);
        return playerRepository.save(player);
    }
}
