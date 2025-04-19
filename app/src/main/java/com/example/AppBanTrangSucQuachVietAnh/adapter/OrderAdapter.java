package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.model.Order;
import com.example.AppBanTrangSucQuachVietAnh.model.OrderDetail;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private final NumberFormat currencyFormat;
    private final SimpleDateFormat dateFormat;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.textOrderId.setText(String.valueOf(order.getId()));
        holder.textStatus.setText(order.getStatus());
        holder.textDate.setText(dateFormat.format(order.getCreatedAt()));
        holder.textTotalAmount.setText(currencyFormat.format(order.getTotalAmount()));

        // Set up order details RecyclerView
        OrderDetailAdapter detailAdapter = new OrderDetailAdapter(order.getOrderDetails());
        holder.recyclerViewOrderDetails.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewOrderDetails.setAdapter(detailAdapter);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        public TextView textOrderId;
        public TextView textStatus;
        public TextView textDate;
        public TextView textTotalAmount;
        public RecyclerView recyclerViewOrderDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDate = itemView.findViewById(R.id.textDate);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            recyclerViewOrderDetails = itemView.findViewById(R.id.recyclerViewOrderDetails);
        }
    }

    private static class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
        private final List<OrderDetail> details;
        private final NumberFormat currencyFormat;

        public OrderDetailAdapter(List<OrderDetail> details) {
            this.details = details;
            this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        }

        @NonNull
        @Override
        public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
            return new OrderDetailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
            OrderDetail detail = details.get(position);

            holder.textName.setText(detail.getProductName());
            holder.textPrice.setText(currencyFormat.format(detail.getPrice()));
            holder.textQuantity.setText(String.valueOf(detail.getQuantity()));
            holder.textSubtotal.setText(currencyFormat.format(detail.getSubtotal()));

            // Load image
            if (detail.getProductImage() != null && detail.getProductImage().length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(detail.getProductImage(), 0, detail.getProductImage().length);
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_jewelry);
            }
        }

        @Override
        public int getItemCount() {
            return details.size();
        }

        public static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textName;
            public TextView textPrice;
            public TextView textQuantity;
            public TextView textSubtotal;

            public OrderDetailViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                textName = itemView.findViewById(R.id.textName);
                textPrice = itemView.findViewById(R.id.textPrice);
                textQuantity = itemView.findViewById(R.id.textQuantity);
                textSubtotal = itemView.findViewById(R.id.textSubtotal);
            }
        }
    }
} 