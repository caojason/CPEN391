package com.cpen391.torch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 420;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        signInButton.setOnClickListener(view -> signIn());

        //use shared preference to record user login
        //so that a user does not need to login again on multiple use
        //also record all user-related info such as username, email and profile picture
        //may need to separate shared preference for different users
        //or we can just completely depend on the server to do the job
        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sp.getBoolean(getString(R.string.LOGGED), false)) {
            //the user has logged in before, and we have the credential
            //can be redirected to home screen directly.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ActivityCompat.finishAffinity(this);
            //completely stop this activity, so that user will not go back to this activity
            //by clicking the back button
        } else {
            //the user has logged out, we need to sign the user's google account out as well
            mGoogleSignInClient.signOut();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            validateAndLogin(account);
        } catch (ApiException e) {
            validateAndLogin(null);
        }
    }

    private void validateAndLogin(GoogleSignInAccount account) {
        if (account != null) {
            //use google ID as our default id
            sp.edit().putString(getString(R.string.UID), account.getId()).apply();

            if (!getFavoriteList(account.getId())) {
                new Thread(() ->
                        OtherUtils.uploadToServer(getString(R.string.create_user),
                                account.getId(),
                                account.getEmail()))
                        .start();
            }

            goToHomeActivity();
        } else {
            signIn();
        }
    }

    private boolean getFavoriteList(String uid) {
        String url = getString(R.string.BASE_URL) + getString(R.string.favorite_list_endpoint) + "?uid=" + uid;
        String favoriteList = OtherUtils.readFromURL(url);
        try {
            JSONArray jsonArray = new JSONArray(favoriteList);
            sp.edit().putString(getString(R.string.FAVORITES), jsonArray.getString(0)).apply();
        } catch (Exception e) {
            Log.d("D", "first parse of favorite list failed");
        }
        return !OtherUtils.stringIsNullOrEmpty(favoriteList);
    }

    private void goToHomeActivity() {
        sp.edit().putBoolean(getString(R.string.LOGGED), true).apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}