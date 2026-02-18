package com.nutriflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private Long menuId;
    private Long batchId;
    private Integer year;
    private Integer month;
    private String dietaryNotes;
    private String status;
    private List<MenuItemResponse> items;

}