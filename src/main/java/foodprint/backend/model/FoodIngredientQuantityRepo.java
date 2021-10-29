package foodprint.backend.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodIngredientQuantityRepo extends JpaRepository<FoodIngredientQuantity, FoodIngredientQuantityKey>{
    
}
