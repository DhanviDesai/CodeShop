package com.example.codeshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Cart extends AppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        tv = findViewById(R.id.resultText);

        Intent i = getIntent();
        String value = i.getStringExtra("value");

        tv.setText(value);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
