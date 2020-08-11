package com.android2.taxidrivershelpeachother.model;

import java.util.Calendar;
import java.util.Date;

public class Complaint {
    Date complaintDate;
    String reason;
    User submittedBy;

    public Complaint(String reason, User submittedBy){
        complaintDate = Calendar.getInstance().getTime();
        this.reason = reason;
        this.submittedBy = submittedBy;
    }
}
