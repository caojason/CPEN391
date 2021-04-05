package com.cpen391.torch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

public class LetterActivity extends AppCompatActivity {

    private String email;
    private String subject;
    private String message;
    private String macAddr = "";
    private String ownerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.letter_layout);
        EditText subjectEditText = findViewById(R.id.editTextSubject);
        EditText emailEditText = findViewById(R.id.editTextEmail);
        EditText requestInfoEditText = findViewById(R.id.editTextMessage);
        Button submitButton = findViewById(R.id.submitEmailButton);
        editTextListenerSetup(subjectEditText, emailEditText, requestInfoEditText);

        submitButton.setOnClickListener(v -> onSubmitClicked());

        macAddr = getIntent().getStringExtra(getString(R.string.MAC_ADDR));
        ownerId = getIntent().getStringExtra(getString(R.string.OWNER_ID));
    }

    private void editTextListenerSetup(EditText subjectEditText, EditText emailEditText, EditText requestInfoEditText) {
        subjectEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                subject = editable.toString();
            }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                email = editable.toString();
            }
        });

        requestInfoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                message = editable.toString();
            }
        });
    }

    private void onSubmitClicked() {
        if (!checkSubmissionValid()) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.UI_warning)
                .setMessage("Are you sure you want to submit?")
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(R.string.YES, (dialogInterface, i) -> {parseInfo();})
                .show();
    }


    private boolean checkSubmissionValid() {
        if (OtherUtils.stringIsNullOrEmpty(subject)) {
            Toast.makeText(this, R.string.UI_fill_subject, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (OtherUtils.stringIsNullOrEmpty(message)) {
            Toast.makeText(this, R.string.UI_fill_justification, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!OtherUtils.checkValidEmail(email)) {
            Toast.makeText(this, R.string.UI_email_invalid, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void parseInfo() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("macAddr", macAddr);
        jsonObject.addProperty("ownerId", ownerId);
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("subject", subject);
        jsonObject.addProperty("message", message);

        String dataToSend = jsonObject.toString();

        SharedPreferences sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        String uid = sp.getString(getString(R.string.UID), "");
        new Thread(()-> {
            OtherUtils.uploadToServer(getString(R.string.create_email), uid, dataToSend);
        }).start();
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                .setMessage(R.string.UI_request_letter_edit_warning)
                .setPositiveButton(R.string.YES, (dialogInterface, i) -> super.onBackPressed())
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }
}
