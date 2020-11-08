package com.android2.taxidrivershelpeachother.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.LogicHandler;
import com.android2.taxidrivershelpeachother.controller.ShuttleLogic;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.Passenger;
import com.android2.taxidrivershelpeachother.model.ShuttleItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class NewShuttleFragment extends Fragment implements IRefreshableFragment{
    private ShuttleLogic shuttleLogic;
    private FragmentManager fragmentManager;
    private int AUTOCOMPLETE_FROM_REQUEST_CODE = 2;
    private int AUTOCOMPLETE_DESTINATION_REQUEST_CODE = 3;
    private EditText commissionFee_et, price_et;
    private EditText remarks_et, passengerName_et, passengerPhone_et, sellStatus_ET;
    private MaterialButton time_btn;
    private MaterialButton date_btn;
    private MaterialButton from_btn;
    private MaterialButton destination_btn;
    private MaterialButton cancel_btn;
    private MaterialButton save_btn;

    private TextInputLayout fixedPriceLayout, shuttleSellStatusLayout;
    private LatLng latLng;
    private Button publish_btn;
    private RadioButton fixedPrice_radioBtn, meter_radioBtn;
    private ViewGroup container;
    private List<TextView> editTextsItems = new ArrayList<>();
    private List<Button> buttonsItems = new ArrayList<>();
    private LinearLayout editLayout;

    private MyPhoneTextWatcher phoneTextWatcher;
    private String countryCode;
//    private String tempTimeAndDistance, timeAndDistanceToOriginAddress, shuttleTimeAndDistance;

    private ShuttleItem shuttleItemToEdit = null;
    private boolean firstLoad = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
    }

    public NewShuttleFragment(){}

    public NewShuttleFragment(ShuttleItem shuttleItemToEdit){
        this();
        this.shuttleItemToEdit = shuttleItemToEdit;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_shuttle_information_fragment, container, false);
        shuttleLogic = new ShuttleLogic(getContext(), this, view);

        this.container = container;
        cancel_btn = view.findViewById(R.id.SIC_cancel);
        save_btn = view.findViewById(R.id.SIC_save);
        editLayout = view.findViewById(R.id.shuttle_edit_layout);
        from_btn = view.findViewById(R.id.from);
        destination_btn = view.findViewById(R.id.destination);
        publish_btn = view.findViewById(R.id.publishNewShuttleBtn);
        fixedPrice_radioBtn = view.findViewById(R.id.fixedPriceRadioButton);
        meter_radioBtn = view.findViewById(R.id.meterRadioButton);
        fixedPriceLayout = view.findViewById(R.id.priceInputLayout);
        price_et = view.findViewById(R.id.priceTV);
        commissionFee_et = view.findViewById(R.id.commissionFeeTV);
        time_btn = view.findViewById(R.id.SIC_time);
        date_btn = view.findViewById(R.id.SIC_date);
        remarks_et = view.findViewById(R.id.new_shuttle_remarks_et);
        passengerName_et = view.findViewById(R.id.new_shuttle_client_name_et);
        passengerPhone_et = view.findViewById(R.id.new_shuttle_client_phone_et);
        sellStatus_ET = view.findViewById(R.id.shuttle_sell_status_ET);
        shuttleSellStatusLayout = view.findViewById(R.id.shuttle_sell_status_TIL);

        commissionFee_et.setText(String.format("%s10", Currency.getInstance(Locale.getDefault()).getSymbol()));
        Selection.setSelection(commissionFee_et.getText(), commissionFee_et.getText().length());

        price_et.setText(String.format("%s%s", Currency.getInstance(Locale.getDefault()).getSymbol(), price_et.getText()));
        Selection.setSelection(price_et.getText(), price_et.getText().length());

