package com.example.joshuakublnick_inventoryapp.sms_notifs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.joshuakublnick_inventoryapp.R;
import com.example.joshuakublnick_inventoryapp.inventory_screen.InventoryActivity;

public class SmsNotifsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    Button btnCheckPermission, btnRequestPermission;
    TextView textPermissionStatus;
    boolean smsPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_notifs);

        btnCheckPermission = findViewById(R.id.btnCheckPermission);
        btnRequestPermission = findViewById(R.id.btnRequestPermission);
        textPermissionStatus = findViewById(R.id.textPermissionStatus);

        // "Check" button
        btnCheckPermission.setOnClickListener(v -> {
            checkSmsPermission(false);
            goToInventory(); // continue after checking
        });

        // "Request" button
        btnRequestPermission.setOnClickListener(v -> checkSmsPermission(true));
    }

    private void checkSmsPermission(boolean requestIfNeeded) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            smsPermissionGranted = true;
            textPermissionStatus.setText("SMS permission already granted ✅");
            textPermissionStatus.setTextColor(getColor(R.color.teal_200));
        } else {
            smsPermissionGranted = false;
            textPermissionStatus.setText("SMS permission not granted ❌");
            textPermissionStatus.setTextColor(getColor(android.R.color.holo_red_light));

            if (requestIfNeeded) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsPermissionGranted = true;
                textPermissionStatus.setText("Permission granted SMS alerts enabled.");
                textPermissionStatus.setTextColor(getColor(R.color.teal_200));
                Toast.makeText(this, "You can now receive SMS notifications.", Toast.LENGTH_SHORT).show();
            } else {
                smsPermissionGranted = false;
                textPermissionStatus.setText("Permission denied SMS alerts disabled.");
                textPermissionStatus.setTextColor(getColor(android.R.color.holo_red_light));
                Toast.makeText(this, "App will still function without SMS alerts.", Toast.LENGTH_LONG).show();
            }

            goToInventory();
        }
    }

    private void goToInventory() {
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
        finish();
    }
}




