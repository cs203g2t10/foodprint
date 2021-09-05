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
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {
    
    private ReservationRepo reservationRepo;

    @Autowired
    ReservationController(ReservationRepo reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    //get reservation by id
    @GetMapping({"/id/{reservationId}"})
    public ResponseEntity<Reservation> getReservation(@PathVariable("resrvationId") Integer id) {
        Optional<Reservation> reservation = reservationRepo.findById(id);
        if (reservation.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(reservation.get(), HttpStatus.OK);
        }
    }

    //get all reservations
    @GetMapping({"/all"})
    public ResponseEntity<List<Reservation>> getAllReservation() {
        List<Reservation> reservationList = reservationRepo.findAll();
        return new ResponseEntity<>(reservationList, HttpStatus.OK);
    }

    //create a new reservation
    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        Date date = reservation.getDate();
        List<Reservation> reservationList = reservationRepo.findByDate(date);
        //TODO find by timing and allow change of maximum capacity
        if (reservationList.size() < 15) { 
            var savedReservation = reservationRepo.saveAndFlush(reservation);
            return new ResponseEntity<>(savedReservation, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

   //update reservation
   @PutMapping({"/id/{reservationId}"})
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
       currentReservation.setOrder(updatedReservation.getOrder());
       currentReservation.setPax(updatedReservation.getPax());
       currentReservation.setStatus(updatedReservation.getStatus());
       currentReservation = reservationRepo.saveAndFlush(currentReservation);
       return new ResponseEntity<>(currentReservation, HttpStatus.OK);
   }

   //delete reservation
   @DeleteMapping({"/id/{reservationId}"})
    public ResponseEntity<Reservation> deleteFood(@PathVariable("reservationId") Integer id) {
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

   
}
