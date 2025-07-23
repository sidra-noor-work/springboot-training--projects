package com.example.blog_website_springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@ModelAttribute AppUser user, Model model) {
        boolean registered = userService.register(user);
        if (!registered) {
            model.addAttribute("error", "Username already exists!");
            return "signup";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

}
