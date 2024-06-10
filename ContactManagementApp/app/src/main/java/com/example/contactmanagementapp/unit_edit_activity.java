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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class unit_edit_activity extends AppCompatActivity {
    ImageButton imgCloseUnit;
    private ImageView imgDV;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final int IMAGE_FROM_GALLERY_CODE = 300;
    private static final int IMAGE_FROM_CAMERA_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    // img uri var
    Uri imageUri;
    private DatabaseHelper dbHelper;
    private int donviId;
    private String TenBanDau, SDTBanDau, EmailBanDau, WebsiteBanDau, DiaChiBanDau, MaChaBanDau;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unit_edit);

        dbHelper = new DatabaseHelper(this);
        // Ánh xạ các view trong layout
        EditText edtTenDV = findViewById(R.id.editTen);
        EditText edtEmailDV = findViewById(R.id.editEmail);
        EditText edtWebDV = findViewById(R.id.editWebsite);
        EditText edtDiaChiDV = findViewById(R.id.editDiachi);
        EditText edtSDTDV = findViewById(R.id.editSDT);
        EditText edtMaChaDV = findViewById(R.id.editDVC);
        imgDV = findViewById(R.id.imgLogo);
        ImageButton btnXong = findViewById(R.id.imgSave);
        Button btnXoa = findViewById(R.id.btnDeleteUnit);

        imgCloseUnit = findViewById(R.id.imgCloseUnit);
        imgDV = findViewById(R.id.imgLogo);
        // init permission
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Lấy dữ liệu từ Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            donviId = extras.getInt("DONVI_ID");
            TenBanDau = extras.getString("DONVI_NAME");
            EmailBanDau = extras.getString("DONVI_EMAIL");
            WebsiteBanDau = extras.getString("DONVI_WEBSITE");
            DiaChiBanDau = extras.getString("DONVI_ADDRESS");
            SDTBanDau = extras.getString("DONVI_PHONE");
            MaChaBanDau = extras.getString("DONVI_PARENT_ID");
            String logoDV = extras.getString("DONVI_LOGO");

            // Hiển thị thông tin của đơn vị ban đầu lên EditTexts
            edtTenDV.setText(TenBanDau != null ? TenBanDau : "");
            edtEmailDV.setText(EmailBanDau != null ? EmailBanDau : "");
            edtWebDV.setText(WebsiteBanDau != null ? WebsiteBanDau : "");
            edtDiaChiDV.setText(DiaChiBanDau != null ? DiaChiBanDau : "");
            edtSDTDV.setText(SDTBanDau != null ? SDTBanDau : "");
            edtMaChaDV.setText(MaChaBanDau != null ? MaChaBanDau : "");
        }

        // Xử lý sự kiện click nút xóa
        btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(unit_edit_activity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa đơn vị này không?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Xóa đơn vị khỏi SQLite
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                int rowsAffected = db.delete(DatabaseHelper.TABLE_DONVI, DatabaseHelper.COLUMN_DONVI_ID + " = ?", new String[]{String.valueOf(donviId)});
                                db.close();

                                if (rowsAffected > 0) {
                                    // Thông báo xóa thành công
                                    Toast.makeText(unit_edit_activity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();

                                    // Đặt kết quả là OK và đóng activity
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    // Thông báo xóa không thành công
                                    Toast.makeText(unit_edit_activity.this, "Xóa không thành công", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        // Xử lý sự kiện click nút cập nhật
        btnXong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy dữ liệu từ các EditText
                String tenDVmoi = edtTenDV.getText().toString().trim();
                String emailDVmoi = edtEmailDV.getText().toString().trim();
                String webDVmoi = edtWebDV.getText().toString().trim();
                String diaChiDVmoi = edtDiaChiDV.getText().toString().trim();
                String sdtDVmoi = edtSDTDV.getText().toString().trim();
                String maChaDVmoi = edtMaChaDV.getText().toString().trim();
                String imagePath = imageUri != null ? imageUri.toString() : null;

                // Kiểm tra điều kiện và báo lỗi nếu cần
                if (TextUtils.isEmpty(tenDVmoi)) {
                    edtTenDV.setError("Tên đơn vị không được để trống");
                    return;
                }
                if (TextUtils.isEmpty(emailDVmoi)) {
                    edtEmailDV.setError("Email không được để trống");
                    return;
                }
                if (TextUtils.isEmpty(diaChiDVmoi)) {
                    edtDiaChiDV.setError("Địa chỉ không được để trống");
                    return;
                }
                if (TextUtils.isEmpty(sdtDVmoi)) {
                    edtSDTDV.setError("Số điện thoại không được để trống");
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Kiểm tra trùng số điện thoại
                if (!sdtDVmoi.equals(SDTBanDau) && kiemTraTrungSDT(sdtDVmoi, db)) {
                    Toast.makeText(unit_edit_activity.this, "Số điện thoại đã tồn tại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cập nhật thông tin vào SQLite
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_DONVI_NAME, tenDVmoi);
                values.put(DatabaseHelper.COLUMN_DONVI_EMAIL, emailDVmoi);
                values.put(DatabaseHelper.COLUMN_DONVI_ADDRESS, diaChiDVmoi);
                values.put(DatabaseHelper.COLUMN_DONVI_PHONE, sdtDVmoi);
                values.put(DatabaseHelper.COLUMN_DONVI_LOGO, imagePath); // Lưu đường dẫn hình ảnh

                // Kiểm tra và thêm vào cơ sở dữ liệu nếu địa chỉ website không rỗng
                if (!TextUtils.isEmpty(webDVmoi)) {
                    values.put(DatabaseHelper.COLUMN_DONVI_WEBSITE, webDVmoi);
                } else {
                    values.putNull(DatabaseHelper.COLUMN_DONVI_WEBSITE); // Lưu giá trị null nếu không nhập website
                }

                // Kiểm tra và thêm vào cơ sở dữ liệu nếu mã cha không rỗng
                if (!TextUtils.isEmpty(maChaDVmoi)) {
                    values.put(DatabaseHelper.COLUMN_DONVI_PARENT_ID, maChaDVmoi);
                } else {
                    values.putNull(DatabaseHelper.COLUMN_DONVI_PARENT_ID); // Lưu giá trị null nếu không nhập mã cha
                }
                int rowsAffected = db.update(DatabaseHelper.TABLE_DONVI, values, DatabaseHelper.COLUMN_DONVI_ID + " = ?", new String[]{String.valueOf(donviId)});
                db.close();

                // Hiển thị thông báo và quay lại trang thông tin đơn vị
                if (rowsAffected > 0) {
                    Toast.makeText(unit_edit_activity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    // Create an Intent to return the updated data
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("DONVI_ID", donviId);
                    resultIntent.putExtra("DONVI_NAME", tenDVmoi);
                    resultIntent.putExtra("DONVI_EMAIL", emailDVmoi);
                    resultIntent.putExtra("DONVI_WEBSITE", webDVmoi);
                    resultIntent.putExtra("DONVI_ADDRESS", diaChiDVmoi);
                    resultIntent.putExtra("DONVI_PHONE", sdtDVmoi);
                    resultIntent.putExtra("DONVI_PARENT_ID", maChaDVmoi);
                    resultIntent.putExtra("DONVI_LOGO", imagePath);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(unit_edit_activity.this, "Cập nhật không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
        imgCloseUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // camera
        imgDV.setOnClickListener(new View.OnClickListener() {
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

    // Phương thức kiểm tra trùng số điện thoại
    private boolean kiemTraTrungSDT(String sdt, SQLiteDatabase db) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_DONVI +
                " WHERE " + DatabaseHelper.COLUMN_DONVI_PHONE + "=? AND " + DatabaseHelper.COLUMN_DONVI_ID + "!=?";
        String[] selectionArgs = {sdt, String.valueOf(donviId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count > 0;
        }
        return false;
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
                imgDV.setImageBitmap(resizedBitmap);
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
                        imgDV.setImageBitmap(rotatedBitmap);
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
                        imgDV.setImageBitmap(rotatedBitmap);
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