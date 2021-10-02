package foodprint.backend.service;

import java.util.Optional;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import foodprint.backend.dto.CreateReservationDTO;
import foodprint.backend.dto.LineItemDTO;
import foodprint.backend.model.Food;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.LineItemRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.Reservation.Status;
import foodprint.backend.exceptions.NotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReservationService {

    private ReservationRepo reservationRepo;
    private RestaurantService restaurantService;
    private LineItemRepo lineItemRepo;

    @Autowired
    ReservationService(ReservationRepo reservationRepo, RestaurantService restaurantService, LineItemRepo lineItemRepo) {
        this.reservationRepo = reservationRepo;
        this.restaurantService = restaurantService;
        this.lineItemRepo = lineItemRepo;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public boolean slotAvailable(Restaurant restaurant, LocalDateTime date) {
        // assumes that duration of slot is 1 hour
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
        Optional<Reservation> reservation = reservationRepo.findById(id);
        if (reservation.isEmpty()) {
            throw new NotFoundException("Reservation not found");
        }
        return reservation.get().getLineItems();
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
    public Reservation create(CreateReservationDTO req) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Restaurant restaurant = restaurantService.get(req.getRestaurantId());
        LocalDateTime dateOfReservation = req.getDate();
        LocalDateTime startTime = dateOfReservation.truncatedTo(ChronoUnit.HOURS);

        if (this.slotAvailable(restaurant, startTime)) {
            Reservation reservation = new Reservation();
            reservation.user(currentUser).date(dateOfReservation).pax(req.getPax()).isVaccinated(req.getIsVaccinated())
                    .reservedOn(LocalDateTime.now()).status(Status.ONGOING).restaurant(restaurant);

            List<LineItem> savedLineItems = new ArrayList<>();
            for (LineItemDTO lineItem : req.getLineItems()) {
                Food food = restaurantService.getFood(req.getRestaurantId(), lineItem.getFoodId());
                LineItem savedLineItem = new LineItem(food, reservation, lineItem.getQuantity());
                savedLineItems.add(savedLineItem);
            }
            reservation.lineItems(savedLineItems);
            return reservationRepo.saveAndFlush(reservation);
        } else {
            throw new NotFoundException("Slot not found");
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation update(Reservation oldReservation, Reservation reservation) {

        List<LineItem> oldLineItems = oldReservation.getLineItems();
        for (int i = 0; i < oldLineItems.size(); i++) {
            oldLineItems.remove(oldLineItems.get(i));
        }

        if (reservation.getStatus().equals(Status.CANCELLED)) {
            List<LineItem> newLineItems = reservation.getLineItems();
            for (int i = 0; i < newLineItems.size(); i++) {
                newLineItems.remove(newLineItems.get(i));
            }
        }
        reservation.changeReservationId(oldReservation.getReservationId());
        return reservationRepo.saveAndFlush(reservation);
        
    }

    public Reservation adminUpdate(Reservation reservation) {
        return reservationRepo.saveAndFlush(reservation);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
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
            } else { // assume output can only range from 0 to 59
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekendOpeningHour() + 1, 0);
            }
            endTime = localDate.atTime(restaurantReserved.getRestaurantWeekendClosingHour(),
                    restaurantReserved.getRestaurantWeekendClosingMinutes());
        } else {
            if (restaurantReserved.getRestaurantWeekdayOpeningMinutes() == 0) {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayOpeningHour(), 0);
            } else if (restaurantReserved.getRestaurantWeekdayOpeningMinutes() <= 30) {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayOpeningHour(), 30);
            } else {
                startTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayOpeningHour() + 1, 0);
            }
            endTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayClosingHour(),
                    restaurantReserved.getRestaurantWeekdayClosingMinutes());
        }

        while (!startTime.equals(endTime) && startTime.isBefore(endTime)) {
            if (this.slotAvailable(restaurantReserved, startTime)) {
                availableSlots.add(startTime);
            }
            startTime = startTime.plusMinutes(30);
        }
        return availableSlots;
    }

}
