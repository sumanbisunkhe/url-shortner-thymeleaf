package com.example.urlshortner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
<<<<<<< HEAD

=======
>>>>>>> c8a73bf542437b4a82eb4ae49705ad7fcf75a231
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private List<String> errors;
}