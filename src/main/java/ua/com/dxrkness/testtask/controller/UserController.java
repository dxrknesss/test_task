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
            return ResponseEntity.ok(userService.findUserById(id));
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
        return ResponseEntity.ok(userService.findAllUsers());
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
        return ResponseEntity.ok("User was successfully added");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateUserFields(@PathVariable(name = "id") Long id,
                                                   @RequestBody Map<String, String> updatedUserFields) {
        User userToUpdate;
        try {
            userToUpdate = userService.findUserById(id);
            userService.updateUser(userToUpdate, updatedUserFields);
        }
        catch(NoSuchElementException | IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(String.format("User's №%d fields were successfully updated", id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User was deleted successfully");
    }
}
