package com.etransportation.repository;

import com.etransportation.model.Schedules;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulesRepository extends JpaRepository<Schedules, Long> {
    Optional<Schedules> findByIdAndCompany_Departments_Accounts_Username(Long id, String username);
}
