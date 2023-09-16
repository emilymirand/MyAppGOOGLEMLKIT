package com.example.myappgooglemlkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 111;
    private static final int REQUEST_GALLERY = 113;
    private boolean isCameraSource = true;

    private Bitmap mSelectedImage;
    private ImageView mImageView;
    private TextView txtResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
    }

    private void initializeViews() {
        txtResults = findViewById(R.id.txtresultados);
        mImageView = findViewById(R.id.image_view);
    }

    public void onCameraButtonClick(View view) {
        launchCamera();
    }
    public void onGalleryButtonClick(View view) {
        isCameraSource = false;
        launchGallery();
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }
    private void launchGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            handleImageCapture(data);
        }
    }

    /*private void handleImageCapture(int requestCode, Intent data) {
        try {
            mSelectedImage = requestCode == REQUEST_CAMERA ? (Bitmap) data.getExtras().get("data")
                    : MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            displayCapturedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private void displayCapturedImage() {
        mImageView.setImageBitmap(mSelectedImage);
    }

    public void onScanQRButtonClick(View v) {
        if (mSelectedImage != null) {
            scanImageForQR();
        } else {
            txtResults.setText("No image to scan");
        }
    }

    private void scanImageForQR() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        BarcodeScanner scanner = BarcodeScanning.getClient();
        processImage(scanner, image);
    }

    private void processImage(BarcodeScanner scanner, InputImage image) {
        scanner.process(image)
                .addOnSuccessListener(this::displayBarcodes)
                .addOnFailureListener(e -> txtResults.setText("Error al procesar imagen"));
    }

    private void displayBarcodes(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            String value = barcode.getDisplayValue();
            txtResults.setText(value);
        }
    }
    public void selectImageSource(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar fuente de imagen")
                .setItems(new CharSequence[]{"Cámara", "Galería"}, (dialog, which) -> {
                    if (which == 0) {
                        isCameraSource = true;
                        launchCamera();
                    } else {
                        isCameraSource = false;
                        launchGallery();
                    }
                })
                .show();
    }
    private void handleImageCapture(Intent data) {
        try {
            if (isCameraSource) {
                mSelectedImage = (Bitmap) data.getExtras().get("data");
            } else {
                Uri imageUri = data.getData();
                mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
            displayCapturedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}