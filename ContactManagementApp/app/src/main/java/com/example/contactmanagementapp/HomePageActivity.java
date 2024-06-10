package com.example.contactmanagementapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {

    private ListView lvEmployee, lvUnit;
    private EditText editSearchUnit, editSearchEmployee;
    private EmployeeAdapter employeeAdapter;
    private UnitAdapter unitAdapter;
    private ImageButton btnAddEmployee, btnAddUnit, btnSearchClearUnit, btnSearchClearEmployee;
    TabHost mytab;
    private DatabaseHelper dbHelper;
    private CustomAdapter donViAdapter;
    private CustomAdapter nhanVienAdapter;
    private ArrayList<Item> donViList;
    private ArrayList<Item> nhanVienList;
    private ArrayList<Item> originalDonViList;
    private ArrayList<Item> originalNhanVienList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_page_activity);


        // Xử lý tabHost
        mytab = findViewById(R.id.mytabhost);
        mytab.setup();
        // khai báo các Tab con (TabSpec)
        TabHost.TabSpec spec1, spec2;
        // ứng với mỗi tab con, thực hiện 4 công việc
        // nhân viên
        spec1 = mytab.newTabSpec("nhanvien");   // tạo mới tab
        spec1.setContent(R.id.tabNhanvien); // tham chiếu id tab con
        spec1.setIndicator("Nhân viên");
        mytab.addTab(spec1);
        // đơn vị
        spec2 = mytab.newTabSpec("donvi");   // tạo mới tab
        spec2.setContent(R.id.tabDonvi); // tham chiếu id tab con
        spec2.setIndicator("Đơn vị");
        mytab.addTab(spec2);


        lvEmployee = findViewById(R.id.lvEmployee);
        lvUnit = findViewById(R.id.lvUnit);
        btnAddEmployee = findViewById(R.id.btn_addEmployee);
        btnAddUnit = findViewById(R.id.btn_addUnit);
        editSearchEmployee = findViewById(R.id.editSearchEmployee);
        btnSearchClearEmployee = findViewById(R.id.btnSearchClearEmployee);
        editSearchUnit = findViewById(R.id.editSearchUnit);
        btnSearchClearUnit = findViewById(R.id.btnSearchClearUnit);

        dbHelper = new DatabaseHelper(this);
        donViList = new ArrayList<>();
        nhanVienList = new ArrayList<>();
        // Load dữ liệu
        loadDonViData();
        loadNhanVienData();

        // Khởi tạo adapter và set adapter cho ListView
        donViAdapter = new CustomAdapter(this, R.layout.item, donViList);
        lvUnit.setAdapter(donViAdapter);
        nhanVienAdapter = new CustomAdapter(this, R.layout.item, nhanVienList);
        lvEmployee.setAdapter(nhanVienAdapter);
        // Sao chép danh sách ban đầu để khôi phục khi xoá văn bản tìm kiếm
        originalDonViList = new ArrayList<>(donViList);
        originalNhanVienList = new ArrayList<>(nhanVienList);
        // Thiết lập tìm kiếm
        setupSearch();
        hideKeyboardOnStart();
        addEvent();
        lvUnit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Lấy ra database để truy vấn thông tin của đơn vị được chọn
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                // Lấy cursor trỏ đến dữ liệu của đơn vị được chọn
                Cursor cursor = db.query(DatabaseHelper.TABLE_DONVI, null, null, null, null, null, null);
                cursor.moveToPosition(position);
                // Lấy thông tin của đơn vị được chọn từ cursor
                int donviId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_EMAIL));
                String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_WEBSITE));
                String logo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_LOGO));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_ADDRESS));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_PHONE));
                int parentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_PARENT_ID));


                // Đóng kết nối đến cơ sở dữ liệu
                db.close();

                // Tạo Intent để chuyển sang TTDonViActivity và chuyển dữ liệu của đơn vị được chọn
                Intent intent = new Intent(HomePageActivity.this, UnitDetailActivity.class);
                intent.putExtra("DONVI_ID", donviId);
                intent.putExtra("DONVI_NAME", name);
                intent.putExtra("DONVI_EMAIL", email);
                intent.putExtra("DONVI_WEBSITE", website);
                intent.putExtra("DONVI_LOGO", logo);
                intent.putExtra("DONVI_ADDRESS", address);
                intent.putExtra("DONVI_PHONE", phone);
                intent.putExtra("DONVI_PARENT_ID", parentId);


                startActivityForResult(intent, 1);
            }
        });
        lvEmployee.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Lấy ra database để truy vấn thông tin của nhân viên được chọn
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                // Lấy cursor trỏ đến dữ liệu của nhân viên được chọn
                Cursor cursor = db.query(DatabaseHelper.TABLE_NHANVIEN, null, null, null, null, null, null);
                cursor.moveToPosition(position);

                // Lấy thông tin của nhân viên được chọn từ cursor
                int nhanvienId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_NAME));
                String chucVu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_POSITION));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_PHONE));
                String avatar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_AVATAR));
                String maDonVi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_UNIT_ID));
                // Đóng kết nối đến cơ sở dữ liệu
                db.close();

                // Tạo Intent để chuyển sang TTNhanvienActivity và chuyển dữ liệu của nhân viên được chọn
                Intent intent = new Intent(HomePageActivity.this, EmployeeDetailActivity.class);
                intent.putExtra("NHANVIEN_ID", nhanvienId);
                intent.putExtra("NHANVIEN_NAME", name);
                intent.putExtra("NHANVIEN_POSITION", chucVu);
                intent.putExtra("NHANVIEN_EMAIL", email);
                intent.putExtra("NHANVIEN_PHONE", phone);
                intent.putExtra("NHANVIEN_AVATAR", avatar);
                intent.putExtra("NHANVIEN_UNIT_ID", maDonVi);

                startActivityForResult(intent, 1);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    } private void setupSearch() {
        editSearchEmployee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchEmployee(s.toString());
                toggleClearButtonVisibility(editSearchEmployee, btnSearchClearEmployee);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editSearchUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUnit(s.toString());
                toggleClearButtonVisibility(editSearchUnit, btnSearchClearUnit);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnSearchClearEmployee.setOnClickListener(v -> {
            editSearchEmployee.setText("");
        });

        btnSearchClearUnit.setOnClickListener(v -> {
            editSearchUnit.setText("");
        });

    }
    private void toggleClearButtonVisibility(EditText editText, ImageButton clearButton) {
        if (editText.getText().toString().isEmpty()) {
            clearButton.setVisibility(View.GONE);
        } else {
            clearButton.setVisibility(View.VISIBLE);
        }
    }

    private void searchEmployee(String keyword) {
        List<Item> searchResults = new ArrayList<>();

        if (keyword.isEmpty()) {
            // Khôi phục danh sách ban đầu nếu từ khoá trống
            searchResults.addAll(originalNhanVienList);
        } else {
            List<String> nhanVienResults = dbHelper.searchNhanVienByName(keyword);

            for (String nhanVien : nhanVienResults) {
                searchResults.add(new Item(nhanVien, null));
            }
        }

        nhanVienAdapter.clear();
        nhanVienAdapter.addAll(searchResults);
        nhanVienAdapter.notifyDataSetChanged();
    }
    private void searchUnit(String keyword) {
        List<Item> searchResults = new ArrayList<>();

        if (keyword.isEmpty()) {
            // Khôi phục danh sách ban đầu nếu từ khoá trống
            searchResults.addAll(originalDonViList);
        } else {
            List<String> donViResults = dbHelper.searchDonViByName(keyword);

            for (String donVi : donViResults) {
                searchResults.add(new Item(donVi, null));
            }
        }

        donViAdapter.clear();
        donViAdapter.addAll(searchResults);
        donViAdapter.notifyDataSetChanged();
    }
    protected void onResume() {
        super.onResume();
        // Xóa dữ liệu cũ
        donViList.clear();
        nhanVienList.clear();
        // Load dữ liệu mới
        loadDonViData();
        loadNhanVienData();
        // Cập nhật lại adapter
        donViAdapter.notifyDataSetChanged();
        nhanVienAdapter.notifyDataSetChanged();
    }
    // Thêm phương thức onActivityResult để nhận kết quả từ TTDonviActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            donViList.clear();
            nhanVienList.clear();
            loadDonViData();
            loadNhanVienData();
            donViAdapter.notifyDataSetChanged();
            nhanVienAdapter.notifyDataSetChanged();
        }
    }
    private void loadDonViData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_DONVI_NAME,
                DatabaseHelper.COLUMN_DONVI_LOGO
        };
        Cursor cursor = db.query(DatabaseHelper.TABLE_DONVI, projection, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_NAME));
                    String logo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONVI_LOGO));
                    donViList.add(new Item(name, logo));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
    }

    private void loadNhanVienData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_NHANVIEN_NAME,
                DatabaseHelper.COLUMN_NHANVIEN_AVATAR
        };
        Cursor cursor = db.query(DatabaseHelper.TABLE_NHANVIEN, projection, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_NAME));
                    String avatar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NHANVIEN_AVATAR));
                    nhanVienList.add(new Item(name, avatar));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
    }
    private void hideKeyboardOnStart() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    private void addEvent() {

        // định nghĩa nút thêm nhân viên
        btnAddEmployee.setOnClickListener(v -> {
            Intent intentEmployee = new Intent(HomePageActivity.this, addEmployeeActivity.class);
            startActivity(intentEmployee);
        });
        // định nghĩa nút thêm đơn vị
        btnAddUnit.setOnClickListener(v -> {
            Intent intentUnit = new Intent(HomePageActivity.this, addUnitActivity.class);
            startActivity(intentUnit);
        });

//        // Thiết lập TextWatcher cho editSearchEmployee
//        editSearchEmployee.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // Không cần xử lý
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Kiểm tra nếu có văn bản trong EditText
//                if (s.length() > 0) {
//                    btnSearchClearEmployee.setVisibility(View.VISIBLE); // Hiển thị nút xóa
//                    employeeAdapter.getFilter().filter(s.toString());
//                } else {
//                    btnSearchClearEmployee.setVisibility(View.GONE); // Ẩn nút xóa
//                    employeeAdapter.getFilter().filter("");
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Không cần xử lý
//            }
//        });
//
//        // Thiết lập TextWatcher cho editSearchUnit
//        editSearchUnit.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // Không cần xử lý
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Kiểm tra nếu có văn bản trong EditText
//                if (s.length() > 0) {
//                    btnSearchClearUnit.setVisibility(View.VISIBLE); // Hiển thị nút xóa
//                    unitAdapter.getFilter().filter(s.toString());
//                } else {
//                    btnSearchClearUnit.setVisibility(View.GONE); // Ẩn nút xóa
//                    unitAdapter.getFilter().filter("");
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Không cần xử lý
//            }
//        });

//        // Thiết lập sự kiện nhấn cho btnSearchClear
//        btnSearchClearEmployee.setOnClickListener(v -> editSearchEmployee.setText("")); // Xóa văn bản trong EditText
//        btnSearchClearUnit.setOnClickListener(v -> editSearchUnit.setText("")); // Xóa văn bản trong EditText

        // Thiết lập sự kiện nhấn cho ListView nhân viên
        lvEmployee.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Employee selectedEmployee = (Employee) parent.getItemAtPosition(position);
                Intent intent = new Intent(HomePageActivity.this, EmployeeDetailActivity.class);
                intent.putExtra("employeeName", selectedEmployee.getName());
                intent.putExtra("employeeImage", selectedEmployee.getImageResource());
                startActivity(intent);
            }
        });

        // Thiết lập sự kiện nhấn cho ListView đơn vị
        lvUnit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Unit selectedUnit = (Unit) parent.getItemAtPosition(position);
                Intent intent = new Intent(HomePageActivity.this, UnitDetailActivity.class);
                intent.putExtra("unitName", selectedUnit.getName());
                intent.putExtra("unitLogo", selectedUnit.getImageUri());
                startActivity(intent);
            }
        });
    }
}