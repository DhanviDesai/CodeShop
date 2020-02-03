package com.example.codeshop;

//import android.support.v7.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.HardwareBuffer;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codeshop.model.ProductModel;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 11111;

    public static final int MY_PERMISSIONS_REQUEST_INTERNET = 22222;

    private ArrayList<ProductModel> products;

    private ImageReader mReader;

    private HandlerThread mImageAvailbleThread;

    private Handler mImageHandler;

    private TextureView textureView;
    private FirebaseVisionBarcodeDetectorOptions options;
    private boolean isPermissionGranted = false;
    private boolean isInternetGranted = false;
    private RecyclerView recyclerView;
    private TextView checkout, instructions;
    private View divider;
    private LinearLayout heading;
    private SurfaceView surfaceView;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                startCameraFeed(width,height);

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        recyclerView = findViewById(R.id.scannedRecycler);
        divider = findViewById(R.id.view);
        checkout = findViewById(R.id.checkout);
        instructions = findViewById(R.id.instructions);
        heading = findViewById(R.id.linearLayout);
        // previewView = findViewById(R.id.previewView);

        // processCameraProvider = ProcessCameraProvider.getInstance(this);

        products = new ArrayList<>();

        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_EAN_13,
                        FirebaseVisionBarcode.FORMAT_QR_CODE,
                        FirebaseVisionBarcode.FORMAT_AZTEC,
                        FirebaseVisionBarcode.FORMAT_EAN_8,
                        FirebaseVisionBarcode.TYPE_ISBN
                )
                .build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            isInternetGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);
        }

   /*   processCameraProvider.addListener(() -> {

            try {
                ProcessCameraProvider camProvider = processCameraProvider.get();
                Log.i("Here",""+i++);


                Preview preview = new Preview.Builder()
                        .setTargetName("Preview")
                        .build();

                preview.setPreviewSurfaceProvider(previewView.getPreviewSurfaceProvider());


                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .build();

                analysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, new ImageAnalysis.Analyzer() {
                    @SuppressLint("UnsafeExperimentalUsageError")
                    @Override
                    public void analyze(@NonNull ImageProxy image) {
                        if(image.getImage() == null){
                            return;
                        }
                        Image im = image.getImage();
                        int rotation = degreesToFirebaseRotation(previewView.getDisplay().getRotation());
                        Log.i("Here",""+rotation);
                        FirebaseVisionImage image1 = FirebaseVisionImage.fromMediaImage(im,rotation);

                        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

                        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image1);
                        result.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                                Log.i("Here","Success");
                                Log.i("Here",""+firebaseVisionBarcodes.size());
                                for(FirebaseVisionBarcode barcode : firebaseVisionBarcodes){
                                    Log.i("Here",barcode.getDisplayValue());
                                }
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("Here","Failure");
                                    }
                                });


                    }
                });

                CameraSelector selector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

               Camera camera =  camProvider.bindToLifecycle((LifecycleOwner)this,selector,preview,analysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }


        },ContextCompat.getMainExecutor(this));*/


        //startCameraFeed();


        toggleVisibility();
        // setCameraFeed();

    }

    private void toggleVisibility() {
        if (products.size() == 0) {
            recyclerView.setVisibility(GONE);
            divider.setVisibility(GONE);
            checkout.setVisibility(GONE);
            heading.setVisibility(GONE);
            instructions.setVisibility(VISIBLE);
        } else {
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
        if (requestCode == MY_PERMISSIONS_REQUEST_INTERNET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isInternetGranted = true;
            }
        }
    }

    public void startCameraFeed(int desiredWidth,int desiredHeight) {
        Handler cameraBackgroundHandler = new Handler();
        CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer direction = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (direction != null && direction == CameraCharacteristics.LENS_FACING_BACK) {

                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        cameraManager.openCamera(id, new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(@NonNull CameraDevice camera) {





                                FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);



                                mReader = ImageReader.newInstance(desiredWidth, desiredHeight,
                                        ImageFormat.YUV_420_888, 1);


                                mReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                                    @Override
                                    public void onImageAvailable(ImageReader reader) {
                                       // Toast.makeText(MainActivity.this, "Here", Toast.LENGTH_SHORT).show();
                                        try {
                                            //Toast.makeText(MainActivity.this, "Here", Toast.LENGTH_SHORT).show();

                                            Image im = reader.acquireNextImage();

                                            Image.Plane[] planes = im.getPlanes();

                                            ByteBuffer buffer = planes[0].getBuffer();

                                            byte[] bytes = new byte[buffer.capacity()];

                                            buffer.get(bytes);

                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);

                                            //Toast.makeText(MainActivity.this, "" + im.toString(), Toast.LENGTH_SHORT).show();

                                            int rotation = getRotationCompensation(id, MainActivity.this, MainActivity.this);


                                            int rotation1 = getJpegOrientation(characteristics,getWindowManager().getDefaultDisplay().getRotation());

                                            Log.i("Rotation",""+rotation1);

                                            FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                                                    .setWidth(480)   // 480x360 is typically sufficient for
                                                    .setHeight(360)  // image recognition
                                                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                                                    .setRotation(rotation1)
                                                    .build();


                                            FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(bytes,metadata);


                                            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image);


                                            result.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                                @Override
                                                public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                                                    //Log.i("Here", "Success--"+firebaseVisionBarcodes.size());
                                                    //Toast.makeText(MainActivity.this, "" + firebaseVisionBarcodes.size(), Toast.LENGTH_SHORT).show();
                                                    for(FirebaseVisionBarcode barcode : firebaseVisionBarcodes){
                                                        Log.i("Hi",barcode.getDisplayValue());
                                                        Toast.makeText(MainActivity.this, ""+barcode.getDisplayValue(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                  //  Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                    e.printStackTrace();
                                                }
                                            });
                                            im.close();
                                        } catch (CameraAccessException e) {
                                            Log.i("HereBarcode","Error");
                                            e.printStackTrace();
                                        }

                                    }


                                }, cameraBackgroundHandler);

                                CameraCaptureSession.StateCallback callback = new CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(@NonNull CameraCaptureSession session) {
                                        try {
                                            Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                            builder.addTarget(surfaceView.getHolder().getSurface());
                                            builder.addTarget(mReader.getSurface());
                                            session.setRepeatingRequest(builder.build(), null, null);
                                            Toast.makeText(MainActivity.this, "CameraOpened", Toast.LENGTH_SHORT).show();
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                                        Log.i("CameraConfiguring", "Failed");

                                        mReader = ImageReader.newInstance(desiredWidth,desiredHeight,ImageFormat.YUV_420_888,1);

                                    }
                                };

                                try {
                                    Surface surface = surfaceView.getHolder().getSurface();
                                    ArrayList<Surface> surfaces = new ArrayList<>();
                                    surfaces.add(surface);
                                    surfaces.add(mReader.getSurface());
                                    Log.i("Surfaces", "" + surfaces.size());
                                    camera.createCaptureSession(surfaces, callback, cameraBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    Log.i("HereCapture","SessionError");
                                    e.printStackTrace();
                                }


                            }

                            @Override
                            public void onDisconnected(@NonNull CameraDevice camera) {

                                Log.i("CameraOpen", "Disconnected");

                            }

                            @Override
                            public void onError(@NonNull CameraDevice camera, int error) {

                                Log.i("CameraOpen", "Error");

                            }
                        }, cameraBackgroundHandler);

                    }
                }
                }
            } catch(CameraAccessException e){
            Log.i("Error","MainError");
                e.printStackTrace();
            }
        }

