package com.etransportation.controller;

import com.etransportation.model.Account;
import com.etransportation.payload.dto.SchedulesDto;
import com.etransportation.payload.dto.TimeKeepingDto.EmployeeTimeKeeping;
import com.etransportation.payload.dto.TimeKeepingDto.TimeKeepingPost;
import com.etransportation.repository.AccountRepository;
import com.etransportation.repository.TimeKeepingRepository;
import com.etransportation.util.DateConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final AccountRepository accountRepository;
    private final TimeKeepingRepository timeKeepingRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/schedules")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getSchedules(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails.isEnabled() == false) {
            throw new IllegalArgumentException("employee is RETIRED");
        }
        if (userDetails.isAccountNonLocked() == false) {
            throw new IllegalArgumentException("employee is block");
        }

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

    @GetMapping("/timekeeping")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllTimeKeeping(
        @Schema(allowableValues = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }) @RequestParam(required = true) int month,
        @Schema(allowableValues = { "2020", "2021", "2022", "2022", "2023", "2024", "2025" }) @RequestParam(required = true) int year, // //
        Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails.isEnabled() == false) {
            throw new IllegalArgumentException("employee is RETIRED");
        }
        if (userDetails.isAccountNonLocked() == false) {
            throw new IllegalArgumentException("employee is block");
        }

        Date startDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01));
        Date endDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01).with(TemporalAdjusters.lastDayOfMonth()));

        EmployeeTimeKeeping employeeTimeKeepingDto = accountRepository
            .findByUsername(authentication.getName())
            .map(accountEntity -> modelMapper.map(accountEntity, EmployeeTimeKeeping.class))
            .map(employeeTimeKeeping -> {
                List<TimeKeepingPost> timeKeepingPosts = timeKeepingRepository
                    .findByDateBetweenAndAccount_Id(startDate, endDate, employeeTimeKeeping.getId())
                    .stream()
                    .map(timekeepingEntity -> modelMapper.map(timekeepingEntity, TimeKeepingPost.class))
                    .collect(Collectors.toList());
                employeeTimeKeeping.setTimeKeepingList(timeKeepingPosts);
                return employeeTimeKeeping;
            })
            .orElseThrow(() -> new IllegalArgumentException("employee is not found!"));

        return ResponseEntity.ok(employeeTimeKeepingDto);
    }
}
