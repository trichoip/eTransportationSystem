package com.etransportation.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "feature")
@Data
@EqualsAndHashCode(callSuper = true)
public class Feature extends Base {

    @Column(columnDefinition = "nvarchar(100)")
    private String name;

    private String icon;

    // relationship

    @ManyToMany(mappedBy = "features", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<Car>();

    // getter and setter

}
