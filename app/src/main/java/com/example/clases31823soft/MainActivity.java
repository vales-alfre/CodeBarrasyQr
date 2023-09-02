package com.example.clases31823soft;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnFailureListener {

    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;

    TextView txtResults;
    ImageView mImageView;
    Bitmap mSelectedImage;
    ArrayList<String> permisosNoAprobados;
    Button btnCamara, btnGaleria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> permisos_requeridos = new ArrayList<String>();
        permisos_requeridos.add(Manifest.permission.CAMERA);
        permisos_requeridos.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        permisos_requeridos.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        txtResults = findViewById(R.id.txtresults);
        mImageView = findViewById(R.id.image_view);
        btnCamara = findViewById(R.id.btCamera);
        btnGaleria = findViewById(R.id.btGallery);
    }


    public void abrirGaleria(View view) {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }

    public void abrirCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try {
                if (requestCode == REQUEST_CAMERA)
                    mSelectedImage = (Bitmap) data.getExtras().get("data");
                else
                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                mImageView.setImageBitmap(mSelectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.CAMERA)) {

                btnCamara.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            } else if (permissions[i].equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE) ||
                    permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                btnGaleria.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            }
        }
    }

    public ArrayList<String> getPermisosNoAprobados(ArrayList<String> listaPermisos) {
        ArrayList<String> list = new ArrayList<String>();
        Boolean habilitado;


        if (Build.VERSION.SDK_INT >= 23)
            for (String permiso : listaPermisos) {
                if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permiso);
                    habilitado = false;
                }
            }
        return list;
    }


    @Override
    public void onFailure(@NonNull Exception e) {
        txtResults.setText("Error al procesar imagen");
    }


    public void scanBarcodeQR(View view) {
        if (mSelectedImage == null) {
            txtResults.setText("Primero selecciona una imagen.");
            return;
        }

        InputImage image = getInputImageFromSelectedImage();
        BarcodeScanner scanner = getBarcodeScannerWithOptions();

        processBarcodeImage(scanner, image);
    }

    private InputImage getInputImageFromSelectedImage() {
        return InputImage.fromBitmap(mSelectedImage, 0);
    }

    private BarcodeScanner getBarcodeScannerWithOptions() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39, Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E, Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_ITF, Barcode.FORMAT_PDF417,
                        Barcode.FORMAT_AZTEC
                )
                .build();

        return BarcodeScanning.getClient(options);
    }

    private void processBarcodeImage(BarcodeScanner scanner, InputImage image) {
        scanner.process(image)
                .addOnSuccessListener(this::handleSuccessfulScan)
                .addOnFailureListener(this::handleFailedScan);
    }

    private void handleSuccessfulScan(List<Barcode> barcodes) {
        if (barcodes.isEmpty()) {
            txtResults.setText("No se encontraron códigos de barras o códigos QR.");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (Barcode barcode : barcodes) {
            result.append("Tipo de código: ")
                    .append(barcode.getFormat())
                    .append("\nValor: ")
                    .append(barcode.getRawValue())
                    .append("\n");
        }
        txtResults.setText(result.toString());
    }

    private void handleFailedScan(@NonNull Exception e) {
        txtResults.setText("Error al escanear códigos de barras o códigos QR.");
    }
}


