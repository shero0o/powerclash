package at.fhv.wallet.repository;

import at.fhv.wallet.model.PlayerCoins;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerCoinsRepository extends JpaRepository<PlayerCoins, Long> {
    // findById wird bereits von JpaRepository bereitgestellt
}
