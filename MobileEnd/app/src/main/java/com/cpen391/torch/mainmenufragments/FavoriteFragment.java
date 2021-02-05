package com.cpen391.torch.mainmenufragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.cpen391.torch.DetailsActivity;
import com.cpen391.torch.OtherUtils;
import com.cpen391.torch.R;
import com.cpen391.torch.data.StoreInfo;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class FavoriteFragment extends Fragment {

    private String userId;
    private LinearLayout contentLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences sp;
    private static final int PERMISSION_CODE1 = 13;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        sp = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);

        String favoriteList = sp.getString(getString(R.string.FAVORITES), "");
        userId = sp.getString(getString(R.string.UID), "");

        contentLayout = view.findViewById(R.id.content_layout);

        swipeRefreshLayout = view.findViewById(R.id.favorite_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::refresh).start());

        new Thread(this::refresh).start();

        contentLayout = view.findViewById(R.id.content_layout);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8,16,8,0);

        if (!OtherUtils.stringIsNullOrEmpty(favoriteList)) {
            setupFavoriteList(favoriteList);
        }

        setupFavoriteList(favoriteList);
    }


    private void refresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
        {
            Toast.makeText(this.getContext(), "refreshing", Toast.LENGTH_LONG).show();

            //get data from server
            String favoriteList = sp.getString(getString(R.string.FAVORITES), "");
            if (!OtherUtils.stringIsNullOrEmpty(favoriteList)) {
                setupFavoriteList(favoriteList);
            }
        });
    }

    private void setupFavoriteList(String favoriteList) {
        contentLayout.removeAllViews();

        try {
            JSONArray jsonArray = new JSONArray(favoriteList);
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonString = jsonArray.get(i).toString();
                Gson g = new Gson();
                StoreInfo storeInfo = g.fromJson(jsonString, StoreInfo.class);
                setupEachBlock(storeInfo);
            }
        } catch (Exception e) {
            Log.d("D", "parse json failed");
        }
    }

    private void setupEachBlock(StoreInfo storeInfo) {
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8,16,8,0);

        View listBlock = getLayoutInflater().inflate(R.layout.list_block_layout, contentLayout, false);
        listBlock.setLayoutParams(layoutParams);

        TextView storeNameText = listBlock.findViewById(R.id.store_name);
        storeNameText.setText(storeInfo.getStoreName());

        if (storeInfo.getStoreOwnerId().equals(userId)) {
            storeNameText.setText(String.format(getString(R.string.UI_your_store_name), storeInfo.getStoreName()));
            storeNameText.setTextColor(getResources().getColor(R.color.colorGolden));
        }

        TextView distanceText = listBlock.findViewById(R.id.distance_text);

        String distanceString = calculateDistance(storeInfo.getLongitude(), storeInfo.getLatitude());
        distanceText.setText(String.format(getString(R.string.UI_distance_from_you), distanceString));

        ImageView storeImageView = listBlock.findViewById(R.id.store_image);
        Bitmap imageBitmap = OtherUtils.decodeImage(storeInfo.getEncodedLogo());
        imageBitmap = OtherUtils.scaleImage(imageBitmap, 100, 100);
        storeImageView.setImageBitmap(imageBitmap);

        Button button = listBlock.findViewById(R.id.details_button);
        button.setOnClickListener(view1 -> enterDetails(storeInfo, distanceString));

        contentLayout.addView(listBlock);
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
                        PERMISSION_CODE1);
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

    private void enterDetails(StoreInfo storeInfo, String distanceString) {
        Intent i = new Intent(getActivity(), DetailsActivity.class);
        i.putExtra(getString(R.string.STORE_INFO), storeInfo.toJson());
        i.putExtra(getString(R.string.Intent_distance_attribute), distanceString);
        startActivity(i);
    }

}