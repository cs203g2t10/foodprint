package foodprint.backend.controller;



import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.dto.UpdateUserDTO;
import foodprint.backend.dto.RequestResetPwdDTO;
import foodprint.backend.dto.ResetPwdDTO;
import foodprint.backend.exceptions.BadRequestException;
import foodprint.backend.exceptions.InvalidException;
import foodprint.backend.exceptions.MailException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.User;
import foodprint.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    // POST: Create the user
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a user account on Foodprint")
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        User savedUser = userService.createUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // GET: Get the user by ID
    @GetMapping({"/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a user account on Foodprint")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        User user = userService.getUser(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // // GET: Get all users
    // @GetMapping
    // @ResponseStatus(code = HttpStatus.OK)
    // @Operation(summary = "Gets all users on Foodprint")
    // public ResponseEntity<List<User>> getAllUsers() {
    //     List<User> users = userService.getAllUsers();
    //     return new ResponseEntity<>(users, HttpStatus.OK);
    // }

    // GET: Get all users, paged version
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all users on Foodprint")
    public ResponseEntity<Page<User>> getAllUsers(
        @RequestParam(value="emailContains", required = false) String emailQuery,
        @RequestParam(value="page", defaultValue="0") Integer pageNumber,
        @RequestParam(value="sortBy", defaultValue="id") String sortByField,
        @RequestParam(value="sortDesc", defaultValue="false") Boolean sortDesc
    ) {
        Direction direction = (sortDesc) ? Direction.DESC : Direction.ASC;
        Pageable pageDetails = PageRequest.of(pageNumber, 10, direction, sortByField);
        Page<User> respEntities = userService.searchUsers(pageDetails, emailQuery);
        return new ResponseEntity<>(respEntities, HttpStatus.OK);
    }

    // PATCH: Update a user via DTO
    @PatchMapping({"/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a user based on Foodprint")
    public ResponseEntity<UpdateUserDTO> updateUser(@PathVariable("id") Long id, @RequestBody @Valid UpdateUserDTO updatedUserDto) {
        User updatedUser = convertToEntity(id, updatedUserDto);
        User currentUser = userService.getUser(id);
        updatedUser = userService.updateUser(id, currentUser, updatedUser);
        updatedUserDto = convertToDto(updatedUser);
        return new ResponseEntity<>(updatedUserDto, HttpStatus.OK);
    }

    // PATCH: Update a user
    @PatchMapping({"/admin/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Updates a user on Foodprint (Email, Password, First Name, Last Name, Roles)")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody @Valid User updatedUser) {
        User currentUser = userService.getUser(id);
        updatedUser = userService.updateUser(id, currentUser, updatedUser);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping({"/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Deletes a user on Foodprint")
    public ResponseEntity<User> deleteUser(@PathVariable("id") Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getId() == id) {
            throw new BadRequestException("You cannot delete yourself.");
        }
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Conversion from DTO to actual entity
    private User convertToEntity(Long id, UpdateUserDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(dto.getPassword());
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
}
