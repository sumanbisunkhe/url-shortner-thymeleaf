package com.example.urlshortner.controller;

import com.example.urlshortner.dto.request.UserRequest;
import com.example.urlshortner.dto.response.UserResponse;
import com.example.urlshortner.enums.Role;
import com.example.urlshortner.exception.*;
import com.example.urlshortner.model.User;
import com.example.urlshortner.repository.UserRepository;
import com.example.urlshortner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    public String dashboard(Model model) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Add user info to model if needed
        model.addAttribute("username", username);

        return "dashboard";
    }

    @GetMapping("/profile")
    public String userProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + authentication); // Debug logging

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Not authenticated, redirecting to login"); // Debug logging
            return "redirect:/login";
        }

        String username = authentication.getName();
        System.out.println("Fetching user for username: " + username); // Debug logging

        try {
            UserResponse currentUser = userService.getUserByUsername(username);
            System.out.println("Found user: " + currentUser); // Debug logging
            model.addAttribute("user", currentUser);
            return "users/profile";
        } catch (ResourceNotFoundException e) {
            System.out.println("User not found for username: " + username); // Debug logging
            return "redirect:/login?error=user_not_found";
        }
    }


    @GetMapping("/users")
    public String listUsers(
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        model.addAttribute("users", users);
        return "users/list";
    }

    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        model.addAttribute("roles", Role.values());
        return "users/create";
    }

    @PostMapping("/users")
    public String createUser(
            @ModelAttribute UserRequest userRequest,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.createUser(userRequest);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (DuplicateUsernameException | DuplicateEmailException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/users/create";
        }
        return "redirect:/dashboard/users";
    }

    @GetMapping("/users/{userId}/edit")
    public String showEditUserForm(@PathVariable Long userId, Model model) {
        try {
            UserResponse user = userService.getUserById(userId);
            model.addAttribute("userRequest", user);
            model.addAttribute("roles", Role.values());
            return "users/edit";
        } catch (ResourceNotFoundException e) {
            return "redirect:/dashboard/users";
        }
    }

    @PostMapping("/users/{userId}")
    public String updateUser(
            @PathVariable Long userId,
            @ModelAttribute UserRequest userRequest,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.updateUser(userId, userRequest);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found");
        } catch (DuplicateUsernameException | DuplicateEmailException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/users/" + userId + "/edit";
        }
        return "redirect:/dashboard/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found");
        }
        return "redirect:/dashboard/users";
    }
}