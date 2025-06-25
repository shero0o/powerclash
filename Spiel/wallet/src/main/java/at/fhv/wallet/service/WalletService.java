package at.fhv.wallet.service;

import at.fhv.wallet.model.*;
import at.fhv.wallet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final BrawlerRepository brawlerRepo;
    private final GadgetRepository gadgetRepo;
    private final LevelRepository levelRepo;
    private final WeaponRepository weaponRepo;
    private final PlayerRepository playerRepo;
    private final SelectedRepository selectedRepo;

    public Player createPlayer(UUID playerId) {

        if (playerRepo.existsById(playerId)) {
            throw new IllegalArgumentException("Ein Spieler mit dieser ID existiert bereits.");
        }

        Player player = new Player();
        player.setId(playerId);
        player.setCoins(0);
        playerRepo.save(player);

        assignDefaults(player);

        return player;
    }

    public List<Brawler> getAllBrawlers() {
        return brawlerRepo.findAll();
    }

    public List<Brawler> getAllBrawlersOwnedByPlayer(UUID playerId) {
        Player player = playerRepo.findById(playerId).orElseThrow();
        return new ArrayList<>(player.getBrawlers());
    }

    public void buyBrawler(UUID playerId, Long brawlerId) {
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden."));
        Brawler b = brawlerRepo.findById(brawlerId).orElseThrow(() -> new IllegalArgumentException("Brawler nicht gefunden."));

        if (player.getCoins() < b.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um den Brawler zu kaufen.");
        }

        player.setCoins(player.getCoins() - b.getCost());
        player.getBrawlers().add(b);
        playerRepo.save(player);
    }

    public List<Brawler> getAllBrawlersNotOwnedByPlayer(UUID playerId) {
        List<Brawler> owned = getAllBrawlersOwnedByPlayer(playerId);
        List<Long> ownedIds = owned.stream().map(Brawler::getId).toList();
        return ownedIds.isEmpty() ? brawlerRepo.findAll() : brawlerRepo.findByIdNotIn(ownedIds);
    }

    public List<Gadget> getAllGadgets() {
        return gadgetRepo.findAll();
    }

    public List<Gadget> getAllGadgetsOwnedByPlayer(UUID playerId) {
        Player player = playerRepo.findById(playerId).orElseThrow();
        return new ArrayList<>(player.getGadgets());
    }

    public void buyGadget(UUID playerId, Long gadgetId) {
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden."));
        Gadget g = gadgetRepo.findById(gadgetId).orElseThrow(() -> new IllegalArgumentException("Gadget nicht gefunden."));

        if (player.getCoins() < g.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um das Gadget zu kaufen.");
        }

        player.setCoins(player.getCoins() - g.getCost());
        player.getGadgets().add(g);
        playerRepo.save(player);
    }

    public List<Gadget> getAllGadgetsNotOwnedByPlayer(UUID playerId) {
        List<Gadget> owned = getAllGadgetsOwnedByPlayer(playerId);
        List<Long> ownedIds = owned.stream().map(Gadget::getId).toList();
        return ownedIds.isEmpty() ? gadgetRepo.findAll() : gadgetRepo.findByIdNotIn(ownedIds);
    }

    public void addCoinsForPlayer(UUID playerId, Integer amount) {
        Player player = playerRepo.findById(playerId).orElseThrow();
        player.setCoins(player.getCoins() + amount);
        playerRepo.save(player);
    }

    public Integer getCoinsForPlayer(UUID playerId) {
        return playerRepo.findById(playerId).map(Player::getCoins).orElse(0);
    }

    public List<Weapon> getAllWeapons() {
        return weaponRepo.findAll();
    }

    public List<Level> getAllLevels() {
        return levelRepo.findAll();
    }

    public List<Level> getAllLevelsOwnedByPlayer(UUID playerId) {
        Player player = playerRepo.findById(playerId).orElseThrow();
        return new ArrayList<>(player.getLevels());
    }

    public void buyLevel(UUID playerId, Long levelId) {
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden."));
        Level level = levelRepo.findById(levelId).orElseThrow(() -> new IllegalArgumentException("Level nicht gefunden."));

        if (player.getCoins() < level.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um das Level zu kaufen.");
        }

        player.setCoins(player.getCoins() - level.getCost());
        player.getLevels().add(level);
        playerRepo.save(player);
    }

    public List<Level> getAllLevelsNotOwnedByPlayer(UUID playerId) {
        List<Level> owned = getAllLevelsOwnedByPlayer(playerId);
        List<Long> ownedIds = owned.stream().map(Level::getId).toList();
        return ownedIds.isEmpty() ? levelRepo.findAll() : levelRepo.findByIdNotIn(ownedIds);
    }

    public void selectBrawler(UUID playerId, Long weaponId) {
        Selected selected = selectedRepo.findById(playerId).orElseThrow();
        selected.setBrawlerId(weaponId);
        selectedRepo.save(selected);
    }

    public void selectWeapon(UUID playerId, Long weaponId) {
        Selected selected = selectedRepo.findById(playerId).orElseThrow();
        selected.setWeaponId(weaponId);
        selectedRepo.save(selected);
    }

    public void selectGadget(UUID playerId, Long gadgetId) {
        Selected selected = selectedRepo.findById(playerId).orElseThrow();
        selected.setGadgetId(gadgetId);
        selectedRepo.save(selected);
    }

    public void selectLevel(UUID playerId, Long levelId) {
        Selected selected = selectedRepo.findById(playerId).orElseThrow();
        selected.setLevelId(levelId);
        selectedRepo.save(selected);
    }

    public Selected getSelectedForPlayer(UUID playerId) {
        return selectedRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden."));
    }

    public void assignDefaults(Player player) {

        Level lvl = levelRepo.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Standard-Level mit ID 1 nicht gefunden!"));
        Brawler brawler = brawlerRepo.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Standard-Brawler mit ID 1 nicht gefunden!"));
        Gadget gadget = gadgetRepo.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Standard-Gadget mit ID 1 nicht gefunden!"));


        player.setLevels(List.of(lvl));
        player.setBrawlers(List.of(brawler));
        player.setGadgets(List.of(gadget));
        playerRepo.save(player);

        Selected s = new Selected();
        s.setSelectedId(player.getId());
        s.setSelectedId(player.getId());
        s.setBrawlerId(1L);
        s.setWeaponId(1L);
        s.setGadgetId(1L);
        s.setLevelId(1L);
        selectedRepo.save(s);
    }
}
