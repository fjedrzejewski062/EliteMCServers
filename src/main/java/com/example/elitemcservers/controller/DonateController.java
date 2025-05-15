package com.example.elitemcservers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DonateController {
    @GetMapping("/donate")
    public String donatePage() {
        return "donate";
    }

    @GetMapping("/donate/success")
    public String donationSuccess(@RequestParam("session_id") String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "donate_success";
    }

    @GetMapping("/donate/cancel")
    public String donationCancelled() {
        return "donate_cancel";
    }
}
