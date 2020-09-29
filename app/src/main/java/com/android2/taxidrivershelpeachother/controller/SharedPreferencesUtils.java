package com.android2.taxidrivershelpeachother.controller;

import android.content.SharedPreferences;
import android.view.View;

import com.android2.taxidrivershelpeachother.view.MainActivity;

public class SharedPreferencesUtils {
    private boolean isAvailable, usePremiumSettings, hasPremiumShuttlesSettingsCB;
    private String notificationTopic;
    private int timeBetweenNotifications, generalMaxDistanceInMinutes, premiumMaxDistanceInMinutes, premiumMinPrice;

    public void savePreferences(){
        MainActivity.savePreferences();
    }

    public boolean isHasPremiumShuttlesSettingsCB() {
        return hasPremiumShuttlesSettingsCB;
    }

    public void setHasPremiumShuttlesSettingsCB(boolean hasPremiumShuttlesSettingsCB) {
        this.hasPremiumShuttlesSettingsCB = hasPremiumShuttlesSettingsCB;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isUsePremiumSettings() {
        return usePremiumSettings;
    }

    public void setUsePremiumSettings(boolean usePremiumSettings) {
        this.usePremiumSettings = usePremiumSettings;
    }

    public String getNotificationTopic() {
        return notificationTopic;
    }

    public void setNotificationTopic(String notificationTopic) {
        this.notificationTopic = notificationTopic;
    }

    public int getTimeBetweenNotifications() {
        return timeBetweenNotifications;
    }

    public void setTimeBetweenNotifications(int timeBetweenNotifications) {
        this.timeBetweenNotifications = timeBetweenNotifications;
    }

    public int getGeneralMaxDistanceInMinutes() {
        return generalMaxDistanceInMinutes;
    }

    public void setGeneralMaxDistanceInMinutes(int generalMaxDistanceInMinutes) {
        this.generalMaxDistanceInMinutes = generalMaxDistanceInMinutes;
    }

    public int getPremiumMaxDistanceInMinutes() {
        return premiumMaxDistanceInMinutes;
    }

    public void setPremiumMaxDistanceInMinutes(int premiumMaxDistanceInMinutes) {
        this.premiumMaxDistanceInMinutes = premiumMaxDistanceInMinutes;
    }

    public int getPremiumMinPrice() {
        return premiumMinPrice;
    }

    public void setPremiumMinPrice(int premiumMinPrice) {
        this.premiumMinPrice = premiumMinPrice;
    }
}
