
package com.android2.taxidrivershelpeachother.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.LogicHandler;
import com.android2.taxidrivershelpeachother.controller.SharedPreferencesUtils;

/*
TODO:
 *      Quick explanation (max 30 sec) after first installation and from side menu
 *      Application must work faster
 *      Bit (payment) configuration
 *      Products offers
 *      Search 
 *      Closing the keyboard after input
 *      All phone numbers must be clickable
 *      Add distance and time for pickup in all fragments
 *      Add balance in the sort / filter line
 *      On sold shuttle edit:
             if has more than 30 minutes to pickup handling driver get 10 minutes to Re-approval
             else handling driver get 5 minutes to Re-approval
 *      Time picker - Change from selection by dial to selection by scrolling
 *      Sort:
             by time on taken shuttles
             by time / location / nearby on available shuttles
 *      Navigation to address / location - on shuttle information fragments and on committed shuttle time to go notification
 *      WAZE:
             connection - for shuttle driving time information and for navigation
             add information on approximately shuttle driving time in the same hour and day of the week (from waze DB)
 *      New lead client from contacts (or other if need manually)
 *      Remainder for committed shuttles - driving time + 15 minutes
 *      Double confirmation on cancel operation
 *      Cancel commitments policy:
             first 5 minutes - free of charge
             up to one hour before pickup - half commission fee payment
             30 minutes or less to pickup - full commission fee payment, limited to max 3 cancels in the last 30 days
 *      Tabs font must be bigger - DONE
 *      Add shuttle classification:
            Passenger
            Shipment
            Other

*/

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
    private static long currTime;

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

    public static long getPrevRefreshTime() {
        return 0;
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

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }


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