package com.ajithpoison.lovequotes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.ajithpoison.IOnBackPressed;
import com.ajithpoison.lovequotes.BroadcastReceiver.AlarmReceiver;
import com.google.android.gms.ads.MobileAds;
import com.kobakei.ratethisapp.RateThisApp;

import java.io.*;
import java.util.Calendar;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private Toast backToast;
    private NavigationView navigationView;
    boolean doubleBackToExitPressedOnce = false;
    FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        MobileAds.initialize(this, "ca-app-pub-8453607245436940~8868276246");
        RateThisApp.Config config = new RateThisApp.Config(1, 2);
        RateThisApp.init(config);
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // toolbar fancy stuff
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.toolbar_title);
        }
        drawer = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.NavigationView);
        navigationView.setNavigationItemSelectedListener(this);

        registerAlarm();

        this.getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        Fragment current = getCurrentFragment();
                        if (current instanceof QuotesFragment) {
                            navigationView.setCheckedItem(R.id.nav_quotes);
                        } else if (current instanceof ContactFragment) {
                            navigationView.setCheckedItem(R.id.nav_contact);
                        } else if (current instanceof FavoritesFragment) {
                            navigationView.setCheckedItem(R.id.nav_favorites);
                        } else {
                            navigationView.setCheckedItem(R.id.nav_quotes);
                        }
                    }
                });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new QuotesFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_quotes);
        }

        //get context by calling "this" in activity or getActivity() in fragment
        String appDataPath = this.getApplicationInfo().dataDir;

        File dbFolder = new File(appDataPath + "/databases");//Make sure the /databases folder exists

        boolean isCreated = dbFolder.mkdir();//This can be called multiple times.
        if (isCreated) {
            Log.d("Directory Creation: ", "Successful");
        } else {
            Log.d("Directory Creation: ", "Failed");
        }

        File dbFilePath = new File(appDataPath + "/databases/quotesDB.db");

        try {
            InputStream inputStream = this.getAssets().open("quotesDB.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            //handle
        }

    }

    public void registerAlarm() {
        Intent intent = new Intent(Home.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Home.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        try {
            AlarmManager manager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            if (manager != null) {

                    manager.setWindow(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mShare:
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=com.ajithpoison.lovequotes";
                String shareSub = "Your Subject here";
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(myIntent, "Share \"Love Quotes App\" via"));
                break;
            case R.id.mRate:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + "com.ajithpoison.lovequotes")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + "com.ajithpoison.lovequotes")));
                }
                break;
        }
        return true;
    }

    public Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_quotes:
                QuotesFragment quotesFrag = new QuotesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, quotesFrag, "QuotesFragment")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.nav_contact:
                ContactFragment contactFrag = new ContactFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, contactFrag, "ContactFragment")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.nav_favorites:
                FavoritesFragment favoritesFrag = new FavoritesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, favoritesFrag, "FavoritesFragment")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.nav_exit:
                finish();
                System.exit(0);
                break;
            case R.id.nav_share:
                Intent myIntent1 = new Intent(Intent.ACTION_SEND);
                myIntent1.setType("text/plain");
                String shareBody1 = "https://play.google.com/store/apps/details?id=com.ajithpoison.lovequotes";
                String shareSub1 = "Your Subject here";
                myIntent1.putExtra(Intent.EXTRA_SUBJECT, shareSub1);
                myIntent1.putExtra(Intent.EXTRA_TEXT, shareBody1);
                startActivity(Intent.createChooser(myIntent1, "Share \"Love Quotes App\" via"));
                break;
            case R.id.nav_rate:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + "com.ajithpoison.lovequotes")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + "com.ajithpoison.lovequotes")));
                }
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else if (!doubleBackToExitPressedOnce) {
                this.doubleBackToExitPressedOnce = true;
                backToast = Toast.makeText(this, "Tap back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                backToast.cancel();
                super.onBackPressed();
            }
        }
    }
}
