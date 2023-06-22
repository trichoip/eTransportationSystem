package com.etransportation.payload.dto;

import com.etransportation.payload.dto.CompanyDto.CompanyPost;
import java.sql.Time;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchedulesDto {

    private Long id;

    private String name;

    private Time timein;

    private Time timeout;

    private Date datefrom;

    private Date dateto;

    @Data
    public static class SchedulesPost {

        private Long id;

        private String name;

        private Time timein;

        private Time timeout;

        private Date datefrom;

        private Date dateto;

        @NotNull
        private CompanyPost company;
    }
}
