package com.android2.taxidrivershelpeachother.controller;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.ShuttleItem;
import com.android2.taxidrivershelpeachother.view.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class LogicHandler {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location prevLocation = MainActivity.getPrevLocation(), lastLocation;
    private Geocoder geocoder;
    private String DBLink;
    private JSONArray shuttlesArray;
    private List<ShuttleItem> shuttleItems = new ArrayList<>();
    private ShuttleItemAdapter shuttleItemAdapter;
    private Context context;
    private NotificationManager notificationManager;
    private final int NOTIF_ID = 8;
    private int currentNotificationNumber = 0;
    private boolean isAvailable;
    private String notificationTopic;
    private boolean worksOnPrevLocation = false;
    public static final int CAMERA_REQUEST = MainActivity.CAMERA_REQUEST;
    private SharedPreferences settings;
    private int maxKmFilterBasedOnMaxTimeForPickup, maxMinuteFilterBasedOnMaxTimeForPickup;
    private int maxKmPerMinute = 2; // 120 / 60 = 2 , based on max speed 120 km/h
    private boolean usePremiumSettings;
    private int generalMaxDistanceInMinutes, premiumMaxDistanceInMinutes, premiumMinPrice;
    private String minute, minutes, from, to, shuttleTime, pickupAt, pickupDistance;
    private static boolean isInFetchingDataProgress = false;
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static DateTimeFormatter htf = DateTimeFormatter.ofPattern("HH:mm");
    public static SimpleDateFormat dateAndTimeSimpleDateFormatUTC = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static SimpleDateFormat onlyDateSimpleDateFormatUTC = new SimpleDateFormat("dd/MM/yyyy");
    private static LocalDateTime today;
    private static LocalDateTime tomorrow;
    private static boolean notificationChannelIsInitialized = false;
    private NotificationChannel notificationChannel;
    private NotificationCompat.Builder builder;



//    public LogicHandler(Context context, List<ShuttleItem> shuttleItems, boolean isAvailable, String notificationTopic){
    public LogicHandler(Context context){
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        this.notificationTopic = context.getString(R.string.new_shuttle_request);
        settings = context.getSharedPreferences(context.getApplicationContext().getPackageName() + "_preferences", MODE_PRIVATE);
        usePremiumSettings = settings.getBoolean("usePremiumSettings", false);
        this.minute = context.getString(R.string.minute);
        this.minutes = context.getString(R.string.minutes);
        this.from = context.getString(R.string.from);
        this.to = context.getString(R.string.destination);
        this.pickupAt = context.getString(R.string.pickup_time);
        this.pickupDistance = context.getString(R.string.pickup_distance);
    }


    public static String getTodayDateString(){
        today = LocalDateTime.now();
        return dtf.format(today);
    }

    public static String getCurrentTimeString(){
        today = LocalDateTime.now();
        return htf.format(today);
    }

    public static String getTomorrowDateString(){
        today = LocalDateTime.now();
        tomorrow = today.plus(1, ChronoUnit.DAYS);
        return dtf.format(tomorrow);
    }

    public static String getMaxTimeString(){
        today = LocalDateTime.now();
        today.plus(1, ChronoUnit.HOURS);

        return htf.format(today);
    }

    public static String getMaxDateString(){
        today = LocalDateTime.now();
        today.plus(1, ChronoUnit.HOURS);

        return dtf.format(today);
    }

    public FusedLocationProviderClient getFusedLocationProviderClient() {
        return fusedLocationProviderClient;
    }

    public void setFusedLocationProviderClient(FusedLocationProviderClient fusedLocationProviderClient) {
        this.fusedLocationProviderClient = fusedLocationProviderClient;
    }

    public LocationCallback getLocationCallback() {
        return locationCallback;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setNotificationTopic(String notificationTopic) {
        this.notificationTopic = notificationTopic;
    }

    public int getMaxMinuteFilterBasedOnMaxTimeForPickup() {
        return maxMinuteFilterBasedOnMaxTimeForPickup;
    }

    public void startLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        generalMaxDistanceInMinutes = settings.getInt("generalMaxDistanceInMinutes", 7);
        maxMinuteFilterBasedOnMaxTimeForPickup = generalMaxDistanceInMinutes;

        if(usePremiumSettings){
            premiumMaxDistanceInMinutes = settings.getInt("premiumMaxDistanceInMinutes", 20); // for settings
            premiumMinPrice = settings.getInt("premiumMinPrice", 200); // for settings
            if(premiumMaxDistanceInMinutes > generalMaxDistanceInMinutes){
                maxMinuteFilterBasedOnMaxTimeForPickup = premiumMaxDistanceInMinutes;
            }
        }

        maxKmFilterBasedOnMaxTimeForPickup = maxMinuteFilterBasedOnMaxTimeForPickup * maxKmPerMinute;

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                lastLocation = locationResult.getLastLocation();
                if(prevLocation == null){
                    MainActivity.setPrevLocation(lastLocation);
//                    getShuttles();
                }
                else if(lastLocation.distanceTo(prevLocation) > maxKmFilterBasedOnMaxTimeForPickup / 2){
                    MainActivity.setPrevLocation(lastLocation);
//                    getShuttles();
                }
                getShuttles();
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
        request.setFastestInterval(500);
        if (MainActivity.getPrevLocation() != null && context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && MainActivity.appIsClosed) {
            worksOnPrevLocation = true;
            lastLocation = MainActivity.getPrevLocation();
            getShuttles();
        }
        else fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null);
    }

    private void getShuttles(){
            FireBaseHandler.getInstance().setLogicHandler(this);
            shuttleItems = FireBaseHandler.getInstance().getAvailableShuttlesFound();
        if(!isInFetchingDataProgress) {
            if (lastLocation != null && shuttleItems.isEmpty()) {
                FireBaseHandler.getInstance().getAvailableShuttlesWithMaxDistanceFromCurrentLocationFromDB(context, new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()), maxKmFilterBasedOnMaxTimeForPickup);
            } else {
                addNotification();
            }
        }
    }

    private void initNotificationChannelAndBuilder(){
        if(!notificationChannelIsInitialized || builder == null){
            String channelId = "taxi_drivers_leads_app_channel_id";
            CharSequence channelName = "Taxi Drivers Help Each Other";

            if (Build.VERSION.SDK_INT >= 26) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, channelName, importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 500, 400, 300, 200, 100});
                notificationManager.createNotificationChannel(notificationChannel);
            }

            builder = new NotificationCompat.Builder(context, channelId);
            builder.setSmallIcon(android.R.drawable.star_on);
            builder.setContentTitle(context.getString(R.string.new_shuttle_request));
            notificationChannelIsInitialized = true;
        }
    }

    public void addNotification() {
        ShuttleItem newestShuttleItem;
        Intent intent;
        PendingIntent pendingIntent;
        Notification notification;
        String description = "";
        String originAdd, destAdd, distInMin;

        initNotificationChannelAndBuilder();

        newestShuttleItem = FireBaseHandler.getInstance().getBestShuttle();
        distInMin = String.valueOf(newestShuttleItem.getDistanceToShuttleInMinutes());

        if (newestShuttleItem.getDistanceToShuttleInMinutes() == -1) {
            distInMin = context.getString(R.string.unable_to_calculate_distance);
        }
        else if (newestShuttleItem.getDistanceToShuttleInMinutes() == 1) {
            distInMin = minute;
        } else {
            distInMin += " " + minutes;
        }

        shuttleTime = newestShuttleItem.getShuttleTime();
        // shuttle time stored in UTC in the DB
        originAdd = newestShuttleItem.getOriginAddress();
        destAdd = newestShuttleItem.getDestinationAddress();
        description = pickupDistance + ": " + distInMin + "\n" + pickupAt + ": " + shuttleTime + "\n" + from + "\n" + originAdd + "\n" + to + "\n" + destAdd;
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(description));

        // TODO open nevigation to origin place
