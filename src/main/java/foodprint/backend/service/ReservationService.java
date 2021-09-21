package foodprint.backend.service;

import java.util.Optional;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import foodprint.backend.model.LineItem;
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

    @PreAuthorize("hasAnyAuthority('FP_USER')")
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

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation getReservationById(Long id) {
        Optional<Reservation> reservation = reservationRepo.findById(id);
        return reservation.orElseThrow(() -> new NotFoundException("Reservation not found"));
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<LineItem> getLineItemsByReservationId(Long id) {
        List<LineItem> lineItems = reservationRepo.findLineItemsByReservationId(id);
        return lineItems;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Reservation> getAllReservationSlots() {
        List<Reservation> reservationList = reservationRepo.findAll();
        return reservationList;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Reservation> getAllReservationByRestaurant(Restaurant restaurant) {
        List<Reservation> reservationList = reservationRepo.findByRestaurant(restaurant);
        return reservationList;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation create(Reservation reservation) {
        return reservationRepo.saveAndFlush(reservation);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation update(Reservation reservation) {
        return reservationRepo.saveAndFlush(reservation);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public void delete(Reservation reservation) {
        reservationRepo.delete(reservation);
        return;
    }
}
