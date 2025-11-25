package com.example.urlshortner.repository;

import com.example.urlshortner.model.ShortUrl;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);
    @Modifying
    @Transactional
    @Query("UPDATE ShortUrl s SET s.active = :active WHERE s.shortCode = :shortCode")
    int updateActiveStatus(String shortCode, boolean active);

    @Modifying
    @Transactional
    default void deleteByShortCode(String shortCode) {

    }

    Page<ShortUrl> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ShortUrl> findAllByCreatedByOrderByCreatedAtDesc(String username, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndActiveAndExpirationDateAfter(
            Long userId,
            boolean active,
            LocalDateTime expirationDate
    );

    @Query("SELECT SUM(s.clickCount) FROM ShortUrl s WHERE s.user.id = :userId")
    Long sumClicksByUserId(Long userId);

    Page<ShortUrl> findAllByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );
    @Query("SELECT s FROM ShortUrl s WHERE s.id = :id")
    Page<ShortUrl> findById(@Param("id") Long id, Pageable pageable);
}