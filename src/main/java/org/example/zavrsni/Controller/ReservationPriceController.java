package org.example.zavrsni.Controller;

import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.Apartman;
import org.example.zavrsni.Entity.Rezervacija;
import org.example.zavrsni.Service.ApartmanService;
import org.example.zavrsni.Service.RezervacijaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RestController
public class ReservationPriceController {
    private final ApartmanService apartmanService;
    private final RezervacijaService rezervacijaService;

    @PostMapping("/calculate-price")
    public Map<String, Object> calculatePrice(@RequestBody Map<String, String> requestBody) {
        String arrivalDate = requestBody.get("arrivalDate");
        String departureDate = requestBody.get("departureDate");
        UUID apartmentId = UUID.fromString(requestBody.get("apartmentId"));


        double totalPrice = calculateTotalPrice(arrivalDate, departureDate, apartmentId);


        Map<String, Object> response = new HashMap<>();
        response.put("totalPrice", totalPrice);

        return response;
    }


    private double calculateTotalPrice(String arrivalDate, String departureDate, UUID apartmentId) {
        // Check if arrivalDate or departureDate is empty
        if (arrivalDate.isEmpty() || departureDate.isEmpty()) {
            throw new IllegalArgumentException("Arrival date and departure date are required.");
        }

        LocalDate arrival = LocalDate.parse(arrivalDate);
        LocalDate departure = LocalDate.parse(departureDate);

        long nights = Duration.between(arrival.atStartOfDay(), departure.atStartOfDay()).toDays();

        Apartman apartment = apartmanService.getApartmanById(apartmentId);
        double apartmentPricePerNight = apartment.getPrice();

        return (apartmentPricePerNight * nights) ;
    }

}