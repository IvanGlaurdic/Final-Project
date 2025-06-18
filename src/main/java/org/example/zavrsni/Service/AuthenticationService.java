package org.example.zavrsni.Service;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Data
@AllArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User authenticate(String UsernameOrEmail, String rawPassword) {
        User user = userRepository.findByUsernameOrEmail(UsernameOrEmail, UsernameOrEmail);
        if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())){
            return user;
        }
        return null;
    }
}

