package com.example.petadoptionservice.repository;

import com.example.petadoptionservice.entity.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {

    List<AdoptionRequest> findAllByUser_Email(String email);
    List<AdoptionRequest> findAllByPetIdAndIdNot(Long petId, Long id);
    boolean existsByUser_EmailAndPetId(String email, Long petId);
}
