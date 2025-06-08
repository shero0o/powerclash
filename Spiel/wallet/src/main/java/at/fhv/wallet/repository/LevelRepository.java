package at.fhv.wallet.repository;

import at.fhv.wallet.model.Brawler;
import at.fhv.wallet.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findByIdNotIn(List<Long> ownedLevelIds);
}
