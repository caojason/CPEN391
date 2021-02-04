package com.cpen391.torch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cpen391.torch.data.StoreInfo;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class StoreInfoActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private TextView storeNameInvalidTextView;
    private ImageButton storeLogoButton;

    private String macAddr = "";
    private String newStoreName = "";
    private double longitude = -1;
    private double latitude = -1;
    private Bitmap storeLogo = null;
    private static final int PERMISSION_CODE1 = 12;
    private static final int PICK_IMG = 13;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_info);

        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        macAddr = getIntent().getStringExtra(getString(R.string.MAC_ADDR));
        String previousStoreInfoString = getIntent().getStringExtra(getString(R.string.STORE_INFO));
        if (!OtherUtils.stringIsNullOrEmpty(previousStoreInfoString)) {
            Gson g = new Gson();
            StoreInfo storeInfo = g.fromJson(previousStoreInfoString, StoreInfo.class);
            macAddr = storeInfo.getMacAddr();
            newStoreName = storeInfo.getStoreName();
            latitude = storeInfo.getLatitude();
            longitude = storeInfo.getLongitude();
            storeLogo = OtherUtils.decodeImage(storeInfo.getEncodedLogo());
        }

        storeNameInvalidTextView = findViewById(R.id.store_name_invalid_text);

        EditText storeNameInput = findViewById(R.id.store_name_input);

        if (!OtherUtils.stringIsNullOrEmpty(newStoreName)) {
            storeNameInput.setText(newStoreName);
        }

        storeNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                newStoreName = editable.toString();
                if (!OtherUtils.checkStoreName(newStoreName)) {
                    storeNameInvalidTextView.setText(R.string.UI_store_name_invalid);
                    storeNameInvalidTextView.setTextColor(getResources().getColor(R.color.colorCrimson));
                } else {
                    storeNameInvalidTextView.setText("");
                }
            }
        });

        TextView locationTextView = findViewById(R.id.location_textView);

        Button getLocationButton = findViewById(R.id.get_location_button);
        getLocationButton.setOnClickListener(v1 -> getLocation(locationTextView));

        storeLogoButton = findViewById(R.id.store_logo_change_button);
        storeLogoButton.setOnClickListener(v1 -> setStoreLogo());

        Button finishButton = findViewById(R.id.finish_store_info_editing_button);
        finishButton.setOnClickListener(v -> finishEditing());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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


    private void getLocation(TextView textView) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(this, R.string.UI_gps_starting_notification, Toast.LENGTH_SHORT).show();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_CODE1);
            }
            Location location = findLocation();
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                textView.setText(String.format(getString(R.string.UI_location_val), longitude, latitude));
            } else {
                textView.setText("Cannot get location, please try again later");
            }
        }
    }

    private Location findLocation() {
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_CODE1);
            }
            locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                locationManager.removeUpdates(locationListener);
                return location;
            }
        }
        return null;
    }

    private void setStoreLogo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ||ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PICK_IMG);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_IMG);
        }
    }

    private void finishEditing() {
        if (OtherUtils.stringIsNullOrEmpty(newStoreName)) {
            new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                    .setMessage(R.string.UI_store_name_invalid)
                    .setPositiveButton(R.string.OK, (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
            return;
        }

        String userId = sp.getString(getString(R.string.UID), "");

        StoreInfo storeInfo = new StoreInfo(newStoreName, userId, latitude, longitude, macAddr, OtherUtils.encodeImage(storeLogo));
        updateFavoriteList(storeInfo);

        goBackToHome();
    }

    private void updateFavoriteList(StoreInfo newInfo) {
        String previousList = sp.getString(getString(R.string.FAVORITES), "");
        String updatedJson = "";
        if (OtherUtils.stringIsNullOrEmpty(previousList)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(newInfo.toJson());
            updatedJson = jsonArray.toString();
            sp.edit().putString(getString(R.string.FAVORITES), updatedJson).apply();
        } else {
            try {
                JSONArray jsonArray = new JSONArray(previousList);
                jsonArray.put(newInfo);
                updatedJson = jsonArray.toString();
                sp.edit().putString(getString(R.string.FAVORITES), updatedJson).apply();
            } catch (Exception e) {
                Log.d("D", "malformed json");
            }
        }
        //upload the new json to server
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMG && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                storeLogo = OtherUtils.scaleImage(bitmap, 100, 100);
                storeLogoButton.setImageBitmap(storeLogo);
            } catch (IOException e) {
                Log.d("Image_error", "bit map conversion error");
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                .setMessage(R.string.UI_store_info_edit_warning)
                .setPositiveButton(R.string.YES, (dialogInterface, i) -> goBackToHome())
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    public void goBackToHome() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
                locationManager = null;
            } catch (Exception e) {
                Log.d("D", "remove location listener failed");
            }
        }
        finishAndRemoveTask();
    }

    @Override
    public void onPause() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception e) {
                Log.d("D", "remove location listener failed");
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception e) {
                Log.d("D", "remove location listener failed");
            }
        }
        super.onDestroy();
    }
}