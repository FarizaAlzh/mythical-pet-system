package com.example.petadoptionservice.dto.matching;

public class MatchResponseDto {

    private Integer compatibilityScore;
    private String explanation;

    public MatchResponseDto() {
    }

    public MatchResponseDto(Integer compatibilityScore, String explanation) {
        this.compatibilityScore = compatibilityScore;
        this.explanation = explanation;
    }

    public Integer getCompatibilityScore() {
        return compatibilityScore;
    }

    public void setCompatibilityScore(Integer compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}