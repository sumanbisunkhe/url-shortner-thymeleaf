package com.example.urlshortner.controller;

import com.example.urlshortner.dto.request.AuthRequest;
import com.example.urlshortner.model.User;
import com.example.urlshortner.repository.UserRepository;
import com.example.urlshortner.security.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
=======
import org.springframework.web.bind.annotation.*;
>>>>>>> c8a73bf542437b4a82eb4ae49705ad7fcf75a231

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("authRequest", new AuthRequest());
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute AuthRequest authRequest, HttpServletResponse response, Model model) {
        try {
            log.info("Attempting authentication for user: {}", authRequest.getUsernameOrEmail());

            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(authRequest.getUsernameOrEmail(), authRequest.getPassword());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get username from authentication
            String username = authentication.getName();
            log.info("User authenticated successfully: {}", username);

            // Find user in repository
            User user = userRepository.findByUsernameOrEmail(username, username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            // Generate JWT token
            String jwtToken = jwtTokenUtil.generateAccessToken(user);
            log.info("JWT token generated for user: {}", username);

            Cookie accessCookie = new Cookie("access_token", jwtToken);
            accessCookie.setPath("/");
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false); // Set to false for HTTP development
            accessCookie.setMaxAge((int) (jwtTokenUtil.getAccessTokenExpiration() / 1000));
            response.addCookie(accessCookie);

            // Redirect to dashboard
            return "redirect:/urls";
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            model.addAttribute("error", "Invalid username/email or password");
            return "login";
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        // Clear cookies
        Cookie cookie = new Cookie("access_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/auth/login";
    }
}