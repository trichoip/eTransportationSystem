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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.etransportation.model.Book;
import com.etransportation.repository.BookRepository;

@Component
public class ScheduledTasks {

    @Autowired
    private BookRepository bookRepository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() throws InterruptedException {
        log.info("Task1 - " + new Date());
    }

    @Scheduled(fixedDelay = 10000)
    public void reportCurrentTime() throws IOException {

        List<Book> list = bookRepository.findAll();

        ExcelExporter excelExporter = new ExcelExporter(list);

        excelExporter.export();
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void reportCurrentTime2() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }
}