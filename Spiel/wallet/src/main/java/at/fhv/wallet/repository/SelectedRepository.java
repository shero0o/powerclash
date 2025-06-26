package at.fhv.wallet.repository;

import at.fhv.wallet.model.Selected;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SelectedRepository extends JpaRepository<Selected, UUID> {
}
