package com.example.appbanbanhnguyenhaidang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appbanbanhnguyenhaidang.adapter.ProductAdapter;
import com.example.appbanbanhnguyenhaidang.database.DatabaseHelper;
import com.example.appbanbanhnguyenhaidang.model.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductListActivity - Hiển thị danh sách sản phẩm và xử lý các thao tác CRUD
 */
public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private List<Product> productList;
    private DatabaseHelper databaseHelper;
    private boolean isAdmin;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Khởi tạo toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Danh sách sản phẩm");

        // Khởi tạo database helper
        databaseHelper = new DatabaseHelper(this);

        // Lấy thông tin đăng nhập từ SharedPreferences
        preferences = getSharedPreferences("login_pref", MODE_PRIVATE);
        isAdmin = "admin".equals(preferences.getString("role", ""));

        // Khởi tạo RecyclerView
        rvProducts = findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList, this, isAdmin);
        rvProducts.setAdapter(adapter);

        // Nút thêm sản phẩm
        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);
        fabAddProduct.setOnClickListener(v -> {
            if (isAdmin) {
                startActivity(new Intent(this, AddEditProductActivity.class));
            } else {
                Toast.makeText(this, "Bạn không có quyền thêm sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị danh sách sản phẩm
        loadProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // Xóa thông tin đăng nhập
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Tải danh sách sản phẩm từ database
     */
    private void loadProducts() {
        databaseHelper.getAllProducts(new DatabaseHelper.OnProductsResultListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                runOnUiThread(() -> {
                    productList.clear();
                    productList.addAll(products);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onProductsError(String error) {
                runOnUiThread(() -> Toast.makeText(ProductListActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onItemClick(Product product) {
        // Xử lý khi click vào sản phẩm
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Product product) {
        if (isAdmin) {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Bạn không có quyền sửa sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(Product product) {
        if (isAdmin) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    databaseHelper.deleteProduct(product.getId(), new DatabaseHelper.OnOperationResultListener() {
                        @Override
                        public void onOperationSuccess(String message) {
                            runOnUiThread(() -> {
                                Toast.makeText(ProductListActivity.this, message, Toast.LENGTH_SHORT).show();
                                loadProducts();
                            });
                        }

                        @Override
                        public void onOperationFailed(String error) {
                            runOnUiThread(() -> Toast.makeText(ProductListActivity.this, error, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
        } else {
            Toast.makeText(this, "Bạn không có quyền xóa sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
} 