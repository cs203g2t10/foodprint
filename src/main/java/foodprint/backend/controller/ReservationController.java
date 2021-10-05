package foodprint.backend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.dto.CreateReservationDTO;
import foodprint.backend.dto.LineItemDTO;
import foodprint.backend.dto.ReservationDTO;
import foodprint.backend.dto.NamedLineItemDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Food;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.User;
import foodprint.backend.service.ReservationService;
import foodprint.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {

    private ReservationService reservationService;

    private RestaurantService restaurantService;

    @Autowired
    ReservationController(ReservationService reservationService, RestaurantService restaurantService) {
        this.reservationService = reservationService;
        this.restaurantService = restaurantService;
    }

    // GET: Get reservation by id (DTO)
    @GetMapping({ "/{reservationId}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a reservation slot of a user")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable("reservationId") Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        ReservationDTO reservationDTO = this.convertToDTO(reservation);
        return new ResponseEntity<>(reservationDTO, HttpStatus.OK);
    }

    // GET: Get reservation by user
    @GetMapping({ "/all" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all the reservation(s) of a user")
    public ResponseEntity<List<ReservationDTO>> getAllReservationByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Reservation> reservations = reservationService.getAllReservationByUser(user);
        List<ReservationDTO> reservationDTOs = new ArrayList<>();
        for (Reservation reservation : reservations) {
            ReservationDTO reservationDTO = this.convertToDTO(reservation);
            reservationDTOs.add(reservationDTO);
        }
        return new ResponseEntity<>(reservationDTOs, HttpStatus.OK);
    }

    // GET: Get all reservations
    @GetMapping({ "/admin/all" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all reservation slots")
    public ResponseEntity<List<Reservation>> getAllReservation() {
        List<Reservation> reservationList = reservationService.getAllReservationSlots();
        return new ResponseEntity<>(reservationList, HttpStatus.OK);
    }

    // POST: Create a new reservation (DTO)
    @PostMapping
    @Operation(summary = "For users to create a new reservation slot")
    public ResponseEntity<ReservationDTO> createReservationDTO(@RequestBody CreateReservationDTO req) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            var reservation = reservationService.create(currentUser, req);
            var reservationDTO = this.convertToDTO(reservation);
            return new ResponseEntity<>(reservationDTO, HttpStatus.CREATED);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_ACCEPTABLE);
        }

    }

    // PUT: Update reservation (DTO)
    @PatchMapping({ "/{reservationId}" })
    @Operation(summary = "For users to update an existing reservation slot")
    public ResponseEntity<ReservationDTO> updateReservationDTO(@PathVariable("reservationId") Long id,
            @RequestBody CreateReservationDTO pendingReservationDTO) {

        var currentReservation = reservationService.getReservationById(id);
        var restaurant = currentReservation.getRestaurant();

        // Dont allow users to change the restaurant of their reservation
        pendingReservationDTO.setRestaurantId(restaurant.getRestaurantId());
        var reservation = this.convertToEntity(pendingReservationDTO);
        reservation.setRestaurant(restaurant);
        var updatedReservation = reservationService.update(id, reservation);
        var updatedReservationDTO = this.convertToDTO(updatedReservation);
        return new ResponseEntity<>(updatedReservationDTO, HttpStatus.OK);
    }

    // DELETE: Delete reservation
    @DeleteMapping({ "/admin/{reservationId}" })
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

    // Get all reservations by a restaurant
    @GetMapping({ "/admin/all/{restaurantId}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all reservation slots by restaurant")
    public ResponseEntity<List<Reservation>> getAllReservationByRestaurant(@PathVariable("restaurantId") Long id) {
        Restaurant restaurant = restaurantService.get(id);
        List<Reservation> reservationList = reservationService.getAllReservationByRestaurant(restaurant);
        return new ResponseEntity<>(reservationList, HttpStatus.OK);
    }

    // Get all available reservation slots by date (should return a list of date
    // objects (with the same date but different hour slots))
    @GetMapping({ "/slots/{restaurantId}/{date}" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all available reservation slots by date")
    public ResponseEntity<List<LocalDateTime>> getAllAvailableSlotsByDateAndRestaurant(
            @PathVariable("restaurantId") Long id, @PathVariable("date") String date) {
        // Assume string date is in ISO format - 2021-19-14
        return new ResponseEntity<>(reservationService.getAllAvailableSlotsByDateAndRestaurant(id, date),
                HttpStatus.OK);
    }

    // Reservation to CreatedReservationDTO
    public ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setDate(reservation.getDate());
        reservationDTO.setIsVaccinated(reservation.getIsVaccinated());
        reservationDTO.setRestaurantName(reservation.getRestaurant().getRestaurantName());
        reservationDTO.setRestaurantId(reservation.getRestaurant().getRestaurantId());
        reservationDTO.setPax(reservation.getPax());
        reservationDTO.setReservationId(reservation.getReservationId());
        reservationDTO.setStatus(reservation.getStatus());

        List<NamedLineItemDTO> lineItemDtos = new ArrayList<>();
        for (LineItem lineItem : reservation.getLineItems()) {
            NamedLineItemDTO lineItemDto = new NamedLineItemDTO();
            lineItemDto.setFoodId(lineItem.getFood().getFoodId());
            lineItemDto.setFoodName(lineItem.getFood().getFoodName());
            lineItemDto.setQuantity(lineItem.getQuantity());
            lineItemDtos.add(lineItemDto);
        }

        reservationDTO.setLineItems(lineItemDtos);
        return reservationDTO;
    }

    // CreateReservationDTO to Reservation
    public Reservation convertToEntity(CreateReservationDTO dto) {
        Reservation reservation = new Reservation();
        Restaurant restaurant = restaurantService.get(dto.getRestaurantId());
        Long restaurantId = restaurant.getRestaurantId();

        reservation.setDate(dto.getDate());
        reservation.setPax(dto.getPax());
        reservation.setIsVaccinated(dto.getIsVaccinated());
        reservation.setStatus(dto.getStatus());

        if (dto.getLineItems() != null) {
            HashMap<Food, Integer> lineItemsHashMap = new HashMap<>();
            for (LineItemDTO lineItemDTO : dto.getLineItems()) {
                Food food = restaurantService.getFood(restaurantId, lineItemDTO.getFoodId());
                if (lineItemsHashMap.containsKey(food)) {
                    lineItemsHashMap.put(food, lineItemDTO.getQuantity() + lineItemsHashMap.get(food));
                } else {
                    lineItemsHashMap.put(food, lineItemDTO.getQuantity());
                }
            }
            List<LineItem> savedLineItems = new ArrayList<>();
            for (Food key : lineItemsHashMap.keySet()) {
                LineItem savedLineItem = new LineItem(key, reservation, lineItemsHashMap.get(key));
                savedLineItems.add(savedLineItem);
            }
            reservation.setLineItems(savedLineItems);
        } else {
            reservation.setLineItems(null);
        }

        return reservation;
    }
}
