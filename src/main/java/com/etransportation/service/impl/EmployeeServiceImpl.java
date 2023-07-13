package com.etransportation.service.impl;

import com.etransportation.model.Account;
import com.etransportation.payload.dto.EmployeeDto.EmployeeRegister;
import com.etransportation.payload.dto.EmployeeDto.EmployeeUpdate;
import com.etransportation.payload.dto.SchedulesDto;
import com.etransportation.payload.dto.TimeKeepingDto.EmployeeTimeKeeping;
import com.etransportation.payload.dto.TimeKeepingDto.TimeKeepingPost;
import com.etransportation.repository.AccountRepository;
import com.etransportation.repository.DepartmentRepository;
import com.etransportation.repository.TimeKeepingRepository;
import com.etransportation.service.EmployeeService;
import com.etransportation.util.DateConverter;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final AccountRepository accountRepository;
    private final TimeKeepingRepository timeKeepingRepository;
    private final ModelMapper modelMapper;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    @Override
    public List<?> findAllSchedulesOfEmployee(Authentication authentication) {
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

        return listSchedulesDto;
    }

    @Transactional(readOnly = true)
    @Override
    public EmployeeTimeKeeping findAllTimeKeepingOfEmployee(int month, int year, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails.isEnabled() == false) {
            throw new IllegalArgumentException("employee is RETIRED");
        }
        if (userDetails.isAccountNonLocked() == false) {
            throw new IllegalArgumentException("employee is block");
        }

        Date startDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01));
        Date endDate = DateConverter.convertToDateViaInstant(LocalDate.of(year, month, 01).with(TemporalAdjusters.lastDayOfMonth()));

        return accountRepository
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
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeRegister findDetailEmployee(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails.isEnabled() == false) {
            throw new IllegalArgumentException("employee is RETIRED");
        }
        if (userDetails.isAccountNonLocked() == false) {
            throw new IllegalArgumentException("employee is block");
        }

        Account account = accountRepository
            .findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("employee not found"));
        return modelMapper.map(account, EmployeeRegister.class);
    }

    @Override
    @Transactional
    public EmployeeRegister updateEmployee(EmployeeUpdate employeeUpdate, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails.isEnabled() == false) {
            throw new IllegalArgumentException("employee is RETIRED");
        }
        if (userDetails.isAccountNonLocked() == false) {
            throw new IllegalArgumentException("employee is block");
        }

        EmployeeRegister account = accountRepository
            .findByUsername(authentication.getName())
            .map(empl -> {
                if (!departmentRepository.existsById(employeeUpdate.getDepartment().getId())) {
                    throw new IllegalArgumentException("department Id not found");
                }
                // ! lưu ý cần set null father để không còn attack
                empl.setDepartment(null);
                modelMapper.map(employeeUpdate, empl);
                return empl;
            })
            .map(accountRepository::save)
            .map(empl -> modelMapper.map(empl, EmployeeRegister.class))
            .orElseThrow(() -> new IllegalArgumentException("employee not found!"));

        return account;
    }
}
