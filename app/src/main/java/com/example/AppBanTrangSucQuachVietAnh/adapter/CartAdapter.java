package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.model.CartItem;
import com.example.AppBanTrangSucQuachVietAnh.database.DatabaseManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách sản phẩm trong giỏ hàng
 * Xử lý hiển thị thông tin sản phẩm và các sự kiện tương tác
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private static final String TAG = "CartAdapter";
    private List<CartItem> cartItems;
    private final NumberFormat currencyFormat;
    private OnItemClickListener listener;

    /**
     * Interface định nghĩa các sự kiện tương tác với item trong giỏ hàng
     */
    public interface OnItemClickListener {
        void onUpdateQuantity(int position, int newQuantity);
        void onRemoveItem(int position);
    }

    /**
     * Constructor của CartAdapter
     * @param cartItems Danh sách sản phẩm trong giỏ hàng
     * @param listener Listener xử lý các sự kiện
     */
    public CartAdapter(List<CartItem> cartItems, OnItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void updateData(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    /**
     * Tạo ViewHolder mới
     * @param parent ViewGroup chứa các item
     * @param viewType Loại view
     * @return ViewHolder mới
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Gắn dữ liệu vào ViewHolder
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, position, listener);
    }

    /**
     * Lấy số lượng item trong danh sách
     * @return Số lượng item
     */
    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * ViewHolder cho item trong giỏ hàng
     * Chứa các view hiển thị thông tin sản phẩm và các nút điều khiển
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView nameTextView;
        private TextView priceTextView;
        private TextView quantityTextView;
        private Button btnRemove;
        private Button btnUpdate;

        /**
         * Constructor của ViewHolder
         * @param itemView View của item
         */
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
        }

        /**
         * Gắn dữ liệu sản phẩm vào các view
         * @param item Sản phẩm trong giỏ hàng
         * @param position Vị trí của item trong danh sách
         * @param listener Listener xử lý các sự kiện
         */
        public void bind(CartItem item, int position, OnItemClickListener listener) {
            // Hiển thị hình ảnh sản phẩm
            if (item.getProductImage() != null) {
                imageView.setImageBitmap(DatabaseManager.byteArrayToBitmap(item.getProductImage()));
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }

            // Hiển thị thông tin sản phẩm
            nameTextView.setText(item.getProductName());
            priceTextView.setText(currencyFormat.format(item.getProductPrice()));
            quantityTextView.setText(String.valueOf(item.getQuantity()));

            // Xử lý sự kiện nút xóa
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(position);
                }
            });

            // Xử lý sự kiện nút cập nhật số lượng
            btnUpdate.setOnClickListener(v -> {
                EditText editQuantity = new EditText(itemView.getContext());
                editQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                editQuantity.setText(String.valueOf(item.getQuantity()));

                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle("Cập nhật số lượng");
                builder.setView(editQuantity);
                builder.setPositiveButton("Cập nhật", (dialog, which) -> {
                    String quantityStr = editQuantity.getText().toString();
                    if (!quantityStr.isEmpty()) {
                        int newQuantity = Integer.parseInt(quantityStr);
                        if (newQuantity > 0) {
                            listener.onUpdateQuantity(position, newQuantity);
                        } else {
                            Toast.makeText(itemView.getContext(), 
                                "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Hủy", null);
                builder.show();
            });
        }
    }
} 