package com.etransportation.payload.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class FeatureDTO {

    private Long id;
    @JsonIgnore
    private String name;
    @JsonIgnore
    private String icon;
}
