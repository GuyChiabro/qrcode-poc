package com.github.hathibelagal.barcodereader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.net.URI;

/**
 * Created by Hathi on 23/8/15.
 */
public class PhotoActivity extends Activity {
    private final int REQUEST_GET_SINGLE_FILE = 911;
    private final String TAG = "My QR Code's Data";
    private URI uri;
    private Button pickBtn;
    private TextView showResultText;
    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        showResultText = findViewById(R.id.result_text);
        mImageView = findViewById(R.id.imageView);


        pickBtn = findViewById(R.id.pick_pic_btn);
        pickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_GET_SINGLE_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "start onActivity Result");
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == REQUEST_GET_SINGLE_FILE){
            if (data != null) {
                Uri imageUri = data.getData();

                try {
                    Log.d(TAG, "start store image into bitmap with Uri: "+imageUri);
                    Bitmap myQRCodeRaw = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    Bitmap myQRCode = getResizedBitmap(myQRCodeRaw, 640);

                    BarcodeDetector barcodeDetector =
                            new BarcodeDetector.Builder(this)
                                    .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                                    .build();

                    Frame myFrame = new Frame.Builder()
                            .setBitmap(myQRCode)
                            .build();

                    SparseArray<Barcode> barcodes = barcodeDetector.detect(myFrame);

                    // Check if at least one barcode was detected
                    if(barcodes.size() != 0) {

                        // Print the QR code's message
                        Log.d(TAG,"Barbode : "+
                                barcodes.valueAt(0).displayValue);
                        //showResultText.setText("testttt");
                        showResultText.setText(barcodes.valueAt(0).displayValue);
                    }
                }catch(Exception e){
                    Log.e("ERROR", e.getMessage());
                }
            }

        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}
