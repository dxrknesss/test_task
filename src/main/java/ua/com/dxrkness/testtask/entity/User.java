package ua.com.dxrkness.testtask.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "\"user\"") // since H2 has reserved user keyword
public class User {
    @Id
    @NotNull
    private Long id;
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String firstName;
    @NotNull
    @NotBlank
    private String lastName;
    @NotNull
    private LocalDateTime birthDate;
    @OneToOne(cascade = CascadeType.ALL)
    private Address address;
    private String phoneNumber;
}
