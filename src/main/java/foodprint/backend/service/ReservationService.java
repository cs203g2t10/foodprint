package foodprint.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import foodprint.backend.model.Restaurant;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Reservation;

import java.util.List;

import java.time.LocalDateTime;

@Service
public class ReservationService {

    private ReservationRepo reservationRepo;

    @Autowired
    ReservationService(ReservationRepo reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    public boolean slotAvailable(Restaurant restaurant, LocalDateTime date) {
        //assumes that duration of slot is 1 hour
        LocalDateTime endTime = date.plusHours(1); 
        List<Reservation> reservations = reservationRepo.findByRestaurantAndDateBetween(restaurant, date, endTime);
        if (reservations.size() < restaurant.getRestaurantTableCapacity()) {
            return true;
        } else {
            return false;
        }
    }
}
