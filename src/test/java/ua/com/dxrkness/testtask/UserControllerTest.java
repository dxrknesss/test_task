package ua.com.dxrkness.testtask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.com.dxrkness.testtask.controller.UserController;
import ua.com.dxrkness.testtask.entity.Address;
import ua.com.dxrkness.testtask.entity.User;
import ua.com.dxrkness.testtask.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    ObjectMapper objectMapper;

    User user1, user2, user3;
    List<User> users;
    LocalDateTime from, to;
    String user1WithChangedFieldsJson = """
                {
                "email":"asdasdoij@telegram.com",
                "firstName":"Joseph",
                "lastName":"Dohn",
                "birthDate":"2004-04-25T18:02:36.574958915",
                "address":null,
                "phoneNumber":null
                }
                """;

    @BeforeEach
    public void setup() throws Exception {
        user1 = new User(
                1L, "somemail@rgam.com", "John", "Doe",
                LocalDateTime.now().minusYears(19),
                null, null
        );
        user2 = new User(
                2L, "janedoth@mail.org", "Jane", "Doth",
                LocalDateTime.now().minusYears(22),
                new Address(1L, null, "USA", "Ohio", "Columbus", "Sierra Ave."),
                "+123585964234"
        );
        user3 = new User(
                null, "somenewemail_34@mail.org", "Ralf", "Mane",
                LocalDateTime.now().minusYears(20),
                new Address(1L, null, "USA", "New Mexico", "Albuquerque", "Alle Ave."),
                "+123586931234"
        );
        users = new ArrayList<>(List.of(user1, user2));

        from = LocalDate.now().minusYears(22).minusDays(1).atStartOfDay();
        to = LocalDate.now().minusYears(20).minusDays(1).atStartOfDay();

        Mockito.when(userService.findUserById(1L)).thenReturn(user1);
        Mockito.when(userService.findUserById(2L)).thenReturn(user2);
        Mockito.when(userService.findAllUsers()).thenReturn(users);
        Mockito.when(userService.findAllUsersByBirthDateBetween(Map.of(
                "from", from.toString(),
                "to", to.toString()))
        ).thenReturn(
                List.of(users.stream().filter(user -> user.getBirthDate().isAfter(from)
                                && user.getBirthDate().isBefore(to))
                        .findAny().orElseThrow())
        );
        Mockito.doAnswer(mock -> users.add(user3)).when(userService).addNewUser(user3);
        Mockito.doAnswer(mock -> {
            user1.setFirstName(JsonPath.read(user1WithChangedFieldsJson, "$.firstName"));
            user1.setLastName(JsonPath.read(user1WithChangedFieldsJson, "$.lastName"));
            user1.setBirthDate(LocalDateTime.parse(JsonPath.read(user1WithChangedFieldsJson, "$.birthDate")));
            return null;
        }).when(userService).updateUser(user1, objectMapper.readValue(user1WithChangedFieldsJson, ObjectNode.class));
    }

    @Test
    public void whenGettingAllUsers_thenReturnUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(objectMapper.writeValueAsString(users))
                );
    }

    @Test
    public void whenGettingUserById_thenReturnUser() throws Exception {
        mockMvc.perform(get("/users/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(objectMapper.writeValueAsString(user1))
                );
    }

    @Test
    public void whenGettingUserByBirthDateRange_thenReturnUserWithBirthDateInRange() throws Exception {
        mockMvc.perform(get("/users")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                       {
                       "from": "%s",
                       "to": "%s"
                       }""", from, to))) // 2002-2004 years
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(objectMapper.writeValueAsString(List.of(user2)))
                );
    }

    @Test
    public void whenAddingNewUser_thenReturnOk_andItWasAdded() throws Exception {
        mockMvc.perform(post("/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );

        mockMvc.perform(get("/users")
                .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.[2]").exists() // if new user was added, last index will be 2
                );
    }

    @Test
    public void whenChangingUserFields_thenReturnOk_andTheyAreChanged() throws Exception {
        mockMvc.perform(patch("/users/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1WithChangedFieldsJson))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );

        InOrder inOrder = Mockito.inOrder(userService);
        inOrder.verify(userService).findUserById(1L);
        inOrder.verify(userService).updateUser(user1, objectMapper.readValue(user1WithChangedFieldsJson, ObjectNode.class));

        String firstName = JsonPath.read(user1WithChangedFieldsJson, "$.firstName"),
        lastName = JsonPath.read(user1WithChangedFieldsJson, "$.lastName"),
        birthDate = JsonPath.read(user1WithChangedFieldsJson, "$.birthDate");
        mockMvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.firstName", is(firstName)),
                        jsonPath("$.lastName", is(lastName)),
                        jsonPath("$.birthDate", is(birthDate))
                );
    }
}
