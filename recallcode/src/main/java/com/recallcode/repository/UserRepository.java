
package com.recallcode.repository;

import com.recallcode.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


//This repository communicates with the PostgreSQL users table.
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
