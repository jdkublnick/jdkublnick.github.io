package com.example.joshuakublnick_inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Handles storing and checking user logins and inventory data
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "InventoryApp.db";
    private static final int DB_VERSION = 2; // Updated version for new barcode column

    // Table for user logins
    private static final String TABLE_USERS = "users";
    private static final String COL_USER = "username";
    private static final String COL_PASS = "password";

    // Table for inventory items
    private static final String TABLE_ITEMS = "items";
    private static final String COL_ID = "id";
    private static final String COL_ITEM = "item_name";
    private static final String COL_QTY = "quantity";
    private static final String COL_BARCODE = "barcode"; // New column to store barcode for each item

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
                COL_ITEM + " TEXT, " + COL_QTY + " INTEGER, " +
                COL_BARCODE + " TEXT)"; // barcode column - each item has a unique barcode
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

    // Adds a new item with a barcode
    public boolean addItemWithBarcode(String name, int qty, String barcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM, name);
        values.put(COL_QTY, qty);
        values.put(COL_BARCODE, barcode); // Store the barcode with the item
        long result = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return result != -1;
    }

    // Finds an item by barcode - returns the item id, or -1 if not found
    public int findItemByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_ID + " FROM " + TABLE_ITEMS +
                " WHERE " + COL_BARCODE + "=?", new String[]{barcode});

        int id = -1; // -1 means item not found
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)); // Get the item id
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return id;
    }

    // Gets item name by id (useful for showing what item was just added)
    public String getItemNameById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_ITEM + " FROM " + TABLE_ITEMS +
                " WHERE " + COL_ID + "=?", new String[]{String.valueOf(id)});

        String name = null;
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM)); // Get the item name
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return name;
    }

    // Gets the current quantity of an item by id
    public int getItemQtyById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_QTY + " FROM " + TABLE_ITEMS +
                " WHERE " + COL_ID + "=?", new String[]{String.valueOf(id)});

        int qty = 0;
        if (cursor != null && cursor.moveToFirst()) {
            qty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QTY)); // Get the current quantity
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return qty;
    }
}
