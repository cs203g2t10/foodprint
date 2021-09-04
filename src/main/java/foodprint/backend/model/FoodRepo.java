package foodprint.backend.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodRepo extends JpaRepository<Food, Integer> {

    Food findByFoodName(String foodName);

    List<Food> findByFoodNameContains(String FoodName);

    List<Food> findByFoodNameContainsIgnoreCase(String name);
}
