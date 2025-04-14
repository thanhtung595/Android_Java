package com.example.AppBanTrangSucQuachVietAnh.data;

import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import java.util.ArrayList;
import java.util.List;

// Lớp quản lý dữ liệu sản phẩm (tạm thời lưu trong bộ nhớ)
public class JewelryData {
    // Danh sách chứa tất cả sản phẩm
    private static List<Jewelry> jewelryList = new ArrayList<>();

    // Lấy danh sách tất cả sản phẩm
    public static List<Jewelry> getJewelryList() {
        return jewelryList;
    }

    // Thêm sản phẩm mới
    public static void addJewelry(Jewelry jewelry) {
        jewelryList.add(jewelry);
    }

    // Cập nhật thông tin sản phẩm
    public static void updateJewelry(int position, Jewelry jewelry) {
        if (position >= 0 && position < jewelryList.size()) {
            jewelryList.set(position, jewelry);
        }
    }

    // Xóa sản phẩm theo ID
    public static void deleteJewelry(int position) {
        if (position >= 0 && position < jewelryList.size()) {
            jewelryList.remove(position);
        }
    }

    // Tìm sản phẩm theo ID
    public static Jewelry getJewelryById(int id) {
        for (Jewelry jewelry : jewelryList) {
            if (jewelry.getId() == id) {
                return jewelry;
            }
        }
        return null;
    }

    public static void clearJewelryList() {
        jewelryList.clear();
    }
} 