package tn.esprit.elabed_zeineb_4cce11.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.elabed_zeineb_4cce11.domain.Reservation;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {
}
