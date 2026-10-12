package com.nimbusfreight.entity;

import com.nimbusfreight.domain.CargoType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trucks")
public class Truck {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo_type", nullable = false, length = 16)
    private CargoType cargoType;

    protected Truck() {
    }

    public Truck(String id, String name, CargoType cargoType) {
        this.id = id;
        this.name = name;
        this.cargoType = cargoType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CargoType getCargoType() {
        return cargoType;
    }
}
