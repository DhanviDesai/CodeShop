package com.example.codeshop;

//import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.app.Application;
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
import android.view.TextureView;
import android.view.View;
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
import androidx.recyclerview.widget.RecyclerView;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 11111;

    private ArrayList<ProductModel> products;


    private TextureView textureView;
    private FirebaseVisionBarcodeDetectorOptions options;
    private boolean isPermissionGranted = false;
    private RecyclerView recyclerView;
    private TextView checkout,instructions;
    private View divider;
    private LinearLayout heading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.texture_view);

        recyclerView = findViewById(R.id.scannedRecycler);
        divider = findViewById(R.id.view);
        checkout = findViewById(R.id.checkout);
        instructions = findViewById(R.id.instructions);
        heading = findViewById(R.id.linearLayout);

        products = new ArrayList<>();

        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_EAN_13, FirebaseVisionBarcode.FORMAT_QR_CODE,
                        FirebaseVisionBarcode.FORMAT_AZTEC, FirebaseVisionBarcode.FORMAT_EAN_8, FirebaseVisionBarcode.TYPE_ISBN)
                .build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        toggleVisibility();
        setCameraFeed();

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
            }
        }
    }

    public void setCameraFeed() {
        if (isPermissionGranted) {
            PreviewConfig config = new PreviewConfig.Builder()
                    .setLensFacing(CameraX.LensFacing.BACK)
                    .build();
            Preview preview = new Preview(config);
            preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
                @Override
                public void onUpdated(Preview.PreviewOutput output) {
                    textureView.setSurfaceTexture(output.getSurfaceTexture());
                }
            });

            ImageAnalysisConfig config1 = new ImageAnalysisConfig.Builder()
                    .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                    .build();

            ImageAnalysis analysis = new ImageAnalysis(config1);

            analysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, new ImageAnalysis.Analyzer() {
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
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                                    String displayValue = null;

                                    for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {
                                        Rect bounds = barcode.getBoundingBox();
                                        Point[] corners = barcode.getCornerPoints();

                                        String rawValue = barcode.getRawValue();

                                        if (!barcode.getDisplayValue().equals(displayValue)) {
                                          //  textureView.setVisibility(View.GONE);
                                            //Toast.makeText(MainActivity.this, displayValue, Toast.LENGTH_SHORT).show();

                                            displayValue = barcode.getDisplayValue();
                                            Toast.makeText(MainActivity.this, ""+displayValue, Toast.LENGTH_SHORT).show();
//                                            Intent i = new Intent(MainActivity.this, Cart.class);
//                                            i.putExtra("value", displayValue);
//                                            startActivity(i);
                                            try {
                                                detector.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                }
                            })
                            .addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {

                                }
                            });


                }
            });

            CameraX.bindToLifecycle((LifecycleOwner) this, analysis, preview);

        }
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

    public void openHistory(View view) {

    }
}
