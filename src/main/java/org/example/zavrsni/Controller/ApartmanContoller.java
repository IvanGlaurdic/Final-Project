package org.example.zavrsni.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.example.zavrsni.Entity.Rezervacija;
import org.example.zavrsni.Entity.Slike;
import org.example.zavrsni.Repository.SlikeRepository;
import org.example.zavrsni.Service.ApartmanService;
import org.example.zavrsni.Service.RezervacijaService;
import org.example.zavrsni.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.zavrsni.Entity.User;
import org.example.zavrsni.Entity.Apartman;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

import java.io.IOException;
import java.util.Base64;


@AllArgsConstructor
@Controller
@RequestMapping("/apartman")
public class ApartmanContoller {

    public final ApartmanService apartmanService;
    public final UserService userService;
    public final RezervacijaService rezervacijaService;
    public final SlikeRepository slikeRepository;


    @GetMapping()
    public String Apartman(Model model, HttpSession session){

        User user =(User) session.getAttribute("user");
        model.addAttribute("user", user);

        model.addAttribute("apartman", new Apartman());
        model.addAttribute("error", null);
        return "add-apartman";
    }


    @PostMapping
    public String addApartman(@ModelAttribute("apartman") Apartman apartman,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam String price,
                              @RequestParam("image") List<MultipartFile> imageFiles,
                              HttpSession session,
                              Model model){

        Object sessionUser = session.getAttribute("user");
        User user;

        if (sessionUser instanceof User userInstance){
            user = userService.getUserByUsername(userInstance.getUsername());
        }
        else {
            throw new RuntimeException("Can't locate user:" + sessionUser);
        }

        for (MultipartFile imageFile : imageFiles) {
            if (!imageFile.isEmpty()) {
                try {
                    byte[] imageBytes = imageFile.getBytes();
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                    Slike slike = new Slike();
                    slike.setImage(base64Image);
                    slike.setApartman(apartman);
                    apartman.getImageURL().add(slike);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        apartmanService.SaveApartman(apartman);

        return "redirect:/account/" + user.getUsername();
    }



    @PostMapping("/delete/{id}")
    public String deleteApartman(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes,
            HttpSession httpSession,
            Model model) {

        User user;

        Object sessionUser = httpSession.getAttribute("user");
        if (sessionUser instanceof User userInstance) {
            user = userService.getUserByUsername(userInstance.getUsername());
        } else {
            model.addAttribute("error", "User authentication failed.");
            return "redirect:/login";
        }

        try {
            List<Rezervacija> rezervacije = rezervacijaService.getAllReservationsForApartment(id);
            for (final Rezervacija rezervacija : rezervacije){
                rezervacijaService.deleteReservation(rezervacija.getId());
            }
            apartmanService.deleteApartmanById(id);

            redirectAttributes.addFlashAttribute("successMessage", "Apartment deleted successfully!");
            return "redirect:/account/" + user.getUsername();
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting apartment.");
            return "redirect:/account/" + user.getUsername();
        }
    }


    @GetMapping("/add-image/{name}")
    public String showAddImageForm(@PathVariable("name") String name, Model model) {
        Apartman apartman = apartmanService. getApartmanByName(name);
        model.addAttribute("apartman", apartman);
        return "add-slike";
    }



    @PostMapping("/add-image/{name}")
    public String addImageToApartman(@PathVariable("name") String name,
                                     @RequestParam("image") List<MultipartFile> files,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) throws IOException {

        Apartman apartman = apartmanService. getApartmanByName(name);
        Object sessionUser = session.getAttribute("user");
        User user;

        if (sessionUser instanceof User userInstance){
            user = userService.getUserByUsername(userInstance.getUsername());
        }
        else {
            throw new RuntimeException("Can't locate user:" + sessionUser);
        }


        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                byte[] imageBytes = file.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                Slike slike = new Slike();
                slike.setImage(base64Image);
                slike.setApartman(apartman);
                apartman.getImageURL().add(slike);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "One or more image files are empty.");
                return "redirect:/account/" + user.getUsername();
            }
        }
        apartmanService.SaveApartman(apartman);

    return "redirect:/account/" + user.getUsername();

    }
    @GetMapping("/edit/{id}")
    public String showEditApartmentForm(@PathVariable UUID id, Model model, HttpSession session) {
        Apartman apartman = apartmanService.getApartmanById(id);
        model.addAttribute("apartman", apartman);
        return "edit-apartman";
    }
    @PostMapping("/update")
    public String updateApartment(@ModelAttribute Apartman apartman) {
        apartmanService.SaveApartman(apartman);
        return "redirect:/";
    }



    @GetMapping("/images/{name}")
    public String listImagesForApartman(@PathVariable("name") String name, Model model) {
        Apartman apartman = apartmanService.getApartmanByName(name);
        List<Slike> slikeList = apartman.getImageURL();

        model.addAttribute("apartman", apartman);
        model.addAttribute("slikeList", slikeList);

        return "delete-slike";
    }


    @PostMapping("/delete-image/{apartmanId}/{imageId}")
    public String deleteImage(
            @PathVariable("apartmanId") UUID apartmanId,
            @PathVariable("imageId") UUID imageId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object sessionUser = session.getAttribute("user");
        User user;

        if (sessionUser instanceof User userInstance) {
            user = userService.getUserByUsername(userInstance.getUsername());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User authentication failed.");
            return "redirect:/login";
        }

        try {
            Apartman apartman = apartmanService.getApartmanById(apartmanId);
            Slike image = slikeRepository.findById(imageId).orElseThrow(() -> new RuntimeException("Image not found"));

            if (image.getApartman().equals(apartman)) {
                apartmanService.removeImageFromApartman(apartman, image);
                apartmanService.deleteImageById(imageId);
                redirectAttributes.addFlashAttribute("successMessage", "Image deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Image does not belong to the specified apartment.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting image.");
        }

        return "redirect:/account/" + user.getUsername();
    }



}
