package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.facade.UserFacade;
import com.example.elitemcservers.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
public class UserController {
    private final UserFacade userFacade;
    @Value("${admin.email}")
    private String adminEmail;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        System.out.println("Register page requested");
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registrationForm(@Valid @ModelAttribute("user") User user,
                                   BindingResult result,
                                   @RequestParam("confirmPassword") String confirmPassword,
                                   @RequestParam(value = "terms", required = false) String terms,
                                   Model model) {
        try {
            userFacade.registerUser(user, confirmPassword, terms);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("password", "error.user", ex.getMessage());
            return "register";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String showLoginForm(){
        return "login";
    }

    @GetMapping("/myprofile")
    public String showProfile(Model model,
                              @AuthenticationPrincipal
                              org.springframework.security.core.userdetails.User currentUser){
        String email = currentUser.getUsername();
        User user = userFacade.findByEmail(email).orElse(null);
        model.addAttribute("user", user);
        return "myProfile";
    }

    @PostMapping("/deleteaccount")
    public String deleteAccount(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser){
        String email = currentUser.getUsername();
        User user = userFacade.findByEmail(email).orElse(null);
        if(user != null){
            userFacade.deleteUserAccount(user);
        }
        return "redirect:/logout";
    }
}
