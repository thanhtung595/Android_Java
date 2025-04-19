package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.model.Order;
import com.example.AppBanTrangSucQuachVietAnh.model.OrderDetail;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public OrderHistoryAdapter(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.textOrderId.setText("Mã đơn hàng: " + order.getId());
        holder.textOrderDate.setText("Ngày đặt: " + dateFormat.format(order.getCreatedAt()));
        holder.textTotalAmount.setText("Tổng tiền: " + currencyFormat.format(order.getTotalAmount()));
        holder.textStatus.setText("Trạng thái: " + order.getStatus());

        holder.itemView.setOnClickListener(v -> showOrderDetails(order));
    }

    private void showOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();
        for (OrderDetail detail : order.getOrderDetails()) {
            details.append(detail.getProductName())
                   .append(" - Số lượng: ")
                   .append(detail.getQuantity())
                   .append(" - Giá: ")
                   .append(currencyFormat.format(detail.getPrice()))
                   .append("\n");
        }

        new AlertDialog.Builder(context)
                .setTitle("Chi tiết đơn hàng #" + order.getId())
                .setMessage(details.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId;
        TextView textOrderDate;
        TextView textTotalAmount;
        TextView textStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
} 