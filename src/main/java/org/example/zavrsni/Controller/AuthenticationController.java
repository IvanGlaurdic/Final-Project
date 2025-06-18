package org.example.zavrsni.Controller;


import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Service.AuthenticationService;
import org.example.zavrsni.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;


    @GetMapping("/login")
    public String login(){
        return "Login-forma";
    }

    @PostMapping("/login")
    public String performLogin(@RequestParam String UsernameOrEmail, @RequestParam String rawPassword, Model model, HttpSession session){
        User user = authenticationService.authenticate(UsernameOrEmail, rawPassword);

        if (user != null){
            session.setAttribute("user", user);
            model.addAttribute("isLoggedIn", session.getAttribute("isLoggedIn"));
            return "redirect:/";
        }
        else {
            model.addAttribute("error","Incorrect name or password");
            return "Login-forma";
        }
    }

    @GetMapping("/register")
    public String register(){return "Registration-forma";}


    @PostMapping("/register")
    public String handleUserRegistration(@ModelAttribute User user,
                                         @RequestParam String confirmPassword,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {


        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "Registration-forma";
        }
        if (userService.getUserByUsername(user.getUsername())!= null){
            model.addAttribute("error", "Username already taken.");
            return "Registration-forma";
        }
        if (userService.getUserByEmail(user.getEmail())!= null){
            model.addAttribute("error", "Email already taken.");
            return "Registration-forma";
        }

        userService.saveNewUser(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                authenticationService.getPasswordEncoder().encode(user.getPassword()),
                user.getPhoneNumber()

        );

        redirectAttributes.addFlashAttribute("user", user);

        return "redirect:/";
    }







    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
