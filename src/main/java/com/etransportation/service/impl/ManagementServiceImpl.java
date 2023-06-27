package com.etransportation.service.impl;

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
import com.etransportation.service.ManagementService;
import com.etransportation.util.DateConverter;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagementServiceImpl implements ManagementService {

    private final DepartmentRepository departmentRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final CompanyRepository companyRepository;
    private final SchedulesRepository schedulesRepository;
    private final TimeKeepingRepository timeKeepingRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<?> findAllEmployee(String status, Pageable pageable) {
        AccountStatus[] accountStatus = new AccountStatus[] { AccountStatus.ACTIVE, AccountStatus.RETIRED, AccountStatus.BLOCKED };
        switch (status) {
            case "All":
                break;
            case "RETIRED":
                accountStatus = new AccountStatus[] { AccountStatus.RETIRED };
                break;
            case "ACTIVE":
                accountStatus = new AccountStatus[] { AccountStatus.ACTIVE };
                break;
            default:
                break;
        }

        RoleAccount[] roleAccounts = new RoleAccount[] { RoleAccount.ADMIN, RoleAccount.MANAGER };
        Page<Account> accounts = accountRepository.findDistinctByRoles_NameInAndStatusIn(roleAccounts, pageable, accountStatus);
        List<EmployeeRegister> listEmployee = modelMapper.map(accounts.getContent(), new TypeToken<List<EmployeeRegister>>() {}.getType());
        return new PageImpl<>(listEmployee, pageable, accounts.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Object findDetailEmployee(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("employee not found"));
        if (!account.getRoles().stream().anyMatch(role -> role.getName() == RoleAccount.ADMIN || role.getName() == RoleAccount.MANAGER)) {
            throw new IllegalArgumentException("this account is USER , not manage or employee");
        }
        return modelMapper.map(account, EmployeeRegister.class);
    }

    @Override
    @Transactional
    public EmployeeRegister saveEmployee(EmployeeRegister employeeRegister) {
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

        return register;
    }

    @Override
    @Transactional
    public EmployeeRegister updateEmployee(EmployeeRegister employeeRegister) {
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
        accountRepository.getReferenceById(1L);
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

        return account;
    }

    @Override
    @Transactional
    public SchedulesPost saveSchedules(SchedulesPost schedulesPost) {
        if (!companyRepository.existsById(schedulesPost.getCompany().getId())) {
            throw new IllegalArgumentException("company Id not found");
        }
        Schedules schedules = modelMapper.map(schedulesPost, Schedules.class);
        return modelMapper.map(schedulesRepository.save(schedules), SchedulesPost.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyList> findAllCompany() {
        List<Company> companies = companyRepository.findAll();
        List<CompanyList> listCompany = modelMapper.map(companies, new TypeToken<List<CompanyList>>() {}.getType());
        return listCompany;
    }

    @Override
    @Transactional
    public DepartmentPost saveDepartment(DepartmentPost dept) {
        if (!companyRepository.existsById(dept.getCompany().getId())) {
            throw new IllegalArgumentException("company Id not found");
        }
        Department department = modelMapper.map(dept, Department.class);
        return modelMapper.map(departmentRepository.save(department), DepartmentPost.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeTimeKeeping> findAllTimeKeepingOfAllEmployee(String status, int month, int year, Pageable pageable) {
        AccountStatus[] accountStatus = new AccountStatus[] { AccountStatus.ACTIVE, AccountStatus.RETIRED, AccountStatus.BLOCKED };
        switch (status) {
            case "All":
                break;
            case "RETIRED":
                accountStatus = new AccountStatus[] { AccountStatus.RETIRED };
                break;
            case "ACTIVE":
                accountStatus = new AccountStatus[] { AccountStatus.ACTIVE };
                break;
            default:
                break;
        }
        RoleAccount[] roleAccounts = new RoleAccount[] { RoleAccount.ADMIN, RoleAccount.MANAGER };
        Date startDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01));
        Date endDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01).with(TemporalAdjusters.lastDayOfMonth()));
        Page<Account> accounts = accountRepository.findDistinctByRoles_NameInAndStatusIn(roleAccounts, accountStatus, pageable);

        return accounts
            .map(accountEntity -> {
                return modelMapper.map(accountEntity, EmployeeTimeKeeping.class);
            })
            .map(employeeTimeKeeping -> {
                List<TimeKeepingPost> timeKeepingPosts = timeKeepingRepository
                    .findByDateBetweenAndAccount_Id(startDate, endDate, employeeTimeKeeping.getId())
                    .stream()
                    .map(timekeepingEntity -> modelMapper.map(timekeepingEntity, TimeKeepingPost.class))
                    .collect(Collectors.toList());
                employeeTimeKeeping.setTimeKeepingList(timeKeepingPosts);
                return employeeTimeKeeping;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeTimeKeeping findAllTimeKeepingByEmployeeId(int month, int year, Long id) {
        Date startDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01));
        Date endDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01).with(TemporalAdjusters.lastDayOfMonth()));
        return accountRepository
            .findDistinctByIdAndRoles_NameIn(id, new RoleAccount[] { RoleAccount.ADMIN, RoleAccount.MANAGER })
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
    }

    @Override
    @Transactional
    public Company saveCompany(CompanyPost companyPost) {
        Company company = modelMapper.map(companyPost, Company.class);
        return companyRepository.save(company);
    }
}
