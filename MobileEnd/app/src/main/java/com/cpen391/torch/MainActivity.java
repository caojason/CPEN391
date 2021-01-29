package com.cpen391.torch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private Animation fromTop;
    private Animation toTop;
    private Button logOutButton;
    private boolean clicked;
    private SharedPreferences sp;

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

}