package at.fhv.wallet.repository;

import at.fhv.wallet.model.Selected;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelectedRepository extends JpaRepository<Selected, Long> {
}
