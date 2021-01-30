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
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.cpen391.torch.OtherUtils;
import com.cpen391.torch.R;

import java.util.Objects;

public class FavoriteFragment extends Fragment {

    private LinearLayout contentLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        contentLayout = view.findViewById(R.id.content_layout);

        swipeRefreshLayout = view.findViewById(R.id.favorite_refresh_layout);
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

    private void refresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                Toast.makeText(this.getContext(), "refreshing", Toast.LENGTH_LONG).show());
    }

}