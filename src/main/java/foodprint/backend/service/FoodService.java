package foodprint.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.Food;
import foodprint.backend.exceptions.NotFoundException;

@Service
public class FoodService {
    
    private FoodRepo foodRepo;

    @Autowired
    FoodService(FoodRepo foodRepo) {
        this.foodRepo = foodRepo;
    }

    public Food getFoodById(Long id) {
        Optional<Food> food = foodRepo.findById(id);
        return food.orElseThrow(() -> new NotFoundException("Food not found"));
    }
}
