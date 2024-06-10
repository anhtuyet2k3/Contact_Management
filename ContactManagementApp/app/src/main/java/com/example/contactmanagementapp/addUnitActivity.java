package com.example.contactmanagementapp;

import android.Manifest;
import android.app.ActionBar;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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

public class addUnitActivity extends AppCompatActivity {
    ImageButton imgClose;
    ActionBar actionBar;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final int IMAGE_FROM_GALLERY_CODE = 300;
    private static final int IMAGE_FROM_CAMERA_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    // img uri var
    Uri imageUri;
    EditText editTenUnit, editSDTUnit, editEmailUnit, editWebsite, editDiachi, editDVC;
    private ImageView imgLogo;
    private ImageButton btnThem;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_unit);

        dbHelper = new DatabaseHelper(this);

        editTenUnit = findViewById(R.id.editTenUnit);
        editSDTUnit = findViewById(R.id.editSDTUnit);
        editEmailUnit = findViewById(R.id.editEmailUnit);
        editWebsite = findViewById(R.id.editWebsite);
        editDiachi = findViewById(R.id.editDiachi);
        editDVC = findViewById(R.id.editDVC);
        imgLogo = findViewById(R.id.imgLogo);
        btnThem = findViewById(R.id.imgSaveUnit);
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                themDonVi();
            }
        });
        // init permission
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        imgClose = findViewById(R.id.imgCloseUnit);
        imgLogo = findViewById(R.id.imgLogo);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // camera
        imgLogo.setOnClickListener(new View.OnClickListener() {
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
    private Bitmap resizeBitmap(Bitmap bitmap, int desiredWidth) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float scale = (float) desiredWidth / originalWidth;
        int newHeight = (int) (originalHeight * scale);
        return Bitmap.createScaledBitmap(bitmap, desiredWidth, newHeight, true);
    }
    private void themDonVi() {
        String tenDV = editTenUnit.getText().toString().trim();
        String emailDV = editEmailUnit.getText().toString().trim();
        String webDV = editWebsite.getText().toString().trim();
        String diaChiDV = editDiachi.getText().toString().trim();
        String sdtDV = editSDTUnit.getText().toString().trim();
        String maChaDV = editDVC.getText().toString().trim();
        String imagePath = imageUri != null ? imageUri.toString() : null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Kiểm tra điều kiện và báo lỗi nếu cần
        if (TextUtils.isEmpty(tenDV)) {
            editTenUnit.setError("Tên đơn vị không được để trống");
            return;
        }
        if (TextUtils.isEmpty(emailDV)) {
            editEmailUnit.setError("Email không được để trống");
            return;
        }
        if (TextUtils.isEmpty(diaChiDV)) {
            editDiachi.setError("Địa chỉ không được để trống");
            return;
        }
        if (TextUtils.isEmpty(sdtDV)) {
            editSDTUnit.setError("Số điện thoại không được để trống");
            return;
        }
        // Kiểm tra trùng số điện thoại
        if (kiemTraTrungSDT(sdtDV, db)) {
            Toast.makeText(this, "Số điện thoại đã tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DONVI_NAME, tenDV);
        values.put(DatabaseHelper.COLUMN_DONVI_EMAIL, emailDV);
        values.put(DatabaseHelper.COLUMN_DONVI_ADDRESS, diaChiDV);
        values.put(DatabaseHelper.COLUMN_DONVI_PHONE, sdtDV);
        values.put(DatabaseHelper.COLUMN_DONVI_LOGO, imagePath); // Lưu đường dẫn hình ảnh

        // Kiểm tra nếu địa chỉ website không rỗng, thêm vào cơ sở dữ liệu
        if (!TextUtils.isEmpty(webDV)) {
            values.put(DatabaseHelper.COLUMN_DONVI_WEBSITE, webDV);
        }

        // Kiểm tra nếu mã cha không rỗng, thêm vào cơ sở dữ liệu
        if (!TextUtils.isEmpty(maChaDV)) {
            values.put(DatabaseHelper.COLUMN_DONVI_PARENT_ID, maChaDV);
        }

        try {
            long newRowId = db.insert(DatabaseHelper.TABLE_DONVI, null, values);

            if (newRowId == -1) {
                Toast.makeText(this, "Lỗi khi thêm đơn vị", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Thêm đơn vị thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (SQLException e) {
            Toast.makeText(this, "Lỗi khi thêm đơn vị: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
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
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 100); // Set the desired width here
                imgLogo.setImageBitmap(resizedBitmap);
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
                        imgLogo.setImageBitmap(rotatedBitmap);
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
                        imgLogo.setImageBitmap(rotatedBitmap);
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
    private boolean kiemTraTrungSDT(String sdt, SQLiteDatabase db) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_DONVI +
                " WHERE " + DatabaseHelper.COLUMN_DONVI_PHONE + "=?";
        String[] selectionArgs = {sdt};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count > 0;
        }
        return false;
    }
}