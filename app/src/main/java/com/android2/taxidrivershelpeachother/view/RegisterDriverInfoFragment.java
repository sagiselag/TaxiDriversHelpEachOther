package com.android2.taxidrivershelpeachother.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.LogicHandler;
import com.android2.taxidrivershelpeachother.model.DatesTextWatcher;
import com.android2.taxidrivershelpeachother.model.MyPhoneTextWatcher;
import com.android2.taxidrivershelpeachother.model.User;

import java.util.Calendar;

public class RegisterDriverInfoFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 7;
    private final int DIGITS_IN_PHONE_NUMBER = 10;
    private Button next, takeDriverLicensePhotoBtn;
    private EditText firstName, lastName, phone, dateOfBirth, email;
    private ImageView driverLicenseIV;
    private String imagePath;
    private boolean dateOfBirthIsOk = false, phoneIsOk = false;
    private String countryCode;
    private boolean isAvailable = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_info_register_fragment, container, false);

        next = view.findViewById(R.id.register_next_to_taxi_info_btn);
        takeDriverLicensePhotoBtn = view.findViewById(R.id.take_driver_license_photo_button);
        firstName = view.findViewById(R.id.register_driver_first_name_et);
        lastName = view.findViewById(R.id.register_driver_last_name_et);
        phone = view.findViewById(R.id.driverPhoneNumberET);
        dateOfBirth = view.findViewById(R.id.register_DriverDateOfBirthEt);
        email = view.findViewById(R.id.register_driver_email);
        driverLicenseIV = view.findViewById(R.id.driverLicenseIV);

        final MyPhoneTextWatcher phoneTextWatcher = new MyPhoneTextWatcher(getContext(), phone);
        countryCode = phoneTextWatcher.getCountryCode();

        next.setEnabled(false);
        addAllFocusChangeListeners();
        setDateOfBirthValidation();

        takeDriverLicensePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePath = LogicHandler.takePicture(getContext(), driverLicenseIV, MainActivity.driverLicensePhotoFileName);
                MainActivity.setImagePathToUpdate(imagePath);
                MainActivity.setImageViewToUpdate(driverLicenseIV);
            }
        });

        driverLicenseIV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                allowOrPreventItemToBeAdded();
            }
        });

        phone.setText(countryCode);
        phone.addTextChangedListener(phoneTextWatcher);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User(firstName.getText().toString(), lastName.getText().toString(), phone.getText().toString(), dateOfBirth.getText().toString(), "", null, 0, isAvailable);
                RegisterTaxiInfoFragment registerTaxiInfoFragment = new RegisterTaxiInfoFragment(user, driverLicenseIV.getDrawable());

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, registerTaxiInfoFragment, null).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    private void setDateOfBirthValidation(){
        TextWatcher tw = new DatesTextWatcher(dateOfBirth, 18, 80);
        dateOfBirth.addTextChangedListener(tw);
    }

    private void addAllFocusChangeListeners(){
        firstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    allowOrPreventItemToBeAdded();
                }
            }
        });

        lastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    allowOrPreventItemToBeAdded();
                }
            }
        });

        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
//                    String phoneNumberStr = phone.getText().toString();
//                    phoneNumberStr = phoneNumberStr.replaceAll("[^\\d.]", "");
                    if (phone.getText().length() == countryCode.length() + DIGITS_IN_PHONE_NUMBER) {
                        phoneIsOk = true;
                        allowOrPreventItemToBeAdded();
                    }
                    else{
                        phoneIsOk = false;
                        phone.setError(getString(R.string.invalid_phone_number));
                    }
                }
            }
        });

        dateOfBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String dateOfBirthStr = dateOfBirth.getText().toString();
                    boolean dateOfBirthIsDate = !dateOfBirthStr.isEmpty() && !dateOfBirthStr.matches(".*[a-zA-Z]+.*");
                    if(dateOfBirthIsDate)
                    {
                        dateOfBirthIsOk = true;
                        allowOrPreventItemToBeAdded();
                    }
                    else{
                        dateOfBirthIsOk = false;
                        dateOfBirth.setError(getString(R.string.invalid_date));
                    }
                }
            }
        });

        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(LogicHandler.isEmailValid(email.getText().toString())){
                        allowOrPreventItemToBeAdded();
                    }
                    else{
                        email.setError(getString(R.string.invalid_email_address));
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(imagePath != null && imagePath.length() > 10){
            driverLicenseIV.setImageDrawable(Drawable.createFromPath(imagePath));
        }
    }


    public void allowOrPreventItemToBeAdded() {
        boolean hasAllInfo = firstName.getText().length() > 0 && lastName.getText().length() > 0 && phoneIsOk && dateOfBirth.getText().length() > 0
                && dateOfBirthIsOk && email.getText().length() > 0 && driverLicenseIV.getDrawable() != null;

        if (hasAllInfo) {
            next.setEnabled(true);
        } else {
            next.setEnabled(false);
        }
    }
}
