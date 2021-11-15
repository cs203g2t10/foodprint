package foodprint.backend.service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import foodprint.backend.exceptions.InvalidException;
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
    @PreAuthorize("hasAnyAuthority('FP_USER', 'FP_MANAGER', 'FP_ADMIN')")
    public Reservation getReservationByIdAndUser(Long id, Long userId) {
        return reservationRepo
            .findByReservationIdAndUserId(id, userId)
            .orElseThrow(
                () -> new NotFoundException("Reservation not found")
            );
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
     * Gets the upcoming reservations for a restaurant between two dates
     * @param restaurant
     * @param startDate
     * @param endDate
     * @param pageNumber
     * @return
     */
    @PreAuthorize("hasAnyAuthority('FP_ADMIN') or (hasAnyAuthority('FP_MANAGER') and #restaurant.restaurantId == #requestorRestaurant.restaurantId)")
    public Page<Reservation> getRestaurantUpcomingReservations(Restaurant restaurant, Restaurant requestorRestaurant, LocalDateTime after, LocalDateTime before, int pageNumber) {
        Pageable pageReq = PageRequest.of(pageNumber, 6);
        return reservationRepo.findByRestaurantAndDateBetween(pageReq, restaurant, after, before);
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public List<Reservation> getAllReservationSlots() {
        return reservationRepo.findAll();
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public List<Reservation> getAllReservationByRestaurant(Restaurant restaurant) {
        return reservationRepo.findByRestaurant(restaurant);
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Reservation create(User currentUser, CreateReservationDTO req) {

        Restaurant restaurant = restaurantService.get(req.getRestaurantId());
        LocalDateTime dateOfReservation = req.getDate();
        LocalDateTime startTime = dateOfReservation.truncatedTo(ChronoUnit.HOURS);
        List<LocalDateTime> upcomingSlots = getUpcomingSlots(restaurant.getRestaurantId());

        if (!upcomingSlots.contains(startTime)) {
            String msg = String.format("Slot not available for %s on %d %s %d at %d:%dHr", restaurant.getRestaurantName(), dateOfReservation.getDayOfMonth(), dateOfReservation.getMonth(), dateOfReservation.getYear(), dateOfReservation.getHour(), dateOfReservation.getMinute());
            throw new NotFoundException(msg);
        }

        Reservation reservation = new Reservation();
        reservation.user(currentUser)
                    .date(dateOfReservation)
                    .pax(req.getPax())
                    .isVaccinated(req.getIsVaccinated())
                    .reservedOn(LocalDateTime.now())
                    .status(ReservationStatus.UNPAID)
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
    
    @PreAuthorize("hasAnyAuthority('FP_USER', 'FP_ADMIN')")
    public Reservation update(Long id, Reservation reservation) {

        Reservation currentReservation = reservationRepo.getById(id);

        if (reservation.getDate() != null) {
            LocalDateTime startTime = reservation.getDate().truncatedTo(ChronoUnit.HOURS);
            if (!this.slotAvailable(reservation.getRestaurant(), startTime)) {
                throw new NotFoundException("Slot not found");
            }
            currentReservation.setDate(reservation.getDate());
        }

        if (reservation.getIsVaccinated() != null) {
            currentReservation.setIsVaccinated(reservation.getIsVaccinated());
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

    @PreAuthorize("hasAnyAuthority('FP_ADMIN')")
    public void deleteReservationById(Long reservationId) {
        if (reservationId == null) {
            throw new InvalidException("reservationId cannot be null");
        }
        reservationRepo.deleteById(reservationId);
    }

    public List<LocalDateTime> getUpcomingSlots(Long restaurantId) {
        List<LocalDateTime> availableSlots = new ArrayList<>();
        Restaurant restaurant = restaurantService.get(restaurantId);

        LocalDate currentDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(30);

        List<Reservation> reservations = reservationRepo.findByRestaurantAndDateBetween(
            restaurant, 
            currentDate.atStartOfDay(), 
            endDate.atStartOfDay()
        );

        while (currentDate.isBefore(endDate)) {
            LocalDateTime startTime;
            LocalDateTime endTime;

            Integer openingHour = restaurant.getRestaurantWeekdayOpeningHour();
            Integer openingMinutes = restaurant.getRestaurantWeekdayOpeningMinutes();
            Integer closingHour = restaurant.getRestaurantWeekdayClosingHour();
            Integer closingMinutes = restaurant.getRestaurantWeekdayClosingMinutes();
            
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                openingHour = restaurant.getRestaurantWeekendOpeningHour();
                openingMinutes = restaurant.getRestaurantWeekendOpeningMinutes();
                closingHour = restaurant.getRestaurantWeekendClosingHour();
                closingMinutes = restaurant.getRestaurantWeekendClosingMinutes();
            }

            if (openingMinutes == 0) {
                startTime = currentDate.atTime(openingHour, 0);
            } else if (openingMinutes <= 30) {
                startTime = currentDate.atTime(openingHour, 30);
            } else {
                startTime = currentDate.atTime(openingHour + 1, 0);
            }

            endTime = currentDate.atTime(closingHour, closingMinutes);

            // As long as there is time left, then insert slot
            while (!startTime.equals(endTime) && startTime.isBefore(endTime)) {
                final LocalDateTime startDateTime = startTime;
                long currentReservations = reservations.stream().filter(res -> 
                    res.getDate().equals(startDateTime)
                ).count();
                long capacity = restaurant.getRestaurantTableCapacity();
                if (currentReservations < capacity) {
                    availableSlots.add(startTime);
                }
                startTime = startTime.plusMinutes(30);
            }

            currentDate = currentDate.plusDays(1);
        }

        return availableSlots;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public void setPaid(Long reservationId, Long userId) {
        Reservation reservation = getReservationByIdAndUser(reservationId, userId);
        reservation.setStatus(ReservationStatus.PAID);
        reservationRepo.saveAndFlush(reservation);
    }


}
