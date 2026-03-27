package com.example.individualassignment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends AppCompatActivity {

    EditText etSite, etUsername, etPassword, etQuestion, etAnswer;
    Button btnSave;
    DBHelper dbHelper;
    String currentUserEmail; // Variable to store the owner of this data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // 1. Receive the email from MainActivity
        currentUserEmail = getIntent().getStringExtra("CURRENT_USER_EMAIL");

        // 2. Initialize the UI elements
        etSite = findViewById(R.id.etSite);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etQuestion = findViewById(R.id.etQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new DBHelper(this);

        btnSave.setOnClickListener(v -> {
            String site = etSite.getText().toString().trim();
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String ques = etQuestion.getText().toString().trim();
            String ans = etAnswer.getText().toString().trim();

            // 3. Validation
            if (site.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in Site, Username, and Password", Toast.LENGTH_SHORT).show();
            } else if (currentUserEmail == null) {
                // Safety check to ensure we have an owner for this data
                Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            } else {
                // 4. Call insertData with the 6th parameter (Email)
                boolean isSaved = dbHelper.insertData(site, user, pass, ques, ans, currentUserEmail);

                if (isSaved) {
                    Toast.makeText(this, "Saved to your Vault!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to MainActivity
                } else {
                    Toast.makeText(this, "Error: Could not save to database", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}