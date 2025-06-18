package org.example.zavrsni.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Table(name = "slike", schema = "public")
@Entity
public class Slike {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "image_url")
    private String image_url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartman_id")
    private Apartman apartman;

    public void setImage(String base64Image) {
        this.image_url=base64Image;
    }
}