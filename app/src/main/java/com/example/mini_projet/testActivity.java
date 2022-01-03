package com.example.mini_projet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class testActivity extends AppCompatActivity {
    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        text = findViewById(R.id.textView);
        Intent i = getIntent();
        text.setText("id = "+i.getStringExtra("id")
                        +"\n email = "+i.getStringExtra("email")
                        +"\n pwd = "+i.getStringExtra("pwd"));
    }
}