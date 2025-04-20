package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onQuantityChange(CartItem cartItem, int newQuantity);
        void onRemoveItem(CartItem cartItem);
    }

    public CartItemAdapter(Context context, List<CartItem> cartItems, OnCartItemClickListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView quantity;
        private final TextView totalPrice;
        private final ImageView btnRemove;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem item, OnCartItemClickListener listener) {
            Jewelry product = item.getProduct();
            if (product != null) {
                productName.setText(product.getName());
                productPrice.setText(String.format("$%.2f", item.getPrice()));
                quantity.setText(String.valueOf(item.getQuantity()));
                totalPrice.setText(String.format("$%.2f", item.getPrice() * item.getQuantity()));

                // Convert byte array to bitmap and set to ImageView
                byte[] imageData = product.getImage();
                if (imageData != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    productImage.setImageBitmap(bitmap);
                } else {
                    productImage.setImageResource(R.drawable.placeholder);
                }

                // Xử lý sự kiện click nút xóa
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveItem(item);
                    }
                });
            }
        }
    }
} 