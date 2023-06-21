package com.etransportation.payload.dto;

import com.etransportation.enums.AccountGender;
import com.etransportation.model.Department;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

public class EmployeeDto {

    @Data
    public static class EmployeeRegister {

        private Long id;

        @NotBlank
        private String name;

        @NotBlank
        private String username;

        @NotBlank
        private String password;

        private AccountGender gender;

        private Date birthDate;

        private String email;

        private String phone;
        private String avatar;
        private String thumnail;

        @NotNull
        private Department department;
    }
}