//        Currency.getInstance(Locale.getDefault()).getSymbol()
//        getString(R.string.today) = Currency.getInstance(Locale.getDefault()).getSymbol();

        setListeners();

        editTextsItems.clear();
        editTextsItems.add(from_btn);
        editTextsItems.add(destination_btn);
        editTextsItems.add(time_btn);
        editTextsItems.add(date_btn);

        editTextsItems.add(commissionFee_et);
        editTextsItems.add(price_et);
        editTextsItems.add(remarks_et);
        editTextsItems.add(passengerName_et);
        editTextsItems.add(passengerPhone_et);

        phoneTextWatcher = new MyPhoneTextWatcher(getContext(), passengerPhone_et, false);

        passengerPhone_et.addTextChangedListener(phoneTextWatcher);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        shuttleLogic.setFragment(this);
        FireBaseHandler.getInstance().setFragment(this);

        if(shuttleItemToEdit != null) {
            if (firstLoad) {
                firstLoad = false;
                String commissionFee = String.valueOf(shuttleItemToEdit.getCommissionFee());
                String priceStr = String.valueOf(shuttleItemToEdit.getPrice());
                commissionFee_et.setText(commissionFee);
                remarks_et.setText(shuttleItemToEdit.getRemarks());
                passengerName_et.setText(shuttleItemToEdit.getPassenger().getName());
                passengerPhone_et.setText(shuttleItemToEdit.getPassenger().getPhone());
                time_btn.setText(shuttleItemToEdit.getShuttleTime());
                date_btn.setText(shuttleItemToEdit.getShuttleDate());
                from_btn.setText(shuttleItemToEdit.getOriginAddress());
                destination_btn.setText(shuttleItemToEdit.getDestinationAddress());
                try {
                    double price = (double) shuttleItemToEdit.getPrice();
                    if(price != 0){
                        fixedPrice_radioBtn.setChecked(true);
                        price_et.setText(priceStr);
                    }
                    else {
                        meter_radioBtn.setChecked(true);
                        price_et.setText(getContext().getString(R.string.meter_price));
                    }
                } catch (Exception e) {
                    meter_radioBtn.setChecked(true);
                }

                publish_btn.setText(getContext().getString(R.string.save));
            }
            shuttleSellStatusLayout.setVisibility(View.VISIBLE);
            if(shuttleItemToEdit.getHandlingDriverPhone() != null && shuttleItemToEdit.getHandlingDriverPhone().length() >= 10){
                shuttleSellStatusLayout.setBackgroundColor(getContext().getColor(R.color.colorSold));
                sellStatus_ET.setText(getContext().getString(R.string.sold));
            }
            else{
                shuttleSellStatusLayout.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent, null));
                sellStatus_ET.setText(getContext().getString(R.string.still_looking_for_driver));
            }
            publish_btn.setVisibility(View.GONE);
            publish_btn.setClickable(false);
            editLayout.setVisibility(View.VISIBLE);
            editLayout.setClickable(true);
        }
        else {
            meter_radioBtn.setChecked(true);
        }
    }

    private void updateShuttleInfo() {
        String originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, passengerName, passengerPhone;
        boolean isFixedPrice;
        int price = 0, commissionFee;
        Passenger passenger;
        ShuttleItem shuttleItem;

        shuttleTime = time_btn.getText().toString();
        shuttleDate = date_btn.getText().toString();

        if (shuttleDate.equalsIgnoreCase(getContext().getString(R.string.today))) {
            shuttleDate = LogicHandler.getTodayDateString();
        } else if (shuttleDate.equalsIgnoreCase(getContext().getString(R.string.tomorrow))) {
            shuttleDate = LogicHandler.getTomorrowDateString();
        }

        if (shuttleTime.equalsIgnoreCase(getContext().getString(R.string.immediate))) {
            shuttleTime = LogicHandler.getCurrentTimeString();
        }

        originAddress = from_btn.getText().toString();
        destinationAddress = destination_btn.getText().toString();
        isFixedPrice = fixedPrice_radioBtn.isChecked();
        if (isFixedPrice) {
            String priceStr = price_et.getText().toString().replaceAll("[^0-9.]", "");
            price = Integer.parseInt(priceStr);
        }
        else{
            price = 0;
        }

        commissionFee = Integer.parseInt(commissionFee_et.getText().toString().replaceAll("[^0-9.]", ""));
        remarks = remarks_et.getText().toString();
        passengerName = passengerName_et.getText().toString();
        passengerPhone = passengerPhone_et.getText().toString();

        passenger = new Passenger(passengerName, passengerPhone);
        if (shuttleLogic.getOriginLatLng() != null) {
            shuttleItem = new ShuttleItem(
                    shuttleLogic.getOriginLatLng(), originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, price, commissionFee, passenger,
                    shuttleLogic.getShuttleDistanceInKm(), shuttleLogic.getShuttleDistanceInMinutes());
        } else {
            shuttleItem = new ShuttleItem(shuttleItemToEdit.getOriginLatLng(), originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, price, commissionFee, passenger,
                    shuttleItemToEdit.getShuttleDistanceInKm(), shuttleItemToEdit.getShuttleDistanceInMinutes());
        }

        shuttleItem.setHandlingDriverPhone(shuttleItemToEdit.getHandlingDriverPhone());
        shuttleItem.setPublishedBy(shuttleItemToEdit.getPublishedBy());

        shuttleLogic.setShuttleID(shuttleItemToEdit.getId());
        FireBaseHandler.getInstance().setFragment(this);

        shuttleLogic.setFragment(null);
        shuttleLogic.publishShuttle(shuttleItem);
    }


    private void setListeners(){
        cancel_btn.setOnClickListener(v -> fragmentManager.popBackStack());
        save_btn.setOnClickListener(v -> {
            if(shuttleItemToEdit != null && shuttleItemToEdit.getId().length() > 0){
                updateShuttleInfo();
            }
        });

        commissionFee_et.addTextChangedListener(new CoinTextWatcher(commissionFee_et));

        price_et.addTextChangedListener(new CoinTextWatcher(price_et){
                                                    @Override
                                                    public void afterTextChanged(Editable s) {
                                                        boolean condition = (!getContext().getString(R.string.meter_price).equalsIgnoreCase(s.toString())) &&
                                                                (!s.toString().startsWith(Currency.getInstance(Locale.getDefault()).getSymbol()));

                                                        if (condition) {
                                                            price_et.setText(String.format("%s%s",
                                                                    Currency.getInstance(Locale.getDefault()).getSymbol(), s.toString().replace(Currency.getInstance(Locale.getDefault()).getSymbol(), "")));
                                                            Selection.setSelection(price_et.getText(), price_et.getText().length());
                                                        }
                                                    }
        });

        fixedPrice_radioBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                price_et.setText("");
                price_et.setFocusableInTouchMode(true);
            }
        });

        meter_radioBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                price_et.setText(getContext().getString(R.string.meter_price));
                price_et.setFocusableInTouchMode(false);
            }
        });

        from_btn.setOnClickListener(v -> shuttleLogic.placeRequest(getContext(), AUTOCOMPLETE_FROM_REQUEST_CODE));

        destination_btn.setOnClickListener(v -> shuttleLogic.placeRequest(getContext(), AUTOCOMPLETE_DESTINATION_REQUEST_CODE));

        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passengerPhone_et.getText().length() == 10) {
                    shuttleLogic.publishShuttle(null);
                }
                else {
                    passengerPhone_et.setError(getString(R.string.please_enter_ten_digits_phone_number));
                }
            }
        });

