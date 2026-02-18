package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PatientMedicalProfileResponse {

    private String firstName;
    private String lastName;
    private Double height;
    private Double weight;
    private String goal;
    private String restrictions;
    private String notes;
    private Double bmi;
    private List<MedicalFileResponse> files;

}