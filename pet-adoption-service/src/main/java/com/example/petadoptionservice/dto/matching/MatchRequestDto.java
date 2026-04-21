package com.example.petadoptionservice.dto.matching;

import com.example.petadoptionservice.entity.HomeType;

public class MatchRequestDto {

    private Integer userMagicTolerance;
    private String userHomeType;
    private Integer petDangerLevel;
    private String petTemperament;
    private Integer petMagicLevel;
    private String petSpecies;

    public MatchRequestDto(Integer magicTolerance,
                           HomeType homeType,
                           Integer dangerLevel,
                           String temperament,
                           Integer magicLevel,
                           String petSpecies) {
        this.userMagicTolerance = magicTolerance;
        this.userHomeType = homeType != null ? homeType.name() : null;
        this.petDangerLevel = dangerLevel;
        this.petTemperament = temperament;
        this.petMagicLevel = magicLevel;
        this.petSpecies = petSpecies;
    }

    public MatchRequestDto(Integer userMagicTolerance,
                           String userHomeType,
                           Integer petDangerLevel,
                           String petTemperament,
                           Integer petMagicLevel,
                           String petSpecies) {
        this.userMagicTolerance = userMagicTolerance;
        this.userHomeType = userHomeType;
        this.petDangerLevel = petDangerLevel;
        this.petTemperament = petTemperament;
        this.petMagicLevel = petMagicLevel;
        this.petSpecies = petSpecies;
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

    public String getPetTemperament() {
        return petTemperament;
    }

    public void setPetTemperament(String petTemperament) {
        this.petTemperament = petTemperament;
    }

    public Integer getPetMagicLevel() {
        return petMagicLevel;
    }

    public void setPetMagicLevel(Integer petMagicLevel) {
        this.petMagicLevel = petMagicLevel;
    }

    public String getPetSpecies() {
        return petSpecies;
    }

    public void setPetSpecies(String petSpecies) {
        this.petSpecies = petSpecies;
    }
}
