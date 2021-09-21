package foodprint.backend.service;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import foodprint.backend.model.Restaurant;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.exceptions.NotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReservationService {

    private ReservationRepo reservationRepo;

    private RestaurantService restaurantService;

    @Autowired
    ReservationService(ReservationRepo reservationRepo, RestaurantService restaurantService) {
        this.reservationRepo = reservationRepo;
        this.restaurantService = restaurantService;
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

    public List<LocalDateTime> getAllAvailableSlotsByDateAndRestaurant(Long restaurantId, String date) {
        List<LocalDateTime> availableSlots = new ArrayList<LocalDateTime>();
        Restaurant restaurantReserved = restaurantService.get(restaurantId);

        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startTime;
        LocalDateTime endTime;
        if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            if (restaurantReserved.getRestaurantWeekendOpeningMinutes() == 0) {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekendOpeningHour(), 0);
            } else if (restaurantReserved.getRestaurantWeekendOpeningMinutes() <= 30) {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekendOpeningHour(), 30);
            } else { //assume output can only range from 0 to 59
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekendOpeningHour() + 1, 0);
            }
            endTime = localDate.atTime(restaurantReserved.getRestaurantWeekendClosingHour(), restaurantReserved.getRestaurantWeekendClosingMinutes());
        } else {
            if (restaurantReserved.getRestaurantWeekdayOpeningMinutes() == 0) {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayOpeningHour(), 0);
            } else if (restaurantReserved.getRestaurantWeekdayOpeningMinutes() <= 30) {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayOpeningHour(), 30);
            } else {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayOpeningHour() + 1, 0);
            }
            endTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayClosingHour(), restaurantReserved.getRestaurantWeekdayClosingMinutes());
        }

        while(!startTime.equals(endTime)  && startTime.isBefore(endTime)) {
            if (this.slotAvailable(restaurantReserved, startTime)) {
                availableSlots.add(startTime);
            }
            startTime = startTime.plusMinutes(30);
        }
        return availableSlots;
    }

    
}
