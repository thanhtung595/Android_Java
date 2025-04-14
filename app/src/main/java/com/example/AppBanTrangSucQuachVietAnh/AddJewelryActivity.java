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
import java.io.IOException;
import android.app.ProgressDialog;

/**
 * Activity thêm mới sản phẩm trang sức
 * Cho phép người dùng nhập thông tin và hình ảnh sản phẩm mới
 */
public class AddJewelryActivity extends AppCompatActivity {
    private static final String TAG = "AddJewelryActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText editName;
    private TextInputEditText editDescription;
    private TextInputEditText editPrice;
    private TextInputEditText editStock;
    private TextInputEditText editCategory;
    private ImageView imageView;
    private Button buttonSave;
    private Button buttonSelectImage;
    private DatabaseManager databaseManager;
    private byte[] imageBytes;
    private int userId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_jewelry);
        Log.d(TAG, "Khởi tạo AddJewelryActivity");

        try {
            // Lấy instance của DatabaseManager
            databaseManager = DatabaseManager.getInstance();

            // Khởi tạo toolbar
            initToolbar();

            // Khởi tạo views
            initViews();

            // Lấy user ID từ SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userId = preferences.getInt("user_id", -1);

            // Xử lý sự kiện chọn ảnh
            buttonSelectImage.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
            });

            // Xử lý sự kiện lưu
            buttonSave.setOnClickListener(v -> saveJewelry());

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error initializing AddJewelryActivity", e);
            Toast.makeText(this, "Khởi tạo màn hình thất bại", Toast.LENGTH_SHORT).show();
            finish();
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

    /**
     * Khởi tạo các thành phần giao diện
     */
    private void initViews() {
        try {
            // Ánh xạ các view
            editName = findViewById(R.id.editName);
            editDescription = findViewById(R.id.editDescription);
            editPrice = findViewById(R.id.editPrice);
            editStock = findViewById(R.id.editStock);
            editCategory = findViewById(R.id.editCategory);
            imageView = findViewById(R.id.imageView);
            buttonSelectImage = findViewById(R.id.buttonSelectImage);
            buttonSave = findViewById(R.id.buttonSave);

            Log.d(TAG, "Đã khởi tạo các view thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo view: " + e.getMessage(), e);
        }
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                imageBytes = DatabaseManager.bitmapToByteArray(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput() {
        String name = editName.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String stockStr = editStock.getText().toString().trim();
        String category = editCategory.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || category.isEmpty()) {
            Log.e(TAG, "Missing required fields");
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (imageBytes == null) {
            Log.e(TAG, "No image selected");
            Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
            
            if (price <= 0) {
                Log.e(TAG, "Invalid price: " + price);
                Toast.makeText(this, "Giá phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (stock < 0) {
                Log.e(TAG, "Invalid stock: " + stock);
                Toast.makeText(this, "Số lượng không thể âm", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing numbers: " + e.getMessage());
            Toast.makeText(this, "Giá và số lượng phải là số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Lưu thông tin sản phẩm mới
     */
    private void saveJewelry() {
        Log.d(TAG, "Starting saveJewelry process...");
        
        // Validate input
        if (!validateInput()) {
            Log.e(TAG, "Input validation failed");
            return;
        }

        // Get user ID from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = 1;
        Log.d(TAG, "User ID from preferences: " + userId);

        if (userId == -1) {
            Log.e(TAG, "Invalid user ID");
            Toast.makeText(this, "Không thể xác định người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Jewelry object with constructor
        Jewelry jewelry = new Jewelry(
            editName.getText().toString().trim(),
            editDescription.getText().toString().trim(),
            Double.parseDouble(editPrice.getText().toString().trim()),
            Integer.parseInt(editStock.getText().toString().trim()),
            editCategory.getText().toString().trim()
        );
        jewelry.setImage(imageBytes);
        jewelry.setCreatedBy(userId);

        Log.d(TAG, "Created Jewelry object: " + jewelry.toString());

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu sản phẩm...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Save to database in background thread
        new Thread(() -> {
            try {
                Log.d(TAG, "Attempting to save jewelry to database...");
                boolean success = databaseManager.addJewelry(jewelry);
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (success) {
                        Log.i(TAG, "Jewelry saved successfully");
                        Toast.makeText(AddJewelryActivity.this, "Lưu sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, "Failed to save jewelry");
                        Toast.makeText(AddJewelryActivity.this, "Lưu sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error saving jewelry: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddJewelryActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 