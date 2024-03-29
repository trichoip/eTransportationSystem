package com.etransportation.service.impl;

import com.etransportation.enums.TimeKeepingStatus;
import com.etransportation.model.Account;
import com.etransportation.model.Schedules;
import com.etransportation.model.TimeKeeping;
import com.etransportation.payload.dto.TimeKeepingDto.TimeKeepingPost;
import com.etransportation.repository.AccountRepository;
import com.etransportation.repository.SchedulesRepository;
import com.etransportation.repository.TimeKeepingRepository;
import com.etransportation.service.TimeKeepingService;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeKeepingServiceImpl implements TimeKeepingService {

    private final AccountRepository accountRepository;
    private final SchedulesRepository schedulesRepository;
    private final TimeKeepingRepository timeKeepingRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Object attendance(String type, Long schedulesId, Authentication authentication) {
        //#region
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails.isEnabled() == false) {
            throw new IllegalArgumentException("employee is RETIRED");
        }
        if (userDetails.isAccountNonLocked() == false) {
            throw new IllegalArgumentException("employee is block");
        }

        if (!schedulesRepository.existsById(schedulesId)) {
            throw new IllegalArgumentException("schedules khong ton tai");
        }

        Account account = accountRepository
            .findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        Schedules schedules = schedulesRepository
            .findByIdAndCompany_Departments_Accounts_Username(schedulesId, authentication.getName())
            .orElse(null);
        if (schedules == null) {
            throw new IllegalArgumentException("employee not have schedules id: " + schedulesId);
        }

        Date now = new Date();
        Time time = Time.valueOf(LocalTime.now());

        TimeKeeping timeKeeping = timeKeepingRepository
            .findByAccount_UsernameAndDateAndSchedules_Id(authentication.getName(), now, schedulesId)
            .orElse(null);

        if (type.equals("checkin")) {
            if (timeKeeping != null) {
                return new ResponseEntity<>(
                    "You already Check In today at: " + timeKeeping.getDate() + " " + timeKeeping.getTimein(),
                    HttpStatus.BAD_REQUEST
                );
            } else {
                timeKeeping = new TimeKeeping();
                if (time.before(schedules.getTimein())) {
                    timeKeeping.setStatus_timein(TimeKeepingStatus.IN_TIME);
                } else {
                    timeKeeping.setStatus_timein(TimeKeepingStatus.lATE_IN);
                    Duration duration = Duration.between(schedules.getTimein().toLocalTime(), time.toLocalTime());
                    timeKeeping.setMinutesLate(duration.toMinutes());
                }
                timeKeeping.setTimein(time);
                timeKeeping.setDate(now);
                timeKeeping.setSchedules(schedules);
                timeKeeping.setAccount(account);

                return ResponseEntity.ok(modelMapper.map(timeKeepingRepository.save(timeKeeping), TimeKeepingPost.class));
            }
        } else if (type.equals("checkout")) {
            if (timeKeeping == null) {
                return new ResponseEntity<>("You not have Check In today, pls checkin before checkout ", HttpStatus.BAD_REQUEST);
            }
            if (timeKeeping.getTimeout() != null) {
                return new ResponseEntity<>(
                    "You already Check Out today at: " + timeKeeping.getDate() + " " + timeKeeping.getTimeout(),
                    HttpStatus.BAD_REQUEST
                );
            }

            if (time.before(schedules.getTimeout())) {
                timeKeeping.setStatus_timeout(TimeKeepingStatus.EARLY_OUT);
                Duration duration = Duration.between(time.toLocalTime(), schedules.getTimeout().toLocalTime());
                timeKeeping.setMinutesOutEarly(duration.toMinutes());
            } else {
                timeKeeping.setStatus_timeout(TimeKeepingStatus.ON_TIME);
            }
            timeKeeping.setTimeout(time);

            Duration durationWorkingHours = Duration.between(
                ((Time) timeKeeping.getTimein()).toLocalTime(),
                ((Time) timeKeeping.getTimeout()).toLocalTime()
            );
            timeKeeping.setTotalWorkingHours(durationWorkingHours.toMinutes() / 60);

            return ResponseEntity.ok(modelMapper.map(timeKeepingRepository.save(timeKeeping), TimeKeepingPost.class));
        } else {
            throw new IllegalArgumentException("type khong hop le");
        }
        //#endregion
    }
}
