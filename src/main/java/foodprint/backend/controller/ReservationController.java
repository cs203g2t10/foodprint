package foodprint.backend.controller;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.dto.CreateReservationDTO;
import foodprint.backend.dto.LineItemDTO;
import foodprint.backend.model.Food;
import foodprint.backend.model.FoodRepo;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.LineItemRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.Restaurant;
import foodprint.backend.service.ReservationService;
import foodprint.backend.service.RestaurantService;
import foodprint.backend.model.User;
import foodprint.backend.model.Reservation.Status;
import foodprint.backend.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {

    private LineItemRepo lineItemRepo;

    private FoodRepo foodRepo;

    private ReservationService reservationService;

    private RestaurantService restaurantService;

    @Autowired
    ReservationController(LineItemRepo lineItemRepo,
            FoodRepo foodRepo, ReservationService reservationService, RestaurantService restaurantService) {
        this.lineItemRepo = lineItemRepo;
        this.foodRepo = foodRepo;
        this.reservationService = reservationService;
        this.restaurantService = restaurantService;
    }

    // GET: Get reservation by id
    @GetMapping({ "/{reservationId}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a reservation slot of a user")
    public ResponseEntity<Reservation> getReservation(@PathVariable("reservationId") Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return new ResponseEntity<>(reservation, HttpStatus.OK);

    }

    // GET: Get all reservations
    @GetMapping({ "/all" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all reservation slots")
    public ResponseEntity<List<Reservation>> getAllReservation() {
        List<Reservation> reservationList = reservationService.getAllReservationSlots();
        return new ResponseEntity<>(reservationList, HttpStatus.OK);
    }

    // POST: Create a new reservation
    @PostMapping({ "/admin" })
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a new reservation slot")
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        Restaurant restaurant = reservation.getRestaurant();
        LocalDateTime dateOfReservation = reservation.getDate();
        LocalDateTime startTime = dateOfReservation.truncatedTo(ChronoUnit.HOURS);

        if (reservationService.slotAvailable(restaurant, startTime)) {
            Reservation savedReservation = reservationService.create(reservation);
            return new ResponseEntity<>(savedReservation, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // POST: Create a new reservation (DTO)
    @PostMapping
    @Operation(summary = "Create a new reservation using DTO")
    public ResponseEntity<CreateReservationDTO> createReservationDTO(@RequestBody CreateReservationDTO req) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Restaurant restaurant = restaurantService.get(req.getRestaurantId());
        
        LocalDateTime dateOfReservation = req.getDate();
        LocalDateTime startTime = dateOfReservation.truncatedTo(ChronoUnit.HOURS);

        if (reservationService.slotAvailable(restaurant, startTime)) {
            Reservation reservation = new Reservation();
            reservation.user(currentUser).date(dateOfReservation).pax(req.getPax()).isVaccinated(req.getIsVaccinated())
                    .reservedOn(LocalDateTime.now()).status(Status.ONGOING).restaurant(restaurant);

            List<LineItem> savedLineItems = new ArrayList<>();
            for (LineItemDTO lineItem : req.getLineItems()) {
                // do we need a service?
                Optional<Food> foodOpt = foodRepo.findById(lineItem.getFoodId());
                if (foodOpt.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                Food food = foodOpt.get();
                if (!restaurant.getAllFood().contains(food)) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                LineItem savedLineItem = new LineItem(food, reservation, lineItem.getQuantity());
                savedLineItems.add(savedLineItem);
                lineItemRepo.saveAndFlush(savedLineItem);
            }
            reservation.lineItems(savedLineItems);
            reservationService.create(reservation);
            return new ResponseEntity<>(req, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // PUT: Update reservation
    @PutMapping({ "/{reservationId}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Update a reservation slot")
    public ResponseEntity<Reservation> updateReservation(@PathVariable("reservationId") Long id,
            @RequestBody Reservation updatedReservation) {

        Reservation currentReservation = reservationService.getReservationById(id);    
        currentReservation.setDate(updatedReservation.getDate());
        currentReservation.setIsVaccinated(updatedReservation.getIsVaccinated());
        currentReservation.setLineItems(updatedReservation.getLineItems());
        currentReservation.setPax(updatedReservation.getPax());
        currentReservation.setStatus(updatedReservation.getStatus());
        currentReservation = reservationService.update(currentReservation);
        return new ResponseEntity<>(currentReservation, HttpStatus.OK);
    }

    // DELETE: Delete reservation
    @DeleteMapping({ "/{reservationId}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Delete an existing reservation slot")
    public ResponseEntity<Reservation> deleteReservation(@PathVariable("reservationId") Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        reservationService.delete(reservation);

         try { 
            reservationService.getReservationById(id);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // TODO: API TESTING
    // POST: Create new lineItems by reservationId
    public ResponseEntity<List<LineItem>> createLineItems(@PathVariable("reservationId") Long id,
            @RequestBody List<LineItem> lineItems) {
        Reservation currentReservation = reservationService.getReservationById(id);    

        if (currentReservation.getLineItems() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            currentReservation.setLineItems(lineItems);
            currentReservation = reservationService.update(currentReservation);
            return new ResponseEntity<>(currentReservation.getLineItems(), HttpStatus.OK);
        }
    }

    // GET: Get lineItems by reservationId
    @GetMapping({ "/{reservationId}/lineItems" })
    @Operation(summary = "Get the list of lineItems under a reservationId")
    public ResponseEntity<List<LineItem>> getLineItems(@PathVariable("reservationId") Long id) {
        Reservation currentReservation = reservationService.getReservationById(id);    

        List<LineItem> lineItems = currentReservation.getLineItems();
        if (lineItems == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<List<LineItem>>(lineItems, HttpStatus.OK);
        }
    }

    // PUT: Update lineItems by reservationId
    @PutMapping({ "/{reservationId}/lineItems" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Update the list of lineItems under a reservationId")
    public ResponseEntity<List<LineItem>> updateLineItems(@PathVariable("reservationId") Long id,
            @RequestBody List<LineItem> updatedLineItems) {
        Reservation currentReservation = reservationService.getReservationById(id);    

        List<LineItem> lineItems = currentReservation.getLineItems();
        if (lineItems == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            currentReservation.setLineItems(updatedLineItems);
            currentReservation =  reservationService.update(currentReservation);
            return new ResponseEntity<>(currentReservation.getLineItems(), HttpStatus.OK);
        }
    }

    // DELETE: Delete lineItems by reservationId
    @DeleteMapping({ "/{reservationId}/lineItems" })
    public ResponseEntity<List<LineItem>> deleteLineItems(@PathVariable("reservationId") Long id) {
        Reservation reservation = reservationService.getReservationById(id);    
        reservation.setLineItems(null);
        reservationService.update(reservation);

        reservation = reservationService.getReservationById(id); 
        if (reservation.getLineItems() == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Get all reservations by a restaurant
    @GetMapping({ "/all/{restaurantId}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all reservation slots by restaurant")
    public ResponseEntity<List<Reservation>> getAllReservationByRestaurant(@PathVariable("restaurantId") Long id) {
        Restaurant restaurant = restaurantService.get(id);
        List<Reservation> reservationList = reservationService.getAllReservationByRestaurant(restaurant);
        return new ResponseEntity<>(reservationList, HttpStatus.OK);
    }

    // TODO
    // Get all available reservation slots by date (should return a list of date
    // objects (with the same date but different hour slots))
    @GetMapping({ "/slots/{restaurantId}/{date}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all available reservation slots by date")
    public ResponseEntity<List<LocalDateTime>> getAllAvailableSlotsByDateAndRestaurant(
            @PathVariable("restaurantId") Long id, @PathVariable("date") String date) {
        // Assume string date is in ISO format - 2021-19-14
        List<LocalDateTime> availableSlots = new ArrayList<LocalDateTime>();
        Restaurant restaurantReserved = restaurantService.get(id);
    
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startTime;
        LocalDateTime endTime;
        if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            startTime = localDate.atTime(restaurantReserved.getRestaurantWeekendOpening(), 0);
            endTime = localDate.atTime(restaurantReserved.getRestaurantWeekendClosing(), 0);
        } else {
            startTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayOpening(), 0);
            endTime = localDate.atTime(restaurantReserved.getRestaurantWeekdayClosing(), 0);
        }

        while (!startTime.equals(endTime)) {
            if (reservationService.slotAvailable(restaurantReserved, startTime)) {
                availableSlots.add(startTime);
            }
            startTime = startTime.plusHours(1); // iterate for every 1 hour
        }
        return new ResponseEntity<>(availableSlots, HttpStatus.OK);
    }

}
