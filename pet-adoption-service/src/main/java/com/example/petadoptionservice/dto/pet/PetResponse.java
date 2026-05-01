package com.example.petadoptionservice.dto.pet;

import com.example.petadoptionservice.entity.AdoptionStatus;

public class PetResponse {

    private Long id;
    private String name;
    private String species;
    private Integer dangerLevel;
    private String temperament;
    private Integer magicLevel;
    private AdoptionStatus adoptionStatus;
    private String description;
    private String imageUrl;

    public PetResponse() {
    }

    public PetResponse(Long id, String name, String species, Integer dangerLevel, String temperament,
                       Integer magicLevel, AdoptionStatus adoptionStatus, String description,String imageUrl) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.dangerLevel = dangerLevel;
        this.temperament = temperament;
        this.magicLevel = magicLevel;
        this.adoptionStatus = adoptionStatus;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public Integer getDangerLevel() {
        return dangerLevel;
    }

    public String getTemperament() {
        return temperament;
    }

    public Integer getMagicLevel() {
        return magicLevel;
    }

    public AdoptionStatus getAdoptionStatus() {
        return adoptionStatus;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
