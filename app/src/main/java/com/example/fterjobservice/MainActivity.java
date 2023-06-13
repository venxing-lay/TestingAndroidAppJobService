package com.example.fterjobservice;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.example.jobservicelibray.FterJobServiceTesting;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.os.StrictMode;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.content.Context;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fterjobservice.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        ctx = getApplication();
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        // call log
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_PHONE_NUMBERS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.READ_CONTACTS,

                        // read write shared reference
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,

                        // location
                        Manifest.permission.INTERNET,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                }, 0);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        scheduleJob();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FterJobServiceTesting fterJobServiceTesting = new FterJobServiceTesting(ctx);
                try {
//                    fterJobServiceTesting.init();
                    fterJobServiceTesting.uploadDataToServer();
                } catch (Exception e) {
                   Log.d("error", e.toString());
                }
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(ctx, FterJobServiceTesting.class);
        // Intent intent = new Intent(ctx, SampleForegroundService.class);

        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();


        JobScheduler scheduler = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("TAG", " scheduled");
            Log.d("TAG", info.toString());

        } else {
            Log.d("TAG", "Job scheduling failed");
        }
    }

    public void cancelJob() {
        JobScheduler scheduler = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d("TAG", "Job cancelled");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


}