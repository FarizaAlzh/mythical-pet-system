package com.example.petadoptionservice.controller;

import com.example.petadoptionservice.dto.pet.PetCreateRequest;
import com.example.petadoptionservice.dto.pet.PetResponse;
import com.example.petadoptionservice.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public List<PetResponse> getAllPets() {
        return petService.getAllPets();
    }

    @GetMapping("/{id}")
    public PetResponse getPetById(@PathVariable Long id) {
        return petService.getPetById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse createPet(@Valid @RequestBody PetCreateRequest request) {
        return petService.createPet(request);
    }
}
