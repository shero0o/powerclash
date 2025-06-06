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

    // -----------------------
    // BRAWLER-FUNKTIONEN
    // -----------------------

    public List<Brawler> getAllBrawlers() {
        return brawlerRepo.findAll();
    }

    // -----------------------
    // GADGET-FUNKTIONEN
    // -----------------------

    public List<Gadget> getAllGadgets() {
        return gadgetRepo.findAll();
    }

}
