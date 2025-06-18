package org.example.zavrsni.Repository;


import org.example.zavrsni.Entity.Apartman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ApartmanRepository extends JpaRepository<Apartman, UUID> {

    Apartman findByName(String name);

    Apartman getReferenceByName(String name);


    @Query("SELECT a FROM Apartman a ORDER BY a.Recenzija DESC")
    List<Apartman> findAllOrderByRecenzijaDesc();
}