//                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newestShuttleItem.get(0).getLink()));
//                pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//                builder.setContentIntent(pendingIntent);


        if (worksOnPrevLocation) {
            String offlineString = description + "\n\n" + context.getString(R.string.offlineLocation);
            builder.setContentText(offlineString).setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(offlineString));
        }
        notification = builder.build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIF_ID, notification);
    }

    public static String takePicture(final Context parentContext, ImageView imageView, String fileName){
        File imageFile = new File(MainActivity.appFilePath, fileName + MainActivity.endOfFile);
        final Uri imageUri = FileProvider.getUriForFile(
                (Activity) parentContext,
                "com.android2.taxidrivershelpeachother.provider", //(use your app signature + ".provider" )
                imageFile);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(imageUri, "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                parentContext.startActivity(intent);
            }
        });
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        ((Activity)parentContext).startActivityForResult(intent, CAMERA_REQUEST);

        return imageFile.getAbsolutePath();
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean getIsInFetchingDataProgress() {
        return isInFetchingDataProgress;
    }

    public void setInFetchingDataProgress(boolean inFetchingDataProgress) {
        isInFetchingDataProgress = inFetchingDataProgress;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean checkIfShuttleIsImmediate(ShuttleItem shuttleItem) {
        return (FireBaseHandler.getInstance().getCollectionNameForShuttle(shuttleItem, true).equalsIgnoreCase("immediateShuttles"));
    }

    public static Date localToGMT(Date localDateToUTC) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(localDateToUTC);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cal.getTime();
    }

    public static Date gmtToLocalDate(Date dateToLocalizeFromUTC) {
        String timeZone = Calendar.getInstance().getTimeZone().getID();
        Date local = new Date(dateToLocalizeFromUTC.getTime() + TimeZone.getTimeZone(timeZone).getOffset(dateToLocalizeFromUTC.getTime()));
        return local;
    }
}
