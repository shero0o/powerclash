package at.fhv.shop_catalogue.service;

import at.fhv.shop_catalogue.model.*;
import at.fhv.shop_catalogue.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient; // Die Dependencykonflikte werden beim docker-compose up --build resolved

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    private final String WALLET_BASE_URL = "http://localhost:8092/api/wallet";

    // -----------------------
    // BRAWLER-FUNKTIONEN
    // -----------------------

    public List<ShopItemDTO> getAllShopItems(UUID playerId) {
        List<ShopItemDTO> result = new ArrayList<>();

        List<Purchase> purchases = purchaseRepo.findByPlayerId(playerId);

        Set<String> purchasedKeys = purchases.stream()
                .map(p -> p.getItemType() + "-" + p.getItemId())
                .collect(Collectors.toSet());

        brawlerRepo.findAll().forEach(brawler -> {
            String key = ShopItemType.BRAWLER + "-" + brawler.getId();
            if (!purchasedKeys.contains(key)) {
                result.add(new ShopItemDTO(brawler.getId(), brawler.getName(), brawler.getPrice(), ShopItemType.BRAWLER));
            }
        });

        gadgetRepo.findAll().forEach(gadget -> {
            String key = ShopItemType.GADGET + "-" + gadget.getId();
            if (!purchasedKeys.contains(key)) {
                result.add(new ShopItemDTO(gadget.getId(), gadget.getName(), gadget.getPrice(), ShopItemType.GADGET));
            }
        });

        levelRepo.findAll().forEach(level -> {
            String key = ShopItemType.LEVEL + "-" + level.getId();
            if (!purchasedKeys.contains(key)) {
                result.add(new ShopItemDTO(level.getId(), level.getName(), level.getPrice(), ShopItemType.LEVEL));
            }
        });

        return result;
    }

    @Transactional
    public void buyBrawler(UUID playerId, Long brawlerId) {
        Brawler brawler = brawlerRepo.findById(brawlerId)
                .orElseThrow(() -> new RuntimeException("Brawler not found"));

        WebClient client = WebClient.create("http://wallet-service:8092");
        ResponseEntity<Void> result = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/wallet/brawlers/buy")
                        .queryParam("playerId", playerId)
                        .queryParam("brawlerId", brawlerId)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .block();
        Purchase purchase = new Purchase(playerId, ShopItemType.BRAWLER, brawlerId);
    }

    @Transactional
    public void buyGadget(UUID playerId, Long gadgetId) {
        Gadget gadget = gadgetRepo.findById(gadgetId)
                .orElseThrow(() -> new RuntimeException("Gadget not found"));

        WebClient client = WebClient.create("http://wallet-service:8092");
        ResponseEntity<Void> result = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/wallet/gadgets/buy")
                        .queryParam("playerId", playerId)
                        .queryParam("gadgetId", gadgetId)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .block();

        Purchase purchase = new Purchase(playerId, ShopItemType.GADGET, gadgetId);
        purchaseRepo.save(purchase);
    }

    @Transactional
    public void buyLevel(UUID playerId, Long levelId) {
        Level level = levelRepo.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));

        WebClient client = WebClient.create("http://wallet-service:8092");
        ResponseEntity<Void> result = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/wallet/levels/buy")
                        .queryParam("playerId", playerId)
                        .queryParam("levelId", levelId)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .block();

        Purchase purchase = new Purchase(playerId, ShopItemType.LEVEL, levelId);
        purchaseRepo.save(purchase);
    }

    public List<Purchase> getPurchasesForPlayer(UUID playerId) {
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
