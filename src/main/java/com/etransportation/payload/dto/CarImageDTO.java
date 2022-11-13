package com.etransportation.payload.dto;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class CarImageDTO {

    @Length(min = 5, message = "Xe ít nhất phải có 3 ảnh, vui lòng thêm ảnh cho xe")
    @NotEmpty(message = "Xe ít nhất phải có 3 ảnh, vui lòng thêm ảnh cho xe")
    private String image;
}
