package ua.com.dxrkness.testtask.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.dxrkness.testtask.UserRepository;
import ua.com.dxrkness.testtask.entity.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findAllUsersByBirthDateBetween(Map<String, String> inputDateTime) {
        LocalDateTime from, to;
        from = LocalDateTime.parse(inputDateTime.get("from"));
        to = LocalDateTime.parse(inputDateTime.get("to"));
        if (to.isBefore(LocalDateTime.now()) && from.isBefore(to)) {
            return userRepository.findAllByBirthDateBetween(from, to);
        }
        throw new IllegalArgumentException("There was an error parsing birth dates");
    }
}
