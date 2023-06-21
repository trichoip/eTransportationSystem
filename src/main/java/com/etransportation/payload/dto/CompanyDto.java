package com.etransportation.payload.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

public class CompanyDto {

    @Data
    public static class CompanyPost {

        private Long id;

        @NotBlank
        private String name;
    }

    @Data
    public static class CompanyList {

        private Long id;

        private String name;

        private List<SchedulesDto> schedules = new ArrayList<>();

        private List<DepartmentDto> departments = new ArrayList<>();
    }
}
