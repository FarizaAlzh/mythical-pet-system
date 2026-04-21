package com.example.petadoptionservice.dto.adoption;

import com.example.petadoptionservice.entity.AdoptionStatus;
import java.time.LocalDateTime;

public class AdoptionResponseDto {

    private Long id;
    private Long userId;
    private Long petId;
    private AdoptionStatus status;
    private Integer compatibilityScore;
    private LocalDateTime createdAt;

    public AdoptionResponseDto() {
    }

    public AdoptionResponseDto(Long id, Long userId, Long petId, AdoptionStatus status,
                               Integer compatibilityScore, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.status = status;
        this.compatibilityScore = compatibilityScore;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPetId() {
        return petId;
    }

    public AdoptionStatus getStatus() {
        return status;
    }

    public Integer getCompatibilityScore() {
        return compatibilityScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
