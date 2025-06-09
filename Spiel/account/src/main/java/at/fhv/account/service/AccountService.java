package at.fhv.account.service;

import at.fhv.account.model.Player;
import at.fhv.account.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Player createPlayer(String name) {
        return playerRepository.save(new Player(null, name));
    }

    public Player updatePlayerName(Long playerId, String newName) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player mit ID " + playerId + " nicht gefunden"));

        player.setName(newName);
        return playerRepository.save(player);
    }
}
