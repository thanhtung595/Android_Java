package com.example.AppBanTrangSucQuachVietAnh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Activity chỉnh sửa thông tin sản phẩm trang sức
 * Cho phép người dùng cập nhật thông tin và hình ảnh sản phẩm
 */
public class EditJewelryActivity extends AppCompatActivity {
    private static final String TAG = "EditJewelryActivity";
    private static final int PICK_IMAGE_REQUEST = 1;  // Mã yêu cầu chọn ảnh

    // Các thành phần giao diện
    private TextInputEditText editName;        // Ô nhập tên sản phẩm
    private TextInputEditText editDescription; // Ô nhập mô tả
    private TextInputEditText editPrice;       // Ô nhập giá
    private TextInputEditText editStock;       // Ô nhập số lượng
    private TextInputEditText editCategory;    // Ô nhập danh mục
    private ImageView imageView;               // Hiển thị hình ảnh
    private Button buttonSave;                 // Nút lưu
    private Button buttonSelectImage;          // Nút chọn ảnh

    // Các biến quản lý dữ liệu
    private DatabaseManager databaseManager;   // Quản lý database
    private byte[] imageBytes;                 // Dữ liệu hình ảnh
    private Jewelry jewelry;                   // Sản phẩm cần chỉnh sửa
    private int jewelryId;                     // ID sản phẩm
    private ProgressDialog progressDialog;     // Dialog loading

    /**
     * Khởi tạo Activity và các thành phần giao diện
     * @param savedInstanceState Trạng thái đã lưu của Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Thiết lập ngôn ngữ tiếng Việt
        Locale locale = new Locale("vi");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        
        setContentView(R.layout.activity_edit_jewelry);
        Log.d(TAG, "Khởi tạo EditJewelryActivity");

        // Kiểm tra quyền admin
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "");
        boolean isAdmin = "admin".equals(userRole);
        
        // Khởi tạo các thành phần
        initializeComponents();
        initToolbar();
        
        // Nếu không phải admin, disable tất cả các input và ẩn nút lưu
        if (!isAdmin) {
            disableInputs();
        }

        // Lấy dữ liệu sản phẩm từ Intent
        loadJewelryData();
    }

    /**
     * Khởi tạo các thành phần giao diện và đối tượng cần thiết
     */
    private void initializeComponents() {
        // Ánh xạ các view
        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        editPrice = findViewById(R.id.editPrice);
        editStock = findViewById(R.id.editStock);
        editCategory = findViewById(R.id.editCategory);
        imageView = findViewById(R.id.imageView);
        buttonSave = findViewById(R.id.buttonSave);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);

        // Khởi tạo database manager
        databaseManager = DatabaseManager.getInstance();

        // Khởi tạo progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        // Thiết lập sự kiện cho các nút
        setupEventListeners();
    }

    /**
     * Thiết lập sự kiện cho các nút
     */
    private void setupEventListeners() {
        // Sự kiện chọn ảnh
        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
        });

        // Sự kiện lưu sản phẩm
        buttonSave.setOnClickListener(v -> {
            if (validateInput()) {
                updateJewelry();
            }
        });
    }

    /**
     * Vô hiệu hóa các input và nút lưu
     */
    private void disableInputs() {
        editName.setEnabled(false);
        editDescription.setEnabled(false);
        editPrice.setEnabled(false);
        editStock.setEnabled(false);
        editCategory.setEnabled(false);
        buttonSelectImage.setEnabled(false);
        buttonSave.setVisibility(View.GONE);
    }

    /**
     * Tải dữ liệu sản phẩm từ Intent
     */
    private void loadJewelryData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("jewelry")) {
            jewelry = (Jewelry) intent.getSerializableExtra("jewelry");
            if (jewelry != null) {
                jewelryId = jewelry.getId();
                displayJewelryData();
            }
        }
    }

    /**
     * Hiển thị dữ liệu sản phẩm lên giao diện
     */
    private void displayJewelryData() {
        editName.setText(jewelry.getName());
        editDescription.setText(jewelry.getDescription());
        editPrice.setText(String.valueOf(jewelry.getPrice()));
        editStock.setText(String.valueOf(jewelry.getStock()));
        editCategory.setText(jewelry.getCategory());

        // Hiển thị hình ảnh
        if (jewelry.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(jewelry.getImage(), 0, jewelry.getImage().length);
            imageView.setImageBitmap(bitmap);
            imageBytes = jewelry.getImage();
        }
    }

    /**
     * Kiểm tra dữ liệu nhập vào
     * @return true nếu dữ liệu hợp lệ
     */
    private boolean validateInput() {
        if (editName.getText().toString().trim().isEmpty()) {
            editName.setError("Vui lòng nhập tên sản phẩm");
            return false;
        }
        if (editPrice.getText().toString().trim().isEmpty()) {
            editPrice.setError("Vui lòng nhập giá sản phẩm");
            return false;
        }
        if (editStock.getText().toString().trim().isEmpty()) {
            editStock.setError("Vui lòng nhập số lượng");
            return false;
        }
        return true;
    }

    /**
     * Cập nhật thông tin sản phẩm vào database
     */
    private void updateJewelry() {
        progressDialog.show();

        // Cập nhật thông tin sản phẩm
        jewelry.setName(editName.getText().toString().trim());
        jewelry.setDescription(editDescription.getText().toString().trim());
        jewelry.setPrice(Double.parseDouble(editPrice.getText().toString().trim()));
        jewelry.setStock(Integer.parseInt(editStock.getText().toString().trim()));
        jewelry.setCategory(editCategory.getText().toString().trim());
        if (imageBytes != null) {
            jewelry.setImage(imageBytes);
        }

        // Cập nhật vào database trong thread riêng
        new Thread(() -> {
            boolean success = databaseManager.updateJewelry(jewelry);
            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (success) {
                    Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Xử lý kết quả chọn ảnh
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Chuyển ảnh thành bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);

                // Chuyển bitmap thành byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imageBytes = stream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Lỗi xử lý ảnh: " + e.getMessage());
                Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Xử lý khi Activity bị hủy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa sản phẩm");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 