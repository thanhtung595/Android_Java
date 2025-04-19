package com.example.AppBanTrangSucQuachVietAnh;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách sản phẩm trang sức
 * Xử lý hiển thị thông tin sản phẩm và các sự kiện tương tác
 */
public class JewelryAdapter extends RecyclerView.Adapter<JewelryAdapter.JewelryViewHolder> {

    private static final String TAG = "JewelryAdapter";
    private List<Jewelry> jewelryList;
    private OnItemClickListener listener;

    /**
     * Interface định nghĩa sự kiện khi click vào một sản phẩm
     */
    public interface OnItemClickListener {
        void onItemClick(Jewelry jewelry);
        void onDeleteClick(Jewelry jewelry);
    }

    /**
     * Constructor của JewelryAdapter
     * @param jewelryList Danh sách sản phẩm cần hiển thị
     */
    public JewelryAdapter(List<Jewelry> jewelryList) {
        this.jewelryList = jewelryList;
    }

    /**
     * Cập nhật danh sách sản phẩm
     * @param newList Danh sách sản phẩm mới
     */
    public void setJewelryList(List<Jewelry> newList) {
        this.jewelryList = newList;
        notifyDataSetChanged();
    }

    /**
     * Tạo ViewHolder mới
     * @param parent ViewGroup chứa các item
     * @param viewType Loại view
     * @return ViewHolder mới
     */
    @NonNull
    @Override
    public JewelryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jewelry, parent, false);
        return new JewelryViewHolder(view);
    }

    /**
     * Gắn dữ liệu vào ViewHolder
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
    @Override
    public void onBindViewHolder(@NonNull JewelryViewHolder holder, int position) {
        Jewelry jewelry = jewelryList.get(position);
        holder.textName.setText(jewelry.getName());
        holder.textPrice.setText(String.format("%,.0f VNĐ", jewelry.getPrice()));
        holder.textStock.setText(String.format("Số lượng: %d", jewelry.getStock()));

        // Load image
        if (jewelry.getImage() != null && jewelry.getImage().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(jewelry.getImage(), 0, jewelry.getImage().length);
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(jewelry);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(jewelry);
            }
        });
    }

    /**
     * Lấy số lượng item trong danh sách
     * @return Số lượng item
     */
    @Override
    public int getItemCount() {
        return jewelryList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * ViewHolder cho item sản phẩm
     * Chứa các view hiển thị thông tin sản phẩm
     */
    public static class JewelryViewHolder extends RecyclerView.ViewHolder {
        public TextView textName;
        public TextView textPrice;
        public TextView textStock;
        public ImageView imageView;
        public Button buttonDelete;

        /**
         * Constructor của ViewHolder
         * @param itemView View của item
         */
        public JewelryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textStock = itemView.findViewById(R.id.textStock);
            imageView = itemView.findViewById(R.id.imageView);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
} 