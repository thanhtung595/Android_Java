package com.example.appbanbanhnguyenhaidang;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appbanbanhnguyenhaidang.database.DatabaseHelper;
import com.example.appbanbanhnguyenhaidang.model.Product;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * AddEditProductActivity - Xử lý thêm mới và chỉnh sửa sản phẩm
 */
public class AddEditProductActivity extends AppCompatActivity {
    private static final String TAG = "AddEditProductActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText editTextName, editTextDescription, editTextPrice;
    private ImageView imageViewProduct;
    private Button buttonSave, buttonChooseImage;
    private DatabaseHelper databaseHelper;
    private Product product;
    private byte[] selectedImageBytes;
    private int productId = -1;
    private Product existingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = new DatabaseHelper(this);

        editTextName = findViewById(R.id.editTextName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        buttonSave = findViewById(R.id.buttonSave);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);

        productId = getIntent().getIntExtra("product_id", -1);
        if (productId != -1) {
            getSupportActionBar().setTitle("Sửa sản phẩm");
            loadProduct(productId);
        } else {
            getSupportActionBar().setTitle("Thêm sản phẩm");
        }

        buttonChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
        });

        buttonSave.setOnClickListener(v -> saveProduct());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewProduct.setImageBitmap(bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                selectedImageBytes = stream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi tải ảnh", e);
                runOnUiThread(() -> Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void loadProduct(int productId) {
        databaseHelper.getProductById(productId, new DatabaseHelper.OnProductResultListener() {
            @Override
            public void onProductLoaded(Product loadedProduct) {
                if (loadedProduct != null) {
                    existingProduct = loadedProduct;
                    product = loadedProduct;
                    runOnUiThread(() -> populateFields(loadedProduct));
                }
            }

            @Override
            public void onProductError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(AddEditProductActivity.this, error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void populateFields(Product product) {
        editTextName.setText(product.getName());
        editTextDescription.setText(product.getDescription());
        editTextPrice.setText(String.valueOf(product.getPrice()));

        byte[] imageBytes = product.getImage();
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageViewProduct.setImageBitmap(bitmap);
            selectedImageBytes = imageBytes;
        }
    }

    private void saveProduct() {
        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (product == null) {
            product = new Product(0, name, description, price, selectedImageBytes != null ? selectedImageBytes : new byte[0]);
            addProduct(product);
        } else {
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            if (selectedImageBytes != null) {
                product.setImage(selectedImageBytes);
            }
            updateProduct(product);
        }
    }

    private void addProduct(Product product) {
        databaseHelper.addProduct(product, new DatabaseHelper.DatabaseCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditProductActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(AddEditProductActivity.this, error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void updateProduct(Product product) {
        databaseHelper.updateProduct(product, new DatabaseHelper.DatabaseCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditProductActivity.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(AddEditProductActivity.this, error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
