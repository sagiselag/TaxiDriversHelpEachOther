package com.android2.taxidrivershelpeachother.controller;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.Passenger;
import com.android2.taxidrivershelpeachother.model.ShuttleItem;
import com.android2.taxidrivershelpeachother.model.VolleyHandler;
import com.android2.taxidrivershelpeachother.view.AvailableShuttleFragment;
import com.android2.taxidrivershelpeachother.view.NewShuttleFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ShuttleLogic {
    private final Calendar calendar = Calendar.getInstance();
    private final Context context;
    private final NewShuttleFragment newShuttleFragment;
    private final View view;
    private final String placesAPI = "AIzaSyA9TU9q7dK5KpTsgawkNjfSArhtM00RzoQ";
    private String placeAddress;
    private LatLng placeLatLng, originLatLng;
    private final String distanceMatrixAPI = "AIzaSyAWAnXrIUC35qBUYXlYwJ0SMdMeTVieeAA", distanceMatrixHttpRequest = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
    private String originAddress, destinationAddress;
    private double shuttleDistanceInKm;
    private int shuttleDistanceInMinutes;
    private VolleyHandler volleyHandler = VolleyHandler.getInstance();
    private Date dateForParsing;
    private Fragment fragment;

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public ShuttleLogic(Context context){
        this.context = context;
        this.newShuttleFragment = null;
        this.view = null;
    }

    public ShuttleLogic(Context context, NewShuttleFragment fragment, View view){
        this.context = context;
        this.newShuttleFragment = fragment;
        this.view = view;

        setDatePicker ();
        setTimePicker();
        initializePlaces();
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

    public LatLng getPlaceLatLng() {
        return placeLatLng;
    }

    public void setPlaceLatLng(LatLng placeLatLng) {
        this.placeLatLng = placeLatLng;
    }

    public LatLng getOriginLatLng() {
        return originLatLng;
    }

    public void setOriginLatLng(LatLng originLatLng) {
        this.originLatLng = originLatLng;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    private boolean timeIsInFuture(String dayPicked, int hourOfDay, int minute){
        boolean res = false;

        if(dayPicked.equalsIgnoreCase(context.getString(R.string.today))){
            int minutesInDay = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
            if(minutesInDay < (hourOfDay * 60) + minute){
                res = true;
            }
        }
        else {
            res = true;
        }

        return res;
    }

    private void setDatePicker(){
        MaterialDatePicker.Builder datePickerBuilder = MaterialDatePicker.Builder.datePicker();
        datePickerBuilder.setSelection(calendar.getTimeInMillis());
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder(); // 1
        constraintsBuilder.setStart(calendar.getTimeInMillis());   //   2
        calendar.roll(Calendar.MONTH, 1);   //   3
        constraintsBuilder.setEnd(calendar.getTimeInMillis());   // 4
        constraintsBuilder.setValidator(DateValidatorPointForward.now()); // 5
        datePickerBuilder.setCalendarConstraints(constraintsBuilder.build());   //  6

        datePickerBuilder.setTitleText("INPUT DATE");
        final MaterialDatePicker materialDatePicker = datePickerBuilder.build();
        final EditText date = view.findViewById(R.id.SIC_date);

        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(newShuttleFragment != null) {
                        materialDatePicker.show(newShuttleFragment.getFragmentManager(), "DATE_PICKER");
                    }
                    else{
                        materialDatePicker.show(fragment.getFragmentManager(), "DATE_PICKER");
                    }
                }
            }
        });

        date.setText(context.getString(R.string.today));

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                String selectedDate = materialDatePicker.getHeaderText();
                long todayLong = materialDatePicker.todayInUtcMilliseconds();
                long selectionLong = (Long) selection;

                if (todayLong == selectionLong){
                    date.setText(context.getString(R.string.today));
                }
                else if(todayLong + 86400000 >= selectionLong){
                    date.setText(context.getString(R.string.tomorrow));
                }
                else {
                    dateForParsing = new Date(selectionLong);
                    selectedDate = LogicHandler.onlyDateSimpleDateFormat.format(selection);
                    date.setText(selectedDate);
                }
            }
        });
    }

    private void setTimePicker(){
        final EditText date = view.findViewById(R.id.SIC_date);
        final EditText time = view.findViewById(R.id.SIC_time);
        if(time != null) {
            time.setText(context.getString(R.string.immediate));

            final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String day = date.getText().toString();
                    if (timeIsInFuture(day, hourOfDay, minute)) {
                        if (minute > 9) {
                            time.setText(hourOfDay + " : " + minute);
                        } else {
                            time.setText(hourOfDay + " : 0" + minute);
                        }
                    } else {
                        time.setText(context.getString(R.string.immediate));
                    }
                }
            };

            time.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(context, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                        timePickerDialog.show();
                    }
                }
            });
        }
    }

    private void initializePlaces(){
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(), placesAPI);
        }

        PlacesClient placesClient= Places.createClient(context);
    }

    public void placeRequest(@Nullable final ViewGroup container, int code){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(container.getContext());
        newShuttleFragment.startActivityForResult(intent, code);

    }

    public void putPlaceInformationInEditTextItemOnPlaceResult(int resultCode, Intent data, EditText editTextToPutInformation){
        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

            editTextToPutInformation.setText(place.getName() + "\n"+place.getAddress());
            placeAddress = place.getAddress();
            placeLatLng = place.getLatLng();
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i(TAG, status.getStatusMessage());
            editTextToPutInformation.setText("");
        } else if (resultCode == RESULT_CANCELED) {
            // The user canceled the operation.
        }
    }

    public void publishShuttle(final ShuttleItem shuttleItem) {
        if (shuttleItem != null){
            originAddress = shuttleItem.getOriginAddress();
            destinationAddress = shuttleItem.getDestinationAddress();
        }
        if (originAddress != null && destinationAddress!= null && !originAddress.equalsIgnoreCase("") && !destinationAddress.equalsIgnoreCase("")) {
            String httpRequest = distanceMatrixHttpRequest + originAddress + "&destinations=" + destinationAddress + "&key=" + distanceMatrixAPI;

            volleyHandler.getJSONObjectWithVolley(context, httpRequest, new Callable<Void>() {
                @Override
                public Void call() {
                    JSONObject info;

                    info = volleyHandler.getJsonObjectReturnedFromVolley();
                    try {
                        JSONArray rows = info.getJSONArray("rows");
                        JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
                        JSONObject distance = elements.getJSONObject(0).getJSONObject("distance");
                        JSONObject duration = elements.getJSONObject(0).getJSONObject("duration");

                        String distanceStr = distance.getString("text").replaceAll("[^0-9.]", "");
                        String durationStr = duration.getString("text").replaceAll("[^0-9.]", "");

                        shuttleDistanceInKm = Double.parseDouble(distanceStr);
                        shuttleDistanceInMinutes = Integer.parseInt(durationStr);

                        // add new shuttle to DB
                        createNewShuttle(shuttleItem);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }
        else{
            Toast.makeText(context, "Please make sure your shuttle include origin and destination information", Toast.LENGTH_LONG).show();
        }
    }

    public void getTimeAndDistanceToShuttle(final AvailableShuttleFragment availableShuttleFragment, final ShuttleItem shuttleItem, Location currentLocation) {
        String httpRequest = distanceMatrixHttpRequest + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&destinations=" + shuttleItem.getOriginAddress() + "&key=" + distanceMatrixAPI;

        volleyHandler.getJSONObjectWithVolley(context, httpRequest, new Callable<Void>() {
            @Override
            public Void call() {
                JSONObject info;

                info = volleyHandler.getJsonObjectReturnedFromVolley();
                try {
                    JSONArray rows = info.getJSONArray("rows");
                    JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
                    JSONObject distance = elements.getJSONObject(0).getJSONObject("distance");
                    JSONObject duration = elements.getJSONObject(0).getJSONObject("duration");

                    String distanceStr = distance.getString("text").replaceAll("[^0-9.]", "");
                    String durationStr = duration.getString("text").replaceAll("[^0-9.]", "");

//                    setDistanceToShuttleInfo(shuttleItem, distanceStr, durationStr);
                    shuttleItem.setDistanceToShuttleInKm(Double.parseDouble(distanceStr));
                    shuttleItem.setDistanceToShuttleInMinutes(Integer.parseInt(durationStr));
                    if (availableShuttleFragment != null) {
                        availableShuttleFragment.refresh();
                    } else {
                        FireBaseHandler.getInstance().sortAndSendShuttlesToDriver();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (availableShuttleFragment == null) {
                        shuttleItem.setDistanceToShuttleInMinutes(-1);
                        FireBaseHandler.getInstance().sortAndSendShuttlesToDriver();
                    }
                }
                return null;
            }
        });
    }

    private void createNewShuttle(ShuttleItem shuttleItem){
        String originAddress, destinationAddress, remarks , shuttleDate, shuttleTime, passengerName, passengerPhone;
        boolean isFixedPrice;
        int price = 0, commissionFee;
        Passenger passenger;

        if(newShuttleFragment != null) {
            shuttleTime = newShuttleFragment.getTime_et().getText().toString();
            shuttleDate = newShuttleFragment.getDate_et().getText().toString();

            if (shuttleDate.equalsIgnoreCase(context.getString(R.string.today))) {
                shuttleDate = LogicHandler.getTodayDateString();
            } else if (shuttleDate.equalsIgnoreCase(context.getString(R.string.tomorrow))) {
                shuttleDate = LogicHandler.getTomorrowDateString();
            }

            if (shuttleTime.equalsIgnoreCase(context.getString(R.string.immediate))) {
                shuttleTime = LogicHandler.getCurrentTimeString();
            }

            originAddress = newShuttleFragment.getFrom_et().getText().toString();
            destinationAddress = newShuttleFragment.getDestination_et().getText().toString();
            isFixedPrice = newShuttleFragment.getFixedPrice_cb().isChecked();
            if (isFixedPrice) {
                String priceStr = newShuttleFragment.getFixedPrice_et().getText().toString().replaceAll("[^0-9.]", "");
                price = Integer.valueOf(priceStr);
            }

            commissionFee = Integer.valueOf(newShuttleFragment.getCommissionFee_et().getText().toString().replaceAll("[^0-9.]", ""));
            remarks = newShuttleFragment.getRemarks_et().getText().toString();
            passengerName = newShuttleFragment.getPassengerName_et().getText().toString();
            passengerPhone = newShuttleFragment.getPassengerPhone_et().getText().toString();

            passenger = new Passenger(passengerName, passengerPhone);

            shuttleItem = new ShuttleItem(originLatLng, originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, price, commissionFee, passenger,
                    shuttleDistanceInKm, shuttleDistanceInMinutes);

            FireBaseHandler.getInstance().addNewShuttleToDB(shuttleItem, "shuttles");
        }
        else{
            FireBaseHandler.getInstance().addNewShuttleToDB(shuttleItem, "soldShuttles");
        }

    }

    public double getShuttleDistanceInKm() {
        return shuttleDistanceInKm;
    }

    public int getShuttleDistanceInMinutes() {
        return shuttleDistanceInMinutes;
    }
}
