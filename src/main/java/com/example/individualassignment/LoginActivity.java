package com.example.individualassignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoToRegister;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        dbHelper = new DBHelper(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                // Check database for user
                if (dbHelper.checkLogin(email, pass)) {
                    // 1. Create the Intent to go to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    // 2. PASS the email to MainActivity so it knows who is logged in
                    intent.putExtra("CURRENT_USER_EMAIL", email);

                    startActivity(intent);
                    finish(); // Close login screen so user can't "back" into it
                } else {
                    Toast.makeText(this, "Invalid Gmail or Password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Navigate to Registration Screen
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
