package com.etransportation.controller;

import com.etransportation.enums.AccountStatus;
import com.etransportation.enums.RoleAccount;
import com.etransportation.model.Account;
import com.etransportation.model.Company;
import com.etransportation.model.Department;
import com.etransportation.model.Role;
import com.etransportation.model.Schedules;
import com.etransportation.payload.dto.CompanyDto.CompanyList;
import com.etransportation.payload.dto.CompanyDto.CompanyPost;
import com.etransportation.payload.dto.DepartmentDto.DepartmentPost;
import com.etransportation.payload.dto.EmployeeDto.EmployeeRegister;
import com.etransportation.payload.dto.SchedulesDto.SchedulesPost;
import com.etransportation.repository.AccountRepository;
import com.etransportation.repository.CompanyRepository;
import com.etransportation.repository.DepartmentRepository;
import com.etransportation.repository.RoleRepository;
import com.etransportation.repository.SchedulesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.HashSet;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class ManagementController {

    private final DepartmentRepository departmentRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final CompanyRepository companyRepository;
    private final SchedulesRepository schedulesRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @PostMapping("/employee")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "Gender enum =>  FEMALE, MALE, OTHER")
    public ResponseEntity<?> saveEmployee(@Valid @RequestBody EmployeeRegister employeeRegister) {
        if (accountRepository.existsByUsername(employeeRegister.getUsername())) {
            throw new IllegalArgumentException("username da ton tai");
        }
        if (!departmentRepository.existsById(employeeRegister.getDepartment().getId())) {
            throw new IllegalArgumentException("department Id not found");
        }
        Account account = modelMapper.map(employeeRegister, Account.class);
        account.setRoles(
            new HashSet<Role>() {
                {
                    add(roleRepository.findByName(RoleAccount.ADMIN).orElseGet(() -> Role.builder().name(RoleAccount.ADMIN).build()));
                }
            }
        );
        account.setPassword(bCryptPasswordEncoder.encode(employeeRegister.getPassword()));
        account.setStatus(AccountStatus.ACTIVE);
        return ResponseEntity.ok(modelMapper.map(accountRepository.save(account), EmployeeRegister.class));
    }

    @PostMapping("/schedules")
    @Operation(
        tags = "timekeeping",
        security = @SecurityRequirement(name = "token_auth"),
        description = "id khac 0 la update, id = 0 la add , format timein timeout => hh:mm:ss => 13:32:39 /n datefrom to format 2023-06-21"
    )
    public ResponseEntity<?> saveSchedules(@Valid @RequestBody SchedulesPost schedulesPost) {
        if (!companyRepository.existsById(schedulesPost.getCompany().getId())) {
            throw new IllegalArgumentException("company Id not found");
        }
        Schedules schedules = modelMapper.map(schedulesPost, Schedules.class);
        schedulesPost = modelMapper.map(schedulesRepository.save(schedules), SchedulesPost.class);
        return ResponseEntity.ok(schedulesPost);
    }

    @PostMapping("/company")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "id khac 0 la update, id = 0 la add")
    public ResponseEntity<?> saveCompany(@Valid @RequestBody CompanyPost companyPost) {
        Company company = modelMapper.map(companyPost, Company.class);
        return ResponseEntity.ok(companyRepository.save(company));
    }

    @GetMapping("/company")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllCompany() {
        List<Company> companies = companyRepository.findAll();
        List<CompanyList> listCompany = modelMapper.map(companies, new TypeToken<List<CompanyList>>() {}.getType());
        return ResponseEntity.ok(listCompany);
    }

    @PostMapping("/department")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "id khac 0 la update, id = 0 la add")
    public ResponseEntity<?> saveDepartment(@Valid @RequestBody DepartmentPost dept) {
        if (!companyRepository.existsById(dept.getCompany().getId())) {
            throw new IllegalArgumentException("company Id not found");
        }
        Department department = modelMapper.map(dept, Department.class);
        dept = modelMapper.map(departmentRepository.save(department), DepartmentPost.class);
        return ResponseEntity.ok(dept);
    }
}
