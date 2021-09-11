package foodprint.backend.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.model.Discount;
import foodprint.backend.model.DiscountRepo;

@RestController
@RequestMapping("/api/v1/restaurant/discount")
public class DiscountController {

    private DiscountRepo repo;
    
    @Autowired
    DiscountController(DiscountRepo repo) {
        this.repo = repo;
    }

    //GET: Get the discounts
    @GetMapping({"/id/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Discount> getDiscount(@PathVariable("discountId") Integer id) {
        Optional<Discount> discount = repo.findById(id);
        if (discount.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(discount.get(), HttpStatus.OK);
    }

    //GET: Get ALL the discounts 
    @GetMapping({"/all"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Discount>> getAllDiscouont() {
        List<Discount> discount = repo.findAll();
        return new ResponseEntity<>(discount, HttpStatus.OK);
    }

    //POST: Create new Discount
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Discount> createDiscount(@RequestBody Discount discount) {
        var savedDiscount = repo.saveAndFlush(discount);
        return new ResponseEntity<>(savedDiscount, HttpStatus.CREATED);
    }

    //PUT: Update Discount
    @PutMapping({"/id/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Discount> updateDiscount(
        @PathVariable("discountId") Integer id,
        @RequestBody Discount updatedDiscount
    ) {
        Optional<Discount> currentDiscountOpt = repo.findById(id);
        if (currentDiscountOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var currentDiscount = currentDiscountOpt.get();
        currentDiscount.setDiscountDescription(updatedDiscount.getDiscountDescription());
        currentDiscount.setDiscountPercentage(updatedDiscount.getDiscountPercentage());
        currentDiscount = repo.saveAndFlush(currentDiscount);
        return new ResponseEntity<>(currentDiscount, HttpStatus.OK);
    }

    //DELETE: Delete the Discount
    @DeleteMapping({"/id/{discountId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Discount> deleteDiscount(@PathVariable("discountId") Integer id) {
        var savedDiscount = repo.findById(id);
        
        if (savedDiscount.isPresent()) {
            repo.delete(savedDiscount.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        savedDiscount = repo.findById(id);
        if (savedDiscount.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
