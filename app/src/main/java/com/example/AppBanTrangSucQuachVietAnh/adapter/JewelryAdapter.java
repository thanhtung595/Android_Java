package com.example.AppBanTrangSucQuachVietAnh.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.AppBanTrangSucQuachVietAnh.R;
import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import android.content.Context;
import java.util.ArrayList;

/**
 * Adapter để hiển thị các sản phẩm trang sức trong RecyclerView
 * Xử lý hiển thị thông tin sản phẩm và các thao tác dành cho admin
 */
public class JewelryAdapter extends RecyclerView.Adapter<JewelryAdapter.ViewHolder> {
    private static final String TAG = "JewelryAdapter";
    private List<Jewelry> jewelryList;
    private final Context context;
    private final boolean isAdmin;
    private final OnDeleteClickListener deleteListener;
    private final OnItemClickListener itemClickListener;
    private NumberFormat currencyFormat;

    /**
     * Interface để xử lý các sự kiện click
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(Jewelry jewelry);    // Xử lý khi click nút xóa
    }

    public interface OnItemClickListener {
        void onItemClick(Jewelry jewelry);      // Xử lý khi click vào item
    }

    /**
     * Constructor của JewelryAdapter
     * @param context Context của Activity
     * @param isAdmin Boolean kiểm soát tính năng admin
     * @param deleteListener Listener xử lý sự kiện xóa
     * @param itemClickListener Listener xử lý sự kiện click vào item
     */
    public JewelryAdapter(Context context, boolean isAdmin, OnDeleteClickListener deleteListener, OnItemClickListener itemClickListener) {
        this.context = context;
        this.isAdmin = isAdmin;
        this.deleteListener = deleteListener;
        this.itemClickListener = itemClickListener;
        this.jewelryList = new ArrayList<>();
        // Khởi tạo định dạng tiền tệ Việt Nam
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setJewelryList(List<Jewelry> newList) {
        this.jewelryList = newList;
        notifyDataSetChanged();
    }

    public void updateData(List<Jewelry> newList) {
        this.jewelryList.clear();
        this.jewelryList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo layout cho item
        View view = LayoutInflater.from(context).inflate(R.layout.item_jewelry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Jewelry jewelry = jewelryList.get(position);
        Log.d(TAG, "Đang gán dữ liệu cho: " + jewelry.toString());

        // Gán tên sản phẩm
        holder.textName.setText(jewelry.getName());

        // Định dạng và gán giá
        String formattedPrice = currencyFormat.format(jewelry.getPrice());
        holder.textPrice.setText(formattedPrice);

        // Gán danh mục
        holder.textCategory.setText("Danh mục: " + jewelry.getCategory());

        // Gán thông tin tồn kho
        holder.textStock.setText("Tồn kho: " + jewelry.getStock());

        // Gán mô tả
        holder.textDescription.setText(jewelry.getDescription());

        // Xử lý hiển thị hình ảnh
        if (jewelry.getImage() != null && jewelry.getImage().length > 0) {
            Log.d(TAG, "Đang tải hình ảnh cho: " + jewelry.getName());
            try {
                // Kiểm tra kích thước ảnh trước khi tải
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(jewelry.getImage(), 0, jewelry.getImage().length, options);
                
                Log.d(TAG, "Kích thước ảnh: " + options.outWidth + "x" + options.outHeight);
                
                // Giải mã ảnh thật
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(jewelry.getImage(), 0, jewelry.getImage().length, options);
                
                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap);
                    Log.d(TAG, "Tải ảnh thành công");
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_jewelry);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải ảnh: " + e.getMessage());
                holder.imageView.setImageResource(R.drawable.ic_jewelry);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_jewelry);
        }

        // Xử lý click vào item
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(jewelry);
            }
        });

        // Hiển thị/ẩn nút xóa dựa trên quyền admin
        if (isAdmin) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(jewelry);
                }
            });
        } else {
            holder.buttonDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return jewelryList.size();
    }

    /**
     * ViewHolder chứa các view cho item sản phẩm
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;      // Hình ảnh sản phẩm
        public TextView textName;        // Tên sản phẩm
        public TextView textPrice;       // Giá sản phẩm
        public TextView textStock;       // Số lượng tồn kho
        public TextView textCategory;    // Danh mục sản phẩm
        public TextView textDescription; // Mô tả sản phẩm
        public Button buttonDelete;      // Nút xóa (chỉ cho admin)

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Khởi tạo các view
            imageView = itemView.findViewById(R.id.imageView);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textStock = itemView.findViewById(R.id.textStock);
            textCategory = itemView.findViewById(R.id.textCategory);
            textDescription = itemView.findViewById(R.id.textDescription);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
} 