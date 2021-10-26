package foodprint.backend.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineItemRepo extends JpaRepository<LineItem, Long> {
    
    List<LineItem> findByLineItemId(Long lineItemId);

    List<LineItem> findByFood(Food food);

    List<LineItem> findByReservation(Reservation reservation);

}
