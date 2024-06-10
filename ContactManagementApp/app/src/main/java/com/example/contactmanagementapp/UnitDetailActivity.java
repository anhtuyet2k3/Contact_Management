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

public class UnitDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private static final int REQUEST_EDIT = 1;
    ImageButton btnBackUnit, btnEditUnit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unit_detail);

        btnBackUnit = findViewById(R.id.btnBackUnit);
        btnEditUnit = findViewById(R.id.btnEditUnit);
        btnBackUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEditUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UnitDetailActivity.this, unit_edit_activity.class);
                startActivity(intent);
            }
        });

        dbHelper = new DatabaseHelper(this);
        TextView txtTenDV = findViewById(R.id.txt_nameUnit);
        TextView txtEmailDV = findViewById(R.id.txtEmailUnit);
        TextView txtWebDV = findViewById(R.id.txtWebsite);
        TextView txtDiaChiDV = findViewById(R.id.txtDiachi);
        TextView txtSDTDV = findViewById(R.id.txtSDTUnit);
        TextView txtMaChaDV = findViewById(R.id.txtDonviCha);
        ImageView imgDV = findViewById(R.id.imgLogo);
        ImageButton btnSua = findViewById(R.id.btnEditUnit);
        Button btnXoa = findViewById(R.id.btnDeleteUnit);
        ImageButton btnGoi = findViewById(R.id.btnCallUnit);
        ImageButton btnNhanTin = findViewById(R.id.btnCommentUnit);
        // Lấy dữ liệu từ Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int donviId = extras.getInt("DONVI_ID");
            String tenDV = extras.getString("DONVI_NAME");
            String emailDV = extras.getString("DONVI_EMAIL");
            String webDV = extras.getString("DONVI_WEBSITE");
            String diaChiDV = extras.getString("DONVI_ADDRESS");
            String sdtDV = extras.getString("DONVI_PHONE");
            String maChaDV = extras.getString("DONVI_PARENT_ID");
            String logoDV = extras.getString("DONVI_LOGO");

            // Hiển thị thông tin của đơn vị
            txtTenDV.setText(tenDV != null ? tenDV : "Không có");
            txtEmailDV.setText(emailDV != null ? emailDV : "Không có");
            txtWebDV.setText(webDV != null ? webDV : "Không có");
            txtDiaChiDV.setText(diaChiDV != null ? diaChiDV : "Không có");
            txtSDTDV.setText(sdtDV != null ? sdtDV : "Không có");
            txtMaChaDV.setText(maChaDV != null && !maChaDV.isEmpty() ? maChaDV : "Không có");

            if (logoDV != null && !logoDV.isEmpty()) {
                Picasso.get().load(logoDV).into(imgDV);
            } else {
                imgDV.setImageResource(R.drawable.ic_user);
            }

            // Xử lý sự kiện click nút gọi
            btnGoi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sdtDV != null && !sdtDV.isEmpty()) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + sdtDV));

                        if (ContextCompat.checkSelfPermission(UnitDetailActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(UnitDetailActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
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
                    if (sdtDV != null && !sdtDV.isEmpty()) {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", sdtDV, null));
                        startActivity(smsIntent);
                    }
                }
            });

            // Xử lý sự kiện click nút xóa
            btnXoa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(UnitDetailActivity.this)
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
                                        Toast.makeText(UnitDetailActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();

                                        // Đóng activity sau khi xóa
                                        finish();
                                    } else {
                                        // Thông báo xóa không thành công
                                        Toast.makeText(UnitDetailActivity.this, "Xóa không thành công", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

            // Xử lý sự kiện click nút sửa
            btnSua.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UnitDetailActivity.this, unit_edit_activity.class);
                    intent.putExtra("DONVI_ID", donviId);
                    intent.putExtra("DONVI_NAME", tenDV);
                    intent.putExtra("DONVI_EMAIL", emailDV);
                    intent.putExtra("DONVI_WEBSITE", webDV);
                    intent.putExtra("DONVI_ADDRESS", diaChiDV);
                    intent.putExtra("DONVI_PHONE", sdtDV);
                    intent.putExtra("DONVI_PARENT_ID", maChaDV);
                    intent.putExtra("DONVI_LOGO", logoDV);
                    startActivityForResult(intent, REQUEST_EDIT);
                }
            });

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            if (data != null) {
                int donviId = data.getIntExtra("DONVI_ID", -1);
                String tenDV = data.getStringExtra("DONVI_NAME");
                String emailDV = data.getStringExtra("DONVI_EMAIL");
                String webDV = data.getStringExtra("DONVI_WEBSITE");
                String diaChiDV = data.getStringExtra("DONVI_ADDRESS");
                String sdtDV = data.getStringExtra("DONVI_PHONE");
                String maChaDV = data.getStringExtra("DONVI_PARENT_ID");
                String logoDV = data.getStringExtra("DONVI_LOGO");

                // Ánh xạ các view trong layout
                TextView txtTenDV = findViewById(R.id.txt_nameUnit);
                TextView txtEmailDV = findViewById(R.id.txtEmailUnit);
                TextView txtWebDV = findViewById(R.id.txtWebsite);
                TextView txtDiaChiDV = findViewById(R.id.txtDiachi);
                TextView txtSDTDV = findViewById(R.id.txtSDTUnit);
                TextView txtMaChaDV = findViewById(R.id.txtDonviCha);
                ImageView imgDV = findViewById(R.id.imgLogo);

                // Hiển thị thông tin cập nhật của đơn vị
                txtTenDV.setText(tenDV);
                txtEmailDV.setText(emailDV);
                txtWebDV.setText(webDV);
                txtDiaChiDV.setText(diaChiDV);
                txtSDTDV.setText(sdtDV);
                txtMaChaDV.setText(maChaDV);
                if (!TextUtils.isEmpty(logoDV)) {
                    Uri imageUri = Uri.parse(logoDV);
                    try {
                        Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        Bitmap resizedBitmap = resizeBitmap(originalBitmap, 100); // Set the desired width here
                        imgDV.setImageBitmap(resizedBitmap);
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