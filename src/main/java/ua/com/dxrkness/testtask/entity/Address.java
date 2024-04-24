package ua.com.dxrkness.testtask.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Address {
    @Id
    private Long id;
    @OneToOne(mappedBy = "address")
    private User user;
    private String country;
    private String province;
    private String city;
    private String homeAddress;
}
