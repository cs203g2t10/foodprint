package foodprint.backend.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodRepo extends JpaRepository<Food, Integer> {

    Food findByFoodName(String foodName);

    List<Food> findByFoodNameContains(String foodName);

    List<Food> findByFoodNameContainsIgnoreCase(String name);

    Page<Food> findByFoodNameContains(Pageable page, String name);

}
