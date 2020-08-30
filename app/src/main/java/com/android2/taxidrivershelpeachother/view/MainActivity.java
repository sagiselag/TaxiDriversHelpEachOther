
package com.android2.taxidrivershelpeachother.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.LogicHandler;
import com.android2.taxidrivershelpeachother.controller.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {
    public static Location prevLocation;
    public static boolean appIsClosed = false;
    public static boolean FirstLoadOfApp = true;
    public static ImageView imageViewToUpdate;
    public static String imagePathToUpdate;
    public final static int INTERNET_PERMISSION_REQUEST = 1;
    public final static int ACCESS_BACKGROUND_LOCATION_PERMISSION_REQUEST = 2;
    public final static int ACCESS_FINE_LOCATION_PERMISSION_REQUEST = 3;
    public final static int NOTIFICATIONS_PERMISSION_REQUEST = 4;
    public final static int AUTOCOMPLETE_FROM_REQUEST_CODE = 5;
    public final static int AUTOCOMPLETE_DESTINATION_REQUEST_CODE = 6;
    public final static int PHONE_CALL_REQUEST_CODE = 7;
    public final static int WRITE_PERMISSION_REQUEST = 8;
    public final static int READ_PERMISSION_REQUEST = 9;
    public final static int CAMERA_REQUEST = 10;
    public final static String driverLicensePhotoFileName = "driverLicensePhoto";
    public final static String vehicleLicensePhotoFileName = "vehicleLicensePhoto";
    public final static String vehicleInsurancePhotoFIleName = "vehicleInsurancePhoto";
    public static String appFilePath;
    public static String endOfFile;
    private static SharedPreferences settings;
    private static SharedPreferencesUtils sharedPreferencesUtils = new SharedPreferencesUtils();

    final String LOGIN_FRAGMENT_TAG = "login_fragment";

    public static SharedPreferencesUtils getSharedPreferencesUtils() {
        return sharedPreferencesUtils;
    }

    public static void setPrevLocation(Location prevLocation) {
        MainActivity.prevLocation = prevLocation;
    }

    public static Location getPrevLocation() {
        return prevLocation;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appIsClosed = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", MODE_PRIVATE);
        loadPreferences();

//        appFilePath = getApplicationContext().getFilesDir() + "/";
        appFilePath = getApplicationContext().getExternalFilesDir(null).getPath()+"/";
        endOfFile = "_JPEG" +".jpg";

        ImageView circleImageView = findViewById(R.id.imageView);

        circleImageView.setScaleX((float) 1.4);
        circleImageView.setScaleY((float) 1.2);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.root_layout, new LoginFragment(), LOGIN_FRAGMENT_TAG);
        fragmentTransaction.commit();

//        ActionBar bar = getSupportActionBar();
//        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EEC107")));

    }

//    @Override
//    public void onBackPressed(){
//        Intent a = new Intent(Intent.ACTION_MAIN);
//        a.addCategory(Intent.CATEGORY_HOME);
//        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(a);
//    }


    public static ImageView getImageViewToUpdate() {
        return imageViewToUpdate;
    }

    public static void setImageViewToUpdate(ImageView imageViewToUpdate) {
        MainActivity.imageViewToUpdate = imageViewToUpdate;
    }

    public static String getImagePathToUpdate() {
        return imagePathToUpdate;
    }

    public static void setImagePathToUpdate(String imagePathToUpdate) {
        MainActivity.imagePathToUpdate = imagePathToUpdate;
    }

    protected void onActivityResult(int requestCode , int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LogicHandler.CAMERA_REQUEST && resultCode == RESULT_OK) {
            imageViewToUpdate.setImageDrawable(Drawable.createFromPath(imagePathToUpdate));
        }
    }

    private void loadPreferences() {
        sharedPreferencesUtils.setAvailable(settings.getBoolean("isAvailable", false));
        sharedPreferencesUtils.setNotificationTopic(settings.getString("notificationTopic", "New Shuttle request"));
        sharedPreferencesUtils.setTimeBetweenNotifications(settings.getInt("timeBetweenNotifications", 1));
        sharedPreferencesUtils.setGeneralMaxDistanceInMinutes(settings.getInt("generalMaxDistanceInMinutes", 7));
        sharedPreferencesUtils.setHasPremiumShuttlesSettingsCB(settings.getBoolean("usePremiumSettings", false));
        sharedPreferencesUtils.setPremiumMaxDistanceInMinutes(settings.getInt("premiumMaxDistanceInMinutes", 40));
        sharedPreferencesUtils.setPremiumMinPrice(settings.getInt("premiumMinPrice", 200));
    }

    public static void savePreferences() {
        SharedPreferences.Editor prefsEditor = settings.edit();

        prefsEditor.putBoolean("isAvailable",sharedPreferencesUtils.isAvailable());
        prefsEditor.putInt("generalMaxDistanceInMinutes", sharedPreferencesUtils.getGeneralMaxDistanceInMinutes());
        prefsEditor.putString("notificationTopic", sharedPreferencesUtils.getNotificationTopic());
        prefsEditor.putInt("timeBetweenNotifications", sharedPreferencesUtils.getTimeBetweenNotifications());
        prefsEditor.putBoolean("usePremiumSettings", sharedPreferencesUtils.isHasPremiumShuttlesSettingsCB());
        prefsEditor.putInt("premiumMaxDistanceInMinutes", sharedPreferencesUtils.getPremiumMaxDistanceInMinutes());
        prefsEditor.putInt("premiumMinPrice", sharedPreferencesUtils.getPremiumMinPrice());
        prefsEditor.apply();
    }
}