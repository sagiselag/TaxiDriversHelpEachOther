package com.android2.taxidrivershelpeachother.model;

import android.content.Context;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.widget.EditText;

import com.android2.taxidrivershelpeachother.R;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class MyPhoneTextWatcher extends PhoneNumberFormattingTextWatcher {
    private EditText phoneNumberET;
    private final Context context;
    private final String countryCode;
    private final int PHONE_NUMBER_LENGTH = 10;
    private final InputFilter[] filterArray = new InputFilter[1];



    public MyPhoneTextWatcher(Context context, EditText editText){
        this.context = context;
        this.phoneNumberET = editText;
        countryCode = "+ ("+ String.valueOf(String.valueOf(getCurrentCountryCode(context))) + ") ";
        filterArray[0] = new InputFilter.LengthFilter(countryCode.length() + PHONE_NUMBER_LENGTH);
        if(phoneNumberET != null)
        {
            this.phoneNumberET.setFilters(filterArray);
            Selection.setSelection(this.phoneNumberET.getText(), this.phoneNumberET.getText().length());
        }
    }

    public String getCountryCode() {
        return countryCode;
    }

    private int getCurrentCountryCode(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = telephonyManager.getSimCountryIso().toUpperCase();
        return PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryIso);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!s.toString().startsWith(countryCode)){
            phoneNumberET.setText(countryCode);
            Selection.setSelection(phoneNumberET.getText(), phoneNumberET.getText().length());
        }

    }
}
