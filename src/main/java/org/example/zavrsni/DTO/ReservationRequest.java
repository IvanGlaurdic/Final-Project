package org.example.zavrsni.DTO;

import org.example.zavrsni.Entity.Apartman;
import org.example.zavrsni.Entity.User;

public class ReservationRequest {
    private User user;
    private Apartman apartman;


    public ReservationRequest(User user, Apartman apartman) {
        this.user = user;
        this.apartman = apartman;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Apartman getApartman() {
        return apartman;
    }

    public void setApartman(Apartman apartman) {
        this.apartman = apartman;
    }
}