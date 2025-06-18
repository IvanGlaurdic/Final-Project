package org.example.zavrsni.Entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Data
@Table(name = "Rezervacija", schema = "public")
@Entity
public class Rezervacija {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "datum_dolaska")
    private LocalDateTime datumDolaska;

    @Column(name = "datum_odlaska")
    private LocalDateTime datumOdlaska;

    @Column(name = "cijena")
    private double cijena;

    @Column(name = "recenzija")
    private Integer recenzija;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "apartman_id")
    private Apartman apartman;


    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name="polog_status")
    private String pologStatus;

    @Column(name = "comment")
    private String comment;

    @Column(name = "rezervacija_status")
    private String rezervacijaStatus;

}
