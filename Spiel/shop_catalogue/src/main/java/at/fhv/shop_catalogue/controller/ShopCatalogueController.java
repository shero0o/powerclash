package at.fhv.shop_catalogue.controller;

import at.fhv.shop_catalogue.model.Brawler;
import at.fhv.shop_catalogue.model.Gadget;
import at.fhv.shop_catalogue.service.ShopCatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop_catalogue")
@RequiredArgsConstructor
public class ShopCatalogueController {

    private final ShopCatalogueService shopCatalogueService;

    // -----------------------
    // Brawler-Endpunkte
    // -----------------------

    @GetMapping("/brawlers")
    public ResponseEntity<List<Brawler>> getAllBrawlers() {
        return ResponseEntity.ok(shopCatalogueService.getAllBrawlers());
    }

    // -----------------------
    // Gadget-Endpunkte
    // -----------------------

    @GetMapping("/gadgets")
    public ResponseEntity<List<Gadget>> getAllGadgets() {
        return ResponseEntity.ok(shopCatalogueService.getAllGadgets());
    }
}
