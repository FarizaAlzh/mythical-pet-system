package com.example.matchingservice.controller;

import com.example.matchingservice.dto.MatchRequestDto;
import com.example.matchingservice.dto.MatchResponseDto;
import com.example.matchingservice.service.MatchingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/match-score")
    public ResponseEntity<MatchResponseDto> calculateMatchScore(@Valid @RequestBody MatchRequestDto requestDto) {
        return ResponseEntity.ok(matchingService.calculateMatchScore(requestDto));
    }
}
