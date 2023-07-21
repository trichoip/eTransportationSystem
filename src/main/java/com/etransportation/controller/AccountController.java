package com.etransportation.controller;

import com.etransportation.payload.request.AccountInfoRequest;
import com.etransportation.payload.request.AccountRegisterRequest;
import com.etransportation.payload.request.ChangePasswordRequest;
import com.etransportation.payload.request.DriverLicenseInfoRequest;
import com.etransportation.payload.request.LoginRequest;
import com.etransportation.payload.response.AccountInfoResponse;
import com.etransportation.payload.response.DriverLicenseInfoResponse;
import com.etransportation.payload.response.LoginResponse;
import com.etransportation.payload.response.LoginResponse.JWTToken;
import com.etransportation.payload.response.TokenVM;
import com.etransportation.security.jwt.JwtTokenProvider;
import com.etransportation.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Operation(summary = "signin", description = "description", tags = "authentication")
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody LoginRequest loginRequest, Errors errors) {
        if (errors.hasErrors()) {
            List<String> errorList = errors.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorList);
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(),
            loginRequest.getPassword()
        );
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.createToken(authentication);
        String Refreshjwt = jwtTokenProvider.generateRefreshToken(authentication.getName());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        LoginResponse loginResponse = accountService.login(loginRequest);
        loginResponse.setJwtToken(new JWTToken(jwt, Refreshjwt));
        return new ResponseEntity<>(loginResponse, httpHeaders, HttpStatus.OK);
    }

    @Operation(summary = "refresh token", description = "description", tags = "authentication")
    @GetMapping("/refresh-token")
    public ResponseEntity<?> getAccessTokenFromRefreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        if (StringUtils.hasText(refreshToken) && jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity.ok(new TokenVM(jwtTokenProvider.generateAccessToken(refreshToken), refreshToken));
        } else {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
    }

    @Operation(summary = "signup", description = "description", tags = "authentication")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AccountRegisterRequest registerRequest) {
        accountService.register(registerRequest);
        // return ResponseEntity.ok().body("Register successfully");
        return ResponseEntity.ok("Đăng ký thành công");
        // return new ResponseEntity<>("Register successfully", HttpStatus.OK);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        if (changePasswordRequest.getId() == null) {
            throw new IllegalArgumentException("Error: Id is null!");
        }
        accountService.changePassword(changePasswordRequest);
        return ResponseEntity.ok("Thay đổi mật khẩu thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInfoAccountById(@PathVariable Long id) {
        AccountInfoResponse accountResponse = accountService.findAccountById(id);
        return ResponseEntity.ok(accountResponse);
    }

    @PutMapping
    public ResponseEntity<?> updateAccount(@Valid @RequestBody AccountInfoRequest accountInfoRequest, Errors errors) {
        if (errors.hasErrors()) {
            throw new IllegalArgumentException(errors.getFieldError().getDefaultMessage());
        }

        if (accountInfoRequest.getId() == null) {
            throw new IllegalArgumentException("Id is null!");
        }
        accountService.updateInfoAccount(accountInfoRequest);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @GetMapping("/driver/{id}")
    public ResponseEntity<?> getDriverLicenseInfo(@PathVariable Long id) {
        DriverLicenseInfoResponse getDriverLicense = accountService.findAccountDriverLicenseInfo(id);
        return ResponseEntity.ok(getDriverLicense);
    }

    @PostMapping("/driver")
    public ResponseEntity<?> updateDriverLicenseInfo(@Valid @RequestBody DriverLicenseInfoRequest driverLicenseInfoRequest, Errors errors) {
        if (errors.hasErrors()) {
            throw new IllegalArgumentException(errors.getFieldError().getDefaultMessage());
        }
        if (driverLicenseInfoRequest.getAccount_Id() == null) {
            throw new IllegalArgumentException("account Id is null!");
        }
        accountService.updateDriverLicenseInfo(driverLicenseInfoRequest);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @GetMapping("/paypal")
    public ResponseEntity<?> paypal() {
        Map<String, String[]> map = new HashMap<>();
        map.put("paypal", new String[] { "sb-kk6pm22027434@business.example.com", "3o9^RD7}" });
        map.put("receive", new String[] { "sb-z47src21290005@business.example.com", "0FCkuV<u" });
        map.put("link", new String[] { "https://www.sandbox.paypal.com/mep/dashboard" });
        return ResponseEntity.ok(map);
    }
}
