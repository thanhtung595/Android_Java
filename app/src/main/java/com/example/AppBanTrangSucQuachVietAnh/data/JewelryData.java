package com.example.AppBanTrangSucQuachVietAnh.data;

import com.example.AppBanTrangSucQuachVietAnh.model.Jewelry;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp quản lý dữ liệu sản phẩm (tạm thời lưu trong bộ nhớ)
 * Cung cấp các phương thức CRUD cho danh sách sản phẩm
 */
public class JewelryData {
    // Danh sách chứa tất cả sản phẩm
    private static List<Jewelry> jewelryList = new ArrayList<>();

    /**
     * Lấy danh sách tất cả sản phẩm
     * @return List<Jewelry> danh sách sản phẩm
     */
    public static List<Jewelry> getJewelryList() {
        return jewelryList;
    }

    /**
     * Thêm sản phẩm mới vào danh sách
     * @param jewelry Sản phẩm cần thêm
     */
    public static void addJewelry(Jewelry jewelry) {
        jewelryList.add(jewelry);
    }

    /**
     * Cập nhật thông tin sản phẩm theo vị trí
     * @param position Vị trí sản phẩm trong danh sách
     * @param jewelry Sản phẩm với thông tin mới
     */
    public static void updateJewelry(int position, Jewelry jewelry) {
        if (position >= 0 && position < jewelryList.size()) {
            jewelryList.set(position, jewelry);
        }
    }

    /**
     * Xóa sản phẩm theo vị trí
     * @param position Vị trí sản phẩm cần xóa
     */
    public static void deleteJewelry(int position) {
        if (position >= 0 && position < jewelryList.size()) {
            jewelryList.remove(position);
        }
    }

    /**
     * Tìm sản phẩm theo ID
     * @param id ID sản phẩm cần tìm
     * @return Jewelry sản phẩm tìm thấy, null nếu không tìm thấy
     */
    public static Jewelry getJewelryById(int id) {
        for (Jewelry jewelry : jewelryList) {
            if (jewelry.getId() == id) {
                return jewelry;
            }
        }
        return null;
    }

    /**
     * Xóa toàn bộ danh sách sản phẩm
     */
    public static void clearJewelryList() {
        jewelryList.clear();
    }
} 