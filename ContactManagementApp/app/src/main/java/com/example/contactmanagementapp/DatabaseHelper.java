package com.example.contactmanagementapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper  extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quanlydanhba.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_DONVI = "DonVi";
    public static final String COLUMN_DONVI_ID = "idDonVi";
    public static final String COLUMN_DONVI_NAME = "tenDonVi";
    public static final String COLUMN_DONVI_EMAIL = "emailDonVi";
    public static final String COLUMN_DONVI_WEBSITE = "website";
    public static final String COLUMN_DONVI_LOGO = "logo";
    public static final String COLUMN_DONVI_ADDRESS = "diaChi";
    public static final String COLUMN_DONVI_PHONE = "dienThoaiDonVi";
    public static final String COLUMN_DONVI_PARENT_ID = "idDonViCha";

    public static final String TABLE_NHANVIEN = "NhanVien";
    public static final String COLUMN_NHANVIEN_ID = "idNhanVien";
    public static final String COLUMN_NHANVIEN_NAME = "tenNhanVien";
    public static final String COLUMN_NHANVIEN_POSITION = "ChucVu";
    public static final String COLUMN_NHANVIEN_EMAIL = "emailNhanVien";
    public static final String COLUMN_NHANVIEN_PHONE = "dienThoaiNhanVien";
    public static final String COLUMN_NHANVIEN_AVATAR = "anhDaiDien";
    public static final String COLUMN_NHANVIEN_UNIT_ID = "idDonViNV";

    private static final String TABLE_DONVI_CREATE =
            "CREATE TABLE " + TABLE_DONVI + " (" +
                    COLUMN_DONVI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DONVI_NAME + " TEXT, " +
                    COLUMN_DONVI_EMAIL + " TEXT, " +
                    COLUMN_DONVI_WEBSITE + " TEXT, " +
                    COLUMN_DONVI_LOGO + " TEXT, " +
                    COLUMN_DONVI_ADDRESS + " TEXT, " +
                    COLUMN_DONVI_PHONE + " TEXT, " +
                    COLUMN_DONVI_PARENT_ID + " INTEGER);";

    private static final String TABLE_NHANVIEN_CREATE =
            "CREATE TABLE " + TABLE_NHANVIEN + " (" +
                    COLUMN_NHANVIEN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NHANVIEN_NAME + " TEXT, " +
                    COLUMN_NHANVIEN_POSITION + " TEXT, " +
                    COLUMN_NHANVIEN_EMAIL + " TEXT, " +
                    COLUMN_NHANVIEN_PHONE + " TEXT, " +
                    COLUMN_NHANVIEN_AVATAR + " TEXT, " +
                    COLUMN_NHANVIEN_UNIT_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_NHANVIEN_UNIT_ID + ") REFERENCES " + TABLE_DONVI + "(" + COLUMN_DONVI_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_DONVI_CREATE);
        db.execSQL(TABLE_NHANVIEN_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DONVI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NHANVIEN);
        onCreate(db);
    }
    public List<String> searchDonViByName(String keyword) {
        List<String> resultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_DONVI_NAME + " FROM " + TABLE_DONVI +
                " WHERE " + COLUMN_DONVI_NAME + " LIKE '%" + keyword + "%'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONVI_NAME));
                resultList.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return resultList;
    }

    public List<String> searchNhanVienByName(String keyword) {
        List<String> resultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_NHANVIEN_NAME + " FROM " + TABLE_NHANVIEN +
                " WHERE " + COLUMN_NHANVIEN_NAME + " LIKE '%" + keyword + "%'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NHANVIEN_NAME));
                resultList.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return resultList;
    }
    public String getTenDonVi(String maDonVi) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_DONVI_NAME};
        String selection = COLUMN_DONVI_ID + "=?";
        String[] selectionArgs = {maDonVi};
        Cursor cursor = db.query(TABLE_DONVI, columns, selection, selectionArgs, null, null, null);
        String tenDonVi = null;
        if (cursor != null && cursor.moveToFirst()) {
            tenDonVi = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONVI_NAME));
            cursor.close();
        }
        return tenDonVi;
    }
}
