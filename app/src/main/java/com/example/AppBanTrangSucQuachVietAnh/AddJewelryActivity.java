package com.example.AppBanTrangSucQuachVietAnh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.app.ProgressDialog;

/**
 * Activity thêm mới sản phẩm trang sức
 * Cho phép người dùng nhập thông tin và hình ảnh sản phẩm mới
 */
public class AddJewelryActivity extends AppCompatActivity {
    private static final String TAG = "AddJewelryActivity";
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
    private int userId;                        // ID người dùng
    private ProgressDialog progressDialog;     // Dialog loading

    /**
     * Khởi tạo Activity và các thành phần giao diện
     * @param savedInstanceState Trạng thái đã lưu của Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_jewelry);
        Log.d(TAG, "Khởi tạo AddJewelryActivity");

        // Khởi tạo các thành phần
        initializeComponents();
        
        // Thiết lập sự kiện cho các nút
        setupEventListeners();
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

        // Lấy ID người dùng từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        // Khởi tạo progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
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
                saveJewelry();
            }
        });
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
        if (imageBytes == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Lưu sản phẩm mới vào database
     */
    private void saveJewelry() {
        progressDialog.show();

        // Tạo đối tượng Jewelry từ dữ liệu nhập
        Jewelry jewelry = new Jewelry();
        jewelry.setName(editName.getText().toString().trim());
        jewelry.setDescription(editDescription.getText().toString().trim());
        jewelry.setPrice(Double.parseDouble(editPrice.getText().toString().trim()));
        jewelry.setStock(Integer.parseInt(editStock.getText().toString().trim()));
        jewelry.setCategory(editCategory.getText().toString().trim());
        jewelry.setImage(imageBytes);
        jewelry.setCreatedBy(userId);

        // Lưu vào database trong thread riêng
        new Thread(() -> {
            boolean success = databaseManager.addJewelry(jewelry);
            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (success) {
                    Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi thêm sản phẩm", Toast.LENGTH_SHORT).show();
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
            getSupportActionBar().setTitle("Thêm sản phẩm mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 