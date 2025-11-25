package com.example.urlshortner.controller;

import com.example.urlshortner.dto.request.UserRequest;
import com.example.urlshortner.dto.response.UserResponse;
import com.example.urlshortner.enums.Role;
import com.example.urlshortner.exception.*;
import com.example.urlshortner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        model.addAttribute("users", users);
        return "users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        model.addAttribute("roles", Role.values());
        return "users/create";
    }

    @PostMapping
    public String createUser(
            @ModelAttribute UserRequest userRequest,
            RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(userRequest);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (DuplicateUsernameException | DuplicateEmailException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/create";
        }
        return "redirect:/users";
    }

    @GetMapping("/{userId}/edit")
    public String showEditForm(@PathVariable Long userId, Model model) {
        try {
            UserResponse user = userService.getUserById(userId);
            model.addAttribute("userRequest", user);
            model.addAttribute("roles", Role.values());
            return "users/edit";
        } catch (ResourceNotFoundException e) {
            return "redirect:/users";
        }
    }

    @PostMapping("/{userId}")
    public String updateUser(
            @PathVariable Long userId,
            @ModelAttribute UserRequest userRequest,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(userId, userRequest);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found");
        } catch (DuplicateUsernameException | DuplicateEmailException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/" + userId + "/edit";
        }
        return "redirect:/users";
    }

    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found");
        }
        return "redirect:/users";
    }
}