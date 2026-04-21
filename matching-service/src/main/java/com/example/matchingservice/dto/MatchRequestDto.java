package com.example.matchingservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MatchRequestDto {

    @NotNull
    @Min(1)
    @Max(10)
    private Integer userMagicTolerance;

    @NotBlank
    private String userHomeType;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer petDangerLevel;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer petMagicLevel;

    @NotBlank
    private String petTemperament;

    private String petSpecies;

    public MatchRequestDto() {
    }

    public Integer getUserMagicTolerance() {
        return userMagicTolerance;
    }

    public void setUserMagicTolerance(Integer userMagicTolerance) {
        this.userMagicTolerance = userMagicTolerance;
    }

    public String getUserHomeType() {
        return userHomeType;
    }

    public void setUserHomeType(String userHomeType) {
        this.userHomeType = userHomeType;
    }

    public Integer getPetDangerLevel() {
        return petDangerLevel;
    }

    public void setPetDangerLevel(Integer petDangerLevel) {
        this.petDangerLevel = petDangerLevel;
    }

    public Integer getPetMagicLevel() {
        return petMagicLevel;
    }

    public void setPetMagicLevel(Integer petMagicLevel) {
        this.petMagicLevel = petMagicLevel;
    }

    public String getPetTemperament() {
        return petTemperament;
    }

    public void setPetTemperament(String petTemperament) {
        this.petTemperament = petTemperament;
    }

    public String getPetSpecies() {
        return petSpecies;
    }

    public void setPetSpecies(String petSpecies) {
        this.petSpecies = petSpecies;
    }
}
