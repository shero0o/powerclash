package at.fhv.shop_catalogue.service;

import at.fhv.shop_catalogue.model.*;
import at.fhv.shop_catalogue.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopCatalogueService {

    private final BrawlerRepository brawlerRepo;
    private final GadgetRepository gadgetRepo;
    private final PlayerCoinsRepository coinsRepo;
    private final PlayerBrawlerRepository playerBrawlerRepo;
    private final PlayerGadgetRepository playerGadgetRepo;

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
                    // Wenn kein Eintrag existiert, neuen anlegen
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

    public void payWithCoinsForPlayer(Long playerId, Integer amount) {
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player nicht gefunden: " + playerId));
        if (pc.getCoins() < amount) {
            throw new IllegalStateException("Nicht genug Münzen verfügbar.");
        }
        pc.setCoins(pc.getCoins() - amount);
        coinsRepo.save(pc);
    }

    public List<PlayerCoins> getAllCoins() {
        return coinsRepo.findAll();
    }
}
