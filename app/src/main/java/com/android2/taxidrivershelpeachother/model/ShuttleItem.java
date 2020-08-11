package com.android2.taxidrivershelpeachother.model;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class ShuttleItem implements Serializable {

    private String originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, publishedBy;
    private int distanceToShuttleInMinutes, price, commissionFee;
    private double shuttleDistanceInKm, distanceToShuttleInKm;
    private int shuttleDistanceInMinutes;
    private Passenger passenger;
    private LatLng originLatLng;
    private String id, handlingDriverPhone;

    public String getHandlingDriverPhone() {
        return handlingDriverPhone;
    }

    public void setHandlingDriverPhone(String handlingDriverPhone) {
        this.handlingDriverPhone = handlingDriverPhone;
    }

    public ShuttleItem(LatLng originLatLng, String originAddress, String destinationAddress, String remarks, String shuttleDate, String shuttleTime, int price, int commissionFee, Passenger passenger, double shuttleDistanceInKm, int shuttleDistanceInMinutes) {
        this.originLatLng = originLatLng;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.remarks = remarks;
        this.shuttleDate = shuttleDate;
        this.shuttleTime = shuttleTime;
        this.price = price;
        this.commissionFee = commissionFee;
        this.passenger = passenger;
        this.shuttleDistanceInKm = shuttleDistanceInKm;
        this.shuttleDistanceInMinutes = shuttleDistanceInMinutes;
    }

    private void createDateAndTimeInMillis(){
        String myDate = "2014/10/29 18:10:45";

        LocalDateTime localDateTime = LocalDateTime.parse(myDate,
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss") );
/*
  With this new Date/Time API, when using a date, you need to
  specify the Zone where the date/time will be used. For your case,
  seems that you want/need to use the default zone of your system.
  Check which zone you need to use for specific behaviour e.g.
  CET or America/Lima
*/
        long millis = localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
    }

    public double getDistanceToShuttleInKm() {
        return distanceToShuttleInKm;
    }

    public void setDistanceToShuttleInKm(double distanceToShuttleInKm) {
        this.distanceToShuttleInKm = distanceToShuttleInKm;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

    public LatLng getOriginLatLng() {
        return originLatLng;
    }

    public void setOriginLatLng(LatLng originLatLng) {
        this.originLatLng = originLatLng;
    }

    public String getShuttleDate() {
        return shuttleDate;
    }

    public void setShuttleDate(String shuttleDate) {
        this.shuttleDate = shuttleDate;
    }

    public void setShuttleTime(String shuttleTime) {
        this.shuttleTime = shuttleTime;
    }

    public String getShuttleTime() {
        return shuttleTime;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getDistanceToShuttleInMinutes() {
        return distanceToShuttleInMinutes;
    }

    public void setDistanceToShuttleInMinutes(int distanceToShuttleInMinutes) {
        this.distanceToShuttleInMinutes = distanceToShuttleInMinutes;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCommissionFee() {
        return commissionFee;
    }

    public void setCommissionFee(int commissionFee) {
        this.commissionFee = commissionFee;
    }

    public double getShuttleDistanceInKm() {
        return shuttleDistanceInKm;
    }

    public void setShuttleDistanceInKm(double shuttleDistanceInKm) {
        this.shuttleDistanceInKm = shuttleDistanceInKm;
    }

    public int getShuttleDistanceInMinutes() {
        return shuttleDistanceInMinutes;
    }

    public void setShuttleDistanceInMinutes(int shuttleDistanceInMinutes) {
        this.shuttleDistanceInMinutes = shuttleDistanceInMinutes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}