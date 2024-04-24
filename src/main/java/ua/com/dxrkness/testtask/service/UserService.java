package ua.com.dxrkness.testtask.service;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.dxrkness.testtask.UserRepository;
import ua.com.dxrkness.testtask.entity.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private UserRepository userRepository;
    @Value("${min-user-age}")
    private int minimumUserAge;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(Long id) throws NoSuchElementException {
        return userRepository.findById(id).orElseThrow();
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findAllUsersByBirthDateBetween(Map<String, String> inputDateTime) throws IllegalArgumentException {
        LocalDateTime from, to;
        from = LocalDateTime.parse(inputDateTime.get("from"));
        to = LocalDateTime.parse(inputDateTime.get("to"));
        if (to.isBefore(LocalDateTime.now()) && from.isBefore(to)) {
            return userRepository.findAllByBirthDateBetween(from, to);
        }
        throw new IllegalArgumentException("There was an error parsing birth dates");
    }

    public void addNewUser(User userToAdd) throws IllegalArgumentException {
        var now = LocalDateTime.now();
        if (userToAdd.getBirthDate().isBefore(now)) {
            long userAge = ChronoUnit.YEARS.between(userToAdd.getBirthDate(), now);
            if (userAge <= minimumUserAge) {
                throw new IllegalArgumentException("Users with age lower than 18 are not allowed");
            }

            if (userToAdd.getPhoneNumber() != null && !userToAdd.getPhoneNumber().isBlank()) {
                PhoneNumberUtil.getInstance().isPossibleNumber(userToAdd.getPhoneNumber(),
                        Phonenumber.PhoneNumber.CountryCodeSource.UNSPECIFIED.name());
            }
            userRepository.save(userToAdd);
            return;
        }
        throw new IllegalArgumentException("User birth date is illegal");
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
