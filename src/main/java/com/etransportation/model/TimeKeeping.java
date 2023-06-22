package com.etransportation.model;

import com.etransportation.enums.TimeKeepingStatus;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeKeeping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIME)
    private Date timein;

    @Temporal(TemporalType.TIME)
    private Date timeout;

    @Temporal(TemporalType.DATE)
    private Date date;

    private Long totalWorkingHours;

    private Long minutesLate;
    private Long minutesOutEarly;

    @Enumerated(EnumType.STRING)
    private TimeKeepingStatus status_timein;

    @Enumerated(EnumType.STRING)
    private TimeKeepingStatus status_timeout;

    private String reason;
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedules_id")
    private Schedules schedules;
}
