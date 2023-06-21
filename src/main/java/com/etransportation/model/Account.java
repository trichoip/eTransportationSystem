package com.etransportation.model;

import com.etransportation.enums.AccountGender;
import com.etransportation.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "account", uniqueConstraints = { @UniqueConstraint(columnNames = { "username" }) })
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Account extends Base {

    @Column(columnDefinition = "nvarchar(50)")
    private String name;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String username;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)")
    private AccountGender gender;

    @Column(columnDefinition = "date")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private Date birthDate;

    @Column(columnDefinition = "varchar(30)")
    private String email;

    @Column(columnDefinition = "varchar(20)")
    private String phone;

    private String avatar;
    private String thumnail;

    // neu la Double , Integer, Long ... thi se co gia tri defaul la null
    // neu int , long , double ... thi se co gia tri default khac null
    private Double balance;

    // jpa listing auditing
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @Temporal(TemporalType.DATE)
    @CreatedDate
    private Date joinDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @Column(columnDefinition = "datetime2(0)")
    @LastModifiedDate
    private Date modifiedDate;

    @Column(columnDefinition = "nvarchar(50)")
    @CreatedBy
    private String createdBy;

    @Column(columnDefinition = "nvarchar(50)")
    @LastModifiedBy
    private String modifiedBy;

    // ------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)")
    private AccountStatus status;

    // relationship

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "like_table",
        joinColumns = @JoinColumn(name = "account_id"),
        inverseJoinColumns = @JoinColumn(name = "car_id"),
        uniqueConstraints = { @UniqueConstraint(columnNames = { "account_id", "car_id" }) }
    )
    private List<Car> likeCars = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "account_role", joinColumns = @JoinColumn(name = "account_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private DrivingLicense drivingLicense;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    // getter and setter

    @PreRemove
    private void preRemove() {}
}
