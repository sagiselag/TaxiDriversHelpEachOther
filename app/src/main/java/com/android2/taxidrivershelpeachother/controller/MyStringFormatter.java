package com.android2.taxidrivershelpeachother.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class MyStringFormatter {

    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static DateTimeFormatter htf = DateTimeFormatter.ofPattern("HH:mm");
    public static SimpleDateFormat dateAndTimeSimpleDateFormatUTC = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static SimpleDateFormat onlyDateSimpleDateFormatUTC = new SimpleDateFormat("dd/MM/yyyy");
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static LocalDateTime today;
    private static LocalDateTime tomorrow;
    private static LocalDateTime date;


    static public String getTimeStr(int hourOfDay, int minute){
        String timeStr;

        if(hourOfDay < 10){
            timeStr = "0" + hourOfDay;
        }
        else{
            timeStr = String.valueOf(hourOfDay);
        }
        timeStr += ":";
        if (minute > 9) {
            timeStr += minute;
        } else {
            timeStr += "0" + minute;
        }

        return timeStr;
    }

    public static String getTodayDateString(){
        today = LocalDateTime.now();
        return dtf.format(today);
    }

    public static String getCurrentTimeString(){
        today = LocalDateTime.now();
        return htf.format(today);
    }

    public static String getTomorrowDateString(){
        today = LocalDateTime.now();
        tomorrow = today.plus(1, ChronoUnit.DAYS);
        return dtf.format(tomorrow);
    }

    public static String getNextDayDateString(String dateStr){
        return getDayPlusNDaysDateString(dateStr, 1);
    }

    public static String getDayPlusNDaysDateString(String dateStr, int amountOfDaysToAdd){
        date = LocalDateTime.parse(dateStr, dateTimeFormatter);
        return dtf.format(date.plus(amountOfDaysToAdd, ChronoUnit.DAYS));
    }

    public static String getTimePlusNMinutesString(String timeStr, int amountOfMinutesToAdd){
        int hour, minutes, index;
        timeStr = timeStr.replaceAll(" ","");
        index = timeStr.indexOf(":");
        hour = Integer.valueOf(timeStr.substring(0, index));
        minutes = Integer.valueOf(timeStr.substring(index+1)) + amountOfMinutesToAdd;

        if(minutes > 59){
            minutes -= 60;
            hour += 1;
            if(hour > 23){
                hour = 0;
            }
        }

        return getTimeStr(hour, minutes);
    }

    public static String getTimeMinusNMinutesString(String timeStr, int amountOfMinutesToAdd){
        return getTimePlusNMinutesString(timeStr, -amountOfMinutesToAdd);
    }

    public static String getMaxTimeString(){
        today = LocalDateTime.now();
        today.plus(1, ChronoUnit.HOURS);

        return htf.format(today);
    }

    public static String getMaxDateString(){
        today = LocalDateTime.now();
        today.plus(1, ChronoUnit.HOURS);

        return dtf.format(today);
    }
}
