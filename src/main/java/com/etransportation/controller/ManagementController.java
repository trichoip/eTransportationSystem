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
import com.etransportation.payload.dto.TimeKeepingDto.EmployeeTimeKeeping;
import com.etransportation.payload.dto.TimeKeepingDto.TimeKeepingPost;
import com.etransportation.repository.AccountRepository;
import com.etransportation.repository.CompanyRepository;
import com.etransportation.repository.DepartmentRepository;
import com.etransportation.repository.RoleRepository;
import com.etransportation.repository.SchedulesRepository;
import com.etransportation.repository.TimeKeepingRepository;
import com.etransportation.util.DateConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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

    private final DepartmentRepository departmentRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final CompanyRepository companyRepository;
    private final SchedulesRepository schedulesRepository;
    private final TimeKeepingRepository timeKeepingRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @GetMapping("/employee")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllEmployee(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        Page<Account> accounts = accountRepository.findDistinctByRoles_NameIn(new RoleAccount[] { RoleAccount.ADMIN, RoleAccount.MANAGER }, pageable);
        List<EmployeeRegister> listEmployee = modelMapper.map(accounts.getContent(), new TypeToken<List<EmployeeRegister>>() {}.getType());
        return ResponseEntity.ok(new PageImpl<>(listEmployee, pageable, accounts.getTotalElements()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/employee/{id}")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getDetailEmployee(@PathVariable Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("employee not found"));
        if (!account.getRoles().stream().anyMatch(role -> role.getName() == RoleAccount.ADMIN || role.getName() == RoleAccount.MANAGER)) {
            throw new IllegalArgumentException("this account is USER , not manage or employee");
        }
        return ResponseEntity.ok(modelMapper.map(account, EmployeeRegister.class));
    }

    @Transactional
    @PostMapping("/employee")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "Gender enum =>  FEMALE, MALE, OTHER")
    public ResponseEntity<?> saveEmployee(@Valid @RequestBody EmployeeRegister employeeRegister) {
        if (employeeRegister.getId() != null && employeeRegister.getId() != 0) {
            throw new IllegalArgumentException("them moi employee không được có id");
        }
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

        EmployeeRegister register = modelMapper.map(accountRepository.save(account), EmployeeRegister.class);
        register.setPassword(employeeRegister.getPassword());
        return ResponseEntity.ok(register);
    }

    @Transactional
    @PutMapping("/employee")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"), description = "Gender enum =>  FEMALE, MALE, OTHER")
    public ResponseEntity<?> updateEmployee(@Valid @RequestBody EmployeeRegister employeeRegister) {
        if (employeeRegister.getId() == null) {
            throw new IllegalArgumentException("update employee cần có id");
        }

        ModelMapper modelMapperEmployeeRegister = new ModelMapper();
        modelMapperEmployeeRegister
            .createTypeMap(EmployeeRegister.class, Account.class)
            .addMappings(mapper -> {
                mapper
                    .using((Converter<String, String>) ctx -> bCryptPasswordEncoder.encode(ctx.getSource()))
                    .map(EmployeeRegister::getPassword, Account::setPassword);
                // ! cách 3:
                // mapper
                //     .using((Converter<DepartmentDto, Department>) ctx -> modelMapper.map(ctx.getSource(), Department.class))
                //     .map(EmployeeRegister::getDepartment, Account::setDepartment);
            });

        EmployeeRegister account = accountRepository
            .findById(employeeRegister.getId())
            .map(empl -> {
                if (!empl.getUsername().equals(employeeRegister.getUsername())) {
                    if (accountRepository.existsByUsername(employeeRegister.getUsername())) {
                        throw new IllegalArgumentException("username da ton tai");
                    }
                }
                if (!departmentRepository.existsById(employeeRegister.getDepartment().getId())) {
                    throw new IllegalArgumentException("department Id not found");
                }
                // ! trong ManyToOne của account nếu dùng modelmapper để map DepartmentDto sang Department
                // ! thì modelmapper chỉ mapper propertier của DepartmentDto sang Department
                // ! cho nên khi add thì nó tạo mới Department nên không có attach nên nó sẽ không thay đổi nội dung của Department
                // ! nếu mà update thì khi findbyid Department thì nó đang được attach, mà dùng modelmapper để map DepartmentDto sang Department thì nó sẽ mapper propertier của DepartmentDto sang Department
                // !  mà chỉ map propertier thì Department vẫn đang đang được attach thì khi save nó sẽ thay đổi luôn nội dung của cha (Department)
                // ! nếu muốn update chỉ thay đổi khóa phụ của cha thì có 3 cách:
                // ! cách 1 : trước khi mapper thì set cha null vì set cha null thì mapper sẽ tạo mới class cha nên không còn được attach
                // ! cách 2 : thay vì dùng DepartmentDto.class thì dùng Department.class trong EmployeeRegister vì mapper mà khác class thì nó mapper properties còn nếu cùng class entity thì nó truyền thằng class vào và không có map properties , do truyền vào class nên nó không còn attach
                // ! cách 3 : dùng TypeMap (cách này giống cách 2 mà dùng TypeMap)
                empl.setDepartment(null);
                modelMapperEmployeeRegister.map(employeeRegister, empl);
                return empl;
            })
            .map(accountRepository::save)
            .map(empl -> modelMapper.map(empl, EmployeeRegister.class))
            .map(empl -> {
                empl.setPassword(employeeRegister.getPassword());
                return empl;
            })
            .orElseThrow(() -> new IllegalArgumentException("employee not found!"));

        return ResponseEntity.ok(account);
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

    @GetMapping("/timekeeping")
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> getAllTimeKeeping(
        @Schema(allowableValues = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }) @RequestParam(required = true) int month,
        @Schema(allowableValues = { "2020", "2021", "2022", "2022", "2023", "2024", "2025" }) @RequestParam(required = true) int year, // //
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        Date startDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01));
        Date endDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01).with(TemporalAdjusters.lastDayOfMonth()));
        Page<Account> accounts = accountRepository.findDistinctByRoles_NameIn(new RoleAccount[] { RoleAccount.ADMIN, RoleAccount.MANAGER }, pageable);
        Page<EmployeeTimeKeeping> employeeTimeKeepings = accounts
            .map(accountEntity -> {
                return modelMapper.map(accountEntity, EmployeeTimeKeeping.class);
            })
            .map(employeeTimeKeeping -> {
                List<TimeKeepingPost> timeKeepingPosts = timeKeepingRepository
                    .findByDateBetweenAndAccount_Id(startDate, endDate, employeeTimeKeeping.getId())
                    .stream()
                    .map(timekeepingEntity -> modelMapper.map(timekeepingEntity, TimeKeepingPost.class))
                    .toList();
                employeeTimeKeeping.setTimeKeepingList(timeKeepingPosts);
                return employeeTimeKeeping;
            });
        return ResponseEntity.ok(employeeTimeKeepings);
    }
}
