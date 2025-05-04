package com.example.urlshortner.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

@Getter
@Data
public class UrlRequest {
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    private String originalUrl;

    @Min(value = 1, message = "Validity days must be at least 1")
    @Max(value = 365, message = "Validity days cannot exceed 365")
    private int validityDays = 30;
}