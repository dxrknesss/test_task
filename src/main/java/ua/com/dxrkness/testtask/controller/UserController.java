package ua.com.dxrkness.testtask.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.dxrkness.testtask.UserRepository;
import ua.com.dxrkness.testtask.entity.User;
import ua.com.dxrkness.testtask.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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
}
