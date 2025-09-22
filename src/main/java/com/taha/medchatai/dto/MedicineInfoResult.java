package com.taha.medchatai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineInfoResult {

    private String medicineName;
    private String activeIngredient;
    private List<String> indications;
    private String dosage;
    private List<String> sideEffects;
    private List<String> contraindications;
    private List<String> warnings;
    private List<String> commonSideEffects;
    private List<String> seriousSideEffects;
    private List<String> specialGroups;
    private String disclaimer;
}
