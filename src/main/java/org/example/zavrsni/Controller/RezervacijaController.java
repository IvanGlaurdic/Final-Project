package org.example.zavrsni.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Entity.Rezervacija;
import org.example.zavrsni.Service.RezervacijaService;
import org.example.zavrsni.Service.UserService;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.zavrsni.Entity.Apartman;
import org.example.zavrsni.Service.ApartmanService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
@RequestMapping("/rezervacija")
public class RezervacijaController {

    private final RezervacijaService rezervacijaService;
    private final ApartmanService apartmanService;
    private final UserService userService;

    @GetMapping("/form")
    public String showReservationForm(Model model, @RequestParam("apartmentId") UUID apartmentId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Apartman apartman = apartmanService.getApartmanById(apartmentId);

        // Get existing reservations for the apartment
        List<Rezervacija> existingReservations = rezervacijaService.getAllReservationsForApartment(apartmentId);

        // Extract reserved date ranges using DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Map<String, String>> reservedDateRanges = existingReservations.stream()
                .map(r -> {
                    Map<String, String> dateRange = new HashMap<>();
                    dateRange.put("startDate", r.getDatumDolaska().format(formatter));
                    dateRange.put("endDate", r.getDatumOdlaska().format(formatter));
                    return dateRange;
                })
                .collect(Collectors.toList());

        model.addAttribute("apartment", apartman);
        model.addAttribute("user", user);
        model.addAttribute("reservedDateRanges", reservedDateRanges);
        model.addAttribute("comments", existingReservations);

        // Remove messages from the session after they have been added to the model
        if (session.getAttribute("successMessage") != null) {
            model.addAttribute("successMessage", session.getAttribute("successMessage"));
            session.removeAttribute("successMessage");
        }

        if (session.getAttribute("depositStatus") != null) {
            model.addAttribute("successMessage", session.getAttribute("depositStatus"));
            session.removeAttribute("depositStatus");
        }

        if (model.containsAttribute("errorMessage")) {
            String errorMessage = (String) model.getAttribute("errorMessage");
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }

        if (session.getAttribute("reservation") == null) {
            Rezervacija rezervacija = new Rezervacija();
            rezervacija.setUser(user);
            rezervacija.setApartman(apartman);
            session.setAttribute("reservation", rezervacija);
            model.addAttribute("rezervacija", null);
        } else {
            Rezervacija rezervacija = (Rezervacija) session.getAttribute("reservation");
            model.addAttribute("rezervacija", rezervacija);
        }

        return "add-rezervacija";
    }


