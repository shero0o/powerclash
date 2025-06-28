package at.fhv.account.service;

import at.fhv.account.model.Player;
import at.fhv.account.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final PlayerRepository playerRepository;



    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public  Optional<Player> getPlayerById(UUID id) {
        return playerRepository.findById(id);
    }


    @Transactional
    public Player createPlayer(String name) {
        Player p = playerRepository.save(new Player(null, name));

        WebClient client = WebClient.create("http://wallet-service:8092");
        Mono<String> result = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/wallet/createPlayer")
                        .queryParam("playerId", p.getId())
                        .build())
                .retrieve()
                .bodyToMono(String.class);
        String body = result.block();

        System.out.println("Assigned defaults for player " + p.getId() + ": " + body);

        return p;
    }

    public Player updatePlayerName(UUID playerId, String newName) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player mit ID " + playerId + " nicht gefunden"));

        player.setName(newName);
        return playerRepository.save(player);
    }

    public Player getPlayerByName(String name) {
        return playerRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        }
}
