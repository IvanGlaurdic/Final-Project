package org.example.zavrsni.Entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Table(name = "User", schema = "public")
@Entity
public class User {

    @Id
    @GeneratedValue
    private UUID id;


    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(nullable = false, unique = true, name = "username")
    private String username;

    @Column(nullable = false, unique = true, name = "email")
    private String email;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false, name = "role")
    private String role;
}
