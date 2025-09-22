package com.taha.medchatai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineInteractionResult {

    private boolean hasInteraction;
    private String severity; // none, mild, moderate, severe
    private String interactionSeverity;
    private String mechanism;
    private List<String> symptoms;
    private List<String> riskGroups;
    private List<String> recommendations;
    private String disclaimer;

    // Backward compatibility için getter
    public String getSeverity() {
        return severity != null ? severity : interactionSeverity;
    }

    // Backward compatibility için setter
    public void setSeverity(String severity) {
        this.severity = severity;
        this.interactionSeverity = severity;
    }
}
