package com.example.codeshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class CodeActivity extends AppCompatActivity {

    private RequestQueue mRequestQueue;
    private ImageView codeView;
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        Intent i = getIntent();

        data = i.getStringExtra("Data");

        mRequestQueue =  Volley.newRequestQueue(this);

        codeView = findViewById(R.id.codeView);

        Bitmap bitmap = QRCodeHelper.newInstance(this)
                .setContent(data)
                .setWidthAndHeight(300,300)
                .getQRCOde();

        codeView.setImageBitmap(bitmap);


    }

    public void getData(){
        String url = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="+data;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
