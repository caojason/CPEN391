package com.cpen391.torch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        String storeName = getIntent().getStringExtra(getString(R.string.Intent_storeName_attribute));
        int distance = getIntent().getIntExtra(getString(R.string.Intent_distance_attribute), 0);

        TextView storeNameText = findViewById(R.id.details_view_store_name);
        storeNameText.setText(String.format(getString(R.string.UI_store_name_placeholder), storeName));

        TextView distanceText = findViewById(R.id.details_view_distance_text);
        distanceText.setText(String.format(getString(R.string.UI_distance_from_you), distance));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.details_view_map_fragment);
        mapFragment.getMapAsync(this);

        Button addToFavoriteButton = findViewById(R.id.add_to_favorite_button);
        addToFavoriteButton.setOnClickListener(v -> addToFavorite());

        LinearLayout switchDayLayout = findViewById(R.id.switch_day_linear_layout);
        for (int i = 0; i < switchDayLayout.getChildCount(); i++) {
            if (!(switchDayLayout.getChildAt(i) instanceof TextView)) continue;
            TextView btn= (TextView)switchDayLayout.getChildAt(i);
            String day = btn.getText().toString();
            btn.setOnClickListener(v -> switchDay(day));
        }
        setupChart(getString(R.string.UI_sunday));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        ScrollView parentScrollView = findViewById(R.id.details_scroll_view);
        mMap.setOnCameraMoveListener(() -> parentScrollView.requestDisallowInterceptTouchEvent(true));
        mMap.setOnCameraIdleListener(() -> parentScrollView.requestDisallowInterceptTouchEvent(false));
    }

    private void addToFavorite() {
        Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show();
    }

    private void switchDay(String day) {
        Toast.makeText(this, String.format("Swtiched to %s", day), Toast.LENGTH_SHORT).show();
        //setupChart(day);
    }

    private void setupChart(String day) {
        AnyChartView chartView = findViewById(R.id.chart_view);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        chartView.setProgressBar(progressBar);
        Cartesian cartesian = AnyChart.column();

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
        cartesian.title(String.format("Volume on %s", day));

        cartesian.yScale().minimum(0d);
        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title(getString(R.string.UI_Time_in_day));
        cartesian.yAxis(0).title(getString(R.string.UI_Customer_counts));

        chartView.setChart(cartesian);
    }
}