package com.example.individualassignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // Version 4: Added user_email to link passwords to specific users
    public DBHelper(Context context) {
        super(context, "PasswordDB", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table 1: Added 'user_email' column to filter data by user
        db.execSQL("CREATE TABLE passwords (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "site TEXT, " +
                "username TEXT, " +
                "password TEXT, " +
                "question TEXT, " +
                "answer TEXT, " +
                "user_email TEXT)");

        // Table 2: User Accounts
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "gmail TEXT, " +
                "password TEXT, " +
                "pin TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS passwords");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // --- USER REGISTRATION & LOGIN METHODS ---

    public boolean registerUser(String username, String gmail, String password, String pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("gmail", gmail);
        cv.put("password", password);
        cv.put("pin", pin);

        long result = db.insert("users", null, cv);
        return result != -1;
    }

    public boolean checkLogin(String gmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE gmail=? AND password=?", new String[]{gmail, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkPin(String gmail, String pin) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE gmail=? AND pin=?", new String[]{gmail, pin});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // --- PASSWORD VAULT METHODS (Now User-Specific) ---

    // 1. CREATE (Now requires the email of the logged-in user)
    public boolean insertData(String site, String username, String password, String question, String answer, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("site", site);
        cv.put("username", username);
        cv.put("password", password);
        cv.put("question", question);
        cv.put("answer", answer);
        cv.put("user_email", email); // Links this entry to the user

        long result = db.insert("passwords", null, cv);
        return result != -1;
    }

    // 2. READ (Now FILTERS by email so users only see their own data)
    public Cursor getAllData(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM passwords WHERE user_email=?", new String[]{email});
    }

    // 3. UPDATE
    public boolean updateData(String id, String site, String username, String password, String question, String answer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("site", site);
        cv.put("username", username);
        cv.put("password", password);
        cv.put("question", question);
        cv.put("answer", answer);

        long result = db.update("passwords", cv, "id=?", new String[]{id});
        return result > 0;
    }

    // 4. DELETE
    public boolean deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete("passwords", "id=?", new String[]{id});
        return result > 0;
    }
}