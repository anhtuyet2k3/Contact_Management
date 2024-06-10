package com.example.contactmanagementapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class EmployeeDetailActivity extends AppCompatActivity {
    ImageButton btnBackEmployee, btnEditEmployee;
    private DatabaseHelper dbHelper;
    private static final int REQUEST_EDIT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_detail);

        dbHelper = new DatabaseHelper(this);
        TextView txtTenNV = findViewById(R.id.txt_nameEmployee);
        TextView txtEmailNV = findViewById(R.id.editEmailEmployee);
        TextView txtSDTNV = findViewById(R.id.editSDTEmployee);
        TextView txtChucVuNV = findViewById(R.id.editChucvu);
        TextView txtMaDonViNV = findViewById(R.id.editDonvi);
        ImageView anhNV = findViewById(R.id.imgUser);
        ImageButton btnGoi = findViewById(R.id.btnCallEmployee);
        ImageButton btnNhanTin = findViewById(R.id.btnCommentEmployee);
        Button btnXoa = findViewById(R.id.btnDeleteUnit);

        btnBackEmployee = findViewById(R.id.btnBackEmployee);
        btnEditEmployee = findViewById(R.id.btnEditEmployee);
        btnBackEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Lấy dữ liệu từ Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int nhanvienId = extras.getInt("NHANVIEN_ID");
            String tenNV = extras.getString("NHANVIEN_NAME");
            String emailNV = extras.getString("NHANVIEN_EMAIL");
            String sdtNV = extras.getString("NHANVIEN_PHONE");
            String chucVuNV = extras.getString("NHANVIEN_POSITION");
            String maDonViNV = extras.getString("NHANVIEN_UNIT_ID");
            String avatarNV = extras.getString("NHANVIEN_AVATAR");

            // Hiển thị thông tin chi tiết của nhân viên
            txtTenNV.setText(tenNV != null ? tenNV : "Không có");
            txtEmailNV.setText(emailNV != null ? emailNV : "Không có");
            txtSDTNV.setText(sdtNV != null ? sdtNV : "Không có");
            txtChucVuNV.setText(chucVuNV != null ? chucVuNV : "Không có");
            txtMaDonViNV.setText(maDonViNV != null ? maDonViNV : "Không có");
            // Lấy tên đơn vị từ mã đơn vị
            String tenDonVi = dbHelper.getTenDonVi(maDonViNV);
            txtMaDonViNV.setText(tenDonVi != null ? tenDonVi : "Không có");
            if (avatarNV != null && !avatarNV.isEmpty()) {
                Picasso.get().load(avatarNV).into(anhNV);
            } else {
                // Set a default image if avatar is not available
                anhNV.setImageResource(R.drawable.ic_user);
            }

            // Xử lý sự kiện click nút gọi
            btnGoi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sdtNV != null && !sdtNV.isEmpty()) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + sdtNV));

                        if (ContextCompat.checkSelfPermission(EmployeeDetailActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(EmployeeDetailActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        } else {
                            startActivity(callIntent);
                        }
                    }
                }
            });

            // Xử lý sự kiện click nút nhắn tin
            btnNhanTin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sdtNV != null && !sdtNV.isEmpty()) {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", sdtNV, null));
                        startActivity(smsIntent);
                    }
                }
            });
            // Xử lý sự kiện click nút xóa
            btnXoa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(EmployeeDetailActivity.this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa nhân viên này không?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Xóa đơn vị khỏi SQLite
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    int rowsAffected = db.delete(DatabaseHelper.TABLE_NHANVIEN, DatabaseHelper.COLUMN_NHANVIEN_ID + " = ?", new String[]{String.valueOf(nhanvienId)});
                                    db.close();

                                    if (rowsAffected > 0) {
                                        // Thông báo xóa thành công
                                        Toast.makeText(EmployeeDetailActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();

                                        // Đóng activity sau khi xóa
                                        finish();
                                    } else {
                                        // Thông báo xóa không thành công
                                        Toast.makeText(EmployeeDetailActivity.this, "Xóa không thành công", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
            // Xử lý sự kiện click nút sửa
            btnEditEmployee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EmployeeDetailActivity.this, employ_edit_activity.class);
                    intent.putExtra("NHANVIEN_ID", nhanvienId);
                    intent.putExtra("NHANVIEN_NAME", tenNV);
                    intent.putExtra("NHANVIEN_POSITION", chucVuNV);
                    intent.putExtra("NHANVIEN_EMAIL", emailNV);
                    intent.putExtra("NHANVIEN_PHONE", sdtNV);
                    intent.putExtra("NHANVIEN_AVATAR", avatarNV);
                    intent.putExtra("NHANVIEN_UNIT_ID", maDonViNV);
                    startActivityForResult(intent, REQUEST_EDIT);
                }
            });

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            if (data != null) {
                int nhanvienId = data.getIntExtra("NHANVIEN_ID", -1);
                String tenNV = data.getStringExtra("NHANVIEN_NAME");
                String emailNV = data.getStringExtra("NHANVIEN_EMAIL");
                String chucVuNV = data.getStringExtra("NHANVIEN_POSITION");
                String sdtNV = data.getStringExtra("NHANVIEN_PHONE");
                String maDonViNV = data.getStringExtra("NHANVIEN_UNIT_ID");
                String avatarNV = data.getStringExtra("NHANVIEN_AVATAR");
                // Ánh xạ các view trong layout
                TextView txtTenNV = findViewById(R.id.txt_nameEmployee);
                TextView txtEmailNV = findViewById(R.id.editEmailEmployee);
                TextView txtSDTNV = findViewById(R.id.editSDTEmployee);
                TextView txtChucVuNV = findViewById(R.id.editChucvu);
                TextView txtMaDonViNV = findViewById(R.id.editDonvi);
                ImageView anhNV = findViewById(R.id.imgUser);
                // Hiển thị thông tin cập nhật
                txtTenNV.setText(tenNV);
                txtEmailNV.setText(emailNV);
                txtSDTNV.setText(sdtNV);
                txtChucVuNV.setText(chucVuNV);
                txtMaDonViNV.setText(maDonViNV);
                if (!TextUtils.isEmpty(avatarNV)) {
                    Uri imageUri = Uri.parse(avatarNV);
                    try {
                        Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        Bitmap resizedBitmap = resizeBitmap(originalBitmap, 100); // Set the desired width here
                        anhNV.setImageBitmap(resizedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Helper method to resize a bitmap
    private Bitmap resizeBitmap(Bitmap originalBitmap, int desiredWidth) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        float aspectRatio = (float) originalHeight / originalWidth;
        int desiredHeight = Math.round(desiredWidth * aspectRatio);
        return Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, false);
    }
}