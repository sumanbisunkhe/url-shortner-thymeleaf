package com.example.urlshortner.service;

import com.example.urlshortner.dto.request.UrlRequest;
import com.example.urlshortner.dto.response.UrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UrlService {
    UrlResponse shortenUrl(UrlRequest urlRequest, String username);
    String getOriginalUrl(String shortCode);
    UrlResponse getUrlStats(String shortCode);
    Page<UrlResponse> getUrlsByUsername(String username, Pageable pageable);
    Page<UrlResponse> getAllUrls(Pageable pageable);
    UrlResponse deactivateUrl(String shortCode);
    UrlResponse activateUrl(String shortCode);
    void deleteUrl(String shortCode);

}