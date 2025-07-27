package com.example.blog_website_springboot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BlogController {

    @Autowired
    private BlogService blogService;


    private final BlogRepository blogRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public BlogController(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    // This ensures opening http://localhost:8080/ redirects to /login
    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/create")
    public String showBlogs(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String loggedInUsername = authentication.getName();




        System.out.println("Logged in as: " +  authentication.getName());


        model.addAttribute("username", authentication.getName());
        model.addAttribute("blog", new Blog());
        model.addAttribute("blogs", blogRepository.findAll());
        return "form";
    }
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("user") AppUser user, RedirectAttributes redirectAttributes) {
        boolean registered = userService.register(user);

        if (!registered) {
            redirectAttributes.addFlashAttribute("error", "User already exists!");
            return "redirect:/login";
        }

        return "redirect:/login";
    }


    @PostMapping("/save")
    public String saveBlog(@ModelAttribute Blog blog, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        blogRepository.save(blog);
        model.addAttribute("blog", new Blog());
        model.addAttribute("blogs", blogRepository.findAll());
        System.out.println("Received POST: " + blog.getTitle());

        return "form";
    }
    @GetMapping("/edit/{id}")
    public String editBlog(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        Blog blog = blogRepository.findById(id).orElseThrow();
        model.addAttribute("blog", blog);
        model.addAttribute("blogs", blogRepository.findAll());
        return "form";
    }

    @PostMapping("/delete/{id}")
    public String deleteBlog(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        blogRepository.deleteById(id);
        return "form";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute AppUser request, HttpServletResponse response, Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();


            AppUser dummyUser = new AppUser();
            dummyUser.setUsername(userDetails.getUsername());
            dummyUser.setRole(userDetails.getAuthorities().iterator().next().getAuthority());


            String token = jwtUtil.generateToken(dummyUser);

            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 2);
            response.addCookie(cookie);


            return "redirect:/create";

        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password");
            System.out.println("error Invalid username or password");
            return "login";
        }
    }

}






