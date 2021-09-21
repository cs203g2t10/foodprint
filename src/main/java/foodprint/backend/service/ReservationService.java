package foodprint.backend.service;

import java.util.Optional;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import foodprint.backend.dto.CreateReservationDTO;
import foodprint.backend.dto.LineItemDTO;
import foodprint.backend.model.Food;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.Restaurant;
import foodprint.backend.service.RestaurantService;
import foodprint.backend.model.LineItemRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.Reservation.Status;
import foodprint.backend.exceptions.InsufficientPermissionsException;
import foodprint.backend.exceptions.NotFoundException;

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

    public List<LineItem> getLineItemsByReservationId(Long id) {
        List<LineItem> lineItems = reservationRepo.findLineItemsByReservationId(id);
        return lineItems;
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
        Restaurant restaurant = reservation.getRestaurant();
        LocalDateTime dateOfReservation = reservation.getDate();
        LocalDateTime startTime = dateOfReservation.truncatedTo(ChronoUnit.HOURS);
        if (this.slotAvailable(restaurant, startTime)) {
            return reservationRepo.saveAndFlush(reservation);
        } else {
            throw new NotFoundException("Slot not found");
        }
    }

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
                lineItemRepo.saveAndFlush(savedLineItem);
            }
            reservation.lineItems(savedLineItems);
            return reservationRepo.saveAndFlush(reservation);
        } else {
            throw new NotFoundException("Slot not found");
        }
    }

    public Reservation update(Reservation reservation) {
        return reservationRepo.saveAndFlush(reservation);
    }

    public void delete(Reservation reservation) {
        reservationRepo.delete(reservation);
        return;
    }

    
}
