package at.fhv.shop_catalogue.service;

import at.fhv.shop_catalogue.model.*;
import at.fhv.shop_catalogue.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopCatalogueService {

    private final BrawlerRepository brawlerRepo;
    private final GadgetRepository gadgetRepo;
    private final LevelRepository levelRepo;
    private final PurchaseRepository purchaseRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String WALLET_BASE_URL = "http://localhost:8090/api/wallet";

    // -----------------------
    // BRAWLER-FUNKTIONEN
    // -----------------------

    public List<ShopItemDTO> getAllShopItems() {
        List<ShopItemDTO> result = new ArrayList<>();

        brawlerRepo.findAll().forEach(brawler -> {
            result.add(new ShopItemDTO(brawler.getId(), brawler.getName(), brawler.getPrice(), ShopItemType.BRAWLER));
        });

        gadgetRepo.findAll().forEach(gadget -> {
            result.add(new ShopItemDTO(gadget.getId(), gadget.getName(), gadget.getPrice(), ShopItemType.GADGET));
        });

        levelRepo.findAll().forEach(level -> {
            result.add(new ShopItemDTO(level.getId(), level.getName(), level.getPrice(), ShopItemType.LEVEL));
        });

        return result;
    }

    public void buyBrawler(String playerId, Long brawlerId) {
        Brawler brawler = brawlerRepo.findById(brawlerId)
                .orElseThrow(() -> new RuntimeException("Brawler not found"));

        // REST Call an WalletService → Brawler kaufen
        String url = WALLET_BASE_URL + "/brawlers/buy?playerId=" + playerId + "&brawlerId=" + brawlerId;
        restTemplate.postForEntity(url, null, Void.class);

        // Purchase speichern
        Purchase purchase = new Purchase(playerId, ShopItemType.BRAWLER, brawlerId);
        purchaseRepo.save(purchase);
    }

    public void buyGadget(String playerId, Long gadgetId) {
        Gadget gadget = gadgetRepo.findById(gadgetId)
                .orElseThrow(() -> new RuntimeException("Gadget not found"));

        String url = WALLET_BASE_URL + "/gadgets/buy?playerId=" + playerId + "&gadgetId=" + gadgetId;
        restTemplate.postForEntity(url, null, Void.class);

        Purchase purchase = new Purchase(playerId, ShopItemType.GADGET, gadgetId);
        purchaseRepo.save(purchase);
    }

    public void buyLevel(String playerId, Long levelId) {
        Level level = levelRepo.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));

        // TODO: Aktuell KEIN buyLevel Endpoint im WalletService → später ergänzen!
        // String url = WALLET_BASE_URL + "/levels/buy?playerId=" + playerId + "&levelId=" + levelId;
        // restTemplate.postForEntity(url, null, Void.class);

        // Jetzt nur Purchase speichern
        Purchase purchase = new Purchase(playerId, ShopItemType.LEVEL, levelId);
        purchaseRepo.save(purchase);
    }

    public List<Purchase> getPurchasesForPlayer(String playerId) {
        return purchaseRepo.findByPlayerId(playerId);
    }
    public List<Brawler> getAllBrawlers() {
        return brawlerRepo.findAll();
    }

    public List<Gadget> getAllGadgets() {
        return gadgetRepo.findAll();
    }

    public List<Level> getAllLevels() {
        return levelRepo.findAll();
    }

}
