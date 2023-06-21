package com.etransportation.controller;

import com.etransportation.model.Account;
import com.etransportation.payload.dto.SchedulesDto;
import com.etransportation.repository.AccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/schedules")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getSchedules(Authentication authentication) {
        Account account = accountRepository
            .findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("employee is not found!"));

        if (account.getDepartment() == null) {
            // neu xoa deptartment thi cho nay null
            // throw new IllegalArgumentException("employee not have Department, pls ");
        }
        List<SchedulesDto> listSchedulesDto = modelMapper.map(
            account.getDepartment().getCompany().getSchedules(),
            new TypeToken<List<SchedulesDto>>() {}.getType()
        );
        return ResponseEntity.ok(listSchedulesDto);
    }
}
