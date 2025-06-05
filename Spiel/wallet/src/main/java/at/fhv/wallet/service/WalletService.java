package at.fhv.wallet.service;

import at.fhv.wallet.model.*;
import at.fhv.wallet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final BrawlerRepository brawlerRepo;
    private final GadgetRepository gadgetRepo;
    private final PlayerCoinsRepository coinsRepo;
    private final PlayerBrawlerRepository playerBrawlerRepo;
    private final PlayerGadgetRepository playerGadgetRepo;

    // -----------------------
    // BRAWLER-FUNKTIONEN
    // -----------------------

    /** 
     * Liefert alle Brawler, die im System existieren. 
     * (Verwendung z.B. in einem Admin-Panel)
     */
    public List<Brawler> getAllBrawlers() {
        return brawlerRepo.findAll();
    }

    /**
     * Liefert alle Brawler, die ein Spieler bereits besitzt.
     */
    public List<Brawler> getAllBrawlersOwnedByPlayer(Long playerId) {
        List<PlayerBrawler> owned = playerBrawlerRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                                   .map(PlayerBrawler::getBrawlerId)
                                   .collect(Collectors.toList());
        return brawlerRepo.findAllById(ownedIds);
    }

    /**
     * Prüft, ob ein Spieler einen bestimmten Brawler besitzt.
     */
    public boolean isBrawlerOwnedByPlayer(Long playerId, Long brawlerId) {
        return playerBrawlerRepo.existsByPlayerIdAndBrawlerId(playerId, brawlerId);
    }

    /**
     * Erlaubt dem Spieler, einen Brawler zu kaufen.
     * Dafür werden Coins abgezogen und der Eintrag in Player_Brawler angelegt.
     */
    public void buyBrawler(Long playerId, Long brawlerId) {
        // 1. Prüfen, ob Spieler genug Coins hat:
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player nicht gefunden: " + playerId));
        Brawler b = brawlerRepo.findById(brawlerId)
                .orElseThrow(() -> new IllegalArgumentException("Brawler nicht gefunden: " + brawlerId));

        if (pc.getCoins() < b.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um den Brawler zu kaufen.");
        }

        // 2. Coins abziehen
        pc.setCoins(pc.getCoins() - b.getCost());
        coinsRepo.save(pc);

        // 3. Eintrag in player_brawler
        if (!playerBrawlerRepo.existsByPlayerIdAndBrawlerId(playerId, brawlerId)) {
            PlayerBrawler pb = new PlayerBrawler(playerId, brawlerId);
            playerBrawlerRepo.save(pb);
        }
    }

    /**
     * Liefert alle Brawler, die der Spieler noch nicht besitzt.
     */
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

    /**
     * Liefert alle Gadgets, die im System existieren.
     */
    public List<Gadget> getAllGadgets() {
        return gadgetRepo.findAll();
    }

    /**
     * Liefert alle Gadgets, die ein Spieler bereits besitzt.
     */
    public List<Gadget> getAllGadgetsOwnedByPlayer(Long playerId) {
        List<PlayerGadget> owned = playerGadgetRepo.findByPlayerId(playerId);
        List<Long> ownedIds = owned.stream()
                                   .map(PlayerGadget::getGadgetId)
                                   .collect(Collectors.toList());
        return gadgetRepo.findAllById(ownedIds);
    }

    /**
     * Prüft, ob ein Spieler ein bestimmtes Gadget besitzt.
     */
    public boolean isGadgetOwnedByPlayer(Long playerId, Long gadgetId) {
        return playerGadgetRepo.existsByPlayerIdAndGadgetId(playerId, gadgetId);
    }

    /**
     * Erlaubt dem Spieler, ein Gadget zu kaufen.
     * Dafür werden Coins abgezogen und der Eintrag in Player_Gadget angelegt.
     */
    public void buyGadget(Long playerId, Long gadgetId) {
        // 1. Prüfen, ob Spieler genug Coins hat:
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player nicht gefunden: " + playerId));
        Gadget g = gadgetRepo.findById(gadgetId)
                .orElseThrow(() -> new IllegalArgumentException("Gadget nicht gefunden: " + gadgetId));

        if (pc.getCoins() < g.getCost()) {
            throw new IllegalStateException("Nicht genug Münzen, um das Gadget zu kaufen.");
        }

        // 2. Coins abziehen
        pc.setCoins(pc.getCoins() - g.getCost());
        coinsRepo.save(pc);

        // 3. Eintrag in player_gadget
        if (!playerGadgetRepo.existsByPlayerIdAndGadgetId(playerId, gadgetId)) {
            PlayerGadget pg = new PlayerGadget(playerId, gadgetId);
            playerGadgetRepo.save(pg);
        }
    }

    /**
     * Liefert alle Gadgets, die der Spieler noch nicht besitzt.
     */
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

    /**
     * Fügt einer Wallet (Spieler) eine bestimmte Anzahl Coins hinzu.
     */
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

    /**
     * Liefert die Anzahl an Coins eines Spielers.
     */
    public Integer getCoinsForPlayer(Long playerId) {
        return coinsRepo.findById(playerId)
                .map(PlayerCoins::getCoins)
                .orElse(0);
    }

    /**
     * Zieht eine bestimmte Anzahl Coins vom Spieler ab (z.B. beim Kauf).
     */
    public void payWithCoinsForPlayer(Long playerId, Integer amount) {
        PlayerCoins pc = coinsRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player nicht gefunden: " + playerId));
        if (pc.getCoins() < amount) {
            throw new IllegalStateException("Nicht genug Münzen verfügbar.");
        }
        pc.setCoins(pc.getCoins() - amount);
        coinsRepo.save(pc);
    }

    /**
     * Holt alle PlayerCoins-Einträge (z.B. für Admin-Statistiken).
     */
    public List<PlayerCoins> getAllCoins() {
        return coinsRepo.findAll();
    }
}
