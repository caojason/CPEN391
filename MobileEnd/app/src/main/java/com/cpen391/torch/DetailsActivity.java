package com.cpen391.torch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.cpen391.torch.data.StoreInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private SharedPreferences sp;
    private static SharedPreferences.OnSharedPreferenceChangeListener onFavoriteChangedListener;

    private StoreInfo storeInfo;
    private Button addToFavoriteButton;
    private TextView storeNameText;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        onFavoriteChangedListener = (sp, key) -> onFavoriteChanged(key);
        sp.registerOnSharedPreferenceChangeListener(onFavoriteChangedListener);
        mRequestQueue = Volley.newRequestQueue(this);
        String distance = getIntent().getStringExtra(getString(R.string.Intent_distance_attribute));
        String storeInfoString = getIntent().getStringExtra(getString(R.string.STORE_INFO));
        Gson g = new Gson();
        storeInfo = g.fromJson(storeInfoString, StoreInfo.class);

        storeNameText = findViewById(R.id.details_view_store_name);
        storeNameText.setText(String.format(getString(R.string.UI_store_name_placeholder), storeInfo.getStoreName()));

        TextView distanceText = findViewById(R.id.details_view_distance_text);
        distanceText.setText(String.format(getString(R.string.UI_distance_from_you), distance));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.details_view_map_fragment);
        mapFragment.getMapAsync(this);

        TextView analysisPicExplainText = findViewById(R.id.analysis_pic_explain);
        ImageView analysisPic = findViewById(R.id.analysis_pic);
        Button requestPicButton = findViewById(R.id.request_pic_button);
        LinearLayout detailsLinearLayout = findViewById(R.id.details_linear_layout);

        if (!storeInfo.isHasPermission()) {
            analysisPicExplainText.setText(R.string.UI_no_permission);
            detailsLinearLayout.removeView(analysisPic);
            requestPicButton.setOnClickListener(v -> requestForPermission());
        } else {
            detailsLinearLayout.removeView(requestPicButton);
        }

        addToFavoriteButton = findViewById(R.id.add_to_favorite_button);
        setupFavoriteButton();

        LinearLayout switchDayLayout = findViewById(R.id.switch_day_linear_layout);

        String[] dates = getResources().getStringArray(R.array.dates);

        for (int i = 0; i < switchDayLayout.getChildCount(); i++) {
            if (!(switchDayLayout.getChildAt(i) instanceof TextView)) continue;
            TextView btn= (TextView)switchDayLayout.getChildAt(i);
            String day = dates[i];
            btn.setOnClickListener(v -> switchDay(day));

        }
        setupChart(dates[0]);
    }

    private void setupFavoriteButton() {
        String userId = sp.getString(getString(R.string.UID), "");
        if (userId.equals(storeInfo.getStoreOwnerId())) {
            //check if the user is the owner
            addToFavoriteButton.setText(R.string.UI_update_info);
            addToFavoriteButton.setOnClickListener(v -> updateInfo());
        } else if (checkInList()){
            //check if the store is already in user's favorite list
            addToFavoriteButton.setVisibility(View.INVISIBLE);
            addToFavoriteButton.setClickable(false);
        } else {
            //can add to favorite list
            addToFavoriteButton.setOnClickListener(v -> addToFavorite());
        }

    }

    private boolean checkInList() {
        String favoriteListStr = sp.getString(getString(R.string.FAVORITES), "");
        Gson g = new Gson();
        try {
            JSONArray favoriteList = new JSONArray(favoriteListStr);
            for (int i = 0; i < favoriteList.length(); i++) {
                String str = favoriteList.getString(i);
                StoreInfo currInfo = g.fromJson(str, StoreInfo.class);
                if (currInfo.getStoreName().equals(storeInfo.getStoreName()) && currInfo.getMacAddr().equals(storeInfo.getMacAddr())) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.d("D", "parse json error");
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (storeInfo.getLatitude() == -1 || storeInfo.getLongitude() == -1) {
            Toast.makeText(this, R.string.UI_no_loc, Toast.LENGTH_SHORT).show();
        }
        LatLng storeLoc = new LatLng(storeInfo.getLatitude(), storeInfo.getLongitude());
        mMap.addMarker(new MarkerOptions().position(storeLoc).title(storeInfo.getStoreName()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLoc, 12.0f));

        ScrollView parentScrollView = findViewById(R.id.details_scroll_view);
        mMap.setOnCameraMoveListener(() -> parentScrollView.requestDisallowInterceptTouchEvent(true));
        mMap.setOnCameraIdleListener(() -> parentScrollView.requestDisallowInterceptTouchEvent(false));
    }

    private void updateInfo() {
        Intent i = new Intent(this, StoreInfoActivity.class);
        i.putExtra(getString(R.string.MAC_ADDR), storeInfo.getMacAddr());
        i.putExtra(getString(R.string.STORE_INFO), storeInfo.toJson());
        startActivity(i);
    }

    private void addToFavorite() {
        Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show();
        String previousList = sp.getString(getString(R.string.FAVORITES), "");
        String updatedJson = "";
        if (OtherUtils.stringIsNullOrEmpty(previousList)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(storeInfo.toJson());
            updatedJson = jsonArray.toString();
            sp.edit().putString(getString(R.string.FAVORITES), updatedJson).apply();
        } else {
            try {
                JSONArray jsonArray = new JSONArray(previousList);
                jsonArray.put(storeInfo.toJson());
                updatedJson = jsonArray.toString();
                sp.edit().putString(getString(R.string.FAVORITES), updatedJson).apply();
            } catch (Exception e) {
                Log.d("D", "malformed json");
            }
        }
    }

    private void requestForPermission() {
        Toast.makeText(this, getString(R.string.UI_request_permission), Toast.LENGTH_SHORT).show();
        Intent requestPermissionIntent = new Intent(DetailsActivity.this, LetterActivity.class);
        startActivity(requestPermissionIntent);
    }

    private void switchDay(String day) {
        Toast.makeText(this, String.format("Swtiched to %s", day), Toast.LENGTH_SHORT).show();
        AnyChartView chartView = findViewById(R.id.chart_view);
        setupChart(day);
    }

    private void setupChart(String day) {
        //BUG: cannot switch day
        AnyChartView chartView = findViewById(R.id.chart_view);
        if(chartView.isActivated()) {
            chartView.clear();
        }
        APIlib.getInstance().setActiveAnyChartView(chartView);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        chartView.setProgressBar(progressBar);
        Cartesian cartesian = AnyChart.column();

        cartesian.title(String.format("Volume on %s", day));
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("08:00", 1));
        data.add(new ValueDataEntry("09:00", 2));
        data.add(new ValueDataEntry("10:00", 4));
        data.add(new ValueDataEntry("11:00", 10));
        data.add(new ValueDataEntry("12:00", 9));
        data.add(new ValueDataEntry("13:00", 8));
        data.add(new ValueDataEntry("14:00", 6));
        data.add(new ValueDataEntry("15:00", 2));
        data.add(new ValueDataEntry("16:00", 1));

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator: }");

        cartesian.animation(false);


        cartesian.yScale().minimum(0d);
        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title(getString(R.string.UI_Time_in_day));
        cartesian.yAxis(0).title(getString(R.string.UI_Customer_counts));

        chartView.setChart(cartesian);

        // Below is how we change the data
        final int delayMillis = 500;
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            public void run() {
// create new data List and populate it with values
                List<DataEntry> data = new ArrayList<>();
                data.add(new ValueDataEntry("08:00", 3));
                data.add(new ValueDataEntry("10:00", 2));

// apply the new List to the existing chart
                //cartesian.column(data);
                cartesian.title(String.format("Volume on %s", day));
                handler.postDelayed(this, delayMillis);
            }
        };
        handler.postDelayed(runnable, delayMillis);
    }

    private void onFavoriteChanged(String key) {
        if (key.equals(getString(R.string.FAVORITES))) {
            String favoriteListString = sp.getString(key, "");
            Gson g = new Gson();
            StoreInfo newInfo = null;
            try {
                JSONArray favoriteList = new JSONArray(favoriteListString);
                for (int i = 0; i < favoriteList.length(); i++) {
                    String str = favoriteList.getString(i);
                    newInfo = g.fromJson(str, StoreInfo.class);
                    if (newInfo == null) continue;
                    if (newInfo.getMacAddr().equals(storeInfo.getMacAddr())) break;
                }
            } catch (Exception e) {
                Log.d("D", "malformed list");
            }
            if (newInfo != null) {
                storeInfo = newInfo;
                storeNameText.setText(storeInfo.getStoreName());
                setupFavoriteButton();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (onFavoriteChangedListener != null) {
            sp.unregisterOnSharedPreferenceChangeListener(onFavoriteChangedListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onFavoriteChangedListener != null) {
            sp.registerOnSharedPreferenceChangeListener(onFavoriteChangedListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (onFavoriteChangedListener != null) {
            sp.unregisterOnSharedPreferenceChangeListener(onFavoriteChangedListener);
        }
    }
}