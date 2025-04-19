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

        /**
         * Constructor của ViewHolder
         * @param itemView View của item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            totalTextView = itemView.findViewById(R.id.totalTextView);
            productImageView = itemView.findViewById(R.id.productImageView);
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
                NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                    .format(orderDetail.getPrice())));
            totalTextView.setText(String.format("Thành tiền: %s", 
                NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                    .format(orderDetail.getPrice() * orderDetail.getQuantity())));

            // Hiển thị hình ảnh sản phẩm
            if (orderDetail.getProductImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                    orderDetail.getProductImage(), 0, orderDetail.getProductImage().length);
                productImageView.setImageBitmap(bitmap);
            } else {
                productImageView.setImageResource(R.drawable.ic_product_placeholder);
            }
        }
    }
} 