package com.example.individualassignment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    Button btnAdd;
    DBHelper dbHelper;
    ArrayList<PasswordModel> passwordList;
    PasswordAdapter adapter;
    String loggedInEmail; // This is the unique key for the current user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Receive the logged-in email from LoginActivity
        loggedInEmail = getIntent().getStringExtra("CURRENT_USER_EMAIL");

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);
        dbHelper = new DBHelper(this);

        passwordList = new ArrayList<>();
        adapter = new PasswordAdapter();
        listView.setAdapter(adapter);

        // View details (Requires PIN)
        listView.setOnItemClickListener((parent, view, position, id) -> showReAuthDialog(position));

        // IMPORTANT: Pass the email to AddActivity so the new data knows who owns it
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            intent.putExtra("CURRENT_USER_EMAIL", loggedInEmail);
            startActivity(intent);
        });
    }

    private void loadData() {
        passwordList.clear();
        // FIX: Pass loggedInEmail to the database to filter results
        Cursor cursor = dbHelper.getAllData(loggedInEmail);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                passwordList.add(new PasswordModel(
                        cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5)
                ));
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    // --- CUSTOM ADAPTER ---
    class PasswordAdapter extends BaseAdapter {
        @Override
        public int getCount() { return passwordList.size(); }
        @Override
        public Object getItem(int i) { return passwordList.get(i); }
        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) view = getLayoutInflater().inflate(R.layout.item_password, null);

            TextView tvSite = view.findViewById(R.id.tvSiteRow);
            TextView tvUser = view.findViewById(R.id.tvUserRow);
            TextView tvPass = view.findViewById(R.id.tvPassRow);
            ImageView ivEdit = view.findViewById(R.id.ivEdit);
            ImageView ivDelete = view.findViewById(R.id.ivDelete);

            PasswordModel model = passwordList.get(i);

            tvSite.setText("Site: " + model.site);
            tvUser.setText("Username: " + model.user);
            tvPass.setText("Password: ********");

            ivEdit.setOnClickListener(v -> showPinBeforeAction(i, "UPDATE"));
            ivDelete.setOnClickListener(v -> showPinBeforeAction(i, "DELETE"));

            return view;
        }
    }

    // --- DATA MODEL ---
    class PasswordModel {
        String id, site, user, pass, ques, ans;
        PasswordModel(String id, String s, String u, String p, String q, String a) {
            this.id=id; this.site=s; this.user=u; this.pass=p; this.ques=q; this.ans=a;
        }
    }

    // --- SECURITY & ACTIONS ---
    private void showPinBeforeAction(int position, String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify PIN");
        builder.setMessage("Enter 6-digit PIN to " + action.toLowerCase() + ":");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            if (dbHelper.checkPin(loggedInEmail, input.getText().toString())) {
                if (action.equals("UPDATE")) showUpdateDialog(position);
                else showDeleteConfirm(position);
            } else {
                Toast.makeText(this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showUpdateDialog(int position) {
        PasswordModel model = passwordList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Entry");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 20);

        final EditText etS = addLabeledEditText(layout, "Site Name:", model.site);
        final EditText etU = addLabeledEditText(layout, "Username:", model.user);
        final EditText etP = addLabeledEditText(layout, "Password:", model.pass);
        final EditText etQ = addLabeledEditText(layout, "Security Question:", model.ques);
        final EditText etA = addLabeledEditText(layout, "Security Answer:", model.ans);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            dbHelper.updateData(model.id, etS.getText().toString(), etU.getText().toString(),
                    etP.getText().toString(), etQ.getText().toString(), etA.getText().toString());
            loadData();
            Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirm(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to remove " + passwordList.get(position).site + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteData(passwordList.get(position).id);
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showReAuthDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("View Details");
        builder.setMessage("Enter PIN to reveal secrets:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Unlock", (dialog, which) -> {
            if (dbHelper.checkPin(loggedInEmail, input.getText().toString())) {
                showFinalDetails(passwordList.get(position));
            } else {
                Toast.makeText(this, "Wrong PIN!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void showFinalDetails(PasswordModel m) {
        new AlertDialog.Builder(this)
                .setTitle(m.site + " Details")
                .setMessage("Username: " + m.user + "\nPassword: " + m.pass + "\nChallenge Question: " + m.ques + "\nSecurity Answer: " + m.ans)
                .setPositiveButton("OK", null)
                .show();
    }

    private EditText addLabeledEditText(LinearLayout layout, String labelTitle, String defaultValue) {
        TextView label = new TextView(this);
        label.setText(labelTitle);
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(0, 20, 0, 0);
        layout.addView(label);
        EditText editText = new EditText(this);
        editText.setText(defaultValue);
        layout.addView(editText);
        return editText;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}