/* int j=0;

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
                    .setImageQueueDepth(1)
                    .build();

            ImageAnalysis analysis = new ImageAnalysis(config1);

            analysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                @Override
                public void analyze(ImageProxy image, int rotationDegrees) {
                    j++;
                    if (image == null || image.getImage() == null) {
                        return;
                    }

                    Image im = image.getImage();
                    int rotation = degreesToFirebaseRotation(rotationDegrees);
                    FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromMediaImage(im, rotation);

                    FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

                    Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(firebaseImage)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                                    String displayValue = null;
                                    if (firebaseVisionBarcodes.size() ==1 ) {
                                        FirebaseVisionBarcode barcode = firebaseVisionBarcodes.get(0);
                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                        } else {
                                            //deprecated in API 26
                                            v.vibrate(500);
                                        }*/

                                     /*  Rect bounds = barcode.getBoundingBox();
                                       Point[] corners = barcode.getCornerPoints();

                                        String rawValue = barcode.getRawValue();

                                        Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                                        dialog.setContentView(R.layout.product_dialog);
                                        dialog.show();
                                        displayValue = barcode.getDisplayValue();
                                        Toast.makeText(MainActivity.this, "" + displayValue+"--"+j, Toast.LENGTH_SHORT).show();

                                        analysis.removeAnalyzer();
                                        //  textureView.setVisibility(View.GONE);
                                        //Toast.makeText(MainActivity.this, displayValue, Toast.LENGTH_SHORT).show();

                                        //Toast.makeText(MainActivity.this, ""+displayValue, Toast.LENGTH_SHORT).show();
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
    }*/

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

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }



    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e(TAG, "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        //stopBackgroundThread();
        super.onPause();
    }

    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 270) % 360;

        return jpegOrientation;
    }

    public void openHistory(View view) {

    }

    }
