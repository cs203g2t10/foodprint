package foodprint.backend.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByDate(LocalDateTime dateOfReservation);
    List<Reservation> findByRestaurant(Restaurant restaurant);

}
