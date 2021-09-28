package foodprint.backend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import foodprint.backend.dto.ReservationDTO;
import foodprint.backend.dto.NamedLineItemDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Food;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.LineItemRepo;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.Restaurant;
import foodprint.backend.service.ReservationService;
import foodprint.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {

    private LineItemRepo lineItemRepo;

    private ReservationService reservationService;

    private RestaurantService restaurantService;

    @Autowired
    ReservationController(LineItemRepo lineItemRepo, 
    ReservationService reservationService, RestaurantService restaurantService) {
        this.lineItemRepo = lineItemRepo;
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

    // POST: Create a new reservation (DTO)
    @PostMapping
    @Operation(summary = "For users to create a new reservation slot")
    public ResponseEntity<ReservationDTO> createReservationDTO(@RequestBody CreateReservationDTO req) {
        var reservation = reservationService.create(req);
        var reservationDTO = this.convertToDTO(reservation);
        return new ResponseEntity<>(reservationDTO, HttpStatus.CREATED);
    }

    // PUT: Update reservation (DTO)
    @PutMapping({"/{reservationId}"})
    @Operation(summary = "For users to update an existing reservation slot")
    public ResponseEntity<ReservationDTO> updateReservationDTO(@PathVariable("reservationId") Long id, 
            @RequestBody CreateReservationDTO pendingReservationDTO) {

        var reservation = this.convertToEntity(pendingReservationDTO);
        var restaurant = restaurantService.get(pendingReservationDTO.getRestaurantId());
        reservation.setRestaurant(restaurant);
        reservation.changeReservationId(id);
        var updatedReservation = reservationService.update(reservation);
        var updatedReservationDTO = this.convertToDTO(updatedReservation);
        return new ResponseEntity<>(updatedReservationDTO, HttpStatus.OK);
    }

    // GET: Get all reservations
    @GetMapping({ "/admin/all" })
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all reservation slots")
    public ResponseEntity<List<Reservation>> getAllReservation() {
        List<Reservation> reservationList = reservationService.getAllReservationSlots();
        return new ResponseEntity<>(reservationList, HttpStatus.OK);
    }


    // PUT: Update reservation
    @PutMapping({ "/admin/{reservationId}" })
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

    // TODO: Convert to take in LineItemDTO and return NamedLineItemDTO
    // POST: Create new lineItems by reservationId
    @PostMapping({ "/{reservationId}/lineItems" })
    @Operation
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

    // // GET: Get lineItems by reservationId
    // @GetMapping({ "/{reservationId}/lineItems" })
    // @Operation(summary = "Get the list of lineItems under a reservationId")
    // public ResponseEntity<List<LineItem>> getLineItems(@PathVariable("reservationId") Long id) {
    //     List<LineItem> lineItems = reservationService.getLineItemsByReservationId(id);
    //     return new ResponseEntity<List<LineItem>>(lineItems, HttpStatus.OK);
    // }

    // TODO: Convert to take in LineItemDTO and return NamedLineItemDTO
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
        List<LineItem> lineItems = reservation.getLineItems();
        for (LineItem lineItem : lineItems) {
            lineItemRepo.delete(lineItem);
        }
        reservation.setLineItems(null);
        reservationService.update(reservation);
        reservation = reservationService.getReservationById(id); 
        if (reservation.getLineItems() == null) {
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
        return new ResponseEntity<>(reservationService.getAllAvailableSlotsByDateAndRestaurant(id, date), HttpStatus.OK);
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
            List<LineItem> lineItems = new ArrayList<LineItem>();
            for (LineItemDTO lineItemDto : dto.getLineItems()) {
                LineItem lineItem = new LineItem();
                Long foodId = lineItemDto.getFoodId();
                Food food = restaurantService.getFood(restaurantId, foodId);
                lineItem.setFood(food);
                lineItem.setQuantity(lineItemDto.getQuantity());
            }
            reservation.setLineItems(lineItems);
        }

        return reservation;
    }
}
