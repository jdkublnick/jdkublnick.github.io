package com.example.joshuakublnick_inventoryapp.login_screen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joshuakublnick_inventoryapp.R;
import com.example.joshuakublnick_inventoryapp.DatabaseHelper;
import com.example.joshuakublnick_inventoryapp.sms_notifs.SmsNotifsActivity;

public class LoginActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        dbHelper = new DatabaseHelper(this);

        EditText username = findViewById(R.id.editUsername);
        EditText password = findViewById(R.id.editPassword);
        Button loginButton = findViewById(R.id.btnLogin);
        Button createAccountButton = findViewById(R.id.btnCreateAccount);

        // Login button
        loginButton.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else if (dbHelper.checkUser(user, pass)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                // Redirect to SMS permission screen
                Intent intent = new Intent(this, SmsNotifsActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Create account button
        createAccountButton.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else if (dbHelper.addUser(user, pass)) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


