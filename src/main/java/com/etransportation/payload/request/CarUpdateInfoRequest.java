package com.etransportation.payload.request;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.etransportation.payload.dto.CarImageDTO;
import com.etransportation.payload.dto.IdDTO;

import lombok.Data;

@Data
public class CarUpdateInfoRequest {

    private Long id;
    private double price;
    private String fuel;
    private String description;
    private String transmission;
    private int saleWeek;
    private int saleMonth;
    private double longitude;
    private double latitude;
    private IdDTO ward;
    private String street;

    @Size(min = 3, max = 10, message = "Xe ít nhất phải có 3 ảnh, vui lòng thêm ảnh cho xe")
    @NotEmpty(message = "Xe ít nhất phải có 3 ảnh, vui lòng thêm ảnh cho xe")
    private List<@Valid CarImageDTO> carImagesUpdate;

    private Set<IdDTO> featuresUpdate;

}
