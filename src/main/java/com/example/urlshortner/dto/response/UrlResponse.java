package com.example.urlshortner.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
<<<<<<< HEAD

=======
>>>>>>> c8a73bf542437b4a82eb4ae49705ad7fcf75a231
import java.time.LocalDateTime;

@Data
@Builder
public class UrlResponse {
    private String shortCode;
    private String originalUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;

    private int clickCount;
    private boolean active;
    private String createdBy;

}
