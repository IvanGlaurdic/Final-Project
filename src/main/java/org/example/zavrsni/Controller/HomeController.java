package org.example.zavrsni.Controller;

import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.Apartman;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Service.ApartmanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@AllArgsConstructor
@Controller
public class HomeController {

    private final ApartmanService apartmanService;

    @GetMapping("/")
    public String home(Model model, HttpSession session, @RequestParam(value = "sort", required = false) String sort) {
        List<Apartman> apartments;


        if ("rating".equals(sort)) {
            apartments = apartmanService.getApartmansSortedByRating();
        } else {
            apartments = apartmanService.getAllApartmans();
        }


        model.addAttribute("apartments", apartments);


        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);


        session.removeAttribute("reservation");
        session.removeAttribute("successMessage");
        session.removeAttribute("deductDeposit");
        session.removeAttribute("errorMessage");

        return "home";
    }
}
