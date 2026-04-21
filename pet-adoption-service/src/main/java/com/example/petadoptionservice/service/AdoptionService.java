package com.example.petadoptionservice.service;

import com.example.petadoptionservice.dto.adoption.AdoptionRequestCreateDto;
import com.example.petadoptionservice.dto.adoption.AdoptionResponseDto;
import com.example.petadoptionservice.dto.adoption.AdoptionStatusUpdateRequest;
import com.example.petadoptionservice.dto.matching.MatchRequestDto;
import com.example.petadoptionservice.dto.matching.MatchResponseDto;
import com.example.petadoptionservice.entity.AdoptionRequest;
import com.example.petadoptionservice.entity.AdoptionStatus;
import com.example.petadoptionservice.entity.Pet;
import com.example.petadoptionservice.entity.Role;
import com.example.petadoptionservice.entity.User;
import com.example.petadoptionservice.exception.MatchingServiceException;
import com.example.petadoptionservice.exception.ResourceNotFoundException;
import com.example.petadoptionservice.feign.MatchingClient;
import com.example.petadoptionservice.repository.AdoptionRequestRepository;
import com.example.petadoptionservice.repository.PetRepository;
import com.example.petadoptionservice.repository.UserRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdoptionService {

    private final AdoptionRequestRepository adoptionRequestRepository; // финальные птм чт как мы присвоили знач поменять уже нельзя
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final MatchingClient matchingClient;
    private final String matchingServiceInternalToken;

    //передаем все сюда
    public AdoptionService(AdoptionRequestRepository adoptionRequestRepository,
                           UserRepository userRepository,
                           PetRepository petRepository,
                           MatchingClient matchingClient,
                           @Value("${matching.service.internal-token}") String matchingServiceInternalToken) {
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
        this.matchingClient = matchingClient;
        this.matchingServiceInternalToken = matchingServiceInternalToken;
    }

    //создаем заявочку
    @Transactional
    public AdoptionResponseDto createAdoptionRequest(String userEmail, AdoptionRequestCreateDto requestDto) { //юзера через токен узнаем / AdoptionRequestCreateDto смотри на petId
        //ищем самое главное юзер(имейл) + пет(айди)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Pet pet = petRepository.findById(requestDto.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + requestDto.getPetId()));

        if (pet.getAdoptionStatus() == AdoptionStatus.APPROVED) {
            throw new IllegalArgumentException("This pet has already been adopted");
        }
        if (adoptionRequestRepository.existsByUser_EmailAndPetId(userEmail, requestDto.getPetId())) {
            throw new IllegalArgumentException("You have already created an adoption request for this pet");
        }

        //создаем пустую заявку
        AdoptionRequest adoptionRequest = new AdoptionRequest();
        adoptionRequest.setUser(user);
        adoptionRequest.setPet(pet);
        adoptionRequest.setStatus(AdoptionStatus.PENDING);
        adoptionRequest.setCompatibilityScore(fetchCompatibilityScore(user, pet)); //вызываем метод который на другом сервисе
        adoptionRequest.setCreatedAt(LocalDateTime.now());

        return toResponse(adoptionRequestRepository.save(adoptionRequest)); // сохраняем заявку в базу
    }

    // список заявок в разных ролях
    public List<AdoptionResponseDto> getAllAdoptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<AdoptionRequest> adoptions = user.getRole() == Role.ADMIN //показываем весь лист заявок
                ? adoptionRequestRepository.findAll()
                : adoptionRequestRepository.findAllByUser_Email(userEmail); // ну или только юзера

        return adoptions
                .stream() // список который обрабатывает все элементы
                .map(this::toResponse)
                .toList();
    }

    //меняем статус
    @Transactional // ведет себя как одна обшая операция
    public AdoptionResponseDto updateAdoptionStatus(String userEmail, Long adoptionId, AdoptionStatusUpdateRequest request) {
        User user = userRepository.findByEmail(userEmail) // ищем пользователя
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        if (user.getRole() != Role.ADMIN) { // если это не админ , пока
            throw new AccessDeniedException("Only admins can update adoption statuses");
        }

        AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(adoptionId) //находим заявку по айдишнику
                .orElseThrow(() -> new ResourceNotFoundException("Adoption request not found with id: " + adoptionId));

        //смотрим на наш статус в боди
        AdoptionStatus status = request.getStatus();
        adoptionRequest.setStatus(status);

        if (status == AdoptionStatus.APPROVED) {
            Pet pet = adoptionRequest.getPet();
            pet.setAdoptionStatus(AdoptionStatus.APPROVED);

            //тут все заявки на этого питомца
            adoptionRequestRepository.findAllByPetIdAndIdNot(pet.getId(), adoptionId)
                    //автоматом все REJECTED
                    .stream()
                    .filter(otherRequest -> otherRequest.getStatus() == AdoptionStatus.PENDING)
                    .forEach(otherRequest -> otherRequest.setStatus(AdoptionStatus.REJECTED));

            //если заявку REJECTEDнули
        } else if (status == AdoptionStatus.REJECTED) {
            // если хотя бы одна APPROVED
            boolean approvedExists = adoptionRequestRepository.findAllByPetIdAndIdNot(adoptionRequest.getPet().getId(), adoptionId)
                    .stream()
                    .anyMatch(otherRequest -> otherRequest.getStatus() == AdoptionStatus.APPROVED);
            //питомец снова становится доступным
            if (!approvedExists) {
                adoptionRequest.getPet().setAdoptionStatus(AdoptionStatus.PENDING);
            }
        }

        return toResponse(adoptionRequest);
    }


    private AdoptionResponseDto toResponse(AdoptionRequest adoptionRequest) { //entity to dto
        return new AdoptionResponseDto(
                adoptionRequest.getId(),
                adoptionRequest.getUser().getId(),
                adoptionRequest.getPet().getId(),
                adoptionRequest.getStatus(),
                adoptionRequest.getCompatibilityScore(),
                adoptionRequest.getCreatedAt()
        );
    }

    //работа со 2 сервисом через Feign
    private Integer fetchCompatibilityScore(User user, Pet pet) { // собираем все данные которые потом идут ко 2 сервису
        MatchRequestDto matchRequestDto = new MatchRequestDto(
                user.getMagicTolerance(),
                user.getHomeType(),
                pet.getDangerLevel(),
                pet.getTemperament(),
                pet.getMagicLevel(),
                pet.getSpecies()
        );

        try {
            //вызываем 2 сервис
            MatchResponseDto matchResponseDto = matchingClient.calculateMatchScore(
                    matchingServiceInternalToken,
                    matchRequestDto
            );
            //1) ошибка если вернули нам пустой балл
            if (matchResponseDto == null || matchResponseDto.getCompatibilityScore() == null) {
                throw new MatchingServiceException("Matching service returned an empty compatibility score.");
            }
            return matchResponseDto.getCompatibilityScore();

            //если это межсервисная ошибка
        } catch (MatchingServiceException ex) {
            throw ex;
        } catch (FeignException ex) {
            throw new MatchingServiceException("Matching service is unavailable. Please try again later.");
        } catch (Exception ex) {
            throw new MatchingServiceException("Could not calculate compatibility score right now.");
        }
    }
}
