package com.example.barandqrcodescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class GetStartedActivity extends AppCompatActivity {
    ImageView get_started_btn;
    ProgressBar pb;
    LinearLayout getStarted_Layout,loading_Layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        get_started_btn=findViewById(R.id.getstarted_btn);
        pb=findViewById(R.id.loading);
        getStarted_Layout=findViewById(R.id.getstarted_layout);
        loading_Layout=findViewById(R.id.loading_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loading_Layout.setVisibility(View.GONE);
                getStarted_Layout.setVisibility(View.VISIBLE);
            }
        }, 0);


        get_started_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it=new Intent(GetStartedActivity.this,MainActivity.class);
                startActivity(it);
            }
        });


    }
}