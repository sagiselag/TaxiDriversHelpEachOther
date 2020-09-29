package com.android2.taxidrivershelpeachother.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.ShuttleLogic;
import com.google.android.material.textfield.TextInputLayout;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class NewShuttleFragment extends Fragment implements IRefreshableFragment{
    private ShuttleLogic shuttleLogic;
    private FragmentManager fragmentManager;
    private int AUTOCOMPLETE_FROM_REQUEST_CODE = 2;
    private int AUTOCOMPLETE_DESTINATION_REQUEST_CODE = 3;
    private EditText from_et, destination_et, commissionFee_et, fixedPrice_et;
    private EditText time_et, date_et, remarks_et, passengerName_et, passengerPhone_et;
    private TextInputLayout fixedPriceLayout;
    private LatLng latLng;
    private Button publish;
    private CheckBox fixedPrice_cb;
    private ViewGroup container;
    private List<EditText> editTextsItems = new ArrayList<>();

//    private String tempTimeAndDistance, timeAndDistanceToOriginAddress, shuttleTimeAndDistance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_shuttle_information_fragment, container, false);
        shuttleLogic = new ShuttleLogic(getContext(), this, view);

        this.container = container;

        from_et = view.findViewById(R.id.from);
        destination_et = view.findViewById(R.id.destination);
        publish = view.findViewById(R.id.publishNewShuttleBtn);
        fixedPrice_cb = view.findViewById(R.id.fixedPriceCheckBox);
        fixedPriceLayout = view.findViewById(R.id.priceInputLayout);
        fixedPrice_et = view.findViewById(R.id.priceTV);
        commissionFee_et = view.findViewById(R.id.commissionFeeTV);
        time_et = view.findViewById(R.id.SIC_time);
        date_et = view.findViewById(R.id.SIC_date);
        remarks_et = view.findViewById(R.id.new_shuttle_remarks_et);
        passengerName_et = view.findViewById(R.id.new_shuttle_client_name_et);
        passengerPhone_et = view.findViewById(R.id.new_shuttle_client_phone_et);

        commissionFee_et.setText(Currency.getInstance(Locale.getDefault()).getSymbol() + "10");
        Selection.setSelection(commissionFee_et.getText(), commissionFee_et.getText().length());

        fixedPrice_et.setText(Currency.getInstance(Locale.getDefault()).getSymbol() + fixedPrice_et.getText());
        Selection.setSelection(fixedPrice_et.getText(), fixedPrice_et.getText().length());

//        Currency.getInstance(Locale.getDefault()).getSymbol()
//        getString(R.string.today) = Currency.getInstance(Locale.getDefault()).getSymbol();

        setListeners();

        editTextsItems.clear();
        editTextsItems.add(from_et);
        editTextsItems.add(destination_et);
        editTextsItems.add(commissionFee_et);
        editTextsItems.add(fixedPrice_et);
        editTextsItems.add(time_et);
        editTextsItems.add(date_et);
        editTextsItems.add(remarks_et);
        editTextsItems.add(passengerName_et);
        editTextsItems.add(passengerPhone_et);

        return view;
    }

    private void setListeners(){
        commissionFee_et.addTextChangedListener(new TextWatcher() {

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
                if(!s.toString().startsWith(Currency.getInstance(Locale.getDefault()).getSymbol())){
                    commissionFee_et.setText(Currency.getInstance(Locale.getDefault()).getSymbol());
                    Selection.setSelection(commissionFee_et.getText(), commissionFee_et.getText().length());

                }

            }
        });

        fixedPrice_et.addTextChangedListener(new TextWatcher() {

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
                if(!s.toString().startsWith(Currency.getInstance(Locale.getDefault()).getSymbol())){
                    fixedPrice_et.setText(Currency.getInstance(Locale.getDefault()).getSymbol());
                    Selection.setSelection(fixedPrice_et.getText(), fixedPrice_et.getText().length());
                }

            }
        });

        fixedPrice_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) fixedPriceLayout.setVisibility(View.VISIBLE);
                else fixedPriceLayout.setVisibility(View.INVISIBLE);
            }
        });

        from_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    shuttleLogic.placeRequest(container, AUTOCOMPLETE_FROM_REQUEST_CODE);
                }
            }
        });

        destination_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    shuttleLogic.placeRequest(container, AUTOCOMPLETE_DESTINATION_REQUEST_CODE);
                }
            }
        });

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuttleLogic.publishShuttle(null);
//                refresh();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == AUTOCOMPLETE_FROM_REQUEST_CODE) {
            shuttleLogic.putPlaceInformationInEditTextItemOnPlaceResult(resultCode, data, from_et);
            //TODO put local in origin address and placeAddress in destinationAddress, call getShuttleInformation(); to put timeAndDistance to pickup location

            shuttleLogic.setOriginAddress(shuttleLogic.getPlaceAddress());
            shuttleLogic.setOriginLatLng(shuttleLogic.getPlaceLatLng());
        }

        else if (requestCode == AUTOCOMPLETE_DESTINATION_REQUEST_CODE) {
            shuttleLogic.putPlaceInformationInEditTextItemOnPlaceResult(resultCode, data, destination_et);
            shuttleLogic.setDestinationAddress(shuttleLogic.getPlaceAddress());
        }
    }

    public EditText getFrom_et() {
        return from_et;
    }

    public EditText getDestination_et() {
        return destination_et;
    }

    public EditText getCommissionFee_et() {
        return commissionFee_et;
    }

    public EditText getFixedPrice_et() {
        return fixedPrice_et;
    }

    public EditText getTime_et() {
        return time_et;
    }

    public EditText getDate_et() {
        return date_et;
    }

    public EditText getRemarks_et() {
        return remarks_et;
    }

    public EditText getPassengerName_et() {
        return passengerName_et;
    }

    public EditText getPassengerPhone_et() {
        return passengerPhone_et;
    }

    public CheckBox getFixedPrice_cb() {
        return fixedPrice_cb;
    }

    public void refresh(){
        for (EditText editText:editTextsItems) {
            editText.setText("");
        }

        if(fixedPriceLayout != null) {
            fixedPriceLayout.getEditText().setText("");
        }
        if(fixedPrice_cb != null) {
            fixedPrice_cb.setChecked(false);
        }
    }
}
