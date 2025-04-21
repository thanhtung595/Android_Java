package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.model.OrderItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.OrderProductViewHolder> {

    private Context context;
    private List<OrderItem> orderItems;

    public OrderProductAdapter(Context context, List<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderProductViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);
        holder.bind(orderItem);
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    public void updateOrderItems(List<OrderItem> newOrderItems) {
        this.orderItems = newOrderItems;
        notifyDataSetChanged();
    }

    class OrderProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProduct;
        private TextView tvProductName;
        private TextView tvQuantity;
        private TextView tvPrice;

        OrderProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        void bind(OrderItem orderItem) {
            tvProductName.setText(orderItem.getProduct().getName());
            tvQuantity.setText(String.format("Số lượng: %d", orderItem.getQuantity()));
            
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvPrice.setText(format.format(orderItem.getPrice()));

            Glide.with(context)
                .load(orderItem.getProduct().getImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(ivProduct);
        }
    }
} 