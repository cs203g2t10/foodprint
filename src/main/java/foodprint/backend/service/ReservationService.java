package foodprint.backend.service;

import java.util.Optional;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import foodprint.backend.dto.CreateReservationDTO;
import foodprint.backend.dto.LineItemDTO;
import foodprint.backend.model.Food;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.User;
import foodprint.backend.model.Reservation.ReservationStatus;
import foodprint.backend.exceptions.NotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReservationService {

    private ReservationRepo reservationRepo;
    private RestaurantService restaurantService;

    @Autowired
    ReservationService(ReservationRepo reservationRepo, RestaurantService restaurantService) {
        this.reservationRepo = reservationRepo;
        this.restaurantService = restaurantService;
    }

    /**
     * Checks if a slot is available for a given date and time
     * @param restaurant
     * @param date
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public boolean slotAvailable(Restaurant restaurant, LocalDateTime date) {
        // assumes that duration of slot is 1 hour
        LocalDateTime endTime = date.plusHours(1);
        List<Reservation> reservations = reservationRepo.findByRestaurantAndDateBetween(restaurant, date, endTime);
        return reservations.size() < restaurant.getRestaurantTableCapacity();
    }


    /**
     * Gets a reservation by its database ID
     * @param id
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation getReservationById(Long id) {
        Optional<Reservation> reservation = reservationRepo.findById(id);
        return reservation.orElseThrow(() -> new NotFoundException("Reservation not found"));
    }

    /**
     * Gets all reservations made by a particular user
     * @param user
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Reservation> getAllReservationByUser(User user) {
        return reservationRepo.findByUser(user);
    }

    /**
     * Gets all upcoming reservations for a given user
     * @param user
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Reservation> getUserUpcomingReservations(User user) {
        List<Reservation> reservationList = reservationRepo.findByUser(user);
        List<Reservation> result = new ArrayList<>();
        for(Reservation reservation : reservationList) {
            LocalDateTime date = reservation.getDate();
            if (date.isAfter(LocalDateTime.now())) {
                result.add(reservation);
            }
        }
        return result;
    }

    /**
     * Gets all past reservations for a given user
     * @param user
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<Reservation> getUserPastReservations(User user) {
        List<Reservation> reservationList = reservationRepo.findByUser(user);
        List<Reservation> result = new ArrayList<>();
        for(Reservation reservation : reservationList) {
            LocalDateTime date = reservation.getDate();
            if (date.isBefore(LocalDateTime.now())) {
                result.add(reservation);
            }
        }
        return result;
    }


    /**
     * Gets all line items under a reservation
     * @param id
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public List<LineItem> getLineItemsByReservationId(Long id) {
        Optional<Reservation> reservation = reservationRepo.findById(id);
        if (reservation.isEmpty()) {
            throw new NotFoundException("Reservation not found");
        }
        return reservation.get().getLineItems();
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public List<Reservation> getAllReservationSlots() {
        return reservationRepo.findAll();
    }

    @PreAuthorize("hasAnyAuthority('FP_MANAGER')")
    public List<Reservation> getAllReservationByRestaurant(Restaurant restaurant) {
        return reservationRepo.findByRestaurant(restaurant);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation create(User currentUser, CreateReservationDTO req) {

        Restaurant restaurant = restaurantService.get(req.getRestaurantId());
        LocalDateTime dateOfReservation = req.getDate();
        LocalDateTime startTime = dateOfReservation.truncatedTo(ChronoUnit.HOURS);

        if (!this.slotAvailable(restaurant, startTime)) {
            String msg = String.format("Slot not available for %s on %d %s %d at %d:%dHr", restaurant.getRestaurantName(), dateOfReservation.getDayOfMonth(), dateOfReservation.getMonth(), dateOfReservation.getYear(), dateOfReservation.getHour(), dateOfReservation.getMinute());
            throw new NotFoundException(msg);
        }

        Reservation reservation = new Reservation();
        reservation.user(currentUser)
                    .date(dateOfReservation)
                    .pax(req.getPax())
                    .isVaccinated(req.getIsVaccinated())
                    .reservedOn(LocalDateTime.now())
                    .status(ReservationStatus.ONGOING)
                    .restaurant(restaurant);

        HashMap<Food, Integer> lineItemsHashMap = new HashMap<>();

        // Prevent multiple line item entries of the same food
        for (LineItemDTO lineItemDTO : req.getLineItems()) {
            Food food = restaurantService.getFood(req.getRestaurantId(), lineItemDTO.getFoodId());
            if (lineItemsHashMap.containsKey(food)) {
                lineItemsHashMap.put(food, lineItemDTO.getQuantity() + lineItemsHashMap.get(food));
            } else {
                lineItemsHashMap.put(food, lineItemDTO.getQuantity());
            }
        }

        // Just to save
        List<LineItem> savedLineItems = new ArrayList<>();
        for (Map.Entry<Food, Integer> entry : lineItemsHashMap.entrySet()) {
            LineItem savedLineItem = new LineItem(entry.getKey(), reservation, entry.getValue());
            savedLineItems.add(savedLineItem);
        }

        reservation.lineItems(savedLineItems);
        return reservationRepo.saveAndFlush(reservation);
    }
    
    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation update(Long id, Reservation reservation) {

        LocalDateTime startTime = reservation.getDate().truncatedTo(ChronoUnit.HOURS);
        if (!this.slotAvailable(reservation.getRestaurant(), startTime)) {
            throw new NotFoundException("Slot not found");
        }

        Reservation currentReservation = reservationRepo.getById(id);

        if (reservation.getIsVaccinated() != null) {
            currentReservation.setIsVaccinated(reservation.getIsVaccinated());
        }

        if (reservation.getDate() != null) {
            currentReservation.setDate(reservation.getDate());
        }

        if (reservation.getLineItems() != null) {
            List<LineItem> currentLineItems = currentReservation.getLineItems();
            currentLineItems.clear();
            List<LineItem> newLineItems = reservation.getLineItems();
            for (LineItem li : newLineItems) {
                li.setReservation(currentReservation);
            }
            currentLineItems.addAll(newLineItems);
        }

        if (reservation.getPax() != null) {
            currentReservation.setPax(reservation.getPax());
        }

        if (reservation.getStatus() != null) {
            currentReservation.setStatus(reservation.getStatus());
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            List<LineItem> currentLineItems = currentReservation.getLineItems();
            currentLineItems.clear();
        }
        return reservationRepo.saveAndFlush(currentReservation);

    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public void delete(Reservation reservation) {
        reservationRepo.delete(reservation);
    }

    public List<LocalDateTime> getAllAvailableSlotsByDateAndRestaurant(Long restaurantId, String date) {
        List<LocalDateTime> availableSlots = new ArrayList<>();
        Restaurant restaurant = restaurantService.get(restaurantId);

        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startTime;
        LocalDateTime endTime;

        Integer openingHour = restaurant.getRestaurantWeekdayOpeningHour();
        Integer openingMinutes = restaurant.getRestaurantWeekdayOpeningMinutes();
        Integer closingHour = restaurant.getRestaurantWeekdayClosingHour();
        Integer closingMinutes = restaurant.getRestaurantWeekdayClosingMinutes();
        
        if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            openingHour = restaurant.getRestaurantWeekendOpeningHour();
            openingMinutes = restaurant.getRestaurantWeekendOpeningMinutes();
            closingHour = restaurant.getRestaurantWeekendClosingHour();
            closingMinutes = restaurant.getRestaurantWeekendClosingMinutes();
        }

        if (openingMinutes == 0) {
            startTime = localDate.atTime(openingHour, 0);
        } else if (openingMinutes <= 30) {
            startTime = localDate.atTime(openingHour, 30);
        } else {
            startTime = localDate.atTime(openingHour + 1, 0);
        }

        endTime = localDate.atTime(closingHour, closingMinutes);

        // As long as there is time left, then insert slot
        while (!startTime.equals(endTime) && startTime.isBefore(endTime)) {
            if (this.slotAvailable(restaurant, startTime)) {
                availableSlots.add(startTime);
            }
            startTime = startTime.plusMinutes(30);
        }

        return availableSlots;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public void setPaid(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setStatus(ReservationStatus.PAID);
        reservationRepo.saveAndFlush(reservation);
    }


}
