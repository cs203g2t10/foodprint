package foodprint.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.config.AuthHelper;
import foodprint.backend.config.JwtTokenUtil;
import foodprint.backend.dto.AdminUserDTO;
import foodprint.backend.dto.ManagerRequestDTO;
import foodprint.backend.dto.RequestResetPwdDTO;
import foodprint.backend.dto.ResetPwdDTO;
import foodprint.backend.dto.UpdateUserDTO;
import foodprint.backend.dto.FavouriteRestaurantDTO;
import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.BadRequestException;
import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.exceptions.MailException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Restaurant;
import foodprint.backend.model.User;
import foodprint.backend.service.AuthenticationService;
import foodprint.backend.service.RestaurantService;
import foodprint.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private UserService userService;

    private RestaurantService restaurantService;

    private AuthenticationService authService;

    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    UserController(UserService userService, RestaurantService restaurantService, PasswordEncoder passwordEncoder, AuthenticationService authService, JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.authService = authService;
    }

    // POST: Create the user
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a user account on Foodprint")
    public ResponseEntity<User> createUser(@RequestBody @Valid AdminUserDTO userDTO) {
        User savedUser = convertToEntity(userDTO);
        return new ResponseEntity<>(userService.createUser(savedUser), HttpStatus.CREATED);
    }

    // GET: Get the user by ID
    @GetMapping({"/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a user account on Foodprint")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        User user = userService.getUser(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // GET: Get all users, paged version
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all users on Foodprint")
    public ResponseEntity<Page<User>> getAllUsers(
        @RequestParam(value="emailContains", required = false) String emailQuery,
        @RequestParam(value="page", defaultValue="0") Integer pageNumber,
        @RequestParam(value="sortBy", defaultValue="id") String sortByField,
        @RequestParam(value="sortDesc", defaultValue="false") boolean sortDesc
    ) {
        Direction direction = (sortDesc) ? Direction.DESC : Direction.ASC;
        Pageable pageDetails = PageRequest.of(pageNumber, 8, direction, sortByField);
        Page<User> respEntities = userService.searchUsers(pageDetails, emailQuery);
        return new ResponseEntity<>(respEntities, HttpStatus.OK);
    }

    // PATCH: Update a user via DTO
    @PatchMapping({"/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a user based on Foodprint")
    public ResponseEntity<UpdateUserDTO> updateUser(@PathVariable("id") Long id, @RequestBody @Valid UpdateUserDTO updatedUserDto) {
        User updatedUser = convertToEntity(updatedUserDto);
        User currentUser = userService.protectedGetUser(id);

        if (updatedUserDto.getNewPassword() != null) {
            try {
                authService.authenticate(new UsernamePasswordAuthenticationToken(currentUser.getEmail(), updatedUserDto.getOldPassword()));
            } catch (BadCredentialsException | UsernameNotFoundException ex) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        updatedUser = userService.updateUser(id, currentUser, updatedUser);
        String jwtToken = jwtTokenUtil.generateAccessToken(updatedUser);
        updatedUserDto = convertToDto(updatedUser);
        return ResponseEntity.ok().header("Authorization", jwtToken).body(updatedUserDto);
    }

    // PATCH: Update a user
    @PatchMapping({"/admin/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a user on Foodprint (Email, Password, First Name, Last Name, Roles)")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody @Valid AdminUserDTO updatedUserDTO) {
        User currentUser = userService.getUser(id);
        User updatedUser = convertToEntity(updatedUserDTO);
        return new ResponseEntity<>(userService.updateUser(id, currentUser, updatedUser), HttpStatus.OK);
    }

    @DeleteMapping({"/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes a user on Foodprint")
    public ResponseEntity<User> deleteUser(@PathVariable("id") Long id) {
        User currentUser = AuthHelper.getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("You cannot delete yourself.");
        }
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // POST: Make an existing user a Manager
    @PostMapping({"/manager"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Makes an existing user manager")
    public ResponseEntity<User> makeManager(@RequestBody ManagerRequestDTO requestDTO) {
        User user = userService.getUser(requestDTO.getUserId());
        if (user.getRoles().contains("FP_MANAGER")) {
            throw new BadRequestException("User is already a manager");
        }
        Restaurant restaurant = restaurantService.get(requestDTO.getRestaurantId());
        User updatedUser = new User();
        updatedUser.setRestaurant(restaurant);
        updatedUser.setRoles(user.getRoles() + ", FP_MANAGER");
        userService.updateUser(user.getId(), user, updatedUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Conversion from DTO to actual entity
    private User convertToEntity(UpdateUserDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(dto.getNewPassword());
        return user;
    }

    private UpdateUserDTO convertToDto(User user) {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }

    /*
    RESET PASSWORD POST METHOD
    */
    @PostMapping({ "auth/requestResetPwd" })
    @Operation(summary = "Raises a reset password request")
    public ResponseEntity<String> requestResetPwd(@RequestBody RequestResetPwdDTO requestResetPwdDTO) {
        String email = requestResetPwdDTO.getEmail();
        try {
            userService.requestPasswordReset(email);
            return new ResponseEntity<>("Email sent, check your inbox!", HttpStatus.CREATED);
        } catch (NotFoundException e) {
            return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
        } catch (MailException e) {
            return new ResponseEntity<>("Unknown error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping({ "auth/resetpwd/{token}" })
    @Operation(summary = "Checks validity of reset password token")
    public ResponseEntity<String> resetPwd(@PathVariable("token") String token) {
        try {
            userService.checkPasswordReset(token);
            return new ResponseEntity<>("Valid token", HttpStatus.OK);
        } catch (InvalidException e) {
            return new ResponseEntity<>("Invalid token", HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return new ResponseEntity<>("Token not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping({ "auth/resetpwd/{token}" })
    @Operation(summary = "Reset password")
    public ResponseEntity<String> doResetPwd(@Valid @RequestBody ResetPwdDTO userForForm, @PathVariable("token") String token) {
        String newPassword = userForForm.getPassword();
        try {
            userService.doPasswordReset(token, newPassword);
            return new ResponseEntity<>("Password successfully changed", HttpStatus.CREATED);
        } catch (InvalidException e) {
            return new ResponseEntity<>("Invalid token", HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return new ResponseEntity<>("Token not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping({ "favourite/{restaurantId}"})
    @Operation(summary = "Adds a restaurant to the current user's list of favourites")
    public ResponseEntity<String> favouriteRestaurant(@PathVariable("restaurantId") Long restaurantId) {
        User user = AuthHelper.getCurrentUser();
        try {
            userService.addFavouriteRestaurant(user, restaurantId);
            return new ResponseEntity<>("Restaurant successfully favourited.", HttpStatus.OK);
        } catch (AlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping({"favouriteRestaurants"})
    @Operation(summary = "Gets all favourited restaurants of the current user")
    public ResponseEntity<List<FavouriteRestaurantDTO>> getAllFavouriteRestaurants() {
        User user = AuthHelper.getCurrentUser();
        Set<Restaurant> restaurants = user.getFavouriteRestaurants();
        List<FavouriteRestaurantDTO> restaurantDtos = new ArrayList<>();
        for(Restaurant restaurant : restaurants) {
            FavouriteRestaurantDTO newFav = new FavouriteRestaurantDTO();
            newFav.setPicture(restaurant.getPicture());
            newFav.setRestaurantId(restaurant.getRestaurantId());
            newFav.setRestaurantName(restaurant.getRestaurantName());
            newFav.setRestaurantLocation(restaurant.getRestaurantLocation());
            restaurantDtos.add(newFav);
        }

        return new ResponseEntity<>(restaurantDtos, HttpStatus.OK);
    }

    @DeleteMapping({"favourite/{restaurantId}"})
    @Operation(summary = "Deletes a restaurant from the current user's list of favourites")
    public ResponseEntity<String> deleteFavouriteRestaurant(@PathVariable("restaurantId") Long restaurantId) {
        User user = AuthHelper.getCurrentUser();
        try {
            userService.deleteFavouriteRestaurant(user, restaurantId);
            return new ResponseEntity<>("Favourite restaurant successfully removed.", HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private User convertToEntity(AdminUserDTO userDTO) {
        ModelMapper mapper = new ModelMapper();
        return mapper.map(userDTO, User.class);
    }

}
