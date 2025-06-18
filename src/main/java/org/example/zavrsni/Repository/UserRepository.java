package org.example.zavrsni.Repository;

import org.example.zavrsni.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsernameOrEmail(String username, String email);



    User getUserById(UUID userId);



    boolean existsByEmail(String email);

    User getUserByEmail(String email);



    User findByUsername(String username);
}