    private List<Date> getDatesBetween(Date startDate, Date endDate) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }
        return dates;
    }

    @PostMapping("/add")
    public String addReservation(RedirectAttributes redirectAttributes,
                                 @RequestParam("arrivalDate")  String arrivalDate,
                                 @RequestParam("departureDate")  String departureDate,
                                 @RequestParam("apartmentId") UUID apartmentId,
                                 @RequestParam("userId") UUID userId,
                                 @RequestParam("totalPrice") double totalPrice,
                                 Model model,
                                 HttpSession session) {

        System.err.print("Arrival " + arrivalDate );
        System.err.print("Departure " + departureDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        LocalDate formattedArrivalDate = LocalDate.parse(arrivalDate, formatter);
        LocalDate formattedDepartureDate = LocalDate.parse(departureDate, formatter);

        System.err.print("Arrival " + arrivalDate + " and " + formattedArrivalDate);
        System.err.print("Departure " + departureDate + " and " + formattedDepartureDate);

        if (!isDatesAvailable(apartmentId, formattedArrivalDate.atStartOfDay(), formattedDepartureDate.atStartOfDay())) {
            redirectAttributes.addFlashAttribute("successMessage", "Odabrani datumi su već zauzeti.");
            return "redirect:/rezervacija/form?apartmentId=" + apartmentId;
        }


        Rezervacija rezervacija = new Rezervacija();
        rezervacija.setDatumDolaska(formattedArrivalDate.atStartOfDay());
        rezervacija.setDatumOdlaska(formattedDepartureDate.atStartOfDay());
        rezervacija.setCijena(totalPrice);
        rezervacija.setPologStatus("false");
        rezervacija.setRecenzija(0);
        rezervacija.setRezervacijaStatus("Reserved");


        Apartman apartman = apartmanService.getApartmanById(apartmentId);
        rezervacija.setApartman(apartman);


        User user = new User();
        user.setId(userId);
        rezervacija.setUser(user);

        session.setAttribute("totalPrice", totalPrice);
        session.setAttribute("reservation", rezervacija);



        rezervacijaService.saveReservation(rezervacija);
        session.removeAttribute("reservation");
        session.setAttribute("successMessage", "You have successfully reserved, the deadline to pay the deposit is 3 days."

        );
        session.setAttribute("deductDeposit", null);
        return "redirect:/rezervacija/form?apartmentId=" + apartmentId;


    }

    @GetMapping("/reserved-dates")
    @ResponseBody
    public List<String> getReservedDates(@RequestParam("apartmentId") UUID apartmentId) {
        List<String> reservedDates = new ArrayList<>();

        try {
            List<Rezervacija> existingReservations = rezervacijaService.getAllReservationsForApartment(apartmentId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Rezervacija reservation : existingReservations) {
                LocalDate startDate = reservation.getDatumDolaska().toLocalDate();
                LocalDate endDate = reservation.getDatumOdlaska().toLocalDate();

                while (!startDate.isAfter(endDate)) {
                    reservedDates.add(startDate.format(formatter));
                    startDate = startDate.plusDays(1);
                }
            }
        } catch (Exception e) {
            // Handle any potential exceptions
            e.printStackTrace();
        }

        System.err.print(reservedDates);

        return reservedDates;
    }

    private boolean isDatesAvailable(UUID apartmentId, LocalDateTime arrivalDate, LocalDateTime departureDate) {
        List<Rezervacija> existingReservations = rezervacijaService.getAllReservationsForApartment(apartmentId);

        for (Rezervacija reservation : existingReservations) {
            LocalDateTime reservationStart = reservation.getDatumDolaska();
            LocalDateTime reservationEnd = reservation.getDatumOdlaska();

            if (!arrivalDate.isAfter(reservationEnd) && !departureDate.isBefore(reservationStart)) {
                return false;
            }
        }
        return true;
    }


    @GetMapping("/check-dates")
    @ResponseBody
    public Map<String, Boolean> checkDates(@RequestParam UUID apartmentId,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDate,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {
        // Convert LocalDate to LocalDateTime at start of day
        LocalDateTime arrivalDateTime = arrivalDate.atStartOfDay();
        LocalDateTime departureDateTime = departureDate.atStartOfDay();

        boolean available = areDatesAvailable(apartmentId, arrivalDateTime, departureDateTime);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return response;
    }


    public boolean areDatesAvailable(UUID apartmentId, LocalDateTime arrivalDate, LocalDateTime departureDate) {
        List<Rezervacija> rezervacije = rezervacijaService.getAllReservationsForApartment(apartmentId);
        for (Rezervacija rezervacija : rezervacije) {
            if (arrivalDate.isBefore(rezervacija.getDatumOdlaska()) && departureDate.isAfter(rezervacija.getDatumDolaska())) {
                return false; // Overlapping dates
            }
        }
        return true; // No overlap
    }


    @GetMapping("/payment")
    public String paymentPage(@RequestParam(required = false) Boolean deposit,
                              @RequestParam(required = false) Boolean depositFlag,
                              @RequestParam(required = false) double polog,
                              Model model, HttpSession session) {
        Rezervacija rezervacija = (Rezervacija) session.getAttribute("reservation");

        if (rezervacija == null) {
            return "redirect:/"; // Redirect to home or appropriate page
        }

        model.addAttribute("rezervacija", rezervacija);
        model.addAttribute("apartmentId", rezervacija.getApartman().getId());
        model.addAttribute("deposit", deposit != null && deposit);

        session.removeAttribute("reservation");

        if (depositFlag) {
            model.addAttribute("cijena", polog);
        } else {
            model.addAttribute("cijena", rezervacija.getCijena());
        }

        return "payment"; // Return the name of your payment HTML file
    }




    @GetMapping("/refund-reservation")
    public String refundReservation(@RequestParam UUID reservationId, Model model) {
        Rezervacija rezervacija = rezervacijaService.findReservationById(reservationId);
        model.addAttribute("cijena", rezervacija.getCijena()-(rezervacija.getCijena() * 0.20));
        rezervacijaService.deleteReservation(reservationId);
        return "payment-2"; // Return the name of your payment HTML file
    }


    @GetMapping("/additional-deposit-payment")
    public String paymentPage1(@RequestParam UUID reservationId, Model model) {
        Rezervacija rezervacija = rezervacijaService.findReservationById(reservationId);

        rezervacija.setPologStatus("true");
        rezervacija.setRezervacijaStatus("Reserved"); // Set the status to "rezervirano"
        double polog = rezervacija.getCijena() * 0.20;
        rezervacija.setCijena(rezervacija.getCijena() - polog);

        rezervacijaService.saveReservation(rezervacija);
        model.addAttribute("cijena", polog);

        return "payment-2"; // Return the name of your payment HTML file
    }

    @GetMapping("/additional-full-payment")
    public String paymentPage2(@RequestParam UUID reservationId, Model model) {
        Rezervacija rezervacija = rezervacijaService.findReservationById(reservationId);

        rezervacija.setPologStatus("true");
        rezervacija.setRezervacijaStatus("Reserved"); // Set the status to "rezervirano"
        rezervacija.setPaymentStatus("Rezervacija plaćena");
        rezervacijaService.saveReservation(rezervacija);

        model.addAttribute("cijena", rezervacija.getCijena());

        return "payment-2"; // Return the name of your payment HTML file
    }

    @GetMapping("/add_reservation")
    public String paymentSuccess(@RequestParam("paymentSuccess") boolean paymentSuccess, HttpSession session, RedirectAttributes redirectAttributes) {
        if (paymentSuccess) {
            Rezervacija rezervacija = (Rezervacija) session.getAttribute("reservation");
            if (rezervacija != null) {
                rezervacija.setPaymentStatus("Plaćeno");
                rezervacija.setRezervacijaStatus("Reserved"); // Set the status to "rezervirano"
                session.setAttribute("reservation", rezervacija);
                redirectAttributes.addFlashAttribute("successMessage", "Payment successful!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed!");
        }
        return "redirect:/rezervacija/form"; // Redirect to the reservation form or another appropriate page
    }

    @PostMapping("/deduct-deposit")
    @ResponseBody
    public Map<String, Object> deductDeposit(@RequestBody Map<String, Object> requestBody, HttpSession session, RedirectAttributes redirectAttributes) {
        Map<String, Object> response = new HashMap<>();
        session.setAttribute("depositStatus", "You have successfully paid the deposit and booked the dates, the deadline to pay the full amount is 10 days");

        try {
            Rezervacija rezervacija = (Rezervacija) session.getAttribute("reservation");
            if (rezervacija == null) {
                response.put("success", false);
                return response;
            }

            Object totalPriceObj = requestBody.get("totalPrice");
            double totalPrice;
            if (totalPriceObj instanceof Integer) {
                totalPrice = ((Integer) totalPriceObj).doubleValue();
            } else if (totalPriceObj instanceof Double) {
                totalPrice = (Double) totalPriceObj;
            } else {
                throw new IllegalArgumentException("Invalid type for totalPrice");
            }

            String depositStatus = (String) requestBody.get("depositStatus");
            LocalDate arrivalDate = LocalDate.parse((String) requestBody.get("arrivalDate"));
            LocalDate departureDate = LocalDate.parse((String) requestBody.get("departureDate"));

            rezervacija.setDatumDolaska(arrivalDate.atStartOfDay());
            rezervacija.setDatumOdlaska(departureDate.atStartOfDay());
            rezervacija.setCijena(totalPrice);
            rezervacija.setPologStatus(depositStatus);
            rezervacija.setRecenzija(0);
            rezervacija.setRezervacijaStatus("Reserved");

            rezervacijaService.saveReservation(rezervacija);

            session.setAttribute("reservation", rezervacija);

            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
        }

        return response;
    }

    @PostMapping("/process-fullpay-data")
    @ResponseBody
    public Map<String, Object> processFullPay(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Rezervacija rezervacija = (Rezervacija) session.getAttribute("reservation");

        Object totalPriceObj = requestBody.get("totalPrice");
        double totalPrice;
        if (totalPriceObj instanceof Integer) {
            totalPrice = ((Integer) totalPriceObj).doubleValue();
        } else if (totalPriceObj instanceof Double) {
            totalPrice = (Double) totalPriceObj;
        } else {
            response.put("success", false);
            response.put("error", "Invalid totalPrice value");
            return response;
        }

        LocalDate arrivalDate = LocalDate.parse((String) requestBody.get("arrivalDate"));
        LocalDate departureDate = LocalDate.parse((String) requestBody.get("departureDate"));

        rezervacija.setDatumDolaska(arrivalDate.atStartOfDay());
        rezervacija.setDatumOdlaska(departureDate.atStartOfDay());
        rezervacija.setCijena(totalPrice);
        rezervacija.setRecenzija(0);
        rezervacija.setPologStatus("true");
        rezervacija.setPaymentStatus("Plaćeno");
        rezervacija.setRezervacijaStatus("Reserved");

        rezervacijaService.saveReservation(rezervacija);

        session.setAttribute("reservation", rezervacija);
        session.setAttribute("successMessage", "Reservation successfully paid and booked."

        );

        response.put("success", true);
        return response;
    }



}
