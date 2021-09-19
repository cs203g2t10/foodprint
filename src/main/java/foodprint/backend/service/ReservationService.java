package foodprint.backend.service;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import foodprint.backend.model.Restaurant;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.exceptions.NotFoundException;

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

    public Reservation getReservationById(Long id) {
        Optional<Reservation> reservation = reservationRepo.findById(id);
        return reservation.orElseThrow(() -> new NotFoundException("Reservation not found"));
    }

    public List<Reservation> getAllReservationSlots() {
        List<Reservation> reservationList = reservationRepo.findAll();
        return reservationList;
    }

    public List<Reservation> getAllReservationByRestaurant(Restaurant restaurant) {
        List<Reservation> reservationList = reservationRepo.findByRestaurant(restaurant);
        return reservationList;
    }

    public Reservation create(Reservation reservation) {
        return reservationRepo.saveAndFlush(reservation);
    }

    public Reservation update(Reservation reservation) {
        return reservationRepo.saveAndFlush(reservation);
    }

    public void delete(Reservation reservation) {
        reservationRepo.delete(reservation);
        return;
    }

    
}
