package com.etransportation.controller;

import com.etransportation.payload.dto.CompanyDto.CompanyPost;
import com.etransportation.payload.dto.DepartmentDto.DepartmentPost;
import com.etransportation.payload.dto.EmployeeDto.EmployeeRegister;
import com.etransportation.payload.dto.SchedulesDto.SchedulesPost;
import com.etransportation.payload.dto.TimeKeepingDto.TimeKeepingPut;
import com.etransportation.service.ManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService managementService;

    @GetMapping("/employee")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllEmployee(
        @Schema(allowableValues = { "All", "ACTIVE", "RETIRED" }) @RequestParam(required = true) String status,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(managementService.findAllEmployee(status, pageable));
    }

    @GetMapping("/employee/{id}")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getDetailEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(managementService.findDetailEmployee(id));
    }

    @PostMapping("/employee")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "Gender enum =>  FEMALE, MALE, OTHER")
    public ResponseEntity<?> saveEmployee(@Valid @RequestBody EmployeeRegister employeeRegister) {
        return ResponseEntity.ok(managementService.saveEmployee(employeeRegister));
    }

    @PutMapping("/employee")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "Gender enum =>  FEMALE, MALE, OTHER")
    public ResponseEntity<?> updateEmployee(@Valid @RequestBody EmployeeRegister employeeRegister) {
        return ResponseEntity.ok(managementService.updateEmployee(employeeRegister));
    }

    @PostMapping("/schedules")
    @Operation(
        tags = "timekeeping",
        security = @SecurityRequirement(name = "token_auth"),
        description = "id khac 0 la update, id = 0 la add \n \n format timein timeout => hh:mm:ss => 13:32:39 /n datefrom to format 2023-06-21"
    )
    public ResponseEntity<?> saveSchedules(@Valid @RequestBody SchedulesPost schedulesPost) {
        return ResponseEntity.ok(managementService.saveSchedules(schedulesPost));
    }

    @PostMapping("/company")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "id khac 0 la update, id = 0 la add")
    public ResponseEntity<?> saveCompany(@Valid @RequestBody CompanyPost companyPost) {
        return ResponseEntity.ok(managementService.saveCompany(companyPost));
    }

    @GetMapping("/company")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllCompany() {
        return ResponseEntity.ok(managementService.findAllCompany());
    }

    @PostMapping("/department")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "id khac 0 la update, id = 0 la add")
    public ResponseEntity<?> saveDepartment(@Valid @RequestBody DepartmentPost dept) {
        return ResponseEntity.ok(managementService.saveDepartment(dept));
    }

    @GetMapping("/timekeeping")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllTimeKeepingOfAllEmployee(
        @Schema(allowableValues = { "All", "ACTIVE", "RETIRED" }) @RequestParam(required = true) String status,
        @Schema(allowableValues = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }) @RequestParam(required = true) int month,
        @Schema(allowableValues = { "2020", "2021", "2022", "2022", "2023", "2024", "2025" }) @RequestParam(required = true) int year,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(managementService.findAllTimeKeepingOfAllEmployee(status, month, year, pageable));
    }

    @GetMapping("/timekeeping/{id}")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllTimeKeepingByEmployeeId(
        @Schema(allowableValues = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }) @RequestParam(required = true) int month,
        @Schema(allowableValues = { "2020", "2021", "2022", "2022", "2023", "2024", "2025" }) @RequestParam(required = true) int year,
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(managementService.findAllTimeKeepingByEmployeeId(month, year, id));
    }

    @PutMapping("/timekeeping")
    @Operation(
        tags = "timekeeping",
        security = @SecurityRequirement(name = "token_auth"),
        description = "status_timein : IN_TIME , lATE_IN \n \n status_timeout : ON_TIME , EARLY_OUT  \n \n  format timein timeout => hh:mm:ss => 13:32:39 /n datefrom to format 2023-06-21 "
    )
    public ResponseEntity<?> updateTimeKeeping(@RequestBody TimeKeepingPut timeKeepingDto) {
        // todo: chua xong
        // if (timeKeepingDto.getId() == null) {
        //     throw new IllegalArgumentException("update TimeKeeping cần có id");
        // }
        // TimeKeepingPut timeKeepingPut = timeKeepingRepository
        //     .findById(timeKeepingDto.getId())
        //     .map(entity -> {
        //         modelMapper.map(timeKeepingDto, entity);
        //         return entity;
        //     })
        //     .map(timeKeepingRepository::save)
        //     .map(entity -> modelMapper.map(entity, TimeKeepingPut.class))
        //     .orElseThrow(() -> new IllegalArgumentException("TimeKeeping not found"));

        return ResponseEntity.ok("chua xong");
    }
}
