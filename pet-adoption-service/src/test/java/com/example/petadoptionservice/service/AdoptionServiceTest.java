package com.example.petadoptionservice.service;

import com.example.petadoptionservice.dto.adoption.AdoptionResponseDto;
import com.example.petadoptionservice.dto.adoption.AdoptionStatusUpdateRequest;
import com.example.petadoptionservice.entity.AdoptionRequest;
import com.example.petadoptionservice.entity.AdoptionStatus;
import com.example.petadoptionservice.entity.Pet;
import com.example.petadoptionservice.entity.Role;
import com.example.petadoptionservice.entity.User;
import com.example.petadoptionservice.feign.MatchingClient;
import com.example.petadoptionservice.repository.AdoptionRequestRepository;
import com.example.petadoptionservice.repository.PetRepository;
import com.example.petadoptionservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private MatchingClient matchingClient;

    private AdoptionService adoptionService;

    @BeforeEach
    void setUp() {
        adoptionService = new AdoptionService(
                adoptionRequestRepository,
                userRepository,
                petRepository,
                matchingClient,
                "dev-internal-token"
        );
    }

    @Test
    void getAllAdoptionsShouldReturnOnlyCurrentUsersRequestsForRegularUser() {
        User user = buildUser(1L, "user@example.com", Role.USER);
        AdoptionRequest adoption = buildAdoption(10L, user, buildPet(100L), AdoptionStatus.PENDING);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(adoptionRequestRepository.findAllByUser_Email(user.getEmail())).thenReturn(List.of(adoption));

        List<AdoptionResponseDto> result = adoptionService.getAllAdoptions(user.getEmail());

        assertEquals(1, result.size());
        assertEquals(adoption.getId(), result.get(0).getId());
        verify(adoptionRequestRepository).findAllByUser_Email(user.getEmail());
        verify(adoptionRequestRepository, never()).findAll();
    }

    @Test
    void getAllAdoptionsShouldReturnAllRequestsForAdmin() {
        User admin = buildUser(1L, "admin@example.com", Role.ADMIN);
        AdoptionRequest first = buildAdoption(10L, buildUser(2L, "u1@example.com", Role.USER), buildPet(100L), AdoptionStatus.PENDING);
        AdoptionRequest second = buildAdoption(11L, buildUser(3L, "u2@example.com", Role.USER), buildPet(101L), AdoptionStatus.APPROVED);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(adoptionRequestRepository.findAll()).thenReturn(List.of(first, second));

        List<AdoptionResponseDto> result = adoptionService.getAllAdoptions(admin.getEmail());

        assertEquals(2, result.size());
        verify(adoptionRequestRepository).findAll();
        verify(adoptionRequestRepository, never()).findAllByUser_Email(admin.getEmail());
    }

    @Test
    void updateAdoptionStatusShouldAllowOnlyAdmin() {
        User user = buildUser(1L, "user@example.com", Role.USER);
        AdoptionStatusUpdateRequest request = new AdoptionStatusUpdateRequest();
        request.setStatus(AdoptionStatus.APPROVED);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class,
                () -> adoptionService.updateAdoptionStatus(user.getEmail(), 99L, request));
    }

    @Test
    void updateAdoptionStatusShouldApproveSelectedRequestAndRejectOtherPendingOnes() {
        User admin = buildUser(1L, "admin@example.com", Role.ADMIN);
        User applicant = buildUser(2L, "user@example.com", Role.USER);
        Pet pet = buildPet(100L);
        AdoptionRequest target = buildAdoption(10L, applicant, pet, AdoptionStatus.PENDING);
        AdoptionRequest otherPending = buildAdoption(11L, buildUser(3L, "other@example.com", Role.USER), pet, AdoptionStatus.PENDING);
        AdoptionRequest otherRejected = buildAdoption(12L, buildUser(4L, "third@example.com", Role.USER), pet, AdoptionStatus.REJECTED);
        AdoptionStatusUpdateRequest request = new AdoptionStatusUpdateRequest();
        request.setStatus(AdoptionStatus.APPROVED);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(adoptionRequestRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(adoptionRequestRepository.findAllByPetIdAndIdNot(pet.getId(), target.getId()))
                .thenReturn(List.of(otherPending, otherRejected));

        AdoptionResponseDto result = adoptionService.updateAdoptionStatus(admin.getEmail(), target.getId(), request);

        assertEquals(AdoptionStatus.APPROVED, result.getStatus());
        assertEquals(AdoptionStatus.APPROVED, pet.getAdoptionStatus());
        assertEquals(AdoptionStatus.REJECTED, otherPending.getStatus());
        assertEquals(AdoptionStatus.REJECTED, otherRejected.getStatus());
    }

    private User buildUser(Long id, String email, Role role) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail(email);
        user.setRole(role);
        user.setMagicTolerance(5);
        return user;
    }

    private Pet buildPet(Long id) {
        Pet pet = new Pet();
        ReflectionTestUtils.setField(pet, "id", id);
        pet.setSpecies("Dragon");
        pet.setDangerLevel(4);
        pet.setMagicLevel(5);
        pet.setTemperament("Calm");
        pet.setAdoptionStatus(AdoptionStatus.PENDING);
        return pet;
    }

    private AdoptionRequest buildAdoption(Long id, User user, Pet pet, AdoptionStatus status) {
        AdoptionRequest adoptionRequest = new AdoptionRequest();
        ReflectionTestUtils.setField(adoptionRequest, "id", id);
        adoptionRequest.setUser(user);
        adoptionRequest.setPet(pet);
        adoptionRequest.setStatus(status);
        adoptionRequest.setCompatibilityScore(75);
        adoptionRequest.setCreatedAt(LocalDateTime.now());
        return adoptionRequest;
    }
}
