package foodprint.backend.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByDate(LocalDateTime dateOfReservation);
    
    List<Reservation> findByRestaurant(Restaurant restaurant);
    
    List<Reservation> findByRestaurantAndDateBetween(Restaurant restaurant, LocalDateTime after, LocalDateTime before);

    Page<Reservation> findByRestaurantAndDateBetween(Pageable page, Restaurant restaurant, LocalDateTime after, LocalDateTime before);
    
    List<Reservation> findByUser(User user);

    Optional<Reservation> findByReservationIdAndUserId(Long id, Long userId);


}
