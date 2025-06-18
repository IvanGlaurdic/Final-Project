package org.example.zavrsni.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.zavrsni.Entity.Apartman;
import org.example.zavrsni.Entity.Rezervacija;
import org.example.zavrsni.Entity.Slike;
import org.example.zavrsni.Repository.ApartmanRepository;
import org.example.zavrsni.Repository.SlikeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Service
public class ApartmanService {

    public final ApartmanRepository apartmanRepository;
    public final SlikeRepository slikeRepository;

    public  List<Apartman> getAllApartmans() {
        return apartmanRepository.findAll();
    }

    public Apartman SaveApartman(Apartman apartman){return apartmanRepository.save(apartman);}


    public Apartman getApartmanById(UUID apartmanId) {
        return apartmanRepository.getReferenceById(apartmanId);
    }

    public void deleteApartmanById(UUID id) {
        apartmanRepository.deleteById(id);
    }

    public Apartman getApartmanByName(String name) {
        return apartmanRepository.getReferenceByName(name);
    }



    public void updateRating(UUID apartmanId, List<Rezervacija> rezervacije) {
        Apartman apartman = apartmanRepository.getReferenceById(apartmanId);

        double sum = 0;
        int count = 0;

        for (final Rezervacija rezervacija : rezervacije){
            if(rezervacija.getRecenzija()!=0){
                sum += rezervacija.getRecenzija();
                count++;
            }
        }

        apartman.setRecenzija(sum/count);
        apartmanRepository.save(apartman);

    }

    public List<Apartman> getApartmansSortedByRating() {
        return apartmanRepository.findAllOrderByRecenzijaDesc();
    }

    public Apartman SaveApartman(String name, String description, float price) {
        Apartman apartman = new Apartman();
        if (apartmanRepository.findByName(name) == null){
            apartman.setName(name);
            apartman.setDescription(description);
            apartman.setPrice(price);

        }

        return apartmanRepository.save(apartman);
    }

    public void removeImageFromApartman(Apartman apartman, Slike image) {
        apartman.getImageURL().remove(image);
        apartmanRepository.save(apartman);  // Save changes to the Apartman
    }

    public void deleteImageById(UUID imageId) {
        slikeRepository.deleteById(imageId);
    }
}
