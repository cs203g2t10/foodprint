package foodprint.backend.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByDate(LocalDateTime dateOfReservation);
    
    List<Reservation> findByRestaurant(Restaurant restaurant);
    
    List<Reservation> findByRestaurantAndDateBetween(Restaurant restaurant, LocalDateTime after, LocalDateTime before);
    
    List<Reservation> findByUser(User user);

}
