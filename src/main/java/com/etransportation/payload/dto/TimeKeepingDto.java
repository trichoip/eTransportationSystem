package com.etransportation.payload.dto;

import com.etransportation.enums.TimeKeepingStatus;
import com.etransportation.model.Schedules;
import java.util.Date;
import lombok.Data;

public class TimeKeepingDto {

    @Data
    public static class TimeKeepingPost {

        private Long id;

        private Date timein;

        private Date timeout;

        private Date date;

        private Long totalWorkingHours;

        private Long minutesLate;
        private Long minutesOutEarly;

        private TimeKeepingStatus status_timein;

        private TimeKeepingStatus status_timeout;

        private String reason;
        private String comment;

        private SchedulesDto schedules;
    }
}
