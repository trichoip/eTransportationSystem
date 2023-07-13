package com.etransportation.service;

import com.etransportation.model.Company;
import com.etransportation.payload.dto.CompanyDto.CompanyList;
import com.etransportation.payload.dto.CompanyDto.CompanyPost;
import com.etransportation.payload.dto.DepartmentDto.DepartmentPost;
import com.etransportation.payload.dto.EmployeeDto.EmployeeRegister;
import com.etransportation.payload.dto.EmployeeDto.EmployeeUpdate;
import com.etransportation.payload.dto.SchedulesDto.SchedulesPost;
import com.etransportation.payload.dto.TimeKeepingDto.EmployeeTimeKeeping;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ManagementService {
    Page<?> findAllEmployee(String status, Pageable pageable);

    Object findDetailEmployee(Long id);

    EmployeeRegister saveEmployee(EmployeeRegister employeeRegister);

    EmployeeRegister updateEmployee(EmployeeUpdate employeeUpdate, Long id);

    SchedulesPost saveSchedules(SchedulesPost schedulesPost);

    Company saveCompany(CompanyPost companyPost);

    List<CompanyList> findAllCompany();

    DepartmentPost saveDepartment(DepartmentPost dept);

    Page<EmployeeTimeKeeping> findAllTimeKeepingOfAllEmployee(String status, int month, int year, Pageable pageable);

    EmployeeTimeKeeping findAllTimeKeepingByEmployeeId(int month, int year, Long id);
}
