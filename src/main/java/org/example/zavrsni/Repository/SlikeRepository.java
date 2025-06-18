package org.example.zavrsni.Repository;

import org.example.zavrsni.Entity.Slike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;
public interface SlikeRepository extends JpaRepository<Slike, UUID> {
}
