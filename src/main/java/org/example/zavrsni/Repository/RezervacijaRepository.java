package org.example.zavrsni.Repository;

import org.example.zavrsni.Entity.Rezervacija;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RezervacijaRepository extends JpaRepository<Rezervacija, UUID> {
    List<Rezervacija> findAllByApartman_Id(UUID apartmentId);

    List<Rezervacija> findAllByUser_Id(UUID id);
}
