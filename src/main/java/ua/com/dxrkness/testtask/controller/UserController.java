package ua.com.dxrkness.testtask.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.com.dxrkness.testtask.entity.User;
import ua.com.dxrkness.testtask.service.UserService;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findUserById(@PathVariable(name = "id") Long id) {
        try {
            return new ResponseEntity<>(userService.findUserById(id), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> findAllUsers(@RequestBody(required = false) Map<String, String> inputDateTime) {
        try {
            if (inputDateTime != null) {
                return new ResponseEntity<>(userService.findAllUsersByBirthDateBetween(inputDateTime), HttpStatus.OK);
            }
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(userService.findAllUsers(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody @Valid User userToAdd, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                throw new IllegalArgumentException("Check the correctness of user data");
            }
            userService.addNewUser(userToAdd);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("User was successfully added", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
