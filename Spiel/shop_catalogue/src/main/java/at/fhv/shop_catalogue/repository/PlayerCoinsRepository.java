package at.fhv.shop_catalogue.repository;

import at.fhv.shop_catalogue.model.PlayerCoins;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerCoinsRepository extends JpaRepository<PlayerCoins, Long> {
}
