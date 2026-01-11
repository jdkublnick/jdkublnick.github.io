package com.example.joshuakublnick_inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Handles storing and checking user logins and inventory data
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "InventoryApp.db";
    private static final int DB_VERSION = 1;

    // Table for user logins
    private static final String TABLE_USERS = "users";
    private static final String COL_USER = "username";
    private static final String COL_PASS = "password";

    // Table for inventory items
    private static final String TABLE_ITEMS = "items";
    private static final String COL_ID = "id";
    private static final String COL_ITEM = "item_name";
    private static final String COL_QTY = "quantity";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creates both tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS +
                " (" + COL_USER + " TEXT PRIMARY KEY, " + COL_PASS + " TEXT)";
        db.execSQL(createUsers);

        String createItems = "CREATE TABLE " + TABLE_ITEMS +
                " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM + " TEXT, " + COL_QTY + " INTEGER)";
        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // Adds a new user account
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER, username);
        values.put(COL_PASS, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    // Checks if a user exists with matching username and password
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE "
                + COL_USER + "=? AND " + COL_PASS + "=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Adds a new item to the inventory
    public boolean addItem(String name, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM, name);
        values.put(COL_QTY, qty);
        long result = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return result != -1;
    }

    // Updates item quantity
    public boolean updateItem(int id, String name, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM, name);
        values.put(COL_QTY, qty);
        int rows = db.update(TABLE_ITEMS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    // Deletes an item from inventory
    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Gets all items from inventory
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
    }
}
