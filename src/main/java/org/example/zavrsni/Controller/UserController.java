package org.example.zavrsni.Controller;



import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.Apartman;
import org.example.zavrsni.Entity.Rezervacija;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Repository.ApartmanRepository;
import org.example.zavrsni.Service.ApartmanService;
import org.example.zavrsni.Service.AuthenticationService;
import org.example.zavrsni.Service.RezervacijaService;
import org.example.zavrsni.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;


@AllArgsConstructor
@Controller
@RequestMapping("/account")
public class UserController {
    private final UserService userService;
    private final ApartmanService apartmanService;
    private final AuthenticationService authenticationService;
    private final RezervacijaService rezervacijaService;


    @GetMapping("/{username}")
    public String account(@PathVariable("username") String username,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {

        User user = userService.getUserByUsername(username);
        List<Rezervacija> rezervacije = rezervacijaService.getAllReservationsForUser(user.getId());
        List<User> users;
        List<Apartman> apartmani;

        if (Objects.equals(user.getRole().toLowerCase(), "admin")) {
            users = userService.getAllUsers();
            apartmani = apartmanService.getAllApartmans();
            rezervacije = rezervacijaService.getAllReservations();
        } else {
            users = null;
            apartmani = null;
        }


        for (Rezervacija rezervacija : rezervacije) {
            Apartman reservedApartman = apartmanService.getApartmanById(rezervacija.getApartman().getId());
            rezervacija.setApartman(reservedApartman);
        }

        model.addAttribute("rezervacije", rezervacije);
        model.addAttribute("users", users);
        model.addAttribute("user", user);
        model.addAttribute("apartmani", apartmani);
        return "account";
    }


    @PostMapping("/delete-user/{username}")
    public String deleteUser(@PathVariable("username") String username,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {

        Object sessionUser = session.getAttribute("user");
        User user = userService.getUserByUsername(username);
        List<Rezervacija> rezervacije = rezervacijaService.getAllReservationsForUser(user.getId());


        if (sessionUser instanceof User sessionUserInstance) {
            User authenticatedUser = userService.getUserByUsername(sessionUserInstance.getUsername());

            if (Objects.equals(authenticatedUser.getRole().toLowerCase(), "admin")
                    || Objects.equals(authenticatedUser.getUsername(), username)) {

                User userToDelete = userService.getUserByUsername(username);

                for (final Rezervacija rezervacija : rezervacije){
                    rezervacijaService.deleteReservation(rezervacija.getId());
                }

                userService.saveUser(userToDelete);
                userService.deleteUser(userToDelete.getId());

                if (Objects.equals(authenticatedUser.getRole().toLowerCase(), "admin")) {
                    return "redirect:/account/" + authenticatedUser.getUsername();
                } else {
                    return "redirect:/";
                }
            }
        }

        return "redirect:/";
    }



    @PostMapping("/delete-reservation/{reservationId}")
    public String deleteReservation(@PathVariable("reservationId") UUID reservationId,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {

        Object sessionUser = session.getAttribute("user");

        if (sessionUser instanceof User sessionUserInstance) {
            User authenticatedUser = userService.getUserByUsername(sessionUserInstance.getUsername());

            // Fetch the reservation
            Rezervacija reservation = rezervacijaService.findReservationById(reservationId);

            // Check if the reservation exists
            if (reservation != null) {
                // Check if the authenticated user is admin or the owner of the reservation
                if (Objects.equals(authenticatedUser.getRole().toLowerCase(), "admin") ||
                        Objects.equals(authenticatedUser.getUsername(), reservation.getUser().getUsername())) {

                    // Delete the reservation
                    rezervacijaService.deleteReservation(reservationId);

                    // Redirect to user's account page
                    return "redirect:/account/" + authenticatedUser.getUsername();
                }
            }
        }

        // If the user is not an admin, or the reservation does not belong to the user, redirect to home
        return "redirect:/";
    }



    @GetMapping("/account/{username}")
    public String userAccount(@PathVariable("username") String username, Model model, HttpSession session) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            model.addAttribute("user", user);
            return "account";
        } else {
            // Handle case where user is not found
            return "redirect:/"; // Redirect to home page or an error page
        }
    }



    @GetMapping("/edit")
    public String editAccount(Model model, HttpSession session) {
        // Retrieve the currently logged-in user
        Object sessionUser = session.getAttribute("user");
        if (sessionUser instanceof User) {
            User user = (User) sessionUser;
            model.addAttribute("user", user);
            return "edit-account";
        } else {
            // If no user is logged in, redirect to the login page
            return "redirect:/login";
        }
    }

    @PostMapping("/edit")
    public String saveAccountChanges(@ModelAttribute("user") User updatedUser, HttpSession session) {

        User sessionUser = (User) session.getAttribute("user");

        sessionUser.setFirstName(updatedUser.getFirstName());
        sessionUser.setLastName(updatedUser.getLastName());
        sessionUser.setEmail(updatedUser.getEmail());

        userService.saveUser(sessionUser);

        session.setAttribute("user", sessionUser);

        return "redirect:/account/" + sessionUser.getUsername();
    }


    @PostMapping("/reservation-rating/{reservationId}")
    public String ratingFunction(@PathVariable UUID reservationId, HttpSession session,@RequestParam String comment, @RequestParam Integer rating){
        Object sessionUser = session.getAttribute("user");

        if (sessionUser instanceof User sessionUserInstance) {
            User authenticatedUser = userService.getUserByUsername(sessionUserInstance.getUsername());

            rezervacijaService.apartmanRating(reservationId, comment,rating);

                return "redirect:/account/" + authenticatedUser.getUsername();

        }

        return "redirect:/";
    }


    @PostMapping("/cancel-reservation/{reservationId}")
    public String cancelReservation(@PathVariable("reservationId") UUID reservationId, HttpSession session, RedirectAttributes redirectAttributes) {
        Object sessionUser = session.getAttribute("user");

        if (sessionUser instanceof User sessionUserInstance) {
            User authenticatedUser = userService.getUserByUsername(sessionUserInstance.getUsername());


            Rezervacija reservation = rezervacijaService.findReservationById(reservationId);
            if (reservation != null) {

                if (Objects.equals(authenticatedUser.getRole().toLowerCase(), "admin") ||
                        Objects.equals(authenticatedUser.getUsername(), reservation.getUser().getUsername())) {


                    if (Objects.equals(authenticatedUser.getRole().toLowerCase(), "admin")) {
                        if (reservation.getPologStatus().equals("false")) {
                            reservation.setRezervacijaStatus("Canceled");
                            rezervacijaService.saveReservation(reservation);
                        }
                    } else if (Objects.equals(authenticatedUser.getUsername(), reservation.getUser().getUsername())) {
                        reservation.setRezervacijaStatus("Canceled");
                        rezervacijaService.saveReservation(reservation);
                    }

                    return "redirect:/account/" + authenticatedUser.getUsername();
                }
            }
        }
        return "redirect:/";
    }


}

