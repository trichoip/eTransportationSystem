package com.etransportation.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenVM {

    private String accessToken;
    private String refreshToken;
}
