package com.etransportation.payload.request;

import java.util.List;

import com.etransportation.enums.CarStatus;
import com.etransportation.enums.FilterType;
import com.etransportation.payload.dto.IdDTO;

import lombok.Data;

@Data
public class FilterCarSearchRequest {

    private IdDTO city;

    private CarStatus status;

    private FilterType sortPriceType;

    private Double[] priceBetween;

    private Integer[] seatsIn;

    private String fuel;

    private Integer[] yearOfManufactureBetween;

    private String transmission;

    private Long brand_Id;

    private Long[] model_Id_In;

    private Long[] feature_Id_in;
}
