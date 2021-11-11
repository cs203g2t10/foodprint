package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import foodprint.backend.dto.CreateReservationDTO;
import foodprint.backend.dto.LineItemDTO;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Food;
import foodprint.backend.model.LineItem;
import foodprint.backend.model.Reservation;
import foodprint.backend.model.ReservationRepo;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.User;
import foodprint.backend.model.Reservation.ReservationStatus;
import foodprint.backend.service.ReservationService;
import foodprint.backend.service.RestaurantService;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    
    @Mock
    private ReservationRepo reservations;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Restaurant restaurant;
    private Long restaurantId;
    private List<LineItem> lineItems;
    private Reservation reservation;
    private Long reservationId;
    private Food food;
    private Long foodId;
    private LineItem lineItem;
    private LineItemDTO lineItemDTO;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String date;
    private List<Reservation> reservationList;
    private List<String> restaurantCategories;

    @BeforeEach
    void init() {
        user = new User("bobbytan@gmail.com", "SuperSecurePassw0rd", "Bobby Tan");
        restaurantCategories = new ArrayList<>();
        restaurantCategories.add("Japanese");
        restaurantCategories.add("Rice");
        restaurant = new Restaurant("Sushi Tei", "Desc", "Serangoon", 15, 10, 10, 11, 11, 10, 10, 10, 10, restaurantCategories);
        restaurantId = 1L;
        lineItems = new ArrayList<LineItem>();
        reservation = new Reservation(user, LocalDateTime.now(), 5, true, LocalDateTime.now(), ReservationStatus.UNPAID, lineItems, restaurant);
        reservationId = 1L;
        food = new Food("sashimi", 10.0, 0.0);
        foodId = 1L;
        lineItem = new LineItem(food, reservation, 1);
        lineItems.add(lineItem);
        lineItemDTO = new LineItemDTO(foodId, 1);
        startTime = reservation.getDate();
        endTime = startTime.plusHours(1);
        reservationList = new ArrayList<>();
        reservationId = 1L;
    }

    @Test
    void viewSlotAvailability_SlotAvailable_ReturnTrue() {
        when(reservations.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);

        Boolean result = reservationService.slotAvailable(restaurant, reservation.getDate());

        assertEquals(true, result);
        verify(reservations).findByRestaurantAndDateBetween(restaurant, startTime, endTime);
    }

    @Test
    void viewSlotAvailability_SlotNotAvailable_ReturnFalse() {
        for (int i = 0; i < 15; i++) {
            reservationList.add(reservation);
        }
        when(reservations.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);

        Boolean result = reservationService.slotAvailable(restaurant, reservation.getDate());

        assertEquals(false, result);
        verify(reservations).findByRestaurantAndDateBetween(restaurant, startTime, endTime);
    }

    @Test
    void getReservation_IdExists_ReturnReservation() {
        when(reservations.findByReservationIdAndUserId(any(Long.class), any(Long.class))).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationByIdAndUser(reservationId, 0L);
        
        assertEquals(reservation, result);
        verify(reservations).findByReservationIdAndUserId(reservationId, 0L);
    }

    @Test
    void getReservation_IdDoesNotExist_ReturnException() {
        when(reservations.findByReservationIdAndUserId(any(Long.class), any(Long.class))).thenReturn(Optional.empty());

        String errorMsg = "";
        try {
            reservationService.getReservationByIdAndUser(reservationId, 0L);
        } catch (NotFoundException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Reservation not found", errorMsg);
        verify(reservations).findByReservationIdAndUserId(reservationId, 0L);
    }

    @Test
    void getAllReservationSlots_ReturnList() {
        reservationList.add(reservation);
        when(reservations.findAll()).thenReturn(reservationList);

        List<Reservation> result = reservationService.getAllReservationSlots();

        assertEquals(reservationList, result);
        verify(reservations).findAll();
    }
    
    @Test
    void getAllReservationByRestaurant_ReturnList() {
        reservationList.add(reservation);
        when(reservations.findByRestaurant(any(Restaurant.class))).thenReturn(reservationList);

        List<Reservation> result = reservationService.getAllReservationByRestaurant(restaurant);

        assertEquals(reservationList, result);
        verify(reservations).findByRestaurant(restaurant);
    }

    @Test
    void getAllReservationByUser_ReturnList() {
        reservationList.add(reservation);
        when(reservations.findByUser(any(User.class))).thenReturn(reservationList);

        List<Reservation> result = reservationService.getAllReservationByUser(user);

        assertEquals(reservationList, result);
        verify(reservations).findByUser(user);
    }

    @Test
    void getUserUpcomingReservations_UserFound_ReturnList() {
        reservation.setDate(LocalDateTime.now().plusDays(10));
        reservationList.add(reservation);
        when(reservations.findByUser(any(User.class))).thenReturn(reservationList);

        List<Reservation> result = reservationService.getUserUpcomingReservations(user);

        assertEquals(reservationList, result);
        verify(reservations).findByUser(user);
    }

    @Test
    void getUserPastReservations_UserFound_ReturnList() {
        reservationList.add(reservation);
        user.setReservations(reservationList);
        reservation.setDate(LocalDateTime.now().minusDays(10));
        when(reservations.findByUser(any(User.class))).thenReturn(reservationList);

        List<Reservation> result = reservationService.getUserPastReservations(user);

        assertEquals(reservationList, result);
        verify(reservations).findByUser(user);
    }

    @Test
    void createReservation_SlotAvailable_ReturnReservation() {
        List<LineItemDTO> lineItemDTOs = new ArrayList<>();
        lineItemDTOs.add(lineItemDTO);
        CreateReservationDTO req = new CreateReservationDTO(LocalDateTime.now(), 5, true, lineItemDTOs, restaurantId, ReservationStatus.UNPAID);

        when(restaurantService.get(any(Long.class))).thenReturn(restaurant);
        when(reservations.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);
        when(reservations.saveAndFlush(any(Reservation.class))).thenReturn(reservation);

        Reservation result = reservationService.create(user, req);

        assertEquals(reservation, result);
        verify(restaurantService).get(restaurantId);
        verify(reservations).findByRestaurantAndDateBetween(restaurant, startTime.truncatedTo(ChronoUnit.HOURS), endTime.truncatedTo(ChronoUnit.HOURS));
        verify(reservations).saveAndFlush(any(Reservation.class));
    }

    @Test
    void createReservation_SlotNotAvailable_ReturnException() {
        List<LineItemDTO> lineItemDTOs = new ArrayList<>();
        lineItemDTOs.add(lineItemDTO);
        CreateReservationDTO req = new CreateReservationDTO(LocalDateTime.now(), 5, true, lineItemDTOs, restaurantId, ReservationStatus.UNPAID);
        
        when(restaurantService.get(any(Long.class))).thenReturn(restaurant);
        for (int i = 0; i < 15; i++) {
            reservationList.add(reservation);
        }
        when(reservations.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);

        String errorMsg = "";
        try {
            reservationService.create(user, req);
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }

        assertNotEquals("", errorMsg);
        verify(restaurantService).get(restaurantId);
        verify(reservations).findByRestaurantAndDateBetween(restaurant, startTime.truncatedTo(ChronoUnit.HOURS), endTime.truncatedTo(ChronoUnit.HOURS));
    }

    @Test
    void updateReservation_SlotAvailable_ReturnReservation() {
        Reservation updatedReservation = new Reservation(user, LocalDateTime.now(), 3, true, LocalDateTime.now(), ReservationStatus.UNPAID, lineItems, restaurant);
        reservationList.add(reservation);
        when(reservations.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);
        when(reservations.getById(any(Long.class))).thenReturn(reservation);
        when(reservations.saveAndFlush(any(Reservation.class))).thenReturn(reservation);

        Reservation result = reservationService.update(reservationId, updatedReservation);

        assertEquals(reservation, result);
        verify(reservations).findByRestaurantAndDateBetween(restaurant, startTime.truncatedTo(ChronoUnit.HOURS), endTime.truncatedTo(ChronoUnit.HOURS));
        verify(reservations).getById(reservationId);
        verify(reservations).saveAndFlush(reservation);
    }

    @Test
    void updateReservation_SlotNotAvailable_ReturnException() {
        Reservation updatedReservation = new Reservation(user, LocalDateTime.now(), 3, true, LocalDateTime.now(), ReservationStatus.UNPAID, lineItems, restaurant);
        for (int i = 0; i < 15; i++) {
            reservationList.add(reservation);
        }
        when(reservations.findByRestaurantAndDateBetween(any(Restaurant.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(reservationList);

        String errorMsg = "";
        try {
            reservationService.update(reservationId, updatedReservation);
        } catch (NotFoundException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Slot not found", errorMsg);
        verify(reservations).findByRestaurantAndDateBetween(restaurant, startTime.truncatedTo(ChronoUnit.HOURS), endTime.truncatedTo(ChronoUnit.HOURS));
    }

    @Test
    void deleteReservation_ReservationIsNull_ReturnException() {
        String errorMsg = "";
        try {
            reservationService.deleteReservationById(null);
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }

        assertNotEquals("", errorMsg);
    }

    @Test
    void deleteReservation_ReservationNotNull_Success() {
        doNothing().when(reservations).deleteById(any(Long.class));
        String errorMsg = "";

        try {
            reservationService.deleteReservationById(reservationId);
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }
        
        assertEquals("", errorMsg);
        verify(reservations).deleteById(reservationId);
    }
}
