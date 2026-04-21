package com.example.matchingservice.service;

import com.example.matchingservice.dto.MatchRequestDto;
import com.example.matchingservice.dto.MatchResponseDto;
import org.springframework.stereotype.Service;

@Service
public class MatchingService {

    public MatchResponseDto calculateMatchScore(MatchRequestDto requestDto) {
        int userMagicTolerance = valueOrZero(requestDto.getUserMagicTolerance()); // null == 0
        int petDangerLevel = valueOrZero(requestDto.getPetDangerLevel());
        int petMagicLevel = valueOrZero(requestDto.getPetMagicLevel());

        int score = 50;

        int toleranceVsDanger = userMagicTolerance - petDangerLevel;
        //если tolerance выше danger -> это хорошо
        if (toleranceVsDanger >= 2) {
            score += 25;
        } else if (toleranceVsDanger >= 0) {
            score += 15;
        } else if (toleranceVsDanger == -1) {
            score += 5;
        } else {
            score -= 20;
        }

        //тип бонуса если спокойное животное + КВ
        boolean calmApartmentMatch = equalsIgnoreCase(requestDto.getPetTemperament(), "calm")
                && equalsIgnoreCase(requestDto.getUserHomeType(), "APARTMENT");
        if (calmApartmentMatch) {
            score += 15;
        }

        //сравнение petMagicLevel и userMagicTolerance
        if (petMagicLevel > userMagicTolerance + 2) {
            score -= 20;
        } else if (petMagicLevel > userMagicTolerance) {
            score -= 10;
        } else {
            score += 10;
        }

        score = Math.max(0, Math.min(100, score));

        String explanation = buildExplanation(score, calmApartmentMatch, toleranceVsDanger, requestDto.getPetSpecies());
        return new MatchResponseDto(score, explanation);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    //стравнение строк без регистра
    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && left.equalsIgnoreCase(right);
    }

    private String buildExplanation(int score, boolean calmApartmentMatch, int toleranceVsDanger, String petSpecies) {
        String petType = petSpecies == null || petSpecies.isBlank() ? "pet" : petSpecies;

        if (score >= 80) {
            return "Strong match: your profile fits this " + petType + " well.";
        }

        if (score >= 60) {
            if (calmApartmentMatch) {
                return "Good match: calm temperament suits apartment living.";
            }
            return "Good match: you can likely manage this " + petType + ".";
        }

        if (toleranceVsDanger < 0) {
            return "Low match: the pet may be too dangerous for this user.";
        }
        return "Low match: the pet's magic level may be too challenging.";
    }
}
