package com.example.joshuakublnick_inventoryapp.barcode_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joshuakublnick_inventoryapp.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

// This activity handles barcode scanning - can use camera or test mode
public class BarcodeScannerActivity extends AppCompatActivity {

    private EditText testBarcodeInput; // Text field for test mode
    private Button btnTestMode;
    private Button btnCameraMode;
    private Button btnSubmitTestBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        // Connect to UI elements
        testBarcodeInput = findViewById(R.id.testBarcodeInput);
        btnTestMode = findViewById(R.id.btnTestMode);
        btnCameraMode = findViewById(R.id.btnCameraMode);
        btnSubmitTestBarcode = findViewById(R.id.btnSubmitTestBarcode);

        // Test Mode: User can type a barcode manually
        btnTestMode.setOnClickListener(v -> {
            testBarcodeInput.setVisibility(android.view.View.VISIBLE);
            btnSubmitTestBarcode.setVisibility(android.view.View.VISIBLE);
        });

        // Camera Mode: Use device camera to scan barcodes
        btnCameraMode.setOnClickListener(v -> {
            // IntentIntegrator starts the ZXing barcode scanner
            new IntentIntegrator(BarcodeScannerActivity.this).initiateScan();
        });

        // Submit the test barcode
        btnSubmitTestBarcode.setOnClickListener(v -> {
            String barcode = testBarcodeInput.getText().toString().trim();
            if (barcode.isEmpty()) {
                Toast.makeText(this, "Please enter a barcode", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send the scanned barcode back to InventoryActivity
            returnBarcode(barcode);
        });
    }

    // This method gets called when ZXing finishes scanning
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // IntentIntegrator will call this when scan is done
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            // Check if scan was successful
            if (result.getContents() != null) {
                String barcode = result.getContents(); // The scanned barcode
                returnBarcode(barcode); // Send it back to InventoryActivity
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Send the barcode back to InventoryActivity
    private void returnBarcode(String barcode) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("barcode", barcode); // Put the barcode in the result
        setResult(RESULT_OK, resultIntent);
        finish(); // Close this activity
    }
}
