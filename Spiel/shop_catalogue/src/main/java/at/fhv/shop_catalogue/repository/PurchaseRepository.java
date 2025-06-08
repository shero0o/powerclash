package at.fhv.shop_catalogue.repository;

import at.fhv.shop_catalogue.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByPlayerId(String playerId);
}
