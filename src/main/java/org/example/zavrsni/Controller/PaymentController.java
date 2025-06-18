package org.example.zavrsni.Controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import jakarta.servlet.http.HttpSession;
import org.example.zavrsni.Entity.Apartman;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Service.ApartmanService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class PaymentController {


    private final ApartmanService apartmanService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public PaymentController(ApartmanService apartmanService) {
        this.apartmanService = apartmanService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @GetMapping("/payment")
    public String paymentForm(Model model,@RequestParam("apartmentId") UUID apartmentId) {
        model.addAttribute("apartmentId",apartmentId);
        return "payment";
    }

    @PostMapping("/charge")
    public String charge(@RequestParam("stripeToken") String token, @RequestParam("apartmentId") UUID apartmentId, Model model, HttpSession session) {
        Apartman apartman = apartmanService.getApartmanById(apartmentId);
        User user = (User) session.getAttribute("user");
        try {
            System.out.println("Received stripeToken: " + token); // Log the received token
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", 999); // Amount in cents
            chargeParams.put("currency", "eur");
            chargeParams.put("description", "Example charge");
            chargeParams.put("source", token);

            Charge charge = Charge.create(chargeParams);
            model.addAttribute("status", "success");
            model.addAttribute("apartment",apartman);
            model.addAttribute("user",user);
        } catch (StripeException e) {
            model.addAttribute("status", "failure");
            model.addAttribute("error", e.getMessage());
        }


        return "redirect:/rezervacija/form?apartmentId=" + apartmentId;
    }

    @PostMapping("/additional-charge")
    public String charge2(@RequestParam("stripeToken") String token, HttpSession session) {
        User user = (User) session.getAttribute("user");
        try {
            System.out.println("Received stripeToken: " + token); // Log the received token
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", 999); // Amount in cents
            chargeParams.put("currency", "eur");
            chargeParams.put("description", "Example charge");
            chargeParams.put("source", token);

            Charge charge = Charge.create(chargeParams);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }


        return "redirect:/account/" + user.getUsername();
    }

}
