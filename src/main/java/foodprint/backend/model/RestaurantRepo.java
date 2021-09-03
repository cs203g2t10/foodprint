package foodprint.backend.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, Integer> {

    Restaurant findByRestaurantName(String restaurantName);

    List<Restaurant> findByRestaurantNameContains(String restaurantName);

    List<Restaurant> findByRestaurantNameContainsIgnoreCase(String name);

}