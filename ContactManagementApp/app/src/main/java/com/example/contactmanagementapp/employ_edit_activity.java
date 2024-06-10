package com.example.contactmanagementapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class employ_edit_activity extends AppCompatActivity {

    ImageButton imgCloseEmployee;
    private ImageView imgUser;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final int IMAGE_FROM_GALLERY_CODE = 300;
    private static final int IMAGE_FROM_CAMERA_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    // img uri var
    Uri imageUri;
    private DatabaseHelper dbHelper;
    private int nhanvienId;
    private String tenNVBanDau, chucVuNVBanDau, emailNVBanDau, sdtNVBanDau, maDonViNVBanDau;
    Spinner spinnerPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employ_edit);

        dbHelper = new DatabaseHelper(this);

        EditText edtTenNV = findViewById(R.id.editTenEmployee);
        EditText edtChucVuNV = findViewById(R.id.editChucvu);
        EditText edtEmailNV = findViewById(R.id.editEmailEmployee);
        EditText edtSDTNV = findViewById(R.id.editSDTEmployee);
        spinnerPosition = findViewById(R.id.spinnerPosition);
        imgUser = findViewById(R.id.imgUser);
        ImageButton btnXong = findViewById(R.id.imgSaveEmployee);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nhanvienId = extras.getInt("NHANVIEN_ID");
            tenNVBanDau = extras.getString("NHANVIEN_NAME");
            chucVuNVBanDau = extras.getString("NHANVIEN_POSITION");
            emailNVBanDau = extras.getString("NHANVIEN_EMAIL");
            sdtNVBanDau = extras.getString("NHANVIEN_PHONE");
            maDonViNVBanDau = extras.getString("NHANVIEN_UNIT_ID");
            String avatarNV = extras.getString("NHANVIEN_AVATAR");

            edtTenNV.setText(tenNVBanDau != null ? tenNVBanDau : "");
            edtChucVuNV.setText(chucVuNVBanDau != null ? chucVuNVBanDau : "");
            edtEmailNV.setText(emailNVBanDau != null ? emailNVBanDau : "");
            edtSDTNV.setText(sdtNVBanDau != null ? sdtNVBanDau : "");

            if (!TextUtils.isEmpty(avatarNV)) {
                imageUri = Uri.parse(avatarNV);
                try {
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(employ_edit_activity.this.getContentResolver(), imageUri);
                    Bitmap resizedBitmap = resizeBitmap(originalBitmap, 100); // Set the desired width here
                    imgUser.setImageBitmap(resizedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Call loadUnitsIntoSpinner after setting maDonViNVBanDau
        loadUnitsIntoSpinner();

        btnXong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenNVmoi = edtTenNV.getText().toString().trim();
                String chucVuNVmoi = edtChucVuNV.getText().toString().trim();
                String emailNVmoi = edtEmailNV.getText().toString().trim();
                String sdtNVmoi = edtSDTNV.getText().toString().trim();
                String selectedUnit = (String) spinnerPosition.getSelectedItem();
                int maDonVimoi = Integer.parseInt(selectedUnit.split(" - ")[0]);
                String imagePath = imageUri != null ? imageUri.toString() : null;

                if (TextUtils.isEmpty(tenNVmoi)) {
                    edtTenNV.setError("Tên nhân viên không được để trống");
                    return;
                }
                if (TextUtils.isEmpty(emailNVmoi)) {
                    edtEmailNV.setError("Email không được để trống");
                    return;
                }
                if (TextUtils.isEmpty(sdtNVmoi)) {
                    edtSDTNV.setError("Số điện thoại không được để trống");
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();

                if (!sdtNVmoi.equals(sdtNVBanDau) && kiemTraTrungSDT(sdtNVmoi, db)) {
                    Toast.makeText(employ_edit_activity.this, "Số điện thoại đã tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_NHANVIEN_NAME, tenNVmoi);
                values.put(DatabaseHelper.COLUMN_NHANVIEN_EMAIL, emailNVmoi);
                values.put(DatabaseHelper.COLUMN_NHANVIEN_PHONE, sdtNVmoi);
                values.put(DatabaseHelper.COLUMN_NHANVIEN_UNIT_ID, maDonVimoi);
                values.put(DatabaseHelper.COLUMN_NHANVIEN_AVATAR, imagePath); // Lưu đường dẫn hình ảnh
                if (!TextUtils.isEmpty(chucVuNVmoi)) {
                    values.put(DatabaseHelper.COLUMN_NHANVIEN_POSITION, chucVuNVmoi);
                } else {
                    values.putNull(DatabaseHelper.COLUMN_NHANVIEN_POSITION); // Lưu giá trị null nếu không nhập website
                }

                int rowsAffected = db.update(DatabaseHelper.TABLE_NHANVIEN, values, DatabaseHelper.COLUMN_NHANVIEN_ID + " = ?", new String[]{String.valueOf(nhanvienId)});
                db.close();

                if (rowsAffected > 0) {
                    Toast.makeText(employ_edit_activity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("NHANVIEN_ID", nhanvienId);
                    resultIntent.putExtra("NHANVIEN_NAME", tenNVmoi);
                    resultIntent.putExtra("NHANVIEN_POSITION", chucVuNVmoi);
                    resultIntent.putExtra("NHANVIEN_EMAIL", emailNVmoi);
                    resultIntent.putExtra("NHANVIEN_PHONE", sdtNVmoi);
                    resultIntent.putExtra("NHANVIEN_AVATAR", imagePath);
                    resultIntent.putExtra("NHANVIEN_UNIT_ID", maDonVimoi);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(employ_edit_activity.this, "Cập nhật không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgCloseEmployee = findViewById(R.id.imgCloseEmployee);
        imgUser = findViewById(R.id.imgUser);
        // init permission
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        imgCloseEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // camera
        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 99);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int desiredWidth) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float scale = (float) desiredWidth / originalWidth;
        int newHeight = (int) (originalHeight * scale);
        return Bitmap.createScaledBitmap(bitmap, desiredWidth, newHeight, true);
    }

    private boolean kiemTraTrungSDT(String sdt, SQLiteDatabase db) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NHANVIEN +
                " WHERE " + DatabaseHelper.COLUMN_NHANVIEN_PHONE + "=? AND " + DatabaseHelper.COLUMN_NHANVIEN_ID + "!=?";
        String[] selectionArgs = {sdt, String.valueOf(nhanvienId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count > 0;
        }
        return false;
    }

    // employ_edit_activity.java
    private void loadUnitsIntoSpinner() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DONVI,
                new String[]{DatabaseHelper.COLUMN_DONVI_ID, DatabaseHelper.COLUMN_DONVI_NAME},
                null, null, null, null, null);

        ArrayList<String> unitList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_NAME));
            unitList.add(id + " - " + name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, unitList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);

        // Tìm vị trí của đơn vị trực thuộc ban đầu và đặt cho Spinner
        if (!TextUtils.isEmpty(maDonViNVBanDau)) {
            int position = findUnitPosition(maDonViNVBanDau);
            spinnerPosition.setSelection(position);
        }
    }

    private int findUnitPosition(String unit) {
        // Lấy danh sách đơn vị từ Spinner
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerPosition.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i);
            if (item != null && item.equals(unit)) {
                return i;
            }
        }
        return 0; // Trả về vị trí đầu tiên nếu không tìm thấy
    }

    private void showImagePickerDialog() {
        // option for dialog
        String options[] = {"Máy ảnh", "Thư viện"};
        // Alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set Title
        builder.setTitle("Chọn một lựa chọn");
        // set Items
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // handle item click
                if (which == 0) { // start from 0 index
                    // camera selected
                    if (!checkCameraPermission()) {
                        // request camera permission
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    // gallery selected
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        }).create().show();
    }

    private void pickFromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery, IMAGE_FROM_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Tiêu đề ảnh");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Chi tiết ảnh");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(camera, IMAGE_FROM_CAMERA_CODE);
    }

    // CHECK CAMERA PERMISSION
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    // REQUEST CAMERA PERMISSION
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_PERMISSION_CODE); // handle request permission on override method
    }

    // CHECK STORAGE PERMISSION
    private boolean checkStoragePermission() {
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result1;
    }

    // REQUEST STORAGE PERMISSION
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted && storageAccepted) {
                    pickFromCamera();
                } else {
                    Toast.makeText(this, "Quyền sử dụng máy ảnh bị từ chối", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Quyền sử dụng thư viện bị từ chối", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 100);
                imgUser.setImageBitmap(resizedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_FROM_GALLERY_CODE) {
                Uri selectedImageUri = data != null ? data.getData() : null;
                if (selectedImageUri != null) {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, selectedImageUri);
                        imgUser.setImageBitmap(rotatedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi khi chọn ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == IMAGE_FROM_CAMERA_CODE) {
                if (imageUri != null) {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, imageUri);
                        imgUser.setImageBitmap(rotatedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (android.os.Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }
}