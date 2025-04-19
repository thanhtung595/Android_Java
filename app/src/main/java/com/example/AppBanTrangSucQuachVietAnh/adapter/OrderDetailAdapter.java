package com.example.AppBanTrangSucQuachVietAnh.adapter;

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
import com.example.AppBanTrangSucQuachVietAnh.model.OrderDetail;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị chi tiết đơn hàng
 * Xử lý hiển thị thông tin sản phẩm trong đơn hàng
 */
public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private static final String TAG = "OrderDetailAdapter";
    private List<OrderDetail> orderDetails;
    private final NumberFormat currencyFormat;

    /**
     * Constructor của OrderDetailAdapter
     * @param orderDetails Danh sách chi tiết đơn hàng
     */
    public OrderDetailAdapter(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    /**
     * Tạo ViewHolder mới
     * @param parent ViewGroup chứa các item
     * @param viewType Loại view
     * @return ViewHolder mới
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Gắn dữ liệu vào ViewHolder
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetails.get(position);
        holder.bind(orderDetail);
    }

    /**
     * Lấy số lượng item trong danh sách
     * @return Số lượng item
     */
    @Override
    public int getItemCount() {
        return orderDetails.size();
    }

    /**
     * ViewHolder cho item chi tiết đơn hàng
     * Chứa các view hiển thị thông tin sản phẩm trong đơn hàng
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productNameTextView;
        private TextView quantityTextView;
        private TextView priceTextView;
        private TextView totalTextView;
        private ImageView productImageView;
        private NumberFormat currencyFormat;

        /**
         * Constructor của ViewHolder
         * @param itemView View của item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.textProductName);
            quantityTextView = itemView.findViewById(R.id.textQuantity);
            priceTextView = itemView.findViewById(R.id.textPrice);
            totalTextView = itemView.findViewById(R.id.textSubtotal);
            productImageView = itemView.findViewById(R.id.imageView);
            currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        }

        /**
         * Gắn dữ liệu chi tiết đơn hàng vào các view
         * @param orderDetail Chi tiết đơn hàng cần hiển thị
         */
        public void bind(OrderDetail orderDetail) {
            // Hiển thị thông tin sản phẩm
            productNameTextView.setText(orderDetail.getProductName());
            quantityTextView.setText(String.format("Số lượng: %d", orderDetail.getQuantity()));
            priceTextView.setText(String.format("Đơn giá: %s", 
                currencyFormat.format(orderDetail.getPrice())));
            totalTextView.setText(String.format("Thành tiền: %s", 
                currencyFormat.format(orderDetail.getPrice() * orderDetail.getQuantity())));

            // Hiển thị hình ảnh sản phẩm
            if (orderDetail.getProductImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                    orderDetail.getProductImage(), 0, orderDetail.getProductImage().length);
                productImageView.setImageBitmap(bitmap);
            } else {
                productImageView.setImageResource(R.drawable.placeholder);
            }
        }
    }
} 