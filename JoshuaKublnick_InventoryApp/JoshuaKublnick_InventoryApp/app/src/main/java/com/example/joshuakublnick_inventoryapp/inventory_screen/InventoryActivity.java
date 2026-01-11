package com.example.joshuakublnick_inventoryapp.inventory_screen;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
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

public class InventoryActivity extends AppCompatActivity {

    EditText editItemName, editQuantity;
    Button btnAddItem;
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
}
