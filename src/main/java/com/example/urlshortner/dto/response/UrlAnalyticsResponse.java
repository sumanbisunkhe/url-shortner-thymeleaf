package com.example.urlshortner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlAnalyticsResponse {
    private long totalUrlCount;
    private long activeUrlCount;
    private long totalClicks;
    private double averageClickRate;
    private List<UrlResponse> recentUrls;
}