package com.etransportation.repository;

import com.etransportation.model.TimeKeeping;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeKeepingRepository extends JpaRepository<TimeKeeping, Long> {
    Optional<TimeKeeping> findByAccount_UsernameAndDateAndSchedules_Id(String username, Date date, Long schedulesId);
}
