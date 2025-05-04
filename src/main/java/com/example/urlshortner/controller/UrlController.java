package com.example.urlshortner.controller;

import com.example.urlshortner.dto.request.UrlRequest;
import com.example.urlshortner.dto.response.UrlResponse;
import com.example.urlshortner.exception.ShortCodeNotFoundException;
import com.example.urlshortner.exception.UrlExpiredException;
import com.example.urlshortner.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;
    private final String baseUrl;


    public UrlController(UrlService urlService, @Value("${app.base-url}") String baseUrl) {
        this.urlService = urlService;
        this.baseUrl = baseUrl;
    }

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("urlRequest", new UrlRequest());
        return "url/index";
    }

    @PostMapping
    public String shortenUrl(@Valid @ModelAttribute UrlRequest urlRequest,
                             BindingResult result,
                             Model model,
                             Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("org.springframework.validation.BindingResult.urlRequest", result);
            model.addAttribute("urlRequest", urlRequest);
            return "url/index";
        }

        try {
            UrlResponse response = urlService.shortenUrl(urlRequest, principal.getName());
//            return "redirect:/urls/" + response.getShortCode();
            model.addAttribute("urlResponse", response);

            return "url/result";
        } catch (Exception e) {
            model.addAttribute("error", "Error shortening URL: " + e.getMessage());
            return "url/index";
        }
    }

    @GetMapping("/{shortCode}")
    public String getUrlStats(@PathVariable String shortCode, Model model) {
        UrlResponse response = urlService.getUrlStats(shortCode);
        model.addAttribute("stats", response);
        model.addAttribute("baseUrl", baseUrl);
        return "url/stats";
    }
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<UrlResponse> urls = urlService.getAllUrls(pageable);
        model.addAttribute("urls", urls);
        model.addAttribute("success", null);
        model.addAttribute("error", null);
        return "url/list";
    }

    @GetMapping("/my-urls")
    public String listUrls(
            @RequestParam(defaultValue = "0") int userPage,
            @RequestParam(defaultValue = "0") int allPage,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "my-urls") String activeTab,
            Principal principal,
            Model model) {

        Pageable pageable = PageRequest.of(userPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("userUrls", urlService.getUrlsByUsername(principal.getName(), pageable));

        // For admin view
        Pageable adminPageable = PageRequest.of(allPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("allUrls", urlService.getAllUrls(adminPageable));

        // Add activeTab to model to preserve tab state
        model.addAttribute("activeTab", activeTab);

        return "url/list";
    }

    @PostMapping("/{shortCode}/deactivate")
    public String deactivateUrl(
            @PathVariable String shortCode,
            RedirectAttributes redirectAttributes) {

        try {
            UrlResponse response = urlService.deactivateUrl(shortCode);
            redirectAttributes.addFlashAttribute("success", "URL deactivated successfully");
        } catch (ShortCodeNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/urls/" + shortCode;
    }

    @PostMapping("/{shortCode}/activate")
    public String activateUrl(
            @PathVariable String shortCode,
            RedirectAttributes redirectAttributes) {

        try {
            UrlResponse response = urlService.activateUrl(shortCode);
            redirectAttributes.addFlashAttribute("success", "URL activated successfully");
        } catch (ShortCodeNotFoundException | UrlExpiredException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/urls/" + shortCode;
    }

    @PostMapping("/{shortCode}/delete")
    public String deleteUrl(
            @PathVariable String shortCode,
            RedirectAttributes redirectAttributes) {

        try {
            urlService.deleteUrl(shortCode);
            redirectAttributes.addFlashAttribute("success", "URL deleted successfully");
            return "redirect:/urls/my-urls";
        } catch (ShortCodeNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/urls/" + shortCode;
        }
    }
}