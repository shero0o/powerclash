package at.fhv.wallet.service;

import at.fhv.wallet.model.*;
import at.fhv.wallet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final BrawlerRepository brawlerRepo;
    private final GadgetRepository gadgetRepo;
    private final LevelRepository levelRepo;
    private final PlayerLevelRepository playerLevelRepo;
    private final PlayerCoinsRepository coinsRepo;
    private final PlayerBrawlerRepository playerBrawlerRepo;
    private final PlayerGadgetRepository playerGadgetRepo;
    private final SelectedRepository selectedRepo;

    // -----------------------
    // BRAWLER-FUNKTIONEN
    // -----------------------

    public List<Brawler> getAllBrawlers() {
        return brawlerRepo.findAll();
    }

    public List<Brawler> getAllBrawlersOwnedByPlayer(Long playerId) {
        List<PlayerBrawler> owned = playerBrawlerRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                                   .map(PlayerBrawler::getBrawlerId)
                                   .collect(Collectors.toList());
        return brawlerRepo.findAllById(ownedIds);
    }

    public boolean isBrawlerOwnedByPlayer(Long playerId, Long brawlerId) {
        return playerBrawlerRepo.existsByPlayerIdAndBrawlerId(playerId, brawlerId);
    }

    public void buyBrawler(Long playerId, Long brawlerId) {
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player nicht gefunden: " + playerId));
        Brawler b = brawlerRepo.findById(brawlerId)
                .orElseThrow(() -> new IllegalArgumentException("Brawler nicht gefunden: " + brawlerId));

        if (pc.getCoins() < b.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um den Brawler zu kaufen.");
        }

        pc.setCoins(pc.getCoins() - b.getCost());
        coinsRepo.save(pc);

        if (!playerBrawlerRepo.existsByPlayerIdAndBrawlerId(playerId, brawlerId)) {
            PlayerBrawler pb = new PlayerBrawler(playerId, brawlerId);
            playerBrawlerRepo.save(pb);
        }
    }

    public List<Brawler> getAllBrawlersNotOwnedByPlayer(Long playerId) {
        List<PlayerBrawler> owned = playerBrawlerRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                                   .map(PlayerBrawler::getBrawlerId)
                                   .collect(Collectors.toList());
        if (ownedIds.isEmpty()) {
            return brawlerRepo.findAll();
        } else {
            return brawlerRepo.findByIdNotIn(ownedIds);
        }
    }

    // -----------------------
    // GADGET-FUNKTIONEN
    // -----------------------

    public List<Gadget> getAllGadgets() {
        return gadgetRepo.findAll();
    }

    public List<Gadget> getAllGadgetsOwnedByPlayer(Long playerId) {
        List<PlayerGadget> owned = playerGadgetRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                                   .map(PlayerGadget::getGadgetId)
                                   .collect(Collectors.toList());
        return gadgetRepo.findAllById(ownedIds);
    }

    public boolean isGadgetOwnedByPlayer(Long playerId, Long gadgetId) {
        return playerGadgetRepo.existsByPlayerIdAndGadgetId(playerId, gadgetId);
    }

    public void buyGadget(Long playerId, Long gadgetId) {
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player nicht gefunden: " + playerId));
        Gadget g = gadgetRepo.findById(gadgetId)
                .orElseThrow(() -> new IllegalArgumentException("Gadget nicht gefunden: " + gadgetId));

        if (pc.getCoins() < g.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um das Gadget zu kaufen.");
        }

        pc.setCoins(pc.getCoins() - g.getCost());
        coinsRepo.save(pc);

        if (!playerGadgetRepo.existsByPlayerIdAndGadgetId(playerId, gadgetId)) {
            PlayerGadget pg = new PlayerGadget(playerId, gadgetId);
            playerGadgetRepo.save(pg);
        }
    }

    public List<Gadget> getAllGadgetsNotOwnedByPlayer(Long playerId) {
        List<PlayerGadget> owned = playerGadgetRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                                   .map(PlayerGadget::getGadgetId)
                                   .collect(Collectors.toList());
        if (ownedIds.isEmpty()) {
            return gadgetRepo.findAll();
        } else {
            return gadgetRepo.findByIdNotIn(ownedIds);
        }
    }

    // -----------------------
    // COIN-FUNKTIONEN
    // -----------------------

    public void addCoinsForPlayer(Long playerId, Integer amount) {
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseGet(() -> {
                    PlayerCoins neu = new PlayerCoins(playerId, 0);
                    return neu;
                });
        pc.setCoins(pc.getCoins() + amount);
        coinsRepo.save(pc);
    }

    public Integer getCoinsForPlayer(Long playerId) {
        return coinsRepo.findById(playerId)
                .map(PlayerCoins::getCoins)
                .orElse(0);
    }

    public List<PlayerCoins> getAllCoins() {
        return coinsRepo.findAll();
    }

    // -----------------------
    // LEVEL-FUNKTIONEN
    // -----------------------

    public List<Level> getAllLevels() {
        return levelRepo.findAll();
    }

    public List<Level> getAllLevelsOwnedByPlayer(Long playerId) {
        List<PlayerLevel> owned = playerLevelRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                .map(PlayerLevel::getLevelId)
                .collect(Collectors.toList());
        return levelRepo.findAllById(ownedIds);
    }

    public boolean isLevelOwnedByPlayer(Long playerId, Long levelId) {
        return playerLevelRepo.existsByPlayerIdAndLevelId(playerId, levelId);
    }

    public void buyLevel(Long playerId, Long levelId) {
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player nicht gefunden: " + playerId));
        Level g = levelRepo.findById(levelId)
                .orElseThrow(() -> new IllegalArgumentException("Level nicht gefunden: " + levelId));

        if (pc.getCoins() < g.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um das Level zu kaufen.");
        }

        pc.setCoins(pc.getCoins() - g.getCost());
        coinsRepo.save(pc);

        if (!playerLevelRepo.existsByPlayerIdAndLevelId(playerId, levelId)) {
            PlayerLevel pg = new PlayerLevel(playerId, levelId);
            playerLevelRepo.save(pg);
        }
    }

    public List<Level> getAllLevelsNotOwnedByPlayer(Long playerId) {
        List<PlayerLevel> owned = playerLevelRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                .map(PlayerLevel::getLevelId)
                .collect(Collectors.toList());
        if (ownedIds.isEmpty()) {
            return levelRepo.findAll();
        } else {
            return levelRepo.findByIdNotIn(ownedIds);
        }
    }
    
    // -----------------------
    // SELECTED-FUNKTIONEN
    // -----------------------

    public void selectWeapon(Long playerId, Weapon weapon) {
        Selected selected = selectedRepo.findById(playerId)
                .orElse(new Selected(playerId, weapon.getId(), null, null));
        selected.setBrawlerId(weapon.getId());
        selectedRepo.save(selected);
    }

    public void selectGadget(Long playerId, Gadget gadget) {
        Selected selected = selectedRepo.findById(playerId)
                .orElse(new Selected(playerId, null, gadget.getId(), null));
        selected.setGadgetId(gadget.getId());
        selectedRepo.save(selected);
    }

    public void selectLevel(Long playerId, Level level) {
        Selected selected = selectedRepo.findById(playerId)
                .orElse(new Selected(playerId, null, null, level.getId()));
        selected.setLevelId(level.getId());
        selectedRepo.save(selected);
    }
    public Optional<Selected> getSelectedForPlayer(Long playerId) {
        return selectedRepo.findById(playerId);
    }
}
