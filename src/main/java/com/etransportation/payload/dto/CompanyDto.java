package com.etransportation.payload.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

public class CompanyDto {

    @Data
    public static class CompanyPost {

        private Long id;

        @NotBlank
        private String name;
    }
}
