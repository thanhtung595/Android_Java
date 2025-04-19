package com.example.AppBanTrangSucQuachVietAnh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.text.InputType;

import com.example.AppBanTrangSucQuachVietAnh.adapter.JewelryAdapter;
import com.example.AppBanTrangSucQuachVietAnh.data.DatabaseManager;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Activity chính của ứng dụng, hiển thị danh sách sản phẩm và các chức năng chính
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private JewelryAdapter adapter;
    private List<Jewelry> jewelryList;
    private DatabaseManager dbManager;
    private TextView textEmpty;
    private SharedPreferences preferences;
    private int userId;
    private String userRole;
    private boolean isAdmin = false;
    private ProgressDialog progressDialog;
    private View progressBar;
    private View emptyView;
    private SearchView searchView;
    private MenuItem searchMenuItem;

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
        
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate started");

        // Khởi tạo các thành phần
        initializeComponents();
        
        // Lấy thông tin user từ SharedPreferences
        loadUserInfo();
        
        // Khởi tạo RecyclerView và Adapter
        setupRecyclerView();
        
        // Tải danh sách sản phẩm
        loadJewelryList();
    }

    /**
     * Khởi tạo các thành phần giao diện và đối tượng cần thiết
     */
    private void initializeComponents() {
        recyclerView = findViewById(R.id.recyclerView);
        dbManager = DatabaseManager.getInstance();
        jewelryList = new ArrayList<>();
        textEmpty = findViewById(R.id.textEmpty);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
    }

    /**
     * Lấy thông tin user từ SharedPreferences
     */
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Log.e(TAG, "Không tìm thấy user_id trong SharedPreferences");
            finish();
        }
        userRole = prefs.getString("user_role", "");
        isAdmin = "admin".equals(userRole);
        Log.d(TAG, "Quyền người dùng: " + userRole + ", là admin: " + isAdmin);
    }

    /**
     * Thiết lập RecyclerView và Adapter
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new JewelryAdapter(
            this, 
            isAdmin,
            // Xử lý sự kiện xóa
            jewelry -> {
                new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteJewelry(jewelry))
                    .setNegativeButton("Hủy", null)
                    .show();
            },
            // Xử lý sự kiện click vào item
            jewelry -> {
                Log.d(TAG, "Click vào sản phẩm: " + jewelry.getId());
                if (isAdmin) {
                    Intent intent = new Intent(this, EditJewelryActivity.class);
                    intent.putExtra("jewelry", jewelry);
                    startActivity(intent);
                } else {
                    showAddToCartDialog(jewelry);
                }
            }
        );
        recyclerView.setAdapter(adapter);
    }

    /**
     * Tải danh sách sản phẩm từ database
     */
    private void loadJewelryList() {
        Log.d(TAG, "Bắt đầu tải danh sách sản phẩm");
        
        // Hiển thị loading trên main thread
        runOnUiThread(() -> showLoading());

        new Thread(() -> {
            try {
                // Kiểm tra kết nối database
                if (dbManager == null) {
                    Log.e(TAG, "DatabaseManager chưa được khởi tạo");
                    runOnUiThread(() -> {
                        hideLoading();
                        showError("Lỗi kết nối cơ sở dữ liệu");
                    });
                    return;
                }

                // Kiểm tra kết nối
                if (!dbManager.checkConnection()) {
                    Log.e(TAG, "Không thể kết nối đến cơ sở dữ liệu");
                    runOnUiThread(() -> {
                        hideLoading();
                        showError("Không thể kết nối đến cơ sở dữ liệu");
                    });
                    return;
                }

                Log.d(TAG, "Đang lấy danh sách sản phẩm từ database");
                List<Jewelry> jewelryList = dbManager.getAllJewelry();
                Log.d(TAG, "Số lượng sản phẩm lấy được: " + (jewelryList != null ? jewelryList.size() : 0));

                // Cập nhật UI trên main thread
                runOnUiThread(() -> {
                    hideLoading();
                    if (jewelryList == null || jewelryList.isEmpty()) {
                        Log.d(TAG, "Không có sản phẩm nào");
                        showEmptyView();
                    } else {
                        Log.d(TAG, "Hiển thị " + jewelryList.size() + " sản phẩm");
                        hideEmptyView();
                        this.jewelryList.clear();
                        this.jewelryList.addAll(jewelryList);
                        adapter.setJewelryList(this.jewelryList);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi tải danh sách sản phẩm: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    hideLoading();
                    showError("Lỗi tải danh sách sản phẩm: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume bắt đầu");
        // Tải lại dữ liệu khi activity được khôi phục
        new Thread(this::loadJewelryList).start();
    }

    /**
     * Xử lý các lựa chọn menu (tìm kiếm, lọc, đăng xuất)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            // Xóa thông tin đăng nhập
            SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            
            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_history) {
            // Chuyển đến màn hình lịch sử mua hàng
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_cart) {
            // Chuyển đến màn hình giỏ hàng
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_search) {
            showSearchDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * Tạo menu tùy chọn với các chức năng tìm kiếm, lọc và đăng xuất
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        // Thiết lập SearchView
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        setupSearchView();
        
        return true;
    }

    /**
     * Thiết lập SearchView cho chức năng tìm kiếm
     */
    private void setupSearchView() {
        searchView.setQueryHint("Nhập tên sản phẩm...");
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchJewelry(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadJewelryList();
                }
                return true;
            }
        });
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     */
    private void searchJewelry(String keyword) {
        if (keyword.trim().isEmpty()) {
            loadJewelryList();
            return;
        }

        new Thread(() -> {
            try {
                List<Jewelry> searchResults = dbManager.searchJewelry(keyword);
                runOnUiThread(() -> {
                    if (searchResults != null && !searchResults.isEmpty()) {
                        jewelryList.clear();
                        jewelryList.addAll(searchResults);
                        adapter.setJewelryList(jewelryList);
                        textEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        jewelryList.clear();
                        textEmpty.setText("Không tìm thấy sản phẩm phù hợp");
                        textEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi tìm kiếm: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadJewelryList();
                });
            }
        }).start();
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tìm kiếm sản phẩm");

        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // Tạo EditText để nhập tên sản phẩm
        EditText editSearch = new EditText(this);
        editSearch.setHint("Nhập tên sản phẩm");
        editSearch.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(editSearch);

        builder.setView(layout);

        // Thêm nút tìm kiếm
        builder.setPositiveButton("Tìm kiếm", (dialog, which) -> {
            String keyword = editSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchJewelry(keyword);
            } else {
                Toast.makeText(this, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        // Thêm nút hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Xóa sản phẩm khỏi cơ sở dữ liệu
     */
    private void deleteJewelry(Jewelry jewelry) {
        Log.d(TAG, "Bắt đầu xóa sản phẩm: " + jewelry.getId());
        new Thread(() -> {
            try {
                if (dbManager.deleteJewelry(jewelry.getId())) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        // Đóng kết nối hiện tại
                        dbManager.close();
                        // Tạo kết nối mới
                        dbManager = DatabaseManager.getInstance();
                        // Tải lại dữ liệu
                        loadJewelryList();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xóa sản phẩm: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Hiển thị dialog thêm sản phẩm vào giỏ hàng
     * @param jewelry Sản phẩm cần thêm vào giỏ hàng
     */
    private void showAddToCartDialog(Jewelry jewelry) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView textProductName = dialogView.findViewById(R.id.textProductName);
        TextView textProductPrice = dialogView.findViewById(R.id.textProductPrice);
        TextView textStock = dialogView.findViewById(R.id.textStock);
        EditText editQuantity = dialogView.findViewById(R.id.editQuantity);
        ImageButton buttonDecrease = dialogView.findViewById(R.id.buttonDecrease);
        ImageButton buttonIncrease = dialogView.findViewById(R.id.buttonIncrease);
        Button buttonAddToCart = dialogView.findViewById(R.id.buttonAddToCart);

        // Hiển thị thông tin sản phẩm
        textProductName.setText(jewelry.getName());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textProductPrice.setText(currencyFormat.format(jewelry.getPrice()));
        textStock.setText("Còn lại: " + jewelry.getStock());

        // Xử lý tăng giảm số lượng
        buttonDecrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(editQuantity.getText().toString());
            if (quantity > 1) {
                editQuantity.setText(String.valueOf(quantity - 1));
            }
        });

        buttonIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(editQuantity.getText().toString());
            if (quantity < jewelry.getStock()) {
                editQuantity.setText(String.valueOf(quantity + 1));
            }
        });

        // Xử lý thêm vào giỏ hàng
        buttonAddToCart.setOnClickListener(v -> {
            int quantity = Integer.parseInt(editQuantity.getText().toString());
            if (quantity > jewelry.getStock()) {
                Toast.makeText(this, "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show();
                return;
            }

            CartItem cartItem = new CartItem(userId, jewelry.getId(), quantity);
            new Thread(() -> {
                boolean success = dbManager.addToCart(cartItem);
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Lỗi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}