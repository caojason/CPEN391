package com.cpen391.torch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Animation fromTop;
    private Animation toTop;
    private Button logOutButton;
    private boolean clicked;
    private SharedPreferences sp;
    private final static int PERMISSION_CODE = 11;
    private final static int GPS_PERMISSION = 14;

    private LocationListener locationListener;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_favorite, R.id.navigation_pair, R.id.navigation_browse)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        fromTop = AnimationUtils.loadAnimation(this, R.anim.from_top_anim);
        toTop = AnimationUtils.loadAnimation(this, R.anim.to_top_anim);

        FloatingActionButton menuFab = findViewById(R.id.menu_fab);
        menuFab.setOnClickListener(view -> onMenuFabClicked());

        logOutButton = findViewById(R.id.log_out_btn);
        logOutButton.setVisibility(View.INVISIBLE);
        logOutButton.setOnClickListener(view -> checkForLogout());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        PERMISSION_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_CODE);
        }

        setupLocationManager();
        requestGPSUpdates();
    }

    private void setupLocationManager() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                locationManager.removeUpdates(locationListener);
            }
        };
    }

    private void requestGPSUpdates() {
        if (locationManager == null) {
            setupLocationManager();
        }
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        GPS_PERMISSION);
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
        }
    }

    private void onMenuFabClicked() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked) {
            logOutButton.setVisibility(View.VISIBLE);
            logOutButton.setClickable(true);
        } else {
            logOutButton.setVisibility(View.INVISIBLE);
            logOutButton.setClickable(false);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {
            logOutButton.startAnimation(fromTop);
        } else {
            logOutButton.startAnimation(toTop);
        }
    }

    private void checkForLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.UI_warning)
                .setMessage(R.string.UI_logout_msg)
                .setPositiveButton(R.string.YES, ((dialogInterface, i) -> {dialogInterface.dismiss(); logOut();}))
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void logOut() {
        sp.edit().clear().apply();

        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        ActivityCompat.finishAffinity(this);
    }

    @Override
    protected void onPause() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e) {
            Log.d("D", "remove location listener failed");
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e) {
            Log.d("D", "remove location listener failed");
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestGPSUpdates();
    }
}