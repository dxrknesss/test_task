package ua.com.dxrkness.testtask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.dxrkness.testtask.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByBirthDateBetween(LocalDateTime from, LocalDateTime to);
}
