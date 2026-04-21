package com.example.petadoptionservice.dto.adoption;

import com.example.petadoptionservice.entity.AdoptionStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = false)
public class AdoptionStatusUpdateRequest {

    @NotNull
    private AdoptionStatus status;

    public AdoptionStatus getStatus() {
        return status;
    }

    public void setStatus(AdoptionStatus status) {
        this.status = status;
    }
}
