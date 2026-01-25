package com.example.joshuakublnick_inventoryapp.inventory_screen;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.joshuakublnick_inventoryapp.DatabaseHelper;
import com.example.joshuakublnick_inventoryapp.R;
import com.example.joshuakublnick_inventoryapp.barcode_scanner.BarcodeScannerActivity;

public class InventoryActivity extends AppCompatActivity {

    EditText editItemName, editQuantity;
    Button btnAddItem;
    Button btnScanBarcode; // New button for barcode scanning
    GridLayout gridLayout;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_screen);

        // Connect layout elements
        editItemName = findViewById(R.id.editItemName);
        editQuantity = findViewById(R.id.editQuantity);
        btnAddItem   = findViewById(R.id.btnAddItem);
        btnScanBarcode = findViewById(R.id.btnScanBarcode); // Connect the scan button
        gridLayout   = findViewById(R.id.gridInventory);

        // Database helper
        db = new DatabaseHelper(this);

        // Show existing inventory
        showItems();

        // Add button adds new items to inventory
        btnAddItem.setOnClickListener(v -> {
            String name = editItemName.getText().toString().trim();
            String qtyStr = editQuantity.getText().toString().trim();

            if (name.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(this, "Enter item name and quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int qty = Integer.parseInt(qtyStr);

            if (db.addItem(name, qty)) {
                Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();

                // Send low stock alert if item quantity is low
                if (qty <= 2) {
                    sendLowStockAlert(name, qty);
                }

                editItemName.setText("");
                editQuantity.setText("");
                showItems();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });

        // Scan barcode button - opens the barcode scanner activity
        btnScanBarcode.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, BarcodeScannerActivity.class);
            startActivityForResult(intent, 1); // Start barcode scanner with request code 1
        });
    }

    // Displays all items from the database
    private void showItems() {
        gridLayout.removeAllViews();
        Cursor c = db.getAllItems();

        if (c != null && c.moveToFirst()) {
            do {
                int id   = c.getInt(c.getColumnIndexOrThrow("id"));
                String n = c.getString(c.getColumnIndexOrThrow("item_name"));
                int q    = c.getInt(c.getColumnIndexOrThrow("quantity"));

                // Create a row for each item
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackgroundColor(Color.parseColor("#1A1A1A"));
                row.setPadding(12, 12, 12, 12);
                row.setGravity(Gravity.CENTER_VERTICAL);

                // Item name
                TextView nameView = new TextView(this);
                nameView.setText(n);
                nameView.setTextColor(Color.WHITE);
                nameView.setGravity(Gravity.CENTER);
                nameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

                // Quantity
                TextView qtyView = new TextView(this);
                qtyView.setText(String.valueOf(q));
                qtyView.setTextColor(Color.WHITE);
                qtyView.setGravity(Gravity.CENTER);
                qtyView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                // Edit button (pencil icon)
                Button editBtn = new Button(this);
                editBtn.setText("\u270E"); // Unicode pencil icon
                editBtn.setTextColor(Color.BLACK);
                editBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FFC6")));
                editBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                editBtn.setOnClickListener(v -> showEditDialog(id, n, q));

                // Delete button (X)
                Button deleteBtn = new Button(this);
                deleteBtn.setText("X");
                deleteBtn.setTextColor(Color.BLACK);
                deleteBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FFC6")));
                deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                deleteBtn.setOnClickListener(v -> {
                    db.deleteItem(id);
                    showItems();
                });

                // Add all components to row
                row.addView(nameView);
                row.addView(qtyView);
                row.addView(editBtn);
                row.addView(deleteBtn);

                gridLayout.addView(row);

            } while (c.moveToNext());
            c.close(); // Always close cursor to free memory
        }
    }

    // Opens a dialog to edit quantity
    private void showEditDialog(int id, String name, int qty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + name);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(qty));
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            int newQty = Integer.parseInt(input.getText().toString().trim());
            if (db.updateItem(id, name, newQty)) {
                Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show();

                // Send SMS alert if quantity is low
                if (newQty <= 2) {
                    sendLowStockAlert(name, newQty);
                }

                showItems();
            } else {
                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Sends a low stock SMS alert
    private void sendLowStockAlert(String itemName, int qty) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                String phoneNumber = "5554"; // Emulator number for testing
                String message = "ALERT: Inventory low for item '" + itemName + "'. Only " + qty + " left!";

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);

                Toast.makeText(this, "Low-stock SMS sent for " + itemName, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "SMS permission denied — no alert sent.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // This method is called when the barcode scanner returns a result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if result is from the barcode scanner (request code 1)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String barcode = data.getStringExtra("barcode"); // Get the scanned barcode

            if (barcode == null || barcode.isEmpty()) {
                Toast.makeText(this, "Invalid barcode", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if an item with this barcode already exists
            int itemId = db.findItemByBarcode(barcode);

            if (itemId != -1) {
                // Item exists - increase its quantity by 1
                String itemName = db.getItemNameById(itemId);
                int currentQty = db.getItemQtyById(itemId);
                int newQty = currentQty + 1;

                db.updateItem(itemId, itemName, newQty);
                Toast.makeText(this, "Added 1 to " + itemName + "!", Toast.LENGTH_SHORT).show();

                // Check if quantity is low
                if (newQty <= 2) {
                    sendLowStockAlert(itemName, newQty);
                }
            } else {
                // Item doesn't exist yet - ask user for item name and create new item
                showBarcodeItemDialog(barcode);
            }

            // Refresh the inventory list
            showItems();
        }
    }

    // Shows dialog to add a new item with a barcode
    private void showBarcodeItemDialog(String barcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Item (Barcode: " + barcode + ")");

        // Input field for item name
        final EditText input = new EditText(this);
        input.setHint("Enter item name");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add with quantity 1", (dialog, which) -> {
            String itemName = input.getText().toString().trim();
            if (itemName.isEmpty()) {
                Toast.makeText(InventoryActivity.this, "Please enter item name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add the new item with barcode and quantity 1
            if (db.addItemWithBarcode(itemName, 1, barcode)) {
                Toast.makeText(InventoryActivity.this, "New item added: " + itemName, Toast.LENGTH_SHORT).show();
                showItems(); // Refresh list
            } else {
                Toast.makeText(InventoryActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
