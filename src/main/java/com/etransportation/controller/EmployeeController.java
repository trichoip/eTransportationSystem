package com.etransportation.controller;

import com.etransportation.payload.dto.EmployeeDto.EmployeeUpdate;
import com.etransportation.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/schedules")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getSchedules(Authentication authentication) {
        return ResponseEntity.ok(employeeService.findAllSchedulesOfEmployee(authentication));
    }

    @GetMapping("/timekeeping")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllTimeKeeping(
        @Schema(allowableValues = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }) @RequestParam(required = true) int month,
        @Schema(allowableValues = { "2020", "2021", "2022", "2022", "2023", "2024", "2025" }) @RequestParam(required = true) int year,
        Authentication authentication
    ) {
        return ResponseEntity.ok(employeeService.findAllTimeKeepingOfEmployee(month, year, authentication));
    }

    @PutMapping
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "Gender enum =>  FEMALE, MALE, OTHER")
    public ResponseEntity<?> updateEmployee(@Valid @RequestBody EmployeeUpdate employeeUpdate, Authentication authentication) {
        return ResponseEntity.ok(employeeService.updateEmployee(employeeUpdate, authentication));
    }

    @GetMapping
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getDetailEmployee(Authentication authentication) {
        return ResponseEntity.ok(employeeService.findDetailEmployee(authentication));
    }
}
