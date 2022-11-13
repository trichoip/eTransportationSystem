package com.etransportation.payload.request;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DriverLicenseInfoRequest {

    private Long account_Id;

    @Min(value = 2147483647, message = "Số GPLX phải có 12 chữ số")
    @NotNull(message = "Số GPLX không được để trống")
    @NumberFormat(style = Style.NUMBER)
    private Long numberDrivingLicense;

    @NotEmpty(message = "Tên không được để trống")
    @NotBlank(message = "Tên không được để trống")
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    @NotEmpty(message = "Ảnh không được để trống")
    private String imageFront;
}
