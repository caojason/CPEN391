package com.cpen391.torch.mainmenufragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cpen391.torch.DetailsActivity;
import com.cpen391.torch.OtherUtils;
import com.cpen391.torch.R;
import com.cpen391.torch.data.StoreInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BrowseFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;

    private boolean inMapView = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout browseConstraintLayout;
    private LinearLayout contentLayout;
    private LinearLayout.LayoutParams layoutParams;
    private View mapView;
    private FloatingActionButton switchFab;
    private List<StoreInfo> storeInfoList;
    private Map<String, StoreInfo> storeInfoMap;
    private static final int PERMISSION_CODE = 15;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        browseConstraintLayout = view.findViewById(R.id.browse_constraint_layout);

        layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8,16,8,0);

        switchFab = view.findViewById(R.id.switch_map_fab);
        switchFab.setOnClickListener(view1 -> switchView());

        setupStoreInfoList();
        setupListView();
    }

    private void setupStoreInfoList() {
        storeInfoList = new ArrayList<>();
        storeInfoMap = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_img);
            imageBitmap = OtherUtils.scaleImage(imageBitmap, 100, 100);
            String encodedString = OtherUtils.encodeImage(imageBitmap);
            String storeName = String.format(getString(R.string.UI_store_name_placeholder), String.valueOf(i));
            StoreInfo storeInfo = new StoreInfo(
                    storeName,
                    "-1",
                    -1,
                    -1,
                    "FF:FF:FF:FF:FF:FF",
                    encodedString,
                    false
            );
            storeInfoList.add(storeInfo);
            storeInfoMap.put(storeName, storeInfo);
        }
    }

    private void setupListView() {
        View view = getLayoutInflater().inflate(R.layout.browse_list_view, browseConstraintLayout, false);
        browseConstraintLayout.addView(view);

        swipeRefreshLayout = view.findViewById(R.id.browse_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::refresh).start());

        contentLayout = view.findViewById(R.id.content_layout);

        for (int i = 0; i < storeInfoList.size(); i++) {
            StoreInfo storeInfo = storeInfoList.get(i);
            View listBlock = getLayoutInflater().inflate(R.layout.list_block_layout, contentLayout, false);
            listBlock.setLayoutParams(layoutParams);

            TextView storeNameText = listBlock.findViewById(R.id.store_name);
            storeNameText.setText(storeInfo.getStoreName());

            String distanceString = calculateDistance(storeInfo.getLongitude(), storeInfo.getLatitude());
            TextView distanceText = listBlock.findViewById(R.id.distance_text);
            distanceText.setText(String.format(getString(R.string.UI_distance_from_you), distanceString));

            ImageView storeImageView = listBlock.findViewById(R.id.store_image);
            storeImageView.setImageBitmap(OtherUtils.decodeImage(storeInfo.getEncodedLogo()));

            Button button = listBlock.findViewById(R.id.details_button);
            button.setOnClickListener(view1 -> enterDetails(storeInfo.toJson(), distanceString));

            contentLayout.addView(listBlock);
        }
    }

    private void switchView() {
        browseConstraintLayout.removeAllViews();

        if (inMapView) {
            //switch to list view
            Toast.makeText(this.getContext(), "switch to list view", Toast.LENGTH_LONG).show();
            inMapView = false;
            switchFab.setImageResource(R.drawable.ic_baseline_map_24);

            setupListView();
        } else {
            //switch to map view
            Toast.makeText(this.getContext(), "switch to map view", Toast.LENGTH_LONG).show();
            inMapView = true;
            switchFab.setImageResource(R.drawable.ic_baseline_list_alt_24);

            if (mapView == null) {
                mapView = getLayoutInflater().inflate(R.layout.fragment_maps, browseConstraintLayout, false);
                mapView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            browseConstraintLayout.addView(mapView);
            setupMap();
        }
    }

    private String calculateDistance(double longitude, double latitude) {
        double distance = 0;
        LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity())
                .getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(this.getContext(), R.string.UI_distance_notification, Toast.LENGTH_SHORT).show();
        } else {
            if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(this.getContext()), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this.getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_CODE);
                return "";
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            float[] results = new float[1];
            if (location != null && longitude != -1 && latitude != -1) {
                Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), results);
                distance = results[0];
            }
        }

        int val = (int)distance;
        String str = val + "";
        if (str.length() > 3) {
            distance = (double)val / 1000.0;
            return String.format(getString(R.string.km_distance), distance);
        } else {
            return String.format(getString(R.string.meter_distance), distance);
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void refresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        //TODO: read from server
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> contentLayout.removeAllViews());
        for (int i = 0; i < storeInfoList.size(); i++) {
            StoreInfo storeInfo = storeInfoList.get(i);
            View listBlock = getLayoutInflater().inflate(R.layout.list_block_layout, contentLayout, false);
            listBlock.setLayoutParams(layoutParams);

            TextView storeNameText = listBlock.findViewById(R.id.store_name);
            storeNameText.setText(storeInfo.getStoreName());

            String distanceString = calculateDistance(storeInfo.getLongitude(), storeInfo.getLatitude());
            TextView distanceText = listBlock.findViewById(R.id.distance_text);
            distanceText.setText(String.format(getString(R.string.UI_distance_from_you), distanceString));

            ImageView storeImageView = listBlock.findViewById(R.id.store_image);
            storeImageView.setImageBitmap(OtherUtils.decodeImage(storeInfo.getEncodedLogo()));

            Button button = listBlock.findViewById(R.id.details_button);
            button.setOnClickListener(view1 -> enterDetails(storeInfo.toJson(), distanceString));

            Objects.requireNonNull(getActivity()).runOnUiThread(() -> contentLayout.addView(listBlock));
        }
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(this.getContext(), "refreshing", Toast.LENGTH_LONG).show());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (storeInfoList.size() == 0) {
            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        } else {
            LatLng pos = new LatLng(storeInfoList.get(0).getLatitude(), storeInfoList.get(0).getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12.0f));
            for (StoreInfo storeInfo : storeInfoList) {
                pos = new LatLng(storeInfo.getLatitude(), storeInfo.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pos).title(storeInfo.getStoreName()));
                mMap.setOnInfoWindowClickListener(this::onMapInfoWindowClick);
            }
        }
    }

    public void onMapInfoWindowClick(Marker marker) {
        String title = marker.getTitle();
        StoreInfo storeInfo = storeInfoMap.get(title);
        String storeInfoString = storeInfo == null ? "" : storeInfo.toJson();
        new AlertDialog.Builder(Objects.requireNonNull(this.getContext()))
                .setTitle(R.string.UI_warning)
                .setMessage(R.string.UI_entering_details)
                .setPositiveButton(R.string.OK, ((dialogInterface, i) -> {
                    enterDetails(storeInfoString, calculateDistance(marker.getPosition().longitude, marker.getPosition().latitude));
                    dialogInterface.dismiss();
                }))
                .setNegativeButton(R.string.CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void enterDetails(String storeInfo, String distance) {
        Intent i = new Intent(getActivity(), DetailsActivity.class);
        i.putExtra(getString(R.string.STORE_INFO), storeInfo);
        i.putExtra(getString(R.string.Intent_distance_attribute), distance);
        startActivity(i);
    }
}