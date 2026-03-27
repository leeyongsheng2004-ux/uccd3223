package com.example.individualassignment;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    // 1. Declare variables for UI elements
    EditText etRegUsername, etRegGmail, etRegPassword, etRegPin;
    Button btnRegisterUser, btnBackToLogin;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 2. Initialize the UI elements
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegGmail = findViewById(R.id.etRegGmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegPin = findViewById(R.id.etRegPin);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        dbHelper = new DBHelper(this);

        // 3. Register Button Logic
        btnRegisterUser.setOnClickListener(v -> {
            String user = etRegUsername.getText().toString().trim();
            String gmail = etRegGmail.getText().toString().trim();
            String pass = etRegPassword.getText().toString().trim();
            String pin = etRegPin.getText().toString().trim();

            // --- VALIDATION SECTION ---

            // A. Check if any fields are empty
            if (user.isEmpty() || gmail.isEmpty() || pass.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
            // B. GMAIL FORMAT CHECK (The "HTML-like" solution)
            else if (!Patterns.EMAIL_ADDRESS.matcher(gmail).matches() || !gmail.endsWith("@gmail.com")) {
                etRegGmail.setError("Please enter a valid @gmail.com address");
                etRegGmail.requestFocus();
            }
            // C. PIN LENGTH CHECK
            else if (pin.length() != 6) {
                Toast.makeText(this, "PIN must be exactly 6 digits", Toast.LENGTH_SHORT).show();
            }
            else {
                // 5. Save to database
                boolean isInserted = dbHelper.registerUser(user, gmail, pass, pin);

                if (isInserted) {
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Registration Failed. Gmail might already exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 6. Navigation Logic: Go back to Login Screen
        btnBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }
}
