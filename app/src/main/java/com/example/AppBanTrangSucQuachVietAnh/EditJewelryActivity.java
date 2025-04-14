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
import java.io.InputStream;
import java.util.Locale;

/**
 * Activity chỉnh sửa thông tin sản phẩm trang sức
 * Cho phép người dùng cập nhật thông tin và hình ảnh sản phẩm
 */
public class EditJewelryActivity extends AppCompatActivity {
    private static final String TAG = "EditJewelryActivity";
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
    private Jewelry jewelry;
    private int jewelryId;
    private ProgressDialog progressDialog;

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
        
        // Khởi tạo các thành phần giao diện
        initViews();
        initToolbar();
        
        // Nếu không phải admin, disable tất cả các input và ẩn nút lưu
        if (!isAdmin) {
            editName.setEnabled(false);
            editDescription.setEnabled(false);
            editPrice.setEnabled(false);
            editStock.setEnabled(false);
            editCategory.setEnabled(false);
            buttonSelectImage.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
            Toast.makeText(this, "Bạn không có quyền chỉnh sửa sản phẩm", Toast.LENGTH_SHORT).show();
        }
        
        // Khởi tạo DatabaseManager
        databaseManager = DatabaseManager.getInstance();
        
        // Lấy thông tin sản phẩm từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("jewelry")) {
            jewelry = (Jewelry) intent.getSerializableExtra("jewelry");
            if (jewelry != null) {
                jewelryId = jewelry.getId();
                Log.d(TAG, "Nhận dữ liệu sản phẩm với ID: " + jewelryId);
                populateFields(jewelry);
            } else {
                Log.e(TAG, "Nhận được đối tượng jewelry null");
                Toast.makeText(this, "Dữ liệu sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e(TAG, "Không có dữ liệu sản phẩm trong intent");
            Toast.makeText(this, "Không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Thiết lập xử lý sự kiện click chỉ khi là admin
        if (isAdmin) {
            buttonSelectImage.setOnClickListener(v -> {
                Intent imageIntent = new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
            });

            buttonSave.setOnClickListener(v -> saveJewelry());
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
            buttonSave = findViewById(R.id.buttonSave);
            buttonSelectImage = findViewById(R.id.buttonSelectImage);

            // Thiết lập toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chỉnh sửa sản phẩm");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            Log.d(TAG, "Đã khởi tạo các view thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo view: " + e.getMessage(), e);
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

    private void populateFields(Jewelry jewelry) {
        try {
            editName.setText(jewelry.getName());
            editDescription.setText(jewelry.getDescription());
            editPrice.setText(String.valueOf(jewelry.getPrice()));
            editStock.setText(String.valueOf(jewelry.getStock()));
            editCategory.setText(jewelry.getCategory());
            
            // Set image if available
            if (jewelry.getImage() != null && jewelry.getImage().length > 0) {
                Log.d(TAG, "Setting image from database, size: " + jewelry.getImage().length + " bytes");
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(jewelry.getImage(), 0, jewelry.getImage().length);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        imageView.setBackgroundResource(0); // Remove background
                        imageBytes = jewelry.getImage();
                        Log.d(TAG, "Image set successfully, size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    } else {
                        Log.e(TAG, "Failed to decode image from database");
                        imageView.setImageResource(R.drawable.ic_jewelry);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error setting image: " + e.getMessage(), e);
                    imageView.setImageResource(R.drawable.ic_jewelry);
                }
            } else {
                Log.d(TAG, "No image data available");
                imageView.setImageResource(R.drawable.ic_jewelry);
            }
            
            Log.d(TAG, "Fields populated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error populating fields: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi hiển thị dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xử lý kết quả chọn ảnh
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                // Lấy URI của ảnh đã chọn
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    // Hiển thị ảnh đã chọn
                    imageView.setImageURI(imageUri);
                    
                    // Chuyển ảnh thành mảng byte
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    imageBytes = getBytes(inputStream);
                    Log.d(TAG, "Đã chọn ảnh mới: " + imageBytes.length + " bytes");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xử lý ảnh đã chọn: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Chuyển InputStream thành mảng byte
     */
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void showToast(String message) {
        try {
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            View view = toast.getView();
            if (view != null) {
                TextView tv = view.findViewById(android.R.id.message);
                if (tv != null) {
                    tv.setTextSize(14);
                    tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                }
            }
            toast.show();
        } catch (Exception e) {
            // Fallback to simple toast if customization fails
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInput() {
        String name = editName.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String stockStr = editStock.getText().toString().trim();
        String category = editCategory.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Vui lòng nhập tên sản phẩm");
            showToast("Vui lòng nhập tên sản phẩm");
            return false;
        }

        if (description.isEmpty()) {
            editDescription.setError("Vui lòng nhập mô tả");
            showToast("Vui lòng nhập mô tả sản phẩm");
            return false;
        }

        if (priceStr.isEmpty()) {
            editPrice.setError("Vui lòng nhập giá");
            showToast("Vui lòng nhập giá sản phẩm");
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                editPrice.setError("Giá phải lớn hơn 0");
                showToast("Giá sản phẩm phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException e) {
            editPrice.setError("Giá không hợp lệ");
            showToast("Giá sản phẩm không hợp lệ");
            return false;
        }

        if (stockStr.isEmpty()) {
            editStock.setError("Vui lòng nhập số lượng");
            showToast("Vui lòng nhập số lượng sản phẩm");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                editStock.setError("Số lượng không được âm");
                showToast("Số lượng sản phẩm không được âm");
                return false;
            }
        } catch (NumberFormatException e) {
            editStock.setError("Số lượng không hợp lệ");
            showToast("Số lượng sản phẩm không hợp lệ");
            return false;
        }

        if (category.isEmpty()) {
            editCategory.setError("Vui lòng nhập loại sản phẩm");
            showToast("Vui lòng nhập loại sản phẩm");
            return false;
        }

        return true;
    }

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
            showToast("Không thể xác định người dùng");
            return;
        }

        try {
            // Cập nhật thông tin cho đối tượng jewelry hiện tại
            jewelry.setName(editName.getText().toString().trim());
            jewelry.setDescription(editDescription.getText().toString().trim());
            jewelry.setPrice(Double.parseDouble(editPrice.getText().toString().trim()));
            jewelry.setStock(Integer.parseInt(editStock.getText().toString().trim()));
            jewelry.setCategory(editCategory.getText().toString().trim());
            
            // Cập nhật ảnh nếu có chọn ảnh mới
            if (imageBytes != null) {
                jewelry.setImage(imageBytes);
            }

            Log.d(TAG, "Updated Jewelry object: " + jewelry.toString());

            // Show progress dialog
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang cập nhật sản phẩm...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Save to database in background thread
            new Thread(() -> {
                try {
                    Log.d(TAG, "Attempting to update jewelry in database...");
                    boolean success = databaseManager.updateJewelry(jewelry);
                    
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (success) {
                            Log.i(TAG, "Jewelry updated successfully");
                            showToast("Cập nhật sản phẩm thành công");
                            finish();
                        } else {
                            Log.e(TAG, "Failed to update jewelry");
                            showToast("Cập nhật sản phẩm thất bại");
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error updating jewelry: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showToast("Lỗi: " + e.getMessage());
                    });
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Error preparing jewelry data: " + e.getMessage(), e);
            showToast("Lỗi chuẩn bị dữ liệu: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Ẩn dialog tiến trình
     */
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
} 