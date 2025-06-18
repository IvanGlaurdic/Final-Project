package org.example.zavrsni.Service;

import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.Rezervacija;
import org.example.zavrsni.Repository.RezervacijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RezervacijaService {

    private final RezervacijaRepository rezervacijaRepository;
    private final ApartmanService apartmanService;



    @Transactional
    public void saveReservation(Rezervacija rezervacija) {
        rezervacijaRepository.save(rezervacija);
    }

    public List<Rezervacija> getAllReservationsForApartment(UUID apartmentId) {
        return rezervacijaRepository.findAllByApartman_Id(apartmentId);
    }

    public List<Rezervacija> getAllReservationsForUser(UUID id) {
        return rezervacijaRepository.findAllByUser_Id(id);
    }

    public List<Rezervacija> getAllReservations() {
        return rezervacijaRepository.findAll();
    }

    public void deleteReservation(UUID reservationId) {
        rezervacijaRepository.deleteById(reservationId);
    }


    public void apartmanRating(UUID reservationId, String comment, Integer rating) {
        Rezervacija rezervacija = rezervacijaRepository.getReferenceById(reservationId);
        rezervacija.setComment(comment);
        rezervacija.setRecenzija(rating);
        rezervacijaRepository.save(rezervacija);

        apartmanService.updateRating(rezervacija.getApartman().getId(), getAllReservationsForApartment(rezervacija.getApartman().getId()));

    }

    public Rezervacija findReservationById(UUID reservationId) {
        Optional<Rezervacija> optionalReservation = rezervacijaRepository.findById(reservationId);
        return optionalReservation.orElse(null); // Return the reservation if found, otherwise return null
    }
}
