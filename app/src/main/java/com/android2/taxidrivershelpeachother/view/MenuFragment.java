package com.android2.taxidrivershelpeachother.view;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.LogicHandler;
import com.android2.taxidrivershelpeachother.controller.MenuLogic;
//import com.android2.taxidrivershelpeachother.controller.NotificationReceiver;
import com.android2.taxidrivershelpeachother.controller.NotificationService;
import com.android2.taxidrivershelpeachother.controller.ShuttleItemAdapter;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.ShuttleItem;
import com.android2.taxidrivershelpeachother.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MenuFragment extends Fragment {
    private MenuLogic menuLogic;
    private FragmentManager fragmentManager;
    private final Calendar calendar = Calendar.getInstance();
    private TextInputLayout premiumMinPriceTIL, premiumMaxPickupDrivingTimeTIL;
    private EditText premiumMinPrice, premiumMaxPickupDrivingTime;
    private TextView driverName, generalPickupMinutes, generalPickupMinutesStr;
    private SeekBar generalPickupSeekBar;
    private CheckBox premiumShuttlesSettingsCB;
    private Switch availableSwitch;
    private ViewGroup container;
    private final List<ShuttleItem> shuttleItems = new ArrayList<>();
    private LogicHandler logicHandler;
    private boolean isAvailable;
    private String notificationTopic;
    private int timeBetweenNotifications = 1;
    private static final long CLICK_TIME_INTERVAL = 300;
    private static long lastClickTime;
    private final int INTERNET_PERMISSION_REQUEST = MainActivity.INTERNET_PERMISSION_REQUEST;
    private final int ACCESS_BACKGROUND_LOCATION_PERMISSION_REQUEST = MainActivity.ACCESS_BACKGROUND_LOCATION_PERMISSION_REQUEST;
    private final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST = MainActivity.ACCESS_FINE_LOCATION_PERMISSION_REQUEST;
    private final int NOTIFICATIONS_PERMISSION_REQUEST = MainActivity.NOTIFICATIONS_PERMISSION_REQUEST;
    private final int AUTOCOMPLETE_FROM_REQUEST_CODE = MainActivity.AUTOCOMPLETE_FROM_REQUEST_CODE;
    private final int AUTOCOMPLETE_DESTINATION_REQUEST_CODE = MainActivity.AUTOCOMPLETE_DESTINATION_REQUEST_CODE;
    private ImageView userImageView;
    private boolean toContinuePermissions = false;
//    private String tempTimeAndDistance, timeAndDistanceToOriginAddress, shuttleTimeAndDistance;
    private FireBaseHandler fireBaseHandler;
    private User loggedInUser;
    private SharedPreferences settings;
    private Button postNewShuttleBtn, commitmentShuttlesBtn, availableShuttlesBtn, leadManagementBtn;



    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public Switch getAvailableSwitch() {
        return availableSwitch;
    }

    public void setAvailableSwitch(Switch availableSwitch) {
        this.availableSwitch = availableSwitch;
    }

    public ImageView getUserImageView() {
        return userImageView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getActivity().getSupportFragmentManager();
        if(MainActivity.appIsClosed)
        {
            MainActivity.FirstLoadOfApp = true;
            MainActivity.appIsClosed = false;
        }
    }

    public String getDriverName() {
        return driverName.getText().toString();
    }

    public void setDriverName(String driverName) {
        this.driverName.setText(driverName);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.appIsClosed = false;
        logicHandler = new LogicHandler(getContext());
        logicHandler.setAvailable(isAvailable);

        init();
    }

    public EditText getPremiumMinPrice() {
        return premiumMinPrice;
    }

    public EditText getPremiumMaxPickupDrivingTime() {
        return premiumMaxPickupDrivingTime;
    }

    public TextView getGeneralPickupMinutes() {
        return generalPickupMinutes;
    }

    public CheckBox getPremiumShuttlesSettingsCB() {
        return premiumShuttlesSettingsCB;
    }

    @Override
    public void onPause() {
        super.onPause();

        FusedLocationProviderClient fusedLocationProviderClient = logicHandler.getFusedLocationProviderClient();
        LocationCallback locationCallback = logicHandler.getLocationCallback();

        if(fusedLocationProviderClient != null && locationCallback != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    AlarmManager alarmManager;
    Intent notificationIntent;
    PendingIntent pendingIntent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_fragment, container, false);
        this.container = container;

//        menuLogic = new MenuLogic(getContext(), this, view);

        postNewShuttleBtn = view.findViewById(R.id.post_new_shuttle_lead_button);
        commitmentShuttlesBtn = view.findViewById(R.id.commitment_shuttles_button);
        availableShuttlesBtn = view.findViewById(R.id.available_shuttles_button);
        leadManagementBtn = view.findViewById(R.id.lead_management_button);
        availableSwitch = view.findViewById(R.id.available_switch);
        userImageView = view.findViewById(R.id.user_imageView);
        driverName = view.findViewById(R.id.driverNameTextView);
        fireBaseHandler = FireBaseHandler.getInstance();
        fireBaseHandler.downloadImageIntoImageView(MainActivity.driverLicensePhotoFileName, userImageView);
        fireBaseHandler.getLoggedInUserFromDB(this);
        premiumShuttlesSettingsCB = view.findViewById(R.id.usePremiumSettingsCheckBox);
        premiumMinPrice = view.findViewById(R.id.menuPremiumMinPriceET);
        premiumMaxPickupDrivingTime = view.findViewById(R.id.menuPremiumMaxPickupDrivingTimeEt);
        generalPickupMinutes = view.findViewById(R.id.menuGeneralPickupMinutesTV);
        generalPickupMinutesStr = view.findViewById(R.id.menuGeneralPickupMinutesStrTV);
        generalPickupSeekBar = view.findViewById(R.id.menuGeneralPickupSeekBar);
        premiumMinPriceTIL = view.findViewById(R.id.menuPremiumMinPriceTIL);
        premiumMaxPickupDrivingTimeTIL = view.findViewById(R.id.menuPremiumMaxPickupDrivingTimeTIL);

        if(premiumShuttlesSettingsCB.isChecked()){
            premiumMaxPickupDrivingTimeTIL.setVisibility(View.VISIBLE);
            premiumMinPriceTIL.setVisibility(View.VISIBLE);
        }
        else{
            premiumMaxPickupDrivingTimeTIL.setVisibility(View.INVISIBLE);
            premiumMinPriceTIL.setVisibility(View.INVISIBLE);
        }


        if(generalPickupSeekBar.getProgress() + 1 == 1){
            generalPickupMinutes.setText("");
            generalPickupMinutesStr.setText(getContext().getString(R.string.minute));
        }
        else{
            generalPickupMinutes.setText(String.valueOf(generalPickupSeekBar.getProgress() + 1));
            generalPickupMinutesStr.setText(getContext().getString(R.string.minutes));
        }

        setListeners();

        return view;
    }

    private void setListeners(){
        availableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                logicHandler.setAvailable(isChecked);
                isAvailable = isChecked;
                savePreferences();
                updateTheNotificationIntentAndService();
            }
        });

        postNewShuttleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, new NewShuttleFragment(), null).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        availableShuttlesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, new AvailableShuttleFragment("availableShuttles"), null).addToBackStack("availableShuttles");
                fragmentTransaction.commit();
            }
        });

        commitmentShuttlesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, new AvailableShuttleFragment("commitmentShuttles"), null).addToBackStack("commitmentShuttles");
                fragmentTransaction.commit();
            }
        });

        leadManagementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, new AvailableShuttleFragment("leadManagement"), null).addToBackStack("leadManagement");
                fragmentTransaction.commit();
            }
        });

        premiumShuttlesSettingsCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    premiumMaxPickupDrivingTimeTIL.setVisibility(View.VISIBLE);
                    premiumMinPriceTIL.setVisibility(View.VISIBLE);
                }
                else{
                    premiumMaxPickupDrivingTimeTIL.setVisibility(View.INVISIBLE);
                    premiumMinPriceTIL.setVisibility(View.INVISIBLE);
                    premiumMinPrice.setText("");
                    premiumMaxPickupDrivingTime.setText("");
                }
                savePreferences();
                updateTheNotificationIntentAndService();
            }
        });

        premiumMinPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    savePreferences();
                    updateTheNotificationIntentAndService();
                }
            }
        });

        premiumMaxPickupDrivingTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    savePreferences();
                    updateTheNotificationIntentAndService();
                }
            }
        });

        generalPickupSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress + 1 == 1){
                    generalPickupMinutes.setText("");
                    generalPickupMinutesStr.setText(getContext().getString(R.string.minute));
                }
                else{
                    generalPickupMinutes.setText(String.valueOf(progress + 1));
                    generalPickupMinutesStr.setText(getContext().getString(R.string.minutes));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                savePreferences();
                updateTheNotificationIntentAndService();
            }
        });
    }

    private void updateTheNotificationIntentAndService(){
        if(alarmManager != null)
        {
            alarmManager.cancel(pendingIntent);
            getContext().stopService(new Intent(getContext().getApplicationContext(), NotificationService.class));

            if (availableSwitch.isChecked()) {
                pendingIntent = PendingIntent.getBroadcast(getContext().getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, pendingIntent);
            }
            else{
                logicHandler.setInFetchingDataProgress(false);
            }
        }
    }

    private void init() {
        settings = getContext().getSharedPreferences(getContext().getApplicationContext().getPackageName() + "_preferences", MODE_PRIVATE);

        if (MainActivity.FirstLoadOfApp == true) {
            MainActivity.FirstLoadOfApp = false;
            permissionsInit();
            loadPreferences();
        }

        logicHandler.setAvailable(isAvailable);
        logicHandler.setNotificationTopic(notificationTopic);
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        notificationIntent = new Intent(getContext().getApplicationContext(), NotificationReceiver2.class);
        notificationIntent.putExtra("isAvailable", isAvailable);
        pendingIntent = PendingIntent.getBroadcast(getContext().getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        resetLastClickTime();
    }

    private void permissionsInit() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_REQUEST);
            } else if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, ACCESS_BACKGROUND_LOCATION_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ACCESS_FINE_LOCATION_PERMISSION_REQUEST){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(), getString(R.string.cantWorkProperly), Toast.LENGTH_LONG).show();
            }
            else {
                toContinuePermissions = true;
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, ACCESS_BACKGROUND_LOCATION_PERMISSION_REQUEST);
                }
            }
        }
        else if(requestCode == ACCESS_BACKGROUND_LOCATION_PERMISSION_REQUEST){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getString(R.string.offlineNotificationToast), Toast.LENGTH_LONG).show();
            }
        }
    }



    public static void resetLastClickTime() {
        lastClickTime = System.currentTimeMillis();
    }

    public static boolean timeBetweenClicksIsOk() {
        long now = System.currentTimeMillis();
        long timeBetweenClicks = now - lastClickTime;
        boolean result = timeBetweenClicks > CLICK_TIME_INTERVAL;

        if (result) {
            lastClickTime = now;
        }
        return result;
    }

    private void loadPreferences() {
        isAvailable = settings.getBoolean("isAvailable", false);
        notificationTopic = settings.getString("notificationTopic", "New Shuttle request");
        timeBetweenNotifications = settings.getInt("timeBetweenNotifications", 1);
        generalPickupSeekBar.setProgress(settings.getInt("generalMaxDistanceInMinutes", 7));
        premiumShuttlesSettingsCB.setChecked(settings.getBoolean("usePremiumSettings", false));
        premiumMaxPickupDrivingTime.setText(String.valueOf(settings.getInt("premiumMaxDistanceInMinutes", 40)));
        premiumMinPrice.setText(String.valueOf(settings.getInt("premiumMinPrice", 200)));

        getActivity().getIntent().putExtra("isAvailable", isAvailable);
        getActivity().getIntent().putExtra("notificationTopic", notificationTopic);
        getActivity().getIntent().putExtra("timeBetweenNotifications", timeBetweenNotifications);
    }

    public void savePreferences() {
        if(getActivity().getIntent().hasExtra("isAvailable")) {
            int premiumMaxDistanceInMinutesInt = 0, premiumMinPriceInt = 0;
            try{
                premiumMaxDistanceInMinutesInt = Integer.valueOf(premiumMaxPickupDrivingTime.getText().toString());
            }
            catch (Exception e){ }
            try{
                premiumMinPriceInt = Integer.valueOf(premiumMinPrice.getText().toString());
            }
            catch (Exception e){

            }
            SharedPreferences.Editor prefsEditor = settings.edit();
            prefsEditor.putBoolean("isAvailable",isAvailable);
            prefsEditor.putInt("generalMaxDistanceInMinutes", generalPickupSeekBar.getProgress());
            prefsEditor.putString("notificationTopic", notificationTopic);
            prefsEditor.putInt("timeBetweenNotifications", timeBetweenNotifications);
            prefsEditor.putBoolean("usePremiumSettings", premiumShuttlesSettingsCB.isChecked());
            prefsEditor.putInt("premiumMaxDistanceInMinutes", premiumMaxDistanceInMinutesInt);
            prefsEditor.putInt("premiumMinPrice", premiumMinPriceInt);
            prefsEditor.commit();
        }
    }

}
