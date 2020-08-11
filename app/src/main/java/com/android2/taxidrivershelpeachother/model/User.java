package com.android2.taxidrivershelpeachother.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String firstName, lastName, phone, dateOfBirth;
    private String imageUrl = new String(), status = "Waiting for approval";
    private TaxiInformation taxiInformation;
    private double balance;
    private List<Complaint> complaints = new ArrayList<>();
    private List<ShuttleItem> soldLeads = new ArrayList<>(), boughtLeads= new ArrayList<>();
    private boolean isAvailable;

    public User(String firstName, String lastName, String phone, String dateOfBirth, String imageUrl, TaxiInformation taxiInformation, double balance, boolean isAvailable) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.taxiInformation = taxiInformation;
        this.balance = balance;
        this.dateOfBirth = dateOfBirth;
        this.isAvailable = isAvailable;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public TaxiInformation getTaxiInformation() {
        return taxiInformation;
    }

    public void setTaxiInformation(TaxiInformation taxiInformation) {
        this.taxiInformation = taxiInformation;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Complaint> getComplaints() {
        return complaints;
    }

    public void setComplaints(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    public List<ShuttleItem> getSoldLeads() {
        return soldLeads;
    }

    public void setSoldLeads(List<ShuttleItem> soldLeads) {
        this.soldLeads = soldLeads;
    }

    public List<ShuttleItem> getBoughtLeads() {
        return boughtLeads;
    }

    public void setBoughtLeads(List<ShuttleItem> boughtLeads) {
        this.boughtLeads = boughtLeads;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
