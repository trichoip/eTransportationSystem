package com.etransportation.payload.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.etransportation.payload.dto.IdDTO;

import lombok.Data;

@Data
public class ReviewCarRequest {

    private IdDTO book;
    private String content;
    @Max(value = 5, message = "Bạn phải đánh giá xe từ 1 đến 5")
    @Min(value = 1, message = "Bạn phải đánh giá xe từ 1 đến 5")
    private Integer starReview;

}
