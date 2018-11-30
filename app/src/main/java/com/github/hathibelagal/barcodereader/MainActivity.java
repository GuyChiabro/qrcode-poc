package com.github.hathibelagal.barcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Created by Hathi on 21/8/15.
 */
public class MainActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 10;

    private BarcodeDetector barcodeDetector;
    private BoxDetector boxDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private TextView barcodeInfo;
    private Button pickPicBtn;
    private FrameLayout focusFrame;
    private int focusFrameWidth;
    private int focusFrameHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        focusFrame = findViewById(R.id.focus_frame);
        cameraView = findViewById(R.id.camera_view);
        barcodeInfo = findViewById(R.id.code_info);
        pickPicBtn = findViewById(R.id.pick_pic_btn);
        pickPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PhotoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int height = size.y;
        boxDetector = new BoxDetector(barcodeDetector, focusFrameWidth, focusFrameHeight);


        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                focusFrameWidth = focusFrame.getMeasuredWidth();
                focusFrameHeight = focusFrame.getMeasuredHeight();
                boxDetector.setBox(focusFrameWidth,focusFrameHeight);
                cameraSource = new CameraSource
                        .Builder(MainActivity.this, boxDetector)
                        .setRequestedPreviewSize(height,width)
                        .setAutoFocusEnabled(true)
                        .build();

                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(cameraView.getHolder());
                    }else{
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                MainActivity.this, Manifest.permission.CAMERA)) {


                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        boxDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            barcodeInfo.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue // QR CODE URL
                            );
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }
}
