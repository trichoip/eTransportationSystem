package com.etransportation.payload.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class IdDTO {

    @Min(value = 1, message = "Vui lòng chọn mẫu xe")
    private Long id;
}
