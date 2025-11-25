package com.example.urlshortner.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
<<<<<<< HEAD

=======
>>>>>>> c8a73bf542437b4a82eb4ae49705ad7fcf75a231
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private int statusCode;
    private String message;
    private T data;
    private String error;
    private String path;
}