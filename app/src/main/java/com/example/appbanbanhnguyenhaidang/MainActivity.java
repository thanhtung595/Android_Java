package com.example.appbanbanhnguyenhaidang;

import android.content.Intent;
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
 * MainActivity - Activity chính của ứng dụng
 * Chứa menu và toolbar chính
 */
public class MainActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private DatabaseHelper databaseHelper;
    private FloatingActionButton fabAdd;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra role từ intent
        String userRole = getIntent().getStringExtra("user_role");
        isAdmin = "admin".equalsIgnoreCase(userRole);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList, this, isAdmin);
        databaseHelper = new DatabaseHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

        // Chỉ hiển thị FAB nếu là admin
        fabAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditProductActivity.class);
            startActivity(intent);
        });

        loadProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Ẩn các menu item không cần thiết nếu không phải admin
        menu.findItem(R.id.action_settings).setVisible(isAdmin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            loadProducts();
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Ngăn người dùng quay lại màn hình login bằng nút back
        logout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        databaseHelper.getAllProducts(new DatabaseHelper.OnProductsResultListener() {
            @Override
            public void onProductsLoaded(final List<Product> products) {
                runOnUiThread(() -> {
                    productList.clear();
                    productList.addAll(products);
                    productAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onProductsError(final String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error loading products: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onItemClick(Product product) {
        if (isAdmin) {
            Intent intent = new Intent(MainActivity.this, AddEditProductActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onEditClick(Product product) {
        if (isAdmin) {
            Intent intent = new Intent(MainActivity.this, AddEditProductActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDeleteClick(Product product) {
        if (isAdmin) {
            databaseHelper.deleteProduct(product.getId(), new DatabaseHelper.OnOperationResultListener() {
                @Override
                public void onOperationSuccess(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadProducts();
                    });
                }

                @Override
                public void onOperationFailed(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error deleting product: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
}