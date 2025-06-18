package org.example.zavrsni.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveNewUser(String firstName,
                            String lastName,
                            String username,
                            String email,
                            String password,
                            String phoneNumber) {

        if (userRepository.findByUsernameOrEmail(username,email)==null){
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setPhoneNumber(phoneNumber);
            user.setRole("user");
            return userRepository.save(user);
        }
        return null;
    }
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }
    public void saveUser(User user) {
        userRepository.save(user);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }



}
