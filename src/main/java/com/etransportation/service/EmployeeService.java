package com.etransportation.service;

import com.etransportation.payload.dto.TimeKeepingDto.EmployeeTimeKeeping;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface EmployeeService {
    List<?> findAllSchedulesOfEmployee(Authentication authentication);

    EmployeeTimeKeeping findAllTimeKeepingOfEmployee(int month, int year, Authentication authentication);
}
