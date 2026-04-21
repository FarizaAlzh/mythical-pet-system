package com.example.petadoptionservice.controller;

import com.example.petadoptionservice.dto.adoption.AdoptionRequestCreateDto;
import com.example.petadoptionservice.dto.adoption.AdoptionResponseDto;
import com.example.petadoptionservice.dto.adoption.AdoptionStatusUpdateRequest;
import com.example.petadoptionservice.service.AdoptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/adoptions")
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdoptionResponseDto createAdoption(@Valid @RequestBody AdoptionRequestCreateDto requestDto,
                                              Authentication authentication) {
        return adoptionService.createAdoptionRequest(authentication.getName(), requestDto);
    }

    @GetMapping
    public List<AdoptionResponseDto> getAllAdoptions(Authentication authentication) {
        return adoptionService.getAllAdoptions(authentication.getName());
    }

    @PatchMapping("/{id}/status")
    public AdoptionResponseDto updateAdoptionStatus(@PathVariable Long id,
                                                    @Valid @RequestBody AdoptionStatusUpdateRequest request,
                                                    Authentication authentication) {
        return adoptionService.updateAdoptionStatus(authentication.getName(), id, request);
    }
}
