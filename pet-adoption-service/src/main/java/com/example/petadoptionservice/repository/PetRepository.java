package com.example.petadoptionservice.repository;

import com.example.petadoptionservice.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
