package com.etransportation.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginRequest {

    @Schema(example = "manage")
    @NotEmpty(message = "Username not be empty")
    @Length(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
    private String username;

    @Schema(example = "1")
    @NotEmpty(message = "Password must not be empty")
    @Length(min = 1, max = 100, message = "password must be between 1 and 100 characters")
    private String password;
}
