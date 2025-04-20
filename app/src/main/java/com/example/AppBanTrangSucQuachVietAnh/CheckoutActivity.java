package com.example.AppBanTrangSucQuachVietAnh;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {
    private TextView tvTotalPrice;
    private EditText etName, etPhone, etAddress;
    private Button btnPlaceOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize views
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Get cart items from intent
        List<CartItem> cartItems = (List<CartItem>) getIntent().getSerializableExtra("cart_items");
        double totalPrice = calculateTotalPrice(cartItems);
        tvTotalPrice.setText(formatPrice(totalPrice));

        // Setup place order button
        btnPlaceOrder.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String address = etAddress.getText().toString();

            if (validateInput(name, phone, address)) {
                // TODO: Implement order placement
            }
        });
    }

    private double calculateTotalPrice(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    private String formatPrice(double price) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(price);
    }

    private boolean validateInput(String name, String phone, String address) {
        if (name.isEmpty()) {
            etName.setError("Please enter your name");
            return false;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Please enter your phone number");
            return false;
        }
        if (address.isEmpty()) {
            etAddress.setError("Please enter your address");
            return false;
        }
        return true;
    }
} 