package foodprint.backend.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepo extends JpaRepository<Ingredient, Long> {
    
    Ingredient findByIngredientId(Long ingredientId);

    Page<Ingredient> findByRestaurantRestaurantId(Pageable page, Long restaurantId);
    
}
