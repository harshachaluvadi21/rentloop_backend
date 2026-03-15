package com.rentloop.repository;

import com.rentloop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRole(User.Role role);
    long countByRole(User.Role role);
    
    @Transactional
    void deleteByEmail(String email);
}
