package com.android2.taxidrivershelpeachother.model;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.LogicHandler;
import com.android2.taxidrivershelpeachother.controller.ShuttleItemAdapter;
import com.android2.taxidrivershelpeachother.controller.ShuttleLogic;
import com.android2.taxidrivershelpeachother.view.AuthenticationFragment;
import com.android2.taxidrivershelpeachother.view.AvailableShuttleFragment;
import com.android2.taxidrivershelpeachother.view.MainActivity;
import com.android2.taxidrivershelpeachother.view.MenuFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;
//import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class FireBaseHandler {
    private static FireBaseHandler instance = null;
    private static Object lock = new Object();
    private Activity activity;
    private FragmentManager fragmentManager;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId, code;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private GeoFirestore geoFirestore = new GeoFirestore(db.collection("shuttles"));
    private GeoFirestore todayRelevantGeoFireStore;
    private GeoFirestore tomorrowRelevantGeoFireStore;
    private EditText passwordEditText;
    private String userPhoneNumber;
    private User loggedInUser;
    private TaxiInformation taxiInformation;
    private List<Complaint> complaints;
    private List<ShuttleItem> soldLeads, boughtLeads;
    private final List<ShuttleItem> availableShuttlesFound = new ArrayList<>();
    private final List<ShuttleIDAndGeoPoint> availableShuttlesFoundBasicInfo = new ArrayList<>();
    private final List<String> alreadyCheckedShuttlesIDs = new ArrayList<>();
    private final List<String> todayAvailableDelayedShuttlesFoundIDs = new ArrayList<>();
    private final List<String> tomorrowAvailableDelayedShuttlesFoundIDs = new ArrayList<>();
    private final List<String> pastShuttlesFoundIDs = new ArrayList<>();
    private int counterOfGetDistanceAndTIme = 0;
    private Location o1Location  = new Location("o1");
    private Location o2Location  = new Location("o2");
    private float o1DistToLocation;
    private float o2DistToLocation;
    private ShuttleLogic shuttleLogic;
    private LogicHandler logicHandler;
    private Location currentLocation;
    private Map<String, Integer> delayedNearShuttles = new HashMap<>();
    private boolean succeedToTakeShuttle = false, someoneElseTookTheShuttle = false;
    private boolean handlingDriverPhoneNeedToBeDeleted = false;
    private AvailableShuttleFragment currFragment;


    private FireBaseHandler() {
        firebaseAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    if (passwordEditText != null) passwordEditText.setText(code);
                    verifyCode(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                String developmentMode = "This app is not authorized to use Firebase Authentication. Please verify that the correct package name and SHA-1 are configured in the Firebase Console. [ App validation failed. Is app running on a physical device? ]";
                String errorMsg = e.getMessage().toString();
                boolean isInDevelopmentModeUsingEmulator = errorMsg.equalsIgnoreCase(developmentMode);

                if (isInDevelopmentModeUsingEmulator) {
                    code = "123456";
                    if (passwordEditText != null) passwordEditText.setText(code);
                    verifyCode(code);
                } else {
                    Toast.makeText(activity.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.root_layout, new AuthenticationFragment(), null).addToBackStack(null);
                fragmentTransaction.commit();
            }
        };
    }

    public void setLogicHandler(LogicHandler logicHandler) {
        this.logicHandler = logicHandler;
    }

    public void setPasswordEditText(EditText passwordEditText) {
        this.passwordEditText = passwordEditText;
    }

    public static FireBaseHandler getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new FireBaseHandler();
                }
            }
        }

        return instance;
    }

    public void sendVerificationCode(Activity activity, FragmentManager fragmentManager, String phoneNumber) {
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.userPhoneNumber = phoneNumber;

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    public void verifyCode(String code) {
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithCredential(phoneAuthCredential);
    }

    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String time = String.valueOf(Calendar.getInstance().getTimeInMillis());
                    addMessage(userPhoneNumber + " logged in time:" + time);

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    MenuFragment menuFragment = new MenuFragment();
                    fragmentTransaction.replace(R.id.root_layout, menuFragment, null).addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void addNewUserToDB(User newUser) {
        String key = newUser.getPhone();
        uploadFile(key, MainActivity.driverLicensePhotoFileName);
        uploadFile(key, MainActivity.vehicleLicensePhotoFileName);
        uploadFile(key, MainActivity.vehicleInsurancePhotoFIleName);

        // set new user driverLicensePhoto image path in Cloud Firestore with the correct reference from Firebase Storage
        StorageReference driverLicensePhotoRef = mStorageRef.child("images/" + key + MainActivity.driverLicensePhotoFileName + MainActivity.endOfFile);
        newUser.setImageUrl(driverLicensePhotoRef.getPath());

        // Create a new user with a first and last name, phone number, dateOfBirth, taxiInformation
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", newUser.getFirstName());
        user.put("lastName", newUser.getLastName());
        user.put("phone", newUser.getPhone());
        user.put("dateOfBirth", newUser.getDateOfBirth());
        user.put("taxiInformation", newUser.getTaxiInformation());
        user.put("status", newUser.getStatus());
        user.put("imageUrl", newUser.getImageUrl());
        user.put("balance", newUser.getBalance());
        user.put("isAvailable", newUser.isAvailable());
        user.put("complaints", newUser.getComplaints());
        user.put("soldLeads", newUser.getSoldLeads());
        user.put("boughtLeads", newUser.getBoughtLeads());


        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void uploadFile(String key, String fileName) {
        Uri file = Uri.fromFile(new File(MainActivity.appFilePath + fileName + MainActivity.endOfFile));
        final String fullFileName = key + "_" + fileName + MainActivity.endOfFile;
        StorageReference riversRef = mStorageRef.child("images/" + fullFileName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Successfully uploaded:" + fullFileName);
                        // Get a URL to the uploaded content
//                        Uri downloadUrl = taskSnapshot.getStorage().getDownloadUrl().getResult();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, "Upload failed:" + fullFileName);

                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }

    public void downloadImageIntoImageView(String fileName, final ImageView imageView) {
        String key = userPhoneNumber;
        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File image = localFile;
        final String fullFileName = key + "_" + fileName + MainActivity.endOfFile;

        StorageReference riversRef = mStorageRef.child("images/" + fullFileName);

        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Successfully downloaded:" + fullFileName);

                        // Successfully downloaded data to local file
                        // ...
                        Picasso.get().load(image).fit().into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }

    public void getLoggedInUserFromDB(final MenuFragment menuFragment) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Query query = db.collection("users").whereEqualTo("phone", userPhoneNumber);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Map<String, Object> userData = document.getData();
                        String userStatus = (String) userData.get("status");
                        if (userStatus.equalsIgnoreCase("Approved")) {
                            String firstName, lastName, phone, dateOfBirth, imageUrl;
                            double balance;
                            boolean isAvailable;

                            Map<String, Object> taxiInformationMap, complaintsMap, soldLeadsMap, boughtLeadsMap;

                            firstName = (String) userData.get("firstName");
                            lastName = (String) userData.get("lastName");
                            dateOfBirth = (String) userData.get("dateOfBirth");
                            imageUrl = (String) userData.get("imageUrl");
                            taxiInformationMap = (Map<String, Object>) userData.get("taxiInformation");
                            balance = (long) userData.get("balance");
                            isAvailable = (boolean) userData.get("isAvailable");
                            complaints = (List<Complaint>) userData.get("complaints");
                            soldLeads = (List<ShuttleItem>) userData.get("soldLeads");
                            boughtLeads = (List<ShuttleItem>) userData.get("boughtLeads");
                            putTaxiInformationFromMap(taxiInformationMap);

                            loggedInUser = new User(firstName, lastName, userPhoneNumber, dateOfBirth, imageUrl, taxiInformation, balance, isAvailable);
                            loggedInUser.setComplaints(complaints);
                            loggedInUser.setSoldLeads(soldLeads);
                            loggedInUser.setBoughtLeads(boughtLeads);

                            menuFragment.setLoggedInUser(loggedInUser);
                            menuFragment.setDriverName(firstName + " " + lastName);
                            menuFragment.getAvailableSwitch().setChecked(isAvailable);
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void getSupplierOrDriverNameFromDB(String supplierPhoneNumber, final ShuttleItemAdapter.ItemViewHolder holder) {
        Query query = db.collection("users").whereEqualTo("phone", supplierPhoneNumber);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Map<String, Object> userData = document.getData();
                        String supplierFirstName = (String) userData.get("firstName");
                        String supplierLastName = (String) userData.get("lastName");
                        if(supplierFirstName != null && supplierLastName != null){
                            holder.setSupplierOrDriverNameTV(supplierFirstName + " " + supplierLastName);
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void putTaxiInformationFromMap(Map<String, Object> taxiInformationMap) {
        String licenseNumber, taxiNumber, licenseValidUntil, insuranceValidUntil, model;
        boolean isAccessible;
        int passengerSeats;
        long passengerSeatsLong;

        licenseNumber = (String) taxiInformationMap.get("licenseNumber");
        taxiNumber = (String) taxiInformationMap.get("taxiNumber");
        licenseValidUntil = (String) taxiInformationMap.get("licenseValidUntil");
        insuranceValidUntil = (String) taxiInformationMap.get("insuranceValidUntil");
        model = (String) taxiInformationMap.get("model");
        passengerSeatsLong = (long) taxiInformationMap.get("passengerSeats");
        passengerSeats = (int) passengerSeatsLong;
        isAccessible = (boolean) taxiInformationMap.get("accessible");

        taxiInformation = new TaxiInformation(licenseNumber, taxiNumber, licenseValidUntil, insuranceValidUntil, model, passengerSeats, isAccessible);
    }


    private Task<String> addMessage(String text) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("push", true);

        return mFunctions
                .getHttpsCallable("addMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }

    public void addNewShuttleToDB(final ShuttleItem newShuttleItem, String collectionName) {

        // Create a new shuttle
        Map<String, Object> shuttle = new HashMap<>();
        shuttle.put("shuttleDate", newShuttleItem.getShuttleDate());
        shuttle.put("shuttleTime", newShuttleItem.getShuttleTime());
        shuttle.put("originAddress", newShuttleItem.getOriginAddress());
        shuttle.put("destinationAddress", newShuttleItem.getDestinationAddress());
        shuttle.put("remarks", newShuttleItem.getRemarks());
        shuttle.put("price", newShuttleItem.getPrice());
        shuttle.put("commissionFee", newShuttleItem.getCommissionFee());
        shuttle.put("passenger", newShuttleItem.getPassenger());
        shuttle.put("shuttleDistanceInKm", newShuttleItem.getShuttleDistanceInKm());
        shuttle.put("shuttleDistanceInMinutes", newShuttleItem.getShuttleDistanceInMinutes());
        shuttle.put("originLatLng", newShuttleItem.getOriginLatLng());
        shuttle.put("publishedBy", loggedInUser.getPhone());

        final GeoPoint geoPoint = new GeoPoint(newShuttleItem.getOriginLatLng().latitude, newShuttleItem.getOriginLatLng().longitude);

        // Add a new document with a generated ID
        db.collection(collectionName)
                .add(shuttle)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        geoFirestore.setLocation(documentReference.getId(), geoPoint);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        db.collection("shuttles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void getAvailableShuttlesFromDB(final AvailableShuttleFragment availableShuttleFragment) {
        final ShuttleLogic shuttleLogic = new ShuttleLogic(availableShuttleFragment.getContext());
        String fragmentName = availableShuttleFragment.getFragmentName();

        currFragment = availableShuttleFragment;
        if(fragmentName.equalsIgnoreCase("availableShuttles")) {
            Query immediateShuttlesQuery = db.collection("shuttles");
            Query todayDelayedShuttlesQuery = db.collection("todayDelayedShuttles");
            Query tomorrowShuttlesQuery = db.collection("tomorrowDelayedShuttles");
            Query delayedShuttlesQuery = db.collection("delayedShuttles");

            invokeGetShuttlesQuery(immediateShuttlesQuery, availableShuttleFragment);
            invokeGetShuttlesQuery(todayDelayedShuttlesQuery, availableShuttleFragment);
            invokeGetShuttlesQuery(tomorrowShuttlesQuery, availableShuttleFragment);
            invokeGetShuttlesQuery(delayedShuttlesQuery, availableShuttleFragment);
        }
        else if(fragmentName.equalsIgnoreCase("commitmentShuttles")) {
            Query soldShuttlesQuery = db.collection("soldShuttles").whereEqualTo("handlingDriverPhone", userPhoneNumber);

            invokeGetShuttlesQuery(soldShuttlesQuery, availableShuttleFragment);
        }
        else if(fragmentName.equalsIgnoreCase("leadManagement")) {
            Query waitingForDriverShuttlesQuery = db.collection("shuttles").whereEqualTo("publishedBy", userPhoneNumber);
            Query soldShuttlesQuery = db.collection("soldShuttles").whereEqualTo("publishedBy", userPhoneNumber);

            invokeGetShuttlesQuery(waitingForDriverShuttlesQuery, availableShuttleFragment);
            invokeGetShuttlesQuery(soldShuttlesQuery, availableShuttleFragment);
        }
    }

    private void invokeGetShuttlesQuery(@NonNull Query query, final AvailableShuttleFragment availableShuttleFragment){
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    collectShuttleInfo(task, availableShuttleFragment);
                }
                else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void collectShuttleInfo(@NonNull Task<QuerySnapshot> task, final AvailableShuttleFragment availableShuttleFragment){
        final List<ShuttleItem> availableShuttles = new ArrayList<>();
        boolean condition;

        availableShuttles.addAll(availableShuttleFragment.getShuttleItems());

        for (QueryDocumentSnapshot document : task.getResult()) {
            Log.d(TAG, document.getId() + " => " + document.getData());
            Map<String, Object> shuttleData = document.getData();
            String publishedBy = (String) shuttleData.get("publishedBy");
            String currFragment = availableShuttleFragment.getFragmentName();
            boolean isPublishedByTheCurrUser = publishedBy.equalsIgnoreCase(userPhoneNumber);
            boolean currFragmentIsLeadManagement = currFragment.equalsIgnoreCase("leadManagement");

            condition = (!currFragmentIsLeadManagement && !isPublishedByTheCurrUser) || (currFragmentIsLeadManagement && isPublishedByTheCurrUser);
            if (condition) {
                ShuttleItem shuttleItem = parseShuttle(shuttleData);
                if(shuttleItem != null) {
                    shuttleItem.setId(document.getId());
                    if(MainActivity.getPrevLocation() != null) {
                        shuttleLogic.getTimeAndDistanceToShuttle(availableShuttleFragment, shuttleItem, MainActivity.getPrevLocation());
                    }
                    availableShuttles.add(shuttleItem);
                }
            }
        }
        if(availableShuttleFragment != null) {
            sortShuttlesAndAddToAvailableShuttleFragment(availableShuttles, availableShuttleFragment);
        }
    }

    private void sortShuttlesAndAddToAvailableShuttleFragment(final List<ShuttleItem> availableShuttles, final AvailableShuttleFragment availableShuttleFragment){
        availableShuttles.sort(new Comparator<ShuttleItem>() {
            @Override
            public int compare(ShuttleItem o1, ShuttleItem o2) {
                try {
                    String o1dateStr, o1timeStr, o2dateStr, o2timeStr;

                    o1dateStr = o1.getShuttleDate();
                    o1timeStr = o1.getShuttleTime().replace(" ","");
                    o2dateStr = o2.getShuttleDate();
                    o2timeStr = o2.getShuttleTime().replace(" ","");

                    Date o1date = LogicHandler.dateAndTimeSimpleDateFormat.parse(o1dateStr + " " + o1timeStr);
                    Date o2date = LogicHandler.dateAndTimeSimpleDateFormat.parse(o2dateStr + " " + o2timeStr);
                    if(o1date.before(o2date)) {
                        return -1;
                    }
                    else if(o2date.before(o1date)) {
                        return 1;
                    }
                    else return 0;

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return 0;
            }
        });

        availableShuttleFragment.setShuttleItems(availableShuttles);
    }


    public void getAvailableShuttlesWithMaxDistanceFromCurrentLocationFromDB(final Context context, final GeoPoint currentLocation, final double maxRadius) {
        GeoQuery geoQuery = geoFirestore.queryAtLocation(currentLocation, maxRadius);
        geoQuery.removeAllListeners();
        counterOfGetDistanceAndTIme = 0;
        shuttleLogic = new ShuttleLogic(context);
        logicHandler.setInFetchingDataProgress(true);
        this.currentLocation = new Location(logicHandler.getLastLocation());

        if(availableShuttlesFoundBasicInfo.size() == 0) {
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String s, GeoPoint geoPoint) {
                    if (!alreadyCheckedShuttlesIDs.contains(s) && !delayedNearShuttles.values().contains(s)) {
                        availableShuttlesFoundBasicInfo.add(new ShuttleIDAndGeoPoint(s, geoPoint));
                    }
                }

                @Override
                public void onKeyExited(String s) {

                }

                @Override
                public void onKeyMoved(String s, GeoPoint geoPoint) {

                }

                @Override
                public void onGeoQueryReady() {
                    if(availableShuttlesFoundBasicInfo.size() == 0) {
                        if(delayedNearShuttles.values().size() != 0){
                            delayedNearShuttles.clear();
                        }
                        logicHandler.setInFetchingDataProgress(false);
                    }
                    else{
                        getNearestNShuttles(5);
                    }
                }

                @Override
                public void onGeoQueryError(Exception e) {

                }
            });
        } else{
            getNearestNShuttles(5);
//            logicHandler.setInFetchingDataProgress(false);
        }
    }

    private void getNearestNShuttles(final int numberOfShuttlesToCollect) {
        availableShuttlesFoundBasicInfo.sort(new Comparator<ShuttleIDAndGeoPoint>() {
            @Override
            public int compare(ShuttleIDAndGeoPoint o1, ShuttleIDAndGeoPoint o2) {

                o1Location.setLatitude(o1.getLocation().getLatitude());
                o1Location.setLongitude(o1.getLocation().getLongitude());
                o2Location.setLatitude(o2.getLocation().getLatitude());
                o2Location.setLongitude(o2.getLocation().getLongitude());
                o1DistToLocation = currentLocation.distanceTo(o1Location);
                o2DistToLocation = currentLocation.distanceTo(o2Location);

                if (o1DistToLocation < o2DistToLocation) {
                    return -1;
                } else if (o1DistToLocation > o2DistToLocation) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        final int actualNumberOfShuttlesToCollect;
        if (availableShuttlesFoundBasicInfo.size() < numberOfShuttlesToCollect) {
            actualNumberOfShuttlesToCollect = availableShuttlesFoundBasicInfo.size();
        } else {
            actualNumberOfShuttlesToCollect = numberOfShuttlesToCollect;
        }

        // get full information for nearest N locations
        for (int i = 0; i < actualNumberOfShuttlesToCollect; i++) {
            final String ShuttleID = availableShuttlesFoundBasicInfo.get(i).getId();
            final Location location = currentLocation;
            DocumentReference documentReference = db.collection("shuttles").document(availableShuttlesFoundBasicInfo.get(i).getId());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onKeyEntered + onComplete " + task.getResult().getId() + " => " + task.getResult().getData());
                        Map<String, Object> shuttleData = task.getResult().getData();
                        if(shuttleData != null) {
                            String publishedBy = (String) shuttleData.get("publishedBy");
                            // TODO remove true from the condition - already done
                            if (publishedBy != null && !publishedBy.equalsIgnoreCase(userPhoneNumber)) {
                                ShuttleItem shuttleItem = parseShuttle(shuttleData);
                                shuttleItem.setId(ShuttleID);
                                if (logicHandler.checkIfShuttleIsImmediate(shuttleItem) == true) {
                                    availableShuttlesFound.add(shuttleItem);
                                    shuttleLogic.getTimeAndDistanceToShuttle(null, shuttleItem, location);
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }

        logicHandler.setInFetchingDataProgress(false);
    }

    public void sortAndSendShuttlesToDriver(){
        counterOfGetDistanceAndTIme++;
        // sort and suggest shuttles
        if(availableShuttlesFound.size() == counterOfGetDistanceAndTIme)
        {
            counterOfGetDistanceAndTIme = 0;
            availableShuttlesFound.sort(new Comparator<ShuttleItem>() {
                @Override
                public int compare(ShuttleItem o1, ShuttleItem o2) {
                    if (o1.getDistanceToShuttleInMinutes() > o2.getDistanceToShuttleInMinutes()) {
                        return 1;
                    } else if (o1.getDistanceToShuttleInMinutes() < o2.getDistanceToShuttleInMinutes()) {
                        return -1;
                    } else return 0;
                }
            });

            logicHandler.setInFetchingDataProgress(false);
            logicHandler.addNotification();
        }
    }

    public List<ShuttleItem> getAvailableShuttlesFound() {
        return availableShuttlesFound;
    }

    public ShuttleItem getBestShuttle() {
        ShuttleItem shuttleItemToReturn = null;
        if(availableShuttlesFound.size() > 0) {
            int distanceToShuttleInMinutes =  availableShuttlesFound.get(0).getDistanceToShuttleInMinutes();
            int maxMinuteFilterBasedOnMaxTimeForPickup = availableShuttlesFound.get(0).getDistanceToShuttleInMinutes();
            while(distanceToShuttleInMinutes > maxMinuteFilterBasedOnMaxTimeForPickup){
                delayedNearShuttles.put(availableShuttlesFound.get(0).getId(), distanceToShuttleInMinutes - maxMinuteFilterBasedOnMaxTimeForPickup);
                availableShuttlesFound.remove(0);
            }
            if(availableShuttlesFound.size() > 0) {
                shuttleItemToReturn = availableShuttlesFound.get(0);
                alreadyCheckedShuttlesIDs.add(shuttleItemToReturn.getId());
                availableShuttlesFound.remove(0);
                removeIdFromAvailableShuttlesFoundBasicInfo(shuttleItemToReturn.getId());
            }
        }

        return shuttleItemToReturn;
    }

    private void removeIdFromAvailableShuttlesFoundBasicInfo(String id){
        for (int i = 0; i < availableShuttlesFoundBasicInfo.size() ; i++){
            if(availableShuttlesFoundBasicInfo.get(i).getId().equalsIgnoreCase(id)){
                availableShuttlesFoundBasicInfo.remove(i);
                break;
            }
        }
    }

    private ShuttleItem parseShuttle(Map<String, Object> shuttleData) {
        String publishedBy, originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, passengerName, passengerPhone, handlingDriverPhone;
        int price, commissionFee;
        double shuttleDistanceInKm;
        int shuttleDistanceInMinutes;
        Map<String, Object> passengerInformationMap;
        Map<String, Object> originLatLngInformationMap;
        Passenger passenger;
        LatLng originLatLng;
        double lat, lng;
        ShuttleItem shuttleItem = null;

        publishedBy = (String) shuttleData.get("publishedBy");
        originLatLngInformationMap = (Map<String, Object>) shuttleData.get("originLatLng");
        originAddress = (String) shuttleData.get("originAddress");
        destinationAddress = (String) shuttleData.get("destinationAddress");
        remarks = (String) shuttleData.get("remarks");
        shuttleDate = (String) shuttleData.get("shuttleDate");
        shuttleTime = (String) shuttleData.get("shuttleTime");
        handlingDriverPhone = (String) shuttleData.get("handlingDriverPhone");

        if(shuttleData.get("price") != null) {
            price = (int) ((long)shuttleData.get("price"));
        }
        else{
            price = 0;
        }
        if(shuttleData.get("commissionFee") != null) {
            commissionFee = (int) ((long)shuttleData.get("commissionFee"));
        }
        else{
            commissionFee = 0;
        }
        if(shuttleData.get("shuttleDistanceInKm") != null) {
            shuttleDistanceInKm = (double) shuttleData.get("shuttleDistanceInKm");
        }
        else{
            shuttleDistanceInKm = 0;
        }
        if(shuttleData.get("shuttleDistanceInMinutes") != null) {
            shuttleDistanceInMinutes = (int) ((long)shuttleData.get("shuttleDistanceInMinutes"));
        }
        else{
            shuttleDistanceInMinutes = 0;
        }

        passengerInformationMap = (Map<String, Object>) shuttleData.get("passenger");
        if(passengerInformationMap != null && originLatLngInformationMap != null) {
            passengerName = (String) passengerInformationMap.get("name");
            passengerPhone = (String) passengerInformationMap.get("phone");
            passenger = new Passenger(passengerName, passengerPhone);

            if(originLatLngInformationMap.get("latitude") != null) {
                lat = (double) originLatLngInformationMap.get("latitude");
            }
            else{
                lat = 0;
            }
            if(originLatLngInformationMap.get("longitude") != null) {
                lng = (double) originLatLngInformationMap.get("longitude");
            }
            else{
                lng = 0;
            }

            originLatLng = new LatLng(lat, lng);

            shuttleItem = new ShuttleItem(originLatLng, originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, price, commissionFee, passenger, shuttleDistanceInKm, shuttleDistanceInMinutes);
            shuttleItem.setPublishedBy(publishedBy);
            shuttleItem.setHandlingDriverPhone(handlingDriverPhone);
        }


        return shuttleItem;
    }

    public void moveFirestoreDocument(final DocumentReference fromPath, final DocumentReference toPath) {
        if(fromPath != null && toPath != null) {
            fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.getData() != null) {
                            toPath.set(document.getData())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            if(handlingDriverPhoneNeedToBeDeleted){
                                                toPath.update("handlingDriverPhone", FieldValue.delete());
                                                handlingDriverPhoneNeedToBeDeleted = false;
                                            }
                                            deleteShuttle(fromPath);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    public void putShuttleInTodayDelayedShuttles(final String shuttleID, int delayInMinutes) {
        final DocumentReference documentReference = db.collection("todayDelayedShuttles").document();
        db.collection("shuttles").document(shuttleID).update("delayInMinutes", delayInMinutes).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    moveFirestoreDocument(db.collection("shuttles").document(shuttleID), documentReference);
                    removeIdFromAvailableShuttlesFoundBasicInfo(shuttleID);
                }
            }
        });
    }

    private void putCanceledShuttleInShuttles(final String shuttleID) {
        handlingDriverPhoneNeedToBeDeleted = true;
        DocumentReference documentReference = db.collection("shuttles").document();
        moveFirestoreDocument(db.collection("soldShuttles").document(shuttleID), documentReference);
    }

    public void putShuttleInExpiredShuttles(final String shuttleID) {
        DocumentReference documentReference = db.collection("expiredShuttles").document();
        moveFirestoreDocument(db.collection("shuttles").document(shuttleID), documentReference);
        removeIdFromAvailableShuttlesFoundBasicInfo(shuttleID);
    }

    private void moveEverythingFromExpiredToCurrent(){
        db.collection("expiredShuttles").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DocumentReference documentReference = db.collection("shuttles").document();
                        moveFirestoreDocument(document.getReference(), documentReference);
                    }
                }
            }
        });
    }

    public void putShuttleInTomorrowDelayedShuttles(String shuttleID) {
        DocumentReference documentReference = db.collection("tomorrowDelayedShuttles").document();
        moveFirestoreDocument(db.collection("shuttles").document(shuttleID), documentReference);
        removeIdFromAvailableShuttlesFoundBasicInfo(shuttleID);
    }

    public void putShuttleInDelayedShuttles(String shuttleID) {
        DocumentReference documentReference = db.collection("delayedShuttles").document();
        moveFirestoreDocument(db.collection("shuttles").document(shuttleID), documentReference);
        removeIdFromAvailableShuttlesFoundBasicInfo(shuttleID);
    }

    public void takeShuttle(final String shuttleID){
        final List<DocumentReference> documentReferences = new ArrayList<>();
        int index = 0;

        succeedToTakeShuttle = false;
        someoneElseTookTheShuttle = false;

        documentReferences.add(db.collection("shuttles").document(shuttleID));
        documentReferences.add(db.collection("todayDelayedShuttles").document(shuttleID));
        documentReferences.add(db.collection("delayedShuttles").document(shuttleID));

        while (index < documentReferences.size() && !someoneElseTookTheShuttle && !succeedToTakeShuttle)
        {
            moveShuttleFromCurrentCollectionToSoldCollection(shuttleID, documentReferences.get(index++));
        }
    }

    private void moveShuttleFromCurrentCollectionToSoldCollection(final String shuttleID , final DocumentReference documentReference){
        // check if shuttle is not sold yet and mark it as sold
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onKeyEntered + onComplete " + task.getResult().getId() + " => " + task.getResult().getData());
                    Map<String, Object> shuttleData = task.getResult().getData();
                    if (shuttleData != null) {
                        String handlingDriverPhone = (String) shuttleData.get("handlingDriverPhone");
                        // TODO remove true from the condition - already done
                        if (handlingDriverPhone == null) {
                            // (async) Update one field
                            documentReference.update("handlingDriverPhone", userPhoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    putShuttleInSoldShuttles(shuttleID);
                                    // TODO show you got the shuttle
                                }
                            });
                        }
                        else {
                            someoneElseTookTheShuttle = true;
                        }

                        removeIdFromAvailableShuttlesFoundBasicInfo(shuttleID);
                    }
                    else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            }
        });
    }

    public void putShuttleInSoldShuttles(String shuttleID) {
        final List<DocumentReference> documentReferences = new ArrayList<>();
        final DocumentReference documentReference = db.collection("soldShuttles").document();
        int index = 0;

        documentReferences.add(db.collection("shuttles").document(shuttleID));
        documentReferences.add(db.collection("todayDelayedShuttles").document(shuttleID));
        documentReferences.add(db.collection("delayedShuttles").document(shuttleID));

        while (index < documentReferences.size())
        {
            moveFirestoreDocument(documentReferences.get(index++), documentReference);
        }
    }

    public void cancelShuttle(String shuttleID) {
        putCanceledShuttleInShuttles(shuttleID);

        // TODO refund user Bit account and don't pay to seller
    }

    public void getDriverNameAndPhoneFromDB(String shuttleID, final ShuttleItemAdapter.ItemViewHolder holder) {
        db.collection("soldShuttles").document(shuttleID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Map<String, Object> shuttleData = document.getData();
                    if (shuttleData != null) {
                        String handlingDriverPhone = (String) shuttleData.get("handlingDriverPhone");
                        if (handlingDriverPhone != null) {
                            holder.setSupplierOrDriverNameTV(handlingDriverPhone);
                            getSupplierOrDriverNameFromDB(handlingDriverPhone, holder);
                        }
                    }
                }
                else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void deleteShuttle(DocumentReference fromPath) {
        fromPath.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        if(currFragment != null){
                            fragmentManager.popBackStack(currFragment.getFragmentName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.root_layout, new AvailableShuttleFragment(currFragment.getFragmentName()), null).addToBackStack(currFragment.getFragmentName());
                            fragmentTransaction.commit();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void deleteShuttleFromSoldShuttles(String shuttleID) {
        final DocumentReference fromPath = db.collection("soldShuttles").document(shuttleID);
        deleteShuttle(fromPath);
    }

    public void editShuttle(String shuttleID, ShuttleItem item) {
        final DocumentReference documentReference = db.collection("soldShuttles").document(shuttleID);
        documentReference.set(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}


