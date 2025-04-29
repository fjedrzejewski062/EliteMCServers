package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.User;
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
    private final UserService userService;
    @Value("${admin.email}")
    private String adminEmail;

    public UserController(UserService userService) {
        this.userService = userService;
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
                                   @RequestParam(value="terms", required = false) String terms,
                                   Model model){
        if(!user.getPassword().equals(confirmPassword)){
            result.rejectValue("password", "error.user", "Passwords do not match");
        }

        if(terms == null){
            result.rejectValue("terms", "You must agree to the terms and conditions");
        }

        if(userService.findByUsername(user.getUsername()).isPresent()){
            result.rejectValue("username", "error.user", "Username is already taken");
        }

        if(userService.findByEmail(user.getEmail()).isPresent()){
            result.rejectValue("email", "error.user", "Email is already registered");
        }

        if(adminEmail != null && adminEmail.equalsIgnoreCase(user.getEmail())){
            user.setRole("ADMIN");
        }

        if(result.hasErrors()){
            return "register";
        }
        userService.register(user);
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
        User user = userService.findByEmail(email).orElse(null);
        model.addAttribute("user", user);
        return "myProfile";
    }

    @PostMapping("/deleteaccount")
    public String deleteAccount(@AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser){
        String email = currentUser.getUsername();
        User user = userService.findByEmail(email).orElse(null);
        if(user != null){
            userService.softDelete(user);
        }
        return "redirect:/logout";
    }
}
