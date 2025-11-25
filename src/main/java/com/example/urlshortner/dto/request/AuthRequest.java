package com.example.urlshortner.dto.request;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.Email;
>>>>>>> c8a73bf542437b4a82eb4ae49705ad7fcf75a231
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
