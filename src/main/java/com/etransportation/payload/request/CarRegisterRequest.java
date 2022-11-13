package com.etransportation.payload.request;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.etransportation.payload.dto.CarImageDTO;
import com.etransportation.payload.dto.IdDTO;

import lombok.Data;

@Data
public class CarRegisterRequest {

    private int seats;
    @Min(value = 1, message = "Giá phải lớn hơn 0")
    private double price;
    private String fuel;
    @NotEmpty(message = "Vui lòng nhập biển số.")
    private String licensePlates;
    private String description;
    private String transmission;
    private int yearOfManufacture;
    private int saleWeek;
    private int saleMonth;
    private double longitude;
    private double latitude;

    private IdDTO account;

    private @Valid IdDTO model;
    private IdDTO ward;
    private String street;

    @Size(min = 3, max = 10, message = "Xe ít nhất phải có 3 ảnh, vui lòng thêm ảnh cho xe")
    @NotEmpty(message = "Xe ít nhất phải có 3 ảnh, vui lòng thêm ảnh cho xe")
    private List<@Valid CarImageDTO> carImages;

    private Set<IdDTO> features;

}