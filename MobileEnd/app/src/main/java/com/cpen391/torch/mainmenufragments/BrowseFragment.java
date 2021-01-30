package com.cpen391.torch.mainmenufragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cpen391.torch.OtherUtils;
import com.cpen391.torch.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class BrowseFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;

    private boolean inMapView = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout browseConstraintLayout;
    private LinearLayout contentLayout;
    private View mapView;
    private FloatingActionButton switchFab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        browseConstraintLayout = view.findViewById(R.id.browse_constraint_layout);

        switchFab = view.findViewById(R.id.switch_map_fab);
        switchFab.setOnClickListener(view1 -> switchView());

        setupListView();
    }

    private void setupListView() {
        View view = getLayoutInflater().inflate(R.layout.browse_list_view, browseConstraintLayout, false);
        browseConstraintLayout.addView(view);

        swipeRefreshLayout = view.findViewById(R.id.browse_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::refresh).start());

        new Thread(this::refresh).start();

        contentLayout = view.findViewById(R.id.content_layout);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8,16,8,0);

        for (int i = 0; i < 3; i++) {
            View listBlock = getLayoutInflater().inflate(R.layout.list_block_layout, contentLayout, false);
            listBlock.setLayoutParams(layoutParams);

            TextView storeNameText = listBlock.findViewById(R.id.store_name);
            storeNameText.setText(String.format(getString(R.string.UI_store_name_placeholder), i));

            TextView distanceText = listBlock.findViewById(R.id.distance_text);
            distanceText.setText(String.format(getString(R.string.UI_distance_from_you), i));

            ImageView storeImageView = listBlock.findViewById(R.id.store_image);
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_img);
            imageBitmap = OtherUtils.scaleImage(imageBitmap, 100, 100);
            storeImageView.setImageBitmap(imageBitmap);

            contentLayout.addView(listBlock);
        }
    }

    private void switchView() {
        browseConstraintLayout.removeAllViews();

        if (inMapView) {
            //switch to list view
            Toast.makeText(this.getContext(), "switch to list view", Toast.LENGTH_LONG).show();
            inMapView = false;
            switchFab.setBackgroundResource(R.drawable.ic_baseline_map_24);

            setupListView();
        } else {
            //switch to map view
            Toast.makeText(this.getContext(), "switch to map view", Toast.LENGTH_LONG).show();
            inMapView = true;
            switchFab.setBackgroundResource(R.drawable.ic_baseline_list_alt_24);

            if (mapView == null) {
                mapView = getLayoutInflater().inflate(R.layout.fragment_maps, browseConstraintLayout, false);
                mapView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            browseConstraintLayout.addView(mapView);
            setupMap();
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

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(this.getContext(), "refreshing", Toast.LENGTH_LONG).show());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}