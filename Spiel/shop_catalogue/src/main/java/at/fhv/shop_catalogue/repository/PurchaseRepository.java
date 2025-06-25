package at.fhv.shop_catalogue.repository;

import at.fhv.shop_catalogue.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
    List<Purchase> findByPlayerId(UUID playerId);
}
