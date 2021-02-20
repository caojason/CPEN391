package com.cpen391.torch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LetterActivity extends AppCompatActivity {
    private Button submit;
    private EditText Subject,Email,RequestInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.letter);
        Subject=findViewById(R.id.Subject);
        Email=findViewById(R.id.editTextTextEmail);
        RequestInfo=findViewById(R.id.editTextTextMultiLine);
        submit=findViewById(R.id.button);
        Editable subject=Subject.getText();
        Editable email=Email.getText();
        Editable requestMsg=RequestInfo.getText();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidEmail(email)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + email.toString()));
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject.toString() );

                    intent.putExtra(Intent.EXTRA_TEXT, requestMsg.toString());
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Email can not be empty",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
