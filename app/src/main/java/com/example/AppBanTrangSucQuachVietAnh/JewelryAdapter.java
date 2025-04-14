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

public class JewelryAdapter extends RecyclerView.Adapter<JewelryAdapter.JewelryViewHolder> {

    private List<Jewelry> jewelryList;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Jewelry jewelry);
        void onDeleteClick(Jewelry jewelry);
    }

    public JewelryAdapter(List<Jewelry> jewelryList) {
        this.jewelryList = jewelryList;
    }

    @NonNull
    @Override
    public JewelryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jewelry, parent, false);
        return new JewelryViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return jewelryList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class JewelryViewHolder extends RecyclerView.ViewHolder {
        public TextView textName;
        public TextView textPrice;
        public TextView textStock;
        public ImageView imageView;
        public Button buttonDelete;

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