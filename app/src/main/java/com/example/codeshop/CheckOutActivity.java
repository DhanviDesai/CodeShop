package com.example.codeshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.codeshop.model.ProductModel;
import com.google.gson.Gson;

import java.util.ArrayList;

public class CheckOutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView total,proceedPay;
    private ProductAdapter adapter;
    private int totalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        init();

        ArrayList<ProductModel> products = MainActivity.products;

        adapter = new ProductAdapter(this,products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        for(ProductModel product: products){
            totalCount += Integer.parseInt(product.getProdPrice())*product.getQuantity();
        }

        total.setText("\u20b9 "+totalCount);

        proceedPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = new Gson().toJson(products);
                Intent i = new Intent(CheckOutActivity.this,CodeActivity.class);
                i.putExtra("Data",data);
                startActivity(i);
            }
        });


    }

    public void init(){
        recyclerView = findViewById(R.id.checkoutRecycler);
        total = findViewById(R.id.textView3);
        proceedPay = findViewById(R.id.textView4);
        totalCount = 0;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
