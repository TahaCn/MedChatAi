package com.taha.medchatai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisResult {

    private String analysis;
    private List<PossibleCondition> possibleConditions;
    private List<String> redFlags;
    private String urgencyLevel; // self-care, doctor-visit, emergency
    private String emergencyLevel; // LOW, MEDIUM, HIGH
    private List<String> recommendations;
    private String disclaimer;
    private String sessionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PossibleCondition {
        private String name;
        private String likelihood; // low, moderate, high
        private String description;
    }
}
