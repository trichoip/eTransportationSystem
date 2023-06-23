package com.etransportation.payload.dto;

import com.etransportation.enums.TimeKeepingStatus;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Data
    public static class EmployeeTimeKeeping {

        private Long id;

        private String name;

        private String avatar;

        private List<TimeKeepingPost> timeKeepingList = new ArrayList<>();
    }

    @Data
    public static class TimeKeepingPut {

        private Long id;

        private Time timein;

        private Time timeout;

        private Date date;

        private Long totalWorkingHours;

        private Long minutesLate;
        private Long minutesOutEarly;

        private TimeKeepingStatus status_timein;

        private TimeKeepingStatus status_timeout;

        private String reason;
        private String comment;
    }
}
