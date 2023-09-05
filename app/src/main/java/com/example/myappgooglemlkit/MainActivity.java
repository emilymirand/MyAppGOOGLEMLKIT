package com.example.myappgooglemlkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_PICK = 200;

    private ImageView imageView;
    private TextView resultText;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        Button chooseImageButton = findViewById(R.id.btnChooseImage);

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abre una actividad para seleccionar una imagen de la galería.
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                // Si se elige una imagen de la galería, procesa la imagen.
                Uri imageUri = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageView.setImageBitmap(imageBitmap);
                    detectBarcodeAndQR();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void detectBarcodeAndQR() {
        if (imageBitmap == null) {
            return;
        }

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_128)
                        .build();

        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);

        scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        if (barcodes.size() > 0) {
                            StringBuilder result = new StringBuilder();
                            for (Barcode barcode : barcodes) {
                                result.append("Tipo de código: ").append(barcode.getFormat()).append("\n");
                                result.append("Valor: ").append(barcode.getRawValue()).append("\n\n");
                            }
                            resultText.setText(result.toString());
                        } else {
                            resultText.setText("No se encontraron códigos de barras ni códigos QR.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resultText.setText("Error al detectar códigos: " + e.getMessage());
                    }
                });
    }
}