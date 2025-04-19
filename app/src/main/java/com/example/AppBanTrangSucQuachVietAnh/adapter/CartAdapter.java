package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private final NumberFormat currencyFormat;
    private final CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onDeleteClick(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void updateData(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.textName.setText(item.getProductName());
        holder.textPrice.setText(currencyFormat.format(item.getProductPrice()));
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));

        // Load image
        if (item.getProductImage() != null && item.getProductImage().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(item.getProductImage(), 0, item.getProductImage().length);
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_jewelry);
        }

        // Handle quantity changes
        holder.buttonDecrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            if (newQuantity >= 1) {
                listener.onQuantityChanged(item, newQuantity);
            }
        });

        holder.buttonIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            listener.onQuantityChanged(item, newQuantity);
        });

        // Handle delete
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textName;
        public TextView textPrice;
        public TextView textQuantity;
        public ImageButton buttonDecrease;
        public ImageButton buttonIncrease;
        public ImageButton buttonDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
} 