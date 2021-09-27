package foodprint.backend.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, Long> {

    Restaurant findByRestaurantName(String restaurantName);

    Restaurant findByRestaurantId(Long restaurantId);

    List<Restaurant> findByRestaurantNameContains(String restaurantName);

    List<Restaurant> findByRestaurantNameContainsIgnoreCase(String name);

    Page<Restaurant> findByRestaurantNameContains(Pageable page, String name);

}