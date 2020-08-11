package com.android2.taxidrivershelpeachother.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.LogicHandler;
import com.android2.taxidrivershelpeachother.model.DatesTextWatcher;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.TaxiInformation;
import com.android2.taxidrivershelpeachother.model.User;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterTaxiInfoFragment extends Fragment {
    private User user;
    private Button submit, takeVehicleLicensePhotoBtn, takeVehicleInsurancePhotoBtn;
    private EditText licenseNumber, taxiNumber, vehicleLicenseValidUntil, vehicleInsuranceValidUntil, model;
    private CheckBox accessible;
    private ImageView vehicleLicenseIV, vehicleInsuranceIV;
    private String vehicleLicenseImagePath, vehicleInsuranceImagePath;
    private final int LICENSE_NUMBER_LENGTH = 7, MIN_TAXI_NUMBER_LENGTH = 4, MAX_TAXI_NUMBER_LENGTH = 5;
    private MaterialAutoCompleteTextView passengerSeatsSpinner;
    private boolean modelIsOk = false, vehicleLicenseIsValid = false, vehicleInsuranceIsValid = false, licenseNumberIsOk = false, taxiNumberIsOk = false, passengerIsOk = false;
    private Drawable driverLicensePhoto;

    public RegisterTaxiInfoFragment(User user, Drawable driverLicensePhoto){
        this.user = user;
        this.driverLicensePhoto = driverLicensePhoto;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.vehicle_info_register_fragment, container, false);

        submit = view.findViewById(R.id.register_submit_btn);
        takeVehicleLicensePhotoBtn = view.findViewById(R.id.take_vehicle_license_photo_button);
        takeVehicleInsurancePhotoBtn = view.findViewById(R.id.take_insurance_photo_button);
        licenseNumber = view.findViewById(R.id.vehicle_license_number_et);
        taxiNumber = view.findViewById(R.id.vehicle_taxi_number_et);
        vehicleLicenseValidUntil = view.findViewById(R.id.vehicleLicenseValidUntilET);
        vehicleInsuranceValidUntil = view.findViewById(R.id.vehicleInsuranceValidUntilET);
        accessible = view.findViewById(R.id.isAccessibleCheckBox);
        vehicleLicenseIV = view.findViewById(R.id.imageViewVehicleLicense);
        vehicleInsuranceIV = view.findViewById(R.id.imageViewInsurance);
        model = view.findViewById(R.id.modelET);
        passengerSeatsSpinner = view.findViewById(R.id.filled_exposed_dropdown);

        submit.setEnabled(false);
        addAllFocusChangeListeners();
        setDatesValidation();

        addItemsOnSpinner();

        takeVehicleLicensePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicleLicenseImagePath = LogicHandler.takePicture(getContext(), vehicleLicenseIV, MainActivity.vehicleLicensePhotoFileName);
                MainActivity.setImagePathToUpdate(vehicleLicenseImagePath);
                MainActivity.setImageViewToUpdate(vehicleLicenseIV);
                vehicleLicenseIV.setVisibility(View.VISIBLE);
            }
        });

        takeVehicleInsurancePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicleInsuranceImagePath = LogicHandler.takePicture(getContext(), vehicleInsuranceIV, MainActivity.vehicleInsurancePhotoFIleName);
                MainActivity.setImagePathToUpdate(vehicleInsuranceImagePath);
                MainActivity.setImageViewToUpdate(vehicleInsuranceIV);
                vehicleInsuranceIV.setVisibility(View.VISIBLE);
            }
        });

        vehicleLicenseIV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                allowOrPreventItemToBeAdded();
            }
        });

        vehicleInsuranceIV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                allowOrPreventItemToBeAdded();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaxiInformation taxiInformation = new TaxiInformation(licenseNumber.getText().toString(), model.getText().toString(), taxiNumber.getText().toString(),
                        vehicleLicenseValidUntil.getText().toString(), vehicleInsuranceValidUntil.getText().toString(),
                        Integer.valueOf(passengerSeatsSpinner.getText().toString()), accessible.isChecked());
                user.setTaxiInformation(taxiInformation);

                FireBaseHandler.getInstance().addNewUserToDB(user);
                // TODO upload all images to DB and set user.imageUrl with the correct one

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, new LoginFragment(), null).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(LICENSE_NUMBER_LENGTH);
        licenseNumber.setFilters(filterArray);
        Selection.setSelection(licenseNumber.getText(), licenseNumber.getText().length());

        InputFilter[] filterArray2 = new InputFilter[1];
        filterArray2[0] = new InputFilter.LengthFilter(MAX_TAXI_NUMBER_LENGTH);
        taxiNumber.setFilters(filterArray2);
        Selection.setSelection(taxiNumber.getText(), taxiNumber.getText().length());

        InputFilter[] filterArray3 = new InputFilter[1];
        filterArray3[0] = new InputFilter.LengthFilter(4);
        model.setFilters(filterArray3);
        Selection.setSelection(model.getText(), model.getText().length());

        return view;
    }

    private void addAllFocusChangeListeners(){
        licenseNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String licenseNumberStr = licenseNumber.getText().toString();
                    if(licenseNumberStr.length() == LICENSE_NUMBER_LENGTH && (licenseNumberStr.endsWith("26") || licenseNumberStr.endsWith("25")) ) {
                        licenseNumberIsOk = true;
                        allowOrPreventItemToBeAdded();
                    }
                    else{
                        licenseNumberIsOk = false;
                        licenseNumber.setError(getString(R.string.invalid_license_number));
                    }
                }
            }
        });

        taxiNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    int taxiNumberLength = taxiNumber.getText().length();
                    if(taxiNumberLength >= MIN_TAXI_NUMBER_LENGTH && taxiNumberLength <= MAX_TAXI_NUMBER_LENGTH) {
                        taxiNumberIsOk = true;
                        allowOrPreventItemToBeAdded();
                    }
                    else{
                        taxiNumberIsOk = false;
                        taxiNumber.setError(getString(R.string.invalid_taxi_number));
                    }
                }
            }
        });

        passengerSeatsSpinner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    int passengerSeatsLength = passengerSeatsSpinner.getText().length();
                    if(passengerSeatsLength > 0) {
                        passengerIsOk = true;
                        allowOrPreventItemToBeAdded();
                    }
                    else{
                        passengerIsOk = false;
                    }
                }
            }
        });

        model.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try {
                        int modelYear = Integer.valueOf(model.getText().toString());
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                        if (currentYear - modelYear >= 0 && currentYear - modelYear <= 10) {
                            modelIsOk = true;
                            allowOrPreventItemToBeAdded();
                        } else {
                            modelIsOk = false;
                            model.setError(getString(R.string.invalid_model) + " (" + String.valueOf(currentYear) + "-" + String.valueOf(currentYear - 10) + ")");
                        }
                    } catch (Exception e){
                        modelIsOk = false;
                    }
                }
            }
        });

        vehicleLicenseValidUntil.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String date = vehicleLicenseValidUntil.getText().toString();
                    boolean dateIsDate = !date.isEmpty() && !date.matches(".*[a-zA-Z]+.*");

                    if(dateIsDate)
                    {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate inputDate = LocalDate.parse(date, formatter);
                        if(LocalDate.now().isBefore(inputDate)) {
                            vehicleLicenseIsValid = true;
                            allowOrPreventItemToBeAdded();
                        }
                        else{
                            vehicleLicenseIsValid = false;
                            vehicleLicenseValidUntil.setError(getString(R.string.date_is_in_the_past_error));
                        }
                    }
                    else{
                        vehicleLicenseIsValid = false;
                        vehicleLicenseValidUntil.setError(getString(R.string.invalid_date));
                    }
                }
            }
        });

        vehicleInsuranceValidUntil.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String date = vehicleInsuranceValidUntil.getText().toString();
                    boolean dateIsDate = !date.isEmpty() && !date.matches(".*[a-zA-Z]+.*");

                    if(dateIsDate)
                    {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate inputDate = LocalDate.parse(date, formatter);
                        if(LocalDate.now().isBefore(inputDate)) {
                            vehicleInsuranceIsValid = true;
                            allowOrPreventItemToBeAdded();
                        }
                        else{
                            vehicleInsuranceIsValid = false;
                            vehicleInsuranceValidUntil.setError(getString(R.string.date_is_in_the_past_error));
                        }
                    }
                    else{
                        vehicleInsuranceIsValid = false;
                        vehicleInsuranceValidUntil.setError(getString(R.string.invalid_date));
                    }
                }
            }
        });
    }

    private void setDatesValidation(){
        TextWatcher tw = new DatesTextWatcher(vehicleLicenseValidUntil, -1, 0);
        TextWatcher tw2 = new DatesTextWatcher(vehicleInsuranceValidUntil, -1, 0);

        vehicleLicenseValidUntil.addTextChangedListener(tw);
        vehicleInsuranceValidUntil.addTextChangedListener(tw2);

    }

    // add items into spinner dynamically
    private void addItemsOnSpinner() {

        List<String> list = new ArrayList<String>();
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("8");
        list.add("10");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, list);
        passengerSeatsSpinner.setAdapter(adapter);
    }

    public void allowOrPreventItemToBeAdded() {
        boolean hasAllInfo = licenseNumberIsOk && taxiNumberIsOk && modelIsOk && passengerIsOk && vehicleLicenseIsValid && vehicleInsuranceIsValid
                && vehicleLicenseIV.getDrawable() != null && vehicleInsuranceIV.getDrawable() != null;

        if (hasAllInfo) {
            submit.setEnabled(true);
        } else {
            submit.setEnabled(false);
        }
    }


}

