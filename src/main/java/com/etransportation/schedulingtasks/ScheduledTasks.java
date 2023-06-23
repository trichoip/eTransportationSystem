/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.etransportation.schedulingtasks;

import com.etransportation.enums.RoleAccount;
import com.etransportation.enums.TimeKeepingStatus;
import com.etransportation.model.Account;
import com.etransportation.model.Schedules;
import com.etransportation.model.TimeKeeping;
import com.etransportation.repository.AccountRepository;
import com.etransportation.repository.SchedulesRepository;
import com.etransportation.repository.TimeKeepingRepository;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final SchedulesRepository schedulesRepository;
    private final TimeKeepingRepository timeKeepingRepository;
    private final AccountRepository accountRepository;

    // @Autowired
    // private BookRepository bookRepository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    // @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() throws InterruptedException {
        log.info("Task1 - " + new Date());
    }

    // @Scheduled(fixedDelay = 10000)
    public void reportCurrentTime() throws IOException {
        // List<Book> list = bookRepository.findAll();
        // ExcelExporter excelExporter = new ExcelExporter(list);
        // excelExporter.export();
    }

    // @Scheduled(cron = "0 01 23 * 6,7 *")
    public void reportCurrentTime2() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }

    @Scheduled(cron = "00 01 23 * 6,7 *")
    @Transactional
    public void fakeTimeKeeping() {
        log.info("The time is now {}", dateFormat.format(new Date()));
        Date now = new Date();
        RoleAccount[] roleAccounts = new RoleAccount[] { RoleAccount.ADMIN, RoleAccount.MANAGER };
        List<Account> accountsHaveTimeKeepingIsDateNow = accountRepository.findDistinctByTimeKeepings_DateAndRoles_NameIn(now, roleAccounts);
        Long[] accountIds = accountsHaveTimeKeepingIsDateNow.stream().map(Account::getId).toArray(Long[]::new);
        accountIds = accountIds.length == 0 ? new Long[] { 0l } : accountIds;
        List<Account> accountsNotHaveTimeKeepingIsDateNow = accountRepository.findDistinctTop3ByIdNotInAndRoles_NameIn(accountIds, roleAccounts);
        List<Schedules> listSchedules = schedulesRepository.findAll();
        List<TimeKeeping> timeKeepings = accountsNotHaveTimeKeepingIsDateNow
            .stream()
            .map(account -> {
                Collections.shuffle(listSchedules); // * random
                Schedules schedules = listSchedules.get(0);
                Time timeIn = Time.valueOf(LocalTime.of(new Random().nextInt(8 - 7 + 1) + 7, new Random().nextInt(59 - 00 + 1) + 00)); // * random
                Time timeOut = Time.valueOf(LocalTime.of(new Random().nextInt(17 - 16 + 1) + 16, new Random().nextInt(59 - 00 + 1) + 00)); // * random

                TimeKeeping timeKeeping = new TimeKeeping();

                if (timeIn.before(schedules.getTimein())) {
                    timeKeeping.setStatus_timein(TimeKeepingStatus.IN_TIME);
                } else {
                    timeKeeping.setStatus_timein(TimeKeepingStatus.lATE_IN);
                    Duration duration = Duration.between(schedules.getTimein().toLocalTime(), timeIn.toLocalTime());
                    timeKeeping.setMinutesLate(duration.toMinutes());
                }
                timeKeeping.setTimein(timeIn);
                timeKeeping.setDate(now);
                timeKeeping.setSchedules(schedules);
                timeKeeping.setAccount(account);

                if (timeOut.before(schedules.getTimeout())) {
                    timeKeeping.setStatus_timeout(TimeKeepingStatus.EARLY_OUT);
                    Duration duration = Duration.between(timeOut.toLocalTime(), schedules.getTimeout().toLocalTime());
                    timeKeeping.setMinutesOutEarly(duration.toMinutes());
                } else {
                    timeKeeping.setStatus_timeout(TimeKeepingStatus.ON_TIME);
                }
                timeKeeping.setTimeout(timeOut);

                Duration durationWorkingHours = Duration.between(
                    ((Time) timeKeeping.getTimein()).toLocalTime(),
                    ((Time) timeKeeping.getTimeout()).toLocalTime()
                );

                timeKeeping.setTotalWorkingHours(durationWorkingHours.toMinutes() / 60);
                timeKeeping.setComment("auto");
                return timeKeeping;
            })
            .collect(Collectors.toList());
        timeKeepingRepository.saveAll(timeKeepings);
    }
}
