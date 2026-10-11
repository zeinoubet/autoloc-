package tn.esprit.elabed_zeineb_4cce11.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.elabed_zeineb_4cce11.domain.Paiement;

public interface IPaiementRepository extends JpaRepository<Paiement, Long> {
}
