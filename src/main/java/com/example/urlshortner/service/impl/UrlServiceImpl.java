package com.example.urlshortner.service.impl;

import com.example.urlshortner.dto.request.UrlRequest;
import com.example.urlshortner.dto.response.UrlAnalyticsResponse;
import com.example.urlshortner.dto.response.UrlResponse;
import com.example.urlshortner.exception.ShortCodeNotFoundException;
import com.example.urlshortner.exception.UrlExpiredException;
import com.example.urlshortner.mapper.UrlMapper;
import com.example.urlshortner.model.ShortUrl;
import com.example.urlshortner.model.User;
import com.example.urlshortner.repository.ShortUrlRepository;
import com.example.urlshortner.repository.UserRepository;
import com.example.urlshortner.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final ShortUrlRepository repository;
    private  final UserRepository userRepository;
    private final UrlMapper mapper;


    @Override
    @Transactional
    public UrlResponse shortenUrl(UrlRequest urlRequest, String username) {
        String originalUrl = urlRequest.getOriginalUrl();
        int validityDays = urlRequest.getValidityDays();

        validateUrl(originalUrl);

        String shortCode = generateShortCode(originalUrl);
        while (repository.existsByShortCode(shortCode)) {
            shortCode = generateShortCode(originalUrl + System.currentTimeMillis());
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));

        ShortUrl newUrl = ShortUrl.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .expirationDate(LocalDateTime.now().plusDays(validityDays))
                .createdBy(username)
                .user(user)
                .build();

        return mapper.toResponse(repository.save(newUrl));
    }

    @Override
    @Transactional
    public String getOriginalUrl(String shortCode) {
        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found"));

        if (LocalDateTime.now().isAfter(shortUrl.getExpirationDate())) {
            shortUrl.setActive(false);
            throw new UrlExpiredException("This URL has expired");
        }

        if (!shortUrl.isActive()) {
            throw new DisabledException("URL is disabled");
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        return shortUrl.getOriginalUrl();
    }

    @Override
    public UrlResponse getUrlStats(String shortCode) {
        return mapper.toResponse(repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found")));
    }

    @Override
    public UrlResponse getUrlById(Long id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with id: " + id)));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<UrlResponse> getUrlsByUsername(String username, Pageable pageable) {
        Page<ShortUrl> shortUrls = repository.findAllByCreatedByOrderByCreatedAtDesc(username, pageable);
        return shortUrls.map(mapper::toResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<UrlResponse> getAllUrls(Pageable pageable) {
        Page<ShortUrl> shortUrls = repository.findAllByOrderByCreatedAtDesc(pageable);
        return shortUrls.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public UrlResponse deactivateUrl(String shortCode) {
        int updated = repository.updateActiveStatus(shortCode, false);
        if (updated == 0) {
            throw new ShortCodeNotFoundException("Short code not found");
        }
        return getUrlStats(shortCode);
    }

    @Override
    @Transactional
    public UrlResponse activateUrl(String shortCode) {
        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found"));

        if (LocalDateTime.now().isAfter(shortUrl.getExpirationDate())) {
            throw new UrlExpiredException("Cannot activate expired URL");
        }

        int updated = repository.updateActiveStatus(shortCode, true);
        return getUrlStats(shortCode);
    }
    @Override
    public UrlAnalyticsResponse getUrlAnalytics(Long userId) {
        // Get total URLs count
        long totalUrlCount = repository.countByUserId(userId);

        // Get active URLs count
        long activeUrlCount = repository.countByUserIdAndActiveAndExpirationDateAfter(
                userId,
                true,
                LocalDateTime.now()
        );

        // Get total clicks
        Long totalClicks = repository.sumClicksByUserId(userId);
        if (totalClicks == null) {
            totalClicks = 0L;
        }

        // Calculate average click rate
        double averageClickRate;
        if (totalUrlCount > 0) {
            BigDecimal rate = BigDecimal.valueOf(totalClicks)
                    .divide(BigDecimal.valueOf(totalUrlCount), 2, RoundingMode.HALF_UP);
            averageClickRate = rate.doubleValue();
        } else {
            averageClickRate = 0.0;
        }


        // Get recent URLs (last 5 created)
        Page<ShortUrl> recentUrls = repository.findAllByUserIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(0, 5)
        );

        return UrlAnalyticsResponse.builder()
                .totalUrlCount(totalUrlCount)
                .activeUrlCount(activeUrlCount)
                .totalClicks(totalClicks)
                .averageClickRate(averageClickRate)
                .recentUrls(recentUrls.map(mapper::toResponse).getContent())
                .build();
    }
    @Override
    @Transactional
    public void deleteUrl(String shortCode) {
        if (!repository.existsByShortCode(shortCode)) {
            throw new ShortCodeNotFoundException("Short code not found");
        }
        repository.deleteByShortCode(shortCode);
    }

    private String generateShortCode(String originalUrl) {
        try {
            String saltedUrl = originalUrl + UUID.randomUUID();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(saltedUrl.getBytes(StandardCharsets.UTF_8));
            long hashLong = bytesToLong(hashBytes);
            return base62Encode(hashLong).substring(0, 8);
        } catch (Exception e) {
            throw new RuntimeException("Error generating short code", e);
        }
    }

    private long bytesToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[i] & 0xFF);
        }
        return value & Long.MAX_VALUE;
    }

    private String base62Encode(long number) {
        if (number == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE62.charAt((int) (number % 62)));
            number /= 62;
        }
        return sb.reverse().toString();
    }

    private String regenerateShortCode(String originalCode) {
        return originalCode + BASE62.charAt(new Random().nextInt(BASE62.length()));
    }

    private void validateUrl(String url) {
        try {
            new URI(url).parseServerAuthority();
        } catch (URISyntaxException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid URL format");
        }
    }
}