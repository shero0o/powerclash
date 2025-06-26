package at.fhv.wallet.repository;

import at.fhv.wallet.model.Brawler;
import at.fhv.wallet.model.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeaponRepository extends JpaRepository<Weapon, Long> {
    List<Brawler> findByIdNotIn(List<Long> ownedBrawlerIds);
}
