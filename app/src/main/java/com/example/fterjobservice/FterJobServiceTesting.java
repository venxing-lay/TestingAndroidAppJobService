//package com.example.fterjobservice;
//
//import android.annotation.SuppressLint;
//import android.app.job.JobParameters;
//import android.app.job.JobService;
//
//import androidx.core.content.ContextCompat;
//
//import android.annotation.TargetApi;
//import android.os.Build;
//import android.os.Bundle;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.Looper;
//import android.provider.CallLog;
//import android.database.Cursor;
//import android.Manifest;
//
////Json
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.json.JSONArray;
//
////Util
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.HashMap;
//
//import android.util.Log;
//
//// Telephony
//import android.telephony.TelephonyManager;
//import android.provider.Telephony;
//import android.content.Context;
//import android.telephony.SubscriptionInfo;
//import android.telephony.SubscriptionManager;
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.content.ContentResolver;
//
////App Usages
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//
////AppInfo
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageManager;
//
//// Contact
//import android.provider.ContactsContract.PhoneLookup;
//import android.provider.ContactsContract;
//
//import java.io.InputStream;
//import java.lang.reflect.Array;
//
//import android.content.ContentUris;
//import android.graphics.BitmapFactory;
//import android.graphics.Bitmap;
//import android.net.Uri;
//
//import androidx.core.app.ActivityCompat;
//
////Location
//import android.location.Criteria;
//import android.location.Location;
//import android.location.Geocoder;
//import android.location.Address;
//import android.location.LocationManager;
//import android.location.LocationListener;
//
////local storage
//import android.content.SharedPreferences;
//import android.widget.EditText;
//
//import java.sql.Timestamp;
//import java.util.Locale;
//
//import android.os.Bundle;
//
//
//@TargetApi(Build.VERSION_CODES.M)
//public class FterJobServiceTesting extends JobService implements LocationListener {
//    private static final String TAG = "JobService";
//    private boolean jobCancelled = false;
//    private Context ctx;
//    private static final String locationKey = "locationKey";
//    private static final String latitudeKey = "latitudeKey";
//    private static final String longitudeKey = "longitudeKey";
//    private static final String MyPREFERENCES = "MyPrefs";
//
//    @Override
//    public boolean onStartJob(JobParameters params) {
//        ctx = getApplicationContext();
//        Log.d(TAG, "Job started");
//        doBackgroundWork(params);
//
//        return true;
//    }
//
//    private void doBackgroundWork(final JobParameters params) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                Looper.prepare();
//                for (int i = 0; i < 7; i++) {
//                    Log.d(TAG, "run: " + i);
//                    if (jobCancelled) {
//                        return;
//                    }
//
//                    try {
//                        handleCallMethod(i);
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                Log.d(TAG, "Job finished");
//                jobFinished(params, true);
//            }
//        }).start();
//    }
//
//    private void handleCallMethod(int i) {
//        switch (i) {
//            case 0:
//                if (hasPermission(Manifest.permission.READ_PHONE_STATE)) {
//                    getYourPhoneNumber(ctx);
//                } else {
//                    Log.d("", "No Permissions");
//                }
//                break;
//            case 1:
//                getInstalledApps(ctx);
//                break;
//            case 2:
//                getCallLogs(ctx);
//                break;
//            case 3:
//                getAllContactName(ctx);
//                break;
//            case 4:
//                getDeviceLongLat(ctx);
//                break;
//            case 5:
//                getSMSLogs(ctx);
//                break;
//            case 6:
//                getAppUsages(ctx);
//                break;
//
//            default:
//                break;
//        }
//    }
//
//    private boolean hasPermission(String permission) {
//        int result = ctx.checkCallingOrSelfPermission(permission);
//        return result == PackageManager.PERMISSION_GRANTED;
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters params) {
//        Log.d(TAG, "Job cancelled before completion");
//        jobCancelled = true;
//        return true;
//    }
//
//    // App Info
//    private void getInstalledApps(Context ctx) {
//        PackageManager pm = ctx.getPackageManager();
//        List<ApplicationInfo> applications = pm.getInstalledApplications(0);
//        List<HashMap<String, Object>> entries = new ArrayList<>();
//
//        for (ApplicationInfo appInfo : applications) {
//            HashMap<String, Object> map = new HashMap<>();
//
//            map.put("name", appInfo.name);
//            map.put("appGame", appInfo.CATEGORY_GAME);
//            map.put("appNews", appInfo.CATEGORY_NEWS);
//            map.put("appSocial", appInfo.CATEGORY_SOCIAL);
//            map.put("appProductivity", appInfo.CATEGORY_PRODUCTIVITY);
//            map.put("appVideo", appInfo.CATEGORY_VIDEO);
//            map.put("appAudio", appInfo.CATEGORY_AUDIO);
//            map.put("packageName", appInfo.packageName);
//            map.put("permission", appInfo.permission);
//            map.put("className", appInfo.className);
//            map.put("processName", appInfo.processName);
//            map.put("flags", appInfo.flags);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                map.put("minSdkVersion", appInfo.minSdkVersion);
//            }
//            // map.put("compileSdkVersion", appInfo.compileSdkVersion);
//            map.put("flagSystem", ApplicationInfo.FLAG_SYSTEM);
//
//            String categoryStr = "";
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                categoryStr = String.valueOf(appInfo.getCategoryTitle(ctx, appInfo.category));
//            }
//            map.put("category", categoryStr);
//
//            entries.add(map);
//        }
//        Log.d("", entries.toString());
//
//    }
//
//    // Get Call Logs =================================
//    private void getCallLogs(Context ctx) {
//        // get Sim Number
//        TelephonyManager tMgr = ContextCompat.getSystemService(ctx, TelephonyManager.class);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        String getSimNumber = tMgr.getLine1Number();
//
//        final String[] CURSOR_PROJECTION = {
//                CallLog.Calls.CACHED_FORMATTED_NUMBER,
//                CallLog.Calls.NUMBER,
//                CallLog.Calls.TYPE,
//                CallLog.Calls.DATE,
//                CallLog.Calls.DURATION,
//                CallLog.Calls.CACHED_NAME,
//                CallLog.Calls.CACHED_NUMBER_TYPE,
//                CallLog.Calls.CACHED_NUMBER_LABEL,
//                CallLog.Calls.CACHED_MATCHED_NUMBER,
//                CallLog.Calls.PHONE_ACCOUNT_ID,
//        };
//
//        SubscriptionManager subscriptionManager = ContextCompat.getSystemService(ctx, SubscriptionManager.class);
//        List<SubscriptionInfo> subscriptions = null;
//        if (subscriptionManager != null) {
//            subscriptions = subscriptionManager.getActiveSubscriptionInfoList();
//        }
//
//        try (Cursor cursor = ctx.getContentResolver().query(
//                CallLog.Calls.CONTENT_URI,
//                CURSOR_PROJECTION,
//                null,
//                null,
//                CallLog.Calls.DATE + " DESC")) {
//            List<HashMap<String, Object>> entries = new ArrayList<>();
//            while (cursor != null && cursor.moveToNext()) {
//                HashMap<String, Object> map = new HashMap<>();
//                map.put("formattedNumber", cursor.getString(0));
//                map.put("number", cursor.getString(1));
//                map.put("callType", cursor.getInt(2));
//                map.put("timestamp", cursor.getLong(3));
//                map.put("duration", cursor.getInt(4));
//                map.put("name", cursor.getString(5));
//                map.put("cachedNumberType", cursor.getInt(6));
//                map.put("cachedNumberLabel", cursor.getString(7));
//                map.put("cachedMatchedNumber", cursor.getString(8));
//                map.put("simDisplayName", getSimDisplayName(subscriptions, cursor.getString(9)));
//                // map.put("yourPhoneNumber", getSimNumber);
//                map.put("phoneAccountId", cursor.getString(9));
//
//                entries.add(map);
//            }
//            Log.d("", "entry.toString()");
//            for (HashMap entry : entries) {
//
//                Log.d("", entry.toString());
//            }
//        } catch (Exception e) {
//            Log.d("", e.toString());
//        }
//    }
//
//    private String getSimDisplayName(List<SubscriptionInfo> subscriptions, String accountId) {
//        if (accountId != null && subscriptions != null) {
//            for (SubscriptionInfo info : subscriptions) {
//                if (Integer.toString(info.getSubscriptionId()).equals(accountId) ||
//                        accountId.contains(info.getIccId())) {
//                    return String.valueOf(info.getDisplayName());
//                }
//            }
//        }
//        return null;
//    }
//
//    // Get all Contact Name
//    @SuppressLint("Range")
//    private void getAllContactName(Context ctx) {
//        try {
//            List<String> list = new ArrayList<>();
//            ContentResolver contentResolver = ctx.getContentResolver();
//            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//            if (cursor.getCount() > 0) {
//                while (cursor.moveToNext()) {
//                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
//                        Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                                null,
//                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
//                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
//                                ctx.getContentResolver(),
//                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));
//
//                        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
//                        Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
//
//                        Bitmap photo = null;
//                        if (inputStream != null) {
//                            photo = BitmapFactory.decodeStream(inputStream);
//                        }
//                        while (cursorInfo.moveToNext()) {
//                            String contactName = cursor
//                                    .getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                            list.add(contactName);
//                        }
//
//                        cursorInfo.close();
//                    }
//                }
//                cursor.close();
//            }
//            Log.d("", list.toString());
//        } catch (Exception e) {
//            Log.d("", e.toString());
//        }
//
//    }
//
//    // App Usages
//    private void getAppUsages(Context ctx) {
//
//        try {
//            UsageStatsManager mUsageStatsManager = ContextCompat.getSystemService(ctx,
//                    UsageStatsManager.class);
//            List<UsageStats> appList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, 0,
//                    System.currentTimeMillis());
//            for (UsageStats usageStats : appList) {
//                Log.d("", String.valueOf(usageStats.getTotalTimeInForeground()));
//            }
//
//            Log.d("", appList.toString());
//        } catch (Exception e) {
//            Log.d("", e.toString());
//        }
//    }
//
//    // Get SMS logs
//    private void getSMSLogs(Context ctx) {
//        try (Cursor cursor = ctx.getContentResolver().query(
//                Telephony.Sms.CONTENT_URI,
//                null, null, null, null)) {
//            List<HashMap<String, Object>> entries = new ArrayList<>();
//            while (cursor != null && cursor.moveToNext()) {
//                HashMap<String, Object> map = new HashMap<>();
//
//                String smsDate = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
//                Date dateFormat = new Date(java.lang.Long.valueOf(smsDate));
//
//                String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
//                String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
//                String type = "";
//
//                int typeInt = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)));
//                if (typeInt == Telephony.Sms.MESSAGE_TYPE_INBOX)
//                    type = "inbox";
//                else if (typeInt == Telephony.Sms.MESSAGE_TYPE_SENT)
//                    type = "sent";
//                else if (typeInt == Telephony.Sms.MESSAGE_TYPE_OUTBOX)
//                    type = "outbox";
//
//                map.put("date", smsDate);
//                map.put("formattedDate", String.valueOf(dateFormat));
//                map.put("number", number);
//                map.put("body", body);
//                map.put("type", type);
//
//                entries.add(map);
//            }
//            Log.d("", entries.toString());
//        } catch (Exception e) {
//            Log.d("", e.toString());
//        }
//
//    }
//
//    // Get Phone number
//    private String getYourPhoneNumber(Context ctx) {
//        try {
//            TelephonyManager tMgr = ContextCompat.getSystemService(ctx, TelephonyManager.class);
//
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return "TODO";
//            }
//            String getSimNumber = tMgr.getLine1Number();
//            String simDisplayName = tMgr.getNetworkOperatorName();
//
//            Log.d("", getSimNumber + ": " + simDisplayName);
//            return getSimNumber + ": " + simDisplayName;
//        } catch (Exception e) {
//            Log.d("", e.toString());
//        }
//        return "";
//    }
//
//    // Get LongLat
//    public void getDeviceLongLat(Context ctx) {
//        try {
//            final LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
//            final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            Criteria criteria = new Criteria();
//            if (gpsEnabled) {
//                criteria.setAccuracy(Criteria.ACCURACY_FINE);
//                criteria.setPowerRequirement(Criteria.POWER_HIGH);
//                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
//                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
//                // criteria.setAltitudeRequired(false);
//                // criteria.setSpeedRequired(false);
//                // criteria.setCostAllowed(true);
//                // criteria.setBearingRequired(false);
//
//                String provider = locationManager.getBestProvider(criteria, false);
//
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                locationManager.requestLocationUpdates(10000, 10, criteria, this, null);
//                Location location = locationManager.getLastKnownLocation(provider);
//
//                // Initialize the location fields
//                if (location != null) {
//                    onLocationChanged(location);
//                }
//
//            }
//
//        } catch (Exception e) {
//            Log.d("error", e.toString());
//        }
//    }
//
//    private JSONObject handleStringToJson(String string) {
//        try {
//            JSONObject jsonObject = new JSONObject(string);
//            return jsonObject;
//        } catch (JSONException err) {
//            Log.d("Error", err.toString());
//            return null;
//        }
//    }
//
//    public void onLocationChanged(Location location) {
//        // Called when a new location is found by the network location provider.
//        final String TAG = "LocationChanged";
//        try {
//            SharedPreferences sharedpreferences = ctx.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
//            String getLocationSharedPref = sharedpreferences.getString(locationKey, null);
//            SharedPreferences.Editor editor = sharedpreferences.edit();
//            JSONObject objAllLocation = new JSONObject();
//            JSONObject objLocation = new JSONObject();
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//            List<Address> addresses;
//            Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//
//            // Using geocoder to determine the address by coordinate
//            addresses = geocoder.getFromLocation(latitude, longitude, 1);
//
//            // write to json
//            objLocation.put("latitude", latitude);
//            objLocation.put("longitude", longitude);
//            objLocation.put("address", addresses.get(0).getAddressLine(0).toString());
//            objLocation.put("timestamp", timestamp.toString());
//
//            // Log.d("Message: ",
//            // "Location changed, " + location.getAccuracy() + " , " +
//            // location.getLatitude() + ","
//            // + location.getLongitude());
//
//            JSONObject jsonObject = handleStringToJson(objLocation.toString());
//            objLocation.put("location", jsonObject);
//
//            // try {
//            // JSONObject jsonObject = new JSONObject(objLocation.toString());
//            // objLocation.put("location", jsonObject);
//            // } catch (JSONException err) {
//            // Log.d("Error", err.toString());
//            // }
//
//            if (getLocationSharedPref == null) {
//                try {
//                    JSONArray jsonArray = new JSONArray();
//                    JSONObject jsonObjectLocation = new JSONObject(objLocation.getString("location"));
//                    jsonArray.put(jsonObjectLocation);
//                    objAllLocation.put("allLocation", jsonArray);
//                } catch (JSONException err) {
//                    Log.d("Error", err.toString());
//                }
//                editor.putString(locationKey, objAllLocation.toString());
//            } else {
//                try {
//                    JSONObject jsonObjectSharedPref = new JSONObject(getLocationSharedPref);
//                    JSONArray resultArray = jsonObjectSharedPref.getJSONArray("allLocation");
//                    JSONObject locationObject = new JSONObject(objLocation.getString("location"));
//                    resultArray.put(locationObject);
//                    String totalLocationVisited = String.valueOf(resultArray.length() + 1);
//                    objAllLocation.put("allLocation", resultArray);
//                    objAllLocation.put("totalLocation", totalLocationVisited);
//                } catch (JSONException err) {
//                    Log.d("Error", err.toString());
//                }
//                editor.putString(locationKey, objAllLocation.toString());
//            }
//            editor.commit();
//            Log.d(TAG, getLocationSharedPref);
//        } catch (Exception e) {
//            Log.d(TAG, e.toString());
//        }
//    }
//
//}
