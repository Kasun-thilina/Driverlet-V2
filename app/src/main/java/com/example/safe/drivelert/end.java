package com.example.safe.drivelert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.safe.drivelert.Authentication.LoginActivity;

import java.text.DateFormat;
import java.util.Date;

public class end extends AppCompatActivity {
    Button proceed;
    TextView a,b,c,tvStatus;
    private String key ="facetrackeractivity";
    private String key_3 = "hello";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        proceed = (Button)findViewById(R.id.button2);
        a = (TextView)findViewById(R.id.textView10);
        b = (TextView)findViewById(R.id.textView11);
        tvStatus = findViewById(R.id.tvStatus);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent next = new Intent(end.this, LoginActivity.class);
                next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(next);
            }
        });
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String counter=pref.getString("key_driveTime", "0:0");
        String split[]=counter.split(":");

        int miniuts=Integer.parseInt(split[0]);
        int seconds=Integer.parseInt(split[1]);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        b.setText(currentDateTimeString);
        Intent intent2 = getIntent();
        String status = intent2.getStringExtra(key);
        String start = intent2.getStringExtra(key_3);
        String finalDisplay="Time you have driven the vehicle:"+counter;
        tvStatus.setText(finalDisplay);
        a.setText(start);



    }
}
