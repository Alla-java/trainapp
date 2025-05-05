package com.example.trainapp.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String house;
    private String building;
    private String number;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL)
    private List<FurnitureItem> furnitureItems = new ArrayList<>();

    public Apartment() {
        // обязателен для Hibernate
    }

    public Apartment(String street, String house, String building, String number) {
        this.street = street;
        this.house = house;
        this.building = building;
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}