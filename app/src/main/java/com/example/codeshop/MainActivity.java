package com.example.codeshop;

//import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.codeshop.model.ProductModel;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 11111;

    public static ArrayList<ProductModel> products;


    private TextureView textureView;
    private FirebaseVisionBarcodeDetectorOptions options;
    private boolean isPermissionGranted = false;
    private RecyclerView recyclerView;
    private TextView checkout,instructions;
    private View divider;
    private LinearLayout heading;
    private Preview preview;
    private ImageAnalysis analysis;
    private RequestQueue mRequestQueue;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        products = new ArrayList<>();

        init();
        toggleVisibility();
        setCameraFeed();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,CheckOutActivity.class);
                startActivity(i);
            }
        });
    }

    private void init(){

        mRequestQueue =  Volley.newRequestQueue(this);


        PreviewConfig config = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .build();

         preview = new Preview(config);



        ImageAnalysisConfig config1 = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

         analysis = new ImageAnalysis(config1);


        textureView = findViewById(R.id.texture_view);

        recyclerView = findViewById(R.id.scannedRecycler);
        divider = findViewById(R.id.view);
        checkout = findViewById(R.id.checkout);
        instructions = findViewById(R.id.instructions);
        heading = findViewById(R.id.linearLayout);

        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_EAN_13, FirebaseVisionBarcode.FORMAT_QR_CODE,
                        FirebaseVisionBarcode.FORMAT_AZTEC, FirebaseVisionBarcode.FORMAT_EAN_8, FirebaseVisionBarcode.TYPE_ISBN)
                .build();
        adapter = new ProductAdapter(this,products);

    }

    private void toggleVisibility(){
        if(products.size()==0){
            recyclerView.setVisibility(GONE);
            divider.setVisibility(GONE);
            checkout.setVisibility(GONE);
            heading.setVisibility(GONE);
            instructions.setVisibility(VISIBLE);
        }
        else{
            recyclerView.setVisibility(VISIBLE);
            divider.setVisibility(VISIBLE);
            checkout.setVisibility(VISIBLE);
            heading.setVisibility(VISIBLE);
            instructions.setVisibility(GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted = true;
                setCameraFeed();
            }
        }
    }

    private ImageAnalysis.Analyzer analyzer = new ImageAnalysis.Analyzer() {
        @Override
        public void analyze(ImageProxy image, int rotationDegrees) {
            if (image == null || image.getImage() == null) {
                return;
            }

            Image im = image.getImage();
            int rotation = degreesToFirebaseRotation(rotationDegrees);
            FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromMediaImage(im, rotation);

            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();

            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(firebaseImage)
                    .addOnSuccessListener((firebaseVisionBarcodes) -> {
                        String displayValue = null;

                        if(firebaseVisionBarcodes.size()==1){
                            FirebaseVisionBarcode barcode = firebaseVisionBarcodes.get(0);
                            displayValue = barcode.getDisplayValue();
                            String rawValue = barcode.getRawValue();
                            Toast.makeText(MainActivity.this, ""+rawValue, Toast.LENGTH_SHORT).show();
                            setUpData(rawValue);

                            try {
                                detector.close();
                            } catch (IOException e) {
                                Log.e("Firebase","DetectorClose");
                                e.printStackTrace();
                            }
                            analysis.removeAnalyzer();
                        }
                    })
                    .addOnCanceledListener(() -> {

                    });


        }
    };

    public void setCameraFeed() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
                @Override
                public void onUpdated(Preview.PreviewOutput output) {
                    textureView.setSurfaceTexture(output.getSurfaceTexture());
                }
            });


            analysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR,analyzer);

            CameraX.unbindAll();

            CameraX.bindToLifecycle(this, analysis, preview);

        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        CameraX.unbindAll();
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
        setCameraFeed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraX.unbindAll();
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }


    }

    private void setUpData(String upc){
        String url = "https://api.upcitemdb.com/prod/trial/lookup?upc="+upc;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String code = response.getString("code");
                    if(code.equals("OK")) {
                        //Check for total and offset
                        int offset = response.getInt("offset");
                        int total = response.getInt("total");
                        if (total > 0) {
                            JSONArray items = response.getJSONArray("items");
                            for (int i = offset; i < total; i++) {
                                JSONObject currentObject = items.getJSONObject(i);
                                String title = currentObject.getString("title");
                                String description = currentObject.getString("description");
                                String brand = currentObject.getString("brand");
                                int price = currentObject.getInt("lowest_recorded_price");
                                JSONArray images = currentObject.getJSONArray("images");
                                String imageLink = images.getString(0);
                                ProductModel productModel = new ProductModel();
                                productModel.setProdName(title);
                                productModel.setProdDescritpion(description);
                                productModel.setProdBrand(brand);
                                productModel.setProdImageLink(imageLink);
                                productModel.setProdPrice(String.valueOf(price));
                                products.add(productModel);
//                            Dialog dialog = new Dialog(MainActivity.this);
//                            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
//                            View promptsView = inflater.inflate(R.layout.product_dialog,null);
//                            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
//                            dialog.setContentView(promptsView);
//                            dialog.getWindow().setGravity(Gravity.CENTER);
//                            dialog.show();
                                Toast.makeText(MainActivity.this, "Got the data", Toast.LENGTH_SHORT).show();
                                analysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, analyzer);
                            }
                            recyclerView.setAdapter(adapter);
                            Log.i("Products--", "" + products.size());
                            toggleVisibility();
                        }else{
                            Toast.makeText(MainActivity.this, "Valid Code, but no data found", Toast.LENGTH_SHORT).show();
                            analysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, analyzer);
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Invalid Code,Try Again", Toast.LENGTH_SHORT).show();
                        analysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, analyzer);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

            }
        });
        mRequestQueue.add(request);
    }
    public void openHistory(View view) {

    }
}
