package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

/**
 * Adapter cho RecyclerView hiển thị danh sách đơn hàng
 * Xử lý hiển thị thông tin đơn hàng và các sự kiện tương tác
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private static final String TAG = "OrderAdapter";
    private List<Order> orders;
    private final NumberFormat currencyFormat;
    private final SimpleDateFormat dateFormat;
    private OnItemClickListener listener;

    /**
     * Interface định nghĩa các sự kiện tương tác với đơn hàng
     */
    public interface OnItemClickListener {
        void onOrderClick(Order order);
    }

    /**
     * Constructor của OrderAdapter
     * @param orders Danh sách đơn hàng
     * @param listener Listener xử lý các sự kiện
     */
    public OrderAdapter(List<Order> orders, OnItemClickListener listener) {
        this.orders = orders;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    /**
     * Tạo ViewHolder mới
     * @param parent ViewGroup chứa các item
     * @param viewType Loại view
     * @return ViewHolder mới
     */
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view, currencyFormat, dateFormat);
    }

    /**
     * Gắn dữ liệu vào ViewHolder
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);

        // Set up order details RecyclerView
        OrderDetailAdapter detailAdapter = new OrderDetailAdapter(order.getOrderDetails());
        holder.recyclerViewOrderDetails.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewOrderDetails.setAdapter(detailAdapter);
    }

    /**
     * Lấy số lượng item trong danh sách
     * @return Số lượng item
     */
    @Override
    public int getItemCount() {
        return orders.size();
    }

    /**
     * ViewHolder cho item đơn hàng
     * Chứa các view hiển thị thông tin đơn hàng
     */
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        public TextView textOrderId;
        public TextView textStatus;
        public TextView textDate;
        public TextView textTotalAmount;
        public RecyclerView recyclerViewOrderDetails;
        private final NumberFormat currencyFormat;
        private final SimpleDateFormat dateFormat;

        /**
         * Constructor của ViewHolder
         * @param itemView View của item
         */
        public OrderViewHolder(@NonNull View itemView, NumberFormat currencyFormat, SimpleDateFormat dateFormat) {
            super(itemView);
            this.currencyFormat = currencyFormat;
            this.dateFormat = dateFormat;
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDate = itemView.findViewById(R.id.textDate);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            recyclerViewOrderDetails = itemView.findViewById(R.id.recyclerViewOrderDetails);
        }

        /**
         * Gắn dữ liệu đơn hàng vào các view
         * @param order Đơn hàng cần hiển thị
         * @param listener Listener xử lý các sự kiện
         */
        public void bind(Order order, OnItemClickListener listener) {
            // Hiển thị thông tin đơn hàng
            textOrderId.setText(String.valueOf(order.getId()));
            textStatus.setText(order.getStatus());
            textDate.setText(dateFormat.format(order.getCreatedAt()));
            textTotalAmount.setText(currencyFormat.format(order.getTotalAmount()));
            
            // Hiển thị trạng thái đơn hàng
            String statusText;
            int statusColor;
            switch (order.getStatus()) {
                case Order.STATUS_PENDING:
                    statusText = "Đang xử lý";
                    statusColor = Color.YELLOW;
                    break;
                case Order.STATUS_COMPLETED:
                    statusText = "Đã hoàn thành";
                    statusColor = Color.GREEN;
                    break;
                case Order.STATUS_CANCELLED:
                    statusText = "Đã hủy";
                    statusColor = Color.RED;
                    break;
                default:
                    statusText = "Không xác định";
                    statusColor = Color.GRAY;
            }
            textStatus.setText(statusText);
            textStatus.setTextColor(statusColor);

            // Xử lý sự kiện click vào đơn hàng
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
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