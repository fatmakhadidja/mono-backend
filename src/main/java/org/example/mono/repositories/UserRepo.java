package org.example.mono.repositories;

import org.example.mono.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Integer id);
}
