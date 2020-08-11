package com.android2.taxidrivershelpeachother.model;

public class TaxiInformation {
    private String licenseNumber, taxiNumber, licenseValidUntil, insuranceValidUntil, model;
    private boolean isAccessible;
    private int passengerSeats;

    public TaxiInformation(String licenseNumber, String model, String taxiNumber, String licenseValidUntil, String insuranceValidUntil, int passengerSeats, boolean isAccessible) {
        this.licenseNumber = licenseNumber;
        this.model = model;
        this.taxiNumber = taxiNumber;
        this.licenseValidUntil = licenseValidUntil;
        this.insuranceValidUntil = insuranceValidUntil;
        this.passengerSeats = passengerSeats;
        this.isAccessible = isAccessible;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getTaxiNumber() {
        return taxiNumber;
    }

    public void setTaxiNumber(String taxiNumber) {
        this.taxiNumber = taxiNumber;
    }

    public String getLicenseValidUntil() {
        return licenseValidUntil;
    }

    public void setLicenseValidUntil(String licenseValidUntil) {
        this.licenseValidUntil = licenseValidUntil;
    }

    public String getInsuranceValidUntil() {
        return insuranceValidUntil;
    }

    public void setInsuranceValidUntil(String insuranceValidUntil) {
        this.insuranceValidUntil = insuranceValidUntil;
    }

    public int getPassengerSeats() {
        return passengerSeats;
    }

    public void setPassengerSeats(int passengerSeats) {
        this.passengerSeats = passengerSeats;
    }
}
