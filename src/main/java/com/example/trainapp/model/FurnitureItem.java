package com.example.trainapp.model;
import javax.persistence.*;

@Entity
public class FurnitureItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double unitPrice;
    private int quantity;

    @Column(name = "total_price")
    private double totalPrice;

    @ManyToOne
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private FurnitureType type;

    public FurnitureItem() {
        // обязателен для Hibernate
    }

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        this.totalPrice = unitPrice * quantity;
    }

    public FurnitureItem(double unitPrice, int quantity, Apartment apartment, FurnitureType type) {
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.apartment = apartment;
        this.type = type;
        calculateTotalPrice();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    public FurnitureType getFurnitureType() {
        return type;
    }

    public void setFurnitureType(FurnitureType type) {
        this.type = type;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
