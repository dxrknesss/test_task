package ua.com.dxrkness.testtask.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.dxrkness.testtask.UserRepository;
import ua.com.dxrkness.testtask.entity.Address;
import ua.com.dxrkness.testtask.entity.User;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserService {
    private UserRepository userRepository;
    @Value("${min-user-age}")
    private int minimumUserAge;
    private ObjectMapper jsonObjectMapper = new ObjectMapper();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void validateUser(User user) throws IllegalArgumentException {
        var now = LocalDateTime.now();
        long userAge = ChronoUnit.YEARS.between(user.getBirthDate(), now);

        if (!user.getBirthDate().isBefore(now)) {
            throw new IllegalArgumentException("User birth date is illegal");
        }
        if (userAge <= minimumUserAge) {
            throw new IllegalArgumentException("Users with age lower than 18 are not allowed");
        }
        if (user.getPhoneNumber() != null && (user.getPhoneNumber().isBlank() || !isPossiblePhoneNumber(user.getPhoneNumber()))) {
            throw new IllegalArgumentException("User's phone number has illegal format");
        }
    }

    private boolean isPossiblePhoneNumber(String phoneNumber) {
        return PhoneNumberUtil.getInstance().isPossibleNumber(phoneNumber,
                Phonenumber.PhoneNumber.CountryCodeSource.UNSPECIFIED.name());
    }

    public User findUserById(Long id) throws NoSuchElementException {
        return userRepository.findById(id).orElseThrow();
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findAllUsersByBirthDateBetween(Map<String, String> inputDateTime) throws IllegalArgumentException {
        LocalDateTime from = LocalDateTime.parse(inputDateTime.get("from")),
                to = LocalDateTime.parse(inputDateTime.get("to"));

        if (to.isBefore(LocalDateTime.now()) && from.isBefore(to)) {
            return userRepository.findAllByBirthDateBetween(from, to);
        }
        throw new IllegalArgumentException("There was an error parsing birth dates");
    }

    public void addNewUser(User userToAdd) throws IllegalArgumentException {
        validateUser(userToAdd);
        userRepository.save(userToAdd);
    }

    private void changeFieldValue(Field field, Object objectToUpdateFieldOn, Object newValue) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(objectToUpdateFieldOn, newValue);
        field.setAccessible(false);
    }

    public void updateUser(User userToUpdate, ObjectNode updatedUserFields) throws IllegalArgumentException {
        var userClassFields = Arrays.stream(User.class.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, field -> field));

        updatedUserFields.fieldNames().forEachRemaining(updatedField -> {
            try {
                if (userClassFields.containsKey(updatedField)) {
                    Object newValue = updatedUserFields.get(updatedField).asText();
                    if (updatedField.equals("id")) {
                        return;
                    }
                    if (updatedField.equals("birthDate")) {
                        newValue = LocalDateTime.parse((String) newValue);
                    }
                    if (updatedField.equals("address")) {
                        newValue = jsonObjectMapper.treeToValue(updatedUserFields.get(updatedField), Address.class);
                        // set id as in the old user's object, so the new record won't be created in the DB
                        if (userToUpdate.getAddress() != null) {
                            ((Address)newValue).setId(userToUpdate.getAddress().getId());
                        }
                    }
                    changeFieldValue(userClassFields.get(updatedField), userToUpdate, newValue);
                }
            } catch (IllegalAccessException | JsonProcessingException e) {
                throw new IllegalArgumentException("There was an error while processing updated user fields!");
            }
        });
        validateUser(userToUpdate);

        userRepository.save(userToUpdate);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