//        publish.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                shuttleLogic.publishShuttle(null);
////                refresh();
//            }
//        });
    }

    //TODO make verification method for all necessary fields

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == AUTOCOMPLETE_FROM_REQUEST_CODE) {
            shuttleLogic.putPlaceInformationInEditTextItemOnPlaceResult(resultCode, data, from_btn);
            //TODO put local in origin address and placeAddress in destinationAddress, call getShuttleInformation(); to put timeAndDistance to pickup location

            shuttleLogic.setOriginAddress(shuttleLogic.getPlaceAddress());
            shuttleLogic.setOriginLatLng(shuttleLogic.getPlaceLatLng());
        }

        else if (requestCode == AUTOCOMPLETE_DESTINATION_REQUEST_CODE) {
            shuttleLogic.putPlaceInformationInEditTextItemOnPlaceResult(resultCode, data, destination_btn);
            shuttleLogic.setDestinationAddress(shuttleLogic.getPlaceAddress());
        }
    }

    public TextView getFrom_btn() {
        return from_btn;
    }

    public TextView getDestination_btn() {
        return destination_btn;
    }

    public EditText getCommissionFee_et() {
        return commissionFee_et;
    }

    public EditText getPrice_et() {
        return price_et;
    }

    public TextView getTime_btn() {
        return time_btn;
    }

    public TextView getDate_btn() {
        return date_btn;
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

    public boolean fixedPrice() {
        return fixedPrice_radioBtn.isChecked();
    }

    public void refresh() {
        if(shuttleItemToEdit == null) {
            for (TextView editText : editTextsItems) {
                editText.setText("");
            }

            if (getContext() != null) {
                date_btn.setText(getContext().getString(R.string.today));
                time_btn.setText(getContext().getString(R.string.immediate));
            }

            if (fixedPriceLayout != null) {
                fixedPriceLayout.getEditText().setText("");
            }
            if (meter_radioBtn != null) {
                meter_radioBtn.setChecked(true);
            }

            if (commissionFee_et != null) {
                commissionFee_et.setText(String.format("%s10", Currency.getInstance(Locale.getDefault()).getSymbol()));
                Selection.setSelection(commissionFee_et.getText(), commissionFee_et.getText().length());
            }
        }
        else{
            fragmentManager.popBackStack();
        }
    }


}
