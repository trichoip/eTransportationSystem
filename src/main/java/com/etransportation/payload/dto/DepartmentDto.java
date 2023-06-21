package com.etransportation.payload.dto;

import com.etransportation.payload.dto.CompanyDto.CompanyPost;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentDto {

    private Long id;

    @NotBlank
    private String name;

    @Data
    public static class DepartmentPost {

        private Long id;

        @NotBlank
        private String name;

        @NotNull
        private CompanyPost company;
    }
}
