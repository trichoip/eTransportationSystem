package com.etransportation.payload.response;

import com.etransportation.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String username;
    private String email;
    private AccountStatus status;
    private JWTToken jwtToken;

    @Data
    @AllArgsConstructor
    public static class JWTToken {

        private String token;
        private String refreshToken;
    }
}
