package com.example.petadoptionservice.service;

import com.example.petadoptionservice.dto.pet.PetCreateRequest;
import com.example.petadoptionservice.dto.pet.PetResponse;
import com.example.petadoptionservice.entity.AdoptionStatus;
import com.example.petadoptionservice.entity.Pet;
import com.example.petadoptionservice.exception.ResourceNotFoundException;
import com.example.petadoptionservice.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // работа с api/pets
    public List<PetResponse> getAllPets() {
        //достаем всех питомцев из нашей базы
        return petRepository.findAll()
                .stream()
                .map(this::toResponse) // превращаем всех питом в PetResponse
                .toList();
    }

    // работает api/pets/{id}
    public PetResponse getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + id)); // возвр животного если он конечно есть
        return toResponse(pet);
    }

    // создаем нового питомца его входные данные  PetCreateRequest
    public PetResponse createPet(PetCreateRequest request) {
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setDangerLevel(request.getDangerLevel());
        pet.setTemperament(request.getTemperament());
        pet.setMagicLevel(request.getMagicLevel());
        pet.setDescription(request.getDescription());
        pet.setImageUrl(request.getImageUrl());
        pet.setAdoptionStatus(AdoptionStatus.PENDING);//система по дефолту

        return toResponse(petRepository.save(pet));
    }

    //превращаем его в PetResponse
    private PetResponse toResponse(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getDangerLevel(),
                pet.getTemperament(),
                pet.getMagicLevel(),
                pet.getAdoptionStatus(),
                pet.getDescription(),
                pet.getImageUrl()
        );
    }
}
