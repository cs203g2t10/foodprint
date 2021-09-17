package foodprint.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import foodprint.backend.model.User;
import foodprint.backend.model.UserRepo;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private UserRepo repo;

    private PasswordEncoder passwordEncoder;
    
    @Autowired
    UserController(UserRepo repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    // POST: Create the user
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Creates a user account on Foodprint")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        Optional<User> existingUser = repo.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var savedUser = repo.saveAndFlush(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // GET: Get the user by ID
    @GetMapping({"/{id}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets a user account on Foodprint")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        Optional<User> user = repo.findById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }
}
