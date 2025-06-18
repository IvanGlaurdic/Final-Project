package org.example.zavrsni.Entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Table(name = "Apartman", schema = "public")
@Entity
public class Apartman {
    @Id
    @GeneratedValue
    private UUID id;

    @Column( name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private float price;

    @OneToMany(mappedBy = "apartman",cascade = CascadeType.ALL)
    private List<Slike> imageURL= new ArrayList<>();;

    @Column(name="Recenzija")
    private double Recenzija=0.0;

}




