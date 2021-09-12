package foodprint.backend.controller;

import java.util.Optional;
import java.util.List;
import java.util.Date;

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

import foodprint.backend.model.LineItem;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Restaurant;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {
    
    private ReservationRepo reservationRepo;

    @Autowired
    ReservationController(ReservationRepo reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    // GET: Get reservation by id
    @GetMapping({"/id/{reservationId}"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Reservation> getReservation(@PathVariable("reservationId") Integer id) {
        Optional<Reservation> reservation = reservationRepo.findById(id);
        if (reservation.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(reservation.get(), HttpStatus.OK);
        }
    }

    // GET: Get all reservations
    @GetMapping({"/all"})
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<List<Reservation>> getAllReservation() {
        List<Reservation> reservationList = reservationRepo.findAll();
        return new ResponseEntity<>(reservationList, HttpStatus.OK);
    }

    // POST: Create a new reservation
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        Date date = reservation.getDate();
        List<Reservation> reservationList = reservationRepo.findByDate(date);
        Restaurant restaurantReservation = reservation.getRestaurant();
        //TODO find by timing and allow change of maximum capacity
        if (reservationList.size() < restaurantReservation.getMaxReservationSlots()) { 
            var savedReservation = reservationRepo.saveAndFlush(reservation);
            return new ResponseEntity<>(savedReservation, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

   // PUT: Update reservation
   @PutMapping({"/id/{reservationId}"})
   @ResponseStatus(code = HttpStatus.OK)
   public ResponseEntity<Reservation> updateReservation(
       @PathVariable("reservationId") Integer id,
       @RequestBody Reservation updatedReservation
   ) {
       Optional<Reservation> currentReservationOpt = reservationRepo.findById(id);
       if (currentReservationOpt.isEmpty()) {
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
       var currentReservation = currentReservationOpt.get();
       currentReservation.setDate(updatedReservation.getDate());
       currentReservation.setIsVaccinated(updatedReservation.getIsVaccinated());
       currentReservation.setLineItems(updatedReservation.getLineItems());
       currentReservation.setPax(updatedReservation.getPax());
       currentReservation.setStatus(updatedReservation.getStatus());
       currentReservation = reservationRepo.saveAndFlush(currentReservation);
       return new ResponseEntity<>(currentReservation, HttpStatus.OK);
   }

   // DELETE: Delete reservation
   @DeleteMapping({"/id/{reservationId}"})
    public ResponseEntity<Reservation> deleteReservation(@PathVariable("reservationId") Integer id) {
        var reservation = reservationRepo.findById(id);
        
        if (reservation.isPresent()) {
            reservationRepo.delete(reservation.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        reservation = reservationRepo.findById(id);
        if (reservation.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // TODO: API TESTING
    // POST: Create new lineItems by reservationId
    public ResponseEntity<List<LineItem>> createLineItems(
        @PathVariable("reservationId") Integer id,
        @RequestBody List<LineItem> lineItems
    ) {
        Optional<Reservation> currentReservationOpt = reservationRepo.findById(id);
        if (currentReservationOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        var currentReservation = currentReservationOpt.get();
        if (currentReservation.getLineItems() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            currentReservation.setLineItems(lineItems);
            currentReservation = reservationRepo.saveAndFlush(currentReservation);
            return new ResponseEntity<>(currentReservation.getLineItems(), HttpStatus.OK);
        }
    }

    // GET: Get lineItems by reservationId
    @GetMapping({"/id/{reservationId}/lineItems"})
    @Operation(summary = "Get the list of lineItems under a reservationId")
    public ResponseEntity<List<LineItem>> getLineItems(@PathVariable("reservationId") Integer id) {
        Optional<Reservation> currentReservationOpt = reservationRepo.findById(id);
        if (currentReservationOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<LineItem> lineItems = currentReservationOpt.get().getLineItems();
        if (lineItems == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<List<LineItem>>(lineItems, HttpStatus.OK);
        }
    }

    // PUT: Update lineItems by reservationId
    @PutMapping({"/id/{reservationId}/lineItems"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Update the list of lineItems under a reservationId")
    public ResponseEntity<List<LineItem>> updateLineItems(
        @PathVariable("reservationId") Integer id,
        @RequestBody List<LineItem> updatedLineItems
    ) {
        Optional<Reservation> currentReservationOpt = reservationRepo.findById(id);
        if (currentReservationOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        var currentReservation = currentReservationOpt.get();
        List<LineItem> lineItems = currentReservation.getLineItems();
        if (lineItems == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            currentReservation.setLineItems(updatedLineItems);
            currentReservation = reservationRepo.saveAndFlush(currentReservation);
            return new ResponseEntity<>(currentReservation.getLineItems(), HttpStatus.OK);
        }
    }

    // DELETE: Delete lineItems by reservationId
    @DeleteMapping({"/id/{reservationId}/lineItems"})
    public ResponseEntity<List<LineItem>> deleteLineItems(@PathVariable("reservationId") Integer id) {
        Optional<Reservation> reservation = reservationRepo.findById(id);
        if (reservation.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            reservation.get().setLineItems(null);
        }

        reservation = reservationRepo.findById(id);
        if (reservation.isPresent() && reservation.get().getLineItems() == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
