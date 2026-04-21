package com.example.petadoptionservice.dto.adoption;

import jakarta.validation.constraints.NotNull;

public class AdoptionRequestCreateDto {

    @NotNull
    private Long petId;

    public AdoptionRequestCreateDto() {
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }
}
